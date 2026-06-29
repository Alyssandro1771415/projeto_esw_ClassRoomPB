# Diagrama de Sequência — RF23

**Requisito:** O sistema deve manter lista de espera por turma.

**Fluxo principal:** aluno solicita matrícula em turma lotada → matrícula registrada com status `EM_ESPERA` → coordenador/professor consulta a fila → aluno consulta posição → coordenador remove aluno da espera.

## Solicitar matrícula sem vaga (entrada na lista de espera)

```mermaid
sequenceDiagram
    actor Aluno
    participant CLI as ClassRoomCLI
    participant Auth as AutenticacaoController
    participant Mat as MatriculaController
    participant Repo as MatriculaRepository

    Aluno ->> CLI: Opção solicitar matrícula (idTurma)
    CLI ->> Auth: isAutenticado() / getUsuarioLogado()
    Auth -->> CLI: Aluno autenticado
    CLI ->> Mat: solicitarMatricula(idTurma)
    Mat ->> Mat: validarTurmaDisponivel()
    Mat ->> Mat: validarMatriculaDuplicada()
    Mat ->> Mat: validarPreRequisitos()
    Mat ->> Mat: validarChoqueHorario()
    Mat ->> Mat: possuiVagasDisponiveis() = false
    Mat ->> Mat: new Matricula(aluno, turma, EM_ESPERA)
    Mat -->> CLI: Matricula (EM_ESPERA)
    CLI ->> Repo: salvarMatriculas()
    CLI -->> Aluno: Matrícula registrada em lista de espera
```

## Coordenador consulta lista de espera

```mermaid
sequenceDiagram
    actor Coord as Coordenador
    participant CLI as ClassRoomCLI
    participant Auth as AutenticacaoController
    participant Mat as MatriculaController

    Coord ->> CLI: Opção 19 — Consultar lista de espera
    CLI ->> Auth: getUsuarioLogado()
    Auth -->> CLI: COORDENADOR
    CLI ->> Mat: consultarListaEsperaCompleta(idTurma)
    Mat ->> Mat: validarCoordenadorOuProfessorDaTurma()
    Mat ->> Mat: listarMatriculasEmEsperaOrdenadas()
    Mat -->> CLI: List~Matricula~ EM_ESPERA
    CLI -->> Coord: Exibe fila por turma
```

## Aluno consulta posição na fila

```mermaid
sequenceDiagram
    actor Aluno
    participant CLI as ClassRoomCLI
    participant Mat as MatriculaController

    Aluno ->> CLI: Opção 19 — Consultar posição na espera
    CLI ->> Mat: consultarPosicaoAluno(idTurma)
    Mat ->> Mat: percorrer matriculas EM_ESPERA (FIFO)
    Mat -->> CLI: posição (1..n) ou 0
    CLI -->> Aluno: Sua posição: Nª
```

## Coordenador remove aluno da lista de espera

```mermaid
sequenceDiagram
    actor Coord as Coordenador
    participant CLI as ClassRoomCLI
    participant Mat as MatriculaController
    participant Repo as MatriculaRepository

    Coord ->> CLI: Opção 20 — Remover da espera
    CLI ->> Mat: removerAlunoListaEspera(idTurma, idMatricula)
    Mat ->> Mat: validarCoordenadorAutenticado()
    Mat ->> Mat: validar status EM_ESPERA
    Mat ->> Mat: removerMatricula()
    Mat -->> CLI: Matricula removida
    CLI ->> Repo: salvarMatriculas()
    CLI -->> Coord: Aluno removido da lista de espera
```
