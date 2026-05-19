package pb.classroom.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Atribuicao de perfis ao usuario")
class UsuarioAtribuicaoTest {

    @Test
    @DisplayName("usuario mantem o perfil informado sem subclasses")
    void usuarioMantemPerfilInformadoSemSubclasses() {
        Usuario aluno = new Usuario(
                PerfilUsuario.ALUNO,
                "Aluno Teste",
                "2024001",
                "aluno@classroompb.com",
                "senha");

        assertAll(
                () -> assertEquals(PerfilUsuario.ALUNO, aluno.getPerfil()),
                () -> assertEquals("Aluno Teste", aluno.getNome()),
                () -> assertEquals(Usuario.class, aluno.getClass()));
    }

    @Test
    @DisplayName("perfil nulo nao cria usuario")
    void perfilNuloNaoCriaUsuario() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Usuario(null, "Nome Teste", "2024001", "email@classroompb.com", "senha"));
    }
}
