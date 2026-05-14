package pb.classroom.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pb.classroom.model.Administrador;
import pb.classroom.model.Aluno;
import pb.classroom.model.Coordenador;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.Professor;
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

    public Usuario cadastrarUsuario(PerfilUsuario perfil, String matricula, String email, String senha) {
        if (!isAutenticado() || usuarioLogado.getPerfil() != PerfilUsuario.ADMINISTRADOR) {
            throw new IllegalArgumentException("Apenas administradores podem cadastrar usuários.");
        }

        validarCampoObrigatorio(matricula, "matrícula");
        validarCampoObrigatorio(email, "e-mail");
        validarCampoObrigatorio(senha, "senha");

        if (perfil == null) {
            throw new IllegalArgumentException("perfil é obrigatório.");
        }

        validarDuplicidade(matricula.trim(), email.trim());

        Usuario novoUsuario = criarUsuario(perfil, matricula.trim(), email.trim(), senha);
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

    private Usuario criarUsuario(PerfilUsuario perfil, String matricula, String email, String senha) {
        switch (perfil) {
            case ALUNO:
                return new Aluno(matricula, email, senha);
            case PROFESSOR:
                return new Professor(matricula, email, senha);
            case COORDENADOR:
                return new Coordenador(matricula, email, senha);
            case ADMINISTRADOR:
                return new Administrador(matricula, email, senha);
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
