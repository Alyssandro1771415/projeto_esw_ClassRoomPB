package pb.classroom.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pb.classroom.model.Curso;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.Usuario;

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
                new Usuario(PerfilUsuario.ADMINISTRADOR, "Admin Sistema", "0001", "admin@classroompb.com", SENHA),
                new Usuario(PerfilUsuario.ALUNO, "Aluno Teste", "2026001", "aluno@classroompb.com", SENHA)));
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
    @DisplayName("nao permite codigo duplicado")
    void naoPermiteCodigoDuplicado() {
        autenticacaoController.login("0001", SENHA);
        cursoController.cadastrarCurso("Computacao", "CC");

        assertThrows(
                IllegalArgumentException.class,
                () -> cursoController.cadastrarCurso("Engenharia de Software", "cc"));
    }

    @Test
    @DisplayName("getCursos retorna lista imutavel")
    void getCursosRetornaListaImutavel() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> cursoController.getCursos().add(new Curso("Outro")));
    }
}
