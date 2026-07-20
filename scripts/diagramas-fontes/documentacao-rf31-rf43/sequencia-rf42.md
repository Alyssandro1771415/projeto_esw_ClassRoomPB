# Diagrama de Sequência — RF42

**Requisito:** O coordenador deve gerar relatório de reprovação por disciplina.

**Métodos:** `RelatorioController.gerarRelatorioReprovacaoPorDisciplina` e `exportarRelatorioReprovacaoPorDisciplinaPdf`.

## Gerar relatório de reprovação e baixar PDF

```mermaid
sequenceDiagram
    actor Coord as Coordenador
    participant CLI as ClassRoomCLI
    participant Rel as RelatorioController
    participant Hist as HistoricoAcademico
    participant PDF as PdfRelatorioWriter

    Coord ->> CLI: Opção 31 — Relatório de reprovação por disciplina
    CLI ->> CLI: selecionar disciplina
    CLI ->> Rel: gerarRelatorioReprovacaoPorDisciplina(idDisciplina)
    Rel ->> Rel: validarCoordenadorAutenticado()
    Rel ->> Rel: validarDisciplinaExistente()
    loop Históricos da disciplina
        Rel ->> Hist: getSituacao()
        alt situacao contém REPROVADO
            Rel ->> Rel: totalReprovados++
        end
        Rel ->> Rel: totalRegistros++
    end
    Rel ->> Rel: taxa = reprovados / total × 100
    Rel -->> CLI: List~String~
    CLI -->> Coord: Exibe totais e taxa de reprovação

    Coord ->> CLI: Deseja baixar PDF? (S)
    CLI ->> Rel: exportarRelatorioReprovacaoPorDisciplinaPdf(...)
    Rel ->> PDF: escrever(...)
    PDF -->> CLI: Path do arquivo .pdf
    CLI -->> Coord: PDF gerado com sucesso
```

## Contagem de reprovações

```mermaid
sequenceDiagram
    participant Rel as RelatorioController
    participant Hist as HistoricoAcademico

    Rel ->> Hist: situacao.toString()
    alt REPROVADO_NOTA ou REPROVADO_FALTA
        Rel -->> Rel: conta como reprovado
    else APROVADO / EM_RECUPERACAO / EM_ANDAMENTO
        Rel -->> Rel: não conta como reprovado
    end
```
