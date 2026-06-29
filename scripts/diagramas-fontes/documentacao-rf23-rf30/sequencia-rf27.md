# Diagrama de Sequência — RF27

**Requisito:** O professor deve poder registrar presença/falta dos alunos por aula.

**Método principal:** `PresencaController.registrarPresenca(String idTurma, LocalDate data, Map<String, Boolean> presencas)`.

## Registrar presença/falta em uma aula

```mermaid
sequenceDiagram
    actor Prof as Professor
    participant CLI as ClassRoomCLI
    participant Auth as AutenticacaoController
    participant Pres as PresencaController
    participant Repo as PresencaRepository

    Prof ->> CLI: Login
    CLI ->> Auth: login(...)
    Auth -->> CLI: Usuario PROFESSOR

    Prof ->> CLI: Opção 20 — Registrar presença/falta
    CLI ->> CLI: ler idTurma, data
    CLI ->> CLI: listar alunos matriculados (CONFIRMADA)
    loop Para cada aluno
        Prof ->> CLI: P (presente) ou F (falta)
    end
    CLI ->> Pres: registrarPresenca(idTurma, data, presencas)
    Pres ->> Pres: validarProfessorAutenticado()
    Pres ->> Pres: validarProfessorDaTurma()
    Pres ->> Pres: validarTurmaNaoCancelada()
    Pres ->> Pres: validarDataPresenca()
    loop Para cada aluno
        Pres ->> Pres: validarAlunoMatriculadoNaTurma()
        Pres ->> Pres: validarRegistroDuplicado()
        Pres ->> Pres: new RegistroPresenca(PRESENTE|FALTA)
    end
    Pres -->> CLI: List~RegistroPresenca~
    CLI ->> Repo: salvarPresencas()
    CLI -->> Prof: Presença registrada com sucesso
```

## Validações de regra de negócio

```mermaid
sequenceDiagram
    actor Prof as Professor
    participant Pres as PresencaController

    Prof ->> Pres: registrarPresenca(turma, data, mapa)

    alt professor de outra turma
        Pres -->> Prof: Erro — só professor responsável
    else data anterior ao início das aulas
        Pres -->> Prof: Erro — data inválida
    else data futura
        Pres -->> Prof: Erro — data futura
    else aluno não matriculado
        Pres -->> Prof: Erro — matrícula confirmada exigida
    else registro duplicado (mesma data)
        Pres -->> Prof: Erro — já existe registro
    else perfil não professor
        Pres -->> Prof: Erro — apenas professores
    end
```
