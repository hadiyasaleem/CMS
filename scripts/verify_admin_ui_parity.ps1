param([switch]$Quiet)

$ErrorActionPreference = "Stop"
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$issues = [System.Collections.Generic.List[string]]::new()
$checks = 0

function Resolve-RepoPath([string]$relativePath) {
    Join-Path $repoRoot $relativePath
}

function Add-Issue([string]$message) {
    $script:issues.Add($message)
}

function Assert-Contains([string]$relativePath, [string]$pattern, [string]$description) {
    $script:checks++
    $content = Get-Content -Raw (Resolve-RepoPath $relativePath)
    if ($content -notmatch $pattern) {
        Add-Issue("$description ($relativePath)")
    }
}

function Assert-NotContains([string]$relativePath, [string]$pattern, [string]$description) {
    $script:checks++
    $content = Get-Content -Raw (Resolve-RepoPath $relativePath)
    if ($content -match $pattern) {
        Add-Issue("$description ($relativePath)")
    }
}

function Assert-SameFile([string]$leftRelativePath, [string]$rightRelativePath, [string]$description) {
    $script:checks++
    $left = Resolve-RepoPath $leftRelativePath
    $right = Resolve-RepoPath $rightRelativePath
    if (-not (Test-Path -LiteralPath $left)) {
        Add-Issue("Missing $description source: $leftRelativePath")
        return
    }
    if (-not (Test-Path -LiteralPath $right)) {
        Add-Issue("Missing $description counterpart: $rightRelativePath")
        return
    }
    if ((Get-FileHash -LiteralPath $left).Hash -ne (Get-FileHash -LiteralPath $right).Hash) {
        Add-Issue("$description differs: $leftRelativePath <> $rightRelativePath")
    }
}

$mobileComponentsRelative = "mobile-shared\src\main\java\com\mbd\cmscommon\ui\components"
$desktopComponentsRelative = "desktop-shared\src\main\kotlin\com\mbd\cmscommon\ui\components"
$mobileComponents = Resolve-RepoPath $mobileComponentsRelative
$desktopComponents = Resolve-RepoPath $desktopComponentsRelative

$adminComponentFiles = Get-ChildItem -File $mobileComponents -Filter "*.kt" | Where-Object {
    $_.Name -notlike "Teacher*" -and $_.Name -notlike "Student*" -and $_.Name -ne "FileUploadPicker.kt"
}

foreach ($mobileFile in $adminComponentFiles) {
    Assert-SameFile "$mobileComponentsRelative\$($mobileFile.Name)" "$desktopComponentsRelative\$($mobileFile.Name)" "Admin shared component"
}

$desktopAdminComponentFiles = Get-ChildItem -File $desktopComponents -Filter "*.kt" | Where-Object {
    $_.Name -notlike "Teacher*" -and $_.Name -notlike "Student*"
}
foreach ($desktopFile in $desktopAdminComponentFiles) {
    if (-not (Test-Path -LiteralPath (Join-Path $mobileComponents $desktopFile.Name))) {
        Add-Issue("Desktop-only Admin shared component: $($desktopFile.Name)")
    }
}

foreach ($themeFile in @("Color.kt", "CmsColors.kt", "CollegeInfo.kt", "Shape.kt", "Theme.kt")) {
    Assert-SameFile "mobile-shared\src\main\java\com\mbd\cmscommon\ui\theme\$themeFile" "desktop-shared\src\main\kotlin\com\mbd\cmscommon\ui\theme\$themeFile" "Admin theme file"
}

foreach ($fontName in @("archivo_regular.ttf", "archivo_medium.ttf", "archivo_semibold.ttf", "archivo_bold.ttf", "archivo_extrabold.ttf")) {
    Assert-SameFile "mobile-shared\src\main\res\font\$fontName" "desktop-shared\src\main\resources\fonts\$fontName" "Archivo font asset"
}

$assetPairs = @{
    "admin_dashboard_hero.png" = "admin-dashboard-hero.png"
    "admin_people_hero.jpg" = "admin-people-hero.jpg"
    "admin_records_hero.jpg" = "admin-records-hero.jpg"
    "admin_more_hero.jpg" = "admin-more-hero.jpg"
    "departments_hero.png" = "departments-hero.png"
    "splash_postgraduate_block.jpg" = "splash_postgraduate_block.jpg"
    "splash_app_logo.png" = "splash_app_logo.png"
}
foreach ($mobileAsset in $assetPairs.Keys) {
    Assert-SameFile "mobile-admin\src\main\res\drawable-nodpi\$mobileAsset" "desktop-admin\src\main\resources\$($assetPairs[$mobileAsset])" "Admin image asset"
}

$desktopNavHost = "desktop-shared\src\main\kotlin\com\mbd\cmsdesktop\ui\admin\AdminNavHost.kt"
Assert-Contains $desktopNavHost "NavigationBar\(" "Desktop Admin must use mobile-style bottom navigation"
Assert-NotContains $desktopNavHost "NavigationRail\(" "Desktop Admin must not use a navigation rail"
Assert-Contains $desktopNavHost 'title\s*=\s*"GGC-MBD"' "Desktop Admin top bar title must match mobile"
Assert-Contains $desktopNavHost "RecordsDestination\.FEES\s*->\s*push\(AdminScreen\.FeesPicker\)" "Desktop Fee Structures action must navigate"
Assert-Contains $desktopNavHost "AdminScreen\.FeesPicker\s*->\s*DepartmentsScreen\(" "Desktop Fee Structures picker screen must exist"
Assert-Contains $desktopNavHost "RefreshBox\(" "Desktop Admin shell must expose the same refresh surface"

$desktopMain = "desktop-admin\src\main\kotlin\com\mbd\cmsdesktopadmin\Main.kt"
Assert-Contains $desktopMain "width\(ADMIN_MOBILE_CANVAS_WIDTH\)" "Desktop Admin must render in the centered mobile canvas"
Assert-Contains $desktopMain "ADMIN_MOBILE_CANVAS_WIDTH\s*=\s*412\.dp" "Desktop Admin reference canvas must remain 412dp wide"

Assert-Contains "mobile-shared\src\main\java\com\mbd\cmscommon\ui\components\CardGrid.kt" "const val CardGridColumns\s*=\s*2" "Mobile Admin card grids must use two columns"
Assert-Contains "desktop-shared\src\main\kotlin\com\mbd\cmscommon\ui\components\CardGrid.kt" "const val CardGridColumns\s*=\s*2" "Desktop Admin card grids must use two columns"

foreach ($tabLabel in @("Dashboard", "Academics", "People", "Records", "More")) {
    $quotedLabel = [regex]::Escape('"' + $tabLabel + '"')
    Assert-Contains "mobile-admin\src\main\java\com\mbd\cmsadmin\navigation\AdminDestinations.kt" $quotedLabel "Mobile Admin tab is missing: $tabLabel"
    Assert-Contains "desktop-shared\src\main\kotlin\com\mbd\cmsdesktop\ui\admin\AdminTab.kt" $quotedLabel "Desktop Admin tab is missing: $tabLabel"
}

foreach ($loginText in @(
    "Security Portal",
    "Admin login",
    "Central console — enrolment, faculty, attendance, examinations & records.",
    "GGC-MBD - ADMIN PORTAL",
    "Email Address",
    "admin@ggcmbd.edu.pk",
    "Admin accounts are created by another administrator. Self-registration isn't available."
)) {
    $quotedLoginText = [regex]::Escape('"' + $loginText + '"')
    Assert-Contains "mobile-admin\src\main\java\com\mbd\cmsadmin\feature\auth\LoginScreen.kt" $quotedLoginText "Mobile Admin login copy is missing"
    Assert-Contains "desktop-admin\src\main\kotlin\com\mbd\cmsdesktopadmin\Main.kt" $quotedLoginText "Desktop Admin login copy differs"
}

foreach ($attendanceFile in @(
    "mobile-admin\src\main\java\com\mbd\cmsadmin\feature\records\AttendanceRecordsScreen.kt",
    "desktop-shared\src\main\kotlin\com\mbd\cmsdesktop\ui\admin\AttendanceRecordsScreen.kt"
)) {
    Assert-Contains $attendanceFile 'Text\("Export"\)' "Attendance report must expose the same Export action"
    Assert-Contains $attendanceFile 'Text\("Choose a format\."' "Attendance export dialog copy must match"
    Assert-Contains $attendanceFile "private val NAME_W\s*=\s*108\.dp" "Attendance name-column width must match"
    Assert-Contains $attendanceFile "private val TOT_W\s*=\s*38\.dp" "Attendance total-column width must match"
}

if ($issues.Count -gt 0) {
    Write-Host "ADMIN UI PARITY: FAILED" -ForegroundColor Red
    foreach ($issue in $issues) {
        Write-Host " - $issue"
    }
    exit 1
}

if (-not $Quiet) {
    Write-Host "ADMIN UI PARITY: PASSED" -ForegroundColor Green
    Write-Host "Checks: $checks"
    Write-Host "Admin components: $($adminComponentFiles.Count)"
    Write-Host "Theme files: 5"
    Write-Host "Fonts: 5"
    Write-Host "Admin image assets: $($assetPairs.Count)"
}
