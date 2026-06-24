package com.trailmate.app.core.network

import com.trailmate.app.core.model.AiGearAdvisorResponse
import com.trailmate.app.core.model.ConfidenceLevel
import com.trailmate.app.core.model.GearRecommendation
import com.trailmate.app.core.model.GearStatus
import com.trailmate.app.core.model.MatchLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailMateServerApiContractTest {
    @Test
    fun endpointCatalogMatchesServerPlan() {
        assertEquals("/api/v1", TrailMateServerApiContract.BASE_PATH)
        assertEquals("/auth/register", TrailMateServerApiContract.Endpoints.register)
        assertEquals("/auth/login", TrailMateServerApiContract.Endpoints.login)
        assertEquals("/auth/refresh", TrailMateServerApiContract.Endpoints.refreshSession)
        assertEquals("/auth/phone/code", TrailMateServerApiContract.Endpoints.requestPhoneCode)
        assertEquals("/auth/phone/login", TrailMateServerApiContract.Endpoints.loginWithPhone)
        assertEquals("/auth/wechat/login", TrailMateServerApiContract.Endpoints.loginWithWechat)
        assertEquals("/imports/gpx", TrailMateServerApiContract.Endpoints.gpxImport)
        assertEquals("/imports/{jobId}", TrailMateServerApiContract.Endpoints.importJob)
        assertEquals("/routes/{routeId}/assessments", TrailMateServerApiContract.Endpoints.routeAssessments)
        assertEquals("/assessments/{assessmentId}/plans", TrailMateServerApiContract.Endpoints.assessmentPlans)
        assertEquals("/plans/{planId}/gear-advice", TrailMateServerApiContract.Endpoints.gearAdvice)
        assertEquals("/plans/{planId}/feedback", TrailMateServerApiContract.Endpoints.planFeedback)
        assertEquals("/tracks", TrailMateServerApiContract.Endpoints.tracks)
        assertEquals("/exports/me", TrailMateServerApiContract.Endpoints.accountExport)
        assertEquals("/users/me", TrailMateServerApiContract.Endpoints.deleteAccount)
    }

    @Test
    fun mobileRemotePortGroupsCoreServerOperations() {
        val operations = TrailMateBackendOperation.entries.map { it.name }.toSet()

        assertTrue("RegisterLogin" in operations)
        assertTrue("RequestPhoneCode" in operations)
        assertTrue("PhoneLogin" in operations)
        assertTrue("WechatLogin" in operations)
        assertTrue("SyncOnboardingProfile" in operations)
        assertTrue("ImportGpx" in operations)
        assertTrue("ListActivities" in operations)
        assertTrue("RecalculateProfile" in operations)
        assertTrue("AssessRoute" in operations)
        assertTrue("GeneratePlan" in operations)
        assertTrue("RequestGearAdvice" in operations)
        assertTrue("SubmitFeedback" in operations)
        assertTrue("UploadRecordedTrack" in operations)
        assertTrue("ExportAndDeleteAccount" in operations)
    }

    @Test
    fun apiErrorPreservesServerRecoveryFields() {
        val error = TrailMateApiError(
            status = 422,
            code = "GPX_MISSING_TIME",
            message = "该历史轨迹缺少时间信息，不能用于个人速度画像。",
            traceId = "trace-123"
        )
        val result = TrailMateApiResult.Failure(error)

        assertEquals(error, result.errorOrNull())
        assertEquals("GPX_MISSING_TIME", result.errorOrNull()?.code)
        assertEquals("trace-123", result.errorOrNull()?.traceId)
    }

    @Test
    fun androidFacingAssessmentAndPlanDtosCarryFingerprintsAndAlgorithmVersions() {
        val assessment = TrailMateAssessmentDto(
            assessmentId = "asm-1",
            routeId = "route-1",
            profileId = "profile-7",
            assessmentFingerprint = "route-1#profile-7#assessment-v1",
            matchScore = 68,
            matchLevel = MatchLevel.CAUTION,
            confidenceLevel = ConfidenceLevel.MEDIUM,
            estimatedMinSeconds = 24_000,
            estimatedMaxSeconds = 28_200,
            riskFactors = listOf(
                TrailMateRiskFactorDto(
                    code = "LATE_STAGE_ASCENT",
                    severity = "HIGH",
                    routeStartMeters = 11_200,
                    routeEndMeters = 14_600,
                    message = "最后阶段仍有连续爬升，可能放大后程速度下降"
                )
            ),
            algorithmVersion = "assessment-v1"
        )
        val plan = TrailMatePlanDto(
            planId = "plan-1",
            assessmentId = assessment.assessmentId,
            assessmentFingerprint = assessment.assessmentFingerprint,
            checkpoints = listOf(
                TrailMatePlanCheckpointDto(
                    type = "REST_CHECK",
                    title = "长爬升前检查",
                    distanceMeters = 5_200,
                    timeFromStartMinSeconds = 7_800,
                    timeFromStartMaxSeconds = 9_000,
                    note = "进入连续爬升前检查体力、饮水和能量。"
                )
            ),
            algorithmVersion = "plan-v1"
        )

        assertEquals("route-1#profile-7#assessment-v1", assessment.assessmentFingerprint)
        assertEquals(assessment.assessmentFingerprint, plan.assessmentFingerprint)
        assertEquals("assessment-v1", assessment.algorithmVersion)
        assertEquals("plan-v1", plan.algorithmVersion)
    }

    @Test
    fun backendApiPortCanReturnGearAdviceThroughUnifiedResult() {
        val response = AiGearAdvisorResponse(
            assessmentFingerprint = "route-1#profile-7#assessment-v1",
            recommendations = listOf(
                GearRecommendation(
                    category = "雨衣（防水透气）",
                    status = GearStatus.MISSING,
                    rationale = "路线海拔和天气变化可能导致降雨或低温。"
                )
            )
        )
        val result: TrailMateApiResult<AiGearAdvisorResponse> = TrailMateApiResult.Success(response)

        assertEquals(response, result.getOrNull())
        assertEquals(null, result.errorOrNull())
    }

    @Test
    fun authDtosSupportPhoneAndWechatRegistrationLogin() {
        val phoneCodeRequest = TrailMatePhoneCodeRequestDto(
            phoneNumber = "+8613800138000",
            scene = TrailMatePhoneAuthScene.LOGIN_OR_REGISTER
        )
        val phoneLoginRequest = TrailMatePhoneLoginRequestDto(
            phoneNumber = phoneCodeRequest.phoneNumber,
            smsCode = "123456"
        )
        val wechatLoginRequest = TrailMateWechatLoginRequestDto(
            authCode = "wx-auth-code",
            state = "nonce-from-client"
        )
        val session = TrailMateAuthSessionDto(
            userId = "usr-1",
            provider = TrailMateAuthProviderDto.PHONE,
            accessToken = "access",
            refreshToken = "refresh",
            expiresAt = "2026-06-22T12:00:00Z",
            phoneNumber = phoneLoginRequest.phoneNumber,
            wechatOpenId = null,
            displayName = null
        )

        assertEquals(TrailMatePhoneAuthScene.LOGIN_OR_REGISTER, phoneCodeRequest.scene)
        assertEquals("123456", phoneLoginRequest.smsCode)
        assertEquals("wx-auth-code", wechatLoginRequest.authCode)
        assertEquals(TrailMateAuthProviderDto.PHONE, session.provider)
        assertEquals("+8613800138000", session.phoneNumber)
    }
}
