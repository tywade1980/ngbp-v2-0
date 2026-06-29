package com.constructionmanager.ai.knowledge

import com.constructionmanager.ai.knowledge.ConstructionKnowledge as KB

/**
 * Deterministic construction Q&A over [ConstructionKnowledge]: price lookups, parametric estimates
 * (assemblies and whole-project ballparks), labor rates, and how-to/process guidance. Every figure
 * is grounded in the bundled data — no model guessing — so answers are consistent and citeable as
 * "ballpark national averages." Each entry point returns null when nothing matches, letting the
 * caller fall through to the LLM/offline assistant.
 */
object ConstructionEstimator {

    private val numberRegex = Regex("""([0-9][0-9,]*(?:\.[0-9]+)?)""")

    fun answer(message: String): String? {
        val m = message.lowercase()
        return when {
            isEstimate(m) -> estimate(m)
            isPriceLookup(m) -> priceLookup(m)
            isPractice(m) -> practice(m)
            else -> null
        }
    }

    private fun isEstimate(m: String) =
        m.contains("estimate") || m.contains("how much to") || m.contains("cost to") ||
            (m.contains("how much") && (matchAssembly(m) != null || matchBenchmark(m) != null)) ||
            (m.contains("budget for") && (matchAssembly(m) != null || matchBenchmark(m) != null))

    private fun isPriceLookup(m: String) =
        (m.contains("price") || m.contains("cost of") || m.contains("how much is") ||
            m.contains("rate") || m.contains("per hour") || m.contains("hourly") || m.contains("wage")) &&
            (matchMaterial(m) != null || matchLabor(m) != null)

    private fun isPractice(m: String) =
        m.contains("how do i") || m.contains("how to") || m.contains("process") ||
            m.contains("steps") || m.contains("sequence") || m.contains("best practice") ||
            m.startsWith("what is") || m.startsWith("what are") || m.contains("explain")

    // --- estimates ------------------------------------------------------------

    private fun estimate(m: String): String? {
        val qty = firstNumber(m)
        val benchmark = matchBenchmark(m)
        val assembly = matchAssembly(m)
        val preferBenchmark = listOf("remodel", "addition", "renovation", "reno", "build a", "new home", "new construction")
            .any { m.contains(it) }

        if (benchmark != null && (preferBenchmark || assembly == null)) {
            if (qty == null) {
                return "${benchmark.name}: ballpark ${money(benchmark.low)}–${money(benchmark.high)} per ${benchmark.unit}. " +
                    "Tell me the size (e.g. \"${benchmark.name.lowercase()} 200 sq ft\") for a total. " +
                    benchmark.note + disclaimer()
            }
            val low = qty * benchmark.low
            val high = qty * benchmark.high
            return buildString {
                appendLine("${benchmark.name} — ${fmt(qty)} ${benchmark.unit}")
                appendLine("Ballpark total: ${money(low)} – ${money(high)}")
                appendLine("(@ ${money(benchmark.low)}–${money(benchmark.high)}/${benchmark.unit})")
                if (benchmark.note.isNotBlank()) appendLine(benchmark.note)
                append(disclaimer())
            }
        }

        if (assembly != null) {
            if (qty == null) {
                return "${assembly.name}: material ${money(assembly.materialLow)}–${money(assembly.materialHigh)} + " +
                    "labor ${money(assembly.laborLow)}–${money(assembly.laborHigh)} per ${assembly.unit}. " +
                    "Give me a quantity (e.g. \"${assembly.keywords.first()} 200\") for a full estimate." + disclaimer()
            }
            val matLow = qty * assembly.materialLow * (1 + KB.WASTE_STANDARD)
            val matHigh = qty * assembly.materialHigh * (1 + KB.WASTE_STANDARD)
            val labLow = qty * assembly.laborLow
            val labHigh = qty * assembly.laborHigh
            val markup = 1 + KB.OVERHEAD + KB.PROFIT
            val totLow = (matLow + labLow) * markup
            val totHigh = (matHigh + labHigh) * markup
            return buildString {
                appendLine("${assembly.name} — ${fmt(qty)} ${assembly.unit}")
                appendLine("• Material (+${pct(KB.WASTE_STANDARD)} waste): ${money(matLow)} – ${money(matHigh)}")
                appendLine("• Labor (${assembly.trade}): ${money(labLow)} – ${money(labHigh)}")
                appendLine("• + ${pct(KB.OVERHEAD)} overhead & ${pct(KB.PROFIT)} profit")
                appendLine("Estimated total: ${money(totLow)} – ${money(totHigh)}")
                if (assembly.note.isNotBlank()) appendLine(assembly.note)
                append(disclaimer())
            }
        }
        return null
    }

    // --- price / rate lookups -------------------------------------------------

    private fun priceLookup(m: String): String? {
        val labor = matchLabor(m)
        if (labor != null && (m.contains("rate") || m.contains("hour") || m.contains("wage") || m.contains("labor"))) {
            return "${labor.trade}: ballpark ${money(labor.low)}–${money(labor.high)}/hr (loaded). " +
                "Loaded labor includes ~${pct(KB.LABOR_BURDEN)} burden over base wage." + disclaimer()
        }
        val material = matchMaterial(m)
        if (material != null) {
            return "${material.name}: ballpark ${money(material.low)}–${money(material.high)} per ${material.unit}." + disclaimer()
        }
        if (labor != null) {
            return "${labor.trade}: ballpark ${money(labor.low)}–${money(labor.high)}/hr (loaded)." + disclaimer()
        }
        return null
    }

    // --- practices ------------------------------------------------------------

    private fun practice(m: String): String? {
        val match = KB.practices.firstOrNull { p -> p.keywords.any { m.contains(it) } } ?: return null
        return "${match.topic}\n\n${match.text}"
    }

    // --- matching helpers -----------------------------------------------------

    private fun matchMaterial(m: String): KB.MaterialCost? =
        KB.materials.firstOrNull { mat -> mat.keywords.any { m.contains(it) } }

    private fun matchLabor(m: String): KB.LaborRate? =
        KB.laborRates.firstOrNull { rate -> rate.keywords.any { m.contains(it) } }

    private fun matchAssembly(m: String): KB.Assembly? =
        KB.assemblies.firstOrNull { a -> a.keywords.any { m.contains(it) } }

    private fun matchBenchmark(m: String): KB.ProjectBenchmark? =
        KB.benchmarks.firstOrNull { b -> b.keywords.any { m.contains(it) } }

    private fun firstNumber(m: String): Double? =
        numberRegex.find(m)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()

    private fun money(v: Double): String = "$%,.0f".format(v)
    private fun fmt(v: Double): String = if (v == v.toLong().toDouble()) "%,d".format(v.toLong()) else "%,.1f".format(v)
    private fun pct(v: Double): String = "${(v * 100).toInt()}%"
    private fun disclaimer(): String = "\n\n(Ballpark U.S. national averages — verify against local quotes.)"
}
