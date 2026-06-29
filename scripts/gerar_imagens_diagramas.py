#!/usr/bin/env python3
"""Gera PNG a partir dos diagramas Mermaid em scripts/diagramas-fontes/."""

from __future__ import annotations

import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FONTES = ROOT / "scripts" / "diagramas-fontes"
DIAGRAMAS = ROOT / "releases" / "diagramas"
MMDC = ROOT / "node_modules" / ".bin" / "mmdc"
CONFIG = ROOT / "scripts" / "mermaid-export-config.json"
CONFIG_LARGE = ROOT / "scripts" / "mermaid-export-config-large.json"

LARGE_DIAGRAMS = {"diagrama-casos-de-uso", "diagrama-classes"}

# md relativo a FONTES -> pasta de saída em releases/diagramas
OUTPUT_DIRS: dict[str, Path] = {
    "": DIAGRAMAS,
    "documentacao-rf15-rf22": DIAGRAMAS / "documentacao-rf15-rf22" / "Documentação Release 1",
    "documentacao-rf23-rf30": DIAGRAMAS / "documentacao-rf23-rf30" / "Documentação Release 2",
}


def extract_mermaid_blocks(md_path: Path) -> list[str]:
    text = md_path.read_text(encoding="utf-8")
    return re.findall(r"```mermaid\s*\n(.*?)```", text, re.DOTALL)


def export_options(stem: str) -> dict[str, int]:
    if stem in LARGE_DIAGRAMS:
        return {"width": 6400, "scale": 3}
    if stem.startswith("sequencia-"):
        return {"width": 2000, "scale": 2}
    return {"width": 1600, "scale": 2}


def output_dir_for(md_path: Path) -> Path:
    rel = md_path.relative_to(FONTES)
    key = rel.parent.as_posix() if rel.parent.as_posix() != "." else ""
    return OUTPUT_DIRS[key]


def convert_all() -> list[Path]:
    if not MMDC.exists():
        raise SystemExit(f"mmdc nao encontrado em {MMDC}. Execute: npm install @mermaid-js/mermaid-cli")

    generated: list[Path] = []
    temp_dir = FONTES / ".tmp"
    temp_dir.mkdir(exist_ok=True)

    md_files = sorted(FONTES.rglob("*.md"))
    if not md_files:
        raise SystemExit(f"Nenhum .md encontrado em {FONTES}")

    for md_file in md_files:
        if md_file.parent.name == ".tmp":
            continue
        blocks = extract_mermaid_blocks(md_file)
        if not blocks:
            continue

        stem = md_file.stem
        opts = export_options(stem)
        config = CONFIG_LARGE if stem in LARGE_DIAGRAMS else CONFIG
        out_dir = output_dir_for(md_file)
        out_dir.mkdir(parents=True, exist_ok=True)

        for index, block in enumerate(blocks, start=1):
            suffix = f"-{index}" if len(blocks) > 1 else ""
            mmd_path = temp_dir / f"{stem}{suffix}.mmd"
            out_path = out_dir / f"{stem}{suffix}.png"
            mmd_path.write_text(block.strip() + "\n", encoding="utf-8")
            cmd = [
                str(MMDC),
                "-i",
                str(mmd_path),
                "-o",
                str(out_path),
                "-e",
                "png",
                "-b",
                "white",
                "-w",
                str(opts["width"]),
                "-s",
                str(opts["scale"]),
            ]
            if config.exists():
                cmd.extend(["-c", str(config)])
            print(
                f"Gerando {out_path.relative_to(ROOT)} "
                f"(w={opts['width']}, scale={opts['scale']}) ..."
            )
            result = subprocess.run(cmd, capture_output=True, text=True)
            if result.returncode != 0:
                print(result.stderr or result.stdout, file=sys.stderr)
                raise SystemExit(f"Falha ao gerar {out_path}")
            generated.append(out_path)

    for f in temp_dir.glob("*.mmd"):
        f.unlink()
    if temp_dir.exists():
        temp_dir.rmdir()

    # Diagramas gerais não ficam nas pastas de RF
    for sub in (
        DIAGRAMAS / "documentacao-rf23-rf30" / "Documentação Release 2",
        DIAGRAMAS / "documentacao-rf15-rf22" / "Documentação Release 1",
    ):
        for name in ("diagrama-casos-de-uso.png", "diagrama-classes.png"):
            stale = sub / name
            if stale.exists():
                stale.unlink()
                print(f"Removido {stale.relative_to(ROOT)} (diagrama geral na raiz)")

    return generated


if __name__ == "__main__":
    files = convert_all()
    print(f"\n{len(files)} imagens geradas em {DIAGRAMAS}")
