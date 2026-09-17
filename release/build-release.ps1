<#
.SYNOPSIS
    Gera o pacote on-prem do Chronos Pulse (release\*.zip) a partir do repositório.
.DESCRIPTION
    Roda `mvn package`, copia o jar + lançadores + manuais para
    release\dist\chronos-pulse-<versão> e compacta em
    release\chronos-pulse-<versão>-onprem.zip.
.PARAMETER Maven
    Comando Maven (padrão: mvn).
.PARAMETER SkipTests
    Pula os testes do build.
#>
[CmdletBinding()]
param(
    [string]$Maven = "mvn",
    [switch]$SkipTests
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$release = $PSScriptRoot

$testFlag = if ($SkipTests) { "-DskipTests" } else { "" }
& $Maven -q -B -f "$root\pom.xml" package $testFlag
if ($LASTEXITCODE -ne 0) {
    throw "Falha no build Maven (exit $LASTEXITCODE)."
}

[xml]$pom = Get-Content -LiteralPath "$root\pom.xml"
$version = $pom.project.version

$jar = Get-ChildItem -LiteralPath "$root\target" -Filter *.jar |
    Where-Object { $_.Name -notmatch "original|sources|javadoc" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $jar) {
    throw "Nenhum jar encontrado em $root\target."
}

$dist = Join-Path $release "dist\chronos-pulse-$version"
if (Test-Path -LiteralPath $dist) {
    Remove-Item -LiteralPath $dist -Recurse -Force
}
New-Item -ItemType Directory -Path $dist -Force | Out-Null

Copy-Item -LiteralPath $jar.FullName -Destination (Join-Path $dist "chronos-pulse.jar")
foreach ($item in @("start.bat", "start.sh", "stop.bat", "stop.sh",
                    "docker-compose.yml", ".env.example", "README.md",
                    "chronos-pulse.service", "install-linux.sh")) {
    Copy-Item -LiteralPath (Join-Path $release $item) -Destination $dist
}

$zip = Join-Path $release "chronos-pulse-$version-onprem.zip"
if (Test-Path -LiteralPath $zip) {
    Remove-Item -LiteralPath $zip -Force
}
Compress-Archive -Path "$dist\*" -DestinationPath $zip -Force

Write-Host "Pacote gerado: $zip"
Write-Host "Conteudo: $dist"
Write-Host "Jar: $($jar.Name) ($([math]::Round($jar.Length / 1MB, 1)) MB)"