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
import pb.classroom.model.EtapaAvaliacao;
import pb.classroom.model.HistoricoAcademico;
import pb.classroom.model.Matricula;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.RegistroNota;
import pb.classroom.model.ResultadoAvaliacao;
import pb.classroom.model.SituacaoAcademica;
import pb.classroom.model.Turma;
import pb.classroom.model.Usuario;

@DisplayName("RF31-RF35 - Notas e Avaliação")
class NotaRf31Rf35Test {

  private static final String SENHA = "senha";

  private Usuario aluno;
  private Usuario professor;
  private Usuario outroProfessor;
  private Usuario coordenador;
  private AutenticacaoController autenticacaoController;
  private Turma turma;
  private Matricula matriculaAluno;
  private List<RegistroNota> notas;
  private List<HistoricoAcademico> historicos;
  private NotaController notaController;

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
    outroProfessor =
        new Usuario(
            "prof-2",
            PerfilUsuario.PROFESSOR,
            "Outro Professor",
            "P002",
            "outroprof@classroompb.com",
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
    autenticacaoController =
        new AutenticacaoController(List.of(aluno, professor, outroProfessor, coordenador));
    turma = criarTurma("turma-1", professor.getId());
    matriculaAluno = new Matricula("mat-1", aluno.getId(), turma.getId());
    notas = new ArrayList<>();
    historicos = new ArrayList<>();
    notaController = criarNotaController();
  }

  @Test
  @DisplayName("RF31: professor lança notas das etapas 1 e 2")
  void professorLancaNotasDasEtapas() {
    autenticacaoController.login("P001", SENHA);

    RegistroNota etapa1 =
        notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 8.0);
    RegistroNota etapa2 =
        notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA2, 6.0);

    assertAll(
        () -> assertEquals(8.0, etapa1.getNotaEtapa1()),
        () -> assertEquals(6.0, etapa2.getNotaEtapa2()),
        () -> assertEquals(1, notaController.getNotas().size()));
  }

  @Test
  @DisplayName("RF32: sistema calcula média final automaticamente")
  void sistemaCalculaMediaFinal() {
    autenticacaoController.login("P001", SENHA);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 8.0);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA2, 6.0);

    ResultadoAvaliacao resultado = notaController.calcularResultado(turma.getId(), aluno.getId());

    assertEquals(7.0, resultado.getMediaFinal(), 0.001);
  }

  @Test
  @DisplayName("RF33: aluno consulta suas notas")
  void alunoConsultaSuasNotas() {
    autenticacaoController.login("P001", SENHA);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 9.0);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA2, 8.0);

    autenticacaoController.logout();
    autenticacaoController.login("A001", SENHA);
    List<ResultadoAvaliacao> resultados = notaController.consultarMinhasNotas();

    assertEquals(1, resultados.size());
    assertEquals(8.5, resultados.get(0).getMediaFinal(), 0.001);
  }

  @Test
  @DisplayName("RF34: informa situação aprovado")
  void informaSituacaoAprovado() {
    autenticacaoController.login("P001", SENHA);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 8.0);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA2, 8.0);

    ResultadoAvaliacao resultado = notaController.calcularResultado(turma.getId(), aluno.getId());

    assertEquals(SituacaoAcademica.APROVADO, resultado.getSituacao());
  }

  @Test
  @DisplayName("RF34: informa situação em recuperação")
  void informaSituacaoEmRecuperacao() {
    autenticacaoController.login("P001", SENHA);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 6.0);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA2, 5.0);

    ResultadoAvaliacao resultado = notaController.calcularResultado(turma.getId(), aluno.getId());

    assertEquals(SituacaoAcademica.EM_RECUPERACAO, resultado.getSituacao());
  }

  @Test
  @DisplayName("RF34: informa situação reprovado por nota")
  void informaSituacaoReprovadoPorNota() {
    autenticacaoController.login("P001", SENHA);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 3.0);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA2, 4.0);

    ResultadoAvaliacao resultado = notaController.calcularResultado(turma.getId(), aluno.getId());

    assertEquals(SituacaoAcademica.REPROVADO_NOTA, resultado.getSituacao());
  }

  @Test
  @DisplayName("RF35: professor altera nota antes do fechamento da turma")
  void professorAlteraNotaAntesDoFechamento() {
    autenticacaoController.login("P001", SENHA);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 5.0);
    notaController.alterarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 7.0);

    RegistroNota registro =
        notaController.getNotas().stream()
            .filter(n -> n.getIdAluno().equals(aluno.getId()))
            .findFirst()
            .orElseThrow();
    assertEquals(7.0, registro.getNotaEtapa1());
  }

  @Test
  @DisplayName("RF35: não permite alterar nota após fechamento da turma")
  void naoPermiteAlterarNotaAposFechamento() {
    autenticacaoController.login("P001", SENHA);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 6.0);

    autenticacaoController.logout();
    autenticacaoController.login("C001", SENHA);
    notaController.fecharTurma(turma.getId());

    autenticacaoController.logout();
    autenticacaoController.login("P001", SENHA);
    assertThrows(
        IllegalArgumentException.class,
        () -> notaController.alterarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 8.0));
  }

  @Test
  @DisplayName("apenas professor da turma pode lançar notas")
  void apenasProfessorDaTurmaPodeLancarNotas() {
    autenticacaoController.login("P002", SENHA);
    assertThrows(
        IllegalArgumentException.class,
        () -> notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 7.0));
  }

  @Test
  @DisplayName("RF34: informa situação reprovado por falta")
  void informaSituacaoReprovadoPorFalta() {
    PresencaController presencaController =
        new PresencaController(
            autenticacaoController, new ArrayList<>(), List.of(turma), List.of(matriculaAluno));
    NotaController controllerComPresenca =
        new NotaController(
            autenticacaoController,
            presencaController,
            notas,
            historicos,
            List.of(turma),
            List.of(matriculaAluno));

    autenticacaoController.login("P001", SENHA);
    controllerComPresenca.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 9.0);
    controllerComPresenca.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA2, 9.0);

    presencaController.registrarPresenca(
        turma.getId(), LocalDate.of(2026, 3, 2), java.util.Map.of(aluno.getId(), false));
    presencaController.registrarPresenca(
        turma.getId(), LocalDate.of(2026, 3, 3), java.util.Map.of(aluno.getId(), false));
    presencaController.registrarPresenca(
        turma.getId(), LocalDate.of(2026, 3, 4), java.util.Map.of(aluno.getId(), true));

    ResultadoAvaliacao resultado =
        controllerComPresenca.calcularResultado(turma.getId(), aluno.getId());

    assertEquals(SituacaoAcademica.REPROVADO_FALTA, resultado.getSituacao());
  }

  @Test
  @DisplayName("coordenador consulta resultados por turma")
  void coordenadorConsultaResultadosPorTurma() {
    autenticacaoController.login("P001", SENHA);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 7.0);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA2, 8.0);

    autenticacaoController.logout();
    autenticacaoController.login("C001", SENHA);
    List<ResultadoAvaliacao> resultados = notaController.consultarResultadosPorTurma(turma.getId());

    assertEquals(1, resultados.size());
    assertEquals(7.5, resultados.get(0).getMediaFinal(), 0.001);
  }

  @Test
  @DisplayName("professor lança nota de recuperação")
  void professorLancaNotaRecuperacao() {
    autenticacaoController.login("P001", SENHA);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 5.0);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA2, 5.0);

    RegistroNota registro = notaController.lancarNotaRecuperacao(turma.getId(), aluno.getId(), 7.5);

    assertEquals(7.5, registro.getNotaRecuperacao());
    ResultadoAvaliacao resultado = notaController.calcularResultado(turma.getId(), aluno.getId());
    assertEquals(SituacaoAcademica.APROVADO, resultado.getSituacao());
  }

  @Test
  @DisplayName("não permite alterar nota inexistente")
  void naoPermiteAlterarNotaInexistente() {
    autenticacaoController.login("P001", SENHA);
    assertThrows(
        IllegalArgumentException.class,
        () -> notaController.alterarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 8.0));
  }

  @Test
  @DisplayName("não permite fechar turma já fechada")
  void naoPermiteFecharTurmaJaFechada() {
    autenticacaoController.login("P001", SENHA);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA1, 7.0);
    notaController.lancarNota(turma.getId(), aluno.getId(), EtapaAvaliacao.ETAPA2, 8.0);

    autenticacaoController.logout();
    autenticacaoController.login("C001", SENHA);
    notaController.fecharTurma(turma.getId());

    assertThrows(IllegalArgumentException.class, () -> notaController.fecharTurma(turma.getId()));
  }

  @Test
  @DisplayName("aluno não consulta resultados por turma")
  void alunoNaoConsultaResultadosPorTurma() {
    autenticacaoController.login("A001", SENHA);
    assertThrows(
        IllegalArgumentException.class,
        () -> notaController.consultarResultadosPorTurma(turma.getId()));
  }

  private NotaController criarNotaController() {
    PresencaController presencaController =
        new PresencaController(
            autenticacaoController, new ArrayList<>(), List.of(turma), List.of(matriculaAluno));
    return new NotaController(
        autenticacaoController,
        presencaController,
        notas,
        historicos,
        List.of(turma),
        List.of(matriculaAluno));
  }

  private Turma criarTurma(String id, String idProfessor) {
    return new Turma(
        id,
        "disc-1",
        "periodo-1",
        idProfessor,
        30,
        "Sala 101",
        LocalDate.of(2026, 3, 1),
        List.of(new BlocoHorario(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(10, 0))),
        false);
  }
}
