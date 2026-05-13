package pb.classroom.model;

/**
 * Usuário com perfil professor (RF01).
 */
public class Professor extends Usuario {

    public Professor(String matricula, String email, String senha) {
        super(matricula, email, senha);
    }

    public Professor(String id, String matricula, String email, String senha, boolean ativo) {
        super(id, matricula, email, senha, ativo);
    }

    @Override
    public PerfilUsuario getPerfil() {
        return PerfilUsuario.PROFESSOR;
    }
}
