# Diagrama de Sequência — RF36

**Requisito:** O sistema deve manter histórico das disciplinas cursadas pelo aluno.

**Método principal:** `NotaController.fecharTurma(String idTurma)` gera e persiste `HistoricoAcademico`.

## Geração do histórico no fechamento da turma

```mermaid
sequenceDiagram
    actor Coord as Coordenador
    participant CLI as ClassRoomCLI
    participant Nota as NotaController
    participant Hist as HistoricoAcademico
    participant Repo as HistoricoAcademicoRepository

    Coord ->> CLI: Opção 25 — Fechar turma
    CLI ->> Nota: fecharTurma(idTurma)
    Nota ->> Nota: validarCoordenadorAutenticado()
    Nota ->> Nota: validarTurmaNaoCancelada()
    alt turma já fechada
        Nota -->> CLI: Erro — turma já fechada
    else turma aberta
        loop Para cada aluno CONFIRMADA
            Nota ->> Nota: calcularResultado(turma, aluno)
            Nota ->> Hist: new HistoricoAcademico(...)
            Nota ->> Nota: historicos.add(historico)
        end
        Nota ->> Nota: turma.setFechada(true)
        Nota -->> CLI: List~HistoricoAcademico~
        CLI ->> Repo: salvarHistoricos()
        CLI -->> Coord: Histórico mantido para disciplinas cursadas
    end
```

## Persistência do histórico

```mermaid
sequenceDiagram
    participant CLI as ClassRoomCLI
    participant Repo as HistoricoAcademicoRepository
    participant JSON as armazenamento_interno.json

    CLI ->> Repo: salvarHistoricos(lista)
    Repo ->> JSON: montarDocumento(... historicos ...)
    JSON -->> Repo: arquivo atualizado
    Repo -->> CLI: OK
```
