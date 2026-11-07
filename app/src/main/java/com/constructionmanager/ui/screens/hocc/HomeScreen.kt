package com.constructionmanager.ui.screens.hocc

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.constructionmanager.data.location.LocationReader
import com.google.gson.JsonObject

/*
 * HOME IS THE BOARD (Tyler, Nov 2 2026, 20:42: his PDF of the Job Board - "this is what I expected to see... near
 * identical" - against a search list: "if I show you the outcome of what you sent me vs what I said I wanted you could
 * see the difference"). Home opens ON a job: the one he is standing at (the record's places), else the last one he
 * opened on this phone, else the first job on site with an address, else the first open job. The Clients drawer in the
 * board's header is the switcher - one plain search for a name, an address or a phone number. No list screen first.
 */
@Composable
fun HomeScreen(vm: HoccViewModel, room: RoomViewModel, roomKey: RoomKey?, onOpenRoom: (RoomKey) -> Unit) {
    val jobs by vm.jobs.collectAsState()
    val here by room.here.collectAsState()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { room.locate(context) }

    LaunchedEffect(Unit) {
        vm.loadJobs()
        if (LocationReader.hasPermission(context)) room.locate(context)
        else launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
    val all = jobs.data?.objects().orEmpty()
    // a job whose standing changed (archived, removed, reopened, moved on) leaves or rejoins the lists at once
    val jobsChanged by room.jobsChanged.collectAsState()
    LaunchedEffect(jobsChanged) { if (jobsChanged > 0) vm.loadJobs(force = true) }

    // no room chosen yet: choose the one the facts point at, once the jobs are in hand
    val matched = here.matched
    LaunchedEffect(roomKey, all.size, matched?.num("id"), here.reading) {
        if (roomKey != null || all.isEmpty()) return@LaunchedEffect
        val byPlace = matched?.let { keyForPlace(it, all) }
        if (byPlace != null) { onOpenRoom(byPlace); return@LaunchedEffect }
        if (here.reading) return@LaunchedEffect          // give the place match its chance before falling back
        val last = room.lastJob(context)?.let { id -> all.firstOrNull { it.num("id")?.toInt() == id } }
        val pick = last
            ?: all.firstOrNull { it.boolOf("open") == true && Board.stage(it.str("status"), true).first == "On site" && !it.str("address").isNullOrBlank() }
            ?: all.firstOrNull { it.boolOf("open") == true && !it.str("address").isNullOrBlank() }
            ?: all.firstOrNull { it.boolOf("open") == true }
            ?: all.firstOrNull()
        val id = pick?.num("id")?.toInt() ?: return@LaunchedEffect
        onOpenRoom(oneJob(id, all))
    }

    if (roomKey == null) {
        Box(Modifier.fillMaxSize().background(Board.ground)) {
            if (jobs.loading || all.isEmpty() && jobs.error == null) LoadingLine("Opening your board…")
            jobs.error?.let { ErrorLine(it) { vm.loadJobs() } }
            if (!jobs.loading && jobs.error == null && all.isEmpty()) EmptyLine("No jobs on file yet.")
        }
    } else {
        // a deleted job leaves nothing to show: read the jobs again and open the next open one (0.7.4)
        val leave: () -> Unit = {
            vm.loadJobs(force = true)
            all.firstOrNull { it.boolOf("open") == true && it.num("id")?.toInt() !in roomKey.projectIds }?.num("id")?.toInt()
                ?.let { onOpenRoom(oneJob(it, all.filter { p -> p.num("id")?.toInt() !in roomKey.projectIds })) }
        }
        androidx.compose.runtime.CompositionLocalProvider(LocalLeaveRoom provides leave) {
            JobRoomScreen(room, roomKey, all, onBack = null, onOpenRoom = onOpenRoom,
                client = { phone, label, back, openProject -> ClientWheel(vm, phone, label, onBack = back, onOpenProject = openProject) },
                readJson = { path -> vm.readJson(path) })
        }
    }
}

/** The room a known place points at: the jobs at its address, else its tied job, else its client's jobs. */
fun keyForPlace(place: JsonObject, all: List<JsonObject>): RoomKey? {
    val pid = place.num("project_id")?.toInt()
    val phone = place.str("contact_phone")
    val addr = place.str("address_text", "address")
    val atAddr = if (!addr.isNullOrBlank()) all.filter { (it.str("address") ?: "").trim().equals(addr.trim(), ignoreCase = true) } else emptyList()
    val ids = when {
        atAddr.isNotEmpty() -> atAddr.mapNotNull { it.num("id")?.toInt() }
        pid != null -> listOf(pid)
        !phone.isNullOrBlank() -> all.filter { it.str("client_phone", "phone") == phone }.mapNotNull { it.num("id")?.toInt() }
        else -> emptyList()
    }
    if (ids.isEmpty()) return null
    val start = pid?.takeIf { it in ids } ?: all.firstOrNull { it.num("id")?.toInt() in ids && it.boolOf("open") == true }?.num("id")?.toInt() ?: ids.first()
    return RoomKey(RoomKey.ADDRESS, addr?.takeIf { it.isNotBlank() } ?: (place.str("name") ?: "Here"), ids, start)
}

/** One job opened on its own: the room still names the address, and shows the other jobs there as chips. */
fun oneJob(id: Int, all: List<JsonObject>): RoomKey {
    val p = all.firstOrNull { it.num("id")?.toInt() == id }
    val addr = p?.str("address")?.trim()
    val siblings = if (!addr.isNullOrBlank()) all.filter { (it.str("address") ?: "").trim().equals(addr, ignoreCase = true) }.mapNotNull { it.num("id")?.toInt() } else emptyList()
    val ids = if (id in siblings) siblings else listOf(id)
    return RoomKey(if (addr.isNullOrBlank()) RoomKey.JOB else RoomKey.ADDRESS, addr?.takeIf { it.isNotBlank() } ?: (p?.str("client_name") ?: p?.str("name") ?: "Job #$id"), ids, id)
}
