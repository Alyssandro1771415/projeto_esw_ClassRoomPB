# Diagrama de Sequência — RF31

**Requisito:** O professor deve poder lançar notas das etapas (etapa1 e etapa2).

**Método principal:** `NotaController.lancarNota(String idTurma, String idAluno, EtapaAvaliacao etapa, double valor)`.

## Lançar notas das etapas

```mermaid
sequenceDiagram
    actor Prof as Professor
    participant CLI as ClassRoomCLI
    participant Auth as AutenticacaoController
    participant Nota as NotaController
    participant Repo as NotaRepository

    Prof ->> CLI: Login
    CLI ->> Auth: login(...)
    Auth -->> CLI: Usuario PROFESSOR

    Prof ->> CLI: Opção 25 — Lançar notas (etapa1/etapa2)
    CLI ->> CLI: selecionar turma do professor
    CLI ->> CLI: ler etapa (1 ou 2)
    loop Para cada aluno CONFIRMADA
        Prof ->> CLI: informar nota (0.0 a 10.0)
        CLI ->> Nota: lancarNota(idTurma, idAluno, etapa, valor)
        Nota ->> Nota: validarProfessorAutenticado()
        Nota ->> Nota: validarProfessorDaTurma()
        Nota ->> Nota: validarTurmaNaoCancelada()
        Nota ->> Nota: validarTurmaNaoFechada()
        Nota ->> Nota: validarAlunoMatriculadoNaTurma()
        Nota ->> Nota: buscarOuCriarNota()
        Nota ->> Nota: definirNota(etapa, valor)
        Nota -->> CLI: RegistroNota
    end
    CLI ->> Repo: salvarNotas()
    CLI -->> Prof: Notas lançadas com sucesso
```

## Validações de regra de negócio

```mermaid
sequenceDiagram
    actor Prof as Professor
    participant Nota as NotaController

    Prof ->> Nota: lancarNota(turma, aluno, etapa, valor)

    alt perfil não professor
        Nota -->> Prof: Erro — apenas professores
    else professor de outra turma
        Nota -->> Prof: Erro — só professor responsável
    else turma cancelada
        Nota -->> Prof: Erro — turma cancelada
    else turma fechada
        Nota -->> Prof: Erro — notas bloqueadas (RF35)
    else aluno sem matrícula CONFIRMADA
        Nota -->> Prof: Erro — matrícula confirmada exigida
    else nota fora de 0.0–10.0
        Nota -->> Prof: Erro — nota inválida
    end
```
