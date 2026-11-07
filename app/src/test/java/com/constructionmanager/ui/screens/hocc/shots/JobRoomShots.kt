package com.constructionmanager.ui.screens.hocc.shots

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.android.resources.Density
import com.constructionmanager.ui.screens.hocc.Board
import com.constructionmanager.ui.screens.hocc.FaceHeader
import com.constructionmanager.ui.screens.hocc.FaceJobsList
import com.constructionmanager.ui.screens.hocc.FaceStage
import com.constructionmanager.ui.screens.hocc.JobPlan
import com.constructionmanager.ui.screens.hocc.JobRoomFace
import com.constructionmanager.ui.screens.hocc.Plan
import com.constructionmanager.ui.screens.hocc.RoomData
import com.constructionmanager.ui.screens.hocc.RoomFaces
import com.constructionmanager.ui.screens.hocc.Tonight
import com.constructionmanager.ui.screens.hocc.dayLine
import com.constructionmanager.ui.screens.hocc.objects
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.time.LocalDate

/*
 * Pictures of the real job room composables, drawn on the JVM by Paparazzi, from a snapshot of Tyler's office record.
 *
 * The record never goes into the repo: the snapshot is read from the directory named by HOCC_SHOT_DATA (on the Caroline box), and
 * every test is skipped where that directory is absent (CI). Each job file holds the room's reads in the shape the app reads them
 * (projects/{id}, hours, materials, estimate, tasks, schedule); jobs.json is the jobs list.
 */
abstract class ShotBase(widthPx: Int, heightPx: Int) {
    @get:Rule val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(screenWidth = widthPx, screenHeight = heightPx, xdpi = 480, ydpi = 480, density = Density.XXHIGH, softButtons = false),
        renderingMode = SessionParams.RenderingMode.NORMAL,
        showSystemUi = false
    )

    private val dir: File? = System.getenv("HOCC_SHOT_DATA")?.let { File(it) }?.takeIf { it.isDirectory }
    private val today: LocalDate = System.getenv("HOCC_SHOT_TODAY")?.let { LocalDate.parse(it) } ?: LocalDate.now()

    private fun read(name: String): JsonObject? = dir?.let { File(it, name) }?.takeIf { it.isFile }?.readText()?.let { JsonParser.parseString(it).asJsonObject }

    private fun obj(j: JsonObject, k: String): JsonObject? = j.get(k)?.takeIf { it.isJsonObject }?.asJsonObject

    private fun room(j: JsonObject): RoomData = RoomData(
        id = j.get("id").asInt, card = obj(j, "card"), client = null, billing = obj(j, "billing"),
        schedule = obj(j, "schedule"), legacySchedule = null, hours = j.get("hours"), clock = null, takeoffs = null, takeoff = null,
        estimates = null, estimate = j.get("estimate"), materials = j.get("materials"), attachments = j.get("attachments"), tasks = j.get("tasks"),
        thread = null, comms = obj(j, "comms")
    )

    private fun plan(d: RoomData): Plan? = d.schedule?.let { JobPlan.fromSchedule(d.id, d.card?.get("name")?.asString ?: "Job", it).input }
        ?.let { JobPlan.compute(it, today) }

    /*
     * Paparazzi shrinks every picture to 1000 px on its long side, so a whole folded stack in one picture is too small to read.
     * A folded shot is drawn as one tall page (pageDp high) and photographed one screen at a time: part k shows the page from
     * k screens down.
     */
    @Composable
    private fun Paged(part: Int?, pageDp: Int, screenDp: Int, content: @Composable () -> Unit) {
        if (part == null) { content(); return }
        Box(Modifier.wrapContentHeight(align = Alignment.Top, unbounded = true).requiredHeight(pageDp.dp).offset(y = (-part * screenDp).dp)) { content() }
    }

    protected fun shoot(file: String, wide: Boolean, part: Int? = null, pageDp: Int = 2600, screenDp: Int = 640) {
        assumeTrue("no record snapshot here", dir != null)
        val j = read(file)
        assumeTrue("no $file", j != null)
        val jobs = read("jobs.json")?.getAsJsonArray("projects")?.objects().orEmpty()
        val d = room(j!!)
        val p = plan(d)
        val plans = listOfNotNull(p)
        val tonight = Tonight.build(emptyList(), emptyList(),
            listOfNotNull(p?.let { Tonight.JobPlanRow(d.card!!, it, null, needsStart = false) }), emptyList(), today, emptySet())
        val face = RoomFaces.build(d, p, plans, tonight, today)
        val count = RoomFaces.list(jobs, today).filter { it.first != FaceStage.Lead }.sumOf { it.second.size }
        val header = FaceHeader(today, count, dayLine(plans, today), null, tonight.size)
        paparazzi.snapshot {
            MaterialTheme(colorScheme = Board.colors, typography = Board.typography) {
                Paged(part, pageDp, screenDp) { JobRoomFace(face, header, wide = wide, onOpen = {}, onJobs = {}, onTonight = {}) }
            }
        }
    }

    protected fun shootList(part: Int? = null, pageDp: Int = 1920, screenDp: Int = 640) {
        assumeTrue("no record snapshot here", dir != null)
        val jobs = read("jobs.json")?.getAsJsonArray("projects")?.objects().orEmpty()
        assumeTrue("no jobs.json", jobs.isNotEmpty())
        paparazzi.snapshot {
            MaterialTheme(colorScheme = Board.colors, typography = Board.typography) {
                Paged(part, pageDp, screenDp) { FaceJobsList(RoomFaces.list(jobs, today), null, "", {}, {}, null, leadsOpen = false, onLeadsOpen = {}) }
            }
        }
    }
}

/** The Fold7 open: about 720 x 800 dp. */
class FoldOpenShots : ShotBase(2160, 2400) {
    @Test fun humphrey() = shoot("job-80.json", wide = true)
    @Test fun humphreyAfterFill() = shoot("job-80-filled.json", wide = true)
    @Test fun ricordati() = shoot("job-1.json", wide = true)
    @Test fun mills() = shoot("job-2.json", wide = true)
}

/** The Fold7 folded: about 412 dp wide, photographed one 640 dp screen at a time. */
class FoldedShots : ShotBase(1236, 1920) {
    @Test fun humphrey0() = shoot("job-80-filled.json", wide = false, part = 0)
    @Test fun humphrey1() = shoot("job-80-filled.json", wide = false, part = 1)
    @Test fun humphrey2() = shoot("job-80-filled.json", wide = false, part = 2)
    @Test fun humphrey3() = shoot("job-80-filled.json", wide = false, part = 3)
    @Test fun jobsTab0() = shootList(part = 0)
    @Test fun jobsTab1() = shootList(part = 1)
    @Test fun jobsTab2() = shootList(part = 2)
}
