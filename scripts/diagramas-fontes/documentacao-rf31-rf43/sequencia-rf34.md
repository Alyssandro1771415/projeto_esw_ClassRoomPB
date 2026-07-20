# Diagrama de Sequência — RF34

**Requisito:** O sistema deve informar a situação do aluno: aprovado, reprovado por nota, reprovado por falta ou em recuperação.

**Regras:** frequência &lt; 75% → `REPROVADO_FALTA`; média ≥ 7 → `APROVADO`; 5 ≤ média &lt; 7 → `EM_RECUPERACAO`; média &lt; 5 → `REPROVADO_NOTA`; etapas incompletas → `EM_ANDAMENTO`.

## Cálculo da situação acadêmica

```mermaid
sequenceDiagram
    actor Usuario as Aluno / Professor / Coordenador
    participant CLI as ClassRoomCLI
    participant Nota as NotaController
    participant Res as ResultadoAvaliacao

    Usuario ->> CLI: Consultar notas / resultados
    CLI ->> Nota: calcularResultado(idTurma, idAluno)
    Nota ->> Res: new ResultadoAvaliacao(notas, percentualFrequencia)
    Res ->> Res: calcularMediaFinal(...)
    Res ->> Res: calcularSituacao(media, etapas, frequencia)
    Nota -->> CLI: ResultadoAvaliacao
    CLI -->> Usuario: Exibe SituacaoAcademica
```

## Decisão da situação

```mermaid
sequenceDiagram
    participant Res as ResultadoAvaliacao

    Res ->> Res: frequencia > 0 e frequencia < 75%?
    alt sim
        Res -->> Res: REPROVADO_FALTA
    else etapas incompletas
        Res -->> Res: EM_ANDAMENTO
    else mediaFinal >= 7.0
        Res -->> Res: APROVADO
    else mediaFinal >= 5.0
        Res -->> Res: EM_RECUPERACAO
    else mediaFinal < 5.0
        Res -->> Res: REPROVADO_NOTA
    end
```

## Situações possíveis

```mermaid
sequenceDiagram
    participant Sistema as ClassRoomPB
    participant Situacao as SituacaoAcademica

    Note over Sistema,Situacao: Enum SituacaoAcademica
    Sistema ->> Situacao: APROVADO
    Sistema ->> Situacao: REPROVADO_NOTA
    Sistema ->> Situacao: REPROVADO_FALTA
    Sistema ->> Situacao: EM_RECUPERACAO
    Sistema ->> Situacao: EM_ANDAMENTO
```
