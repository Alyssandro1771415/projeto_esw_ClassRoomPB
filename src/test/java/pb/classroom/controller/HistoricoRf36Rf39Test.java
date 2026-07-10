package pb.classroom.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pb.classroom.model.BlocoHorario;
import pb.classroom.model.Curso;
import pb.classroom.model.Disciplina;
import pb.classroom.model.EtapaAvaliacao;
import pb.classroom.model.HistoricoAcademico;
import pb.classroom.model.Matricula;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.PeriodoLetivo;
import pb.classroom.model.RegistroNota;
import pb.classroom.model.SituacaoAcademica;
import pb.classroom.model.Turma;
import pb.classroom.model.Usuario;

@DisplayName("RF36-RF39 - Histórico Acadêmico")
class HistoricoRf36Rf39Test {

  private static final String SENHA = "senha";

  private Usuario aluno;
  private Usuario professor;
  private Usuario coordenador;
  private AutenticacaoController autenticacaoController;
  private Curso curso;
  private Disciplina disciplina;
  private PeriodoLetivo periodo;
  private Turma turma;
  private Matricula matriculaAluno;
  private List<RegistroNota> notas;
  private List<HistoricoAcademico> historicos;
  private NotaController notaController;
  private HistoricoAcademicoController historicoController;

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
            "Professor Teste",
            "P001",
            "prof@classroompb.com",
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
    autenticacaoController = new AutenticacaoController(List.of(aluno, professor, coordenador));
    curso = new Curso("curso-1", "Engenharia de Software", "ESW");
    disciplina =
        new Disciplina("disc-1", "ESW101", "Projeto de Software", 60, 4, curso.getId(), List.of());
    periodo = new PeriodoLetivo("periodo-1", "2026.1", true);
    turma = criarTurma("turma-1", professor.getId());
    matriculaAluno = new Matricula("mat-1", aluno.getId(), turma.getId());
    notas = new ArrayList<>();
    historicos = new ArrayList<>();

    PresencaController presencaController =
        new PresencaController(
            autenticacaoController, new ArrayList<>(), List.of(turma), List.of(matriculaAluno));
    notaController =
        new NotaController(
            autenticacaoController,
            presencaController,
            notas,
            historicos,
            List.of(turma),
            List.of(matriculaAluno));
    historicoController =
        new HistoricoAcademicoController(
            autenticacaoController, historicos, List.of(disciplina), List.of(aluno, professor));
  }

  @Test
  @DisplayName("RF36: sistema mantém histórico ao fechar turma")
  void sistemaMantemHistoricoAoFecharTurma() {
    autenticacaoController.login("P001", SENHA);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 8.0);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA2, 9.0);

    autenticacaoController.logout();
    autenticacaoController.login("C001", SENHA);
    List<HistoricoAcademico> gerados = notaController.fecharTurma(turma.getId());

    assertEquals(1, gerados.size());
    assertEquals(1, historicoController.getHistoricos().size());
  }

  @Test
  @DisplayName(
      "RF37: histórico registra período, disciplina, professor, nota, frequência e situação")
  void historicoRegistraCamposObrigatorios() {
    autenticacaoController.login("P001", SENHA);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 7.0);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA2, 8.0);

    autenticacaoController.logout();
    autenticacaoController.login("C001", SENHA);
    HistoricoAcademico historico = notaController.fecharTurma(turma.getId()).get(0);

    assertAll(
        () -> assertEquals(periodo.getId(), historico.getIdPeriodoLetivo()),
        () -> assertEquals(disciplina.getId(), historico.getIdDisciplina()),
        () -> assertEquals(professor.getId(), historico.getIdProfessor()),
        () -> assertEquals(7.5, historico.getMediaFinal(), 0.001),
        () -> assertEquals(SituacaoAcademica.APROVADO, historico.getSituacao()),
        () -> assertNotNull(historico.getDataRegistro()));
  }

  @Test
  @DisplayName("RF38: aluno consulta seu histórico acadêmico")
  void alunoConsultaSeuHistorico() {
    gerarHistorico();

    autenticacaoController.logout();
    autenticacaoController.login("A001", SENHA);
    List<HistoricoAcademico> historico = historicoController.consultarMeuHistorico();

    assertEquals(1, historico.size());
    assertEquals(aluno.getId(), historico.get(0).getIdAluno());
  }

  @Test
  @DisplayName("RF39: coordenador consulta histórico de aluno do curso")
  void coordenadorConsultaHistoricoDoCurso() {
    gerarHistorico();

    autenticacaoController.logout();
    autenticacaoController.login("C001", SENHA);
    List<HistoricoAcademico> porAluno = historicoController.consultarHistoricoAluno(aluno.getId());
    List<HistoricoAcademico> porCurso =
        historicoController.consultarHistoricoPorCurso(curso.getId());

    assertEquals(1, porAluno.size());
    assertEquals(1, porCurso.size());
    assertEquals(disciplina.getId(), porCurso.get(0).getIdDisciplina());
  }

  @Test
  @DisplayName("aluno aprovado em disciplina é reconhecido para pré-requisitos")
  void alunoAprovadoReconhecidoParaPreRequisitos() {
    gerarHistorico();
    assertTrue(historicoController.alunoAprovadoEmDisciplina(aluno.getId(), disciplina.getId()));
  }

  @Test
  @DisplayName("coordenador não consulta histórico de aluno inexistente")
  void coordenadorNaoConsultaHistoricoDeAlunoInexistente() {
    autenticacaoController.login("C001", SENHA);
    assertThrows(
        IllegalArgumentException.class,
        () -> historicoController.consultarHistoricoAluno("aluno-inexistente"));
  }

  @Test
  @DisplayName("aluno não consulta histórico de outro aluno")
  void alunoNaoConsultaHistoricoDeOutro() {
    gerarHistorico();
    autenticacaoController.logout();
    autenticacaoController.login("A001", SENHA);
    assertThrows(
        IllegalArgumentException.class,
        () -> historicoController.consultarHistoricoAluno("outro-aluno"));
  }

  @Test
  @DisplayName("consulta histórico por curso sem registros retorna vazio")
  void consultaHistoricoPorCursoSemRegistros() {
    autenticacaoController.login("C001", SENHA);
    List<HistoricoAcademico> historicos =
        historicoController.consultarHistoricoPorCurso(curso.getId());
    assertTrue(historicos.isEmpty());
  }

  private void gerarHistorico() {
    autenticacaoController.login("P001", SENHA);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 7.0);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA2, 8.0);
    autenticacaoController.logout();
    autenticacaoController.login("C001", SENHA);
    notaController.fecharTurma(turma.getId());
  }

  private Turma criarTurma(String id, String idProfessor) {
    return new Turma(
        id,
        disciplina.getId(),
        periodo.getId(),
        idProfessor,
        30,
        "Sala 101",
        LocalDate.of(2026, 3, 1),
        List.of(new BlocoHorario(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(10, 0))),
        false);
  }
}
