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
import pb.classroom.model.FrequenciaAluno;
import pb.classroom.model.Matricula;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.PeriodoLetivo;
import pb.classroom.model.RegistroPresenca;
import pb.classroom.model.StatusMatricula;
import pb.classroom.model.StatusPresenca;
import pb.classroom.model.Turma;
import pb.classroom.model.Usuario;

@DisplayName("RF28 - Calcular Percentual de Frequência")
class PresencaRf28Test {

  private static final String SENHA = "senha";

  private Usuario aluno;
  private Usuario outroAluno;
  private Usuario professor;
  private Usuario coordenador;
  private AutenticacaoController autenticacaoController;
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
            "Professor",
            "P001",
            "prof@classroompb.com",
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
    autenticacaoController =
        new AutenticacaoController(List.of(aluno, outroAluno, professor, coordenador));

    PeriodoLetivo periodo = new PeriodoLetivo("periodo-1", "2026.1", true);
    Disciplina disciplina =
        new Disciplina("disc-1", "ESW101", "Projeto de Software", 60, 4, "curso-1", List.of());
    turma =
        new Turma(
            "turma-1",
            disciplina.getId(),
            periodo.getId(),
            professor.getId(),
            10,
            "Sala 101",
            LocalDate.of(2026, 3, 1),
            List.of(
                new BlocoHorario(
                    DayOfWeek.MONDAY, LocalTime.parse("08:00"), LocalTime.parse("10:00"))),
            false);
    matriculaAluno =
        new Matricula("mat-1", aluno.getId(), turma.getId(), StatusMatricula.CONFIRMADA);
    matriculaOutroAluno =
        new Matricula("mat-2", outroAluno.getId(), turma.getId(), StatusMatricula.CONFIRMADA);
  }

  @Test
  @DisplayName("RF28: calcula 100% quando todas as aulas foram presença")
  void calculaCemPorcentoQuandoTodasPresencas() {
    List<RegistroPresenca> registros =
        List.of(
            new RegistroPresenca(
                "reg-1",
                turma.getId(),
                aluno.getId(),
                LocalDate.of(2026, 3, 2),
                StatusPresenca.PRESENTE),
            new RegistroPresenca(
                "reg-2",
                turma.getId(),
                aluno.getId(),
                LocalDate.of(2026, 3, 9),
                StatusPresenca.PRESENTE));
    PresencaController controller = criarController(registros);

    FrequenciaAluno frequencia = controller.calcularFrequenciaAluno(turma.getId(), aluno.getId());

    assertAll(
        () -> assertEquals(2, frequencia.getTotalAulasRegistradas()),
        () -> assertEquals(2, frequencia.getTotalPresencas()),
        () -> assertEquals(0, frequencia.getTotalFaltas()),
        () -> assertEquals(100.0, frequencia.getPercentual(), 0.01));
  }

  @Test
  @DisplayName("RF28: calcula 50% com metade presenças e metade faltas")
  void calculaCinquentaPorcentoComMetadePresencas() {
    List<RegistroPresenca> registros =
        List.of(
            new RegistroPresenca(
                "reg-1",
                turma.getId(),
                aluno.getId(),
                LocalDate.of(2026, 3, 2),
                StatusPresenca.PRESENTE),
            new RegistroPresenca(
                "reg-2",
                turma.getId(),
                aluno.getId(),
                LocalDate.of(2026, 3, 9),
                StatusPresenca.FALTA));
    PresencaController controller = criarController(registros);

    FrequenciaAluno frequencia = controller.calcularFrequenciaAluno(turma.getId(), aluno.getId());

    assertAll(
        () -> assertEquals(2, frequencia.getTotalAulasRegistradas()),
        () -> assertEquals(1, frequencia.getTotalPresencas()),
        () -> assertEquals(50.0, frequencia.getPercentual(), 0.01));
  }

  @Test
  @DisplayName("RF28: retorna 0% quando não há registros de presença")
  void retornaZeroQuandoNaoHaRegistros() {
    PresencaController controller = criarController(List.of());

    FrequenciaAluno frequencia = controller.calcularFrequenciaAluno(turma.getId(), aluno.getId());

    assertAll(
        () -> assertEquals(0, frequencia.getTotalAulasRegistradas()),
        () -> assertEquals(0, frequencia.getTotalPresencas()),
        () -> assertEquals(0.0, frequencia.getPercentual(), 0.01));
  }

  @Test
  @DisplayName("RF28: professor consulta frequência de todos os alunos da turma")
  void professorConsultaFrequenciaDeTodosAlunos() {
    List<RegistroPresenca> registros =
        List.of(
            new RegistroPresenca(
                "reg-1",
                turma.getId(),
                aluno.getId(),
                LocalDate.of(2026, 3, 2),
                StatusPresenca.PRESENTE),
            new RegistroPresenca(
                "reg-2",
                turma.getId(),
                aluno.getId(),
                LocalDate.of(2026, 3, 9),
                StatusPresenca.FALTA),
            new RegistroPresenca(
                "reg-3",
                turma.getId(),
                outroAluno.getId(),
                LocalDate.of(2026, 3, 2),
                StatusPresenca.PRESENTE));
    PresencaController controller = criarController(registros);
    autenticacaoController.login(professor.getMatricula(), SENHA);

    List<FrequenciaAluno> frequencias = controller.calcularFrequenciaPorTurma(turma.getId());

    assertAll(
        () -> assertEquals(2, frequencias.size()),
        () -> assertEquals(50.0, frequencias.get(0).getPercentual(), 0.01),
        () -> assertEquals(100.0, frequencias.get(1).getPercentual(), 0.01));
  }

  @Test
  @DisplayName("RF28: coordenador consulta frequência da turma")
  void coordenadorConsultaFrequenciaDaTurma() {
    List<RegistroPresenca> registros =
        List.of(
            new RegistroPresenca(
                "reg-1",
                turma.getId(),
                aluno.getId(),
                LocalDate.of(2026, 3, 2),
                StatusPresenca.FALTA));
    PresencaController controller = criarController(registros);
    autenticacaoController.login(coordenador.getMatricula(), SENHA);

    List<FrequenciaAluno> frequencias = controller.calcularFrequenciaPorTurma(turma.getId());

    assertAll(
        () -> assertEquals(2, frequencias.size()),
        () -> assertEquals(0.0, frequencias.get(0).getPercentual(), 0.01));
  }

  @Test
  @DisplayName("RF28: aluno consulta própria frequência")
  void alunoConsultaPropriaFrequencia() {
    List<RegistroPresenca> registros =
        List.of(
            new RegistroPresenca(
                "reg-1",
                turma.getId(),
                aluno.getId(),
                LocalDate.of(2026, 3, 2),
                StatusPresenca.PRESENTE),
            new RegistroPresenca(
                "reg-2",
                turma.getId(),
                aluno.getId(),
                LocalDate.of(2026, 3, 9),
                StatusPresenca.PRESENTE),
            new RegistroPresenca(
                "reg-3",
                turma.getId(),
                aluno.getId(),
                LocalDate.of(2026, 3, 16),
                StatusPresenca.FALTA));
    PresencaController controller = criarController(registros);
    autenticacaoController.login(aluno.getMatricula(), SENHA);

    FrequenciaAluno frequencia = controller.consultarMinhaFrequencia(turma.getId());

    assertAll(
        () -> assertEquals(aluno.getId(), frequencia.getIdAluno()),
        () -> assertEquals(3, frequencia.getTotalAulasRegistradas()),
        () -> assertEquals(66.67, frequencia.getPercentual(), 0.1));
  }

  @Test
  @DisplayName(
      "RF28: aluno não consulta frequência de outros perfis via calcularFrequenciaPorTurma")
  void alunoNaoConsultaFrequenciaPorTurma() {
    PresencaController controller = criarController(List.of());
    autenticacaoController.login(aluno.getMatricula(), SENHA);

    assertThrows(
        IllegalArgumentException.class, () -> controller.calcularFrequenciaPorTurma(turma.getId()));
  }

  @Test
  @DisplayName("RF28: professor de outra turma não consulta frequência")
  void professorDeOutraTurmaNaoConsultaFrequencia() {
    Usuario outroProfessor =
        new Usuario(
            "prof-2",
            PerfilUsuario.PROFESSOR,
            "Outro Professor",
            "P002",
            "outro.prof@classroompb.com",
            SENHA,
            true);
    autenticacaoController = new AutenticacaoController(List.of(aluno, professor, outroProfessor));
    PresencaController controller = criarController(List.of());
    autenticacaoController.login(outroProfessor.getMatricula(), SENHA);

    assertThrows(
        IllegalArgumentException.class, () -> controller.calcularFrequenciaPorTurma(turma.getId()));
  }

  @Test
  @DisplayName("RF28: ignora registros de outras turmas no cálculo")
  void ignoraRegistrosDeOutrasTurmas() {
    List<RegistroPresenca> registros =
        List.of(
            new RegistroPresenca(
                "reg-1",
                turma.getId(),
                aluno.getId(),
                LocalDate.of(2026, 3, 2),
                StatusPresenca.PRESENTE),
            new RegistroPresenca(
                "reg-2",
                "outra-turma",
                aluno.getId(),
                LocalDate.of(2026, 3, 2),
                StatusPresenca.FALTA));
    PresencaController controller = criarController(registros);

    FrequenciaAluno frequencia = controller.calcularFrequenciaAluno(turma.getId(), aluno.getId());

    assertAll(
        () -> assertEquals(1, frequencia.getTotalAulasRegistradas()),
        () -> assertEquals(100.0, frequencia.getPercentual(), 0.01));
  }

  private PresencaController criarController(List<RegistroPresenca> registros) {
    return new PresencaController(
        autenticacaoController,
        registros,
        List.of(turma),
        List.of(matriculaAluno, matriculaOutroAluno));
  }
}
