package pb.classroom.model;

/**
 * Usuário com perfil coordenador (RF01).
 */
public class Coordenador extends Usuario {

    public Coordenador(String matricula, String email, String senha) {
        super(matricula, email, senha);
    }

    public Coordenador(String id, String matricula, String email, String senha, boolean ativo) {
        super(id, matricula, email, senha, ativo);
    }

    @Override
    public PerfilUsuario getPerfil() {
        return PerfilUsuario.COORDENADOR;
    }
}
