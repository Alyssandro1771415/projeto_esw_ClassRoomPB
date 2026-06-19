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

@DisplayName("RF24 - Chamar Automaticamente o Próximo Aluno da Lista de Espera")
class ListaEsperaRf24Test {

  private static final String SENHA = "senha";

  private Usuario alunoConfirmado;
  private Usuario primeiroAluno;
  private Usuario segundoAluno;
  private Usuario terceiroAluno;
  private Usuario coordenador;
  private Usuario professor;
  private AutenticacaoController autenticacaoController;
  private PeriodoLetivo periodoAtivo;
  private Disciplina disciplina;
  private Turma turma;

  @BeforeEach
  void setUp() {
    alunoConfirmado = criarAluno("aluno-confirmado", "A000", "confirmado@classroompb.com");
    primeiroAluno = criarAluno("aluno-1", "A001", "primeiro@classroompb.com");
    segundoAluno = criarAluno("aluno-2", "A002", "segundo@classroompb.com");
    terceiroAluno = criarAluno("aluno-3", "A003", "terceiro@classroompb.com");
    coordenador =
        new Usuario(
            "coord-1",
            PerfilUsuario.COORDENADOR,
            "Coordenador",
            "C001",
            "coord@classroompb.com",
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
    autenticacaoController =
        new AutenticacaoController(
            List.of(
                alunoConfirmado,
                primeiroAluno,
                segundoAluno,
                terceiroAluno,
                coordenador,
                professor));
    periodoAtivo = new PeriodoLetivo("periodo-1", "2026.1", true);
    disciplina =
        new Disciplina("disc-1", "ESW101", "Projeto de Software", 60, 4, "curso-1", List.of());
    turma = criarTurma("turma-1", 1, LocalDate.now().plusDays(30));
  }

  @Test
  @DisplayName("RF24: cancelamento de confirmada chama automaticamente o próximo da espera")
  void cancelamentoChamaAutomaticamenteProximoDaEspera() {
    Matricula confirmada =
        new Matricula("mat-1", alunoConfirmado.getId(), turma.getId(), StatusMatricula.CONFIRMADA);
    Matricula espera =
        new Matricula("mat-2", primeiroAluno.getId(), turma.getId(), StatusMatricula.EM_ESPERA);
    MatriculaController controller = criarController(List.of(confirmada, espera));
    autenticacaoController.login(alunoConfirmado.getMatricula(), SENHA);

    controller.cancelarMatricula(confirmada.getId());

    assertAll(
        () -> assertEquals(StatusMatricula.CONFIRMADA, espera.getStatus()),
        () -> assertTrue(controller.consultarListaEspera(turma.getId()).isEmpty()));
  }

  @Test
  @DisplayName("RF24: preenche múltiplas vagas ao aumentar limite da turma")
  void preencheMultiplasVagasQuandoLimiteAumenta() {
    Turma turmaComDuasVagas = criarTurma("turma-2", 3, LocalDate.now().plusDays(30));
    Matricula confirmada =
        new Matricula("mat-1", alunoConfirmado.getId(), turmaComDuasVagas.getId());
    Matricula espera1 =
        new Matricula(
            "mat-2", primeiroAluno.getId(), turmaComDuasVagas.getId(), StatusMatricula.EM_ESPERA);
    Matricula espera2 =
        new Matricula(
            "mat-3", segundoAluno.getId(), turmaComDuasVagas.getId(), StatusMatricula.EM_ESPERA);
    MatriculaController controller =
        new MatriculaController(
            autenticacaoController,
            List.of(confirmada, espera1, espera2),
            List.of(turmaComDuasVagas),
            List.of(periodoAtivo),
            List.of(disciplina));

    List<Matricula> promovidos =
        controller.processarChamadaAutomaticaListaEspera(turmaComDuasVagas.getId());

    assertAll(
        () -> assertEquals(2, promovidos.size()),
        () -> assertEquals(StatusMatricula.CONFIRMADA, espera1.getStatus()),
        () -> assertEquals(StatusMatricula.CONFIRMADA, espera2.getStatus()),
        () -> assertTrue(controller.consultarListaEspera(turmaComDuasVagas.getId()).isEmpty()));
  }

  @Test
  @DisplayName("RF24: pula aluno inelegível e chama o próximo elegível")
  void pulaAlunoInelegivelEChamaProximoElegivel() {
    Turma turmaConflito =
        new Turma(
            "turma-conflito",
            disciplina.getId(),
            periodoAtivo.getId(),
            professor.getId(),
            1,
            "Sala 102",
            LocalDate.now().plusDays(30),
            List.of(
                new BlocoHorario(
                    DayOfWeek.MONDAY, LocalTime.parse("08:00"), LocalTime.parse("10:00"))),
            false);
    Turma turmaLotada =
        new Turma(
            "turma-lotada",
            disciplina.getId(),
            periodoAtivo.getId(),
            professor.getId(),
            1,
            "Sala 101",
            LocalDate.now().plusDays(30),
            List.of(
                new BlocoHorario(
                    DayOfWeek.MONDAY, LocalTime.parse("09:00"), LocalTime.parse("11:00"))),
            false);

    Matricula matriculaConflito =
        new Matricula("mat-conf", primeiroAluno.getId(), turmaConflito.getId());
    Matricula confirmada =
        new Matricula(
            "mat-1", alunoConfirmado.getId(), turmaLotada.getId(), StatusMatricula.CONFIRMADA);
    Matricula esperaInelegivel =
        new Matricula(
            "mat-2", primeiroAluno.getId(), turmaLotada.getId(), StatusMatricula.EM_ESPERA);
    Matricula esperaElegivel =
        new Matricula(
            "mat-3", segundoAluno.getId(), turmaLotada.getId(), StatusMatricula.EM_ESPERA);

    MatriculaController controller =
        new MatriculaController(
            autenticacaoController,
            List.of(matriculaConflito, confirmada, esperaInelegivel, esperaElegivel),
            List.of(turmaConflito, turmaLotada),
            List.of(periodoAtivo),
            List.of(disciplina));

    autenticacaoController.login(alunoConfirmado.getMatricula(), SENHA);
    controller.cancelarMatricula(confirmada.getId());

    assertAll(
        () -> assertEquals(StatusMatricula.EM_ESPERA, esperaInelegivel.getStatus()),
        () -> assertEquals(StatusMatricula.CONFIRMADA, esperaElegivel.getStatus()),
        () ->
            assertEquals(
                List.of(esperaInelegivel), controller.consultarListaEspera(turmaLotada.getId())));
  }

  @Test
  @DisplayName("RF24: coordenador aciona chamada manual com sucesso")
  void coordenadorAcionaChamadaManualComSucesso() {
    Turma turmaComDuasVagas = criarTurma("turma-3", 2, LocalDate.now().plusDays(30));
    Matricula confirmada =
        new Matricula("mat-1", alunoConfirmado.getId(), turmaComDuasVagas.getId());
    Matricula espera =
        new Matricula(
            "mat-2", primeiroAluno.getId(), turmaComDuasVagas.getId(), StatusMatricula.EM_ESPERA);
    MatriculaController controller =
        new MatriculaController(
            autenticacaoController,
            List.of(confirmada, espera),
            List.of(turmaComDuasVagas),
            List.of(periodoAtivo),
            List.of(disciplina));
    autenticacaoController.login(coordenador.getMatricula(), SENHA);

    List<Matricula> promovidos =
        controller.chamarProximosAlunosListaEsperaManualmente(turmaComDuasVagas.getId());

    assertAll(
        () -> assertEquals(1, promovidos.size()),
        () -> assertEquals(espera, promovidos.get(0)),
        () -> assertEquals(StatusMatricula.CONFIRMADA, espera.getStatus()));
  }

  @Test
  @DisplayName("RF24: chamada manual sem vagas disponíveis é bloqueada")
  void chamadaManualSemVagasDisponiveisEBloqueada() {
    Matricula confirmada =
        new Matricula("mat-1", alunoConfirmado.getId(), turma.getId(), StatusMatricula.CONFIRMADA);
    Matricula espera =
        new Matricula("mat-2", primeiroAluno.getId(), turma.getId(), StatusMatricula.EM_ESPERA);
    MatriculaController controller = criarController(List.of(confirmada, espera));
    autenticacaoController.login(coordenador.getMatricula(), SENHA);

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.chamarProximosAlunosListaEsperaManualmente(turma.getId()));
  }

  @Test
  @DisplayName("RF24: chamada manual sem alunos elegíveis é bloqueada")
  void chamadaManualSemAlunosElegiveisEBloqueada() {
    Turma turmaComDuasVagas = criarTurma("turma-4", 2, LocalDate.now().plusDays(30));
    Matricula confirmada =
        new Matricula("mat-1", alunoConfirmado.getId(), turmaComDuasVagas.getId());
    MatriculaController controller =
        new MatriculaController(
            autenticacaoController,
            List.of(confirmada),
            List.of(turmaComDuasVagas),
            List.of(periodoAtivo),
            List.of(disciplina));
    autenticacaoController.login(coordenador.getMatricula(), SENHA);

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.chamarProximosAlunosListaEsperaManualmente(turmaComDuasVagas.getId()));
  }

  @Test
  @DisplayName("RF24: apenas coordenador pode acionar chamada manual")
  void apenasCoordenadorPodeAcionarChamadaManual() {
    Matricula confirmada =
        new Matricula("mat-1", alunoConfirmado.getId(), turma.getId(), StatusMatricula.CONFIRMADA);
    Matricula espera =
        new Matricula("mat-2", primeiroAluno.getId(), turma.getId(), StatusMatricula.EM_ESPERA);
    MatriculaController controller = criarController(List.of(confirmada, espera));

    autenticacaoController.login(professor.getMatricula(), SENHA);
    assertThrows(
        IllegalArgumentException.class,
        () -> controller.chamarProximosAlunosListaEsperaManualmente(turma.getId()));

    autenticacaoController.login(primeiroAluno.getMatricula(), SENHA);
    assertThrows(
        IllegalArgumentException.class,
        () -> controller.chamarProximosAlunosListaEsperaManualmente(turma.getId()));
  }

  private Usuario criarAluno(String id, String matricula, String email) {
    return new Usuario(
        id, PerfilUsuario.ALUNO, "Aluno " + matricula, matricula, email, SENHA, true);
  }

  private Turma criarTurma(String id, int limiteVagas, LocalDate dataInicio) {
    return new Turma(
        id,
        disciplina.getId(),
        periodoAtivo.getId(),
        professor.getId(),
        limiteVagas,
        "Sala 101",
        dataInicio,
        List.of(
            new BlocoHorario(DayOfWeek.MONDAY, LocalTime.parse("08:00"), LocalTime.parse("10:00"))),
        false);
  }

  private MatriculaController criarController(List<Matricula> matriculas) {
    return new MatriculaController(
        autenticacaoController,
        matriculas,
        List.of(turma),
        List.of(periodoAtivo),
        List.of(disciplina));
  }
}
