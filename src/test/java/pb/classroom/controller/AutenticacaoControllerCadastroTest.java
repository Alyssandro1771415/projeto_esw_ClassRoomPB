package pb.classroom.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pb.classroom.model.Administrador;
import pb.classroom.model.Aluno;
import pb.classroom.model.Coordenador;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.Professor;
import pb.classroom.model.Usuario;

import java.text.Normalizer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AutenticacaoController - Testes de cadastro")
class AutenticacaoControllerCadastroTest {

    private static final String MATRICULA_ADMIN = "0001";
    private static final String EMAIL_ADMIN = "admin@classroompb.com";
    private static final String SENHA_ADMIN = "admin123";

    private Administrador administrador;
    private Aluno aluno;
    private AutenticacaoController controller;

    // Mantem um administrador e um aluno para testar permissao e duplicidade.
    @BeforeEach
    void setUp() {
        administrador = new Administrador(MATRICULA_ADMIN, EMAIL_ADMIN, SENHA_ADMIN);
        aluno = new Aluno("2024001", "aluno@classroompb.com", "senha@123");
        controller = new AutenticacaoController(List.of(administrador, aluno));
    }

    @Nested
    @DisplayName("Cadastro com sucesso")
    class CadastroComSucesso {

        @Test
        @DisplayName("administrador autenticado cadastra novo aluno")
        void administradorAutenticadoCadastraNovoAluno() {
            controller.login(MATRICULA_ADMIN, SENHA_ADMIN);

            Usuario cadastrado = controller.cadastrarUsuario(
                    PerfilUsuario.ALUNO,
                    "2024002",
                    "novo.aluno@classroompb.com",
                    "senhaNova");

            assertAll(
                    () -> assertInstanceOf(Aluno.class, cadastrado),
                    () -> assertEquals(PerfilUsuario.ALUNO, cadastrado.getPerfil()),
                    () -> assertEquals("2024002", cadastrado.getMatricula()),
                    () -> assertEquals("novo.aluno@classroompb.com", cadastrado.getEmail()),
                    () -> assertEquals("senhaNova", cadastrado.getSenha()),
                    () -> assertTrue(cadastrado.isAtivo()),
                    () -> assertTrue(controller.getUsuarios().contains(cadastrado)),
                    () -> assertEquals(3, controller.getUsuarios().size()));
        }

        @Test
        @DisplayName("cadastro remove espacos ao redor de matricula e e-mail")
        void cadastroRemoveEspacosAoRedorDeMatriculaEEmail() {
            controller.login(EMAIL_ADMIN, SENHA_ADMIN);

            Usuario cadastrado = controller.cadastrarUsuario(
                    PerfilUsuario.ALUNO,
                    "  2024003  ",
                    "  aluno.trim@classroompb.com  ",
                    "senhaTrim");

            assertEquals("2024003", cadastrado.getMatricula());
            assertEquals("aluno.trim@classroompb.com", cadastrado.getEmail());
        }

        @Test
        @DisplayName("usuario cadastrado consegue fazer login")
        void usuarioCadastradoConsegueFazerLogin() {
            controller.login(MATRICULA_ADMIN, SENHA_ADMIN);
            controller.cadastrarUsuario(
                    PerfilUsuario.ALUNO,
                    "2024004",
                    "login.novo@classroompb.com",
                    "senhaLogin");
            controller.logout();

            Usuario logado = controller.login("2024004", "senhaLogin");

            assertEquals(PerfilUsuario.ALUNO, logado.getPerfil());
            assertEquals("login.novo@classroompb.com", logado.getEmail());
        }

        @Test
        @DisplayName("administrador cadastra professor")
        void administradorCadastraProfessor() {
            controller.login(MATRICULA_ADMIN, SENHA_ADMIN);

            Usuario cadastrado = controller.cadastrarUsuario(
                    PerfilUsuario.PROFESSOR,
                    "P001",
                    "professor@classroompb.com",
                    "senhaProfessor");

            assertAll(
                    () -> assertInstanceOf(Professor.class, cadastrado),
                    () -> assertEquals(PerfilUsuario.PROFESSOR, cadastrado.getPerfil()));
        }

        @Test
        @DisplayName("administrador cadastra coordenador")
        void administradorCadastraCoordenador() {
            controller.login(MATRICULA_ADMIN, SENHA_ADMIN);

            Usuario cadastrado = controller.cadastrarUsuario(
                    PerfilUsuario.COORDENADOR,
                    "C001",
                    "coordenador@classroompb.com",
                    "senhaCoordenador");

            assertAll(
                    () -> assertInstanceOf(Coordenador.class, cadastrado),
                    () -> assertEquals(PerfilUsuario.COORDENADOR, cadastrado.getPerfil()));
        }

        @Test
        @DisplayName("administrador cadastra outro administrador")
        void administradorCadastraOutroAdministrador() {
            controller.login(MATRICULA_ADMIN, SENHA_ADMIN);

            Usuario cadastrado = controller.cadastrarUsuario(
                    PerfilUsuario.ADMINISTRADOR,
                    "0002",
                    "admin2@classroompb.com",
                    "admin456");

            assertAll(
                    () -> assertInstanceOf(Administrador.class, cadastrado),
                    () -> assertEquals(PerfilUsuario.ADMINISTRADOR, cadastrado.getPerfil()));
        }
    }

    @Nested
    @DisplayName("Permissoes")
    class Permissoes {

        @Test
        @DisplayName("usuario nao autenticado nao pode cadastrar")
        void usuarioNaoAutenticadoNaoPodeCadastrar() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.cadastrarUsuario(
                            PerfilUsuario.ALUNO,
                            "2024002",
                            "bloqueado@classroompb.com",
                            "senha"));

            assertEquals("Apenas administradores podem cadastrar usuarios.", removerAcentos(ex.getMessage()));
            assertEquals(2, controller.getUsuarios().size());
        }

        @Test
        @DisplayName("aluno autenticado nao pode cadastrar")
        void alunoAutenticadoNaoPodeCadastrar() {
            controller.login(aluno.getMatricula(), aluno.getSenha());

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.cadastrarUsuario(
                            PerfilUsuario.PROFESSOR,
                            "P001",
                            "professor@classroompb.com",
                            "senha"));

            assertEquals("Apenas administradores podem cadastrar usuarios.", removerAcentos(ex.getMessage()));
            assertEquals(2, controller.getUsuarios().size());
        }
    }

    @Nested
    @DisplayName("Campos obrigatorios")
    class CamposObrigatorios {

        // Cadastro exige sessao administrativa antes de validar os campos.
        @BeforeEach
        void loginComoAdministrador() {
            controller.login(MATRICULA_ADMIN, SENHA_ADMIN);
        }

        @Test
        @DisplayName("perfil nulo lanca IllegalArgumentException")
        void perfilNuloLancaExcecao() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.cadastrarUsuario(
                            null,
                            "2024002",
                            "perfil.nulo@classroompb.com",
                            "senha"));

            assertEquals("perfil e obrigatorio.", removerAcentos(ex.getMessage()));
        }

        @Test
        @DisplayName("matricula nula lanca IllegalArgumentException")
        void matriculaNulaLancaExcecao() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.cadastrarUsuario(
                            PerfilUsuario.ALUNO,
                            null,
                            "matricula.nula@classroompb.com",
                            "senha"));
        }

        @Test
        @DisplayName("matricula em branco lanca IllegalArgumentException")
        void matriculaEmBrancoLancaExcecao() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.cadastrarUsuario(
                            PerfilUsuario.ALUNO,
                            "   ",
                            "matricula.branco@classroompb.com",
                            "senha"));
        }

        @Test
        @DisplayName("e-mail nulo lanca IllegalArgumentException")
        void emailNuloLancaExcecao() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.cadastrarUsuario(
                            PerfilUsuario.ALUNO,
                            "2024002",
                            null,
                            "senha"));
        }

        @Test
        @DisplayName("e-mail em branco lanca IllegalArgumentException")
        void emailEmBrancoLancaExcecao() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.cadastrarUsuario(
                            PerfilUsuario.ALUNO,
                            "2024002",
                            "   ",
                            "senha"));
        }

        @Test
        @DisplayName("senha nula lanca IllegalArgumentException")
        void senhaNulaLancaExcecao() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.cadastrarUsuario(
                            PerfilUsuario.ALUNO,
                            "2024002",
                            "senha.nula@classroompb.com",
                            null));
        }

        @Test
        @DisplayName("senha em branco lanca IllegalArgumentException")
        void senhaEmBrancoLancaExcecao() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.cadastrarUsuario(
                            PerfilUsuario.ALUNO,
                            "2024002",
                            "senha.branco@classroompb.com",
                            "   "));
        }
    }

    @Nested
    @DisplayName("Duplicidade")
    class Duplicidade {

        // Duplicidade so e avaliada apos passar pela permissao de administrador.
        @BeforeEach
        void loginComoAdministrador() {
            controller.login(MATRICULA_ADMIN, SENHA_ADMIN);
        }

        @Test
        @DisplayName("matricula duplicada nao cadastra usuario")
        void matriculaDuplicadaNaoCadastraUsuario() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.cadastrarUsuario(
                            PerfilUsuario.ALUNO,
                            aluno.getMatricula(),
                            "outro.email@classroompb.com",
                            "senha"));

            assertEquals("Ja existe usuario cadastrado com essa matricula.", removerAcentos(ex.getMessage()));
            assertEquals(2, controller.getUsuarios().size());
        }

        @Test
        @DisplayName("e-mail duplicado nao cadastra usuario")
        void emailDuplicadoNaoCadastraUsuario() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.cadastrarUsuario(
                            PerfilUsuario.ALUNO,
                            "2024002",
                            aluno.getEmail(),
                            "senha"));

            assertEquals("Ja existe usuario cadastrado com esse e-mail.", removerAcentos(ex.getMessage()));
            assertEquals(2, controller.getUsuarios().size());
        }

        @Test
        @DisplayName("e-mail duplicado com caixa diferente nao cadastra usuario")
        void emailDuplicadoComCaixaDiferenteNaoCadastraUsuario() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> controller.cadastrarUsuario(
                            PerfilUsuario.ALUNO,
                            "2024002",
                            aluno.getEmail().toUpperCase(),
                            "senha"));

            assertEquals(2, controller.getUsuarios().size());
        }
    }

    private static String removerAcentos(String texto) {
        // Evita falhas por diferenca de acentuacao nas mensagens.
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
