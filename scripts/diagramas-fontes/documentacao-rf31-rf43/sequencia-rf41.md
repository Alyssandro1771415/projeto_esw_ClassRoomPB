# Diagrama de Sequência — RF41

**Requisito:** O coordenador deve gerar relatório de ocupação de vagas.

**Métodos:** `RelatorioController.gerarRelatorioOcupacaoVagas` e `exportarRelatorioOcupacaoVagasPdf`.

## Gerar relatório de ocupação e baixar PDF

```mermaid
sequenceDiagram
    actor Coord as Coordenador
    participant CLI as ClassRoomCLI
    participant Rel as RelatorioController
    participant PDF as PdfRelatorioWriter

    Coord ->> CLI: Opção 30 — Relatório de ocupação de vagas
    CLI ->> Rel: gerarRelatorioOcupacaoVagas()
    Rel ->> Rel: validarCoordenadorAutenticado()
    loop Para cada turma não cancelada
        Rel ->> Rel: contar matrículas CONFIRMADA
        Rel ->> Rel: ocupação = matriculados / limiteVagas × 100
    end
    Rel -->> CLI: List~String~
    CLI -->> Coord: Exibe vagas, matriculados e % ocupação

    Coord ->> CLI: Deseja baixar PDF? (S)
    CLI ->> Rel: exportarRelatorioOcupacaoVagasPdf(destino)
    Rel ->> PDF: escrever(...)
    PDF -->> CLI: Path do arquivo .pdf
    CLI -->> Coord: PDF gerado com sucesso
```

## Cálculo de ocupação

```mermaid
sequenceDiagram
    participant Rel as RelatorioController
    participant Turma as Turma

    Rel ->> Turma: getLimiteVagas()
    Rel ->> Rel: matriculadosConfirmados
    alt limiteVagas > 0
        Rel -->> Rel: percentual = matriculados / limite × 100
    else limite inválido
        Rel -->> Rel: percentual = 0.0
    end
```
