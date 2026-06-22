param(
    [string]$DeviceId,
    [string]$OutputDir,
    [string]$PackageName = "com.trailmate.app",
    [string]$AdbPath = "adb"
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

function Write-EvidenceFile {
    param(
        [string]$FileName,
        [string[]]$Lines
    )

    $path = Join-Path $script:EvidenceDir $FileName
    Redact-SensitiveText $Lines | Set-Content -Path $path -Encoding UTF8
}

function Invoke-AdbTextCapture {
    param(
        [string]$FileName,
        [string[]]$Arguments,
        [switch]$NoDeviceSelector
    )

    $fullArgs = @()
    if (-not $NoDeviceSelector -and -not [string]::IsNullOrWhiteSpace($script:ResolvedDeviceId)) {
        $fullArgs += @("-s", $script:ResolvedDeviceId)
    }
    $fullArgs += $Arguments

    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add("# Captured: $(Get-Timestamp)")
    $lines.Add("# Command: $AdbPath $($fullArgs -join ' ')")
    $lines.Add("")

    try {
        $output = & $AdbPath @fullArgs 2>&1
        $exitCode = $LASTEXITCODE
        $lines.Add("# ExitCode: $exitCode")
        $lines.Add("")
        foreach ($entry in $output) {
            $lines.Add([string]$entry)
        }
    } catch {
        $lines.Add("# CaptureError: $($_.Exception.Message)")
    }

    Write-EvidenceFile -FileName $FileName -Lines $lines
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

$usingDefaultOutputDir = [string]::IsNullOrWhiteSpace($OutputDir)
$resolvedOutputDir = $OutputDir
if ($usingDefaultOutputDir) {
    $resolvedOutputDir = Join-Path "outputs\qa" ("physical-device-evidence-" + (Get-Date -Format "yyyyMMdd-HHmmss"))
}
if (-not [System.IO.Path]::IsPathRooted($resolvedOutputDir)) {
    $resolvedOutputDir = Join-Path (Get-Location) $resolvedOutputDir
}

$script:EvidenceDir = $resolvedOutputDir
try {
    New-Item -ItemType Directory -Force -Path $script:EvidenceDir | Out-Null
} catch {
    if (-not $usingDefaultOutputDir) {
        throw
    }

    $fallbackRoot = if ([string]::IsNullOrWhiteSpace($env:TEMP)) {
        [System.IO.Path]::GetTempPath()
    } else {
        $env:TEMP
    }
    $script:EvidenceDir = Join-Path $fallbackRoot ("trailmate-device-evidence-" + (Get-Date -Format "yyyyMMdd-HHmmss"))
    New-Item -ItemType Directory -Force -Path $script:EvidenceDir | Out-Null
    Write-Warning "Could not create the default outputs\\qa evidence directory. Falling back to: $script:EvidenceDir"
}

Invoke-AdbTextCapture -FileName "adb-devices.txt" -Arguments @("devices", "-l") -NoDeviceSelector

$script:ResolvedDeviceId = Resolve-DeviceId
Invoke-AdbTextCapture -FileName "adb-get-state.txt" -Arguments @("get-state")

$manifest = @(
    "# TrailMate Device Evidence Manifest",
    "capturedAt=$(Get-Timestamp)",
    "deviceId=$script:ResolvedDeviceId",
    "packageName=$PackageName",
    "outputDir=$script:EvidenceDir",
    "",
    "This script captures diagnostic state only. It does not change permissions, location settings, network state, or airplane mode.",
    "AMap API keys and bare 32-character hex tokens are redacted from captured text."
)
Write-EvidenceFile -FileName "manifest.txt" -Lines $manifest

Invoke-AdbTextCapture -FileName "device-props.txt" -Arguments @(
    "shell",
    "getprop"
)
Invoke-AdbTextCapture -FileName "package-path.txt" -Arguments @(
    "shell",
    "pm",
    "path",
    $PackageName
)
Invoke-AdbTextCapture -FileName "package-dumpsys-redacted.txt" -Arguments @(
    "shell",
    "dumpsys",
    "package",
    $PackageName
)
Invoke-AdbTextCapture -FileName "appops.txt" -Arguments @(
    "shell",
    "cmd",
    "appops",
    "get",
    $PackageName
)
Invoke-AdbTextCapture -FileName "location-mode.txt" -Arguments @(
    "shell",
    "settings",
    "get",
    "secure",
    "location_mode"
)
Invoke-AdbTextCapture -FileName "location-dumpsys.txt" -Arguments @(
    "shell",
    "dumpsys",
    "location"
)
Invoke-AdbTextCapture -FileName "connectivity-dumpsys.txt" -Arguments @(
    "shell",
    "dumpsys",
    "connectivity"
)
Invoke-AdbTextCapture -FileName "battery-dumpsys.txt" -Arguments @(
    "shell",
    "dumpsys",
    "battery"
)
Invoke-AdbTextCapture -FileName "activity-top.txt" -Arguments @(
    "shell",
    "dumpsys",
    "activity",
    "top"
)
Invoke-AdbTextCapture -FileName "app-services.txt" -Arguments @(
    "shell",
    "dumpsys",
    "activity",
    "services",
    $PackageName
)
$logArgs = @("-s", $script:ResolvedDeviceId, "logcat", "-d", "-t", "3000", "-v", "time")
$logLines = New-Object System.Collections.Generic.List[string]
$logLines.Add("# Captured: $(Get-Timestamp)")
$logLines.Add("# Command: $AdbPath $($logArgs -join ' ')")
$logLines.Add("")
try {
    $logOutput = & $AdbPath @logArgs 2>&1
    $logLines.Add("# ExitCode: $LASTEXITCODE")
    $logLines.Add("")
    foreach ($line in $logOutput) {
        $logLines.Add([string]$line)
    }
} catch {
    $logLines.Add("# CaptureError: $($_.Exception.Message)")
}
Write-EvidenceFile -FileName "logcat-last-3000.txt" -Lines $logLines

$filtered = @(
    $logLines |
        Where-Object {
            $_ -match '(?i)trailmate|com\.trailmate|amap|location|gps|foreground|service|crash|fatal|anr|exception'
        }
)
Write-EvidenceFile -FileName "logcat-trailmate-filtered.txt" -Lines $filtered

Write-Host "TrailMate device evidence saved to: $script:EvidenceDir"
