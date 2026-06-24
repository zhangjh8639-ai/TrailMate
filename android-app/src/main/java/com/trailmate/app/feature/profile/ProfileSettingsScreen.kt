package com.trailmate.app.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateGlyph
import com.trailmate.app.core.design.TrailMateLineIcon
import com.trailmate.app.core.design.TrailMatePageScaffold
import com.trailmate.app.core.design.TrailMateSectionHeader
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.feature.home.DataControlClearUiState

@Composable
fun ProfileSettingsScreen(
    profile: BaselineProfile,
    onClearLocalData: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isConfirmingClear by rememberSaveable { mutableStateOf(false) }
    var isConfirmingLogout by rememberSaveable { mutableStateOf(false) }
    val clearUiState = DataControlClearUiState(isConfirmingClear = isConfirmingClear)
    val logoutUiState = AccountLogoutUiState(isConfirmingLogout = isConfirmingLogout)

    TrailMatePageScaffold(
        title = "我的",
        caption = "档案、权限、隐私和本机数据管理。",
        modifier = modifier
    ) {
        ProfileSummaryCard(profile = profile)
        CompactInfoCard(
            glyph = TrailMateGlyph.Warning,
            title = "安全边界",
            value = "辅助建议",
            caption = "TrailMate 不替代离线地图、路标和现场判断。"
        )

        TrailMateSectionHeader(title = "权限状态")
        SettingsGroupCard {
            SettingStatusRow(
                glyph = TrailMateGlyph.Location,
                title = "定位权限",
                value = "路线页使用",
                caption = "开始导航或记录时请求定位权限。"
            )
            SettingsDivider()
            SettingStatusRow(
                glyph = TrailMateGlyph.Bell,
                title = "通知权限",
                value = "按需提醒",
                caption = "轨迹记录和安全提醒会使用系统通知。"
            )
            SettingsDivider()
            SettingStatusRow(
                glyph = TrailMateGlyph.Map,
                title = "地图授权",
                value = "路线页确认",
                caption = "进入实景地图能力时确认隐私授权。"
            )
        }

        TrailMateSectionHeader(title = "账号")
        AccountControls(
            logoutUiState = logoutUiState,
            onRequestLogout = {
                isConfirmingLogout = logoutUiState.requestLogout().isConfirmingLogout
            },
            onConfirmLogout = {
                isConfirmingLogout = logoutUiState.confirmLogout(onLogout).isConfirmingLogout
            },
            onCancelLogout = {
                isConfirmingLogout = logoutUiState.cancelLogout().isConfirmingLogout
            }
        )

        TrailMateSectionHeader(title = "数据与隐私")
        LocalDataControls(
            clearUiState = clearUiState,
            onRequestClear = {
                isConfirmingClear = clearUiState.requestClear().isConfirmingClear
            },
            onConfirmClear = {
                isConfirmingClear = clearUiState.confirmClear(onClearLocalData).isConfirmingClear
            },
            onCancelClear = {
                isConfirmingClear = clearUiState.cancelClear().isConfirmingClear
            }
        )
    }
}

@Composable
private fun AccountControls(
    logoutUiState: AccountLogoutUiState,
    onRequestLogout: () -> Unit,
    onConfirmLogout: () -> Unit,
    onCancelLogout: () -> Unit
) {
    SettingsGroupCard {
        SettingStatusRow(
            glyph = TrailMateGlyph.Profile,
            title = "账号登录",
            value = "已登录",
            caption = "退出后保留本机路线、活动和装备匹配缓存。"
        )
        SettingsDivider()
        if (logoutUiState.isConfirmingLogout) {
            Column(
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "退出当前账号？",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "这只会退出账号，不会清除本机 GPX、路线、历史记录和装备匹配缓存。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onConfirmLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("确认退出")
                }
                OutlinedButton(
                    onClick = onCancelLogout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("取消")
                }
            }
        } else {
            OutlinedButton(
                onClick = onRequestLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 12.dp)
            ) {
                Text("退出登录")
            }
        }
    }
}

@Composable
private fun ProfileSummaryCard(profile: BaselineProfile) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileIconBadge(glyph = TrailMateGlyph.Profile)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "基础档案",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (profile.hasSavedContext()) "已保存" else "待完善",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "运动习惯、户外经验和身体信息默认保存在本机。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CompactInfoCard(
    glyph: TrailMateGlyph,
    title: String,
    value: String,
    caption: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        SettingStatusRow(
            glyph = glyph,
            title = title,
            value = value,
            caption = caption
        )
    }
}

@Composable
private fun SettingsGroupCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingStatusRow(
    glyph: TrailMateGlyph,
    title: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileIconBadge(
            glyph = glyph,
            modifier = Modifier.size(42.dp),
            muted = true
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LocalDataControls(
    clearUiState: DataControlClearUiState,
    onRequestClear: () -> Unit,
    onConfirmClear: () -> Unit,
    onCancelClear: () -> Unit
) {
    SettingsGroupCard {
        SettingStatusRow(
            glyph = TrailMateGlyph.Folder,
            title = "本机数据",
            value = "仅本机",
            caption = "基础档案、路线、活动资料和装备匹配缓存保存在这台设备上。"
        )
        SettingsDivider()
        if (clearUiState.isConfirmingClear) {
            Column(
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "清除本地数据？",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "这会从本设备移除档案、路线、历史和装备匹配缓存。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onConfirmClear,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("确认清除")
                }
                OutlinedButton(
                    onClick = onCancelClear,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("取消")
                }
            }
        } else {
            OutlinedButton(
                onClick = onRequestClear,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 12.dp)
            ) {
                Text("清除本地数据")
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 15.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)
    )
}

@Composable
private fun ProfileIconBadge(
    glyph: TrailMateGlyph,
    modifier: Modifier = Modifier.size(50.dp),
    muted: Boolean = false
) {
    val containerColor = if (muted) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.primary
    }
    val contentColor = if (muted) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        TrailMateLineIcon(
            glyph = glyph,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = contentColor
        )
    }
}

private fun BaselineProfile.hasSavedContext(): Boolean =
    heightCm != null ||
        weightKg != null ||
        commonPackWeightKg != null
