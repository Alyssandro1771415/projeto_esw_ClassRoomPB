# Diagrama de Sequência — RF39

**Requisito:** O coordenador deve poder consultar o histórico dos alunos do curso.

**Métodos:** `consultarHistoricoAluno(String idAluno)` e `consultarHistoricoPorCurso(String idCurso)`.

## Coordenador consulta histórico de um aluno

```mermaid
sequenceDiagram
    actor Coord as Coordenador
    participant CLI as ClassRoomCLI
    participant HistCtrl as HistoricoAcademicoController

    Coord ->> CLI: Opção 27 — Consultar histórico de aluno
    CLI ->> CLI: ler idAluno
    CLI ->> HistCtrl: consultarHistoricoAluno(idAluno)
    HistCtrl ->> HistCtrl: validarCoordenadorAutenticado()
    HistCtrl ->> HistCtrl: validarAlunoExistente()
    HistCtrl ->> HistCtrl: filtrarPorAluno(idAluno)
    HistCtrl -->> CLI: List~HistoricoAcademico~
    CLI -->> Coord: Exibe histórico do aluno
```

## Coordenador consulta histórico por curso

```mermaid
sequenceDiagram
    actor Coord as Coordenador
    participant CLI as ClassRoomCLI
    participant HistCtrl as HistoricoAcademicoController
    participant Disc as Disciplina

    Coord ->> CLI: Opção 28 — Consultar histórico por curso
    CLI ->> CLI: selecionar curso
    CLI ->> HistCtrl: consultarHistoricoPorCurso(idCurso)
    HistCtrl ->> HistCtrl: validarCoordenadorAutenticado()
    loop Para cada HistoricoAcademico
        HistCtrl ->> Disc: buscarDisciplinaPorId()
        alt disciplina.idCurso == idCurso
            HistCtrl ->> HistCtrl: incluir no resultado
        end
    end
    HistCtrl -->> CLI: List~HistoricoAcademico~
    CLI -->> Coord: Exibe histórico dos alunos do curso
```

## Restrição de perfil

```mermaid
sequenceDiagram
    actor Usuario
    participant HistCtrl as HistoricoAcademicoController

    Usuario ->> HistCtrl: consultarHistoricoAluno(...) / consultarHistoricoPorCurso(...)
    alt perfil != COORDENADOR
        HistCtrl -->> Usuario: Erro — apenas coordenadores
    else coordenador autenticado
        HistCtrl -->> Usuario: List~HistoricoAcademico~
    end
```
