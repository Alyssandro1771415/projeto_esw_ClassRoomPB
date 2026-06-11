package pb.classroom.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.Usuario;

@DisplayName("AutenticacaoController - login")
class AutenticacaoControllerLoginTest {

  private static final String MATRICULA = "2024001";
  private static final String EMAIL = "aluno@classroompb.com";
  private static final String SENHA = "senha@123";
  private static final String MATRICULA_ADMIN = "0001";
  private static final String EMAIL_ADMIN = "admin@classroompb.com";
  private static final String SENHA_ADMIN = "admin123";

  private Usuario alunoAtivo;
  private Usuario alunoInativo;
  private Usuario administrador;
  private AutenticacaoController controller;

  @BeforeEach
  void setUp() {
    alunoAtivo = new Usuario(PerfilUsuario.ALUNO, "Aluno Ativo", MATRICULA, EMAIL, SENHA);
    alunoInativo =
        new Usuario(
            "id-inativo",
            PerfilUsuario.ALUNO,
            "Aluno Inativo",
            "2024002",
            "inativo@classroompb.com",
            SENHA,
            false);
    administrador =
        new Usuario(
            PerfilUsuario.ADMINISTRADOR,
            "Admin Sistema",
            MATRICULA_ADMIN,
            EMAIL_ADMIN,
            SENHA_ADMIN);
    controller = new AutenticacaoController(List.of(alunoAtivo, alunoInativo, administrador));
  }

  @Test
  @DisplayName("login com matricula correta retorna usuario")
  void loginComMatriculaCorreta() {
    Usuario retornado = controller.login(MATRICULA, SENHA);

    assertEquals(alunoAtivo, retornado);
    assertTrue(controller.isAutenticado());
  }

  @Test
  @DisplayName("login com e-mail correto ignora caixa")
  void loginComEmailCorretoIgnoraCaixa() {
    Usuario retornado = controller.login(EMAIL.toUpperCase(), SENHA);

    assertEquals(alunoAtivo, retornado);
  }

  @Test
  @DisplayName("senha incorreta nao autentica")
  void senhaIncorretaNaoAutentica() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> controller.login(MATRICULA, "senhaErrada"));

    assertEquals("Matricula/e-mail ou senha invalidos.", ex.getMessage());
    assertFalse(controller.isAutenticado());
  }

  @Test
  @DisplayName("usuario inativo nao autentica")
  void usuarioInativoNaoAutentica() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> controller.login("2024002", SENHA));

    assertEquals("Usuário inativo.", ex.getMessage());
    assertFalse(controller.isAutenticado());
  }

  @Test
  @DisplayName("logout limpa usuario logado")
  void logoutLimpaUsuarioLogado() {
    controller.login(MATRICULA, SENHA);
    controller.logout();

    assertFalse(controller.isAutenticado());
    assertNull(controller.getUsuarioLogado());
  }

  @Test
  @DisplayName("campos obrigatorios sao validados")
  void camposObrigatoriosSaoValidados() {
    assertAll(
        () -> assertThrows(IllegalArgumentException.class, () -> controller.login(null, SENHA)),
        () -> assertThrows(IllegalArgumentException.class, () -> controller.login("   ", SENHA)),
        () -> assertThrows(IllegalArgumentException.class, () -> controller.login(MATRICULA, null)),
        () ->
            assertThrows(IllegalArgumentException.class, () -> controller.login(MATRICULA, "   ")));
  }

  @Test
  @DisplayName("construtor aceita lista vazia e getUsuarios e imutavel")
  void construtorAceitaListaVaziaEGetUsuariosEImutavel() {
    AutenticacaoController vazio = new AutenticacaoController(Collections.emptyList());

    assertThrows(IllegalArgumentException.class, () -> vazio.login(MATRICULA, SENHA));
    assertThrows(
        UnsupportedOperationException.class,
        () ->
            controller
                .getUsuarios()
                .add(new Usuario(PerfilUsuario.ALUNO, "Outro Aluno", "X", "x@x.com", "pass")));
  }
}
