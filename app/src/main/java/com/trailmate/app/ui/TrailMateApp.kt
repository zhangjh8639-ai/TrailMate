package com.trailmate.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TrailMateApp() {
    var selectedTab by rememberSaveable { mutableStateOf(TrailMateTab.Discover) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                TrailMateTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        icon = {
                            Icon(
                                imageVector = tab.icon(),
                                contentDescription = tab.label,
                            )
                        },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            TabContent(
                tab = selectedTab,
                paddingValues = innerPadding,
            )
        }
    }
}

@Composable
private fun TabContent(
    tab: TrailMateTab,
    paddingValues: PaddingValues,
) {
    val copy = tab.screenCopy()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 24.dp, vertical = 72.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary,
                    ) {
                        Icon(
                            modifier = Modifier.padding(10.dp),
                            imageVector = tab.icon(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = copy.eyebrow,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = copy.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Text(
                    text = copy.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = copy.status,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

private fun TrailMateTab.icon(): ImageVector =
    when (this) {
        TrailMateTab.Discover -> Icons.Outlined.Explore
        TrailMateTab.Routes -> Icons.Outlined.Map
        TrailMateTab.Navigation -> Icons.Outlined.Navigation
        TrailMateTab.Records -> Icons.Outlined.Timeline
        TrailMateTab.Profile -> Icons.Outlined.Person
    }

private fun TrailMateTab.screenCopy(): ScreenCopy =
    when (this) {
        TrailMateTab.Discover -> ScreenCopy(
            eyebrow = "路线发现",
            title = "找到适合今天的可信路线",
            body = "聚合天气、距离、爬升、难度和近期反馈，帮助用户先选对路线。",
            status = "下一步：接入路线卡片与可信度标签",
        )
        TrailMateTab.Routes -> ScreenCopy(
            eyebrow = "路线资产",
            title = "管理可离线导航的路线",
            body = "承载已离线、已导入、收藏和最近导航路线，GPX/KML 导入也放在这里。",
            status = "下一步：路线列表、导入结果、路线详情",
        )
        TrailMateTab.Navigation -> ScreenCopy(
            eyebrow = "核心导航",
            title = "沿轨迹导航，不迷路",
            body = "导航中会聚焦地图、当前位置、偏航状态、剩余距离、原路返回和紧急卡片。",
            status = "下一步：模拟导航状态机与地图占位",
        )
        TrailMateTab.Records -> ScreenCopy(
            eyebrow = "复盘反馈",
            title = "沉淀轨迹与路况",
            body = "结束后复盘实际轨迹、偏航、停留点，并提交结构化路况反馈。",
            status = "下一步：记录列表与反馈表单",
        )
        TrailMateTab.Profile -> ScreenCopy(
            eyebrow = "安全设置",
            title = "管理隐私、离线数据和紧急信息",
            body = "集中处理离线路线占用空间、轨迹默认私密、紧急联系人和设备导航权限。",
            status = "下一步：隐私默认与离线数据入口",
        )
    }

private data class ScreenCopy(
    val eyebrow: String,
    val title: String,
    val body: String,
    val status: String,
)
