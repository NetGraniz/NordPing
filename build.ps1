param(
    [string]$ProxyPath = 'Z:\Minecraft Proxy'
)

$ErrorActionPreference = 'Stop'
$projectPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$sourcePath = Join-Path $projectPath 'src\main\java'
$resourcePath = Join-Path $projectPath 'src\main\resources'
$buildPath = Join-Path $projectPath 'build'
$classesPath = Join-Path $buildPath 'classes'
$outputPath = Join-Path $buildPath 'NordPing-1.0.0.jar'
$velocityJar = Join-Path $ProxyPath 'velocity.jar'
$javaPath = 'C:\Program Files\Java\jdk-25\bin'

if (-not (Test-Path -LiteralPath $velocityJar)) { throw "Velocity jar not found: $velocityJar" }
New-Item -ItemType Directory -Force -Path $classesPath | Out-Null
Get-ChildItem -LiteralPath $classesPath -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force
$sources = Get-ChildItem -LiteralPath $sourcePath -Recurse -Filter '*.java' | Select-Object -ExpandProperty FullName
& (Join-Path $javaPath 'javac.exe') --release 25 -encoding UTF-8 -classpath $velocityJar -d $classesPath $sources
if ($LASTEXITCODE -ne 0) { throw 'NordPing compilation failed.' }
Copy-Item -Path (Join-Path $resourcePath '*') -Destination $classesPath -Recurse -Force
if (Test-Path -LiteralPath $outputPath) { Remove-Item -LiteralPath $outputPath -Force }
Push-Location $classesPath
try {
    & (Join-Path $javaPath 'jar.exe') --create --file $outputPath .
    if ($LASTEXITCODE -ne 0) { throw 'NordPing packaging failed.' }
} finally {
    Pop-Location
}
Write-Output $outputPath
