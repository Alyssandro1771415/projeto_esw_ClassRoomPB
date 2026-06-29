# Diagrama de Sequência — RF25

**Requisito:** A lista de espera deve respeitar a ordem de solicitação.

**Implementação:** a ordem FIFO é garantida pela sequência de inserção na lista em memória (`List<Matricula>`); consultas e promoções percorrem essa lista na mesma ordem.

## Múltiplas solicitações preservam ordem FIFO

```mermaid
sequenceDiagram
    actor A1 as Aluno 1
    actor A2 as Aluno 2
    actor A3 as Aluno 3
    participant Mat as MatriculaController

    A1 ->> Mat: solicitarMatricula(turma) [turma lotada]
    Mat ->> Mat: add Matricula EM_ESPERA (1º)
    A2 ->> Mat: solicitarMatricula(turma)
    Mat ->> Mat: add Matricula EM_ESPERA (2º)
    A3 ->> Mat: solicitarMatricula(turma)
    Mat ->> Mat: add Matricula EM_ESPERA (3º)

    Note over Mat: consultarListaEsperaOrdenadaPorSolicitacao()
    Mat -->> A1: posição 1
    Mat -->> A2: posição 2
    Mat -->> A3: posição 3
```

## Consulta ordenada e promoção respeitam a fila

```mermaid
sequenceDiagram
    participant Mat as MatriculaController
    participant Lista as List~Matricula~

    Note over Lista: [confirmada, espera₁, espera₂, espera₃]

    Mat ->> Lista: listarMatriculasEmEsperaOrdenadas()
    Lista -->> Mat: [espera₁, espera₂, espera₃]

    Note over Mat: cancelamento libera 1 vaga
    Mat ->> Mat: promoverProximoAlunoElegivel()
    Mat ->> Lista: itera na ordem → promove espera₁
    Mat ->> Mat: consultarListaEsperaOrdenadaPorSolicitacao()
    Mat -->> Mat: [espera₂, espera₃] (ordem restante preservada)
```

## Persistência mantém ordem no JSON

```mermaid
sequenceDiagram
    participant Mat as MatriculaController
    participant Repo as MatriculaRepository
    participant JSON as armazenamento_interno.json

    Mat ->> Repo: salvarMatriculas(lista ordenada)
    Repo ->> JSON: grava array na ordem atual
    Repo ->> Mat: carregarMatriculas()
    Mat ->> Repo: ler JSON
    Repo -->> Mat: lista na mesma ordem de gravação
    Note over Mat,JSON: RF25 — ordem de solicitação preservada em memória e arquivo
```
