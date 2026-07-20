#!/usr/bin/env python3
"""Gera PNG via mermaid.ink a partir dos Mermaid em scripts/diagramas-fontes/."""

from __future__ import annotations

import base64
import re
import sys
import urllib.error
import urllib.request
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FONTES = ROOT / "scripts" / "diagramas-fontes"
DIAGRAMAS = ROOT / "releases" / "diagramas"

OUTPUT_DIRS: dict[str, Path] = {
    "": DIAGRAMAS,
    "documentacao-rf15-rf22": DIAGRAMAS / "documentacao-rf15-rf22",
    "documentacao-rf23-rf30": DIAGRAMAS / "documentacao-rf23-rf30",
    "documentacao-rf31-rf43": DIAGRAMAS / "documentacao-rf31-rf43",
}


def extract_mermaid_blocks(md_path: Path) -> list[str]:
    text = md_path.read_text(encoding="utf-8")
    return re.findall(r"```mermaid\s*\n(.*?)```", text, re.DOTALL)


def output_dir_for(md_path: Path) -> Path:
    rel = md_path.relative_to(FONTES)
    key = rel.parent.as_posix() if rel.parent.as_posix() != "." else ""
    return OUTPUT_DIRS[key]


def encode_mermaid_plain(source: str) -> str:
    return base64.urlsafe_b64encode(source.strip().encode("utf-8")).decode("ascii").rstrip("=")


def encode_mermaid_pako(source: str) -> str:
    compressed = zlib.compress(source.strip().encode("utf-8"), 9)
    encoded = base64.urlsafe_b64encode(compressed).decode("ascii")
    return f"pako:{encoded}"


def fetch_diagram(encoded: str) -> bytes:
    url = f"https://mermaid.ink/img/{encoded}?type=png&bgColor=!white"
    req = urllib.request.Request(
        url,
        headers={"User-Agent": "ClassRoomPB-diagram-generator/1.0"},
    )
    with urllib.request.urlopen(req, timeout=120) as resp:
        return resp.read()


def jpeg_to_png(jpeg_bytes: bytes, out_path: Path) -> None:
    try:
        from PIL import Image
        import io

        img = Image.open(io.BytesIO(jpeg_bytes)).convert("RGB")
        img.save(out_path, format="PNG")
        return
    except Exception:
        pass

    import subprocess
    import tempfile

    with tempfile.NamedTemporaryFile(suffix=".jpg", delete=False) as tmp:
        tmp.write(jpeg_bytes)
        tmp_path = Path(tmp.name)
    try:
        result = subprocess.run(
            ["convert", str(tmp_path), str(out_path)],
            capture_output=True,
            text=True,
        )
        if result.returncode != 0:
            raise SystemExit(f"Falha convert: {result.stderr}")
    finally:
        tmp_path.unlink(missing_ok=True)


def render_png(mermaid: str, out_path: Path) -> None:
    errors: list[str] = []
    data = None
    for encoder in (encode_mermaid_plain, encode_mermaid_pako):
        try:
            data = fetch_diagram(encoder(mermaid))
            break
        except urllib.error.HTTPError as exc:
            body = exc.read().decode("utf-8", errors="replace")[:200]
            errors.append(f"{encoder.__name__}: HTTP {exc.code} {body}")
            continue
    if data is None:
        raise SystemExit(f"Falha ao gerar {out_path}: {'; '.join(errors)}")

    if data[:8] == b"\x89PNG\r\n\x1a\n":
        out_path.write_bytes(data)
    elif data[:2] == b"\xff\xd8":
        jpeg_to_png(data, out_path)
    else:
        out_path.write_bytes(data)
        if out_path.read_bytes()[:8] != b"\x89PNG\r\n\x1a\n":
            raise SystemExit(f"Formato inesperado ao gerar {out_path}")


def convert_selected(patterns: list[str] | None = None) -> list[Path]:
    generated: list[Path] = []
    md_files = sorted(FONTES.rglob("*.md"))

    for md_file in md_files:
        if md_file.parent.name == ".tmp":
            continue
        rel = str(md_file.relative_to(FONTES))
        if patterns and not any(p in rel for p in patterns):
            continue

        blocks = extract_mermaid_blocks(md_file)
        if not blocks:
            continue

        stem = md_file.stem
        out_dir = output_dir_for(md_file)
        out_dir.mkdir(parents=True, exist_ok=True)

        for index, block in enumerate(blocks, start=1):
            suffix = f"-{index}" if len(blocks) > 1 else ""
            out_path = out_dir / f"{stem}{suffix}.png"
            print(f"Gerando {out_path.relative_to(ROOT)} ...")
            try:
                render_png(block, out_path)
            except urllib.error.HTTPError as exc:
                detail = exc.read().decode("utf-8", errors="replace")
                raise SystemExit(f"Falha HTTP {exc.code} em {out_path}: {detail}") from exc
            print(f"  OK ({out_path.stat().st_size} bytes)")
            generated.append(out_path)

    return generated


if __name__ == "__main__":
    patterns = sys.argv[1:] or [
        "documentacao-rf31-rf43",
        "diagrama-casos-de-uso.md",
        "diagrama-classes.md",
    ]
    files = convert_selected(patterns)
    print(f"\n{len(files)} imagens geradas em {DIAGRAMAS}")
