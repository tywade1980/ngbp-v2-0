package com.constructionmanager.ui.screens.hocc

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.material3.Typography
import com.constructionmanager.R

/*
 * THE LOOK - Tyler's "Wade OS Job Board" design file (Oct 9-11 2026; "This is what I expected to see ... near
 * identical", Nov 2 2026). Ground #E9EDEA, ink #1B2229, Caroline blue #1F4E9A, label grey #5B6670, card border
 * #D5DAD4; IBM Plex Sans for words, IBM Plex Mono for numbers, Barlow Condensed for display. The stage colours are
 * the design file's: on site #9A3B12, signed #1F4E9A, estimate out #5A4A9A, site visit #2E6B45.
 */
object Board {
    val ground = Color(0xFFE9EDEA)
    val ink = Color(0xFF1B2229)
    val blue = Color(0xFF1F4E9A)
    val blueDark = Color(0xFF163A73)
    val grey = Color(0xFF5B6670)
    val body = Color(0xFF36414B)
    val border = Color(0xFFD5DAD4)
    val borderSoft = Color(0xFFEEF0EC)
    val toolBorder = Color(0xFFC9CFC8)
    val tile = Color(0xFFF4F6F3)
    val hover = Color(0xFFF2F5F9)
    val dashed = Color(0xFFB9C0B7)
    val green = Color(0xFF2E6B45)
    val rust = Color(0xFF9A3B12)
    val purple = Color(0xFF5A4A9A)
    val red = Color(0xFFB3261E)
    val amberBg = Color(0xFFFFF6E0)
    val amberLine = Color(0xFFE8D9A8)
    val track = Color(0xFFEEF1F4)
    val barDone = Color(0xFF5B6670)
    val barCalendar = Color(0xFF1F4E9A)
    val barProjected = Color(0xFFC9772E)

    val sans = FontFamily(
        Font(R.font.ibm_plex_sans, FontWeight.Normal),
        Font(R.font.ibm_plex_sans_medium, FontWeight.Medium),
        Font(R.font.ibm_plex_sans_semibold, FontWeight.SemiBold),
        Font(R.font.ibm_plex_sans_bold, FontWeight.Bold)
    )
    val mono = FontFamily(Font(R.font.ibm_plex_mono, FontWeight.Normal), Font(R.font.ibm_plex_mono_semibold, FontWeight.SemiBold))
    val disp = FontFamily(Font(R.font.barlow_condensed_semibold, FontWeight.SemiBold), Font(R.font.barlow_condensed_bold, FontWeight.Bold))

    val colors = lightColorScheme(
        primary = blue, onPrimary = Color.White, primaryContainer = Color(0xFFDCE6F5), onPrimaryContainer = ink,
        secondary = ink, onSecondary = Color.White, secondaryContainer = tile, onSecondaryContainer = ink,
        background = ground, onBackground = ink, surface = Color.White, onSurface = ink,
        surfaceVariant = tile, onSurfaceVariant = grey, outline = toolBorder, outlineVariant = border,
        error = red, onError = Color.White
    )

    val typography = Typography(
        headlineMedium = TextStyle(fontFamily = disp, fontWeight = FontWeight.Bold, fontSize = 28.sp),
        headlineSmall = TextStyle(fontFamily = disp, fontWeight = FontWeight.Bold, fontSize = 24.sp),
        titleLarge = TextStyle(fontFamily = disp, fontWeight = FontWeight.Bold, fontSize = 22.sp),
        titleMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
        titleSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
        bodyLarge = TextStyle(fontFamily = sans, fontSize = 16.sp),
        bodyMedium = TextStyle(fontFamily = sans, fontSize = 14.sp),
        bodySmall = TextStyle(fontFamily = sans, fontSize = 12.5.sp),
        labelLarge = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
        labelMedium = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
        labelSmall = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 0.08.em)
    )

    /** The life of a lead, in the record's status words and his: lead -> estimating -> estimate out -> signed -> on site -> punch list -> done. */
    val LIFE: List<Pair<String, String>> = listOf(
        "lead" to "Lead", "estimating" to "Estimating", "quoted" to "Estimate out", "approved" to "Signed, not started",
        "active" to "On site", "on_hold" to "On hold", "punch" to "Punch list", "complete" to "Done", "declined" to "Declined"
    )
    /** Statuses that mean the lead signed: a client's project, on the Clients spoke and off the Leads page. */
    val SIGNED = setOf("approved", "signed", "scheduled", "accepted", "active", "in_progress", "in progress", "underway", "on_site", "started", "on_hold", "hold", "punch", "complete", "done", "completed", "closed", "paid")
    val STAGE_ORDER = listOf("On site", "Punch list", "Signed, not started", "Estimate out", "Estimating", "On hold", "Lead", "Done", "Declined")

    fun stage(status: String?, open: Boolean?): Pair<String, Color> {
        val s = (status ?: "").lowercase()
        return when (s) {
            "active", "in_progress", "in progress", "underway", "on_site", "started" -> "On site" to rust
            "punch" -> "Punch list" to rust
            "approved", "signed", "scheduled", "accepted" -> "Signed, not started" to blue
            "quoted", "sent", "proposal" -> "Estimate out" to purple
            "estimating", "estimate" -> "Estimating" to purple
            "on_hold", "hold" -> "On hold" to grey
            "lead", "new", "site_visit", "consult", "inquiry" -> "Lead" to green
            "complete", "done", "completed", "closed", "archived", "paid" -> "Done" to grey
            "declined", "cancelled", "canceled" -> "Declined" to grey
            else -> if (open == true) "On site" to rust else (humanStatus(status) ?: "Job") to grey
        }
    }
}

private val Double.em: TextUnit get() = TextUnit(this.toFloat(), androidx.compose.ui.unit.TextUnitType.Em)

/** The small uppercase grey label over a value ("WHAT THEY NEED"). */
@Composable
fun Lbl(text: String, color: Color = Board.grey, modifier: Modifier = Modifier) =
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, color = color, modifier = modifier)

/** Display words - Barlow Condensed. */
@Composable
fun Disp(text: String, size: Int = 20, color: Color = Board.ink, modifier: Modifier = Modifier, maxLines: Int = Int.MAX_VALUE) =
    Text(text, fontFamily = Board.disp, fontWeight = FontWeight.Bold, fontSize = size.sp, lineHeight = (size * 1.05).sp, color = color, modifier = modifier, maxLines = maxLines)

/** Numbers - IBM Plex Mono. */
@Composable
fun Num(text: String, size: Int = 13, color: Color = Board.ink, weight: FontWeight = FontWeight.SemiBold, modifier: Modifier = Modifier) =
    Text(text, fontFamily = Board.mono, fontWeight = weight, fontSize = size.sp, color = color, modifier = modifier)

/** A card of the board: white, 1px border, 10dp corners. [strong] draws the design's 2px ink/blue frame. */
@Composable
fun BoardCard(modifier: Modifier = Modifier, strong: Color? = null, padding: Int = 16, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(if (strong != null) 2.dp else 1.dp, strong ?: Board.border, RoundedCornerShape(10.dp))
            .padding(padding.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

/** The card's head: display title left, tool buttons right. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CardHead(title: String, color: Color = Board.ink, tools: @Composable RowScope.() -> Unit = {}) {
    // the title keeps its width; the tools wrap onto their own line when the card is narrow (Tyler, Nov 4 2026: "get rid of the
    // vertical words everywhere" - a title squeezed beside three buttons on the phone wrapped letter by letter)
    androidx.compose.foundation.layout.FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Disp(title, 20, color, Modifier.padding(end = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { tools() }
    }
}

/** The design's "tool" button: white, thin border, small semibold words. */
@Composable
fun Tool(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick, enabled = enabled,
        shape = RoundedCornerShape(5.dp),
        border = BorderStroke(1.dp, Board.toolBorder),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Board.ink),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        modifier = Modifier.heightIn(min = 34.dp)
    ) { Text(text, style = MaterialTheme.typography.labelMedium) }
}

/** The design's dark button ("Clients") and blue button ("Send to Caroline"). */
@Composable
fun Solid(text: String, color: Color = Board.ink, enabled: Boolean = true, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = enabled, shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White, disabledContainerColor = color.copy(alpha = 0.45f), disabledContentColor = Color.White),
        modifier = modifier.heightIn(min = 44.dp)
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

/** The dashed empty-state box. */
@Composable
fun Dashed(text: String) {
    Text(
        text, style = MaterialTheme.typography.bodySmall, color = Board.body,
        modifier = Modifier.fillMaxWidth().border(1.dp, Board.dashed, RoundedCornerShape(6.dp)).padding(12.dp)
    )
}

/** A grey stat tile ("CONTRACT  $30,000"). */
@Composable
fun Tile(label: String, value: String?, color: Color = Board.ink, modifier: Modifier = Modifier, approx: Boolean = false) {
    Column(modifier.clip(RoundedCornerShape(8.dp)).background(Board.tile).padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Lbl(label)
        if (approx) Num("≈", 13, color, FontWeight.Normal)
        Num(value ?: "—", 19, color)
    }
}
