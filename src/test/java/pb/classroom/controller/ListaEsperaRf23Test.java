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

@DisplayName("RF23 - Manter Lista de Espera por Turma")
class ListaEsperaRf23Test {

  private static final String SENHA = "senha";

  private Usuario aluno;
  private Usuario outroAluno;
  private Usuario terceiroAluno;
  private Usuario coordenador;
  private Usuario professor;
  private Usuario outroProfessor;
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
            "Coordenador Teste",
            "C001",
            "coord@classroompb.com",
            SENHA,
            true);
    professor =
        new Usuario(
            "prof-1",
            PerfilUsuario.PROFESSOR,
            "Professor Teste",
            "P001",
            "prof@classroompb.com",
            SENHA,
            true);
    outroProfessor =
        new Usuario(
            "prof-2",
            PerfilUsuario.PROFESSOR,
            "Outro Professor",
            "P002",
            "outroprof@classroompb.com",
            SENHA,
            true);
    autenticacaoController =
        new AutenticacaoController(
            List.of(aluno, outroAluno, terceiroAluno, coordenador, professor, outroProfessor));
    periodoAtivo = new PeriodoLetivo("periodo-1", "2026.1", true);
    disciplina =
        new Disciplina("disc-1", "ESW101", "Projeto de Software", 60, 4, "curso-1", List.of());
  }

  @Test
  @DisplayName("RF23: coordenador consulta lista de espera com alunos")
  void coordenadorConsultaListaEsperaComAlunos() {
    autenticacaoController.login("C001", SENHA);
    Turma turma = criarTurma("turma-1", 1);
    Matricula confirmada =
        new Matricula("mat-1", aluno.getId(), turma.getId(), StatusMatricula.CONFIRMADA);
    Matricula espera1 =
        new Matricula("mat-2", outroAluno.getId(), turma.getId(), StatusMatricula.EM_ESPERA);
    Matricula espera2 =
        new Matricula("mat-3", terceiroAluno.getId(), turma.getId(), StatusMatricula.EM_ESPERA);
    MatriculaController controller =
        criarController(List.of(confirmada, espera1, espera2), List.of(turma));

    List<Matricula> listaEspera = controller.consultarListaEsperaCompleta(turma.getId());

    assertAll(
        () -> assertEquals(2, listaEspera.size()),
        () -> assertEquals(espera1, listaEspera.get(0)),
        () -> assertEquals(espera2, listaEspera.get(1)));
  }

  @Test
  @DisplayName("RF23: coordenador consulta lista de espera vazia")
  void coordenadorConsultaListaEsperaVazia() {
    autenticacaoController.login("C001", SENHA);
    Turma turma = criarTurma("turma-1", 10);
    Matricula confirmada =
        new Matricula("mat-1", aluno.getId(), turma.getId(), StatusMatricula.CONFIRMADA);
    MatriculaController controller = criarController(List.of(confirmada), List.of(turma));

    List<Matricula> listaEspera = controller.consultarListaEsperaCompleta(turma.getId());

    assertTrue(listaEspera.isEmpty());
  }

  @Test
  @DisplayName("RF23: professor da turma consulta lista de espera com sucesso")
  void professorDaTurmaConsultaListaEspera() {
    autenticacaoController.login("P001", SENHA);
    Turma turma = criarTurma("turma-1", 1);
    Matricula espera =
        new Matricula("mat-1", aluno.getId(), turma.getId(), StatusMatricula.EM_ESPERA);
    MatriculaController controller = criarController(List.of(espera), List.of(turma));

    List<Matricula> listaEspera = controller.consultarListaEsperaCompleta(turma.getId());

    assertEquals(1, listaEspera.size());
    assertEquals(espera, listaEspera.get(0));
  }

  @Test
  @DisplayName("RF23: professor de outra turma não pode consultar lista de espera")
  void professorDeOutraTurmaNaoConsultaListaEspera() {
    autenticacaoController.login("P002", SENHA);
    Turma turma = criarTurma("turma-1", 1);
    Matricula espera =
        new Matricula("mat-1", aluno.getId(), turma.getId(), StatusMatricula.EM_ESPERA);
    MatriculaController controller = criarController(List.of(espera), List.of(turma));

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.consultarListaEsperaCompleta(turma.getId()));
  }

  @Test
  @DisplayName("RF23: aluno consulta sua posição na lista de espera (posição correta)")
  void alunoConsultaPosicaoNaListaDeEspera() {
    autenticacaoController.login("A002", SENHA);
    Turma turma = criarTurma("turma-1", 1);
    Matricula confirmada =
        new Matricula("mat-1", aluno.getId(), turma.getId(), StatusMatricula.CONFIRMADA);
    Matricula espera1 =
        new Matricula("mat-2", terceiroAluno.getId(), turma.getId(), StatusMatricula.EM_ESPERA);
    Matricula espera2 =
        new Matricula("mat-3", outroAluno.getId(), turma.getId(), StatusMatricula.EM_ESPERA);
    MatriculaController controller =
        criarController(List.of(confirmada, espera1, espera2), List.of(turma));

    int posicao = controller.consultarPosicaoAluno(turma.getId());

    assertEquals(2, posicao);
  }

  @Test
  @DisplayName("RF23: aluno consulta posição quando não está na lista (retorna 0)")
  void alunoConsultaPosicaoQuandoNaoEstaLista() {
    autenticacaoController.login("A001", SENHA);
    Turma turma = criarTurma("turma-1", 1);
    Matricula confirmada =
        new Matricula("mat-1", aluno.getId(), turma.getId(), StatusMatricula.CONFIRMADA);
    MatriculaController controller = criarController(List.of(confirmada), List.of(turma));

    int posicao = controller.consultarPosicaoAluno(turma.getId());

    assertEquals(0, posicao);
  }

  @Test
  @DisplayName("RF23: coordenador remove aluno da lista de espera com sucesso")
  void coordenadorRemoveAlunoDaListaDeEspera() {
    autenticacaoController.login("C001", SENHA);
    Turma turma = criarTurma("turma-1", 1);
    Matricula confirmada =
        new Matricula("mat-1", aluno.getId(), turma.getId(), StatusMatricula.CONFIRMADA);
    Matricula espera =
        new Matricula("mat-2", outroAluno.getId(), turma.getId(), StatusMatricula.EM_ESPERA);
    MatriculaController controller = criarController(List.of(confirmada, espera), List.of(turma));

    Matricula removida = controller.removerAlunoListaEspera(turma.getId(), espera.getId());

    assertAll(
        () -> assertEquals(espera, removida),
        () -> assertEquals(1, controller.getMatriculas().size()),
        () -> assertTrue(controller.consultarListaEspera(turma.getId()).isEmpty()));
  }

  @Test
  @DisplayName("RF23: coordenador não pode remover matrícula confirmada via remoção de espera")
  void coordenadorNaoPodeRemoverMatriculaConfirmada() {
    autenticacaoController.login("C001", SENHA);
    Turma turma = criarTurma("turma-1", 1);
    Matricula confirmada =
        new Matricula("mat-1", aluno.getId(), turma.getId(), StatusMatricula.CONFIRMADA);
    MatriculaController controller = criarController(List.of(confirmada), List.of(turma));

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.removerAlunoListaEspera(turma.getId(), confirmada.getId()));
  }

  @Test
  @DisplayName("RF23: tentativa de remoção com matrícula inexistente lança erro")
  void remocaoComMatriculaInexistenteLancaErro() {
    autenticacaoController.login("C001", SENHA);
    Turma turma = criarTurma("turma-1", 1);
    MatriculaController controller = criarController(List.of(), List.of(turma));

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.removerAlunoListaEspera(turma.getId(), "mat-inexistente"));
  }

  @Test
  @DisplayName("RF23: ordem FIFO é mantida na lista de espera")
  void ordemFifoMantidaNaListaDeEspera() {
    autenticacaoController.login("C001", SENHA);
    Turma turma = criarTurma("turma-1", 1);
    Matricula espera1 =
        new Matricula("mat-1", aluno.getId(), turma.getId(), StatusMatricula.EM_ESPERA);
    Matricula espera2 =
        new Matricula("mat-2", outroAluno.getId(), turma.getId(), StatusMatricula.EM_ESPERA);
    Matricula espera3 =
        new Matricula("mat-3", terceiroAluno.getId(), turma.getId(), StatusMatricula.EM_ESPERA);
    MatriculaController controller =
        criarController(List.of(espera1, espera2, espera3), List.of(turma));

    List<Matricula> listaEspera = controller.consultarListaEsperaCompleta(turma.getId());

    assertAll(
        () -> assertEquals(3, listaEspera.size()),
        () -> assertEquals(espera1, listaEspera.get(0)),
        () -> assertEquals(espera2, listaEspera.get(1)),
        () -> assertEquals(espera3, listaEspera.get(2)));
  }

  private MatriculaController criarController(List<Matricula> matriculas, List<Turma> turmas) {
    return new MatriculaController(
        autenticacaoController, matriculas, turmas, List.of(periodoAtivo), List.of(disciplina));
  }

  private Turma criarTurma(String id, int limiteVagas) {
    return new Turma(
        id,
        disciplina.getId(),
        periodoAtivo.getId(),
        professor.getId(),
        limiteVagas,
        "Sala 101",
        LocalDate.of(2026, 8, 1),
        List.of(
            new BlocoHorario(DayOfWeek.MONDAY, LocalTime.parse("08:00"), LocalTime.parse("10:00"))),
        false);
  }
}
