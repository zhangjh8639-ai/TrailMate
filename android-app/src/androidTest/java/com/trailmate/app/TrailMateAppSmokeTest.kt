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
import com.trailmate.app.core.location.TrackRecordingBroadcastCodec
import com.trailmate.app.core.location.TrackRecordingForegroundService
import com.trailmate.app.core.location.TrailMateLocationSnapshot
import com.trailmate.app.core.location.TrailMateLocationStatus
import com.trailmate.app.core.model.AiGearAdvisorResponse
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.gpx.GpxImportJobKind
import com.trailmate.app.core.gpx.GpxImportJobStatus
import com.trailmate.app.core.gpx.GpxImportQueue
import com.trailmate.app.core.map.AmapOfflineBaseMapTileProof
import com.trailmate.app.core.map.AmapPrivacyConsent
import com.trailmate.app.core.model.ExerciseFrequency
import com.trailmate.app.core.model.ExperienceLevel
import com.trailmate.app.core.model.GearInventory
import com.trailmate.app.core.model.GearItem
import com.trailmate.app.core.model.GearRecommendation
import com.trailmate.app.core.model.GearStatus
import com.trailmate.app.core.model.HistoricalActivity
import com.trailmate.app.core.model.HikeLocationFix
import com.trailmate.app.core.model.ImportedRoute
import com.trailmate.app.core.model.LocationBackedHikeStatus
import com.trailmate.app.core.model.RecordedTrackPoint
import com.trailmate.app.core.model.AscentExperience
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.core.model.TrackRecordingEngine
import com.trailmate.app.core.model.TrackRecordingState
import com.trailmate.app.core.model.TypicalDuration
import com.trailmate.app.core.model.offlineRoutePackKey
import com.trailmate.app.core.persistence.TrailMateSessionRepository
import com.trailmate.app.core.persistence.TrailMateSnapshot
import com.trailmate.app.feature.gear.MyGearScreen
import com.trailmate.app.feature.home.HomeScreen
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
                TrailMateApp(sessionRepository = FakeTrailMateSessionRepository())
            }
        }

        compose.onNodeWithText("TrailMate").assertExists()
        compose.onNodeWithText("开始基础档案").assertExists()
    }

    @Test
    fun onboardingCollectsBaselineProfileBeforeHome() {
        compose.setContent {
            TrailMateTheme {
                TrailMateApp(sessionRepository = FakeTrailMateSessionRepository())
            }
        }

        compose.onNodeWithText("开始基础档案").performClick()
        compose.onNodeWithText("身高 cm").performScrollTo().performTextInput("181")
        compose.onNodeWithText("体重 kg").performScrollTo().performTextInput("76")
        compose.onNodeWithText("常用背包 kg").performScrollTo().performTextInput("7")
        compose.onNodeWithText("保存档案").performScrollTo().performClick()
        compose.onNodeWithText("地图与定位准备").assertExists()
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
        compose.setContent {
            TrailMateTheme {
                HomeScreen(initialImportedRoute = TrailMateSampleData.importedTargetRoute)
            }
        }

        compose.onNodeWithTag("home-tab-路线").performClick()
        compose.onNodeWithText("路线准备").assertExists()
        compose.onNodeWithText("当前路线").assertExists()
        compose.onNodeWithText("继续准备").assertExists()
        compose.onNodeWithText("更换 GPX").assertExists()
        compose.onAllNodesWithText("导入状态").assertCountEquals(0)
        compose.onAllNodesWithText("导入队列").assertCountEquals(0)
        compose.onAllNodesWithText("路线包").assertCountEquals(0)
        compose.onAllNodesWithText("风险因素").assertCountEquals(0)
        compose.onAllNodesWithText("现场状态").assertCountEquals(0)
        compose.onAllNodesWithTag("route-cockpit").assertCountEquals(0)
        compose.onAllNodesWithText("保存路线包").assertCountEquals(0)
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
        compose.setContent {
            TrailMateTheme {
                HomeScreen(initialImportedRoute = TrailMateSampleData.importedTargetRoute)
            }
        }

        compose.onNodeWithText("首页").assertExists()
        compose.onNodeWithText("路线").assertExists()
        compose.onAllNodesWithText("装备").onFirst().assertExists()
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
        compose.onAllNodesWithText("开始徒步").assertCountEquals(0)
        compose.onAllNodesWithText("当前检查点").onFirst().assertExists()
        compose.onAllNodesWithText("仅提供路线辅助，不替代路标与离线地图").onFirst().assertExists()
        compose.onNodeWithText("全屏导航").assertExists()
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
        compose.onAllNodesWithText("装备").onFirst().assertExists()
        compose.onNodeWithText("路线清单").assertExists()
        compose.onNodeWithText("我的装备").assertExists()
        compose.onNodeWithText("详情").assertExists()
        compose.onNodeWithText("AI 装备建议").assertExists()
        compose.onNodeWithText("必备装备").assertExists()
        compose.onAllNodesWithText("查看匹配").onFirst().assertExists()
        compose.onAllNodesWithText("添加已有装备").onFirst().assertExists()
        compose.onNodeWithText("保存到我的装备").assertExists()
    }

    @Test
    fun onboardingSavePersistsProfile() {
        val store = FakeTrailMateSessionRepository()

        compose.setContent {
            TrailMateTheme {
                TrailMateApp(sessionRepository = store)
            }
        }

        compose.onNodeWithText("开始基础档案").performClick()
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
        val store = FakeTrailMateSessionRepository()

        compose.setContent {
            TrailMateTheme {
                TrailMateApp(sessionRepository = store)
            }
        }

        compose.onNodeWithText("开始基础档案").performClick()
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
        val store = FakeTrailMateSessionRepository()

        compose.setContent {
            TrailMateTheme {
                TrailMateApp(
                    sessionRepository = store,
                    requestOnboardingLocationPermission = true
                )
            }
        }

        compose.onNodeWithText("开始基础档案").performClick()
        compose.onNodeWithText("暂时跳过").performScrollTo().performClick()
        compose.onNodeWithText("稍后，仅使用本地路线").performScrollTo().performClick()

        compose.onNodeWithText("准备走哪条线？").assertExists()
        assertFalse(store.snapshot.amapPrivacyConsent.accepted)
    }

    @Test
    fun onboardingMapConsentCompletesWhenLocationPermissionIsAlreadyGranted() {
        grantRuntimePermission(Manifest.permission.ACCESS_FINE_LOCATION)
        grantRuntimePermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        val store = FakeTrailMateSessionRepository()

        compose.setContent {
            TrailMateTheme {
                TrailMateApp(
                    sessionRepository = store,
                    requestOnboardingLocationPermission = true
                )
            }
        }

        compose.onNodeWithText("开始基础档案").performClick()
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

        compose.onNodeWithText("计划").performClick()
        compose.onNodeWithText("补给与休息计划").assertExists()

        compose.onAllNodesWithText("装备").onFirst().performClick()
        compose.onNodeWithText("AI 装备建议").assertExists()
    }

    @Test
    fun routeReadinessCanSaveOfflineRoutePack() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost()
            }
        }

        compose.onNodeWithText("路线").performClick()
        compose.onNodeWithContentDescription("路线包")
            .performClick()

        compose.onAllNodesWithText("已保存").onFirst().assertExists()
        compose.onAllNodesWithText("离线").onFirst().assertExists()
    }

    @Test
    fun routeCockpitCompactActionsKeepOfflineAndGearAccessible() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost()
            }
        }

        compose.onNodeWithText("路线").performClick()
        compose.onNodeWithContentDescription("路线包")
            .assertHasClickAction()
            .performClick()
        compose.onAllNodesWithText("已保存").onFirst().assertExists()

        compose.onAllNodesWithText("装备").onFirst().performClick()
        compose.onNodeWithText("AI 装备建议").assertExists()
    }

    @Test
    fun appPersistsOfflineRoutePackReadiness() {
        val store = FakeTrailMateSessionRepository(
            TrailMateSnapshot(
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
        compose.onNodeWithText("进入路线").performClick()
        compose.onNodeWithContentDescription("路线包")
            .performClick()
        compose.waitForIdle()

        assertTrue(
            store.snapshot.savedOfflineRoutePackKeys.contains(
                TrailMateSampleData.importedTargetRoute.offlineRoutePackKey()
            )
        )
    }

    @Test
    fun myGearShowsOwnedBrandGearAndAddAction() {
        val inventory = GearInventory(TrailMateSampleData.gearItems)

        compose.setContent {
            TrailMateTheme {
                MyGearScreen(
                    inventory = inventory,
                    routeGearRecommendations = inventory.applyTo(TrailMateSampleData.gearRecommendations),
                    requestedCategory = "登山杖",
                    onAddBrandGear = { _, _, _, _ -> },
                    onSetAvailability = { _, _ -> },
                    onDeleteGear = {}
                )
            }
        }

        compose.onAllNodesWithText("装备").onFirst().assertExists()
        compose.onAllNodesWithText("添加已有装备").onFirst().assertExists()
        compose.onNodeWithText("Salomon X Ultra 4 GTX").assertExists()
        compose.onNodeWithText("保存到我的装备").assertExists()
    }

    @Test
    fun myGearDetailsTabShowsSelectedGearRouteContext() {
        val inventory = GearInventory(
            items = listOf(
                GearItem(
                    id = "shell-1",
                    category = "雨衣",
                    brand = "Patagonia",
                    model = "Torrentshell",
                    weightGrams = 400,
                    available = true
                )
            )
        )
        val recommendations = inventory.applyTo(
            listOf(
                GearRecommendation(
                    category = "雨衣",
                    status = GearStatus.MISSING,
                    rationale = "现有雨衣可以覆盖山脊风和小雨。"
                )
            )
        )

        compose.setContent {
            TrailMateTheme {
                MyGearScreen(
                    inventory = inventory,
                    routeGearRecommendations = recommendations,
                    requestedCategory = "",
                    onAddBrandGear = { _, _, _, _ -> },
                    onSetAvailability = { _, _ -> },
                    onDeleteGear = {}
                )
            }
        }

        compose.onNodeWithText("详情").performClick()

        compose.onNodeWithText("装备详情").assertExists()
        compose.onNodeWithText("Patagonia Torrentshell").assertExists()
        compose.onNodeWithText("400g / 可用", substring = true).assertExists()
        compose.onNodeWithText("匹配雨衣建议。").assertExists()
        compose.onNodeWithText("现有雨衣可以覆盖山脊风和小雨。").assertExists()
    }

    @Test
    fun routeGearTabShowsMatchedOwnedGear() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost()
            }
        }

        compose.onNodeWithText("装备").performClick()
        compose.onNodeWithText("AI 装备建议").assertExists()
        compose.onNodeWithText("本地清单启用").assertExists()
        compose.onNodeWithText("路线清单").assertExists()
        compose.onNodeWithText("必备装备", substring = true).assertExists()
        compose.onNodeWithText("雨衣").assertExists()
        compose.onNodeWithText("已匹配 Patagonia Torrentshell", substring = true).assertExists()
        compose.onNodeWithText("备用水").assertExists()
        compose.onAllNodesWithText("添加已有装备").onFirst().assertExists()
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

        compose.onNodeWithText("计划").performClick()

        compose.onNodeWithText("行程节奏").assertExists()
        compose.onNodeWithText("补给与休息计划", substring = true).assertExists()
        compose.onNodeWithText("天气与返程判断").assertExists()
        compose.onNodeWithText("检查点时间线").assertExists()
        compose.onNodeWithText("补给检查", substring = true).assertExists()
        compose.onNodeWithText("风险复核").assertExists()
        compose.onAllNodesWithText("Plan checkpoints").assertCountEquals(0)
    }

    @Test
    fun fullscreenNavigationStartsAndAdvancesActiveHike() {
        grantRouteRuntimePermissions()

        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost(
                    notificationPermissionGranted = true,
                    initialLocationSnapshot = locatedTrailSnapshot()
                )
            }
        }

        compose.onNodeWithText("路线").performClick()
        compose.onNodeWithTag("route-cockpit").assertExists()
        compose.onAllNodesWithText("开始徒步").assertCountEquals(0)
        compose.onNodeWithText("全屏导航").performScrollTo().performClick()
        compose.onNodeWithTag("route-navigation-fullscreen").assertExists()
        compose.onNodeWithText("开始徒步").assertExists()
        compose.onNodeWithTag("route-navigation-fullscreen-primary-action").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("标记点").assertExists()
        compose.onAllNodesWithText("补给检查", substring = true).onFirst().assertExists()
        compose.onAllNodesWithText("补水、补能量", substring = true).onFirst().assertExists()

        compose.onNodeWithText("标记点").performClick()
        compose.onAllNodesWithText("休息判断", substring = true).onFirst().assertExists()
    }

    @Test
    fun routeTabShowsGpsAndTrackRecordingControls() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost(notificationPermissionGranted = false)
            }
        }

        compose.onNodeWithText("路线").performClick()

        compose.onNodeWithTag("route-cockpit").assertExists()
        compose.onNodeWithTag("route-cockpit-primary-action").assertExists()
        compose.onAllNodesWithTag("route-cockpit-readiness-strip").assertCountEquals(0)
        compose.onAllNodesWithText("开始徒步").assertCountEquals(0)
        compose.onAllNodesWithText("标记点").assertCountEquals(0)
        compose.onNodeWithText("全屏导航").assertExists()
        compose.onNodeWithText("安全分享").assertExists()
        compose.onNodeWithContentDescription("路线包").assertHasClickAction()
        compose.onAllNodesWithText("地图状态").assertCountEquals(0)
        compose.onAllNodesWithText("地图状态与轻导航").assertCountEquals(0)
        compose.onAllNodesWithText("地图与离线").assertCountEquals(0)
        compose.onAllNodesWithText("本地路线、定位、离线包和图层说明").assertCountEquals(0)
        compose.onNodeWithText("检查点与补给").assertExists()
        compose.onNodeWithText("5 个检查点 · 补给/休息/风险").assertExists()
        compose.onAllNodesWithText("现场状态").assertCountEquals(0)
        compose.onAllNodesWithText("准备轻导航").assertCountEquals(0)
        compose.onNodeWithText("定位：", substring = true).assertExists()
        compose.onNodeWithText("记录：", substring = true).assertExists()
        compose.onNodeWithText("路线包：", substring = true).assertExists()
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
        compose.onNodeWithText("已记录 0.0 km / 0 个点").assertExists()
        compose.onNodeWithText("轨迹通知").assertExists()
        compose.onNodeWithText("允许通知").assertExists()
        compose.onNodeWithText("锁屏或切后台时可看到记录状态", substring = true).assertExists()
        compose.onNodeWithText("前台服务记录", substring = true).assertExists()
        compose.onNodeWithText("前台服务记录真实定位轨迹", substring = true).assertExists()
        compose.onAllNodesWithText("安全分享").onFirst().assertExists()
        compose.onNodeWithText("等待定位后分享").assertExists()
        compose.onNodeWithText("授权定位后可分享当前位置", substring = true).assertExists()
        compose.onNodeWithText("地图与离线").assertExists()
        compose.onNodeWithText("当前使用本地路线").assertExists()
        compose.onAllNodesWithText("在线底图暂不可用", substring = true).onFirst().assertExists()
        compose.onNodeWithText("出发检查").assertExists()
        compose.onNodeWithText("出发前还差 4 项").assertExists()
        compose.onNodeWithText("建议补齐").assertExists()
        compose.onAllNodesWithText("离线底图").onFirst().assertExists()
        compose.onAllNodesWithText("缺 3 项").onFirst().assertExists()
        compose.onAllNodesWithText("路线提示点").onFirst().assertExists()
        compose.onNodeWithText("地图图层").performScrollTo().assertExists()
        compose.onNodeWithText("计划路线").assertExists()
        compose.onNodeWithText("未记录").assertExists()
        compose.onNodeWithText("补给 ·", substring = true).assertExists()
        compose.onNodeWithText("休息 ·", substring = true).assertExists()
        compose.onNodeWithText("风险 ·", substring = true).assertExists()
    }

    @Test
    fun homeRouteFullscreenFocusesNavigationAndHidesBottomBar() {
        compose.setContent {
            TrailMateTheme {
                HomeScreen(initialImportedRoute = TrailMateSampleData.importedTargetRoute)
            }
        }

        compose.onNodeWithTag("home-tab-路线").performClick()
        compose.onNodeWithText("继续准备").performScrollTo().performClick()
        compose.onNodeWithText("进入路线").performClick()
        compose.onNodeWithText("全屏导航").performScrollTo().performClick()

        compose.onNodeWithTag("route-navigation-fullscreen").assertExists()
        compose.onAllNodesWithTag("home-tab-首页").assertCountEquals(0)
        compose.onNodeWithTag("route-navigation-fullscreen-primary-action").assertExists()
        val fullscreenBounds = compose.onNodeWithTag("route-navigation-fullscreen").getUnclippedBoundsInRoot()
        val dockBounds = compose.onNodeWithTag("route-navigation-fullscreen-dock")
            .assertIsDisplayed()
            .getUnclippedBoundsInRoot()
        assertTrue(dockBounds.top < fullscreenBounds.bottom)
        assertTrue(dockBounds.bottom <= fullscreenBounds.bottom)
        compose.onNodeWithText("安全分享").assertExists()
        compose.onNodeWithText("标记点").assertExists()
        compose.onNodeWithText("结束记录").assertExists()

        compose.onNodeWithContentDescription("退出全屏导航").performClick()
        compose.onNodeWithTag("route-cockpit").assertExists()
        compose.onNodeWithTag("home-tab-首页").assertExists()
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
                    )
                )
            }
        }

        compose.onNodeWithText("路线").performClick()

        compose.onNodeWithText("查看恢复建议").assertExists()
        compose.onNodeWithTag("route-cockpit-primary-action").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithText("收起").assertExists()
        compose.onNodeWithText("偏离恢复").performScrollTo().assertExists()
        compose.onNodeWithText("停止自动推进").assertExists()
        compose.onNodeWithText("疑似偏离路线约 112 m", substring = true).assertExists()
        compose.onNodeWithText("返回最近路径").assertExists()
        compose.onAllNodesWithText("分享当前位置").assertCountEquals(2)
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

        compose.onNodeWithText("路线").performClick()

        compose.onNodeWithText("检查点与补给").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithText("已回到路线").performScrollTo().assertExists()
        compose.onNodeWithText("可继续推进").assertExists()
        compose.onNodeWithText("确认下一检查点").assertExists()
        compose.onNodeWithText("继续导航").assertExists()
    }

    @Test
    fun routeDiagnosticsShowRecordedTrackWhenRecordingHasPoints() {
        val recording = TrackRecordingEngine.appendLocation(
            state = TrackRecordingEngine.appendLocation(
                state = TrackRecordingEngine.start(
                    routeName = "龙井山脊",
                    nowEpochMillis = 1_000L
                ),
                point = RecordedTrackPoint(
                    latitude = 30.00,
                    longitude = 120.00,
                    elevationMeters = 100.0,
                    horizontalAccuracyMeters = 8.0,
                    timestampEpochMillis = 1_100L
                )
            ),
            point = RecordedTrackPoint(
                latitude = 30.01,
                longitude = 120.01,
                elevationMeters = 110.0,
                horizontalAccuracyMeters = 8.0,
                timestampEpochMillis = 421_100L
            )
        )

        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost(initialTrackRecording = recording)
            }
        }

        compose.onNodeWithText("路线").performClick()
        compose.onNodeWithText("检查点与补给").performScrollTo().performClick()

        compose.onNodeWithText("轨迹记录").performScrollTo().assertExists()
        compose.onAllNodesWithText("2 个点", substring = true).onFirst().assertExists()
    }

    @Test
    fun routeMapLocationToolIsActionable() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost()
            }
        }

        compose.onNodeWithText("路线").performClick()

        compose.onNodeWithContentDescription("定位").assertHasClickAction()
    }

    @Test
    fun routeMapDoesNotExposeCompassToolBeforeHeadingSupport() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost()
            }
        }

        compose.onNodeWithText("路线").performClick()

        compose.onAllNodesWithContentDescription("校准方向").assertCountEquals(0)
    }

    @Test
    fun routeTabShowsFinishedTrackReviewCard() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost(initialTrackRecording = recordedTrack())
            }
        }

        compose.onNodeWithText("路线").performClick()
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
    fun routeTabOpensCheckpointDetailFromMapHint() {
        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost()
            }
        }

        compose.onNodeWithText("路线").performClick()
        compose.onNodeWithText("补给 ·", substring = true).performClick()

        compose.onNodeWithText("提示点详情").assertExists()
        compose.onNodeWithText("预计到达").assertExists()
        compose.onAllNodesWithText("补给未齐").onFirst().assertExists()
        compose.onAllNodesWithText("备用水", substring = true).onFirst().assertExists()
        compose.onNodeWithText("建议动作").assertExists()
        compose.onAllNodesWithText("补水、补能量", substring = true).onFirst().assertExists()
        compose.onNodeWithText("设为当前关注").assertExists()
    }

    @Test
    fun activeHikeResetsWhenRouteChanges() {
        grantRouteRuntimePermissions()
        var route by mutableStateOf(TrailMateSampleData.importedTargetRoute)

        compose.setContent {
            TrailMateTheme {
                RouteDetailTestHost(
                    route = route,
                    notificationPermissionGranted = true,
                    initialLocationSnapshot = locatedTrailSnapshot()
                )
            }
        }

        compose.onNodeWithText("路线").performClick()
        compose.onAllNodesWithText("开始徒步").assertCountEquals(0)
        compose.onNodeWithText("全屏导航").performScrollTo().performClick()
        compose.onNodeWithTag("route-navigation-fullscreen").assertExists()
        compose.onNodeWithText("开始徒步").assertExists()
        compose.onNodeWithTag("route-navigation-fullscreen-primary-action").performClick()
        compose.waitForIdle()
        compose.onNodeWithContentDescription("退出全屏导航").performClick()
        compose.onNodeWithTag("route-cockpit").assertExists()
        compose.onAllNodesWithText("标记点").assertCountEquals(0)

        compose.runOnIdle {
            route = route.copy(
                routeName = "替换路线",
                fileName = "replacement.gpx",
                distanceKm = 4.8,
                ascentMeters = 180,
                pointCount = 64
            )
        }

        compose.onNodeWithText("替换路线").assertExists()
        compose.onAllNodesWithText("开始徒步").assertCountEquals(0)
        compose.onNodeWithText("全屏导航").assertExists()
        compose.onAllNodesWithText("暂停").assertCountEquals(0)
    }

    @Test
    fun homeGearAddFlowPrefillsCategoryAndUpdatesRouteMatch() {
        var savedInventory: GearInventory? = null

        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    initialImportedRoute = TrailMateSampleData.importedTargetRoute,
                    onInventoryChanged = { inventory -> savedInventory = inventory }
                )
            }
        }

        compose.onAllNodesWithText("装备").onFirst().performClick()
        compose.onAllNodesWithText("装备").onFirst().assertExists()
        compose.onAllNodesWithText("登山杖").assertCountEquals(2)

        compose.onNodeWithText("品牌").performScrollTo().performTextInput("Leki")
        compose.onNodeWithText("型号").performScrollTo().performTextInput("Makalu Lite")
        compose.onNodeWithText("保存到我的装备").performScrollTo().performClick()
        compose.waitForIdle()

        assertTrue(savedInventory?.items.orEmpty().any { item ->
            item.category == "登山杖" && item.brand == "Leki" && item.model == "Makalu Lite"
        })
    }

    @Test
    fun homeNotifiesPersistenceWhenRouteAndGearChange() {
        var savedRoute: ImportedRoute? = null
        var savedInventory: GearInventory? = null
        val savedQueues = mutableListOf<GpxImportQueue>()

        compose.setContent {
            TrailMateTheme {
                HomeScreen(
                    profile = TrailMateSampleData.baselineProfile,
                    showSampleRouteAction = true,
                    onRouteImported = { route -> savedRoute = route },
                    onInventoryChanged = { inventory -> savedInventory = inventory },
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
        compose.onNodeWithText("品牌").performScrollTo().performTextInput("Leki")
        compose.onNodeWithText("型号").performScrollTo().performTextInput("Makalu Lite")
        compose.onNodeWithText("保存到我的装备").performScrollTo().performClick()

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
        assertTrue(savedInventory?.items.orEmpty().any { item ->
            item.category == "登山杖" && item.brand == "Leki"
        })
    }

    @Test
    fun profileTabShowsDataPrivacyClearControls() {
        val initialSnapshot = TrailMateSnapshot(
            profile = savedProfile(),
            inventory = GearInventory(TrailMateSampleData.gearItems),
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
        compose.onNodeWithText("开始基础档案").assertExists()

        compose.onNodeWithText("开始基础档案").performClick()
        compose.onNodeWithText("暂时跳过").performScrollTo().performClick()
        compose.onNodeWithText("稍后，仅使用本地路线").performScrollTo().performClick()
        compose.onNodeWithTag("home-tab-数据").performClick()

        compose.onNodeWithText("完成一次记录后会出现复盘").assertExists()
        compose.onAllNodesWithText("清除本地数据").assertCountEquals(0)
        compose.onAllNodesWithText("龙井山脊 / 15.2 km / +860 m").assertCountEquals(0)
    }

    @Test
    fun dataTabShowsRecordedTrackReviewAndTrendSummary() {
        val store = FakeTrailMateSessionRepository(
            TrailMateSnapshot(
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
            TrailMateSnapshot(profile = savedProfile())
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

        compose.onNodeWithText("路线").performClick()
        compose.onNodeWithText("检查点与补给").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onAllNodesWithText("启用在线底图").assertCountEquals(0)
        compose.onAllNodesWithText("同意并启用在线底图").assertCountEquals(0)
        compose.onNodeWithText("当前使用本地路线").performScrollTo().assertIsDisplayed()
    }

    @Composable
    private fun RouteDetailTestHost(
        route: ImportedRoute = TrailMateSampleData.importedTargetRoute,
        aiGearAdvisorResponse: AiGearAdvisorResponse? = null,
        amapApiKeyConfigured: Boolean = false,
        amapPrivacyConsent: AmapPrivacyConsent = AmapPrivacyConsent(),
        notificationPermissionGranted: Boolean? = null,
        initialTrackRecording: TrackRecordingState = TrackRecordingState(),
        initialLocationSnapshot: TrailMateLocationSnapshot = TrailMateLocationSnapshot.disabled(),
        initialLocationGuidanceStatus: LocationBackedHikeStatus = LocationBackedHikeStatus.WAITING,
    initialLocationGuidanceCaption: String = "授权定位后，可用当前位置辅助检查点推进。",
        initialLocationFix: HikeLocationFix? = null,
        initialWasRecentlyOffRoute: Boolean = false
    ) {
        var routeNavigationFullscreen by androidx.compose.runtime.remember { mutableStateOf(false) }
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            com.trailmate.app.feature.route.RouteDetailScreen(
                route = route,
                aiGearAdvisorResponse = aiGearAdvisorResponse,
                initialTrackRecording = initialTrackRecording,
                amapApiKeyConfigured = amapApiKeyConfigured,
                amapPrivacyConsent = amapPrivacyConsent,
                notificationPermissionGranted = notificationPermissionGranted,
                initialLocationSnapshot = initialLocationSnapshot,
                initialLocationGuidanceStatus = initialLocationGuidanceStatus,
                initialLocationGuidanceCaption = initialLocationGuidanceCaption,
                initialLocationFix = initialLocationFix,
                initialWasRecentlyOffRoute = initialWasRecentlyOffRoute,
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
                    )
                ),
                point = RecordedTrackPoint(
                    latitude = 30.01,
                    longitude = 120.0,
                    elevationMeters = 120.0,
                    horizontalAccuracyMeters = 8.0,
                    timestampEpochMillis = 421_000L
                )
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
            timestampEpochMillis = 1_000L
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

    private class FakeTrailMateSessionRepository(
        initialSnapshot: TrailMateSnapshot = TrailMateSnapshot()
    ) : TrailMateSessionRepository {
        var snapshot: TrailMateSnapshot = initialSnapshot
            private set

        override fun loadSnapshot(): TrailMateSnapshot = snapshot

        override fun saveProfile(profile: BaselineProfile) {
            snapshot = snapshot.copy(profile = profile)
        }

        override fun saveInventory(inventory: GearInventory) {
            snapshot = snapshot.copy(inventory = inventory)
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
