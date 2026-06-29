# Diagrama de Sequência — RF26

**Requisito:** O coordenador deve poder visualizar a lista de espera de cada turma.

**Método principal:** `MatriculaController.visualizarListaEsperaPorTurma(String idTurma)` — exclusivo para perfil **COORDENADOR**.

## Visualização da lista de espera por turma

```mermaid
sequenceDiagram
    actor Coord as Coordenador
    participant CLI as ClassRoomCLI
    participant Auth as AutenticacaoController
    participant Mat as MatriculaController

    Coord ->> CLI: Login (matrícula/e-mail + senha)
    CLI ->> Auth: login(identificador, senha)
    Auth -->> CLI: Usuario COORDENADOR

    Coord ->> CLI: Opção 19 — Consultar lista de espera
    CLI ->> CLI: ler idTurma
    CLI ->> Mat: visualizarListaEsperaPorTurma(idTurma)
    Mat ->> Mat: validarCoordenadorAutenticado()
    alt não é coordenador
        Mat -->> CLI: IllegalArgumentException
        CLI -->> Coord: Operação não permitida
    else coordenador autenticado
        Mat ->> Mat: consultarListaEsperaOrdenadaPorSolicitacao()
        Mat -->> CLI: List~Matricula~ EM_ESPERA (FIFO)
        alt lista vazia
            CLI -->> Coord: Nenhum aluno na lista de espera
        else lista com alunos
            CLI -->> Coord: Exibe posição, id, aluno e status
        end
    end
```

## Tentativa de acesso por outros perfis (rejeitada)

```mermaid
sequenceDiagram
    actor Aluno
    actor Prof as Professor
    participant Mat as MatriculaController

    Aluno ->> Mat: visualizarListaEsperaPorTurma(idTurma)
    Mat -->> Aluno: IllegalArgumentException

    Prof ->> Mat: visualizarListaEsperaPorTurma(idTurma)
    Mat -->> Prof: IllegalArgumentException

    Note over Mat: RF26 restringe visualização completa ao coordenador.<br/>Professor usa consultarListaEsperaCompleta (RF23).
```
