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
import pb.classroom.model.Matricula;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.PeriodoLetivo;
import pb.classroom.model.StatusMatricula;
import pb.classroom.model.Turma;
import pb.classroom.model.Usuario;

@DisplayName("MatriculaController - RF16 a RF22")
class MatriculaRequisitosRf16aRf22Test {

  private static final String SENHA = "senha";

  private Usuario aluno;
  private Usuario outroAluno;
  private AutenticacaoController autenticacaoController;
  private PeriodoLetivo periodoAtivo;
  private Disciplina disciplina;

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
    outroAluno =
        new Usuario(
            "aluno-2",
            PerfilUsuario.ALUNO,
            "Outro Aluno",
            "A002",
            "outro@classroompb.com",
            SENHA,
            true);
    autenticacaoController = new AutenticacaoController(List.of(aluno, outroAluno));
    autenticacaoController.login("A001", SENHA);
    periodoAtivo = new PeriodoLetivo("periodo-1", "2026.1", true);
    disciplina =
        new Disciplina("disc-1", "ESW101", "Projeto de Software", 60, 4, "curso-1", List.of());
  }

  @Test
  @DisplayName("RF16/RF17/RF20: aluno solicita matricula e recebe confirmacao quando ha vaga")
  void alunoSolicitaMatriculaERecebeConfirmacaoQuandoHaVaga() {
    Turma turma = criarTurma("turma-1", 2, LocalDate.now().plusDays(30));
    MatriculaController controller = criarController(List.of(), List.of(turma));

    Matricula matricula = controller.solicitarMatricula(turma.getId());

    assertAll(
        () -> assertEquals(aluno.getId(), matricula.getIdAluno()),
        () -> assertEquals(turma.getId(), matricula.getIdTurma()),
        () -> assertEquals(StatusMatricula.CONFIRMADA, matricula.getStatus()));
  }

  @Test
  @DisplayName("RF17/RF21: sem vaga registra em lista de espera")
  void semVagaRegistraEmListaDeEspera() {
    Turma turma = criarTurma("turma-1", 1, LocalDate.now().plusDays(30));
    Matricula confirmada = new Matricula("mat-1", outroAluno.getId(), turma.getId());
    MatriculaController controller = criarController(List.of(confirmada), List.of(turma));

    Matricula espera = controller.solicitarMatricula(turma.getId());

    assertAll(
        () -> assertEquals(StatusMatricula.EM_ESPERA, espera.getStatus()),
        () -> assertEquals(List.of(espera), controller.consultarListaEspera(turma.getId())));
  }

  @Test
  @DisplayName("RF22: aluno cancela matricula antes do inicio das aulas")
  void alunoCancelaMatriculaAntesDoInicioDasAulas() {
    Turma turma = criarTurma("turma-1", 2, LocalDate.now().plusDays(30));
    Matricula existente = new Matricula("mat-1", aluno.getId(), turma.getId());
    MatriculaController controller = criarController(List.of(existente), List.of(turma));

    Matricula cancelada = controller.cancelarMatricula(existente.getId());

    assertAll(
        () -> assertEquals(existente, cancelada),
        () -> assertTrue(controller.getMatriculas().isEmpty()));
  }

  @Test
  @DisplayName("RF22: cancelamento apos inicio das aulas e bloqueado")
  void cancelamentoAposInicioDasAulasEBloqueado() {
    Turma turma = criarTurma("turma-1", 2, LocalDate.now().minusDays(1));
    Matricula existente = new Matricula("mat-1", aluno.getId(), turma.getId());
    MatriculaController controller = criarController(List.of(existente), List.of(turma));

    assertAll(
        () ->
            assertThrows(
                IllegalArgumentException.class,
                () -> controller.cancelarMatricula(existente.getId())),
        () -> assertEquals(List.of(existente), controller.getMatriculas()));
  }

  @Test
  @DisplayName("RF21/RF22: cancelamento de confirmada promove primeiro aluno da espera")
  void cancelamentoDeConfirmadaPromovePrimeiroAlunoDaEspera() {
    Turma turma = criarTurma("turma-1", 1, LocalDate.now().plusDays(30));
    Matricula confirmada =
        new Matricula("mat-1", aluno.getId(), turma.getId(), StatusMatricula.CONFIRMADA);
    Matricula espera =
        new Matricula("mat-2", outroAluno.getId(), turma.getId(), StatusMatricula.EM_ESPERA);
    MatriculaController controller = criarController(List.of(confirmada, espera), List.of(turma));

    controller.cancelarMatricula(confirmada.getId());

    assertAll(
        () -> assertEquals(1, controller.getMatriculas().size()),
        () -> assertEquals(StatusMatricula.CONFIRMADA, espera.getStatus()));
  }

  private MatriculaController criarController(List<Matricula> matriculas, List<Turma> turmas) {
    return new MatriculaController(
        autenticacaoController, matriculas, turmas, List.of(periodoAtivo), List.of(disciplina));
  }

  private Turma criarTurma(String id, int limiteVagas, LocalDate dataInicio) {
    return new Turma(
        id,
        disciplina.getId(),
        periodoAtivo.getId(),
        "prof-1",
        limiteVagas,
        "Sala 101",
        dataInicio,
        List.of(
            new BlocoHorario(DayOfWeek.MONDAY, LocalTime.parse("08:00"), LocalTime.parse("10:00"))),
        false);
  }
}
