#!/usr/bin/env bash
# Gera o resumo digital (hash SHA-256) do código-fonte para depósito no INPI.
# Mesmo algoritmo de inpi/gerar_resumo_hash.ps1 (resultados reproduzíveis).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND="${BACKEND:-$(cd "$SCRIPT_DIR/.." && pwd)}"
BASE="$(cd "$(dirname "$BACKEND")" && pwd)"
APP="${APP:-$BASE/chronos_pulse_app}"
OUT="${OUT:-$SCRIPT_DIR/resumo_hash.txt}"

roots=("$BACKEND/src/main" "$BACKEND/src/test" "$APP/lib" "$APP/test")

rows=()
for root in "${roots[@]}"; do
  if [ ! -d "$root" ]; then
    echo "IGNORADO (não existe): $root" >&2
    continue
  fi
  while IFS= read -r -d '' full; do
    case "$full" in
      */.git/*|*/target/*|*/build/*|*/.dart_tool/*|*/.idea/*|*/.vscode/*|*/node_modules/*|*/.gradle/*|*/.settings/*|*/dist/*|*/coverage/*|*/.symlinks/*|*/android/build/*|*/ios/build/*)
        continue ;;
    esac
    case "$full" in
      *.class|*.jar|*.p7s) continue ;;
    esac
    rel="${full#"$root"/}"
    hash="$(sha256sum "$full" | cut -d' ' -f1)"
    rows+=("$rel"$'\t'"$hash"$'\t'"$full")
  done < <(find "$root" -type f -print0)
done

sorted="$(printf '%s\n' "${rows[@]}" | LC_ALL=C sort -f)"

lines="$(printf '%s' "$sorted" | cut -f1-2)"

# hash_manifesto: SHA-256 das linhas do manifest unidas por \n (sem \n final).
manifest_bytes="$(IFS=$'\n'; printf '%s' "$lines")"
hash_manifest="$(printf '%s' "$manifest_bytes" | sha256sum | cut -d' ' -f1)"

# hash_concatenacao: SHA-256 do conteúdo bruto de todos os arquivos, na ordem do manifest.
content_file="$(mktemp)"
(printf '%s\n' "$sorted" | cut -f3 | while IFS= read -r f; do
  [ -n "$f" ] && [ -f "$f" ] && cat "$f"
done) > "$content_file"
hash_concat="$(sha256sum "$content_file" | cut -d' ' -f1)"
rm -f "$content_file"

count="$(printf '%s\n' "$sorted" | sed '/^$/d' | wc -l)"
{
  printf 'Resumo digital de codigo-fonte - Chronos Pulse\n'
  printf 'Gerado em: %s\n' "$(date '+%Y-%m-%d %H:%M:%S %z')"
  printf 'Algoritmo: SHA-256 (manifest por arquivo, ordenado por caminho relativo)\n'
  printf 'Arquivos: %s\n' "$count"
  printf 'hash_manifesto=%s\n' "$hash_manifest"
  printf 'hash_concatenacao=%s\n' "$hash_concat"
  printf -- '---\n'
  printf '%s\n' "$lines"
} > "$OUT"

echo "Arquivos computados: $count"
echo "hash_manifesto=$hash_manifest"
echo "hash_concatenacao=$hash_concat"
echo "Resumo salvo em: $OUT"