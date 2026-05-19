package pb.classroom.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.Usuario;

public class UsuarioRepository {

    private static final Pattern OBJETO_JSON = Pattern.compile("\\{([^{}]*)\\}");
    private static final String ARQUIVO_PADRAO = "armazenamento_interno.json";

    private final Path caminhoArquivo;

    public UsuarioRepository() {
        this(Paths.get(ARQUIVO_PADRAO));
    }

    public UsuarioRepository(Path caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }

    public List<Usuario> carregarUsuarios() {
        try {
            if (!Files.exists(caminhoArquivo)) {
                return criarESalvarAdministradorInicial();
            }

            String conteudo = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
            List<Usuario> usuarios = converterJsonParaUsuarios(conteudo);

            if (usuarios.isEmpty()) {
                return criarESalvarAdministradorInicial();
            }

            return usuarios;
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível carregar os usuários.", e);
        }
    }

    public void salvarUsuarios(List<Usuario> usuarios) {
        try {
            String conteudoAtual = "";
            if (Files.exists(caminhoArquivo)) {
                conteudoAtual = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
            }

            String disciplinasJson = ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "disciplinas");
            String cursosJson = ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "cursos");
            String periodosLetivosJson = ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "periodosLetivos");
            String documento = ArmazenamentoJson.montarDocumento(
                    converterUsuariosParaJson(usuarios),
                    disciplinasJson,
                    cursosJson,
                    periodosLetivosJson);
            Files.write(caminhoArquivo, documento.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar os usuários.", e);
        }
    }

    private List<Usuario> criarESalvarAdministradorInicial() {
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(new Usuario(
                PerfilUsuario.ADMINISTRADOR,
                "Administrador Padrao",
                "0001",
                "admin@classroompb.com",
                "admin123"));
        salvarUsuarios(usuarios);
        return usuarios;
    }

    private List<Usuario> converterJsonParaUsuarios(String conteudo) {
        List<Usuario> usuarios = new ArrayList<>();
        Matcher matcher = OBJETO_JSON.matcher(conteudo);

        while (matcher.find()) {
            String objeto = matcher.group(1);
            if (!objeto.contains("\"perfil\"") || !objeto.contains("\"matricula\"")) {
                continue;
            }

            PerfilUsuario perfil = PerfilUsuario.valueOf(obterTexto(objeto, "perfil"));
            String id = obterTexto(objeto, "id");
            String nome = obterTextoOuPadrao(objeto, "nome", obterTexto(objeto, "matricula"));
            String matricula = obterTexto(objeto, "matricula");
            String email = obterTexto(objeto, "email");
            String senha = obterTexto(objeto, "senha");
            boolean ativo = obterBooleano(objeto, "ativo");

            usuarios.add(new Usuario(id, perfil, nome, matricula, email, senha, ativo));
        }

        return usuarios;
    }

    private String converterUsuariosParaJson(List<Usuario> usuarios) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < usuarios.size(); i++) {
            Usuario usuario = usuarios.get(i);
            json.append("    {\n");
            json.append("      \"id\": \"").append(escapar(usuario.getId())).append("\",\n");
            json.append("      \"perfil\": \"").append(usuario.getPerfil()).append("\",\n");
            json.append("      \"nome\": \"").append(escapar(usuario.getNome())).append("\",\n");
            json.append("      \"matricula\": \"").append(escapar(usuario.getMatricula())).append("\",\n");
            json.append("      \"email\": \"").append(escapar(usuario.getEmail())).append("\",\n");
            json.append("      \"senha\": \"").append(escapar(usuario.getSenha())).append("\",\n");
            json.append("      \"ativo\": ").append(usuario.isAtivo()).append("\n");
            json.append("    }");

            if (i < usuarios.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]");
        return json.toString();
    }

    private String obterTexto(String objeto, String campo) {
        Matcher matcher = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(objeto);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Campo obrigatório ausente no armazenamento: " + campo);
        }
        return desescapar(matcher.group(1));
    }

    private String obterTextoOuPadrao(String objeto, String campo, String padrao) {
        Matcher matcher = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(objeto);
        if (!matcher.find()) {
            return padrao;
        }
        return desescapar(matcher.group(1));
    }

    private boolean obterBooleano(String objeto, String campo) {
        Matcher matcher = Pattern.compile("\"" + campo + "\"\\s*:\\s*(true|false)").matcher(objeto);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Campo obrigatório ausente no armazenamento: " + campo);
        }
        return Boolean.parseBoolean(matcher.group(1));
    }

    private String escapar(String valor) {
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String desescapar(String valor) {
        return valor.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
