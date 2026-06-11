package pb.classroom.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Entidades do dominio (model)")
class EntidadesModeloTest {

  @Nested
  @DisplayName("Curso")
  class CursoTests {

    @Test
    void criaCursoComNome() {
      Curso curso = new Curso("Ciencia da Computacao");
      assertNotNull(curso.getId());
      assertEquals("Ciencia da Computacao", curso.getNome());
      assertNull(curso.getCodigo());
    }

    @Test
    void defineCodigoOpcional() {
      Curso curso = new Curso("id-1", "Engenharia", "ENG");
      assertEquals("ENG", curso.getCodigo());
      curso.setCodigo("  eng-2  ");
      assertEquals("eng-2", curso.getCodigo());
      curso.setCodigo("");
      assertNull(curso.getCodigo());
    }

    @Test
    void rejeitaNomeVazio() {
      assertThrows(IllegalArgumentException.class, () -> new Curso("id", "", null));
    }

    @Test
    void equalsPorId() {
      Curso a = new Curso("mesmo-id", "A", null);
      Curso b = new Curso("mesmo-id", "B", null);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
    }
  }

  @Nested
  @DisplayName("Disciplina")
  class DisciplinaTests {

    @Test
    void criaDisciplinaValida() {
      Disciplina d = new Disciplina("MAT01", "Calculo I", 60, 4, "curso-1");
      assertEquals("MAT01", d.getCodigo());
      assertTrue(d.getPreRequisitosIds().isEmpty());
    }

    @Test
    void aceitaPreRequisitos() {
      Disciplina d = new Disciplina("d1", "ALG01", "Alg", 40, 3, "c1", List.of("pre-1", "pre-2"));
      assertEquals(List.of("pre-1", "pre-2"), d.getPreRequisitosIds());
    }

    @Test
    void rejeitaPreRequisitoDuplicado() {
      assertThrows(
          IllegalArgumentException.class,
          () -> new Disciplina("d1", "X01", "X", 40, 3, "c1", List.of("p1", "p1")));
    }

    @Test
    void rejeitaAutoPreRequisito() {
      assertThrows(
          IllegalArgumentException.class,
          () -> new Disciplina("d1", "X01", "X", 40, 3, "c1", List.of("d1")));
    }

    @Test
    void rejeitaCargaHorariaInvalida() {
      Disciplina d = new Disciplina("d1", "X", 40, 3, "c1");
      assertThrows(IllegalArgumentException.class, () -> d.setCargaHoraria(0));
    }

    @Test
    void atualizaPreRequisitosESetters() {
      Disciplina d = new Disciplina("d1", "COD", "Nome", 40, 3, "c1", List.of());
      d.setPreRequisitosIds(List.of("pre-a"));
      d.setCodigo("COD2");
      d.setNome("Outro");
      assertEquals("COD2", d.getCodigo());
      assertEquals(List.of("pre-a"), d.getPreRequisitosIds());
    }

    @Test
    void equalsPorIdentificador() {
      Disciplina a = new Disciplina("id-1", "A01", "Disc A", 40, 3, "c1", List.of());
      Disciplina b = new Disciplina("id-1", "B01", "Disc B", 50, 4, "c2", List.of());
      assertEquals(a, b);
    }
  }

  @Nested
  @DisplayName("PeriodoLetivo")
  class PeriodoLetivoTests {

    @Test
    void criaPeriodoNoFormatoEsperado() {
      PeriodoLetivo p = new PeriodoLetivo("2026.2");
      assertEquals("2026.2", p.getCodigo());
      assertFalse(p.isAtivo());
    }

    @Test
    void ativaEEncerra() {
      PeriodoLetivo p = new PeriodoLetivo("id", "2026.1", false);
      p.ativar();
      assertTrue(p.isAtivo());
      p.encerrar();
      assertFalse(p.isAtivo());
    }

    @Test
    void rejeitaCodigoInvalido() {
      assertThrows(IllegalArgumentException.class, () -> new PeriodoLetivo("2026-2"));
    }

    @Test
    void equalsPorId() {
      PeriodoLetivo a = new PeriodoLetivo("p1", "2026.1", true);
      PeriodoLetivo b = new PeriodoLetivo("p1", "2026.2", false);
      assertEquals(a, b);
    }
  }

  @Nested
  @DisplayName("BlocoHorario")
  class BlocoHorarioTests {

    @Test
    void criaBlocoValido() {
      BlocoHorario b = new BlocoHorario(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(10, 0));
      assertEquals(DayOfWeek.MONDAY, b.getDiaSemana());
    }

    @Test
    void rejeitaHorarioInvalido() {
      assertThrows(
          IllegalArgumentException.class,
          () -> new BlocoHorario(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(9, 0)));
    }

    @Test
    void equalsConsideraDiaEHorarios() {
      BlocoHorario a =
          new BlocoHorario(DayOfWeek.WEDNESDAY, LocalTime.of(14, 0), LocalTime.of(16, 0));
      BlocoHorario b =
          new BlocoHorario(DayOfWeek.WEDNESDAY, LocalTime.of(14, 0), LocalTime.of(16, 0));
      assertEquals(a, b);
    }
  }

  @Nested
  @DisplayName("Turma")
  class TurmaTests {

    private BlocoHorario blocoPadrao() {
      return new BlocoHorario(DayOfWeek.THURSDAY, LocalTime.of(19, 0), LocalTime.of(21, 0));
    }

    @Test
    void criaTurmaCompleta() {
      Turma t =
          new Turma(
              "disc-1",
              "per-1",
              "prof-1",
              30,
              "Sala 101",
              LocalDate.of(2026, 8, 1),
              List.of(blocoPadrao()));
      assertEquals("disc-1", t.getIdDisciplina());
      assertFalse(t.isCancelada());
    }

    @Test
    void alteraHorarios() {
      Turma t =
          new Turma(
              "t1",
              "d1",
              "p1",
              "prof1",
              25,
              "Sala A",
              LocalDate.now().plusDays(10),
              List.of(blocoPadrao()),
              false);
      BlocoHorario novo =
          new BlocoHorario(DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(10, 0));
      t.setHorarios(List.of(novo));
      assertEquals(1, t.getHorarios().size());
      assertEquals(novo, t.getHorarios().get(0));
    }

    @Test
    void rejeitaLimiteVagasInvalido() {
      Turma t = new Turma("d1", "p1", "prof", 20, "S1", LocalDate.now(), List.of(blocoPadrao()));
      assertThrows(IllegalArgumentException.class, () -> t.setLimiteVagas(0));
    }

    @Test
    void rejeitaHorariosVazios() {
      assertThrows(
          IllegalArgumentException.class,
          () -> new Turma("d1", "p1", "prof", 20, "S1", LocalDate.now(), List.of()));
    }

    @Test
    void marcaCanceladaEEquals() {
      Turma t =
          new Turma(
              "t1", "d1", "p1", "prof", 20, "S1", LocalDate.now(), List.of(blocoPadrao()), false);
      t.setCancelada(true);
      assertTrue(t.isCancelada());
      Turma outra =
          new Turma(
              "t1",
              "d2",
              "p2",
              "prof2",
              10,
              "S2",
              LocalDate.now().plusDays(1),
              List.of(blocoPadrao()),
              true);
      assertEquals(t, outra);
    }
  }

  @Nested
  @DisplayName("Matricula")
  class MatriculaTests {

    @Test
    void criaMatriculaValida() {
      Matricula matricula = new Matricula("mat-1", "aluno-1", "turma-1");

      assertAll(
          () -> assertEquals("mat-1", matricula.getId()),
          () -> assertEquals("aluno-1", matricula.getIdAluno()),
          () -> assertEquals("turma-1", matricula.getIdTurma()));
    }

    @Test
    void rejeitaCamposObrigatoriosEComparaPorId() {
      assertAll(
          () -> assertThrows(IllegalArgumentException.class, () -> new Matricula("", "a1", "t1")),
          () -> assertThrows(IllegalArgumentException.class, () -> new Matricula("m1", null, "t1")),
          () -> assertThrows(IllegalArgumentException.class, () -> new Matricula("m1", "a1", " ")));

      Matricula primeira = new Matricula("m1", "a1", "t1");
      Matricula segunda = new Matricula("m1", "a2", "t2");
      assertEquals(primeira, segunda);
      assertEquals(primeira.hashCode(), segunda.hashCode());
    }
  }

  @Nested
  @DisplayName("Usuario")
  class UsuarioTests {

    @Test
    void validaMatriculaEmailESenha() {
      Usuario u =
          new Usuario(
              "u1",
              PerfilUsuario.PROFESSOR,
              "Prof",
              "2026001",
              "prof@uepb.edu.br",
              "segredo",
              true);
      assertTrue(u.isAtivo());
      u.setAtivo(false);
      assertFalse(u.isAtivo());
    }

    @Test
    void rejeitaEmailVazio() {
      Usuario u = new Usuario(PerfilUsuario.ALUNO, "A", "1", "a@b.com", "s");
      assertThrows(IllegalArgumentException.class, () -> u.setEmail(" "));
    }

    @Test
    void equalsPorId() {
      Usuario a =
          new Usuario("id-x", PerfilUsuario.ADMINISTRADOR, "Admin", "1", "a@b.com", "s", true);
      Usuario b =
          new Usuario("id-x", PerfilUsuario.COORDENADOR, "Outro", "2", "c@d.com", "s2", false);
      assertEquals(a, b);
    }

    @Test
    void rejeitaMatriculaESenhaInvalidas() {
      Usuario u = new Usuario(PerfilUsuario.ALUNO, "A", "1", "a@b.com", "s");
      assertThrows(IllegalArgumentException.class, () -> u.setMatricula(" "));
      assertThrows(IllegalArgumentException.class, () -> u.setSenha(""));
    }
  }
}
