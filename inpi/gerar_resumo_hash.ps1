<#
.SYNOPSIS
    Gera o resumo digital (hash SHA-256) do código-fonte para depósito no INPI.
.DESCRIPTION
    Produz um manifest determinístico (caminho relativo + SHA-256 por arquivo)
    e dois hashes finais: hash_manifesto (SHA-256 do manifest) e
    hash_concatenacao (SHA-256 da concatenação do conteúdo de todos os arquivos).
.PARAMETER Backend
    Raiz do repositório do backend (padrão: raiz acompanhando este script).
.PARAMETER App
    Raiz do repositório do app Flutter (padrão: C:\app\chronos_pulse_app).
.PARAMETER OutFile
    Caminho de saída do resumo (padrão: inpi\resumo_hash.txt).
#>
[CmdletBinding()]
param(
    [string]$Backend,
    [string]$App = "C:\app\chronos_pulse_app",
    [string]$OutFile
)

$ErrorActionPreference = "Stop"

if (-not $Backend) {
    $Backend = Split-Path -Parent $PSScriptRoot
}
if (-not $OutFile) {
    $OutFile = Join-Path $PSScriptRoot "resumo_hash.txt"
}

$roots = @(
    (Join-Path $Backend "src\main"),
    (Join-Path $Backend "src\test"),
    (Join-Path $App "lib"),
    (Join-Path $App "test")
)

$excludeDirs = @(
    ".git", "target", "build", ".dart_tool", ".idea", ".vscode",
    "node_modules", ".gradle", ".settings", "dist", "coverage",
    ".symlinks", "android\build", "ios\build"
)

function Get-Sha256Hex {
    param([byte[]]$Bytes)
    $sha = [System.Security.Cryptography.SHA256]::Create()
    try {
        $hash = $sha.ComputeHash($Bytes)
    } finally {
        $sha.Dispose()
    }
    -join ($hash | ForEach-Object { $_.ToString("x2") })
}

function Get-RelativePath {
    param([string]$Path, [string]$Root)
    return $Path.Substring($Root.Length).TrimStart("\", "/").Replace("\", "/")
}

$manifest = @()
foreach ($root in $roots) {
    if (-not (Test-Path -LiteralPath $root)) {
        Write-Warning "Diretório ignorado (não existe): $root"
        continue
    }
    $files = Get-ChildItem -LiteralPath $root -Recurse -File -ErrorAction SilentlyContinue
    foreach ($file in $files) {
        $full = $file.FullName
        if ($excludeDirs | Where-Object { $full -like "*\$_\*" }) {
            continue
        }
        $rel = Get-RelativePath -Path $full -Root $root
        if ($rel -match "(^|/)(\.class|\.classpath|\.project)$") {
            continue
        }
        if ($full -match "\.(class|jar|p7s)$") {
            continue
        }
        $hash = Get-Sha256Hex ([System.IO.File]::ReadAllBytes($full))
        $manifest += [pscustomobject]@{ Path = $rel; Hash = $hash; Abs = $full }
    }
}

$list = [System.Collections.Generic.List[pscustomobject]]([pscustomobject[]]$manifest)
$list.Sort([System.Comparison[pscustomobject]]{
    param($a, $b)
    [System.StringComparer]::OrdinalIgnoreCase.Compare($a.Path, $b.Path)
})
$manifest = $list

$lines = $manifest | ForEach-Object { "$($_.Path)`t$($_.Hash)" }
$manifestBytes = [System.Text.Encoding]::UTF8.GetBytes(($lines -join "`n"))
$hashManifest = Get-Sha256Hex $manifestBytes

$stream = [System.IO.MemoryStream]::new()
try {
    foreach ($entry in $manifest) {
        $data = [System.IO.File]::ReadAllBytes($entry.Abs)
        $stream.Write($data, 0, $data.Length)
    }
    $hashConcatenacao = Get-Sha256Hex $stream.ToArray()
} finally {
    $stream.Dispose()
}

$header = @(
    "Resumo digital de codigo-fonte - Chronos Pulse"
    "Gerado em: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss zzz')"
    "Algoritmo: SHA-256 (manifest por arquivo, ordenado por caminho relativo)"
    "Arquivos: $($manifest.Count)"
    "hash_manifesto=$hashManifest"
    "hash_concatenacao=$hashConcatenacao"
    "---"
)

$dir = Split-Path -Parent $OutFile
if (-not (Test-Path -LiteralPath $dir)) {
    New-Item -ItemType Directory -Path $dir | Out-Null
}
[System.IO.File]::WriteAllLines($OutFile, ($header + $lines), [System.Text.Encoding]::UTF8)

Write-Host "Arquivos computados: $($manifest.Count)"
Write-Host "hash_manifesto=$hashManifest"
Write-Host "hash_concatenacao=$hashConcatenacao"
Write-Host "Resumo salvo em: $OutFile"