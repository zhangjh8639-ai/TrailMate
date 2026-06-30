package com.trailmate.app

import android.Manifest
import android.content.Intent
import android.view.KeyEvent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.trailmate.app.core.design.TrailMateTheme
import com.trailmate.app.core.auth.TrailMateLocalOnboardingAuthActions
import com.trailmate.app.core.auth.TrailMateAuthSession
import com.trailmate.app.core.location.TrackRecordingBroadcastCodec
import com.trailmate.app.core.location.TrackRecordingForegroundService
import com.trailmate.app.core.location.TrailMateLocationSnapshot
import com.trailmate.app.core.location.TrailMateLocationStatus
import com.trailmate.app.core.model.AiGearAdvisorResponse
import com.trailmate.app.core.model.ConfidenceLevel
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.gpx.GpxImportJobKind
import com.trailmate.app.core.gpx.GpxImportJobStatus
import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProof
import com.trailmate.app.core.map.AmapPrivacyConsent
import com.trailmate.app.core.model.ExerciseFrequency
import com.trailmate.app.core.model.ExperienceLevel
import com.trailmate.app.core.model.GearRecommendation
import com.trailmate.app.core.model.GearStatus
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.HikeLocationFix
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.LocationBackedHikeStatus
import com.trailmate.app.core.model.MatchLevel
import com.trailmate.app.core.model.RecordedTrackPoint
import com.trailmate.app.core.model.RouteAssessmentSummary
import com.trailmate.app.core.model.RoutePoint
import com.trailmate.app.core.model.AscentExperience
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.core.model.TrackRecordingEngine
import com.trailmate.app.core.model.TrackRecordingState
import com.trailmate.app.core.model.TypicalDuration
import com.trailmate.app.core.model.offlineRoutePackKey
import com.trailmate.app.core.network.TrailMateApiError
import com.trailmate.app.core.network.TrailMateApiResult
import com.trailmate.app.core.network.TrailMateGearCatalogApi
import com.trailmate.app.core.network.TrailMateGearCatalogItemDto
import com.trailmate.app.core.persistence.TrailMateSessionRepository
import com.trailmate.app.core.persistence.TrailMateSnapshot
import com.trailmate.app.feature.gear.GearMatchScreen
import com.trailmate.app.feature.home.HomeScreen
import com.trailmate.app.feature.onboarding.OnboardingScreen
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TrailMateAppSmokeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun showsTrailMateOnboarding() {
        compose.setContent {
            TrailMateTheme {
                OnboardingScreen(
                    authActions = TrailMateLocalOnboardingAuthActions(),
                    wechatLoginAvailable = true,
                    requestForegroundLocationPermissionOnComplete = false,
                    onComplete = { _, _ -> }
                )
            }
        }

        compose.onNodeWithText("TrailMate").assertExists()
        compose.onNodeWithText("账号 1/3").assertExists()
        compose.onNodeWithText("微信优先登录").assertExists()
        compose.onNodeWithText("微信登录 / 注册").assertExists()
        compose.onAllNodesWithText("手机号").assertCountEquals(0)
        compose.onAllNodesWithText("验证码").assertCountEquals(0)
    }

    @Test
    fun onboardingCollectsBaselineProfileBeforeHome() {
        val store = FakeTrailMateSessionRepository(
            TrailMateSnapshot(authSession = savedAuthSession())
        )

        compose.setContent {
            TrailMateTheme {
                TrailMateApp(sessionRepository = store)
            }
        }

        compose.onNodeWithText("能力基础 2/3").assertExists()
        compose.onNodeWithText("只用于路线评估，不在主页展示", substring = true).assertExists()
        compose.onAllNodesWithText("证据", substring = true).assertCountEquals(0)
        compose.onNodeWithText("身高 cm").performScrollTo().performTextInput("181")
        compose.onNodeWithText("体重 kg").performScrollTo().performTextInput("76")
        compose.onNodeWithText("常用背包 kg").performScrollTo().performTextInput("7")
        compose.onNodeWithText("保存档案").performScrollTo().performClick()
        compose.onNodeWithText("地图与定位准备").assertExists()
        compose.onNodeWithText("地图准备 3/3").assertExists()
        compose.onNodeWithText("离线地图包").assertExists()
        compose.onAllNodesWithText("高德", substring = true).assertCountEquals(0)
        compose.onNodeWithText("同意地图服务并继续").performScrollTo().performClick()

        compose.onNodeWithText("准备走哪条线？").assertExists()
        compose.onAllNodesWithText("181cm / 76kg").assertCountEquals(0)
        compose.onAllNodesWithText("背包 7 kg").assertCountEquals(0)
    }

    @Test
    fun appRestoresSavedProfileToHome() {
        compose.setContent {
            TrailMateTheme {
                TrailMateApp(
                    sessionRepository = FakeTrailMateSessionRepository(
                        TrailMateSnapshot(
                            authSession = savedAuthSession(),
                            profile = savedProfile(),
                            historicalActivities = TrailMateSampleData.historicalActivities
                        )
                    )
                )
            }
        }

        compose.onNodeWithText("准备走哪条线？").assertExists()
        compose.onAllNodesWithText("181cm / 76kg").assertCountEquals(0)
        compose.onAllNodesWithText("3/3 GPX").assertCountEquals(0)
        compose.onAllNodesWithText("历史能力画像").assertCountEquals(0)
        compose.onAllNodesWithText("开始基础档案").assertCountEquals(0)
    }

    @Test
    fun homeDashboardFocusesOnTodayRoutePreparation() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    profile = savedProfile(),
                    initialImportedRoute = TrailMateSampleData.importedTargetRoute,
                    initialHistoricalActivities = TrailMateSampleData.historicalActivities
                )
            }
        }

        compose.onNodeWithText("准备走哪条线？").assertExists()
        compose.onNodeWithText("导入 GPX 文件").assertExists()
        compose.onNodeWithText("当前路线评估").assertExists()
        compose.onNodeWithText("快速开始").performScrollTo().assertExists()
        compose.onAllNodesWithText("今日概览").assertCountEquals(0)
        compose.onAllNodesWithText("历史 GPX 能力证据").assertCountEquals(0)
        compose.onAllNodesWithText("181cm / 76kg").assertCountEquals(0)
        compose.onAllNodesWithText("本地数据").assertCountEquals(0)
    }

    @Test
    fun routeWorkspaceOwnsRouteImportAndCurrentRouteEntry() {
        val routeKey = TrailMateSampleData.importedTargetRoute.offlineRoutePackKey()
        var observedOfflineRoutePackKeys = emptySet<String>()

        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    initialImportedRoute = TrailMateSampleData.importedTargetRoute,
                    onOfflineRoutePackKeysChanged = { keys ->
                        observedOfflineRoutePackKeys = keys
                    }
                )
            }
        }

        compose.onNodeWithTag("home-tab-路线").performClick()
        compose.onNodeWithText("路线准备").assertExists()
        compose.onNodeWithText("当前路线").assertExists()
        compose.onNodeWithText("继续准备").assertExists()
        compose.onNodeWithText("更换 GPX").assertExists()
        compose.onNodeWithText("出发前准备").assertExists()
        compose.onNodeWithText("GPX 路线").assertExists()
        compose.onNodeWithText("GPX 轨迹已导入").assertExists()
        compose.onNodeWithText("离线路线").assertExists()
        compose.onNodeWithText("离线路线：待保存", substring = true).assertExists()
        compose.onNodeWithText("补齐道路、地名和地形背景，辅助判断撤退与岔路。").assertExists()
        compose.onAllNodesWithText("地图状态与轻导航").assertCountEquals(0)
        compose.onAllNodesWithText("导入状态").assertCountEquals(0)
        compose.onAllNodesWithText("导入队列").assertCountEquals(0)
        compose.onAllNodesWithText("路线包").assertCountEquals(0)
        compose.onAllNodesWithText("风险因素").assertCountEquals(0)
        compose.onAllNodesWithText("现场状态").assertCountEquals(0)
        compose.onAllNodesWithTag("route-cockpit").assertCountEquals(0)
        compose.onAllNodesWithText("保存路线包").assertCountEquals(0)

        compose.onNodeWithText("继续准备").performScrollTo().performClick()
        compose.onNodeWithTag("segmented-control-路线").performClick()
        compose.onNodeWithTag("route-cockpit-offline-route-action")
            .performScrollTo()
            .assertExists()
            .performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            routeKey in observedOfflineRoutePackKeys
        }
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText("离线路线已保存", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onAllNodesWithText("离线路线已保存", substring = true).onFirst().assertExists()
    }

    @Test
    fun routeWorkspaceBasemapPreparationOpensRouteMapPreparation() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    initialImportedRoute = TrailMateSampleData.importedTargetRoute,
                    initialOfflineRoutePackKeys = setOf(
                        TrailMateSampleData.importedTargetRoute.offlineRoutePackKey()
                    )
                )
            }
        }

        compose.onNodeWithTag("home-tab-路线").performClick()
        compose.onNodeWithText("准备离线底图").performScrollTo().performClick()

        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("route-cockpit")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithTag("route-cockpit").assertExists()
        compose.onNodeWithText("地图图层").performScrollTo().assertExists()
        compose.onAllNodesWithText("离线底图待准备").onFirst().assertExists()
        compose.onAllNodesWithText("准备离线底图").onFirst().assertExists()
        compose.onAllNodesWithText("打开高德离线底图管理").assertCountEquals(0)
    }

    @Test
    fun routeDetailBackReturnsToRouteWorkspace() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen(initialImportedRoute = TrailMateSampleData.importedTargetRoute)
            }
        }

        compose.onNodeWithTag("home-tab-路线").performClick()
        compose.onNodeWithText("继续准备").performScrollTo().performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("segmented-control-评估")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithTag("segmented-control-评估").performClick()
        compose.onNodeWithTag("route-assessment-decision").assertExists()
        compose.onAllNodesWithTag("route-cockpit").assertCountEquals(0)

        compose.onNodeWithContentDescription("返回路线准备").performClick()

        compose.onNodeWithText("路线准备").assertExists()
        compose.onNodeWithText("当前路线").assertExists()
        compose.onNodeWithText("继续准备").assertExists()
        compose.onAllNodesWithTag("route-cockpit").assertCountEquals(0)
        compose.onAllNodesWithText("保存路线包").assertCountEquals(0)
    }

    @Test
    fun routeImportEntryResetsOpenDetailToWorkspace() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen(initialImportedRoute = TrailMateSampleData.importedTargetRoute)
            }
        }

        compose.onNodeWithTag("home-tab-路线").performClick()
        compose.onNodeWithText("继续准备").performScrollTo().performClick()
        compose.onNodeWithTag("route-assessment-decision").assertExists()
        compose.onAllNodesWithText("保存路线包").assertCountEquals(0)

        compose.onNodeWithTag("home-tab-首页").performClick()
        compose.onNodeWithText("导入 GPX 文件").performClick()
        compose.waitForIdle()
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        compose.waitForIdle()

        compose.onNodeWithTag("home-tab-路线").performClick()
        compose.onNodeWithText("当前路线").assertExists()
        compose.onNodeWithText("继续准备").assertExists()
        compose.onAllNodesWithTag("route-cockpit").assertCountEquals(0)
        compose.onAllNodesWithText("保存路线包").assertCountEquals(0)
    }

    @Test
    fun dataAndProfileTabsOwnEvidenceAndPrivacyControls() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    initialImportedRoute = TrailMateSampleData.importedTargetRoute,
                    initialHistoricalActivities = TrailMateSampleData.historicalActivities,
                    initialTrackRecording = recordedTrack()
                )
            }
        }

        compose.onNodeWithTag("home-tab-数据").performClick()
        compose.onNodeWithText("本次活动复盘").assertExists()
        compose.onNodeWithText("龙井山脊").assertExists()
        compose.onNodeWithText("已记录 1.1 km", substring = true).assertExists()
        compose.onNodeWithText("历史活动").performScrollTo().assertExists()
        compose.onNodeWithText("历史资料").performScrollTo().assertExists()
        compose.onNodeWithText("已导入 3 条").assertExists()
        compose.onNodeWithText("导入历史 GPX").performScrollTo().assertExists()
        compose.onAllNodesWithText("供 AI 评估使用。").assertCountEquals(0)
        compose.onAllNodesWithText("清除本地数据").assertCountEquals(0)
        compose.onAllNodesWithText("龙井山脊 / 已记录 1.1 km / 2 个点").assertCountEquals(0)

        compose.onNodeWithTag("home-tab-我的").performClick()
        compose.onNodeWithText("基础档案").assertExists()
        compose.onAllNodesWithText("能力概览").assertCountEquals(0)
        compose.onNodeWithText("权限状态").performScrollTo().assertExists()
        compose.onNodeWithText("数据与隐私").performScrollTo().assertExists()
        compose.onNodeWithText("清除本地数据").performScrollTo().assertExists()
        compose.onAllNodesWithText("历史资料").assertCountEquals(0)
        compose.onAllNodesWithText("导入历史 GPX").assertCountEquals(0)
        compose.onAllNodesWithText("使用示例历史").assertCountEquals(0)
        compose.onAllNodesWithText("晨间山脊环线").assertCountEquals(0)
        compose.onAllNodesWithText("移除历史").assertCountEquals(0)
        compose.onAllNodesWithText("本次活动复盘").assertCountEquals(0)
    }

    @Test
    fun redesignedChinesePrototypeHasBottomNavigationAndWorkspaces() {
        val route = readyRoute()

        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    initialImportedRoute = route,
                    initialOfflineRoutePackKeys = setOf(
                        route.offlineRoutePackKey()
                    )
                )
            }
        }

        compose.onNodeWithText("首页").assertExists()
        compose.onNodeWithText("路线").assertExists()
        compose.onNodeWithTag("home-tab-装备").assertExists()
        compose.onNodeWithText("数据").assertExists()
        compose.onNodeWithText("我的").assertExists()
        compose.onNodeWithText("下午好，").assertExists()
        compose.onNodeWithText("准备走哪条线？").assertExists()
        compose.onNodeWithText("导入 GPX 文件").assertExists()
        compose.onNodeWithText("当前路线评估").assertExists()
        compose.onNodeWithText("快速开始").performScrollTo().assertExists()
        compose.onAllNodesWithText("今日概览").assertCountEquals(0)
        compose.onAllNodesWithText("0/3 GPX").assertCountEquals(0)

        compose.onNodeWithTag("home-tab-路线").performClick()
        compose.onNodeWithText("继续准备").performScrollTo().performClick()
        compose.onNodeWithTag("route-assessment-decision").assertExists()
        compose.onNodeWithText("路线评估").assertExists()
        compose.onNodeWithText("关键风险").assertExists()
        compose.onNodeWithText("检查装备").assertExists()
        compose.onNodeWithText("进入路线").performClick()
        compose.onNodeWithTag("route-cockpit").assertExists()
        compose.onNodeWithTag("route-cockpit-primary-action").assertExists()
        compose.onAllNodesWithTag("route-cockpit-readiness-strip").assertCountEquals(0)
        compose.onNodeWithText("授权定位").assertExists()
        compose.onAllNodesWithText("当前检查点").onFirst().assertExists()
        compose.onAllNodesWithText("仅提供路线辅助，不替代路标与离线地图").onFirst().assertExists()
        compose.onAllNodesWithText("全屏导航").assertCountEquals(0)
        compose.onAllNodesWithText("标记点").assertCountEquals(0)
        compose.onNodeWithText("评估").assertExists()
        compose.onAllNodesWithText("路线").onFirst().assertExists()
        compose.onNodeWithText("计划").assertExists()
    }

    @Test
    fun redesignedChinesePrototypeShowsGearCoachWorkspace() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen(initialImportedRoute = TrailMateSampleData.importedTargetRoute)
            }
        }

        compose.onAllNodesWithText("装备").onFirst().performClick()
        compose.onNodeWithText("装备匹配").assertExists()
        compose.onNodeWithText("路线需求").assertExists()
        compose.onNodeWithText("品牌候选").assertExists()
        compose.onNodeWithText("装备详情").assertExists()
        compose.onNodeWithText("AI 装备需求").assertExists()
        compose.onNodeWithText("路线装备需求").assertExists()
        compose.onAllNodesWithText("查看匹配").onFirst().assertExists()
        compose.onAllNodesWithText("我的装备").assertCountEquals(0)
        compose.onAllNodesWithText("添加已有装备").assertCountEquals(0)
        compose.onAllNodesWithText("保存到我的装备").assertCountEquals(0)
        compose.onAllNodesWithText("加入我的装备").assertCountEquals(0)
    }

    @Test
    fun routeCockpitPrimaryActionStaysAboveHomeBottomNavigation() {
        val route = readyRoute()

        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    initialImportedRoute = route,
                    initialOfflineRoutePackKeys = setOf(route.offlineRoutePackKey())
                )
            }
        }

        compose.onNodeWithTag("home-tab-路线").performClick()
        compose.onNodeWithText("继续准备").performScrollTo().performClick()
        compose.onNodeWithText("进入路线").performClick()

        val bottomNavigationTop = compose.onNodeWithTag("home-tab-路线")
            .getUnclippedBoundsInRoot()
            .top
        val primaryActionBottom = compose.onNodeWithTag("route-cockpit-primary-action")
            .getUnclippedBoundsInRoot()
            .bottom
        val safetyCopyBottom = compose.onAllNodesWithText("仅提供路线辅助，不替代路标与离线地图")
            .onFirst()
            .getUnclippedBoundsInRoot()
            .bottom

        assertTrue(
            "Primary action bottom $primaryActionBottom should stay above bottom navigation top $bottomNavigationTop",
            primaryActionBottom <= bottomNavigationTop
        )
        assertTrue(
            "Safety copy bottom $safetyCopyBottom should stay above bottom navigation top $bottomNavigationTop",
            safetyCopyBottom <= bottomNavigationTop
        )
    }

    @Test
    fun routeCockpitRecordingNavigationActionStaysAboveHomeBottomNavigation() {
        val route = readyRoute()
        val recording = TrackRecordingEngine.start(
            routeName = route.routeName,
            nowEpochMillis = 1_000L,
            routeKey = route.offlineRoutePackKey()
        )

        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    initialImportedRoute = route,
                    initialOfflineRoutePackKeys = setOf(route.offlineRoutePackKey()),
                    initialTrackRecording = recording
                )
            }
        }

        compose.onNodeWithTag("home-tab-路线").performClick()
        compose.onNodeWithText("继续准备").performScrollTo().performClick()
        compose.onNodeWithText("进入路线").performClick()

        val bottomNavigationTop = compose.onNodeWithTag("home-tab-路线")
            .getUnclippedBoundsInRoot()
            .top
        compose.onAllNodesWithTag("route-cockpit-fullscreen-shortcut").assertCountEquals(0)

        val primaryActionBottom = compose.onNodeWithTag("route-cockpit-primary-action")
            .getUnclippedBoundsInRoot()
            .bottom

        assertTrue(
            "Recording navigation action bottom $primaryActionBottom should stay above bottom navigation top $bottomNavigationTop",
            primaryActionBottom <= bottomNavigationTop
        )
    }

    @Test
    fun gearBrandCandidateDetailsButtonStaysAboveHomeBottomNavigation() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen(initialImportedRoute = TrailMateSampleData.importedTargetRoute)
            }
        }

        compose.onNodeWithTag("home-tab-装备").performClick()
        compose.onNodeWithTag("segmented-control-品牌候选").performClick()

        val bottomNavigationTop = compose.onNodeWithTag("home-tab-装备")
            .getUnclippedBoundsInRoot()
            .top
        val firstCandidateTitleBottom = compose.onNodeWithText("Leki Legacy Lite AS")
            .getUnclippedBoundsInRoot()
            .bottom
        val firstDetailsButtonBottom = compose.onAllNodesWithTag("gear-catalog-candidate-details")
            .onFirst()
            .getUnclippedBoundsInRoot()
            .bottom

        assertTrue(firstCandidateTitleBottom <= bottomNavigationTop)
        assertTrue(
            "Details button bottom $firstDetailsButtonBottom should stay above bottom navigation top $bottomNavigationTop",
            firstDetailsButtonBottom <= bottomNavigationTop
        )
    }

    @Test
    fun onboardingSavePersistsProfile() {
        val store = FakeTrailMateSessionRepository(
            TrailMateSnapshot(authSession = savedAuthSession())
        )

        compose.setContent {
            TrailMateTheme {
                TrailMateApp(sessionRepository = store)
            }
        }

        compose.onNodeWithText("身高 cm").performScrollTo().performTextInput("181")
        compose.onNodeWithText("体重 kg").performScrollTo().performTextInput("76")
        compose.onNodeWithText("常用背包 kg").performScrollTo().performTextInput("7")
        compose.onNodeWithText("保存档案").performScrollTo().performClick()
        compose.onNodeWithText("同意地图服务并继续").performScrollTo().performClick()

        assertEquals(181, store.snapshot.profile?.heightCm)
        assertEquals(76, store.snapshot.profile?.weightKg)
        assertEquals(7, store.snapshot.profile?.commonPackWeightKg)
        assertTrue(store.snapshot.amapPrivacyConsent.accepted)
    }

    @Test
    fun onboardingSkipDoesNotFabricateBodyMetrics() {
        val store = FakeTrailMateSessionRepository(
            TrailMateSnapshot(authSession = savedAuthSession())
        )

        compose.setContent {
            TrailMateTheme {
                TrailMateApp(sessionRepository = store)
            }
        }

        compose.onNodeWithText("暂时跳过").performScrollTo().performClick()
        compose.onNodeWithText("稍后，仅使用本地路线").performScrollTo().performClick()

        compose.onNodeWithText("准备走哪条线？").assertExists()
        compose.onAllNodesWithText("未填写").assertCountEquals(0)
        compose.onAllNodesWithText("背包待填").assertCountEquals(0)
        compose.onAllNodesWithText("172cm / 68kg").assertCountEquals(0)
        assertFalse(store.snapshot.amapPrivacyConsent.accepted)
    }

    @Test
    fun onboardingLocalOnlyCompletesWithoutLocationPermissionGate() {
        val store = FakeTrailMateSessionRepository(
            TrailMateSnapshot(authSession = savedAuthSession())
        )

        compose.setContent {
            TrailMateTheme {
                TrailMateApp(
                    sessionRepository = store,
                    requestOnboardingLocationPermission = true
                )
            }
        }

        compose.onNodeWithText("暂时跳过").performScrollTo().performClick()
        compose.onNodeWithText("稍后，仅使用本地路线").performScrollTo().performClick()

        compose.onNodeWithText("准备走哪条线？").assertExists()
        assertFalse(store.snapshot.amapPrivacyConsent.accepted)
    }

    @Test
    fun onboardingMapConsentCompletesWhenLocationPermissionIsAlreadyGranted() {
        grantRuntimePermission(Manifest.permission.ACCESS_FINE_LOCATION)
        grantRuntimePermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        val store = FakeTrailMateSessionRepository(
            TrailMateSnapshot(authSession = savedAuthSession())
        )

        compose.setContent {
            TrailMateTheme {
                TrailMateApp(
                    sessionRepository = store,
                    requestOnboardingLocationPermission = true
                )
            }
        }

        compose.onNodeWithText("身高 cm").performScrollTo().performTextInput("181")
        compose.onNodeWithText("体重 kg").performScrollTo().performTextInput("76")
        compose.onNodeWithText("保存档案").performScrollTo().performClick()
        compose.onNodeWithText("同意地图服务并继续").performScrollTo().performClick()

        compose.onNodeWithText("准备走哪条线？").assertExists()
        assertTrue(store.snapshot.amapPrivacyConsent.accepted)
        assertEquals(181, store.snapshot.profile?.heightCm)
        assertEquals(76, store.snapshot.profile?.weightKg)
    }

    @Test
    fun routeDetailShowsAssessmentRoutePlanAndGearTabs() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost()
            }
        }

        compose.onNodeWithText("评估").assertExists()
        compose.onNodeWithText("路线").assertExists()
        compose.onNodeWithText("计划").assertExists()
        compose.onAllNodesWithText("装备").onFirst().assertExists()
        compose.onNodeWithTag("route-assessment-decision").assertExists()
        compose.onNodeWithText("路线评估").assertExists()
        compose.onAllNodesWithText("谨慎尝试").onFirst().assertExists()
        compose.onNodeWithText("检查装备").assertExists()
        compose.onNodeWithText("进入路线").assertExists()
        compose.onNodeWithText("关键风险").assertExists()

        compose.onNodeWithTag("segmented-control-计划").performClick()
        compose.onNodeWithText("补给与休息计划", substring = true).assertExists()

        compose.onNodeWithTag("segmented-control-装备").performClick()
        compose.onNodeWithText("路线装备需求").assertExists()
    }

    @Test
    fun routeDetailCompactsDurationRangeMetric() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost(
                    routeAssessment = TrailMateSampleData.routeAssessment.copy(
                        estimatedDurationRange = "12:14-19:26"
                    )
                )
            }
        }

        compose.onNodeWithText("12-20h").assertExists()
        compose.onAllNodesWithText("12:14-19:26").assertCountEquals(0)
    }

    @Test
    fun routeReadinessCanSaveOfflineRoutePack() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost()
            }
        }

        compose.onNodeWithTag("segmented-control-计划").performClick()
        compose.onNodeWithTag("route-readiness-offline-route-pack")
            .performScrollTo()
            .performClick()

        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText("离线路线：已保存", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithText("离线路线：已保存", substring = true).assertExists()
    }

    @Test
    fun routeCockpitCompactActionsKeepOfflineAndGearAccessible() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost()
            }
        }

        compose.onNodeWithTag("segmented-control-路线").performClick()
        compose.onNodeWithTag("route-cockpit-offline-route-action")
            .performScrollTo()
            .assertHasClickAction()
        compose.onNodeWithTag("route-cockpit-offline-basemap-action")
            .performScrollTo()
            .assertHasClickAction()

        compose.onNodeWithTag("segmented-control-装备").performScrollTo().performClick()
        compose.onNodeWithText("AI 装备需求").assertExists()
    }

    @Test
    fun appPersistsOfflineRoutePackReadiness() {
        val store = FakeTrailMateSessionRepository(
            TrailMateSnapshot(
                authSession = savedAuthSession(),
                profile = savedProfile(),
                importedRoute = TrailMateSampleData.importedTargetRoute
            )
        )

        compose.setContent {
            TrailMateTheme {
                TrailMateApp(sessionRepository = store)
            }
        }

        compose.onNodeWithTag("home-tab-路线").performClick()
        compose.onNodeWithText("继续准备").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("route-assessment-decision").assertExists()
        compose.onNodeWithTag("segmented-control-路线").performClick()
        compose.onNodeWithTag("route-cockpit-offline-route-action")
            .performScrollTo()
            .performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            store.snapshot.savedOfflineRoutePackKeys.contains(
                TrailMateSampleData.importedTargetRoute.offlineRoutePackKey()
            )
        }

        assertTrue(
            store.snapshot.savedOfflineRoutePackKeys.contains(
                TrailMateSampleData.importedTargetRoute.offlineRoutePackKey()
            )
        )
    }

    @Test
    fun gearScreenShowsServerCatalogMatchesAndThumbnails() {
        compose.setContent {
            TrailMateTheme {
                GearMatchScreen(
                    routeGearRecommendations = TrailMateSampleData.gearRecommendations,
                    requestedCategory = "登山杖",
                    catalogStatusLabel = "服务端品牌库"
                )
            }
        }

        compose.onNodeWithText("装备匹配").assertExists()
        compose.onAllNodesWithText("服务端品牌库").onFirst().assertExists()
        compose.onAllNodesWithText("龙井山脊 · 谨慎尝试").assertCountEquals(0)
        compose.onNodeWithText("路线需求").assertExists()
        compose.onNodeWithText("品牌候选").performClick()
        compose.onNodeWithText("品牌候选装备").assertExists()
        compose.onNodeWithText("当前需求：登山杖").assertExists()
        compose.onNodeWithText("Leki Legacy Lite AS").assertExists()
        compose.onAllNodesWithText("类别").assertCountEquals(0)
        compose.onAllNodesWithText("添加已有装备").assertCountEquals(0)
        compose.onAllNodesWithText("保存到我的装备").assertCountEquals(0)
        compose.onAllNodesWithText("加入我的装备").assertCountEquals(0)
    }

    @Test
    fun gearScreenEmptyCatalogMatchReturnsToRouteNeedsWithoutInventoryFallback() {
        compose.setContent {
            TrailMateTheme {
                GearMatchScreen(
                    routeGearRecommendations = listOf(
                        GearRecommendation(
                            category = "冰爪",
                            status = GearStatus.OPTIONAL,
                            rationale = "低温湿滑路面时作为备用抓地装备。"
                        )
                    ),
                    requestedCategory = "冰爪",
                    catalogItems = emptyList(),
                    catalogStatusLabel = "服务端品牌库",
                    catalogStatusCaption = "品牌、型号和缩略图由服务端统一维护。"
                )
            }
        }

        compose.onNodeWithText("品牌候选装备").assertExists()
        compose.onNodeWithText("服务端暂未收录冰爪", substring = true).assertExists()
        compose.onNodeWithText("返回路线需求").assertExists().performClick()
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText("路线装备需求")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithText("路线装备需求").assertExists()
        compose.onAllNodesWithText("添加已有装备").assertCountEquals(0)
        compose.onAllNodesWithText("保存到我的装备").assertCountEquals(0)
        compose.onAllNodesWithText("加入我的装备").assertCountEquals(0)
    }

    @Test
    fun gearDetailsTabShowsSelectedCatalogItemReadOnly() {
        compose.setContent {
            TrailMateTheme {
                GearMatchScreen(
                    routeGearRecommendations = TrailMateSampleData.gearRecommendations,
                    requestedCategory = "雨衣（防水透气）"
                )
            }
        }

        compose.onNodeWithText("装备详情").performClick()

        compose.onNodeWithText("雨衣（防水透气）").assertExists()
        compose.onNodeWithText("Arc'teryx Beta LT Jacket").assertExists()
        compose.onNodeWithText("缩略图").assertExists()
        compose.onAllNodesWithText("我的装备").assertCountEquals(0)
        compose.onAllNodesWithText("添加已有装备").assertCountEquals(0)
        compose.onAllNodesWithText("保存到我的装备").assertCountEquals(0)
        compose.onAllNodesWithText("加入我的装备").assertCountEquals(0)
    }

    @Test
    fun routeGearTabShowsMatchedServerCatalogGear() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost()
            }
        }

        compose.onNodeWithTag("segmented-control-装备").performClick()
        compose.onNodeWithText("AI 装备需求").assertExists()
        compose.onNodeWithText("规则清单就绪").assertExists()
        compose.onNodeWithText("路线装备需求", substring = true).assertExists()
        compose.onNodeWithText("雨衣").assertExists()
        compose.onNodeWithText("已匹配 Arc'teryx Beta LT Jacket", substring = true).assertExists()
        compose.onNodeWithText("备用水").assertExists()
        compose.onAllNodesWithText("查看候选").onFirst().assertExists()
        compose.onAllNodesWithText("我的装备").assertCountEquals(0)
    }

    @Test
    fun routeGearTabMarksStaleAiResponseAndKeepsFallbackChecklist() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost(
                    aiGearAdvisorResponse = AiGearAdvisorResponse(
                        assessmentFingerprint = "old-route",
                        recommendations = listOf(
                            GearRecommendation(
                                category = "Avalanche beacon",
                                status = GearStatus.MISSING,
                                rationale = "Old winter route response."
                            )
                        )
                    )
                )
            }
        }

        compose.onNodeWithText("装备").performClick()

        compose.onNodeWithText("响应已过期").assertExists()
        compose.onNodeWithText("另一条路线", substring = true).assertExists()
        compose.onNodeWithText("雨衣").assertExists()
        compose.onAllNodesWithText("Avalanche beacon").assertCountEquals(0)
    }

    @Test
    fun routePlanTabShowsDynamicCheckpoints() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost()
            }
        }

        compose.onNodeWithTag("segmented-control-计划").performClick()

        compose.onNodeWithText("行程节奏").assertExists()
        compose.onNodeWithText("补给与休息计划", substring = true).assertExists()
        compose.onNodeWithText("天气与返程判断").assertExists()
        compose.onNodeWithText("检查点时间线").assertExists()
        compose.onNodeWithText("补给检查", substring = true).assertExists()
        compose.onAllNodesWithText("Plan checkpoints").assertCountEquals(0)
    }

    @Test
    fun fullscreenNavigationStartsFromFullscreenAndShowsFieldControls() {
        grantRouteRuntimePermissions()

        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost(
                    notificationPermissionGranted = true,
                    initialLocationSnapshot = locatedTrailSnapshot(),
                    routeAssessment = nonBlockingAssessment(TrailMateSampleData.importedTargetRoute),
                    gearRecommendations = coveredDepartureGear(),
                    initialOfflineRoutePackReady = true
                )
            }
        }

        compose.onNodeWithTag("segmented-control-路线").performClick()
        compose.onNodeWithTag("route-cockpit").assertExists()
        compose.onAllNodesWithText("开始徒步并记录轨迹").assertCountEquals(0)
        compose.onNodeWithText("进入导航").assertExists()
        compose.onAllNodesWithText("安全分享").assertCountEquals(0)
        compose.onAllNodesWithText("全屏导航").assertCountEquals(0)
        compose.onNodeWithTag("route-cockpit-primary-action").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("route-navigation-fullscreen").assertExists()
        compose.onNodeWithText("开始徒步并记录轨迹").assertExists()
        compose.onNodeWithTag("route-navigation-fullscreen-primary-action").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("标记点").assertExists()
        compose.onNodeWithTag("route-navigation-fullscreen-safety-action").assertExists()
        compose.onAllNodesWithText("补给检查", substring = true).onFirst().assertExists()
        compose.onAllNodesWithText("补水、补能量", substring = true).onFirst().assertExists()
    }

    @Test
    fun expandedRouteDetailsDoNotShowDepartureStartControl() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost(
                    notificationPermissionGranted = true,
                    initialLocationSnapshot = locatedTrailSnapshot(),
                    routeAssessment = nonBlockingAssessment(TrailMateSampleData.importedTargetRoute),
                    gearRecommendations = coveredDepartureGear(),
                    initialOfflineRoutePackReady = true,
                    initiallyExpandRouteDiagnostics = true
                )
            }
        }

        compose.onNodeWithTag("segmented-control-路线").performClick()
        compose.onNodeWithText("现场详情").assertExists()
        compose.onAllNodesWithText("下一步：开始徒步并记录轨迹").assertCountEquals(0)
        compose.onAllNodesWithText("标记点", substring = true).assertCountEquals(0)
    }

    @Test
    fun routeTabShowsGpsAndTrackRecordingControls() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost(notificationPermissionGranted = false)
            }
        }

        compose.onNodeWithTag("segmented-control-路线").performClick()
        compose.waitForIdle()

        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithTag("route-cockpit")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        compose.onNodeWithTag("route-cockpit").assertExists()
        compose.onNodeWithTag("route-cockpit-primary-action").assertExists()
        compose.onAllNodesWithTag("route-cockpit-readiness-strip").assertCountEquals(0)
        compose.onAllNodesWithText("开始徒步").assertCountEquals(0)
        compose.onAllNodesWithText("标记点").assertCountEquals(0)
        compose.onAllNodesWithText("全屏导航").assertCountEquals(0)
        compose.onAllNodesWithText("安全分享").assertCountEquals(0)
        compose.onNodeWithTag("route-cockpit-offline-route-action").assertHasClickAction()
        compose.onAllNodesWithText("地图状态").assertCountEquals(0)
        compose.onAllNodesWithText("地图状态与轻导航").assertCountEquals(0)
        compose.onAllNodesWithText("地图与离线").assertCountEquals(0)
        compose.onAllNodesWithText("本地路线、定位、离线包和图层说明").assertCountEquals(0)
        compose.onNodeWithText("检查点与补给").assertExists()
        compose.onNodeWithText("5 个检查点 · 补给/休息/风险").assertExists()
        compose.onAllNodesWithText("现场状态").assertCountEquals(0)
        compose.onAllNodesWithText("准备轻导航").assertCountEquals(0)
        compose.onAllNodesWithText("定位：", substring = true).assertCountEquals(0)
        compose.onAllNodesWithText("记录：", substring = true).assertCountEquals(0)
        compose.onAllNodesWithText("离线路线：", substring = true).assertCountEquals(0)
        compose.onAllNodesWithText("先授权定位；出发前建议保存路线包并允许轨迹通知。").assertCountEquals(0)
        compose.onAllNodesWithText("高德上线检查").assertCountEquals(0)
        compose.onAllNodesWithText("地图图层").assertCountEquals(0)
        compose.onAllNodesWithText("位置可靠性").assertCountEquals(0)

        compose.onNodeWithText("检查点与补给").performScrollTo().performClick()
        compose.onNodeWithText("现场详情").assertExists()
        compose.onNodeWithText("定位：未启用").assertExists()
        compose.onNodeWithText("轨迹：0 点").assertExists()
        compose.onNodeWithText("通知：待允许").assertExists()
        compose.onNodeWithText("高德上线检查").performScrollTo().assertExists()
        compose.onNodeWithText("Android Key：待配置").assertExists()
        compose.onNodeWithText("Package/SHA1：待绑定").assertExists()
        compose.onNodeWithText("离线底图：待配置").assertExists()
        compose.onNodeWithText("位置可靠性").performScrollTo().assertExists()
        compose.onAllNodesWithText("开始徒步").assertCountEquals(0)
        compose.onAllNodesWithText("标记下一检查点").assertCountEquals(0)
        compose.onAllNodesWithText("当前位置").onFirst().assertExists()
        compose.onNodeWithText("授权定位后开始校准").assertExists()
        compose.onNodeWithText("定位精度").assertExists()
        compose.onNodeWithText("路线匹配").assertExists()
        compose.onNodeWithText("最近更新").assertExists()
        compose.onNodeWithText("路线校验").assertExists()
        compose.onNodeWithText("等待定位推进").assertExists()
        compose.onAllNodesWithText("未开始").onFirst().assertExists()
        compose.onAllNodesWithText("授权定位").onFirst().assertExists()
        compose.onAllNodesWithText("开始记录").assertCountEquals(0)
        compose.onAllNodesWithText("暂停记录").assertCountEquals(0)
        compose.onAllNodesWithText("继续记录").assertCountEquals(0)
        compose.onAllNodesWithText("结束记录").assertCountEquals(0)
        compose.onNodeWithText("已记录 0.0 km / 0 个点").assertExists()
        compose.onNodeWithText("轨迹通知").assertExists()
        compose.onNodeWithText("允许通知").assertExists()
        compose.onNodeWithText("锁屏或切后台时可看到记录状态", substring = true).assertExists()
        compose.onNodeWithText("前台服务记录", substring = true).assertExists()
        compose.onNodeWithText("前台服务记录真实定位轨迹", substring = true).assertExists()
        compose.onAllNodesWithText("安全分享").assertCountEquals(0)
        compose.onAllNodesWithText("等待定位后分享").assertCountEquals(0)
        compose.onAllNodesWithText("授权定位后可分享当前位置", substring = true).assertCountEquals(0)
        compose.onAllNodesWithText("发送出发报备").assertCountEquals(0)
        compose.onNodeWithText("地图准备").assertExists()
        compose.onNodeWithText("离线底图待准备").assertExists()
        compose.onAllNodesWithText("离线地图包").assertCountEquals(0)
        compose.onAllNodesWithText("本地 GPX", substring = true).onFirst().assertExists()
        compose.onNodeWithText("出发检查").assertExists()
        compose.onAllNodesWithText("出发前还差", substring = true).onFirst().assertExists()
        compose.onNodeWithText("建议补齐").assertExists()
        compose.onAllNodesWithText("离线路线").onFirst().assertExists()
        compose.onAllNodesWithText("离线底图").onFirst().assertExists()
        compose.onAllNodesWithText("导入离线地图包").assertCountEquals(0)
        compose.onAllNodesWithText("下一步：导入离线地图包").assertCountEquals(0)
        compose.onAllNodesWithText("装备").onFirst().assertExists()
        compose.onAllNodesWithText("路线提示点").onFirst().assertExists()
        compose.onNodeWithText("地图图层").performScrollTo().assertExists()
        compose.onNodeWithText("计划路线").assertExists()
        compose.onAllNodesWithText("未记录").onFirst().assertExists()
        compose.onAllNodesWithText("补给", substring = true).onFirst().assertExists()
        compose.onAllNodesWithText("休息", substring = true).onFirst().assertExists()
        compose.onAllNodesWithText("风险", substring = true).onFirst().assertExists()
    }

    @Test
    fun homeRouteFullscreenFocusesNavigationAndHidesBottomBar() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost(
                    route = readyRoute(),
                    notificationPermissionGranted = true,
                    initialLocationSnapshot = locatedTrailSnapshot(),
                    routeAssessment = nonBlockingAssessment(readyRoute()),
                    gearRecommendations = coveredDepartureGear(),
                    initialOfflineRoutePackReady = true
                )
            }
        }

        compose.onNodeWithTag("segmented-control-路线").performClick()
        compose.onAllNodesWithText("开始徒步并记录轨迹").assertCountEquals(0)
        compose.onNodeWithText("进入导航").assertExists()
        compose.onAllNodesWithText("全屏导航").assertCountEquals(0)
        compose.onNodeWithTag("route-cockpit-primary-action").performClick()
        compose.waitForIdle()

        compose.onNodeWithTag("route-navigation-fullscreen").assertExists()
        compose.onNodeWithTag("route-navigation-fullscreen-primary-action").assertExists()
        val fullscreenBounds = compose.onNodeWithTag("route-navigation-fullscreen").getUnclippedBoundsInRoot()
        val dockBounds = compose.onNodeWithTag("route-navigation-fullscreen-dock")
            .assertIsDisplayed()
            .getUnclippedBoundsInRoot()
        assertTrue(dockBounds.top < fullscreenBounds.bottom)
        assertTrue(dockBounds.bottom <= fullscreenBounds.bottom)

        compose.onNodeWithContentDescription("退出全屏导航").performClick()
        compose.onNodeWithTag("route-cockpit").assertExists()
    }

    @Test
    fun routeTabShowsOffRouteRecoveryGuidance() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost(
                    notificationPermissionGranted = true,
                    initialLocationSnapshot = TrailMateLocationSnapshot(
                        status = TrailMateLocationStatus.LOCATED,
                        latitude = 30.25,
                        longitude = 120.15,
                        elevationMeters = 142.0,
                        horizontalAccuracyMeters = 8.0,
                        timestampEpochMillis = 1_000L
                    ),
                    initialLocationGuidanceStatus = LocationBackedHikeStatus.CHECK_ROUTE,
                    initialLocationGuidanceCaption = "当前位置距计划路线约 112 m，请核对地图、路标和现场路径。",
                    initialLocationFix = HikeLocationFix(
                        distanceAlongRouteKm = 5.12,
                        crossTrackErrorMeters = 112.0,
                        horizontalAccuracyMeters = 8.0,
                        timestampEpochMillis = 1_000L
                    ),
                    routeAssessment = nonBlockingAssessment(TrailMateSampleData.importedTargetRoute),
                    gearRecommendations = coveredDepartureGear(),
                    initialOfflineRoutePackReady = true,
                    initiallyExpandRouteDiagnostics = true
                )
            }
        }

        compose.onNodeWithTag("segmented-control-路线").performClick()

        compose.onNodeWithText("收起").assertExists()
        compose.onNodeWithText("偏离恢复").performScrollTo().assertExists()
        compose.onNodeWithText("停止自动推进").assertExists()
        compose.onNodeWithText("疑似偏离路线约 112 m", substring = true).assertExists()
        compose.onAllNodesWithText("回到最近路线").onFirst().assertExists()
        compose.onNodeWithText("查看安全退出").assertExists()
    }

    @Test
    fun routeTabConfirmsWhenUserRejoinsRouteAfterDeviation() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost(
                    notificationPermissionGranted = true,
                    initialLocationSnapshot = TrailMateLocationSnapshot(
                        status = TrailMateLocationStatus.LOCATED,
                        latitude = 30.25,
                        longitude = 120.15,
                        elevationMeters = 142.0,
                        horizontalAccuracyMeters = 7.0,
                        timestampEpochMillis = 1_000L
                    ),
                    initialLocationGuidanceStatus = LocationBackedHikeStatus.ON_ROUTE,
                    initialLocationGuidanceCaption = "已对齐「补给检查」，路线进度 5.4 km。",
                    initialLocationFix = HikeLocationFix(
                        distanceAlongRouteKm = 5.38,
                        crossTrackErrorMeters = 18.0,
                        horizontalAccuracyMeters = 7.0,
                        timestampEpochMillis = 1_000L
                    ),
                    initialWasRecentlyOffRoute = true
                )
            }
        }

        compose.onNodeWithTag("segmented-control-路线").performClick()

        compose.onNodeWithText("检查点与补给").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithText("已回到路线").performScrollTo().assertExists()
        compose.onNodeWithText("可继续推进").assertExists()
        compose.onNodeWithText("确认下一检查点").assertExists()
        compose.onAllNodesWithText("继续导航").onFirst().assertExists()
    }

    @Test
    fun routeDiagnosticsShowsTrackRecordingPanel() {
        val recording = TrackRecordingEngine.appendLocation(
            state = TrackRecordingEngine.appendLocation(
                state = TrackRecordingEngine.start(
                    routeName = "龙井山脊",
                    nowEpochMillis = 1_000L,
                    routeKey = TrailMateSampleData.importedTargetRoute.offlineRoutePackKey()
                ),
                point = RecordedTrackPoint(
                    latitude = 30.00,
                    longitude = 120.00,
                    elevationMeters = 100.0,
                    horizontalAccuracyMeters = 8.0,
                    timestampEpochMillis = 1_100L
                ),
                nowEpochMillis = 1_100L
            ),
            point = RecordedTrackPoint(
                latitude = 30.01,
                longitude = 120.01,
                elevationMeters = 110.0,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 421_100L
            ),
            nowEpochMillis = 421_100L
        )

        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost(
                    initialTrackRecording = recording,
                    initiallyExpandRouteDiagnostics = true
                )
            }
        }

        compose.onNodeWithTag("segmented-control-路线").performClick()

        compose.onAllNodesWithText("轨迹记录").onFirst().assertExists()
        compose.onAllNodesWithText("前台服务记录真实定位轨迹", substring = true).onFirst().assertExists()
    }

    @Test
    fun routeMapLocationToolIsActionable() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost()
            }
        }

        compose.onNodeWithTag("segmented-control-路线").performClick()

        compose.onNodeWithContentDescription("定位").assertHasClickAction()
    }

    @Test
    fun routeMapDoesNotExposeCompassToolBeforeHeadingSupport() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost()
            }
        }

        compose.onNodeWithTag("segmented-control-路线").performClick()

        compose.onAllNodesWithContentDescription("校准方向").assertCountEquals(0)
    }

    @Test
    fun routeTabShowsFinishedTrackReviewCard() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost(initialTrackRecording = recordedTrack())
            }
        }

        compose.onNodeWithTag("segmented-control-路线").performClick()
        compose.onNodeWithText("检查点与补给").performScrollTo().performClick()

        val reviewCard = hasAnyAncestor(hasTestTag("track-recording-review"))
        compose.onNodeWithTag("track-recording-review").performScrollTo().assertExists()
        compose.onNode(hasText("轨迹已保存") and reviewCard).assertExists()
        compose.onNode(hasText("龙井山脊") and reviewCard).assertExists()
        compose.onNode(hasText("1.1 km") and reviewCard).assertExists()
        compose.onNode(hasText("2 点") and reviewCard).assertExists()
        compose.onNode(hasText("7 分") and reviewCard).assertExists()
        compose.onNode(hasText("可在数据页复盘本次路线表现。") and reviewCard).assertExists()
        compose.onNode(hasText("去数据页复盘") and reviewCard).assertExists()
    }

    @Test
    fun routeTabKeepsEmbeddedMapHintsOutOfTheRouteWorkspace() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost()
            }
        }

        compose.onNodeWithTag("segmented-control-路线").performClick()
        compose.onAllNodesWithText("补给 ·", substring = true).assertCountEquals(0)
        compose.onAllNodesWithText("提示点详情").assertCountEquals(0)
    }

    @Test
    fun activeHikeResetsWhenRouteChanges() {
        grantRouteRuntimePermissions()
        var route by mutableStateOf(TrailMateSampleData.importedTargetRoute)
        var routeSavedForCurrentRoute by mutableStateOf(true)

        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost(
                    route = route,
                    notificationPermissionGranted = true,
                    initialLocationSnapshot = locatedTrailSnapshot(),
                    routeAssessment = nonBlockingAssessment(route),
                    gearRecommendations = coveredDepartureGear(),
                    initialOfflineRoutePackReady = routeSavedForCurrentRoute
                )
            }
        }

        compose.onNodeWithTag("segmented-control-路线").performClick()
        compose.onAllNodesWithText("开始徒步并记录轨迹").assertCountEquals(0)
        compose.onNodeWithText("进入导航").assertExists()
        compose.onAllNodesWithText("全屏导航").assertCountEquals(0)
        compose.onNodeWithTag("route-cockpit-primary-action").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("route-navigation-fullscreen").assertExists()
        compose.onNodeWithContentDescription("退出全屏导航").performClick()
        compose.onNodeWithTag("route-cockpit").assertExists()
        compose.onAllNodesWithText("标记点").assertCountEquals(0)

        compose.runOnIdle {
            routeSavedForCurrentRoute = false
            route = route.copy(
                routeName = "替换路线",
                fileName = "replacement.gpx",
                distanceKm = 4.8,
                ascentMeters = 180,
                pointCount = 64
            )
        }

        compose.onNodeWithText("替换路线").assertExists()
        compose.onNodeWithTag("route-cockpit-offline-route-action").assertExists()
        compose.onAllNodesWithText("全屏导航").assertCountEquals(0)
        compose.onAllNodesWithText("暂停").assertCountEquals(0)
    }

    @Test
    fun homeGearFlowPrefillsCategoryAndShowsCatalogMatches() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    initialImportedRoute = TrailMateSampleData.importedTargetRoute
                )
            }
        }

        compose.onAllNodesWithText("装备").onFirst().performClick()
        compose.onAllNodesWithText("装备").onFirst().assertExists()
        compose.onAllNodesWithText("登山杖").assertCountEquals(1)
        compose.onNodeWithText("品牌候选").performClick()
        compose.onNodeWithText("品牌候选装备").assertExists()
        compose.onNodeWithText("当前需求：登山杖").assertExists()
        compose.onNodeWithText("Leki Legacy Lite AS").assertExists()
        compose.onAllNodesWithText("类别").assertCountEquals(0)
        compose.onAllNodesWithText("保存到我的装备").assertCountEquals(0)
        compose.onAllNodesWithText("加入我的装备").assertCountEquals(0)

    }

    @Test
    fun homeNotifiesPersistenceWhenRouteChangesAndGearUsesCatalogOnly() {
        var savedRoute: ImportedRoute? = null
        val savedQueues = mutableListOf<GpxImportQueue>()

        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    profile = TrailMateSampleData.baselineProfile,
                    showSampleRouteAction = true,
                    onRouteImported = { route -> savedRoute = route },
                    onGpxImportQueueChanged = { queue -> savedQueues += queue }
                )
            }
        }

        compose.onNodeWithTag("home-tab-路线").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("sample-route-button").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithText("首页").performClick()
        compose.onAllNodesWithText("装备").onFirst().performClick()
        compose.onNodeWithText("品牌候选").performClick()
        compose.onNodeWithText("Leki Legacy Lite AS").assertExists()
        compose.onAllNodesWithText("类别").assertCountEquals(0)
        compose.onAllNodesWithText("保存到我的装备").assertCountEquals(0)
        compose.onAllNodesWithText("加入我的装备").assertCountEquals(0)

        assertTrue(savedQueues.any { queue ->
            queue.jobs.any { job ->
                job.kind == GpxImportJobKind.TARGET_ROUTE &&
                    job.fileName == "longjing-ridge-target.gpx" &&
                    job.status == GpxImportJobStatus.RUNNING
            }
        })
        val queueDebug = savedQueues.joinToString { queue ->
            queue.jobs.joinToString { job -> "${job.fileName}:${job.status}:${job.lastError}" }
        }
        assertTrue(
            "Queues: $queueDebug",
            savedQueues.any { queue ->
                queue.jobs.any { job ->
                    job.kind == GpxImportJobKind.TARGET_ROUTE &&
                        job.fileName == "longjing-ridge-target.gpx" &&
                        job.status == GpxImportJobStatus.SUCCEEDED
                }
            }
        )
        assertEquals("龙井山脊", savedRoute?.routeName)
    }

    @Test
    fun homeGearTabLabelsConnectedServerCatalogAsServerOwned() {
        val gearCatalogApi = FakeGearCatalogApi()

        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    profile = TrailMateSampleData.baselineProfile,
                    gearCatalogApi = gearCatalogApi
                )
            }
        }

        compose.waitUntil(timeoutMillis = 5_000) { gearCatalogApi.searchCalls > 0 }
        compose.onAllNodesWithText("装备").onFirst().performClick()

        compose.onAllNodesWithText("服务端品牌库").onFirst().assertExists()
        compose.onNodeWithText("已同步 1 件品牌装备", substring = true).assertExists()
        compose.onAllNodesWithText("品牌库 · 服务端").assertCountEquals(0)
        compose.onAllNodesWithText("我的装备").assertCountEquals(0)
    }

    @Test
    fun homeGearTabShowsRetryWhenServerCatalogUnavailable() {
        val gearCatalogApi = FakeGearCatalogApi(
            searchResultProvider = {
                TrailMateApiResult.Failure(
                    TrailMateApiError(
                        status = 503,
                        code = "GEAR_CATALOG_UNAVAILABLE",
                        message = "服务端品牌库暂时不可用。",
                        traceId = null
                    )
                )
            }
        )

        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    profile = TrailMateSampleData.baselineProfile,
                    gearCatalogApi = gearCatalogApi
                )
            }
        }

        compose.waitUntil(timeoutMillis = 5_000) { gearCatalogApi.searchCalls > 0 }
        compose.onAllNodesWithText("装备").onFirst().performClick()

        compose.onAllNodesWithText("品牌库缓存").onFirst().assertExists()
        compose.onNodeWithText("服务端品牌库暂时不可用", substring = true).assertExists()
        compose.onNodeWithTag("gear-catalog-retry").assertExists().performClick()
        compose.waitUntil(timeoutMillis = 5_000) { gearCatalogApi.searchCalls > 1 }
        compose.onAllNodesWithText("我的装备").assertCountEquals(0)
        compose.onAllNodesWithText("添加已有装备").assertCountEquals(0)
    }

    @Test
    fun profileTabShowsDataPrivacyClearControls() {
        val initialSnapshot = TrailMateSnapshot(
            authSession = savedAuthSession(),
            profile = savedProfile(),
            importedRoute = TrailMateSampleData.importedTargetRoute
        )
        val store = FakeTrailMateSessionRepository(
            initialSnapshot
        )

        compose.setContent {
            TrailMateTheme {
                TrailMateApp(sessionRepository = store)
            }
        }

        compose.onNodeWithTag("home-tab-数据").performClick()
        compose.onNodeWithText("完成一次记录后会出现复盘").assertExists()
        compose.onNodeWithText("去路线页").assertExists()
        compose.onNodeWithText("历史资料").performScrollTo().assertExists()
        compose.onNodeWithText("已导入 0 条").assertExists()
        compose.onNodeWithText("导入历史 GPX").performScrollTo().assertExists()
        compose.onAllNodesWithText("清除本地数据").assertCountEquals(0)

        compose.onNodeWithTag("home-tab-我的").performClick()
        compose.onNodeWithText("数据与隐私").performScrollTo().assertExists()
        compose.onAllNodesWithText("历史资料").assertCountEquals(0)
        compose.onAllNodesWithText("导入历史 GPX").assertCountEquals(0)
        compose.onAllNodesWithText("使用示例历史").assertCountEquals(0)
        compose.onNodeWithText("清除本地数据").performScrollTo().performClick()

        compose.onNodeWithText("清除本地数据？").performScrollTo().assertExists()
        compose.onNodeWithText("确认清除").performScrollTo().assertExists()
        compose.onNodeWithText("取消").performScrollTo().assertExists()
        assertEquals(initialSnapshot, store.snapshot)

        compose.onNodeWithText("取消").performClick()
        compose.onAllNodesWithText("确认清除").assertCountEquals(0)

        compose.onNodeWithText("清除本地数据").performScrollTo().performClick()
        compose.onNodeWithText("确认清除").performScrollTo().performClick()

        assertEquals(TrailMateSnapshot.empty(), store.snapshot)
        compose.onNodeWithText("TrailMate").assertExists()
        compose.onNodeWithText("手机号登录 / 注册").assertExists()
        compose.onAllNodesWithTag("home-tab-数据").assertCountEquals(0)
        compose.onAllNodesWithText("清除本地数据").assertCountEquals(0)
        compose.onAllNodesWithText("龙井山脊 / 15.2 km / +860 m").assertCountEquals(0)
    }

    @Test
    fun dataTabShowsRecordedTrackReviewAndTrendSummary() {
        val store = FakeTrailMateSessionRepository(
            TrailMateSnapshot(
                authSession = savedAuthSession(),
                profile = savedProfile(),
                latestTrackRecording = recordedTrack()
            )
        )

        compose.setContent {
            TrailMateTheme {
                TrailMateApp(sessionRepository = store)
            }
        }

        compose.onNodeWithText("数据").performClick()

        compose.onNodeWithText("本次活动复盘").performScrollTo().assertExists()
        compose.onNodeWithText("龙井山脊").assertExists()
        compose.onNodeWithText("已记录 1.1 km", substring = true).assertExists()
        compose.onNodeWithText("历史活动").performScrollTo().assertExists()
        compose.onNodeWithText("活动趋势").assertExists()
        compose.onAllNodesWithText("导出 GPX").assertCountEquals(0)
        compose.onAllNodesWithText("龙井山脊 / 已记录 1.1 km / 2 个点").assertCountEquals(0)
    }

    @Test
    fun dataTabReflectsForegroundServiceTrackRecordingBroadcast() {
        val store = FakeTrailMateSessionRepository(
            TrailMateSnapshot(
                authSession = savedAuthSession(),
                profile = savedProfile()
            )
        )
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        compose.setContent {
            TrailMateTheme {
                TrailMateApp(sessionRepository = store)
            }
        }
        compose.waitForIdle()

        context.sendBroadcast(
            Intent(TrackRecordingForegroundService.ACTION_RECORDING_CHANGED)
                .setPackage(context.packageName)
                .putExtra(
                    TrackRecordingForegroundService.EXTRA_RECORDING_PAYLOAD,
                    TrackRecordingBroadcastCodec.encode(recordedTrack())
                )
        )
        compose.waitForIdle()
        compose.onNodeWithText("数据").performClick()

        compose.onNodeWithText("本次活动复盘").performScrollTo().assertExists()
        compose.onNodeWithText("龙井山脊").assertExists()
        compose.onNodeWithText("已记录 1.1 km", substring = true).assertExists()
        assertEquals(2, store.snapshot.latestTrackRecording.pointCount)
    }

    @Test
    fun homeRequiresRouteImportBeforeShowingRouteDetail() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen(showSampleRouteAction = true)
            }
        }

        compose.onNodeWithTag("home-tab-路线").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("尚未导入路线").assertExists()
        compose.onNodeWithText("等待 GPX").assertExists()
        compose.onNodeWithText("导入 GPX 文件").assertExists()
        compose.onAllNodesWithText("15.2 km").assertCountEquals(0)
        compose.onAllNodesWithText("评估").assertCountEquals(0)

        compose.onNodeWithTag("sample-route-button").performScrollTo().performClick()
        compose.waitForIdle()

        compose.onNodeWithText("当前路线").performScrollTo().assertExists()
        compose.onNodeWithText("GPX 就绪").assertExists()
        compose.onNodeWithText("轨迹点").assertExists()
        compose.onNodeWithText("3").assertExists()
        compose.onAllNodesWithText("评估").assertCountEquals(0)
        compose.onNodeWithText("继续准备").performScrollTo().performClick()
        compose.onNodeWithText("评估").assertExists()
        compose.onNodeWithTag("route-assessment-decision").assertExists()
        compose.onAllNodesWithText("谨慎", substring = true).onFirst().assertExists()
        compose.onNodeWithText("检查装备").assertExists()
        compose.onNodeWithText("进入路线").assertExists()
        compose.onAllNodesWithText("装备").onFirst().assertExists()
    }

    @Test
    fun routeWorkspaceHidesSampleGpxActionByDefault() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen()
            }
        }

        compose.onNodeWithTag("home-tab-路线").performClick()
        compose.waitForIdle()

        compose.onNodeWithText("导入 GPX 文件").assertExists()
        compose.onAllNodesWithTag("sample-route-button").assertCountEquals(0)
        compose.onAllNodesWithText("使用示例 GPX").assertCountEquals(0)
    }

    @Test
    fun routeDetailOpensAtTopAfterUsingSampleRouteFromScrolledWorkspace() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen(showSampleRouteAction = true)
            }
        }

        compose.onNodeWithTag("home-tab-路线").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("sample-route-button").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithText("继续准备").performScrollTo().performClick()

        compose.onNodeWithText("龙井山脊").assertIsDisplayed()
        compose.onNodeWithText("15.2km · 累计爬升 860m").assertIsDisplayed()
    }

    @Test
    fun dataTabSummarizesHistoricalGpxManagement() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    initialHistoricalActivities = TrailMateSampleData.historicalActivities
                )
            }
        }

        compose.onAllNodesWithText("0/3 GPX").assertCountEquals(0)
        compose.onAllNodesWithText("历史 GPX").assertCountEquals(0)
        compose.onNodeWithText("数据").performClick()
        compose.onNodeWithText("历史资料").performScrollTo().assertExists()
        compose.onNodeWithText("已导入 3 条").assertExists()
        compose.onNodeWithText("导入历史 GPX").performScrollTo().assertExists()
        compose.onAllNodesWithText("供 AI 评估使用。").assertCountEquals(0)
        compose.onAllNodesWithText("使用示例历史").assertCountEquals(0)
        compose.onAllNodesWithText("移除历史").assertCountEquals(0)
        compose.onAllNodesWithText("历史 GPX 能力证据").assertCountEquals(0)
    }

    @Test
    fun profileTabDoesNotShowHistoricalActivityDetails() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    initialHistoricalActivities = TrailMateSampleData.historicalActivities
                )
            }
        }

        compose.onNodeWithText("我的").performClick()
        compose.waitForIdle()

        compose.onAllNodesWithText("历史资料").assertCountEquals(0)
        compose.onAllNodesWithText("导入历史 GPX").assertCountEquals(0)
        compose.onAllNodesWithText("晨间山脊环线").assertCountEquals(0)
        compose.onAllNodesWithText("9.8 km / +420 m / 2:45").assertCountEquals(0)
        compose.onAllNodesWithText("移除历史").assertCountEquals(0)
    }

    @Test
    fun routeTabDoesNotOfferAmapConsentWhenKeyIsConfigured() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost(
                    amapApiKeyConfigured = true,
                    amapPrivacyConsent = AmapPrivacyConsent()
                )
            }
        }

        compose.onNodeWithTag("segmented-control-路线").performClick()
        compose.onNodeWithText("检查点与补给").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onAllNodesWithText("启用在线底图").assertCountEquals(0)
        compose.onAllNodesWithText("同意并启用在线底图").assertCountEquals(0)
        compose.onNodeWithText("离线底图待准备").performScrollTo().assertIsDisplayed()
    }

    @Composable
    private fun RouteDetailTestHost(
        route: ImportedRoute = TrailMateSampleData.importedTargetRoute,
        aiGearAdvisorResponse: AiGearAdvisorResponse? = null,
        amapApiKeyConfigured: Boolean = false,
        amapPrivacyConsent: AmapPrivacyConsent = AmapPrivacyConsent(),
        notificationPermissionGranted: Boolean? = null,
        routeAssessment: RouteAssessmentSummary? = null,
        gearRecommendations: List<GearRecommendation>? = null,
        initialTrackRecording: TrackRecordingState = TrackRecordingState(),
        initialLocationSnapshot: TrailMateLocationSnapshot = TrailMateLocationSnapshot.disabled(),
        initialLocationGuidanceStatus: LocationBackedHikeStatus = LocationBackedHikeStatus.WAITING,
        initialLocationGuidanceCaption: String = "授权定位后，可用当前位置辅助检查点推进。",
        initialLocationFix: HikeLocationFix? = null,
        initialWasRecentlyOffRoute: Boolean = false,
        initialOfflineRoutePackReady: Boolean = false,
        initiallyExpandRouteDiagnostics: Boolean = false
    ) {
        var routeNavigationFullscreen by androidx.compose.runtime.remember { mutableStateOf(false) }
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            com.trailmate.app.feature.route.RouteDetailScreen(
                route = route,
                aiGearAdvisorResponse = aiGearAdvisorResponse,
                initialTrackRecording = initialTrackRecording,
                amapApiKeyConfigured = amapApiKeyConfigured,
                amapPrivacyConsent = amapPrivacyConsent,
                routeAssessment = routeAssessment,
                gearRecommendations = gearRecommendations,
                notificationPermissionGranted = notificationPermissionGranted,
                initialLocationSnapshot = initialLocationSnapshot,
                initialLocationGuidanceStatus = initialLocationGuidanceStatus,
                initialLocationGuidanceCaption = initialLocationGuidanceCaption,
                initialLocationFix = initialLocationFix,
                initialWasRecentlyOffRoute = initialWasRecentlyOffRoute,
                initialOfflineRoutePackReady = initialOfflineRoutePackReady,
                initiallyExpandRouteDiagnostics = initiallyExpandRouteDiagnostics,
                routeNavigationFullscreen = routeNavigationFullscreen,
                onRouteNavigationFullscreenChanged = { fullscreen ->
                    routeNavigationFullscreen = fullscreen
                }
            )
        }
    }

    private fun savedProfile(): BaselineProfile =
        BaselineProfile(
            exerciseFrequency = ExerciseFrequency.THREE_PLUS_PER_WEEK,
            typicalDuration = TypicalDuration.OVER_60,
            experienceLevel = ExperienceLevel.EXPERIENCED,
            ascentExperience = AscentExperience.OVER_800,
            heightCm = 181,
            weightKg = 76,
            commonPackWeightKg = 7
        )

    private fun savedAuthSession(): TrailMateAuthSession =
        TrailMateAuthSession.localWechatSession(nowEpochMillis = 42L)

    private fun readyRoute(): ImportedRoute =
        ImportedRoute(
            routeName = "九溪轻徒步",
            fileName = "jiuxi-ready.gpx",
            distanceKm = 5.1,
            ascentMeters = 220,
            status = com.trailmate.app.core.model.RouteImportStatus.PARSED,
            pointCount = 4,
            durationMinutes = 150,
            routePoints = listOf(
                RoutePoint(latitude = 30.2170, longitude = 120.1110, elevationMeters = 48.0, distanceAlongRouteKm = 0.0),
                RoutePoint(latitude = 30.2250, longitude = 120.1180, elevationMeters = 92.0, distanceAlongRouteKm = 1.8),
                RoutePoint(latitude = 30.2370, longitude = 120.1260, elevationMeters = 168.0, distanceAlongRouteKm = 3.6),
                RoutePoint(latitude = 30.2450, longitude = 120.1320, elevationMeters = 220.0, distanceAlongRouteKm = 5.1)
            )
        )

    private fun nonBlockingAssessment(route: ImportedRoute): RouteAssessmentSummary =
        RouteAssessmentSummary(
            routeName = route.routeName,
            distanceKm = route.distanceKm,
            ascentMeters = route.ascentMeters,
            matchLevel = MatchLevel.RECOMMENDED,
            confidenceLevel = ConfidenceLevel.HIGH,
            estimatedDurationRange = "2:20-2:50",
            risks = emptyList()
        )

    private fun coveredDepartureGear(): List<GearRecommendation> =
        listOf(
            GearRecommendation(
                category = "雨衣",
                status = GearStatus.COVERED,
                rationale = "测试路线出发装备已覆盖。"
            ),
            GearRecommendation(
                category = "头灯",
                status = GearStatus.CHECK,
                rationale = "出发前确认电量。"
            ),
            GearRecommendation(
                category = "登山杖",
                status = GearStatus.COVERED,
                rationale = "已匹配品牌装备。"
            ),
            GearRecommendation(
                category = "保暖层",
                status = GearStatus.COVERED,
                rationale = "已匹配品牌装备。"
            )
        )

    private fun recordedTrack(): TrackRecordingState =
        TrackRecordingEngine.finish(
            state = TrackRecordingEngine.appendLocation(
                state = TrackRecordingEngine.appendLocation(
                    state = TrackRecordingEngine.start(routeName = "龙井山脊", nowEpochMillis = 1_000L),
                    point = RecordedTrackPoint(
                        latitude = 30.0,
                        longitude = 120.0,
                        elevationMeters = 100.0,
                        horizontalAccuracyMeters = 8.0,
                        timestampEpochMillis = 1_000L
                    ),
                    nowEpochMillis = 1_000L
                ),
                point = RecordedTrackPoint(
                    latitude = 30.01,
                    longitude = 120.0,
                    elevationMeters = 120.0,
                    horizontalAccuracyMeters = 8.0,
                    timestampEpochMillis = 421_000L
                ),
                nowEpochMillis = 421_000L
            ),
            nowEpochMillis = 421_000L
        )

    private fun locatedTrailSnapshot(): TrailMateLocationSnapshot =
        TrailMateLocationSnapshot(
            status = TrailMateLocationStatus.LOCATED,
            latitude = 30.25,
            longitude = 120.15,
            elevationMeters = 142.0,
            horizontalAccuracyMeters = 7.0,
            timestampEpochMillis = System.currentTimeMillis()
        )

    private fun grantRouteRuntimePermissions() {
        grantRuntimePermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        grantRuntimePermission(Manifest.permission.ACCESS_FINE_LOCATION)
        grantRuntimePermission(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun grantRuntimePermission(permission: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val packageName = instrumentation.targetContext.packageName
        instrumentation.uiAutomation.executeShellCommand("pm grant $packageName $permission").use { }
    }

    private class FakeGearCatalogApi(
        private val searchResultProvider: () -> TrailMateApiResult<List<TrailMateGearCatalogItemDto>> = {
            TrailMateApiResult.Success(defaultCatalogItems())
        }
    ) : TrailMateGearCatalogApi {
        var searchCalls: Int = 0
            private set

        override fun listGearCatalogCategories(): TrailMateApiResult<List<String>> =
            TrailMateApiResult.Success(listOf("登山杖"))

        override fun searchGearCatalog(
            category: String,
            query: String
        ): TrailMateApiResult<List<TrailMateGearCatalogItemDto>> {
            searchCalls += 1
            return searchResultProvider()
        }

        companion object {
            private fun defaultCatalogItems(): List<TrailMateGearCatalogItemDto> =
                listOf(
                    TrailMateGearCatalogItemDto(
                        catalogItemId = "cat_poles_leki_legacy_lite",
                        category = "登山杖",
                        brand = "Leki",
                        model = "Legacy Lite AS",
                        displayName = "Leki Legacy Lite AS",
                        weightGrams = 510,
                        tags = listOf("长距离", "下坡", "稳定"),
                        imageUrl = "https://cdn.trailmate.local/gear/leki-legacy-lite-as.png",
                        imageAttribution = "TrailMate hosted catalog thumbnail",
                        source = "server"
                    )
                )
        }
    }

    private class FakeTrailMateSessionRepository(
        initialSnapshot: TrailMateSnapshot = TrailMateSnapshot()
    ) : TrailMateSessionRepository {
        var snapshot: TrailMateSnapshot = initialSnapshot
            private set

        override fun loadSnapshot(): TrailMateSnapshot = snapshot

        override fun saveAuthSession(session: TrailMateAuthSession) {
            snapshot = snapshot.copy(authSession = session)
        }

        override fun clearAuthSession() {
            snapshot = snapshot.copy(authSession = null)
        }

        override fun saveProfile(profile: BaselineProfile) {
            snapshot = snapshot.copy(profile = profile)
        }

        override fun saveImportedRoute(route: ImportedRoute) {
            snapshot = snapshot.copy(importedRoute = route)
        }

        override fun saveHistoricalActivities(historicalActivities: List<HistoricalActivity>) {
            snapshot = snapshot.copy(historicalActivities = historicalActivities)
        }

        override fun saveGpxImportQueue(queue: GpxImportQueue) {
            snapshot = snapshot.copy(gpxImportQueue = queue)
        }

        override fun saveTrackRecording(trackRecording: TrackRecordingState) {
            snapshot = snapshot.copy(latestTrackRecording = trackRecording)
        }

        override fun saveAmapPrivacyConsent(consent: AmapPrivacyConsent) {
            snapshot = snapshot.copy(amapPrivacyConsent = consent)
        }

        override fun saveOfflineRoutePackKeys(keys: Set<String>) {
            snapshot = snapshot.copy(savedOfflineRoutePackKeys = keys)
        }

        override fun saveOfflineBaseMapTileProofs(proofs: List<AmapOfflineBaseMapTileProof>) {
            snapshot = snapshot.copy(offlineBaseMapTileProofs = proofs)
        }

        override fun clearLocalData() {
            snapshot = TrailMateSnapshot.empty()
        }
    }
}
