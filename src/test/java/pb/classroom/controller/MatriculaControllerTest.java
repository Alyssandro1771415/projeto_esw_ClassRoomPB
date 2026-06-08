package pb.classroom.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pb.classroom.model.BlocoHorario;
import pb.classroom.model.Matricula;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.PeriodoLetivo;
import pb.classroom.model.Turma;
import pb.classroom.model.Usuario;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MatriculaController - RF19")
class MatriculaControllerTest {

    private static final String SENHA = "senha";

    private Usuario aluno;
    private Usuario coordenador;
    private AutenticacaoController autenticacaoController;
    private PeriodoLetivo periodoAtivo;
    private PeriodoLetivo outroPeriodoAtivo;
    private Turma turmaMatriculada;

    @BeforeEach
    void setUp() {
        aluno = new Usuario(
                "aluno-1",
                PerfilUsuario.ALUNO,
                "Aluno Teste",
                "A001",
                "aluno@classroompb.com",
                SENHA,
                true);
        coordenador = new Usuario(
                "coord-1",
                PerfilUsuario.COORDENADOR,
                "Coordenador",
                "C001",
                "coord@classroompb.com",
                SENHA,
                true);
        autenticacaoController = new AutenticacaoController(List.of(aluno, coordenador));
        periodoAtivo = new PeriodoLetivo("periodo-1", "2026.1", true);
        outroPeriodoAtivo = new PeriodoLetivo("periodo-2", "2026.2", true);
        turmaMatriculada = criarTurma(
                "turma-1",
                periodoAtivo.getId(),
                DayOfWeek.MONDAY,
                "08:00",
                "10:00",
                false);
    }

    @Test
    @DisplayName("RF19: impede matricula em turma com horario sobreposto")
    void impedeMatriculaEmTurmaComHorarioSobreposto() {
        Turma turmaConflitante = criarTurma(
                "turma-2",
                periodoAtivo.getId(),
                DayOfWeek.MONDAY,
                "09:00",
                "11:00",
                false);
        Matricula existente = new Matricula("mat-1", aluno.getId(), turmaMatriculada.getId());
        MatriculaController controller = criarController(
                List.of(existente),
                List.of(turmaMatriculada, turmaConflitante));
        autenticacaoController.login("A001", SENHA);

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> controller.solicitarMatricula(turmaConflitante.getId()));

        assertAll(
                () -> assertTrue(erro.getMessage().contains("Choque de horário")),
                () -> assertEquals(List.of(existente), controller.getMatriculas()));
    }

    @Test
    @DisplayName("RF19: permite horarios consecutivos sem sobreposicao")
    void permiteHorariosConsecutivosSemSobreposicao() {
        Turma turmaSeguinte = criarTurma(
                "turma-2",
                periodoAtivo.getId(),
                DayOfWeek.MONDAY,
                "10:00",
                "12:00",
                false);
        MatriculaController controller = criarController(
                List.of(new Matricula("mat-1", aluno.getId(), turmaMatriculada.getId())),
                List.of(turmaMatriculada, turmaSeguinte));
        autenticacaoController.login("A001", SENHA);

        Matricula nova = controller.solicitarMatricula(turmaSeguinte.getId());

        assertAll(
                () -> assertEquals(turmaSeguinte.getId(), nova.getIdTurma()),
                () -> assertEquals(2, controller.getMatriculas().size()));
    }

    @Test
    @DisplayName("RF19: permite mesmo horario em outro dia ou periodo")
    void permiteMesmoHorarioEmOutroDiaOuPeriodo() {
        Turma outroDia = criarTurma(
                "turma-2",
                periodoAtivo.getId(),
                DayOfWeek.TUESDAY,
                "08:00",
                "10:00",
                false);
        Turma outroPeriodo = criarTurma(
                "turma-3",
                outroPeriodoAtivo.getId(),
                DayOfWeek.MONDAY,
                "08:00",
                "10:00",
                false);
        MatriculaController controller = criarController(
                List.of(new Matricula("mat-1", aluno.getId(), turmaMatriculada.getId())),
                List.of(turmaMatriculada, outroDia, outroPeriodo));
        autenticacaoController.login("A001", SENHA);

        assertAll(
                () -> assertDoesNotThrow(() -> controller.solicitarMatricula(outroDia.getId())),
                () -> assertDoesNotThrow(() -> controller.solicitarMatricula(outroPeriodo.getId())));
    }

    @Test
    @DisplayName("RF19: turma cancelada ja matriculada nao gera conflito")
    void turmaCanceladaJaMatriculadaNaoGeraConflito() {
        Turma cancelada = criarTurma(
                "turma-cancelada",
                periodoAtivo.getId(),
                DayOfWeek.MONDAY,
                "08:00",
                "10:00",
                true);
        Turma novaTurma = criarTurma(
                "turma-nova",
                periodoAtivo.getId(),
                DayOfWeek.MONDAY,
                "08:00",
                "10:00",
                false);
        MatriculaController controller = criarController(
                List.of(new Matricula("mat-1", aluno.getId(), cancelada.getId())),
                List.of(cancelada, novaTurma));
        autenticacaoController.login("A001", SENHA);

        assertDoesNotThrow(() -> controller.solicitarMatricula(novaTurma.getId()));
    }

    @Test
    @DisplayName("somente aluno autenticado solicita matricula")
    void somenteAlunoAutenticadoSolicitaMatricula() {
        MatriculaController controller = criarController(List.of(), List.of(turmaMatriculada));

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.solicitarMatricula(turmaMatriculada.getId()));

        autenticacaoController.login("C001", SENHA);
        assertThrows(
                IllegalArgumentException.class,
                () -> controller.solicitarMatricula(turmaMatriculada.getId()));
    }

    @Test
    @DisplayName("rejeita turma indisponivel, inexistente ou matricula duplicada")
    void rejeitaTurmaIndisponivelInexistenteOuDuplicada() {
        Turma cancelada = criarTurma(
                "turma-cancelada",
                periodoAtivo.getId(),
                DayOfWeek.WEDNESDAY,
                "08:00",
                "10:00",
                true);
        MatriculaController controller = criarController(
                List.of(new Matricula("mat-1", aluno.getId(), turmaMatriculada.getId())),
                List.of(turmaMatriculada, cancelada));
        autenticacaoController.login("A001", SENHA);

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.solicitarMatricula("turma-inexistente")),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.solicitarMatricula(cancelada.getId())),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.solicitarMatricula(turmaMatriculada.getId())),
                () -> assertThrows(
                        UnsupportedOperationException.class,
                        () -> controller.getMatriculas().clear()));
    }

    private MatriculaController criarController(List<Matricula> matriculas, List<Turma> turmas) {
        return new MatriculaController(
                autenticacaoController,
                matriculas,
                turmas,
                List.of(periodoAtivo, outroPeriodoAtivo));
    }

    private Turma criarTurma(
            String id,
            String idPeriodo,
            DayOfWeek dia,
            String inicio,
            String fim,
            boolean cancelada) {
        return new Turma(
                id,
                "disc-" + id,
                idPeriodo,
                "prof-1",
                30,
                "Sala 101",
                LocalDate.of(2026, 8, 1),
                List.of(new BlocoHorario(dia, LocalTime.parse(inicio), LocalTime.parse(fim))),
                cancelada);
    }
}
