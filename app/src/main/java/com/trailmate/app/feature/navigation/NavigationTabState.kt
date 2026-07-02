package com.trailmate.app.feature.navigation

import com.trailmate.app.feature.routes.RouteDetailState

data class NavigationTabState(
    val title: String,
    val idleState: NavigationIdleState,
    val selectedRoute: NavigationRouteReadyState? = null,
) {
    fun visibleText(): List<String> =
        buildList {
            add(title)
            selectedRoute?.let { addAll(it.visibleText()) } ?: addAll(idleState.visibleText())
        }
}

data class NavigationIdleState(
    val title: String,
    val body: String,
    val routeActionLabel: String,
) {
    fun visibleText(): List<String> = listOf(title, body, routeActionLabel)
}

data class NavigationRouteReadyState(
    val routeKey: String,
    val statusLabel: String,
    val routeName: String,
    val region: String,
    val sourceLabel: String,
    val offlineStatusLabel: String,
    val metrics: List<NavigationMetricState>,
    val confidenceLabel: String,
    val riskTags: List<String>,
    val boundaryNotes: List<String>,
    val changeRouteActionLabel: String = "更换路线",
) {
    fun visibleText(): List<String> =
        buildList {
            add(statusLabel)
            add(routeName)
            add(region)
            add(sourceLabel)
            add(offlineStatusLabel)
            metrics.forEach { metric ->
                add(metric.label)
                add(metric.value)
            }
            add(confidenceLabel)
            add(changeRouteActionLabel)
            addAll(riskTags)
            addAll(boundaryNotes)
        }
}

data class NavigationMetricState(
    val label: String,
    val value: String,
)

object NavigationTabSampleState {
    fun build(): NavigationTabState =
        NavigationTabState(
            title = "导航",
            idleState = NavigationIdleState(
                title = "尚未选择路线",
                body = "从路线页选择可导航路线，确认轨迹、离线状态和风险后再进入导航。",
                routeActionLabel = "去路线页选择可导航路线",
            ),
        )
}

fun NavigationTabState.withSelectedRoute(detail: RouteDetailState): NavigationTabState =
    copy(selectedRoute = detail.toNavigationRouteReadyState())

private fun RouteDetailState.toNavigationRouteReadyState(): NavigationRouteReadyState =
    NavigationRouteReadyState(
        routeKey = routeKey,
        statusLabel = "轨迹导航待开始",
        routeName = title,
        region = subtitle,
        sourceLabel = sourceLabel,
        offlineStatusLabel = offlineStatusLabel,
        metrics = metrics.map { metric ->
            NavigationMetricState(
                label = metric.label,
                value = metric.value,
            )
        },
        confidenceLabel = confidenceLabel,
        riskTags = riskTags,
        boundaryNotes = boundaryNotes,
    )
