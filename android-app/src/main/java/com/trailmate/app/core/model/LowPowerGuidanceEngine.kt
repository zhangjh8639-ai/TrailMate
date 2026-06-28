package com.trailmate.app.core.model

data class LowPowerGuidanceAction(
    val title: String,
    val caption: String
)

enum class LowPowerGuidanceTone {
    CAUTION,
    ALERT
}

data class LowPowerGuidancePresentation(
    val visible: Boolean,
    val title: String,
    val statusLabel: String,
    val caption: String,
    val primaryActionLabel: String,
    val primaryActionRequestsFinalFix: Boolean,
    val tone: LowPowerGuidanceTone?,
    val actions: List<LowPowerGuidanceAction>
)

object LowPowerGuidanceEngine {
    fun present(
        batteryStatus: RouteBatteryStatus,
        trackRecording: TrackRecordingState,
        offlineRouteReady: Boolean,
        offlineBaseMapReady: Boolean
    ): LowPowerGuidancePresentation =
        when (batteryStatus.level) {
            RouteBatteryLevel.LOW -> lowBatteryPresentation(
                trackRecording = trackRecording,
                offlineRouteReady = offlineRouteReady,
                offlineBaseMapReady = offlineBaseMapReady
            )
            RouteBatteryLevel.CRITICAL -> criticalBatteryPresentation(trackRecording)
            RouteBatteryLevel.NORMAL,
            RouteBatteryLevel.UNKNOWN -> hiddenPresentation()
        }

    private fun hiddenPresentation(): LowPowerGuidancePresentation =
        LowPowerGuidancePresentation(
            visible = false,
            title = "低电量引导",
            statusLabel = "正常",
            caption = "",
            primaryActionLabel = "",
            primaryActionRequestsFinalFix = false,
            tone = null,
            actions = emptyList()
        )

    private fun lowBatteryPresentation(
        trackRecording: TrackRecordingState,
        offlineRouteReady: Boolean,
        offlineBaseMapReady: Boolean
    ): LowPowerGuidancePresentation {
        val offlineAction = if (offlineRouteReady && offlineBaseMapReady) {
            LowPowerGuidanceAction(
                title = "保持离线上下文",
                caption = "继续保留已保存路线、离线底图、截图和关键地标，减少反复打开地图。"
            )
        } else {
            LowPowerGuidanceAction(
                title = "保留离线线索",
                caption = "弱网时不要反复尝试下载；优先保留现有 GPX、截图和地标信息。"
            )
        }
        val actions = listOf(
            LowPowerGuidanceAction(
                title = "降低屏幕使用",
                caption = "看清下一段路线后锁屏，保留轨迹记录和定位通知。"
            ),
            LowPowerGuidanceAction(
                title = "确认返程",
                caption = "先确认下一检查点、预计返回和可撤退方向。"
            ),
            LowPowerGuidanceAction(
                title = "准备补电",
                caption = "有充电宝先接入，保留电量给定位、通话和求助信息。"
            ),
            offlineAction
        )

        return LowPowerGuidancePresentation(
            visible = true,
            title = "低电量行动建议",
            statusLabel = "电量偏低",
            caption = lowPowerCaption(trackRecording),
            primaryActionLabel = "刷新最后可靠位置",
            primaryActionRequestsFinalFix = true,
            tone = LowPowerGuidanceTone.CAUTION,
            actions = actions
        )
    }

    private fun criticalBatteryPresentation(
        trackRecording: TrackRecordingState
    ): LowPowerGuidancePresentation =
        LowPowerGuidancePresentation(
            visible = true,
            title = "低电量行动建议",
            statusLabel = "电量危险",
            caption = lowPowerCaption(trackRecording),
            primaryActionLabel = "刷新最后可靠位置",
            primaryActionRequestsFinalFix = true,
            tone = LowPowerGuidanceTone.ALERT,
            actions = listOf(
                LowPowerGuidanceAction(
                    title = "立即缩短路线",
                    caption = "优先原路返回、前往最近已知安全点或等待同伴会合。"
                ),
                LowPowerGuidanceAction(
                    title = "记录最后位置",
                    caption = "刷新定位后截图或分享求助信息，便于断电后说明位置。"
                ),
                LowPowerGuidanceAction(
                    title = "连接电源",
                    caption = "有充电宝先接入，保留电量给定位和通话。"
                ),
                LowPowerGuidanceAction(
                    title = "降低耗电",
                    caption = "减少看图频率，锁屏依靠前台轨迹通知确认记录仍在。"
                )
            )
        )

    private fun lowPowerCaption(trackRecording: TrackRecordingState): String {
        val recordingHint = if (trackRecording.status == TrackRecordingStatus.RECORDING) {
            "轨迹正在记录，"
        } else {
            ""
        }
        return "${recordingHint}TrailMate 不能延长电量或保证持续导航，也不会自动调整系统电源设置。"
    }
}
