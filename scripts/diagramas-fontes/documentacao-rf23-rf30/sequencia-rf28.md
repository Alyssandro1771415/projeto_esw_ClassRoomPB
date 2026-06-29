# Diagrama de Sequência — RF28

**Requisito:** O sistema deve calcular automaticamente o percentual de frequência.

**Fórmula:** `percentual = (totalPresencas / totalAulasRegistradas) × 100`, encapsulada em `FrequenciaAluno`.

## Cálculo automático por turma (professor/coordenador)

```mermaid
sequenceDiagram
    actor Prof as Professor / Coordenador
    participant CLI as ClassRoomCLI
    participant Pres as PresencaController
    participant Freq as FrequenciaAluno

    Prof ->> CLI: Opção 23 — Consultar percentual de frequência
    CLI ->> CLI: ler idTurma
    CLI ->> Pres: calcularFrequenciaPorTurma(idTurma)
    Pres ->> Pres: validarCoordenadorOuProfessorDaTurma()
    Pres ->> Pres: listarAlunosConfirmadosNaTurma()
    loop Para cada aluno matriculado
        Pres ->> Pres: calcularFrequenciaInterna(idTurma, idAluno)
        Pres ->> Pres: contar registros PRESENTE/FALTA
        Pres ->> Freq: new FrequenciaAluno(aluno, turma, total, presencas)
        Freq ->> Freq: percentual = presencas/total × 100
    end
    Pres -->> CLI: List~FrequenciaAluno~
    CLI -->> Prof: Exibe percentual por aluno
```

## Aluno consulta própria frequência na turma

```mermaid
sequenceDiagram
    actor Aluno
    participant CLI as ClassRoomCLI
    participant Pres as PresencaController
    participant Freq as FrequenciaAluno

    Aluno ->> CLI: Opção 22 — Consultar meu percentual
    CLI ->> CLI: ler idTurma
    CLI ->> Pres: consultarMinhaFrequencia(idTurma)
    Pres ->> Pres: validarAlunoAutenticado()
    Pres ->> Pres: calcularFrequenciaInterna(turma, idAluno)
    Pres ->> Freq: new FrequenciaAluno(...)
    Freq -->> Pres: percentual calculado
    Pres -->> CLI: FrequenciaAluno
    CLI -->> Aluno: Presenças, total e frequência %
```

## Sem registros → 0%

```mermaid
sequenceDiagram
    participant Pres as PresencaController
    participant Freq as FrequenciaAluno

    Pres ->> Pres: calcularFrequenciaInterna(turma, aluno)
    Note over Pres: nenhum RegistroPresenca encontrado
    Pres ->> Freq: new FrequenciaAluno(aluno, turma, 0, 0)
    Freq -->> Pres: percentual = 0.0
```
