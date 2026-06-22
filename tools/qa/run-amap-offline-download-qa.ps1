param(
    [string]$DeviceId,
    [string]$CityName = "杭州市",
    [int]$TimeoutMs = 600000,
    [string]$OutputDir,
    [string]$PackageName = "com.trailmate.app",
    [string]$AdbPath = "adb",
    [string]$GradlePath = ".\gradlew.bat",
    [string]$JavaHome,
    [string]$AndroidHome
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Get-Timestamp {
    Get-Date -Format "yyyy-MM-dd HH:mm:ss zzz"
}

function Redact-SensitiveText {
    param([string[]]$Lines)

    $redacted = New-Object System.Collections.Generic.List[string]
    foreach ($line in $Lines) {
        $value = [string]$line
        $value = $value -replace '(?i)(com\.amap\.api\.v2\.apikey[^=:\s]*\s*[=:]\s*)\S+', '$1<redacted>'
        $value = $value -replace '(?i)(AMAP[_-]?API[_-]?KEY\s*[=:]\s*)\S+', '$1<redacted>'
        $value = $value -replace '(?i)(api[_-]?key\s*[=:]\s*)[A-Za-z0-9_\-]{8,}', '$1<redacted>'
        $value = $value -replace '\b[a-fA-F0-9]{32}\b', '<redacted-hex32>'
        $redacted.Add($value)
    }
    $redacted
}

function Write-RedactedFile {
    param(
        [string]$Path,
        [string[]]$Lines
    )

    Redact-SensitiveText $Lines | Set-Content -Path $Path -Encoding UTF8
}

function Resolve-DeviceId {
    if (-not [string]::IsNullOrWhiteSpace($DeviceId)) {
        return $DeviceId
    }

    $devicesOutput = & $AdbPath devices -l 2>&1
    $devices = @(
        $devicesOutput |
            Where-Object { $_ -match '^\S+\s+device\b' } |
            ForEach-Object { ($_ -split '\s+')[0] }
    )

    if ($devices.Count -eq 1) {
        return $devices[0]
    }

    $message = "Expected exactly one connected Android device. Found $($devices.Count)."
    if ($devices.Count -gt 1) {
        $message += " Pass -DeviceId with one of: $($devices -join ', ')."
    }
    throw $message
}

function Invoke-ExternalTextCapture {
    param(
        [string]$FileName,
        [string]$Command,
        [string[]]$Arguments
    )

    $path = Join-Path $script:EvidenceDir $FileName
    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add("# Captured: $(Get-Timestamp)")
    $lines.Add("# Command: $Command $($Arguments -join ' ')")
    $lines.Add("")

    try {
        $output = & $Command @Arguments 2>&1
        $exitCode = $LASTEXITCODE
        $lines.Add("# ExitCode: $exitCode")
        $lines.Add("")
        foreach ($entry in $output) {
            $lines.Add([string]$entry)
        }
    } catch {
        $exitCode = 1
        $lines.Add("# ExitCode: $exitCode")
        $lines.Add("# CaptureError: $($_.Exception.Message)")
    }

    Write-RedactedFile -Path $path -Lines $lines
    return $exitCode
}

function Invoke-DeviceEvidenceCapture {
    param(
        [string]$Label
    )

    $captureScript = Join-Path (Get-Location) "tools\qa\collect-trailmate-device-evidence.ps1"
    if (-not (Test-Path -LiteralPath $captureScript)) {
        throw "Missing device evidence script: $captureScript"
    }

    $targetDir = Join-Path $script:EvidenceDir $Label
    $arguments = @(
        "-NoProfile",
        "-ExecutionPolicy",
        "Bypass",
        "-File",
        $captureScript,
        "-DeviceId",
        $script:ResolvedDeviceId,
        "-PackageName",
        $PackageName,
        "-OutputDir",
        $targetDir,
        "-AdbPath",
        $AdbPath
    )
    Invoke-ExternalTextCapture -FileName "$Label-capture.txt" -Command "powershell" -Arguments $arguments | Out-Null
}

$usingDefaultOutputDir = [string]::IsNullOrWhiteSpace($OutputDir)
$resolvedOutputDir = $OutputDir
if ($usingDefaultOutputDir) {
    $resolvedOutputDir = Join-Path "outputs\qa" ("amap-offline-download-" + (Get-Date -Format "yyyyMMdd-HHmmss"))
}
if (-not [System.IO.Path]::IsPathRooted($resolvedOutputDir)) {
    $resolvedOutputDir = Join-Path (Get-Location) $resolvedOutputDir
}

$script:EvidenceDir = $resolvedOutputDir
New-Item -ItemType Directory -Force -Path $script:EvidenceDir | Out-Null

if (-not [string]::IsNullOrWhiteSpace($JavaHome)) {
    $env:JAVA_HOME = $JavaHome
    $env:Path = "$JavaHome\bin;$env:Path"
}

if (-not [string]::IsNullOrWhiteSpace($AndroidHome)) {
    $env:ANDROID_HOME = $AndroidHome
    $env:ANDROID_SDK_ROOT = $AndroidHome
    $env:Path = "$AndroidHome\platform-tools;$env:Path"
} elseif ([string]::IsNullOrWhiteSpace($env:ANDROID_HOME)) {
    $localAndroidHome = Join-Path (Get-Location) ".android-sdk"
    if (Test-Path -LiteralPath $localAndroidHome) {
        $env:ANDROID_HOME = $localAndroidHome
        $env:ANDROID_SDK_ROOT = $localAndroidHome
        $env:Path = "$localAndroidHome\platform-tools;$env:Path"
    }
}

$script:ResolvedDeviceId = Resolve-DeviceId
$env:ANDROID_SERIAL = $script:ResolvedDeviceId

$manifest = @(
    "# TrailMate AMap Offline Download QA",
    "capturedAt=$(Get-Timestamp)",
    "deviceId=$script:ResolvedDeviceId",
    "packageName=$PackageName",
    "cityName=$CityName",
    "timeoutMs=$TimeoutMs",
    "outputDir=$script:EvidenceDir",
    "",
    "This script runs the opt-in AMap offline base-map download QA and captures before/after device evidence.",
    "It does not toggle airplane mode, grant permissions, or mark offline tile proof in TrailMate.",
    "Production readiness still requires visible offline base-map tiles while the phone network is disabled.",
    "AMap API keys and bare 32-character hex tokens are redacted from captured text."
)
Write-RedactedFile -Path (Join-Path $script:EvidenceDir "manifest.txt") -Lines $manifest

Invoke-ExternalTextCapture -FileName "adb-devices.txt" -Command $AdbPath -Arguments @("devices", "-l") | Out-Null
Invoke-DeviceEvidenceCapture -Label "before-download"

$gradleArguments = @(
    ":android-app:connectedDebugAndroidTest",
    "-Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.AmapOfflineBaseMapDownloadQaTest",
    "-Pandroid.testInstrumentationRunnerArguments.trailmateOfflineDownloadQa=true",
    "-Pandroid.testInstrumentationRunnerArguments.trailmateOfflineCityName=$CityName",
    "-Pandroid.testInstrumentationRunnerArguments.trailmateOfflineDownloadTimeoutMs=$TimeoutMs",
    "--no-daemon"
)
$downloadExitCode = Invoke-ExternalTextCapture -FileName "amap-offline-download-gradle.txt" -Command $GradlePath -Arguments $gradleArguments

Invoke-DeviceEvidenceCapture -Label "after-download"

$summary = @(
    "# TrailMate AMap Offline Download QA Summary",
    "capturedAt=$(Get-Timestamp)",
    "deviceId=$script:ResolvedDeviceId",
    "cityName=$CityName",
    "downloadExitCode=$downloadExitCode",
    "gradleOutput=amap-offline-download-gradle.txt",
    "beforeEvidence=before-download",
    "afterEvidence=after-download",
    "",
    "A zero downloadExitCode means the opt-in download QA assertions passed.",
    "It is not enough for outdoor production readiness by itself.",
    "Next required evidence: return to TrailMate, confirm the target offline base-map region covers the active route, enable airplane mode, verify visible base-map tiles, and record the in-app offline tile proof."
)
Write-RedactedFile -Path (Join-Path $script:EvidenceDir "summary.txt") -Lines $summary

Write-Host "TrailMate AMap offline download QA evidence saved to: $script:EvidenceDir"
exit $downloadExitCode
