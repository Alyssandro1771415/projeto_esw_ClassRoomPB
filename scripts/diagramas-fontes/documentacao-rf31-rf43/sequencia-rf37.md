# Diagrama de Sequência — RF37

**Requisito:** O histórico deve registrar período, disciplina, professor, nota final, frequência e situação.

**Modelo:** `HistoricoAcademico` com campos obrigatórios do RF37.

## Campos registrados no histórico

```mermaid
sequenceDiagram
    participant Nota as NotaController
    participant Res as ResultadoAvaliacao
    participant Turma as Turma
    participant Hist as HistoricoAcademico

    Nota ->> Res: calcularResultado(turma, aluno)
    Res -->> Nota: mediaFinal, percentualFrequencia, situacao
    Nota ->> Turma: getIdPeriodoLetivo()
    Nota ->> Turma: getIdDisciplina()
    Nota ->> Turma: getIdProfessor()
    Nota ->> Hist: new HistoricoAcademico(...)
    Note over Hist: RF37 — período, disciplina, professor,<br/>nota final, frequência e situação
```

## Estrutura do registro

```mermaid
classDiagram
    class HistoricoAcademico {
        -String id
        -String idAluno
        -String idDisciplina
        -String idPeriodoLetivo
        -String idProfessor
        -String idTurma
        -Double mediaFinal
        -double percentualFrequencia
        -SituacaoAcademica situacao
        -LocalDate dataRegistro
        +getIdPeriodoLetivo()
        +getIdDisciplina()
        +getIdProfessor()
        +getMediaFinal()
        +getPercentualFrequencia()
        +getSituacao()
    }

    class SituacaoAcademica {
        <<enumeration>>
        APROVADO
        REPROVADO_NOTA
        REPROVADO_FALTA
        EM_RECUPERACAO
        EM_ANDAMENTO
    }

    HistoricoAcademico --> SituacaoAcademica
```
