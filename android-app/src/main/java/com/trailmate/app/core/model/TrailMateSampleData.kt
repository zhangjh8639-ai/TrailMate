package com.trailmate.app.core.model

object TrailMateSampleData {
    val skippedBaselineProfile = BaselineProfile(
        exerciseFrequency = ExerciseFrequency.RARELY,
        typicalDuration = TypicalDuration.UNDER_30,
        experienceLevel = ExperienceLevel.BEGINNER,
        ascentExperience = AscentExperience.UNDER_300,
        heightCm = null,
        weightKg = null,
        commonPackWeightKg = null
    )

    val baselineProfile = BaselineProfile(
        exerciseFrequency = ExerciseFrequency.ONE_TO_TWO_PER_WEEK,
        typicalDuration = TypicalDuration.MIN_30_TO_60,
        experienceLevel = ExperienceLevel.REGULAR,
        ascentExperience = AscentExperience.M300_TO_800,
        heightCm = 172,
        weightKg = 68,
        commonPackWeightKg = 5
    )

    val gearItems = listOf(
        GearItem("shoes-1", "Hiking shoes", "Salomon", "X Ultra 4 GTX", 760, true),
        GearItem("shell-1", "Rain shell", "Patagonia", "Torrentshell", 400, true),
        GearItem("headlamp-1", "Headlamp", "Black Diamond", "Spot 400", 86, true)
    )

    val importedTargetRoute = ImportedRoute(
        routeName = "Longjing Ridge",
        fileName = "longjing-ridge-target.gpx",
        distanceKm = 15.2,
        ascentMeters = 860,
        status = RouteImportStatus.PARSED,
        pointCount = 3
    )

    val sampleTargetGpx = """
        <?xml version="1.0" encoding="UTF-8"?>
        <gpx version="1.1" creator="TrailMate">
          <trk>
            <name>Longjing Ridge</name>
            <trkseg>
              <trkpt lat="30.250000" lon="120.120000"><ele>100</ele></trkpt>
              <trkpt lat="30.317000" lon="120.120000"><ele>420</ele></trkpt>
              <trkpt lat="30.386700" lon="120.120000"><ele>960</ele></trkpt>
            </trkseg>
          </trk>
        </gpx>
    """.trimIndent()

    val routeAssessment = RouteAssessmentEngine.assess(
        profile = baselineProfile,
        route = importedTargetRoute
    )

    val gearRecommendations = listOf(
        GearRecommendation(
            category = "Rain shell",
            status = GearStatus.COVERED,
            rationale = "Existing shell covers wind and light rain.",
            matchedGearItemId = "shell-1"
        ),
        GearRecommendation(
            category = "Headlamp",
            status = GearStatus.CHECK,
            rationale = "Expected finish may be late; check battery.",
            matchedGearItemId = "headlamp-1"
        ),
        GearRecommendation(
            category = "Trekking poles",
            status = GearStatus.MISSING,
            rationale = "Long descent and late climb make poles useful."
        ),
        GearRecommendation(
            category = "Warm layer",
            status = GearStatus.MISSING,
            rationale = "High point stops and late finish may feel cold."
        )
    )
}
