package pb.classroom.model;

/**
 * Usuário com perfil aluno (RF01).
 */
public class Aluno extends Usuario {

    public Aluno(String matricula, String email, String senha) {
        super(matricula, email, senha);
    }

    public Aluno(String id, String matricula, String email, String senha, boolean ativo) {
        super(id, matricula, email, senha, ativo);
    }

    @Override
    public PerfilUsuario getPerfil() {
        return PerfilUsuario.ALUNO;
    }
}
