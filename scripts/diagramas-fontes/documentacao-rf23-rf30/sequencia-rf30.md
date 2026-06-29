# Diagrama de Sequência — RF30

**Requisito:** O sistema deve alertar quando o aluno estiver abaixo do mínimo exigido.

**Regra:** mínimo fixo de **75%** (`PERCENTUAL_MINIMO_EXIGIDO`); alerta exibido quando há aulas registradas e `percentual < 75%`.

## Alerta na consulta de frequência por turma (RF28 + RF30)

```mermaid
sequenceDiagram
    actor Aluno
    participant CLI as ClassRoomCLI
    participant Pres as PresencaController
    participant Freq as FrequenciaAluno

    Aluno ->> CLI: Opção 22 — Consultar meu percentual
    CLI ->> Pres: consultarMinhaFrequencia(idTurma)
    Pres ->> Freq: new FrequenciaAluno(aluno, turma, totalAulas, presencas)
    Freq ->> Freq: percentual = presencas/total × 100
    Freq ->> Freq: isAbaixoDoMinimoExigido()
    alt percentual >= 75% ou totalAulas = 0
        Freq -->> CLI: getMensagemAlerta() = ""
        CLI -->> Aluno: Exibe percentual sem alerta
    else percentual < 75%
        Freq ->> Freq: getMensagemAlerta()
        Note over Freq: "ALERTA: frequência abaixo do mínimo exigido de 75,0%"
        CLI -->> Aluno: Exibe percentual + mensagem de ALERTA
    end
```

## Alerta na consulta por disciplina (RF29 + RF30)

```mermaid
sequenceDiagram
    actor Aluno
    participant CLI as ClassRoomCLI
    participant Pres as PresencaController
    participant Freq as FrequenciaDisciplinaAluno

    Aluno ->> CLI: Opção 24 — Frequência por disciplina
    CLI ->> Pres: consultarMinhaFrequenciaPorDisciplina(idDisciplina)
    Pres ->> Freq: new FrequenciaDisciplinaAluno(...)
    Freq ->> Freq: isAbaixoDoMinimoExigido()
    alt abaixo de 75%
        CLI ->> CLI: exibirFrequenciaDisciplina()
        CLI ->> CLI: imprime getMensagemAlerta()
        CLI -->> Aluno: ALERTA exibido
    else no limite ou acima
        CLI -->> Aluno: Apenas percentual (sem alerta)
    end
```

## Decisão do alerta (modelo)

```mermaid
sequenceDiagram
    participant Freq as FrequenciaAluno / FrequenciaDisciplinaAluno

    Freq ->> Freq: totalAulasRegistradas > 0 ?
    alt total = 0
        Freq -->> Freq: isAbaixoDoMinimo = false
    else total > 0
        Freq ->> Freq: percentual < 75.0 ?
        alt sim
            Freq -->> Freq: mensagem ALERTA
        else não
            Freq -->> Freq: mensagem vazia
        end
    end
```
