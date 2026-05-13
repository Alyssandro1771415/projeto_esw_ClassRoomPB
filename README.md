# ClassRoomPB

Projeto Maven. Código em `src/main/java`; testes em `src/test/java`.

## Pacotes (MVC)

| Pacote | Papel |
|--------|--------|
| `pb.classroom` | `Main` — entrada da aplicação |
| `pb.classroom.model` | Entidades e enums |
| `pb.classroom.view` | Interface de comandos (CLI) |
| `pb.classroom.controller` | Fluxos e orquestração |

## Raiz do repositório

- `armazenamento_interno.json` — persistência local (estado entre execuções).
- `releases/` — relatórios PDF das entregas.

## Executar

```bash
mvn compile exec:java
```

Classe principal: `pb.classroom.Main`.
