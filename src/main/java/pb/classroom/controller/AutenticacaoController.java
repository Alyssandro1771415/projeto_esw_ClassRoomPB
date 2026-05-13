package pb.classroom.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pb.classroom.model.Usuario;

public class AutenticacaoController {

    private static final String CREDENCIAIS_INVALIDAS = "Matrícula/e-mail ou senha inválidos.";

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
