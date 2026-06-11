package pb.classroom.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pb.classroom.model.Curso;
import pb.classroom.model.Disciplina;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.Usuario;

@DisplayName("DisciplinaController - cadastro vinculado a curso")
class DisciplinaControllerTest {

  private static final String SENHA = "senha";

  private AutenticacaoController autenticacaoController;
  private DisciplinaController disciplinaController;
  private Curso curso;

  @BeforeEach
  void setUp() {
    curso = new Curso("curso-1", "Engenharia de Software", "ESW");
    autenticacaoController =
        new AutenticacaoController(
            List.of(
                new Usuario(
                    PerfilUsuario.COORDENADOR,
                    "Coord Curso",
                    "C001",
                    "coord@classroompb.com",
                    SENHA),
                new Usuario(
                    PerfilUsuario.ALUNO, "Aluno Teste", "A001", "aluno@classroompb.com", SENHA)));
    disciplinaController =
        new DisciplinaController(autenticacaoController, List.of(), List.of(curso));
  }

  @Test
  @DisplayName("coordenador cadastra disciplina para curso existente")
  void coordenadorCadastraDisciplinaParaCursoExistente() {
    autenticacaoController.login("C001", SENHA);

    Disciplina disciplina =
        disciplinaController.cadastrarDisciplina(
            "ESW101", "Projeto de Software", 60, 4, curso.getId(), List.of());

    assertAll(
        () -> assertEquals(curso.getId(), disciplina.getIdCurso()),
        () -> assertTrue(disciplinaController.getDisciplinas().contains(disciplina)));
  }

  @Test
  @DisplayName("nao cadastra disciplina para curso inexistente")
  void naoCadastraDisciplinaParaCursoInexistente() {
    autenticacaoController.login("C001", SENHA);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                disciplinaController.cadastrarDisciplina(
                    "ESW102", "Teste de Software", 60, 4, "curso-inexistente", List.of()));

    assertEquals("Curso não encontrado: curso-inexistente", ex.getMessage());
    assertTrue(disciplinaController.getDisciplinas().isEmpty());
  }

  @Test
  @DisplayName("usuario sem perfil coordenador nao cadastra disciplina")
  void usuarioSemPerfilCoordenadorNaoCadastraDisciplina() {
    autenticacaoController.login("A001", SENHA);

    assertThrows(
        IllegalArgumentException.class,
        () ->
            disciplinaController.cadastrarDisciplina(
                "ESW103", "Arquitetura", 60, 4, curso.getId(), List.of()));
  }

  @Test
  @DisplayName("reconhece curso adicionado apos criar controller")
  void reconheceCursoAdicionadoAposCriarController() {
    List<Curso> cursos = new ArrayList<>();
    disciplinaController = new DisciplinaController(autenticacaoController, List.of(), cursos);
    Curso novoCurso = new Curso("curso-2", "Ciencia da Computacao", "CC");
    cursos.add(novoCurso);
    autenticacaoController.login("C001", SENHA);

    Disciplina disciplina =
        disciplinaController.cadastrarDisciplina(
            "CC101", "Algoritmos", 60, 4, novoCurso.getId(), List.of());

    assertEquals(novoCurso.getId(), disciplina.getIdCurso());
  }
}
