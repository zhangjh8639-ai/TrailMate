package com.trailmate.app.core.network

import com.trailmate.app.core.map.PmTilesLatLngBounds
import com.trailmate.app.core.model.AiGearAdvisorRequest
import com.trailmate.app.core.model.AiGearAdvisorResponse
import com.trailmate.app.core.model.AscentExperience
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.ConfidenceLevel
import com.trailmate.app.core.model.ExerciseFrequency
import com.trailmate.app.core.model.ExperienceLevel
import com.trailmate.app.core.model.MatchLevel
import com.trailmate.app.core.model.RecordedTrackPoint
import com.trailmate.app.core.model.TypicalDuration

object TrailMateServerApiContract {
    const val BASE_PATH = "/api/v1"

    object Endpoints {
        const val register = "/auth/register"
        const val login = "/auth/login"
        const val refreshSession = "/auth/refresh"
        const val logout = "/auth/logout"
        const val requestPhoneCode = "/auth/phone/code"
        const val loginWithPhone = "/auth/phone/login"
        const val loginWithWechat = "/auth/wechat/login"
        const val currentUser = "/users/me"
        const val userProfile = "/users/me/profile"
        const val deleteAccount = "/users/me"
        const val gpxImport = "/imports/gpx"
        const val importJob = "/imports/{jobId}"
        const val confirmImport = "/imports/{jobId}/confirm"
        const val activities = "/activities"
        const val activity = "/activities/{activityId}"
        const val recalculateProfile = "/profiles/recalculate"
        const val currentCapabilityProfile = "/profiles/current"
        const val routes = "/routes"
        const val route = "/routes/{routeId}"
        const val routeAssessments = "/routes/{routeId}/assessments"
        const val assessment = "/assessments/{assessmentId}"
        const val assessmentPlans = "/assessments/{assessmentId}/plans"
        const val plan = "/plans/{planId}"
        const val gear = "/gear"
        const val gearItem = "/gear/{gearId}"
        const val gearCatalogCategories = "/gear/catalog/categories"
        const val gearCatalogSearch = "/gear/catalog/search"
        const val offlineBasemapPmTilesCatalog = "/offline-basemaps/pmtiles/catalog"
        const val gearAdvice = "/plans/{planId}/gear-advice"
        const val planFeedback = "/plans/{planId}/feedback"
        const val tracks = "/tracks"
        const val accountExport = "/exports/me"
    }
}

enum class TrailMateBackendOperation {
    RegisterLogin,
    RequestPhoneCode,
    PhoneLogin,
    WechatLogin,
    SyncOnboardingProfile,
    ImportGpx,
    ListActivities,
    RecalculateProfile,
    AssessRoute,
    GeneratePlan,
    RequestGearAdvice,
    SubmitFeedback,
    UploadRecordedTrack,
    ExportAndDeleteAccount
}

sealed interface TrailMateApiResult<out T> {
    data class Success<out T>(val value: T) : TrailMateApiResult<T>
    data class Failure(val error: TrailMateApiError) : TrailMateApiResult<Nothing>
}

fun <T> TrailMateApiResult<T>.getOrNull(): T? =
    when (this) {
        is TrailMateApiResult.Success -> value
        is TrailMateApiResult.Failure -> null
    }

fun TrailMateApiResult<*>.errorOrNull(): TrailMateApiError? =
    when (this) {
        is TrailMateApiResult.Success -> null
        is TrailMateApiResult.Failure -> error
    }

data class TrailMateApiError(
    val status: Int,
    val code: String,
    val message: String,
    val traceId: String?
)

data class TrailMateAuthRequestDto(
    val email: String,
    val password: String
)

enum class TrailMateAuthProviderDto {
    EMAIL,
    PHONE,
    WECHAT
}

data class TrailMateAuthSessionDto(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: String,
    val provider: TrailMateAuthProviderDto = TrailMateAuthProviderDto.EMAIL,
    val phoneNumber: String? = null,
    val wechatOpenId: String? = null,
    val displayName: String? = null
)

enum class TrailMatePhoneAuthScene {
    LOGIN_OR_REGISTER
}

data class TrailMatePhoneCodeRequestDto(
    val phoneNumber: String,
    val scene: TrailMatePhoneAuthScene
)

data class TrailMatePhoneCodeResponseDto(
    val phoneNumber: String,
    val expiresInSeconds: Int,
    val retryAfterSeconds: Int
)

data class TrailMatePhoneLoginRequestDto(
    val phoneNumber: String,
    val smsCode: String
)

data class TrailMateWechatLoginRequestDto(
    val authCode: String,
    val state: String
)

data class TrailMateRefreshSessionRequestDto(
    val refreshToken: String
)

data class TrailMateLogoutRequestDto(
    val refreshToken: String
)

data class TrailMateUserDto(
    val userId: String,
    val email: String,
    val profile: BaselineProfile?
)

data class TrailMateOnboardingProfileDto(
    val userId: String,
    val exerciseFrequency: ExerciseFrequency,
    val typicalDuration: TypicalDuration,
    val experienceLevel: ExperienceLevel,
    val ascentExperience: AscentExperience,
    val heightCm: Int?,
    val weightKg: Int?,
    val commonPackWeightKg: Int?,
    val updatedAt: String?
) {
    companion object {
        fun from(
            profile: BaselineProfile,
            userId: String,
            updatedAt: String? = null
        ): TrailMateOnboardingProfileDto =
            TrailMateOnboardingProfileDto(
                userId = userId,
                exerciseFrequency = profile.exerciseFrequency,
                typicalDuration = profile.typicalDuration,
                experienceLevel = profile.experienceLevel,
                ascentExperience = profile.ascentExperience,
                heightCm = profile.heightCm,
                weightKg = profile.weightKg,
                commonPackWeightKg = profile.commonPackWeightKg,
                updatedAt = updatedAt
            )
    }
}

enum class TrailMateGpxImportPurpose {
    HISTORY,
    TARGET
}

enum class TrailMateImportJobStatus {
    QUEUED,
    PROCESSING,
    READY_TO_CONFIRM,
    CONFIRMED,
    FAILED
}

data class TrailMateGpxImportRequestDto(
    val fileName: String,
    val purpose: TrailMateGpxImportPurpose,
    val displayName: String?
)

data class TrailMateGpxImportJobDto(
    val jobId: String,
    val purpose: TrailMateGpxImportPurpose,
    val status: TrailMateImportJobStatus,
    val qualityStatus: String,
    val message: String
)

data class TrailMateConfirmedImportDto(
    val jobId: String,
    val importedResourceId: String,
    val resourceType: TrailMateGpxImportPurpose
)

data class TrailMateActivitySummaryDto(
    val activityId: String,
    val name: String,
    val distanceMeters: Double,
    val ascentMeters: Double,
    val usableForProfile: Boolean
)

data class TrailMateCapabilityProfileDto(
    val profileId: String,
    val versionNo: Int,
    val sampleCount: Int,
    val confidenceLevel: ConfidenceLevel,
    val algorithmVersion: String
)

data class TrailMateRouteSummaryDto(
    val routeId: String,
    val name: String,
    val distanceMeters: Double,
    val ascentMeters: Double
)

data class TrailMateRiskFactorDto(
    val code: String,
    val severity: String,
    val routeStartMeters: Int,
    val routeEndMeters: Int,
    val message: String
)

data class TrailMateAssessmentDto(
    val assessmentId: String,
    val routeId: String,
    val profileId: String,
    val assessmentFingerprint: String,
    val matchScore: Int,
    val matchLevel: MatchLevel,
    val confidenceLevel: ConfidenceLevel,
    val estimatedMinSeconds: Long,
    val estimatedMaxSeconds: Long,
    val riskFactors: List<TrailMateRiskFactorDto>,
    val algorithmVersion: String
)

data class TrailMatePlanCheckpointDto(
    val type: String,
    val title: String,
    val distanceMeters: Int,
    val timeFromStartMinSeconds: Long,
    val timeFromStartMaxSeconds: Long,
    val note: String
)

data class TrailMatePlanDto(
    val planId: String,
    val assessmentId: String,
    val assessmentFingerprint: String,
    val checkpoints: List<TrailMatePlanCheckpointDto>,
    val algorithmVersion: String
)

data class TrailMateGearCatalogItemDto(
    val catalogItemId: String,
    val category: String,
    val brand: String,
    val model: String,
    val displayName: String,
    val weightGrams: Int?,
    val tags: List<String>,
    val imageUrl: String?,
    val imageAttribution: String?,
    val source: String
)

data class TrailMatePmTilesBasemapCatalogItemDto(
    val packId: String,
    val regionName: String,
    val downloadUrl: String,
    val sizeBytes: Long?,
    val sha256: String?,
    val tileType: String,
    val minZoom: Int,
    val maxZoom: Int,
    val minLongitude: Double,
    val minLatitude: Double,
    val maxLongitude: Double,
    val maxLatitude: Double,
    val attribution: String,
    val source: String
)

data class TrailMateCompletionFeedbackDto(
    val completed: Boolean,
    val actualDurationSeconds: Long?,
    val fatigueRating: Int?,
    val difficultSegmentNotes: String?
)

data class TrailMateRecordedTrackUploadDto(
    val routeName: String?,
    val points: List<RecordedTrackPoint>
)

data class TrailMateExportJobDto(
    val exportId: String,
    val status: String,
    val message: String
)

data class TrailMateDeletionReceiptDto(
    val receiptId: String,
    val message: String
)

interface TrailMateGearAdviceApi {
    fun requestGearAdvice(
        planId: String,
        request: AiGearAdvisorRequest
    ): TrailMateApiResult<AiGearAdvisorResponse>
}

interface TrailMateGearCatalogApi {
    fun listGearCatalogCategories(): TrailMateApiResult<List<String>>
    fun searchGearCatalog(
        category: String,
        query: String
    ): TrailMateApiResult<List<TrailMateGearCatalogItemDto>>
}

interface TrailMateOfflineBasemapCatalogApi {
    fun listPmTilesBasemaps(
        routeBounds: PmTilesLatLngBounds
    ): TrailMateApiResult<List<TrailMatePmTilesBasemapCatalogItemDto>>
}

interface TrailMateAuthApi {
    fun requestPhoneCode(request: TrailMatePhoneCodeRequestDto): TrailMateApiResult<TrailMatePhoneCodeResponseDto>
    fun loginWithPhone(request: TrailMatePhoneLoginRequestDto): TrailMateApiResult<TrailMateAuthSessionDto>
    fun loginWithWechat(request: TrailMateWechatLoginRequestDto): TrailMateApiResult<TrailMateAuthSessionDto>
    fun refreshSession(request: TrailMateRefreshSessionRequestDto): TrailMateApiResult<TrailMateAuthSessionDto>
    fun logout(request: TrailMateLogoutRequestDto): TrailMateApiResult<Unit>
}

interface TrailMateUserProfileApi {
    fun saveOnboardingProfile(
        userId: String,
        profile: BaselineProfile
    ): TrailMateApiResult<TrailMateOnboardingProfileDto>
}

interface TrailMateBackendApi : TrailMateAuthApi, TrailMateGearAdviceApi {
    fun register(request: TrailMateAuthRequestDto): TrailMateApiResult<TrailMateAuthSessionDto>
    fun login(request: TrailMateAuthRequestDto): TrailMateApiResult<TrailMateAuthSessionDto>
    override fun refreshSession(request: TrailMateRefreshSessionRequestDto): TrailMateApiResult<TrailMateAuthSessionDto>
    override fun logout(request: TrailMateLogoutRequestDto): TrailMateApiResult<Unit>
    override fun requestPhoneCode(request: TrailMatePhoneCodeRequestDto): TrailMateApiResult<TrailMatePhoneCodeResponseDto>
    override fun loginWithPhone(request: TrailMatePhoneLoginRequestDto): TrailMateApiResult<TrailMateAuthSessionDto>
    override fun loginWithWechat(request: TrailMateWechatLoginRequestDto): TrailMateApiResult<TrailMateAuthSessionDto>
    fun getCurrentUser(): TrailMateApiResult<TrailMateUserDto>
    fun saveOnboardingProfile(profile: BaselineProfile): TrailMateApiResult<TrailMateUserDto>
    fun startGpxImport(request: TrailMateGpxImportRequestDto): TrailMateApiResult<TrailMateGpxImportJobDto>
    fun getImportJob(jobId: String): TrailMateApiResult<TrailMateGpxImportJobDto>
    fun confirmImport(jobId: String): TrailMateApiResult<TrailMateConfirmedImportDto>
    fun listActivities(): TrailMateApiResult<List<TrailMateActivitySummaryDto>>
    fun deleteActivity(activityId: String): TrailMateApiResult<Unit>
    fun recalculateProfile(): TrailMateApiResult<TrailMateCapabilityProfileDto>
    fun getCurrentCapabilityProfile(): TrailMateApiResult<TrailMateCapabilityProfileDto>
    fun listRoutes(): TrailMateApiResult<List<TrailMateRouteSummaryDto>>
    fun getRoute(routeId: String): TrailMateApiResult<TrailMateRouteSummaryDto>
    fun createAssessment(routeId: String): TrailMateApiResult<TrailMateAssessmentDto>
    fun getAssessment(assessmentId: String): TrailMateApiResult<TrailMateAssessmentDto>
    fun createPlan(assessmentId: String): TrailMateApiResult<TrailMatePlanDto>
    fun getPlan(planId: String): TrailMateApiResult<TrailMatePlanDto>
    override fun requestGearAdvice(
        planId: String,
        request: AiGearAdvisorRequest
    ): TrailMateApiResult<AiGearAdvisorResponse>
    fun submitCompletionFeedback(
        planId: String,
        feedback: TrailMateCompletionFeedbackDto
    ): TrailMateApiResult<TrailMateCapabilityProfileDto>
    fun uploadRecordedTrack(track: TrailMateRecordedTrackUploadDto): TrailMateApiResult<Unit>
    fun requestAccountExport(): TrailMateApiResult<TrailMateExportJobDto>
    fun deleteAccount(): TrailMateApiResult<TrailMateDeletionReceiptDto>
}
