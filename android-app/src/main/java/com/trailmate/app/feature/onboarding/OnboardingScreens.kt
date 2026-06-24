package com.trailmate.app.feature.onboarding

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.trailmate.app.core.auth.TrailMateAuthActionResult
import com.trailmate.app.core.auth.TrailMateAuthSession
import com.trailmate.app.core.auth.TrailMateLocalOnboardingAuthActions
import com.trailmate.app.core.auth.TrailMateOnboardingAuthActions
import com.trailmate.app.core.auth.TrailMateWechatCallbackAuthActions
import com.trailmate.app.core.design.TrailMateGlyph
import com.trailmate.app.core.design.TrailMateLineIcon
import com.trailmate.app.core.design.TrailMateSegmentedControl
import com.trailmate.app.core.map.AmapPrivacyConsent
import com.trailmate.app.core.model.AscentExperience
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.ExerciseFrequency
import com.trailmate.app.core.model.ExperienceLevel
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.core.model.TypicalDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun OnboardingScreen(
    initialAuthSession: TrailMateAuthSession? = null,
    onAuthenticated: (TrailMateAuthSession) -> Unit = {},
    authActions: TrailMateOnboardingAuthActions = TrailMateLocalOnboardingAuthActions(),
    wechatLoginAvailable: Boolean = true,
    onComplete: (BaselineProfile, AmapPrivacyConsent) -> Unit,
    requestForegroundLocationPermissionOnComplete: Boolean = true
) {
    val context = LocalContext.current
    var step by rememberSaveable {
        mutableStateOf(
            if (initialAuthSession == null) {
                OnboardingStep.Account
            } else {
                OnboardingStep.Profile
            }
        )
    }
    var pendingProfile by rememberSaveable(stateSaver = OnboardingProfileStateSaver) {
        mutableStateOf(TrailMateSampleData.skippedBaselineProfile)
    }
    val completeOnboarding: (Boolean) -> Unit = { accepted ->
        onComplete(
            pendingProfile,
            if (accepted) {
                AmapPrivacyConsent.accepted(nowEpochMillis = System.currentTimeMillis())
            } else {
                AmapPrivacyConsent()
            }
        )
    }
    val foregroundLocationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }
    val completeThenMaybeRequestLocationPermission: (Boolean) -> Unit = { accepted ->
        completeOnboarding(accepted)
        if (
            accepted &&
            requestForegroundLocationPermissionOnComplete &&
            !context.hasForegroundLocationPermission()
        ) {
            foregroundLocationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    val scrollState = rememberScrollState()

    LaunchedEffect(step) {
        scrollState.animateScrollTo(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(Modifier.height(2.dp))
        OnboardingHeader(step = step)

        when (step) {
            OnboardingStep.Account -> AccountStep(
                authActions = authActions,
                wechatLoginAvailable = wechatLoginAvailable,
                onAuthenticated = { session ->
                    onAuthenticated(session)
                    step = OnboardingStep.Profile
                }
            )

            OnboardingStep.Profile -> BaselineProfileStep(
                onBack = { step = OnboardingStep.Account },
                onComplete = { profile ->
                    pendingProfile = profile
                    step = OnboardingStep.MapServices
                }
            )

            OnboardingStep.MapServices -> MapServicesStep(
                onBack = { step = OnboardingStep.Profile },
                onAccept = {
                    completeThenMaybeRequestLocationPermission(true)
                },
                onUseLocalOnly = {
                    completeThenMaybeRequestLocationPermission(false)
                }
            )
        }
    }
}

@Composable
private fun AccountStep(
    authActions: TrailMateOnboardingAuthActions,
    wechatLoginAvailable: Boolean,
    onAuthenticated: (TrailMateAuthSession) -> Unit
) {
    var authState by rememberSaveable(stateSaver = AccountAuthUiStateSaver) {
        mutableStateOf(AccountAuthUiState.initial(wechatAvailable = wechatLoginAvailable))
    }
    var phoneInput by rememberSaveable { mutableStateOf("") }
    var smsCode by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val lifecycleOwner = context as? LifecycleOwner
    val selectedMethod = authState.method
    val authScope = rememberCoroutineScope()
    val authMethodLabels = accountAuthMethodLabels(wechatAvailable = wechatLoginAvailable)

    LaunchedEffect(wechatLoginAvailable) {
        authState = authState.withWechatAvailability(wechatLoginAvailable)
    }

    fun handleAuthResult(result: TrailMateAuthActionResult<TrailMateAuthSession>) {
        when (result) {
            is TrailMateAuthActionResult.Success -> onAuthenticated(result.value)
            is TrailMateAuthActionResult.InvalidInput -> {
                authState = authState.idle(result.message)
            }
            is TrailMateAuthActionResult.Failure -> {
                authState = authState.idle(result.message)
            }
        }
    }

    fun requestPhoneCode() {
        if (authState.phase != AccountAuthPhase.IDLE) {
            return
        }
        authState = authState.processing("正在获取验证码...")
        authScope.launch {
            val result = withContext(Dispatchers.IO) {
                authActions.requestPhoneCode(phoneInput)
            }
            when (result) {
                is TrailMateAuthActionResult.Success -> {
                    authState = authState.idle(
                        message = "验证码已发送至 ${result.value.phoneNumber.takeLast(4).padStart(11, '*')}。",
                        codeRequested = true
                    )
                }
                is TrailMateAuthActionResult.InvalidInput -> {
                    authState = authState.idle(result.message, codeRequested = false)
                }
                is TrailMateAuthActionResult.Failure -> {
                    authState = authState.idle(result.message, codeRequested = false)
                }
            }
        }
    }

    fun loginWithPhone() {
        if (authState.phase != AccountAuthPhase.IDLE) {
            return
        }
        authState = authState.processing("正在登录...")
        authScope.launch {
            val result = withContext(Dispatchers.IO) {
                authActions.loginWithPhone(phoneInput, smsCode)
            }
            handleAuthResult(result)
        }
    }

    fun consumeWechatCallbackIfAvailable() {
        val callbackActions = authActions as? TrailMateWechatCallbackAuthActions ?: return
        if (!authState.shouldConsumeWechatCallbackOnResume) {
            return
        }
        if (authState.phase == AccountAuthPhase.PROCESSING) {
            return
        }
        val wasWaitingForWechat = authState.phase == AccountAuthPhase.WAITING_WECHAT_CALLBACK
        authState = authState.processing("正在确认微信授权...")
        authScope.launch {
            val result = withContext(Dispatchers.IO) {
                callbackActions.consumeWechatCallback()
            }
            if (result == null) {
                authState = if (wasWaitingForWechat) {
                    authState.idle("未收到微信授权结果，可以重新发起。")
                } else {
                    authState.idle(authState.message)
                }
            } else {
                handleAuthResult(result)
            }
        }
    }

    fun loginWithWechat() {
        if (!authState.canSubmitWechat) {
            return
        }
        authState = authState.processing("正在打开微信...")
        authScope.launch {
            val result = withContext(Dispatchers.IO) {
                authActions.loginWithWechat()
            }
            if (
                result is TrailMateAuthActionResult.InvalidInput &&
                result.message.contains("已打开微信授权")
            ) {
                authState = authState.waitingForWechatCallback()
            } else {
                handleAuthResult(result)
            }
        }
    }

    DisposableEffect(selectedMethod, authState.phase, authActions, lifecycleOwner) {
        if (selectedMethod == AccountAuthMethod.WECHAT && lifecycleOwner != null) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    consumeWechatCallbackIfAvailable()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        } else {
            onDispose { }
        }
    }

    OnboardingHeroCard(
        glyph = TrailMateGlyph.Profile,
        eyebrow = "微信优先登录",
        title = "先建立你的私人空间",
        caption = if (wechatLoginAvailable) {
            "一个入口完成登录或注册。随后只收集必要资料，用于路线评估、补给和装备建议。"
        } else {
            "微信登录暂不可用时使用手机号兜底。登录后再收集基础资料，用于路线评估、补给和装备建议。"
        }
    )
    OnboardingSectionCard(
        glyph = TrailMateGlyph.Profile,
        title = "账号方式",
        caption = if (wechatLoginAvailable) {
            "同一个微信入口完成注册和登录；授权处理中会保持等待状态，避免重复发起。"
        } else {
            "微信登录未配置，先使用手机号完成注册和登录。"
        }
    ) {
        TrailMateSegmentedControl(
            labels = authMethodLabels,
            selected = selectedMethod.label,
            onSelected = { selected ->
                if (authState.canChangeMethod) {
                    val nextMethod = AccountAuthMethod.fromLabel(
                        label = selected,
                        wechatAvailable = wechatLoginAvailable
                    )
                    authState = authState.withMethod(nextMethod)
                }
            }
        )
        if (selectedMethod == AccountAuthMethod.PHONE) {
            OutlinedTextField(
                value = phoneInput,
                onValueChange = { input ->
                    phoneInput = input.filter { char -> char.isDigit() || char == '+' || char == ' ' || char == '-' }
                        .take(18)
                },
                label = { Text("手机号") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = smsCode,
                    onValueChange = { input -> smsCode = input.filter(Char::isDigit).take(8) },
                    label = { Text("验证码") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextButton(
                    onClick = {
                        requestPhoneCode()
                    },
                    enabled = phoneInput.isNotBlank() && authState.phase == AccountAuthPhase.IDLE
                ) {
                    Text(authState.phoneCodeActionLabel)
                }
            }
            Text(
                text = authState.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = {
                    loginWithPhone()
                },
                enabled = phoneInput.isNotBlank() &&
                    smsCode.isNotBlank() &&
                    authState.phase == AccountAuthPhase.IDLE,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
            ) {
                Text(authState.primaryActionLabel)
            }
        } else {
            OnboardingPermissionNote(
                glyph = TrailMateGlyph.Check,
                title = "微信授权",
                caption = "使用微信完成身份确认，TrailMate 后端会创建或绑定你的私人账号。"
            )
            Button(
                onClick = {
                    loginWithWechat()
                },
                enabled = authState.canSubmitWechat,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
            ) {
                Text(authState.primaryActionLabel)
            }
            Text(
                text = authState.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    OnboardingInfoCard(
        glyph = TrailMateGlyph.Check,
        title = "默认私密",
        caption = "运动、户外经验和身体信息只用于路线评估，不在主页展示。"
    )
    Text(
        text = "TrailMate 仅提供路线准备辅助，不替代离线地图、路标与安全判断。",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
    )
}

@Composable
private fun BaselineProfileStep(
    onBack: () -> Unit,
    onComplete: (BaselineProfile) -> Unit
) {
    var exercise by rememberSaveable { mutableStateOf(ExerciseFrequency.ONE_TO_TWO_PER_WEEK) }
    var duration by rememberSaveable { mutableStateOf(TypicalDuration.MIN_30_TO_60) }
    var experience by rememberSaveable { mutableStateOf(ExperienceLevel.REGULAR) }
    var ascent by rememberSaveable { mutableStateOf(AscentExperience.M300_TO_800) }
    var heightCm by rememberSaveable { mutableStateOf("") }
    var weightKg by rememberSaveable { mutableStateOf("") }
    var packWeightKg by rememberSaveable { mutableStateOf("") }

    OnboardingHeroCard(
        glyph = TrailMateGlyph.Spark,
        eyebrow = "基础档案",
        title = "让路线评估更贴近你",
        caption = "大约 2 分钟。TrailMate 会用这些资料判断体能压力、休息点和装备建议。"
    )

    OnboardingSectionCard(
        glyph = TrailMateGlyph.Chart,
        title = "运动习惯",
        caption = "判断目标路线对日常运动量的压力。"
    ) {
        OptionControl(
            title = "平时运动频率",
            labels = listOf("很少", "每周1-2次", "每周3次+"),
            selected = exercise.exerciseLabel(),
            onSelected = { label -> exercise = exerciseFromLabel(label) }
        )
        OptionControl(
            title = "单次运动时长",
            labels = listOf("<30分钟", "30-60分钟", "60分钟+"),
            selected = duration.durationLabel(),
            onSelected = { label -> duration = durationFromLabel(label) }
        )
    }

    OnboardingSectionCard(
        glyph = TrailMateGlyph.Mountain,
        title = "户外经验",
        caption = "用来识别爬升、长距离和暴露路段的风险。"
    ) {
        OptionControl(
            title = "户外经验",
            labels = listOf("新手", "常规", "经验丰富"),
            selected = experience.experienceLabel(),
            onSelected = { label -> experience = experienceFromLabel(label) }
        )
        OptionControl(
            title = "近期爬升经验",
            labels = listOf("<300m", "300-800m", "800m+"),
            selected = ascent.ascentLabel(),
            onSelected = { label -> ascent = ascentFromLabel(label) }
        )
    }

    OnboardingSectionCard(
        glyph = TrailMateGlyph.Gear,
        title = "身体与负重",
        caption = "可选填写；会影响用时、补给和装备重量判断。"
    ) {
        NumberField(
            value = heightCm,
            onValueChange = { heightCm = it },
            label = "身高 cm",
            modifier = Modifier.fillMaxWidth()
        )
        NumberField(
            value = weightKg,
            onValueChange = { weightKg = it },
            label = "体重 kg",
            modifier = Modifier.fillMaxWidth()
        )
        NumberField(
            value = packWeightKg,
            onValueChange = { packWeightKg = it },
            label = "常用背包 kg",
            modifier = Modifier.fillMaxWidth()
        )
    }

    OnboardingInfoCard(
        glyph = TrailMateGlyph.Folder,
        title = "默认不展示这些资料",
        caption = "只用于路线评估，不在主页展示；正式页面只展示目标路线、休息补给和装备结论。"
    )

    Button(
        onClick = {
            onComplete(
                BaselineProfile(
                    exerciseFrequency = exercise,
                    typicalDuration = duration,
                    experienceLevel = experience,
                    ascentExperience = ascent,
                    heightCm = heightCm.toIntOrNull(),
                    weightKg = weightKg.toIntOrNull(),
                    commonPackWeightKg = packWeightKg.toIntOrNull()
                )
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
    ) {
        Text("保存档案")
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onBack) {
            Text("返回")
        }
        TextButton(onClick = { onComplete(TrailMateSampleData.skippedBaselineProfile) }) {
            Text("暂时跳过")
        }
    }
}

@Composable
private fun MapServicesStep(
    onBack: () -> Unit,
    onAccept: () -> Unit,
    onUseLocalOnly: () -> Unit
) {
    OnboardingHeroCard(
        glyph = TrailMateGlyph.Map,
        eyebrow = "地图与定位准备",
        title = "先完成出发前授权",
        caption = "离线地图包、当前位置和轨迹记录授权集中在首次设置处理；路线页只展示路线与行动。"
    )
    OnboardingSectionCard(
        glyph = TrailMateGlyph.Map,
        title = "离线地图包",
        caption = "导入目标区域 PMTiles/OSM 地图包后，弱网时也能查看道路、地名和地形背景。"
    ) {
        OnboardingPermissionNote(
            glyph = TrailMateGlyph.Check,
            title = "同意地图服务",
            caption = "TrailMate 会在初始化地图与定位能力前记录你的选择，避免进入路线页后反复打断。"
        )
        OnboardingPermissionNote(
            glyph = TrailMateGlyph.Folder,
            title = "仅使用本地路线",
            caption = "你仍可查看 GPX、评估风险、准备装备；之后需要在线底图时可回到首次设置重新选择。"
        )
    }
    OnboardingSectionCard(
        glyph = TrailMateGlyph.Location,
        title = "定位授权",
        caption = "继续后会请求系统定位权限，用于当前位置、路线辅助和真实轨迹记录。"
    ) {
        OnboardingPermissionNote(
            glyph = TrailMateGlyph.Location,
            title = "当前位置与路线辅助",
            caption = "授权后路线页可直接进入定位、路线校验与轨迹记录；若你拒绝授权，开始徒步时会再给出兜底提醒。"
        )
        OnboardingPermissionNote(
            glyph = TrailMateGlyph.Bell,
            title = "轨迹记录通知",
            caption = "开始记录轨迹时再请求，用于保持记录状态并展示记录控制。"
        )
    }
    Button(
        onClick = onAccept,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp)
    ) {
        Text("同意地图服务并继续")
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onBack) {
            Text("返回")
        }
        TextButton(onClick = onUseLocalOnly) {
            Text("稍后，仅使用本地路线")
        }
    }
}

@Composable
private fun OnboardingHeader(step: OnboardingStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrailMateLineIcon(
            glyph = TrailMateGlyph.Mountain,
            contentDescription = null,
            modifier = Modifier.size(34.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = "TrailMate",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "路线评估、路线辅助和装备检查",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
            )
        }
        Spacer(Modifier.weight(1f))
        StepPill(text = step.stepText())
    }
}

@Composable
private fun StepPill(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun OnboardingHeroCard(
    glyph: TrailMateGlyph,
    eyebrow: String,
    title: String,
    caption: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 5.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OnboardingGlyphBox(glyph = glyph, prominent = true)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = eyebrow,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun OnboardingInfoCard(
    glyph: TrailMateGlyph,
    title: String,
    caption: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OnboardingGlyphBox(glyph = glyph)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OnboardingSectionCard(
    glyph: TrailMateGlyph,
    title: String,
    caption: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OnboardingGlyphBox(glyph = glyph)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun OnboardingPermissionNote(
    glyph: TrailMateGlyph,
    title: String,
    caption: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OnboardingGlyphBox(glyph = glyph)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OnboardingGlyphBox(
    glyph: TrailMateGlyph,
    prominent: Boolean = false
) {
    val container = if (prominent) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    }
    val tint = if (prominent) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = Modifier.size(if (prominent) 58.dp else 44.dp),
        shape = RoundedCornerShape(if (prominent) 18.dp else 14.dp),
        color = container
    ) {
        Box(contentAlignment = Alignment.Center) {
            TrailMateLineIcon(
                glyph = glyph,
                contentDescription = null,
                modifier = Modifier.size(if (prominent) 28.dp else 23.dp),
                tint = tint
            )
        }
    }
}

@Composable
private fun OptionControl(
    title: String,
    labels: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        TrailMateSegmentedControl(
            labels = labels,
            selected = selected,
            onSelected = onSelected
        )
    }
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter(Char::isDigit).take(3)) },
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

private enum class OnboardingStep {
    Account,
    Profile,
    MapServices
}

private fun Context.hasForegroundLocationPermission(): Boolean =
    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun OnboardingStep.stepText(): String =
    when (this) {
        OnboardingStep.Account -> "账号 1/3"
        OnboardingStep.Profile -> "能力基础 2/3"
        OnboardingStep.MapServices -> "地图准备 3/3"
    }

@Suppress("UNCHECKED_CAST")
private val AccountAuthUiStateSaver = androidx.compose.runtime.saveable.mapSaver(
    save = { state ->
        mapOf(
            "method" to state.method.name,
            "phase" to state.phase.name,
            "message" to state.message,
            "codeRequested" to state.codeRequested,
            "wechatAvailable" to state.wechatAvailable
        )
    },
    restore = { saved ->
        AccountAuthUiState(
            method = AccountAuthMethod.valueOf(saved["method"] as String),
            phase = AccountAuthPhase.valueOf(saved["phase"] as String),
            message = saved["message"] as String,
            codeRequested = saved["codeRequested"] as Boolean,
            wechatAvailable = saved["wechatAvailable"] as? Boolean ?: true
        )
    }
)

@Suppress("UNCHECKED_CAST")
private val OnboardingProfileStateSaver = androidx.compose.runtime.saveable.mapSaver(
    save = { profile ->
        mapOf(
            "exerciseFrequency" to profile.exerciseFrequency.name,
            "typicalDuration" to profile.typicalDuration.name,
            "experienceLevel" to profile.experienceLevel.name,
            "ascentExperience" to profile.ascentExperience.name,
            "heightCm" to (profile.heightCm ?: -1),
            "weightKg" to (profile.weightKg ?: -1),
            "commonPackWeightKg" to (profile.commonPackWeightKg ?: -1)
        )
    },
    restore = { saved ->
        BaselineProfile(
            exerciseFrequency = ExerciseFrequency.valueOf(saved["exerciseFrequency"] as String),
            typicalDuration = TypicalDuration.valueOf(saved["typicalDuration"] as String),
            experienceLevel = ExperienceLevel.valueOf(saved["experienceLevel"] as String),
            ascentExperience = AscentExperience.valueOf(saved["ascentExperience"] as String),
            heightCm = (saved["heightCm"] as Int).takeIf { it >= 0 },
            weightKg = (saved["weightKg"] as Int).takeIf { it >= 0 },
            commonPackWeightKg = (saved["commonPackWeightKg"] as Int).takeIf { it >= 0 }
        )
    }
)

private fun ExerciseFrequency.exerciseLabel(): String =
    when (this) {
        ExerciseFrequency.RARELY -> "很少"
        ExerciseFrequency.ONE_TO_TWO_PER_WEEK -> "每周1-2次"
        ExerciseFrequency.THREE_PLUS_PER_WEEK -> "每周3次+"
    }

private fun exerciseFromLabel(label: String): ExerciseFrequency =
    when (label) {
        "很少" -> ExerciseFrequency.RARELY
        "每周3次+" -> ExerciseFrequency.THREE_PLUS_PER_WEEK
        else -> ExerciseFrequency.ONE_TO_TWO_PER_WEEK
    }

private fun TypicalDuration.durationLabel(): String =
    when (this) {
        TypicalDuration.UNDER_30 -> "<30分钟"
        TypicalDuration.MIN_30_TO_60 -> "30-60分钟"
        TypicalDuration.OVER_60 -> "60分钟+"
    }

private fun durationFromLabel(label: String): TypicalDuration =
    when (label) {
        "<30分钟" -> TypicalDuration.UNDER_30
        "60分钟+" -> TypicalDuration.OVER_60
        else -> TypicalDuration.MIN_30_TO_60
    }

private fun ExperienceLevel.experienceLabel(): String =
    when (this) {
        ExperienceLevel.BEGINNER -> "新手"
        ExperienceLevel.REGULAR -> "常规"
        ExperienceLevel.EXPERIENCED -> "经验丰富"
    }

private fun experienceFromLabel(label: String): ExperienceLevel =
    when (label) {
        "新手" -> ExperienceLevel.BEGINNER
        "经验丰富" -> ExperienceLevel.EXPERIENCED
        else -> ExperienceLevel.REGULAR
    }

private fun AscentExperience.ascentLabel(): String =
    when (this) {
        AscentExperience.UNDER_300 -> "<300m"
        AscentExperience.M300_TO_800 -> "300-800m"
        AscentExperience.OVER_800 -> "800m+"
    }

private fun ascentFromLabel(label: String): AscentExperience =
    when (label) {
        "<300m" -> AscentExperience.UNDER_300
        "800m+" -> AscentExperience.OVER_800
        else -> AscentExperience.M300_TO_800
    }
