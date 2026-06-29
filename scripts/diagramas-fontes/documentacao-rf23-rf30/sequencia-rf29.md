# Diagrama de Sequência — RF29

**Requisito:** O aluno deve poder consultar sua frequência por disciplina.

**Método principal:** `PresencaController.consultarMinhaFrequenciaPorDisciplina(String idDisciplina)` — agrega registros de todas as turmas confirmadas do aluno na mesma disciplina.

## Consulta de frequência agregada por disciplina

```mermaid
sequenceDiagram
    actor Aluno
    participant CLI as ClassRoomCLI
    participant Auth as AutenticacaoController
    participant Pres as PresencaController
    participant Freq as FrequenciaDisciplinaAluno

    Aluno ->> CLI: Login
    CLI ->> Auth: login(...)
    Auth -->> CLI: Usuario ALUNO

    Aluno ->> CLI: Opção 24 — Consultar minha frequência por disciplina
    CLI ->> CLI: ler idDisciplina
    CLI ->> Pres: consultarMinhaFrequenciaPorDisciplina(idDisciplina)
    Pres ->> Pres: validarAlunoAutenticado()
    Pres ->> Pres: validarAlunoMatriculadoNaDisciplina()
    Pres ->> Pres: calcularFrequenciaDisciplinaInterna(idAluno, idDisciplina)
    loop Registros de presença do aluno
        Pres ->> Pres: filtrar turmas da disciplina + matrícula confirmada
        Pres ->> Pres: totalAulas++, totalPresencas++ se PRESENTE
    end
    Pres ->> Freq: new FrequenciaDisciplinaAluno(aluno, disciplina, total, presencas)
    Freq ->> Freq: percentual = presencas/total × 100
    Pres -->> CLI: FrequenciaDisciplinaAluno
    CLI -->> Aluno: Exibe presenças, faltas e percentual da disciplina
```

## Listar frequências em todas as disciplinas confirmadas

```mermaid
sequenceDiagram
    actor Aluno
    participant CLI as ClassRoomCLI
    participant Pres as PresencaController

    Aluno ->> CLI: consultarMinhasFrequenciasPorDisciplina()
    CLI ->> Pres: consultarMinhasFrequenciasPorDisciplina()
    Pres ->> Pres: validarAlunoAutenticado()
    Pres ->> Pres: obter idsDisciplinas das matrículas CONFIRMADA
    loop Para cada disciplina
        Pres ->> Pres: calcularFrequenciaDisciplinaInterna()
    end
    Pres -->> CLI: List~FrequenciaDisciplinaAluno~
    CLI -->> Aluno: Frequência por disciplina matriculada
```

## Aluno sem matrícula confirmada na disciplina

```mermaid
sequenceDiagram
    actor Aluno
    participant Pres as PresencaController

    Aluno ->> Pres: consultarMinhaFrequenciaPorDisciplina(idDisciplina)
    Pres ->> Pres: validarAlunoMatriculadoNaDisciplina()
    Pres -->> Aluno: IllegalArgumentException
    Note over Aluno,Pres: RF29 exige matrícula confirmada na disciplina
```
