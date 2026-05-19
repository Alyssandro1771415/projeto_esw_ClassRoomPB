package pb.classroom.controller;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.Usuario;

public class AutenticacaoController {

    private static final String CREDENCIAIS_INVALIDAS = "Matricula/e-mail ou senha invalidos.";

    private final List<Usuario> usuarios;
    private Usuario usuarioLogado;

    public AutenticacaoController(List<Usuario> usuarios) {
        if (usuarios == null) {
            throw new IllegalArgumentException("lista de usuários é obrigatória");
        }
        this.usuarios = new ArrayList<>(usuarios);
    }

    public Usuario login(String identificador, String senha) {
        validarCampoObrigatorio(identificador, "matrícula/e-mail");
        validarCampoObrigatorio(senha, "senha");

        Usuario usuarioEncontrado = buscarPorMatriculaOuEmail(identificador.trim());
        if (usuarioEncontrado == null || !usuarioEncontrado.getSenha().equals(senha)) {
            throw new IllegalArgumentException(CREDENCIAIS_INVALIDAS);
        }

        if (!usuarioEncontrado.isAtivo()) {
            throw new IllegalArgumentException("Usuário inativo.");
        }

        usuarioLogado = usuarioEncontrado;
        return usuarioLogado;
    }

    public Usuario cadastrarUsuario(PerfilUsuario perfil, String matricula, String nome, String senha) {
        if (!isAutenticado() || usuarioLogado.getPerfil() != PerfilUsuario.ADMINISTRADOR) {
            throw new IllegalArgumentException("Apenas administradores podem cadastrar usuários.");
        }

        validarCampoObrigatorio(matricula, "matrícula");
        validarCampoObrigatorio(nome, "nome");
        validarCampoObrigatorio(senha, "senha");

        if (perfil == null) {
            throw new IllegalArgumentException("perfil é obrigatório.");
        }

        String email = gerarEmailPadrao(nome, perfil);
        validarDuplicidade(matricula.trim(), email);

        Usuario novoUsuario = new Usuario(perfil, nome.trim(), matricula.trim(), email, senha);
        usuarios.add(novoUsuario);
        return novoUsuario;
    }

    public void logout() {
        usuarioLogado = null;
    }

    public boolean isAutenticado() {
        return usuarioLogado != null;
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public List<Usuario> getUsuarios() {
        return Collections.unmodifiableList(usuarios);
    }

    private void validarDuplicidade(String matricula, String email) {
        for (Usuario usuario : usuarios) {
            if (usuario.getMatricula().equals(matricula)) {
                throw new IllegalArgumentException("Já existe usuário cadastrado com essa matrícula.");
            }
            if (usuario.getEmail().equalsIgnoreCase(email)) {
                throw new IllegalArgumentException("Já existe usuário cadastrado com esse e-mail.");
            }
        }
    }

    private String gerarEmailPadrao(String nome, PerfilUsuario perfil) {
        String[] partes = normalizarNomeParaEmail(nome).split("\\.");
        String primeiroNome = partes[0];
        String ultimoNome = partes[partes.length - 1];
        return primeiroNome + "." + ultimoNome + "@" + dominioDoPerfil(perfil) + ".classroom.com";
    }

    private String normalizarNomeParaEmail(String nome) {
        String semAcentos = Normalizer.normalize(nome.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String limpo = semAcentos.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ")
                .trim()
                .replaceAll("\\s+", ".");
        if (limpo.isEmpty()) {
            throw new IllegalArgumentException("nome precisa conter letras ou números.");
        }
        return limpo;
    }

    private String dominioDoPerfil(PerfilUsuario perfil) {
        switch (perfil) {
            case ALUNO:
                return "aluno";
            case PROFESSOR:
                return "professor";
            case COORDENADOR:
                return "coordenador";
            case ADMINISTRADOR:
                return "admin";
            default:
                throw new IllegalArgumentException("perfil inválido.");
        }
    }

    private Usuario buscarPorMatriculaOuEmail(String identificador) {
        for (Usuario usuario : usuarios) {
            if (usuario.getMatricula().equals(identificador)
                    || usuario.getEmail().equalsIgnoreCase(identificador)) {
                return usuario;
            }
        }
        return null;
    }

    private static void validarCampoObrigatorio(String valor, String nomeCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(nomeCampo + " é obrigatório(a).");
        }
    }
}
