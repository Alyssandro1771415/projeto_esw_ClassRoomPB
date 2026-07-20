# Diagrama de Sequência — RF43

**Requisito:** O administrador deve gerar relatório geral de usuários cadastrados.

**Métodos:** `RelatorioController.gerarRelatorioGeralUsuarios` e `exportarRelatorioGeralUsuariosPdf`.

## Gerar relatório de usuários e baixar PDF

```mermaid
sequenceDiagram
    actor Admin as Administrador
    participant CLI as ClassRoomCLI
    participant Auth as AutenticacaoController
    participant Rel as RelatorioController
    participant PDF as PdfRelatorioWriter

    Admin ->> CLI: Login
    CLI ->> Auth: login(...)
    Auth -->> CLI: Usuario ADMINISTRADOR

    Admin ->> CLI: Opção 32 — Relatório geral de usuários
    CLI ->> Rel: gerarRelatorioGeralUsuarios()
    Rel ->> Rel: validar perfil ADMINISTRADOR
    loop Para cada usuário
        Rel ->> Rel: formatar matrícula, nome, e-mail, perfil, status
    end
    Rel -->> CLI: List~String~
    CLI -->> Admin: Exibe usuários cadastrados

    Admin ->> CLI: Deseja baixar PDF? (S)
    CLI ->> Rel: exportarRelatorioGeralUsuariosPdf(destino)
    Rel ->> PDF: escrever(...)
    PDF -->> CLI: Path do arquivo .pdf
    CLI -->> Admin: PDF gerado com sucesso
```

## Restrição de perfil

```mermaid
sequenceDiagram
    actor Usuario
    participant Rel as RelatorioController

    Usuario ->> Rel: gerarRelatorioGeralUsuarios()
    alt perfil != ADMINISTRADOR
        Rel -->> Usuario: Erro — apenas administradores
    else administrador autenticado
        Rel -->> Usuario: List~String~ com todos os usuários
    end
```
