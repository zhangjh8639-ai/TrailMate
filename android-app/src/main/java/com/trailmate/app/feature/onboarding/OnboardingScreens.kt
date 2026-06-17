package com.trailmate.app.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMateMetricRow
import com.trailmate.app.core.design.TrailMatePanel
import com.trailmate.app.core.design.TrailMatePanelTone
import com.trailmate.app.core.design.TrailMateSegmentedControl
import com.trailmate.app.core.model.AscentExperience
import com.trailmate.app.core.model.BaselineProfile
import com.trailmate.app.core.model.ExerciseFrequency
import com.trailmate.app.core.model.ExperienceLevel
import com.trailmate.app.core.model.TrailMateSampleData
import com.trailmate.app.core.model.TypicalDuration

@Composable
fun OnboardingScreen(onComplete: (BaselineProfile) -> Unit) {
    var step by rememberSaveable { mutableStateOf(OnboardingStep.Account) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Text(
            text = "TrailMate",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Personal route assessment, light navigation, and route-ready gear checks.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
        )

        when (step) {
            OnboardingStep.Account -> AccountStep(
                email = email,
                password = password,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onContinue = { step = OnboardingStep.Profile }
            )

            OnboardingStep.Profile -> BaselineProfileStep(
                onBack = { step = OnboardingStep.Account },
                onComplete = onComplete
            )
        }
    }
}

@Composable
private fun AccountStep(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    TrailMatePanel(
        title = "Account",
        value = "Sign in",
        caption = "Create an account or sign in before building your private baseline profile."
    )
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
    TrailMatePanel(
        title = "Privacy",
        value = "Private by default",
        caption = "Questionnaire answers stay in your TrailMate profile and only seed low-confidence estimates.",
        tone = TrailMatePanelTone.Neutral
    )
    Button(
        onClick = onContinue,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text("Start baseline profile")
    }
    TextButton(
        onClick = onContinue,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Sign in instead")
    }
    Text(
        text = "TrailMate does not guarantee safety, provide rescue, or make medical judgments.",
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

    TrailMatePanel(
        title = "Baseline profile",
        value = "2 min",
        caption = "These answers are low-confidence defaults until enough GPX history is imported."
    )
    OptionControl(
        title = "Exercise rhythm",
        labels = listOf("Rarely", "1-2/wk", "3+/wk"),
        selected = exercise.exerciseLabel(),
        onSelected = { label -> exercise = exerciseFromLabel(label) }
    )
    OptionControl(
        title = "Typical session",
        labels = listOf("<30m", "30-60m", "60m+"),
        selected = duration.durationLabel(),
        onSelected = { label -> duration = durationFromLabel(label) }
    )
    OptionControl(
        title = "Outdoor experience",
        labels = listOf("Beginner", "Regular", "Experienced"),
        selected = experience.experienceLabel(),
        onSelected = { label -> experience = experienceFromLabel(label) }
    )
    OptionControl(
        title = "Recent ascent",
        labels = listOf("<300m", "300-800m", "800m+"),
        selected = ascent.ascentLabel(),
        onSelected = { label -> ascent = ascentFromLabel(label) }
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NumberField(
            value = heightCm,
            onValueChange = { heightCm = it },
            label = "Height cm",
            modifier = Modifier.weight(1f)
        )
        NumberField(
            value = weightKg,
            onValueChange = { weightKg = it },
            label = "Weight kg",
            modifier = Modifier.weight(1f)
        )
    }
    NumberField(
        value = packWeightKg,
        onValueChange = { packWeightKg = it },
        label = "Usual pack kg",
        modifier = Modifier.fillMaxWidth()
    )
    TrailMateMetricRow(
        items = listOf(
            "Confidence" to "LOW",
            "Body" to draftBodyLabel(heightCm, weightKg),
            "Pack" to (packWeightKg.toIntOrNull()?.let { "$it kg" } ?: "TBD")
        )
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Save profile")
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onBack) {
            Text("Back")
        }
        TextButton(onClick = { onComplete(TrailMateSampleData.skippedBaselineProfile) }) {
            Text("Skip for now")
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
    Profile
}

private fun ExerciseFrequency.exerciseLabel(): String =
    when (this) {
        ExerciseFrequency.RARELY -> "Rarely"
        ExerciseFrequency.ONE_TO_TWO_PER_WEEK -> "1-2/wk"
        ExerciseFrequency.THREE_PLUS_PER_WEEK -> "3+/wk"
    }

private fun exerciseFromLabel(label: String): ExerciseFrequency =
    when (label) {
        "Rarely" -> ExerciseFrequency.RARELY
        "3+/wk" -> ExerciseFrequency.THREE_PLUS_PER_WEEK
        else -> ExerciseFrequency.ONE_TO_TWO_PER_WEEK
    }

private fun TypicalDuration.durationLabel(): String =
    when (this) {
        TypicalDuration.UNDER_30 -> "<30m"
        TypicalDuration.MIN_30_TO_60 -> "30-60m"
        TypicalDuration.OVER_60 -> "60m+"
    }

private fun durationFromLabel(label: String): TypicalDuration =
    when (label) {
        "<30m" -> TypicalDuration.UNDER_30
        "60m+" -> TypicalDuration.OVER_60
        else -> TypicalDuration.MIN_30_TO_60
    }

private fun ExperienceLevel.experienceLabel(): String =
    when (this) {
        ExperienceLevel.BEGINNER -> "Beginner"
        ExperienceLevel.REGULAR -> "Regular"
        ExperienceLevel.EXPERIENCED -> "Experienced"
    }

private fun experienceFromLabel(label: String): ExperienceLevel =
    when (label) {
        "Beginner" -> ExperienceLevel.BEGINNER
        "Experienced" -> ExperienceLevel.EXPERIENCED
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

private fun draftBodyLabel(heightCm: String, weightKg: String): String {
    val height = heightCm.toIntOrNull()?.let { "${it}cm" }
    val weight = weightKg.toIntOrNull()?.let { "${it}kg" }

    return listOfNotNull(height, weight).joinToString(" / ").ifBlank { "TBD" }
}
