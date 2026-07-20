# Diagrama de Sequência — RF32

**Requisito:** O sistema deve calcular automaticamente a média final.

**Fórmula:** `mediaEtapas = (etapa1 + etapa2) / 2`; com recuperação: `mediaFinal = max(mediaEtapas, notaRecuperacao)`.

**Método principal:** `ResultadoAvaliacao.calcularMediaFinal(...)` via `NotaController.calcularResultado`.

## Cálculo automático da média final

```mermaid
sequenceDiagram
    actor Prof as Professor / Sistema
    participant CLI as ClassRoomCLI
    participant Nota as NotaController
    participant Res as ResultadoAvaliacao

    Prof ->> CLI: Consulta notas / resultado
    CLI ->> Nota: calcularResultado(idTurma, idAluno)
    Nota ->> Nota: buscarNotaPorTurmaEAluno()
    Nota ->> Nota: obter percentual de frequência
    Nota ->> Res: new ResultadoAvaliacao(notas, frequencia)
    Res ->> Res: calcularMediaFinal(etapa1, etapa2, recuperacao)
    alt etapa1 ou etapa2 ausente
        Res -->> Nota: mediaFinal = null
    else sem recuperação
        Res -->> Nota: mediaFinal = (e1 + e2) / 2
    else com recuperação
        Res -->> Nota: mediaFinal = max(mediaEtapas, recuperacao)
    end
    Nota -->> CLI: ResultadoAvaliacao
    CLI -->> Prof: Exibe média final calculada
```

## Decisão da média (modelo)

```mermaid
sequenceDiagram
    participant Res as ResultadoAvaliacao

    Res ->> Res: etapa1 e etapa2 preenchidas?
    alt não
        Res -->> Res: mediaFinal = null (EM_ANDAMENTO)
    else sim
        Res ->> Res: media = (e1 + e2) / 2
        alt notaRecuperacao != null
            Res ->> Res: mediaFinal = max(media, recuperacao)
        else sem recuperação
            Res ->> Res: mediaFinal = media
        end
    end
```
