package pb.classroom.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pb.classroom.model.Administrador;
import pb.classroom.model.Aluno;
import pb.classroom.model.Curso;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CursoController - cadastro de cursos")
class CursoControllerTest {

    private static final String SENHA = "senha";

    private AutenticacaoController autenticacaoController;
    private CursoController cursoController;

    @BeforeEach
    void setUp() {
        autenticacaoController = new AutenticacaoController(List.of(
                new Administrador("0001", "admin@classroompb.com", SENHA),
                new Aluno("2026001", "aluno@classroompb.com", SENHA)));
        cursoController = new CursoController(autenticacaoController, List.of());
    }

    @Test
    @DisplayName("administrador autenticado cadastra curso")
    void administradorAutenticadoCadastraCurso() {
        autenticacaoController.login("0001", SENHA);

        Curso curso = cursoController.cadastrarCurso("Computacao", "CC");

        assertAll(
                () -> assertEquals("Computacao", curso.getNome()),
                () -> assertEquals("CC", curso.getCodigo()),
                () -> assertTrue(cursoController.getCursos().contains(curso)));
    }

    @Test
    @DisplayName("cadastro remove espacos de nome e codigo")
    void cadastroRemoveEspacosDeNomeECodigo() {
        autenticacaoController.login("0001", SENHA);

        Curso curso = cursoController.cadastrarCurso("  Engenharia de Software  ", "  ES  ");

        assertAll(
                () -> assertEquals("Engenharia de Software", curso.getNome()),
                () -> assertEquals("ES", curso.getCodigo()));
    }

    @Test
    @DisplayName("codigo em branco e tratado como opcional")
    void codigoEmBrancoETratadoComoOpcional() {
        autenticacaoController.login("0001", SENHA);

        Curso curso = cursoController.cadastrarCurso("Sistemas de Informacao", "   ");

        assertNull(curso.getCodigo());
        assertEquals(1, cursoController.getCursos().size());
    }

    @Test
    @DisplayName("usuario nao autenticado nao cadastra curso")
    void usuarioNaoAutenticadoNaoCadastraCurso() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> cursoController.cadastrarCurso("Computacao", "CC"));

        assertEquals("Apenas administradores podem cadastrar cursos.", ex.getMessage());
        assertTrue(cursoController.getCursos().isEmpty());
    }

    @Test
    @DisplayName("usuario sem perfil administrador nao cadastra curso")
    void usuarioSemPerfilAdministradorNaoCadastraCurso() {
        autenticacaoController.login("2026001", SENHA);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> cursoController.cadastrarCurso("Computacao", "CC"));

        assertEquals("Apenas administradores podem cadastrar cursos.", ex.getMessage());
        assertTrue(cursoController.getCursos().isEmpty());
    }

    @Test
    @DisplayName("nao permite nome duplicado")
    void naoPermiteNomeDuplicado() {
        autenticacaoController.login("0001", SENHA);
        cursoController.cadastrarCurso("Computacao", "CC");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> cursoController.cadastrarCurso(" computacao ", "ES"));

        assertEquals("Ja existe curso cadastrado com esse nome.", removerAcentos(ex.getMessage()));
        assertEquals(1, cursoController.getCursos().size());
    }

    @Test
    @DisplayName("nao permite codigo duplicado")
    void naoPermiteCodigoDuplicado() {
        autenticacaoController.login("0001", SENHA);
        cursoController.cadastrarCurso("Computacao", "CC");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> cursoController.cadastrarCurso("Engenharia de Software", "cc"));

        assertEquals("Ja existe curso cadastrado com esse codigo.", removerAcentos(ex.getMessage()));
        assertEquals(1, cursoController.getCursos().size());
    }

    @Test
    @DisplayName("nome nulo nao cadastra curso")
    void nomeNuloNaoCadastraCurso() {
        autenticacaoController.login("0001", SENHA);

        assertThrows(
                IllegalArgumentException.class,
                () -> cursoController.cadastrarCurso(null, "CC"));

        assertTrue(cursoController.getCursos().isEmpty());
    }

    @Test
    @DisplayName("nome em branco nao cadastra curso")
    void nomeEmBrancoNaoCadastraCurso() {
        autenticacaoController.login("0001", SENHA);

        assertThrows(
                IllegalArgumentException.class,
                () -> cursoController.cadastrarCurso("   ", "CC"));

        assertTrue(cursoController.getCursos().isEmpty());
    }

    @Test
    @DisplayName("getCursos retorna lista imutavel")
    void getCursosRetornaListaImutavel() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> cursoController.getCursos().add(new Curso("Outro")));
    }

    @Test
    @DisplayName("construtor copia lista inicial de cursos")
    void construtorCopiaListaInicialDeCursos() {
        Curso existente = new Curso("Curso Existente");
        List<Curso> listaInicial = new java.util.ArrayList<>(List.of(existente));

        CursoController controller = new CursoController(autenticacaoController, listaInicial);
        listaInicial.clear();

        assertEquals(List.of(existente), controller.getCursos());
    }

    @Test
    @DisplayName("construtor rejeita dependencias nulas")
    void construtorRejeitaDependenciasNulas() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new CursoController(null, List.of())),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new CursoController(autenticacaoController, null)));
    }

    private static String removerAcentos(String texto) {
        return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
