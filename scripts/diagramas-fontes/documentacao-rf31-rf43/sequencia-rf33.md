# Diagrama de Sequência — RF33

**Requisito:** O aluno deve poder consultar suas notas.

**Método principal:** `NotaController.consultarMinhasNotas()`.

## Aluno consulta próprias notas

```mermaid
sequenceDiagram
    actor Aluno
    participant CLI as ClassRoomCLI
    participant Auth as AutenticacaoController
    participant Nota as NotaController
    participant Res as ResultadoAvaliacao

    Aluno ->> CLI: Login
    CLI ->> Auth: login(...)
    Auth -->> CLI: Usuario ALUNO

    Aluno ->> CLI: Opção 25 — Consultar minhas notas
    CLI ->> Nota: consultarMinhasNotas()
    Nota ->> Nota: validarAlunoAutenticado()
    loop Para cada matrícula CONFIRMADA
        Nota ->> Nota: calcularResultado(idTurma, idAluno)
        Nota ->> Res: new ResultadoAvaliacao(...)
        Res ->> Res: média e situação
    end
    Nota -->> CLI: List~ResultadoAvaliacao~
    CLI -->> Aluno: Exibe etapa1, etapa2, média e situação
```

## Restrição de perfil

```mermaid
sequenceDiagram
    actor Usuario
    participant Nota as NotaController

    Usuario ->> Nota: consultarMinhasNotas()
    alt perfil != ALUNO
        Nota -->> Usuario: Erro — apenas alunos consultam as próprias notas
    else aluno autenticado
        Nota -->> Usuario: List~ResultadoAvaliacao~
    end
```
