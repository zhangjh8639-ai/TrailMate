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
        GearItem("shoes-1", "徒步鞋", "Salomon", "X Ultra 4 GTX", 760, true),
        GearItem("shell-1", "雨衣", "Patagonia", "Torrentshell", 400, true),
        GearItem("headlamp-1", "头灯", "Black Diamond", "Spot 400", 86, true)
    )

    val importedTargetRoute = ImportedRoute(
        routeName = "龙井山脊",
        fileName = "longjing-ridge-target.gpx",
        distanceKm = 15.2,
        ascentMeters = 860,
        status = RouteImportStatus.PARSED,
        pointCount = 7,
        routePoints = listOf(
            RoutePoint(latitude = 30.2100, longitude = 120.1100, elevationMeters = 108.0, distanceAlongRouteKm = 0.0),
            RoutePoint(latitude = 30.2250, longitude = 120.1180, elevationMeters = 220.0, distanceAlongRouteKm = 2.3),
            RoutePoint(latitude = 30.2380, longitude = 120.1120, elevationMeters = 360.0, distanceAlongRouteKm = 5.1),
            RoutePoint(latitude = 30.2520, longitude = 120.1260, elevationMeters = 520.0, distanceAlongRouteKm = 8.2),
            RoutePoint(latitude = 30.2670, longitude = 120.1210, elevationMeters = 680.0, distanceAlongRouteKm = 11.8),
            RoutePoint(latitude = 30.2800, longitude = 120.1340, elevationMeters = 850.0, distanceAlongRouteKm = 13.6),
            RoutePoint(latitude = 30.2920, longitude = 120.1300, elevationMeters = 968.0, distanceAlongRouteKm = 15.2)
        )
    )

    val historicalActivities = listOf(
        HistoricalActivity(
            routeName = "晨间山脊环线",
            distanceKm = 9.8,
            ascentMeters = 420,
            durationMinutes = 165
        ),
        HistoricalActivity(
            routeName = "云线横穿",
            distanceKm = 12.7,
            ascentMeters = 760,
            durationMinutes = 285
        ),
        HistoricalActivity(
            routeName = "北峰训练",
            distanceKm = 18.6,
            ascentMeters = 980,
            durationMinutes = 420
        )
    )

    val sampleTargetGpx = """
        <?xml version="1.0" encoding="UTF-8"?>
        <gpx version="1.1" creator="TrailMate">
          <trk>
            <name>龙井山脊</name>
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
            category = "雨衣",
            status = GearStatus.COVERED,
            rationale = "现有雨衣可以覆盖山脊风和小雨。",
            matchedGearItemId = "shell-1"
        ),
        GearRecommendation(
            category = "头灯",
            status = GearStatus.CHECK,
            rationale = "预计收队可能偏晚，出发前检查电量。",
            matchedGearItemId = "headlamp-1"
        ),
        GearRecommendation(
            category = "登山杖",
            status = GearStatus.MISSING,
            rationale = "长下坡和后段爬升会让登山杖更有价值。"
        ),
        GearRecommendation(
            category = "保暖层",
            status = GearStatus.MISSING,
            rationale = "高点停留和晚归都会让体感温度下降。"
        )
    )
}
