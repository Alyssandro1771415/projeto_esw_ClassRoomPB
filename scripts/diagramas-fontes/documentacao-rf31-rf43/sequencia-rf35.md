# Diagrama de Sequência — RF35

**Requisito:** O professor deve poder alterar notas antes do fechamento da turma.

**Métodos:** `NotaController.alterarNota(...)` e `NotaController.fecharTurma(...)` (coordenador).

## Alterar nota antes do fechamento

```mermaid
sequenceDiagram
    actor Prof as Professor
    participant CLI as ClassRoomCLI
    participant Nota as NotaController
    participant Repo as NotaRepository

    Prof ->> CLI: Opção 26 — Alterar nota
    CLI ->> CLI: selecionar turma, aluno e etapa
    CLI ->> CLI: ler nova nota
    CLI ->> Nota: alterarNota(idTurma, idAluno, etapa, valor)
    Nota ->> Nota: validarProfessorDaTurma()
    Nota ->> Nota: validarTurmaNaoFechada()
    Nota ->> Nota: validarNotaExistenteParaAlteracao()
    Nota ->> Nota: definirNota(etapa, valor)
    Nota -->> CLI: RegistroNota atualizado
    CLI ->> Repo: salvarNotas()
    CLI -->> Prof: Nota alterada com sucesso
```

## Fechar turma e bloquear alterações

```mermaid
sequenceDiagram
    actor Coord as Coordenador
    participant CLI as ClassRoomCLI
    participant Nota as NotaController
    participant Hist as HistoricoAcademico
    participant Turma as Turma

    Coord ->> CLI: Opção 25 — Fechar turma
    CLI ->> Nota: fecharTurma(idTurma)
    Nota ->> Nota: validarCoordenadorAutenticado()
    loop Para cada aluno CONFIRMADA
        Nota ->> Nota: calcularResultado(...)
        Nota ->> Hist: new HistoricoAcademico(...)
    end
    Nota ->> Turma: setFechada(true)
    Nota -->> CLI: List~HistoricoAcademico~
    CLI -->> Coord: Turma fechada / histórico gerado
```

## Bloqueio após fechamento

```mermaid
sequenceDiagram
    actor Prof as Professor
    participant Nota as NotaController
    participant Turma as Turma

    Prof ->> Nota: alterarNota(...) ou lancarNota(...)
    Nota ->> Turma: isFechada()?
    alt turma fechada
        Nota -->> Prof: Erro — turma fechada, notas não podem ser alteradas
    else turma aberta
        Nota -->> Prof: Nota atualizada
    end
```
