package pb.classroom.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Usuário autenticável do sistema: matrícula ou e-mail no login (RF02),
 * perfil distinto (RF03), ativo para uso ou inativo após remoção lógica.
 */
public abstract class Usuario {

    private final String id;
    private String matricula;
    private String email;
    private String senha;
    private boolean ativo;

    protected Usuario(String id, String matricula, String email, String senha, boolean ativo) {
        this.id = Objects.requireNonNull(id, "id").trim();
        if (this.id.isEmpty()) {
            throw new IllegalArgumentException("id não pode ser vazio");
        }
        validarMatricula(matricula);
        validarEmail(email);
        validarSenha(senha);
        this.matricula = matricula.trim();
        this.email = email.trim();
        this.senha = senha;
        this.ativo = ativo;
    }

    /**
     * Novo usuário ativo com identificador gerado automaticamente.
     */
    protected Usuario(String matricula, String email, String senha) {
        this(UUID.randomUUID().toString(), matricula, email, senha, true);
    }

    public abstract PerfilUsuario getPerfil();

    public String getId() {
        return id;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        validarMatricula(matricula);
        this.matricula = matricula.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        validarEmail(email);
        this.email = email.trim();
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        validarSenha(senha);
        this.senha = senha;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    private static void validarMatricula(String matricula) {
        if (matricula == null || matricula.trim().isEmpty()) {
            throw new IllegalArgumentException("matrícula é obrigatória");
        }
    }

    private static void validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("e-mail é obrigatório");
        }
    }

    private static void validarSenha(String senha) {
        if (senha == null || senha.isEmpty()) {
            throw new IllegalArgumentException("senha é obrigatória");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Usuario usuario = (Usuario) o;
        return id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
