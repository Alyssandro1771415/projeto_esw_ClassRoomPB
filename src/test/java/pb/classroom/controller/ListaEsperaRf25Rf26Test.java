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

@DisplayName("RF25/RF26 - Lista de Espera")
class ListaEsperaRf25Rf26Test {

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
    alunoConfirmado =
        new Usuario(
            "aluno-confirmado",
            PerfilUsuario.ALUNO,
            "Aluno Confirmado",
            "A000",
            "confirmado@classroompb.com",
            SENHA,
            true);
    primeiroAluno =
        new Usuario(
            "aluno-1",
            PerfilUsuario.ALUNO,
            "Primeiro Aluno",
            "A001",
            "primeiro@classroompb.com",
            SENHA,
            true);
    segundoAluno =
        new Usuario(
            "aluno-2",
            PerfilUsuario.ALUNO,
            "Segundo Aluno",
            "A002",
            "segundo@classroompb.com",
            SENHA,
            true);
    terceiroAluno =
        new Usuario(
            "aluno-3",
            PerfilUsuario.ALUNO,
            "Terceiro Aluno",
            "A003",
            "terceiro@classroompb.com",
            SENHA,
            true);
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
    turma =
        new Turma(
            "turma-1",
            disciplina.getId(),
            periodoAtivo.getId(),
            professor.getId(),
            1,
            "Sala 101",
            LocalDate.now().plusDays(30),
            List.of(
                new BlocoHorario(
                    DayOfWeek.MONDAY, LocalTime.parse("08:00"), LocalTime.parse("10:00"))),
            false);
  }

  @Test
  @DisplayName("RF25: solicitações em espera mantêm a ordem de solicitação")
  void solicitacoesEmEsperaMantemOrdemDeSolicitacao() {
    MatriculaController controller = criarController(List.of());

    Matricula confirmada = solicitarMatriculaComo(controller, alunoConfirmado);
    Matricula primeira = solicitarMatriculaComo(controller, primeiroAluno);
    Matricula segunda = solicitarMatriculaComo(controller, segundoAluno);
    Matricula terceira = solicitarMatriculaComo(controller, terceiroAluno);

    List<Matricula> listaEspera =
        controller.consultarListaEsperaOrdenadaPorSolicitacao(turma.getId());

    assertAll(
        () -> assertEquals(StatusMatricula.CONFIRMADA, confirmada.getStatus()),
        () -> assertEquals(StatusMatricula.EM_ESPERA, primeira.getStatus()),
        () -> assertEquals(List.of(primeira, segunda, terceira), listaEspera));
  }

  @Test
  @DisplayName("RF25: cancelamento promove o primeiro da espera e preserva a ordem restante")
  void cancelamentoPromovePrimeiroDaEsperaEPreservaOrdemRestante() {
    MatriculaController controller = criarController(List.of());
    Matricula confirmada = solicitarMatriculaComo(controller, alunoConfirmado);
    Matricula primeira = solicitarMatriculaComo(controller, primeiroAluno);
    Matricula segunda = solicitarMatriculaComo(controller, segundoAluno);
    Matricula terceira = solicitarMatriculaComo(controller, terceiroAluno);

    autenticacaoController.login(alunoConfirmado.getMatricula(), SENHA);
    controller.cancelarMatricula(confirmada.getId());

    List<Matricula> listaEspera =
        controller.consultarListaEsperaOrdenadaPorSolicitacao(turma.getId());

    assertAll(
        () -> assertEquals(StatusMatricula.CONFIRMADA, primeira.getStatus()),
        () -> assertEquals(StatusMatricula.EM_ESPERA, segunda.getStatus()),
        () -> assertEquals(List.of(segunda, terceira), listaEspera));
  }

  @Test
  @DisplayName("RF26: coordenador visualiza lista de espera da turma")
  void coordenadorVisualizaListaEsperaDaTurma() {
    Matricula confirmada =
        new Matricula("mat-1", alunoConfirmado.getId(), turma.getId(), StatusMatricula.CONFIRMADA);
    Matricula primeira =
        new Matricula("mat-2", primeiroAluno.getId(), turma.getId(), StatusMatricula.EM_ESPERA);
    Matricula segunda =
        new Matricula("mat-3", segundoAluno.getId(), turma.getId(), StatusMatricula.EM_ESPERA);
    MatriculaController controller = criarController(List.of(confirmada, primeira, segunda));
    autenticacaoController.login(coordenador.getMatricula(), SENHA);

    List<Matricula> listaEspera = controller.visualizarListaEsperaPorTurma(turma.getId());

    assertEquals(List.of(primeira, segunda), listaEspera);
  }

  @Test
  @DisplayName("RF26: coordenador visualiza lista vazia quando não há espera")
  void coordenadorVisualizaListaVaziaQuandoNaoHaEspera() {
    Matricula confirmada =
        new Matricula("mat-1", alunoConfirmado.getId(), turma.getId(), StatusMatricula.CONFIRMADA);
    MatriculaController controller = criarController(List.of(confirmada));
    autenticacaoController.login(coordenador.getMatricula(), SENHA);

    List<Matricula> listaEspera = controller.visualizarListaEsperaPorTurma(turma.getId());

    assertTrue(listaEspera.isEmpty());
  }

  @Test
  @DisplayName("RF26: apenas coordenador visualiza lista de espera por turma")
  void apenasCoordenadorVisualizaListaEsperaPorTurma() {
    Matricula espera =
        new Matricula("mat-1", primeiroAluno.getId(), turma.getId(), StatusMatricula.EM_ESPERA);
    MatriculaController controller = criarController(List.of(espera));

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.visualizarListaEsperaPorTurma(turma.getId()));

    autenticacaoController.login(primeiroAluno.getMatricula(), SENHA);
    assertThrows(
        IllegalArgumentException.class,
        () -> controller.visualizarListaEsperaPorTurma(turma.getId()));

    autenticacaoController.login(professor.getMatricula(), SENHA);
    assertThrows(
        IllegalArgumentException.class,
        () -> controller.visualizarListaEsperaPorTurma(turma.getId()));
  }

  @Test
  @DisplayName("RF26: turma inexistente gera erro para coordenador")
  void turmaInexistenteGeraErroParaCoordenador() {
    MatriculaController controller = criarController(List.of());
    autenticacaoController.login(coordenador.getMatricula(), SENHA);

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.visualizarListaEsperaPorTurma("turma-inexistente"));
  }

  private Matricula solicitarMatriculaComo(MatriculaController controller, Usuario usuario) {
    autenticacaoController.login(usuario.getMatricula(), SENHA);
    return controller.solicitarMatricula(turma.getId());
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
