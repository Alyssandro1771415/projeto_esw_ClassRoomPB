package pb.classroom.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pb.classroom.model.Administrador;
import pb.classroom.model.Coordenador;
import pb.classroom.model.Disciplina;
import pb.classroom.model.Professor;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DisciplinaController - cadastro de disciplinas")
class DisciplinaControllerTest {

    private static final String SENHA = "senha";
    private static final String ID_CURSO = "curso-cc";

    private AutenticacaoController autenticacaoController;
    private DisciplinaController disciplinaController;

    @BeforeEach
    void setUp() {
        autenticacaoController = new AutenticacaoController(List.of(
                new Coordenador("C001", "coord@classroompb.com", SENHA),
                new Administrador("0001", "admin@classroompb.com", SENHA),
                new Professor("P001", "prof@classroompb.com", SENHA)));
        disciplinaController = new DisciplinaController(autenticacaoController, List.of());
    }

    @Test
    @DisplayName("coordenador autenticado cadastra disciplina")
    void coordenadorAutenticadoCadastraDisciplina() {
        autenticacaoController.login("C001", SENHA);

        Disciplina disciplina = disciplinaController.cadastrarDisciplina(
                "ESW101",
                "Engenharia de Software",
                60,
                4,
                ID_CURSO,
                List.of());

        assertAll(
                () -> assertEquals("ESW101", disciplina.getCodigo()),
                () -> assertEquals("Engenharia de Software", disciplina.getNome()),
                () -> assertEquals(60, disciplina.getCargaHoraria()),
                () -> assertEquals(4, disciplina.getCreditos()),
                () -> assertEquals(ID_CURSO, disciplina.getIdCurso()),
                () -> assertTrue(disciplina.getPreRequisitosIds().isEmpty()),
                () -> assertTrue(disciplinaController.getDisciplinas().contains(disciplina)));
    }

    @Test
    @DisplayName("cadastro remove espacos dos campos textuais")
    void cadastroRemoveEspacosDosCamposTextuais() {
        autenticacaoController.login("C001", SENHA);

        Disciplina disciplina = disciplinaController.cadastrarDisciplina(
                "  ESW102  ",
                "  Projeto de Software  ",
                80,
                5,
                "  curso-es  ",
                null);

        assertAll(
                () -> assertEquals("ESW102", disciplina.getCodigo()),
                () -> assertEquals("Projeto de Software", disciplina.getNome()),
                () -> assertEquals("curso-es", disciplina.getIdCurso()),
                () -> assertTrue(disciplina.getPreRequisitosIds().isEmpty()));
    }

    @Test
    @DisplayName("cadastro aceita pre-requisito existente")
    void cadastroAceitaPreRequisitoExistente() {
        Disciplina preRequisito = new Disciplina(
                "disc-mat",
                "MAT101",
                "Matematica Basica",
                60,
                4,
                ID_CURSO,
                List.of());
        disciplinaController = new DisciplinaController(autenticacaoController, List.of(preRequisito));
        autenticacaoController.login("C001", SENHA);

        Disciplina disciplina = disciplinaController.cadastrarDisciplina(
                "ESW201",
                "Arquitetura de Software",
                60,
                4,
                ID_CURSO,
                List.of(" disc-mat "));

        assertEquals(List.of("disc-mat"), disciplina.getPreRequisitosIds());
        assertEquals(2, disciplinaController.getDisciplinas().size());
    }

    @Test
    @DisplayName("usuario nao autenticado nao cadastra disciplina")
    void usuarioNaoAutenticadoNaoCadastraDisciplina() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> disciplinaController.cadastrarDisciplina("ESW101", "Engenharia", 60, 4, ID_CURSO, List.of()));

        assertEquals("Apenas coordenadores podem cadastrar disciplinas.", ex.getMessage());
        assertTrue(disciplinaController.getDisciplinas().isEmpty());
    }

    @Test
    @DisplayName("usuario sem perfil coordenador nao cadastra disciplina")
    void usuarioSemPerfilCoordenadorNaoCadastraDisciplina() {
        autenticacaoController.login("0001", SENHA);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> disciplinaController.cadastrarDisciplina("ESW101", "Engenharia", 60, 4, ID_CURSO, List.of()));

        assertEquals("Apenas coordenadores podem cadastrar disciplinas.", ex.getMessage());
        assertTrue(disciplinaController.getDisciplinas().isEmpty());
    }

    @Test
    @DisplayName("nao permite codigo duplicado")
    void naoPermiteCodigoDuplicado() {
        autenticacaoController.login("C001", SENHA);
        disciplinaController.cadastrarDisciplina("ESW101", "Engenharia", 60, 4, ID_CURSO, List.of());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> disciplinaController.cadastrarDisciplina(
                        " esw101 ",
                        "Projeto de Software",
                        60,
                        4,
                        ID_CURSO,
                        List.of()));

        assertEquals("Ja existe disciplina cadastrada com esse codigo.", removerAcentos(ex.getMessage()));
        assertEquals(1, disciplinaController.getDisciplinas().size());
    }

    @Test
    @DisplayName("codigo nulo nao cadastra disciplina")
    void codigoNuloNaoCadastraDisciplina() {
        autenticacaoController.login("C001", SENHA);

        assertThrows(
                IllegalArgumentException.class,
                () -> disciplinaController.cadastrarDisciplina(null, "Engenharia", 60, 4, ID_CURSO, List.of()));

        assertTrue(disciplinaController.getDisciplinas().isEmpty());
    }

    @Test
    @DisplayName("codigo em branco nao cadastra disciplina")
    void codigoEmBrancoNaoCadastraDisciplina() {
        autenticacaoController.login("C001", SENHA);

        assertThrows(
                IllegalArgumentException.class,
                () -> disciplinaController.cadastrarDisciplina("   ", "Engenharia", 60, 4, ID_CURSO, List.of()));

        assertTrue(disciplinaController.getDisciplinas().isEmpty());
    }

    @Test
    @DisplayName("campos obrigatorios da disciplina sao validados")
    void camposObrigatoriosDaDisciplinaSaoValidados() {
        autenticacaoController.login("C001", SENHA);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> disciplinaController.cadastrarDisciplina("ESW101", null, 60, 4, ID_CURSO, List.of())),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> disciplinaController.cadastrarDisciplina("ESW101", "   ", 60, 4, ID_CURSO, List.of())),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> disciplinaController.cadastrarDisciplina("ESW101", "Engenharia", 0, 4, ID_CURSO,
                                List.of())),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> disciplinaController.cadastrarDisciplina("ESW101", "Engenharia", 60, 0, ID_CURSO,
                                List.of())),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> disciplinaController.cadastrarDisciplina("ESW101", "Engenharia", 60, 4, null,
                                List.of())),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> disciplinaController.cadastrarDisciplina("ESW101", "Engenharia", 60, 4, "   ",
                                List.of())));
        assertTrue(disciplinaController.getDisciplinas().isEmpty());
    }

    @Test
    @DisplayName("pre-requisito inexistente nao cadastra disciplina")
    void preRequisitoInexistenteNaoCadastraDisciplina() {
        autenticacaoController.login("C001", SENHA);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> disciplinaController.cadastrarDisciplina(
                        "ESW201",
                        "Arquitetura",
                        60,
                        4,
                        ID_CURSO,
                        List.of("disc-inexistente")));

        assertEquals("Pre-requisito nao encontrado: disc-inexistente", removerAcentos(ex.getMessage()));
        assertTrue(disciplinaController.getDisciplinas().isEmpty());
    }

    @Test
    @DisplayName("pre-requisito duplicado nao cadastra disciplina")
    void preRequisitoDuplicadoNaoCadastraDisciplina() {
        Disciplina preRequisito = new Disciplina(
                "disc-mat",
                "MAT101",
                "Matematica Basica",
                60,
                4,
                ID_CURSO,
                List.of());
        disciplinaController = new DisciplinaController(autenticacaoController, List.of(preRequisito));
        autenticacaoController.login("C001", SENHA);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> disciplinaController.cadastrarDisciplina(
                        "ESW201",
                        "Arquitetura",
                        60,
                        4,
                        ID_CURSO,
                        List.of("disc-mat", "disc-mat")));

        assertEquals("pre-requisito duplicado: disc-mat", removerAcentos(ex.getMessage()));
        assertEquals(1, disciplinaController.getDisciplinas().size());
    }

    @Test
    @DisplayName("getDisciplinas retorna lista imutavel")
    void getDisciplinasRetornaListaImutavel() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> disciplinaController.getDisciplinas().add(
                        new Disciplina("ESW101", "Engenharia", 60, 4, ID_CURSO)));
    }

    @Test
    @DisplayName("construtor copia lista inicial de disciplinas")
    void construtorCopiaListaInicialDeDisciplinas() {
        Disciplina existente = new Disciplina("ESW101", "Engenharia", 60, 4, ID_CURSO);
        List<Disciplina> listaInicial = new ArrayList<>(List.of(existente));

        DisciplinaController controller = new DisciplinaController(autenticacaoController, listaInicial);
        listaInicial.clear();

        assertEquals(List.of(existente), controller.getDisciplinas());
    }

    @Test
    @DisplayName("construtor rejeita dependencias nulas")
    void construtorRejeitaDependenciasNulas() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new DisciplinaController(null, List.of())),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new DisciplinaController(autenticacaoController, null)));
    }

    private static String removerAcentos(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
