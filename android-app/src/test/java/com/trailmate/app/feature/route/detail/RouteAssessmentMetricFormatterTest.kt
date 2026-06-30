package com.trailmate.app.feature.route.detail

import org.junit.Assert.assertEquals
import org.junit.Test

class RouteAssessmentMetricFormatterTest {
    @Test
    fun durationRangeUsesCompactHourMinuteUnitsForMetricCards() {
        assertEquals("3h13-4h17", RouteAssessmentMetricFormatter.durationRangeForMetric("3:13-4:17"))
    }

    @Test
    fun longDurationRangeUsesRoundedWholeHoursForMetricCards() {
        assertEquals("12-20h", RouteAssessmentMetricFormatter.durationRangeForMetric("12:14-19:26"))
    }

    @Test
    fun nonDurationTextIsLeftUnchanged() {
        assertEquals("TBD", RouteAssessmentMetricFormatter.durationRangeForMetric("TBD"))
    }
}
