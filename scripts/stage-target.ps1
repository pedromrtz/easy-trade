[CmdletBinding()]
param(
	[Parameter(Mandatory = $true)]
	[string]$TargetVersion
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$matrixPath = Join-Path $repoRoot "gradle/minecraft-targets.json"
$matrix = Get-Content -LiteralPath $matrixPath -Raw | ConvertFrom-Json
$target = $matrix | Where-Object { $_.minecraft -eq $TargetVersion } | Select-Object -First 1
if (-not $target) {
	throw "Unsupported target '$TargetVersion'. See $matrixPath."
}

$instanceName = "MC $TargetVersion Fabric Smoke"
$instanceRoot = Join-Path $env:APPDATA "PrismLauncher/instances/$instanceName/minecraft"
$modsDir = Join-Path $instanceRoot "mods"
if (-not (Test-Path -LiteralPath $instanceRoot -PathType Container)) {
	throw "Prism instance not found; refusing to create or guess its path: $instanceRoot"
}

$gradle = Join-Path $repoRoot "gradlew.bat"
Push-Location $repoRoot
try {
	& $gradle clean build verifyTargetArtifact "-PmcTarget=$TargetVersion"
	if ($LASTEXITCODE -ne 0) {
		throw "Gradle build/verification failed for Minecraft $TargetVersion (exit $LASTEXITCODE)."
	}
} finally {
	Pop-Location
}

$archiveBase = if ($TargetVersion -eq "26.3") { "trade-assistant" } else { "trade-assistant-mc-$TargetVersion" }
$artifact = Get-ChildItem -LiteralPath (Join-Path $repoRoot "build/libs") -File |
	Where-Object { $_.Name -like "$archiveBase-*.jar" -and $_.Name -notmatch "-(sources|dev)\.jar$" } |
	Sort-Object LastWriteTime -Descending |
	Select-Object -First 1
if (-not $artifact) {
	throw "Could not locate the verified distributable for $TargetVersion in build/libs."
}

$artifactArchiveDir = Join-Path $repoRoot "target-artifacts/$TargetVersion"
New-Item -ItemType Directory -Path $artifactArchiveDir -Force | Out-Null
$archivedArtifact = Join-Path $artifactArchiveDir $artifact.Name
Copy-Item -LiteralPath $artifact.FullName -Destination $archivedArtifact -Force

$artifactStream = [System.IO.File]::OpenRead($artifact.FullName)
try {
	Add-Type -AssemblyName System.IO.Compression
	$archive = [System.IO.Compression.ZipArchive]::new($artifactStream, [System.IO.Compression.ZipArchiveMode]::Read)
	try {
		$metadataEntry = $archive.GetEntry("fabric.mod.json")
		if (-not $metadataEntry) { throw "No fabric.mod.json in $($artifact.Name)." }
		$reader = [System.IO.StreamReader]::new($metadataEntry.Open())
		try { $metadata = $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
	} finally {
		$archive.Dispose()
	}
} finally {
	$artifactStream.Dispose()
}

New-Item -ItemType Directory -Path $modsDir -Force | Out-Null
$stagedModName = "trade-assistant-1.0.0-mc-$TargetVersion.jar"
Get-ChildItem -LiteralPath $modsDir -File -Filter "*.jar" |
	Where-Object { $_.Name -match '^trade-assistant-.*\.jar$' } |
	Remove-Item -Force
Copy-Item -LiteralPath $archivedArtifact -Destination (Join-Path $modsDir $stagedModName) -Force

$apiVersion = $null
$apiFileName = $null
if ($metadata.depends.PSObject.Properties.Name -contains "fabric-api") {
	$apiVersion = if ($TargetVersion -eq "26.3") { "0.163.0+26.3" } else { $target.fabricApi }
	$apiFileName = "fabric-api-$apiVersion.jar"
	$apiDestination = Join-Path $modsDir $apiFileName
	$apiUrl = "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/$([uri]::EscapeDataString($apiVersion))/$apiFileName"
	$apiTemp = Join-Path ([System.IO.Path]::GetTempPath()) $apiFileName
	& curl.exe --fail --silent --show-error --location $apiUrl --output $apiTemp
	if ($LASTEXITCODE -ne 0) { throw "Could not download the matching Fabric API: $apiUrl" }
	if ((Get-Item -LiteralPath $apiTemp).Length -lt 1024) { throw "Downloaded Fabric API looks invalid: $apiUrl" }
	Get-ChildItem -LiteralPath $modsDir -File -Filter "fabric-api-*.jar" |
		Where-Object Name -ne $apiFileName |
		Remove-Item -Force
	Copy-Item -LiteralPath $apiTemp -Destination $apiDestination -Force
}

$resultsPath = Join-Path $repoRoot "docs/target-build-results.csv"
$result = [pscustomobject]@{
	Minecraft = $TargetVersion
	Java = $target.javaCompile
	FabricLoader = $target.fabricLoader
	FabricApi = $apiVersion
	Build = "Passed"
	ArtifactValidation = "Passed"
	Tests = "Passed (Gradle build)"
	Launch = "Not run"
	Artifact = $archivedArtifact
	PrismMods = $modsDir
	StagedMod = (Join-Path $modsDir $stagedModName)
	StagedFabricApi = if ($apiFileName) { Join-Path $modsDir $apiFileName } else { "Not required" }
	UpdatedUtc = [DateTime]::UtcNow.ToString("o")
}

$previousResults = @{}
if (Test-Path -LiteralPath $resultsPath) {
	foreach ($previous in Import-Csv -LiteralPath $resultsPath) { $previousResults[$previous.Minecraft] = $previous }
}
$results = foreach ($entry in $matrix) {
	if ($entry.minecraft -eq $TargetVersion) {
		if ($previousResults.ContainsKey($TargetVersion) -and $previousResults[$TargetVersion].Launch -ne "Not run") {
			$result.Launch = $previousResults[$TargetVersion].Launch
		}
		$result
	} elseif ($previousResults.ContainsKey($entry.minecraft)) {
		$previousResults[$entry.minecraft]
	} else {
		[pscustomobject]@{
			Minecraft = $entry.minecraft
			Java = $entry.javaCompile
			FabricLoader = $entry.fabricLoader
			FabricApi = $entry.fabricApi
			Build = "Not started"
			ArtifactValidation = "Not started"
			Tests = "Not run"
			Launch = "Not run"
			Artifact = ""
			PrismMods = Join-Path "$env:APPDATA\PrismLauncher\instances" "MC $($entry.minecraft) Fabric Smoke\minecraft\mods"
			StagedMod = ""
			StagedFabricApi = ""
			UpdatedUtc = ""
		}
	}
}
$results | Export-Csv -LiteralPath $resultsPath -NoTypeInformation -Encoding UTF8

Write-Host "Staged and recorded Minecraft ${TargetVersion}: $($result.StagedMod)"
