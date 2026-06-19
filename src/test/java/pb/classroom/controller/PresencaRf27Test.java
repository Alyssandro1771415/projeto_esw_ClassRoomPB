package pb.classroom.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pb.classroom.model.BlocoHorario;
import pb.classroom.model.Disciplina;
import pb.classroom.model.Matricula;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.PeriodoLetivo;
import pb.classroom.model.RegistroPresenca;
import pb.classroom.model.StatusPresenca;
import pb.classroom.model.Turma;
import pb.classroom.model.Usuario;

@DisplayName("RF27 - Registrar Presença/Falta dos Alunos")
class PresencaRf27Test {

  private static final String SENHA = "senha";

  private Usuario aluno;
  private Usuario outroAluno;
  private Usuario professor;
  private Usuario outroProfessor;
  private Usuario coordenador;
  private AutenticacaoController autenticacaoController;
  private PeriodoLetivo periodoAtivo;
  private Disciplina disciplina;
  private Turma turma;
  private Matricula matriculaAluno;
  private Matricula matriculaOutroAluno;

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
        new AutenticacaoController(
            List.of(aluno, outroAluno, professor, outroProfessor, coordenador));
    periodoAtivo = new PeriodoLetivo("periodo-1", "2026.1", true);
    disciplina =
        new Disciplina("disc-1", "ESW101", "Projeto de Software", 60, 4, "curso-1", List.of());
    turma = criarTurma("turma-1", professor.getId(), LocalDate.of(2026, 3, 1));
    matriculaAluno = new Matricula("mat-1", aluno.getId(), turma.getId());
    matriculaOutroAluno = new Matricula("mat-2", outroAluno.getId(), turma.getId());
  }

  @Test
  @DisplayName("RF27: professor registra presença com sucesso para alunos matriculados")
  void professorRegistraPresencaComSucesso() {
    autenticacaoController.login("P001", SENHA);
    PresencaController controller =
        criarPresencaController(List.of(), List.of(matriculaAluno, matriculaOutroAluno));

    Map<String, Boolean> presencas = new LinkedHashMap<>();
    presencas.put(aluno.getId(), true);
    presencas.put(outroAluno.getId(), true);

    List<RegistroPresenca> registros =
        controller.registrarPresenca(turma.getId(), LocalDate.of(2026, 3, 2), presencas);

    assertAll(
        () -> assertEquals(2, registros.size()),
        () -> assertEquals(StatusPresenca.PRESENTE, registros.get(0).getStatus()),
        () -> assertEquals(StatusPresenca.PRESENTE, registros.get(1).getStatus()),
        () -> assertEquals(2, controller.getRegistrosPresenca().size()));
  }

  @Test
  @DisplayName("RF27: professor registra falta com sucesso")
  void professorRegistraFaltaComSucesso() {
    autenticacaoController.login("P001", SENHA);
    PresencaController controller = criarPresencaController(List.of(), List.of(matriculaAluno));

    Map<String, Boolean> presencas = new LinkedHashMap<>();
    presencas.put(aluno.getId(), false);

    List<RegistroPresenca> registros =
        controller.registrarPresenca(turma.getId(), LocalDate.of(2026, 3, 2), presencas);

    assertAll(
        () -> assertEquals(1, registros.size()),
        () -> assertEquals(StatusPresenca.FALTA, registros.get(0).getStatus()),
        () -> assertTrue(registros.get(0).isFalta()));
  }

  @Test
  @DisplayName("RF27: professor não pode registrar presença para turma de outro professor")
  void professorNaoRegistraPresencaEmTurmaDeOutro() {
    autenticacaoController.login("P002", SENHA);
    PresencaController controller = criarPresencaController(List.of(), List.of(matriculaAluno));

    Map<String, Boolean> presencas = new LinkedHashMap<>();
    presencas.put(aluno.getId(), true);

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.registrarPresenca(turma.getId(), LocalDate.of(2026, 3, 2), presencas));
  }

  @Test
  @DisplayName("RF27: aluno não pode registrar presença")
  void alunoNaoPodeRegistrarPresenca() {
    autenticacaoController.login("A001", SENHA);
    PresencaController controller = criarPresencaController(List.of(), List.of(matriculaAluno));

    Map<String, Boolean> presencas = new LinkedHashMap<>();
    presencas.put(aluno.getId(), true);

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.registrarPresenca(turma.getId(), LocalDate.of(2026, 3, 2), presencas));
  }

  @Test
  @DisplayName("RF27: registro de presença para data anterior ao início das aulas é bloqueado")
  void registroAntesDoInicioDasAulasBloqueado() {
    autenticacaoController.login("P001", SENHA);
    PresencaController controller = criarPresencaController(List.of(), List.of(matriculaAluno));

    Map<String, Boolean> presencas = new LinkedHashMap<>();
    presencas.put(aluno.getId(), true);

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.registrarPresenca(turma.getId(), LocalDate.of(2026, 2, 28), presencas));
  }

  @Test
  @DisplayName("RF27: registro de presença para data futura é bloqueado")
  void registroParaDataFuturaBloqueado() {
    autenticacaoController.login("P001", SENHA);
    PresencaController controller = criarPresencaController(List.of(), List.of(matriculaAluno));

    Map<String, Boolean> presencas = new LinkedHashMap<>();
    presencas.put(aluno.getId(), true);

    LocalDate dataFutura = LocalDate.now().plusDays(30);
    assertThrows(
        IllegalArgumentException.class,
        () -> controller.registrarPresenca(turma.getId(), dataFutura, presencas));
  }

  @Test
  @DisplayName("RF27: registro para aluno não matriculado na turma é bloqueado")
  void registroParaAlunoNaoMatriculadoBloqueado() {
    autenticacaoController.login("P001", SENHA);
    PresencaController controller = criarPresencaController(List.of(), List.of(matriculaAluno));

    Map<String, Boolean> presencas = new LinkedHashMap<>();
    presencas.put("aluno-inexistente", true);

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.registrarPresenca(turma.getId(), LocalDate.of(2026, 3, 2), presencas));
  }

  @Test
  @DisplayName("RF27: professor consulta todas as presenças da turma")
  void professorConsultaPresencasDaTurma() {
    autenticacaoController.login("P001", SENHA);
    RegistroPresenca reg1 =
        new RegistroPresenca(
            "reg-1",
            turma.getId(),
            aluno.getId(),
            LocalDate.of(2026, 3, 2),
            StatusPresenca.PRESENTE);
    RegistroPresenca reg2 =
        new RegistroPresenca(
            "reg-2",
            turma.getId(),
            outroAluno.getId(),
            LocalDate.of(2026, 3, 2),
            StatusPresenca.FALTA);
    PresencaController controller =
        criarPresencaController(List.of(reg1, reg2), List.of(matriculaAluno, matriculaOutroAluno));

    List<RegistroPresenca> presencas = controller.consultarPresencasPorTurma(turma.getId());

    assertAll(
        () -> assertEquals(2, presencas.size()),
        () -> assertEquals(reg1, presencas.get(0)),
        () -> assertEquals(reg2, presencas.get(1)));
  }

  @Test
  @DisplayName("RF27: aluno consulta suas próprias presenças")
  void alunoConsultaSuasPresencas() {
    autenticacaoController.login("A001", SENHA);
    RegistroPresenca reg1 =
        new RegistroPresenca(
            "reg-1",
            turma.getId(),
            aluno.getId(),
            LocalDate.of(2026, 3, 2),
            StatusPresenca.PRESENTE);
    RegistroPresenca reg2 =
        new RegistroPresenca(
            "reg-2",
            turma.getId(),
            outroAluno.getId(),
            LocalDate.of(2026, 3, 2),
            StatusPresenca.FALTA);
    PresencaController controller =
        criarPresencaController(List.of(reg1, reg2), List.of(matriculaAluno, matriculaOutroAluno));

    List<RegistroPresenca> presencas = controller.consultarPresencasDoAluno(turma.getId());

    assertAll(
        () -> assertEquals(1, presencas.size()),
        () -> assertEquals(reg1, presencas.get(0)),
        () -> assertTrue(presencas.get(0).isPresente()));
  }

  @Test
  @DisplayName("RF27: registro duplicado (mesma turma, aluno e data) é bloqueado")
  void registroDuplicadoBloqueado() {
    autenticacaoController.login("P001", SENHA);
    RegistroPresenca existente =
        new RegistroPresenca(
            "reg-1",
            turma.getId(),
            aluno.getId(),
            LocalDate.of(2026, 3, 2),
            StatusPresenca.PRESENTE);
    PresencaController controller =
        criarPresencaController(List.of(existente), List.of(matriculaAluno));

    Map<String, Boolean> presencas = new LinkedHashMap<>();
    presencas.put(aluno.getId(), false);

    assertThrows(
        IllegalArgumentException.class,
        () -> controller.registrarPresenca(turma.getId(), LocalDate.of(2026, 3, 2), presencas));
  }

  private PresencaController criarPresencaController(
      List<RegistroPresenca> registros, List<Matricula> matriculas) {
    return new PresencaController(autenticacaoController, registros, List.of(turma), matriculas);
  }

  private Turma criarTurma(String id, String idProfessor, LocalDate dataInicio) {
    return new Turma(
        id,
        disciplina.getId(),
        periodoAtivo.getId(),
        idProfessor,
        30,
        "Sala 101",
        dataInicio,
        List.of(
            new BlocoHorario(DayOfWeek.MONDAY, LocalTime.parse("08:00"), LocalTime.parse("10:00"))),
        false);
  }
}
