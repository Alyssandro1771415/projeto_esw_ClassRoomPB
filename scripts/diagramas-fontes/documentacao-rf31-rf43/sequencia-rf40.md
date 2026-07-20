# Diagrama de Sequência — RF40

**Requisito:** O coordenador deve gerar relatório de alunos matriculados por turma.

**Métodos:** `RelatorioController.gerarRelatorioAlunosPorTurma` e `exportarRelatorioAlunosPorTurmaPdf`.

## Gerar relatório e baixar PDF

```mermaid
sequenceDiagram
    actor Coord as Coordenador
    participant CLI as ClassRoomCLI
    participant Rel as RelatorioController
    participant PDF as PdfRelatorioWriter

    Coord ->> CLI: Opção 29 — Relatório de alunos por turma
    CLI ->> CLI: selecionar turma
    CLI ->> Rel: gerarRelatorioAlunosPorTurma(idTurma)
    Rel ->> Rel: validarCoordenadorAutenticado()
    Rel ->> Rel: filtrar matrículas CONFIRMADA
    Rel -->> CLI: List~Usuario~
    CLI -->> Coord: Exibe matrícula, nome e e-mail

    Coord ->> CLI: Deseja baixar PDF? (S)
    CLI ->> Rel: exportarRelatorioAlunosPorTurmaPdf(idTurma, destino)
    Rel ->> PDF: escrever(destino, titulo, linhas)
    PDF -->> Rel: Path do arquivo .pdf
    Rel -->> CLI: Path absoluto
    CLI -->> Coord: PDF gerado com sucesso
```

## Validações

```mermaid
sequenceDiagram
    actor Coord as Coordenador
    participant Rel as RelatorioController

    Coord ->> Rel: gerarRelatorioAlunosPorTurma(idTurma)
    alt perfil != COORDENADOR
        Rel -->> Coord: Erro — apenas coordenadores
    else turma inexistente
        Rel -->> Coord: Erro — turma não encontrada
    else ok
        Rel -->> Coord: lista de alunos confirmados
    end
```
