package com.constructionmanager.ui.screens.hocc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

// ------------------------------------------------------------------ reading the record's JSON
//
// The office routes answer in the record's own field names; these helpers read them tolerantly (a field
// that is absent reads as absent, never as a made-up value) so a screen shows what the record holds.

fun JsonElement?.asObjOrNull(): JsonObject? = if (this != null && isJsonObject) asJsonObject else null

fun JsonElement?.asArrOrNull(): JsonArray? = if (this != null && isJsonArray) asJsonArray else null

fun JsonObject.str(vararg keys: String): String? {
    for (k in keys) {
        val e = get(k) ?: continue
        if (e.isJsonPrimitive) {
            val s = runCatching { e.asString }.getOrNull()
            if (!s.isNullOrBlank()) return s
        }
    }
    return null
}

fun JsonObject.num(vararg keys: String): Double? {
    for (k in keys) {
        val e = get(k) ?: continue
        if (e.isJsonPrimitive) {
            val d = runCatching { e.asDouble }.getOrNull()
            if (d != null) return d
        }
    }
    return null
}

fun JsonObject.boolOf(key: String): Boolean? {
    val e = get(key) ?: return null
    return if (e.isJsonPrimitive) runCatching { e.asBoolean }.getOrNull() else null
}

fun JsonObject.arrOf(vararg keys: String): JsonArray? {
    for (k in keys) {
        val e = get(k)
        if (e != null && e.isJsonArray) return e.asJsonArray
    }
    return null
}

fun JsonObject.objOf(key: String): JsonObject? {
    val e = get(key) ?: return null
    return if (e.isJsonObject) e.asJsonObject else null
}

/** Every array-of-objects a payload carries, under its own key - how a day sheet's parts are found without guessing one key. */
fun JsonObject.arraysOfObjects(): List<Pair<String, JsonArray>> =
    entrySet().mapNotNull { (k, v) ->
        if (v.isJsonArray && v.asJsonArray.size() > 0 && v.asJsonArray[0].isJsonObject) k to v.asJsonArray else null
    }

/** A payload that is either an array itself or an object holding rows under a common key. */
fun JsonElement?.rowsOf(vararg keys: String): JsonArray {
    asArrOrNull()?.let { return it }
    val o = asObjOrNull() ?: return JsonArray()
    for (k in keys) o.arrOf(k)?.let { return it }
    o.arraysOfObjects().firstOrNull()?.let { return it.second }
    return JsonArray()
}

fun Iterable<JsonElement>.objects(): List<JsonObject> = mapNotNull { it.asObjOrNull() }

fun JsonArray.objects(): List<JsonObject> = mapNotNull { it.asObjOrNull() }

fun money(value: Double?): String? =
    value?.let { (if (it < 0) "-$" else "$") + String.format("%,.2f", kotlin.math.abs(it)) }

fun countLabel(n: Int, one: String, many: String = one + "s"): String = if (n == 1) "1 $one" else "$n $many"

/** "active" -> "Active", "on_hold" -> "On hold" - the record's words, made readable, never renamed. */
fun humanStatus(raw: String?): String? =
    raw?.replace('_', ' ')?.replaceFirstChar { it.uppercase() }

// ------------------------------------------------------------------ shared pieces

@Composable
fun HoccCard(title: String? = null, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (title != null) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            content()
        }
    }
}

@Composable
fun StatusChip(text: String) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondaryContainer) {
        Text(
            text, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun KvRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun LoadingLine(text: String = "Reading…") {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CircularProgressIndicator(modifier = Modifier.padding(2.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ErrorLine(message: String, onRetry: () -> Unit) {
    HoccCard {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onRetry) { Text("Try again") }
    }
}

@Composable
fun EmptyLine(message: String) {
    Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
