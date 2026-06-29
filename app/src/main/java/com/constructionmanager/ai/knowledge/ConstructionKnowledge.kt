package com.constructionmanager.ai.knowledge

/**
 * A bundled, offline construction knowledge base — practices, processes, common assemblies, and
 * **ballpark national-average** unit costs / labor rates. It ships with the app so the assistant is
 * useful with zero data entry; figures are 2025–2026 U.S. national averages with ranges and are
 * meant as estimates (not live or region-exact). Update [materials]/[laborRates]/[assemblies] as
 * your real numbers come in, or layer a live web-search/pricing source on top.
 *
 * Sources informing the ranges: HomeAdvisor/Angi cost guides, Fixr/estimators.us, HomeGuide,
 * NAHB framing-lumber data, and BLS construction wage data (2025–2026).
 */
object ConstructionKnowledge {

    // Estimating multipliers (ballpark industry norms).
    const val WASTE_STANDARD = 0.10   // cuts/breakage for typical layouts
    const val WASTE_COMPLEX = 0.15    // complex layouts
    const val OVERHEAD = 0.10         // "10-10 rule"
    const val PROFIT = 0.10
    const val LABOR_BURDEN = 0.35     // taxes/insurance/benefits on base wage (30–40%)

    /** A material with a ballpark retail price range, per [unit]. */
    data class MaterialCost(
        val name: String,
        val unit: String,
        val low: Double,
        val high: Double,
        val keywords: List<String>
    )

    /** Loaded (burdened) hourly labor range for a trade. */
    data class LaborRate(val trade: String, val low: Double, val high: Double, val keywords: List<String>)

    /** An installed assembly priced per [unit], split into material and labor ranges. */
    data class Assembly(
        val name: String,
        val unit: String,
        val materialLow: Double,
        val materialHigh: Double,
        val laborLow: Double,
        val laborHigh: Double,
        val trade: String,
        val keywords: List<String>,
        val note: String = ""
    )

    /** A whole-project ballpark, usually per square foot. */
    data class ProjectBenchmark(
        val name: String,
        val low: Double,
        val high: Double,
        val unit: String,
        val keywords: List<String>,
        val note: String = ""
    )

    data class Practice(val topic: String, val text: String, val keywords: List<String>)

    val materials = listOf(
        MaterialCost("2x4x8 stud (SPF)", "each", 3.00, 4.50, listOf("2x4", "stud", "two by four")),
        MaterialCost("2x6x8 (SPF)", "each", 5.00, 8.00, listOf("2x6", "two by six")),
        MaterialCost("1/2\" drywall sheet (4x8)", "sheet", 10.00, 14.00, listOf("drywall", "sheetrock", "gypsum", "wallboard")),
        MaterialCost("5/8\" drywall sheet (4x8)", "sheet", 13.00, 18.00, listOf("5/8 drywall", "type x", "fire rated drywall")),
        MaterialCost("OSB sheathing 7/16\" (4x8)", "sheet", 20.00, 45.00, listOf("osb", "sheathing", "oriented strand")),
        MaterialCost("Plywood 3/4\" (4x8)", "sheet", 30.00, 60.00, listOf("plywood")),
        MaterialCost("Ready-mix concrete", "cubic yard", 135.00, 185.00, listOf("concrete", "ready mix", "redi mix")),
        MaterialCost("Asphalt shingles", "square (100 sq ft)", 100.00, 150.00, listOf("shingle", "asphalt roof")),
        MaterialCost("Fiberglass batt insulation", "sq ft", 0.50, 1.50, listOf("insulation", "batt", "fiberglass")),
        MaterialCost("Interior latex paint", "gallon", 30.00, 60.00, listOf("paint", "latex")),
        MaterialCost("Interior door (hollow-core slab)", "each", 40.00, 150.00, listOf("interior door", "door slab", "door")),
        MaterialCost("#4 rebar", "linear ft", 0.50, 1.00, listOf("rebar", "reinforcing"))
    )

    val laborRates = listOf(
        LaborRate("General laborer", 20.0, 35.0, listOf("laborer", "general labor", "helper")),
        LaborRate("Carpenter", 35.0, 47.0, listOf("carpenter", "framer", "framing", "finish carpenter")),
        LaborRate("Concrete finisher", 25.0, 50.0, listOf("concrete", "finisher", "flatwork")),
        LaborRate("Drywall installer/finisher", 45.0, 67.0, listOf("drywall", "taper", "finisher")),
        LaborRate("Electrician (licensed)", 50.0, 100.0, listOf("electrician", "electrical")),
        LaborRate("Plumber (licensed)", 50.0, 100.0, listOf("plumber", "plumbing")),
        LaborRate("HVAC technician", 45.0, 90.0, listOf("hvac", "mechanical", "heating", "cooling")),
        LaborRate("Painter", 25.0, 45.0, listOf("painter", "painting")),
        LaborRate("Roofer", 40.0, 70.0, listOf("roofer", "roofing")),
        LaborRate("Mason", 40.0, 70.0, listOf("mason", "masonry", "brick", "block")),
        LaborRate("Tile setter", 40.0, 70.0, listOf("tile", "tile setter")),
        LaborRate("Flooring installer", 35.0, 65.0, listOf("flooring", "floor installer"))
    )

    val assemblies = listOf(
        Assembly("Wood wall framing", "sq ft (floor area)", 4.0, 8.0, 3.0, 8.0, "Carpenter",
            listOf("framing", "frame", "wall framing", "frame a wall", "frame a house"),
            "Total ~\$7–16/sq ft installed; higher in West Coast/Northeast metros."),
        Assembly("Drywall (hang + finish)", "sq ft", 0.40, 0.75, 1.20, 3.00, "Drywall installer/finisher",
            listOf("drywall", "sheetrock", "hang drywall", "drywall install"),
            "Total ~\$1.60–3.75/sq ft installed."),
        Assembly("Asphalt shingle roofing", "sq ft", 2.00, 3.00, 2.00, 3.50, "Roofer",
            listOf("roof", "roofing", "shingle", "reroof"),
            "Roughly 50/50 material/labor."),
        Assembly("Concrete slab (4\")", "sq ft", 2.50, 5.00, 1.50, 6.00, "Concrete finisher",
            listOf("slab", "concrete slab", "pour a slab", "concrete pad", "footing"),
            "Total ~\$4–8/sq ft; decorative finishes can reach \$18/sq ft."),
        Assembly("Interior painting", "sq ft (wall area)", 0.30, 0.80, 1.00, 3.00, "Painter",
            listOf("paint", "painting", "repaint")),
        Assembly("Hardwood flooring", "sq ft", 4.00, 9.00, 4.00, 8.00, "Flooring installer",
            listOf("hardwood", "wood floor")),
        Assembly("Luxury vinyl plank (LVP)", "sq ft", 2.00, 5.00, 2.00, 4.00, "Flooring installer",
            listOf("lvp", "vinyl plank", "vinyl floor")),
        Assembly("Tile flooring", "sq ft", 3.00, 10.00, 4.00, 12.00, "Tile setter",
            listOf("tile", "tile floor", "ceramic", "porcelain")),
        Assembly("Carpet", "sq ft", 1.50, 4.00, 1.50, 4.00, "Flooring installer",
            listOf("carpet")),
        Assembly("Wood deck", "sq ft", 15.00, 25.00, 15.00, 30.00, "Carpenter",
            listOf("deck", "build a deck"),
            "Basic pressure-treated decks average ~\$30–55/sq ft installed.")
    )

    val benchmarks = listOf(
        ProjectBenchmark("Kitchen remodel", 75.0, 250.0, "sq ft", listOf("kitchen remodel", "remodel kitchen", "kitchen"),
            "Typical total \$14.5k–41.5k; average ~\$27k."),
        ProjectBenchmark("Bathroom remodel", 70.0, 250.0, "sq ft", listOf("bathroom remodel", "remodel bathroom", "bath remodel", "bathroom"),
            "Typical total \$6.6k–17.6k."),
        ProjectBenchmark("Wood deck", 30.0, 60.0, "sq ft", listOf("deck"),
            "A 320 sq ft deck commonly runs \$16k+."),
        ProjectBenchmark("Room addition / gut renovation", 80.0, 200.0, "sq ft", listOf("addition", "room addition", "gut renovation", "gut reno", "renovation"),
            "Varies widely with finishes and structural work."),
        ProjectBenchmark("New home construction", 100.0, 200.0, "sq ft", listOf("new home", "new construction", "build a house"),
            "Excludes land; custom/high-end runs higher.")
    )

    val practices = listOf(
        Practice(
            "Construction phase sequence",
            "Typical residential sequence: 1) Pre-construction (design, permits, estimating) → " +
                "2) Site prep & excavation → 3) Foundation/footings → 4) Framing → 5) Roofing & exterior " +
                "(siding, windows) → 6) MEP rough-in (mechanical, electrical, plumbing) → 7) Insulation → " +
                "8) Drywall → 9) Interior finishes (paint, trim, flooring, cabinets) → 10) Final MEP & fixtures → " +
                "11) Final inspection → 12) Punch list & handover. Schedule inspections at foundation, " +
                "rough-in, and final.",
            listOf("phase", "sequence", "order", "steps", "process", "stages", "workflow")
        ),
        Practice(
            "Framing",
            "Standard wood framing: 2x4 or 2x6 studs at 16\" on center (24\" o.c. for some advanced framing). " +
                "Use double top plates, single bottom plate, headers over openings sized to span, and proper " +
                "fire/draft blocking. Sheathe exterior walls with OSB/plywood and a weather-resistive barrier. " +
                "Order ~10% extra lumber for waste.",
            listOf("framing", "frame", "studs", "stud spacing", "on center")
        ),
        Practice(
            "Concrete & foundations",
            "Pour on compacted base; standard slab is 4\" with #4 rebar or wire mesh and a vapor barrier under " +
                "conditioned space. Concrete averages \$135–185/cu yd; 1 cu yd covers ~81 sq ft at 4\". " +
                "Allow 28 days for full cure; protect from rapid drying. Keep a passing footing/foundation " +
                "inspection before backfill.",
            listOf("concrete", "foundation", "slab", "footing", "cure", "rebar")
        ),
        Practice(
            "Drywall",
            "Hang 1/2\" drywall on walls/ceilings (5/8\" Type X where fire-rated, e.g. garages). Stagger joints, " +
                "minimize butt joints, then tape and apply 3 finish coats to the spec level (Level 4 typical, " +
                "Level 5 for critical lighting). Budget ~10–12% material waste.",
            listOf("drywall", "sheetrock", "taping", "finish level", "mud")
        ),
        Practice(
            "Permits & inspections",
            "Most structural, electrical, plumbing, mechanical, and re-roof work needs a permit. Common " +
                "inspection holds: footing/foundation, framing, rough-in (MEP), insulation, and final. Don't " +
                "cover work before its inspection passes. Requirements vary by jurisdiction — confirm with the " +
                "local building department.",
            listOf("permit", "inspection", "code", "compliance", "ahj")
        ),
        Practice(
            "Pricing: markup, overhead & profit",
            "A common baseline is the \"10-10 rule\": ~10% overhead + ~10% profit (≈20% total markup). " +
                "Residential GCs often run 15–25% total markup; material markups are typically 7.5–20%. " +
                "Add a waste factor (10% standard, 15% complex) on materials, and remember loaded labor " +
                "includes 30–40% burden on the base wage.",
            listOf("markup", "overhead", "profit", "margin", "burden", "waste")
        )
    )
}
