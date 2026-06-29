# Diagrama de Sequência — RF22

**Requisito:** O aluno deve poder cancelar matrícula dentro do período permitido.

## Cancelamento permitido (antes do início das aulas)

```mermaid
sequenceDiagram
    actor Aluno
    participant CLI as ClassRoomCLI
    participant Mat as MatriculaController
    participant Repo as MatriculaRepository

    Aluno ->> CLI: Opção 15 — Cancelar matrícula
    CLI ->> Mat: cancelarMatricula(idMatricula)
    Mat ->> Mat: validarAlunoAutenticado()
    Mat ->> Mat: buscarMatriculaDoAluno()
    Mat ->> Mat: validarCancelamentoPermitido(turma)
    Note over Mat: data atual < dataInicioAulas
    Mat ->> Mat: removerMatricula()
    alt era CONFIRMADA
        Mat ->> Mat: processarChamadaAutomaticaListaEspera()
    end
    Mat -->> CLI: Matricula cancelada
    CLI ->> Repo: salvarMatriculas()
    CLI -->> Aluno: Cancelamento confirmado
```

## Cancelamento bloqueado (após início das aulas)

```mermaid
sequenceDiagram
    actor Aluno
    participant CLI as ClassRoomCLI
    participant Mat as MatriculaController

    Aluno ->> CLI: Opção 15 — Cancelar matrícula
    CLI ->> Mat: cancelarMatricula(idMatricula)
    Mat ->> Mat: validarCancelamentoPermitido(turma)
    Note over Mat: data atual >= dataInicioAulas
    Mat -->> CLI: IllegalArgumentException
    CLI -->> Aluno: Matrícula só pode ser cancelada antes do início das aulas
```
