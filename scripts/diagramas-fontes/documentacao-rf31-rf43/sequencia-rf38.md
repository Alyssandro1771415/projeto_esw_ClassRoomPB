# Diagrama de Sequência — RF38

**Requisito:** O aluno deve poder consultar seu histórico acadêmico.

**Método principal:** `HistoricoAcademicoController.consultarMeuHistorico()`.

## Aluno consulta próprio histórico

```mermaid
sequenceDiagram
    actor Aluno
    participant CLI as ClassRoomCLI
    participant Auth as AutenticacaoController
    participant HistCtrl as HistoricoAcademicoController
    participant Hist as HistoricoAcademico

    Aluno ->> CLI: Login
    CLI ->> Auth: login(...)
    Auth -->> CLI: Usuario ALUNO

    Aluno ->> CLI: Opção 27 — Consultar meu histórico acadêmico
    CLI ->> HistCtrl: consultarMeuHistorico()
    HistCtrl ->> HistCtrl: validarAlunoAutenticado()
    HistCtrl ->> HistCtrl: filtrarPorAluno(idAluno)
    HistCtrl -->> CLI: List~HistoricoAcademico~
    loop Para cada registro
        CLI ->> Hist: ler período, disciplina, professor, média, frequência, situação
        CLI -->> Aluno: Exibe linha do histórico
    end
```

## Restrição de acesso

```mermaid
sequenceDiagram
    actor Usuario
    participant HistCtrl as HistoricoAcademicoController

    Usuario ->> HistCtrl: consultarMeuHistorico()
    alt perfil != ALUNO
        HistCtrl -->> Usuario: Erro — apenas alunos consultam o próprio histórico
    else aluno sem registros
        HistCtrl -->> Usuario: lista vazia
    else aluno com histórico
        HistCtrl -->> Usuario: List~HistoricoAcademico~
    end
```
