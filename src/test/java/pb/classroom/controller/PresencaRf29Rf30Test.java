package pb.classroom.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pb.classroom.model.BlocoHorario;
import pb.classroom.model.Disciplina;
import pb.classroom.model.FrequenciaDisciplinaAluno;
import pb.classroom.model.Matricula;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.PeriodoLetivo;
import pb.classroom.model.RegistroPresenca;
import pb.classroom.model.StatusMatricula;
import pb.classroom.model.StatusPresenca;
import pb.classroom.model.Turma;
import pb.classroom.model.Usuario;

@DisplayName("RF29/RF30 - Frequencia por disciplina e alerta")
class PresencaRf29Rf30Test {

  private static final String SENHA = "senha";

  private Usuario aluno;
  private Usuario professor;
  private AutenticacaoController autenticacaoController;
  private Turma turmaA;
  private Turma turmaB;
  private Turma turmaOutraDisciplina;
  private Matricula matriculaA;
  private Matricula matriculaB;
  private Matricula matriculaOutraDisciplina;

  @BeforeEach
  void setUp() {
    aluno =
        new Usuario(
            "aluno-1",
            PerfilUsuario.ALUNO,
            "Aluno Teste",
            "A001",
            "aluno@classroompb.com",
            SENHA,
            true);
    professor =
        new Usuario(
            "prof-1",
            PerfilUsuario.PROFESSOR,
            "Professor",
            "P001",
            "prof@classroompb.com",
            SENHA,
            true);
    autenticacaoController = new AutenticacaoController(List.of(aluno, professor));

    PeriodoLetivo periodo = new PeriodoLetivo("periodo-1", "2026.1", true);
    Disciplina disciplina =
        new Disciplina("disc-1", "ESW101", "Projeto de Software", 60, 4, "curso-1", List.of());
    Disciplina outraDisciplina =
        new Disciplina("disc-2", "ESW102", "Testes", 60, 4, "curso-1", List.of());
    turmaA = criarTurma("turma-1", disciplina.getId(), periodo.getId(), DayOfWeek.MONDAY);
    turmaB = criarTurma("turma-2", disciplina.getId(), periodo.getId(), DayOfWeek.WEDNESDAY);
    turmaOutraDisciplina =
        criarTurma("turma-3", outraDisciplina.getId(), periodo.getId(), DayOfWeek.FRIDAY);
    matriculaA = new Matricula("mat-1", aluno.getId(), turmaA.getId(), StatusMatricula.CONFIRMADA);
    matriculaB = new Matricula("mat-2", aluno.getId(), turmaB.getId(), StatusMatricula.CONFIRMADA);
    matriculaOutraDisciplina =
        new Matricula(
            "mat-3", aluno.getId(), turmaOutraDisciplina.getId(), StatusMatricula.CONFIRMADA);
  }

  @Test
  @DisplayName("RF29: aluno consulta frequencia agregada por disciplina")
  void alunoConsultaFrequenciaAgregadaPorDisciplina() {
    PresencaController controller =
        criarController(
            List.of(
                presenca("reg-1", turmaA, StatusPresenca.PRESENTE),
                presenca("reg-2", turmaA, StatusPresenca.FALTA),
                presenca("reg-3", turmaB, StatusPresenca.PRESENTE),
                presenca("reg-4", turmaOutraDisciplina, StatusPresenca.FALTA)));
    autenticacaoController.login(aluno.getMatricula(), SENHA);

    FrequenciaDisciplinaAluno frequencia =
        controller.consultarMinhaFrequenciaPorDisciplina("disc-1");

    assertAll(
        () -> assertEquals(aluno.getId(), frequencia.getIdAluno()),
        () -> assertEquals("disc-1", frequencia.getIdDisciplina()),
        () -> assertEquals(3, frequencia.getTotalAulasRegistradas()),
        () -> assertEquals(2, frequencia.getTotalPresencas()),
        () -> assertEquals(66.67, frequencia.getPercentual(), 0.1));
  }

  @Test
  @DisplayName("RF30: sistema alerta quando aluno esta abaixo do minimo exigido")
  void alertaQuandoAlunoAbaixoDoMinimo() {
    PresencaController controller =
        criarController(
            List.of(
                presenca("reg-1", turmaA, StatusPresenca.PRESENTE),
                presenca("reg-2", turmaA, StatusPresenca.FALTA)));
    autenticacaoController.login(aluno.getMatricula(), SENHA);

    FrequenciaDisciplinaAluno frequencia =
        controller.consultarMinhaFrequenciaPorDisciplina("disc-1");

    assertTrue(frequencia.isAbaixoDoMinimoExigido());
    assertTrue(frequencia.getMensagemAlerta().contains("ALERTA"));
  }

  @Test
  @DisplayName("RF29: aluno nao consulta disciplina sem matricula confirmada")
  void alunoNaoConsultaDisciplinaSemMatriculaConfirmada() {
    Matricula emEspera =
        new Matricula("mat-4", aluno.getId(), turmaA.getId(), StatusMatricula.EM_ESPERA);
    PresencaController controller =
        new PresencaController(
            autenticacaoController, List.of(), List.of(turmaA), List.of(emEspera));
    autenticacaoController.login(aluno.getMatricula(), SENHA);

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.consultarMinhaFrequenciaPorDisciplina("disc-1"));
  }

  @Test
  @DisplayName("RF29: aluno lista frequencias de todas as disciplinas confirmadas")
  void alunoListaFrequenciasDeTodasDisciplinasConfirmadas() {
    PresencaController controller = criarController(List.of());
    autenticacaoController.login(aluno.getMatricula(), SENHA);

    List<FrequenciaDisciplinaAluno> frequencias =
        controller.consultarMinhasFrequenciasPorDisciplina();

    assertAll(
        () -> assertEquals(2, frequencias.size()),
        () ->
            assertTrue(
                frequencias.stream()
                    .anyMatch(frequencia -> frequencia.getIdDisciplina().equals("disc-1"))),
        () ->
            assertTrue(
                frequencias.stream()
                    .anyMatch(frequencia -> frequencia.getIdDisciplina().equals("disc-2"))));
  }

  private Turma criarTurma(String id, String idDisciplina, String idPeriodo, DayOfWeek dia) {
    return new Turma(
        id,
        idDisciplina,
        idPeriodo,
        professor.getId(),
        10,
        "Sala 101",
        LocalDate.of(2026, 3, 1),
        List.of(new BlocoHorario(dia, LocalTime.parse("08:00"), LocalTime.parse("10:00"))),
        false);
  }

  private RegistroPresenca presenca(String id, Turma turma, StatusPresenca status) {
    return new RegistroPresenca(id, turma.getId(), aluno.getId(), LocalDate.of(2026, 3, 2), status);
  }

  private PresencaController criarController(List<RegistroPresenca> registros) {
    return new PresencaController(
        autenticacaoController,
        registros,
        List.of(turmaA, turmaB, turmaOutraDisciplina),
        List.of(matriculaA, matriculaB, matriculaOutraDisciplina));
  }
}
