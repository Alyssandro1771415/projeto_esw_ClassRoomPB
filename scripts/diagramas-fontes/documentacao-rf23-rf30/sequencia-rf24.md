# Diagrama de Sequência — RF24

**Requisito:** Quando uma vaga for liberada, o sistema deve chamar automaticamente o próximo aluno da lista.

**Gatilhos:** cancelamento de matrícula confirmada; aumento do limite de vagas na alteração de turma; chamada manual pelo coordenador (opção 22).

## Cancelamento libera vaga e promove próximo aluno

```mermaid
sequenceDiagram
    actor Aluno
    participant CLI as ClassRoomCLI
    participant Mat as MatriculaController
    participant Repo as MatriculaRepository

    Aluno ->> CLI: Opção cancelar matrícula
    CLI ->> Mat: cancelarMatricula(idMatricula)
    Mat ->> Mat: validarCancelamentoPermitido()
    Mat ->> Mat: removerMatricula() [era CONFIRMADA]
    Mat ->> Mat: processarChamadaAutomaticaListaEspera(idTurma)
    loop Enquanto houver vaga
        Mat ->> Mat: promoverProximoAlunoElegivel()
        Mat ->> Mat: alunoPodeSerPromovido() ?
        alt elegível
            Mat ->> Mat: setStatus(CONFIRMADA)
        else inelegível
            Mat ->> Mat: pula para próximo da fila
        end
    end
    Mat -->> CLI: Matricula cancelada + promovidos
    CLI ->> Repo: salvarMatriculas()
    CLI -->> Aluno: Cancelamento confirmado
    Note over CLI,Aluno: RF24 — próximo da espera promovido automaticamente
```

## Aumento de vagas na alteração de turma

```mermaid
sequenceDiagram
    actor Coord as Coordenador
    participant CLI as ClassRoomCLI
    participant Turma as TurmaController
    participant Mat as MatriculaController
    participant RepoT as TurmaRepository
    participant RepoM as MatriculaRepository

    Coord ->> CLI: Opção alterar turma (novo limiteVagas)
    CLI ->> Turma: alterarTurma(...)
    Turma -->> CLI: Turma atualizada
    alt limiteVagas aumentou
        CLI ->> Mat: processarChamadaAutomaticaListaEspera(idTurma)
        Mat ->> Mat: chamarProximosAlunosListaEspera()
        Mat -->> CLI: List~Matricula~ promovidos
        CLI -->> Coord: RF24: N aluno(s) chamado(s) automaticamente
    end
    CLI ->> RepoT: salvarTurmas()
    CLI ->> RepoM: salvarMatriculas()
```

## Chamada manual pelo coordenador

```mermaid
sequenceDiagram
    actor Coord as Coordenador
    participant CLI as ClassRoomCLI
    participant Mat as MatriculaController
    participant Repo as MatriculaRepository

    Coord ->> CLI: Opção 22 — Chamar próximos da espera
    CLI ->> Mat: chamarProximosAlunosListaEsperaManualmente(idTurma)
    Mat ->> Mat: validarCoordenadorAutenticado()
    Mat ->> Mat: possuiVagasDisponiveis()
    Mat ->> Mat: chamarProximosAlunosListaEspera()
    Mat -->> CLI: List~Matricula~ promovidos
    CLI ->> Repo: salvarMatriculas()
    CLI -->> Coord: Alunos promovidos com sucesso
```
