package pb.classroom.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.Usuario;

@DisplayName("AutenticacaoController - cadastro")
class AutenticacaoControllerCadastroTest {

  private static final String MATRICULA_ADMIN = "0001";
  private static final String EMAIL_ADMIN = "admin@classroompb.com";
  private static final String SENHA_ADMIN = "admin123";

  private Usuario administrador;
  private Usuario aluno;
  private AutenticacaoController controller;

  @BeforeEach
  void setUp() {
    administrador =
        new Usuario(
            PerfilUsuario.ADMINISTRADOR,
            "Admin Sistema",
            MATRICULA_ADMIN,
            EMAIL_ADMIN,
            SENHA_ADMIN);
    aluno =
        new Usuario(
            PerfilUsuario.ALUNO,
            "Aluno Existente",
            "2024001",
            "aluno@classroompb.com",
            "senha@123");
    controller = new AutenticacaoController(List.of(administrador, aluno));
  }

  @Test
  @DisplayName("administrador cadastra usuario e e-mail e gerado pelo nome")
  void administradorCadastraUsuarioEEmailEGeradoPeloNome() {
    controller.login(MATRICULA_ADMIN, SENHA_ADMIN);

    Usuario cadastrado =
        controller.cadastrarUsuario(
            PerfilUsuario.ALUNO, "2024002", "Rodrigo Almeida Gomes", "senhaNova");

    assertAll(
        () -> assertEquals(PerfilUsuario.ALUNO, cadastrado.getPerfil()),
        () -> assertEquals("Rodrigo Almeida Gomes", cadastrado.getNome()),
        () -> assertEquals("2024002", cadastrado.getMatricula()),
        () -> assertEquals("rodrigo.gomes@aluno.classroom.com", cadastrado.getEmail()),
        () -> assertEquals("senhaNova", cadastrado.getSenha()),
        () -> assertTrue(cadastrado.isAtivo()),
        () -> assertEquals(3, controller.getUsuarios().size()));
  }

  @Test
  @DisplayName("administrador cadastra admin com dominio admin")
  void administradorCadastraAdminComDominioAdmin() {
    controller.login(MATRICULA_ADMIN, SENHA_ADMIN);

    Usuario cadastrado =
        controller.cadastrarUsuario(
            PerfilUsuario.ADMINISTRADOR, "0002", "Rodrigo Almeida Gomes", "admin456");

    assertEquals("rodrigo.gomes@admin.classroom.com", cadastrado.getEmail());
  }

  @Test
  @DisplayName("usuario nao administrador nao cadastra")
  void usuarioNaoAdministradorNaoCadastra() {
    controller.login(aluno.getMatricula(), aluno.getSenha());

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                controller.cadastrarUsuario(
                    PerfilUsuario.PROFESSOR, "P001", "Professor Teste", "senha"));

    assertEquals("Apenas administradores podem cadastrar usuários.", ex.getMessage());
    assertEquals(2, controller.getUsuarios().size());
  }

  @Test
  @DisplayName("campos obrigatorios sao validados")
  void camposObrigatoriosSaoValidados() {
    controller.login(MATRICULA_ADMIN, SENHA_ADMIN);

    assertAll(
        () ->
            assertThrows(
                IllegalArgumentException.class,
                () -> controller.cadastrarUsuario(null, "2024002", "Nome Teste", "senha")),
        () ->
            assertThrows(
                IllegalArgumentException.class,
                () ->
                    controller.cadastrarUsuario(PerfilUsuario.ALUNO, null, "Nome Teste", "senha")),
        () ->
            assertThrows(
                IllegalArgumentException.class,
                () -> controller.cadastrarUsuario(PerfilUsuario.ALUNO, "2024002", "   ", "senha")),
        () ->
            assertThrows(
                IllegalArgumentException.class,
                () ->
                    controller.cadastrarUsuario(
                        PerfilUsuario.ALUNO, "2024002", "Nome Teste", null)));
  }

  @Test
  @DisplayName("duplicidade de matricula ou e-mail gerado nao cadastra")
  void duplicidadeDeMatriculaOuEmailGeradoNaoCadastra() {
    controller.login(MATRICULA_ADMIN, SENHA_ADMIN);
    controller.cadastrarUsuario(PerfilUsuario.ALUNO, "2024002", "Rodrigo Almeida Gomes", "senha");

    assertAll(
        () ->
            assertThrows(
                IllegalArgumentException.class,
                () ->
                    controller.cadastrarUsuario(
                        PerfilUsuario.ALUNO, "2024002", "Outro Nome", "senha")),
        () ->
            assertThrows(
                IllegalArgumentException.class,
                () ->
                    controller.cadastrarUsuario(
                        PerfilUsuario.ALUNO, "2024003", "Rodrigo Gomes", "senha")));
  }
}
