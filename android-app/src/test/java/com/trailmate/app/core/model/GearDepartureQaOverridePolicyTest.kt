package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GearDepartureQaOverridePolicyTest {
    @Test
    fun keepsMissingGearWhenDebugBypassIsDisabled() {
        val recommendations = listOf(
            GearRecommendation(
                category = "登山杖",
                status = GearStatus.MISSING,
                rationale = "长距离路线建议携带。"
            )
        )

        assertEquals(
            recommendations,
            GearDepartureQaOverridePolicy.apply(
                recommendations = recommendations,
                debugBypassEnabled = false
            )
        )
    }

    @Test
    fun marksMissingGearCoveredForDebugDepartureQa() {
        val result = GearDepartureQaOverridePolicy.apply(
            recommendations = listOf(
                GearRecommendation(
                    category = "登山杖",
                    status = GearStatus.MISSING,
                    rationale = "长距离路线建议携带。"
                ),
                GearRecommendation(
                    category = "头灯",
                    status = GearStatus.CHECK,
                    rationale = "出发前检查电量。"
                )
            ),
            debugBypassEnabled = true
        )

        assertEquals(GearStatus.COVERED, result[0].status)
        assertEquals(GearStatus.CHECK, result[1].status)
    }
}
