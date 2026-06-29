#!/usr/bin/env python3
"""Automatiza chamada (professor) e entrega o terminal na consulta de frequência (aluno)."""

from __future__ import annotations

import os
import pty
import select
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

# (trecho esperado na saída, resposta a enviar)
STEPS = [
    ("Opção:", "1"),
    ("Matrícula/e-mail:", "2026101"),
    ("Senha:", "123456"),
    ("Pressione Enter para continuar", ""),
    ("Opção:", "20"),
    ("ID da turma:", "c2c63a25-187c-4cce-8685-4ff530a45638"),
    ("Data da aula (AAAA-MM-DD):", "2026-06-29"),
    ("(P/F):", "P"),
    ("Pressione Enter para continuar", ""),
    ("Opção:", "3"),
    ("Pressione Enter para continuar", ""),
    ("Opção:", "1"),
    ("Matrícula/e-mail:", "2026008"),
    ("Senha:", "123456"),
    ("Pressione Enter para continuar", ""),
    ("Opção:", "22"),
]


def main() -> None:
    master, slave = pty.openpty()
    proc = subprocess.Popen(
        [
            "mvn",
            "-q",
            "-Dmaven.repo.local=.m2/repository",
            "exec:java",
            "-Dexec.mainClass=pb.classroom.Main",
        ],
        cwd=ROOT,
        stdin=slave,
        stdout=slave,
        stderr=slave,
    )
    os.close(slave)

    buffer = ""
    step = 0
    handoff = False

    try:
        while proc.poll() is None or select.select([master], [], [], 0)[0]:
            rlist, _, _ = select.select([master, sys.stdin], [], [], 0.15)

            if master in rlist:
                try:
                    chunk = os.read(master, 4096).decode(errors="replace")
                except OSError:
                    break
                if not chunk:
                    break
                sys.stdout.write(chunk)
                sys.stdout.flush()
                buffer = (buffer + chunk)[-6000:]

                if not handoff and step < len(STEPS):
                    marker, reply = STEPS[step]
                    if marker in buffer:
                        os.write(master, (reply + "\n").encode())
                        if marker == "Opção:" and reply == "22":
                            handoff = True
                            sys.stdout.write(
                                "\n>>> Aguardando você: digite o ID da turma na linha abaixo.\n"
                            )
                            sys.stdout.flush()
                        step += 1
                        buffer = ""

            if handoff and sys.stdin in rlist:
                data = os.read(sys.stdin.fileno(), 4096)
                if data:
                    os.write(master, data)

            if proc.poll() is not None and not select.select([master], [], [], 0)[0]:
                break
    except KeyboardInterrupt:
        proc.terminate()
    finally:
        if proc.poll() is None:
            proc.wait()


if __name__ == "__main__":
    main()
