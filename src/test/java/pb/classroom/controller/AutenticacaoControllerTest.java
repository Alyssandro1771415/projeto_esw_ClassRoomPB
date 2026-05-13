package pb.classroom.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pb.classroom.model.Aluno;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.Usuario;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AutenticacaoController — Testes de Login")
class AutenticacaoControllerTest {

    private static final String MATRICULA = "2024001";
    private static final String EMAIL = "aluno@classroompb.com";
    private static final String SENHA = "senha@123";

    private Aluno alunoAtivo;
    private Aluno alunoInativo;
    private AutenticacaoController controller;

    @BeforeEach
    void setUp() {
        alunoAtivo = new Aluno(MATRICULA, EMAIL, SENHA);
        alunoInativo = new Aluno("id-inativo", "2024002", "inativo@classroompb.com", SENHA, false);
        controller = new AutenticacaoController(List.of(alunoAtivo, alunoInativo));
    }

    // =========================================================================
    // Cenários: Login com sucesso
    // =========================================================================

    @Nested
    @DisplayName("Login com sucesso")
    class LoginComSucesso {

        @Test
        @DisplayName("login com matrícula correta retorna o usuário")
        void loginComMatriculaCorreta() {
            Usuario retornado = controller.login(MATRICULA, SENHA);

            assertNotNull(retornado, "Deve retornar um usuário não-nulo");
            assertEquals(alunoAtivo, retornado, "Deve retornar exatamente o aluno cadastrado");
        }

        @Test
        @DisplayName("login com e-mail correto retorna o usuário")
        void loginComEmailCorreto() {
            Usuario retornado = controller.login(EMAIL, SENHA);

            assertNotNull(retornado);
            assertEquals(alunoAtivo, retornado);
        }

        @Test
        @DisplayName("login com e-mail em caixa diferente (case-insensitive) é aceito")
        void loginComEmailEmCaixaDiferente() {
            Usuario retornado = controller.login(EMAIL.toUpperCase(), SENHA);

            assertNotNull(retornado);
            assertEquals(alunoAtivo, retornado);
        }

        @Test
        @DisplayName("login com espaços em branco ao redor do identificador é aceito")
        void loginComEspacoAoRedor() {
            Usuario retornado = controller.login("  " + MATRICULA + "  ", SENHA);

            assertNotNull(retornado);
            assertEquals(alunoAtivo, retornado);
        }

        @Test
        @DisplayName("após login bem-sucedido o usuário está autenticado")
        void aposLoginUsuarioEstaAutenticado() {
            controller.login(MATRICULA, SENHA);

            assertTrue(controller.isAutenticado());
        }

        @Test
        @DisplayName("getUsuarioLogado retorna o usuário após login")
        void getUsuarioLogadoAposLogin() {
            controller.login(MATRICULA, SENHA);

            assertEquals(alunoAtivo, controller.getUsuarioLogado());
        }

        @Test
        @DisplayName("perfil do usuário retornado é ALUNO")
        void perfilDoUsuarioRetornadoEAluno() {
            Usuario retornado = controller.login(MATRICULA, SENHA);

            assertEquals(PerfilUsuario.ALUNO, retornado.getPerfil());
        }
    }

    // Cenário: Campos obrigatórios

    @Nested
    @DisplayName("Campos obrigatórios")
    class CamposObrigatorios {

        @Test
        @DisplayName("identificador nulo lança IllegalArgumentException")
        void identificadorNuloLancaExcecao() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.login(null, SENHA));
            assertTrue(ex.getMessage().contains("matrícula/e-mail"),
                    "Mensagem deve mencionar o campo inválido");
        }

        @Test
        @DisplayName("identificador em branco lança IllegalArgumentException")
        void identificadorEmBrancoLancaExcecao() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.login("   ", SENHA));
        }

        @Test
        @DisplayName("identificador vazio lança IllegalArgumentException")
        void identificadorVazioLancaExcecao() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.login("", SENHA));
        }

        @Test
        @DisplayName("senha nula lança IllegalArgumentException")
        void senhaNulaLancaExcecao() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.login(MATRICULA, null));
            assertTrue(ex.getMessage().contains("senha"),
                    "Mensagem deve mencionar o campo inválido");
        }

        @Test
        @DisplayName("senha em branco lança IllegalArgumentException")
        void senhaEmBrancoLancaExcecao() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.login(MATRICULA, "   "));
        }

        @Test
        @DisplayName("senha vazia lança IllegalArgumentException")
        void senhaVaziaLancaExcecao() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.login(MATRICULA, ""));
        }
    }

    // Cenário: Credenciais inválidas

    @Nested
    @DisplayName("Credenciais inválidas")
    class CredenciaisInvalidas {

        @Test
        @DisplayName("senha incorreta lança IllegalArgumentException com mensagem adequada")
        void senhaIncorretaLancaExcecao() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.login(MATRICULA, "senhaErrada"));
            assertEquals("Matrícula/e-mail ou senha inválidos.", ex.getMessage());
        }

        @Test
        @DisplayName("matrícula inexistente lança IllegalArgumentException")
        void matriculaInexistenteLancaExcecao() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.login("9999999", SENHA));
        }

        @Test
        @DisplayName("e-mail inexistente lança IllegalArgumentException")
        void emailInexistenteLancaExcecao() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.login("naoexiste@classroompb.com", SENHA));
        }

        @Test
        @DisplayName("após falha de login o usuário não está autenticado")
        void aposFalhaDeLoginNaoEstaAutenticado() {
            try {
                controller.login(MATRICULA, "senhaErrada");
            } catch (IllegalArgumentException ignored) {
            }

            assertFalse(controller.isAutenticado());
        }
    }

    // Cenário: Usuário inativo

    @Nested
    @DisplayName("Usuário inativo")
    class UsuarioInativo {

        @Test
        @DisplayName("login de usuário inativo lança IllegalArgumentException")
        void loginDeUsuarioInativoLancaExcecao() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.login("2024002", SENHA));
            assertEquals("Usuário inativo.", ex.getMessage());
        }

        @Test
        @DisplayName("usuário inativo não fica autenticado após tentativa")
        void usuarioInativoNaoFicaAutenticado() {
            try {
                controller.login("2024002", SENHA);
            } catch (IllegalArgumentException ignored) {
            }

            assertFalse(controller.isAutenticado());
        }
    }

    // Cenário: Logout e estado do controller

    @Nested
    @DisplayName("Logout e estado")
    class LogoutEEstado {

        @Test
        @DisplayName("antes do login o usuário não está autenticado")
        void antesDoLoginNaoEstaAutenticado() {
            assertFalse(controller.isAutenticado());
        }

        @Test
        @DisplayName("getUsuarioLogado retorna null antes do login")
        void getUsuarioLogadoRetornaNullAntesDoLogin() {
            assertNull(controller.getUsuarioLogado());
        }

        @Test
        @DisplayName("após logout o usuário não está mais autenticado")
        void aposLogoutNaoEstaAutenticado() {
            controller.login(MATRICULA, SENHA);
            controller.logout();

            assertFalse(controller.isAutenticado());
        }

        @Test
        @DisplayName("após logout getUsuarioLogado retorna null")
        void aposLogoutGetUsuarioLogadoRetornaNull() {
            controller.login(MATRICULA, SENHA);
            controller.logout();

            assertNull(controller.getUsuarioLogado());
        }

        @Test
        @DisplayName("logout sem login prévio não lança exceção")
        void logoutSemLoginPrevioNaoLancaExcecao() {
            assertDoesNotThrow(() -> controller.logout());
        }
    }

    // Cenário: Construtor

    @Nested
    @DisplayName("Construtor")
    class Construtor {

        @Test
        @DisplayName("lista nula no construtor lança IllegalArgumentException")
        void listaNulaLancaExcecao() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> new AutenticacaoController(null));
        }

        @Test
        @DisplayName("lista vazia cria controller sem usuários (login falha)")
        void listaVaziaPermiteInstanciarMasFalhaNoLogin() {
            AutenticacaoController vazio = new AutenticacaoController(Collections.emptyList());

            assertThrows(
                    IllegalArgumentException.class,
                    () -> vazio.login(MATRICULA, SENHA));
        }

        @Test
        @DisplayName("getUsuarios retorna lista imutável")
        void getUsuariosRetornaListaImutavel() {
            List<Usuario> lista = controller.getUsuarios();

            assertThrows(
                    UnsupportedOperationException.class,
                    () -> lista.add(new Aluno("X", "x@x.com", "pass")));
        }
    }
}
