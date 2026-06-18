package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HistoricalActivityLogTest {
    @Test
    fun addAllDeduplicatesActivitiesByRouteFacts() {
        val duplicate = TrailMateSampleData.historicalActivities.first()
        val log = HistoricalActivityLog(TrailMateSampleData.historicalActivities)
            .addAll(listOf(duplicate))

        assertEquals(TrailMateSampleData.historicalActivities.size, log.activities.size)
        assertEquals(TrailMateSampleData.historicalActivities, log.activities)
    }

    @Test
    fun removeDeletesMatchingActivityAndKeepsOthersInOrder() {
        val target = TrailMateSampleData.historicalActivities[1]
        val log = HistoricalActivityLog(TrailMateSampleData.historicalActivities)
            .remove(target.key())

        assertEquals(2, log.activities.size)
        assertFalse(log.activities.any { it.routeName == target.routeName })
        assertEquals(TrailMateSampleData.historicalActivities.first(), log.activities.first())
    }

    @Test
    fun removeDeletesOnlyFirstMatchingActivityWhenLegacyDuplicatesExist() {
        val duplicate = TrailMateSampleData.historicalActivities.first()
        val legacyLog = HistoricalActivityLog(listOf(duplicate, duplicate, TrailMateSampleData.historicalActivities[1]))

        val log = legacyLog.remove(duplicate.key())

        assertEquals(listOf(duplicate, TrailMateSampleData.historicalActivities[1]), log.activities)
    }

    @Test
    fun activitySummaryFormatsDistanceAscentAndDuration() {
        val activity = TrailMateSampleData.historicalActivities.first()

        assertEquals("9.8 km / +420 m / 2:45", activity.summaryLabel())
    }
}
