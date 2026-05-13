package pb.classroom.model;

/**
 * Usuário com perfil administrador (RF01); cadastra os demais usuários (política acordada).
 */
public class Administrador extends Usuario {

    public Administrador(String matricula, String email, String senha) {
        super(matricula, email, senha);
    }

    public Administrador(String id, String matricula, String email, String senha, boolean ativo) {
        super(id, matricula, email, senha, ativo);
    }

    @Override
    public PerfilUsuario getPerfil() {
        return PerfilUsuario.ADMINISTRADOR;
    }
}
