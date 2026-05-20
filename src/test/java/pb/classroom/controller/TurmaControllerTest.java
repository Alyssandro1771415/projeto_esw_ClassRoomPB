package pb.classroom.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pb.classroom.model.BlocoHorario;
import pb.classroom.model.Curso;
import pb.classroom.model.Disciplina;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.PeriodoLetivo;
import pb.classroom.model.Turma;
import pb.classroom.model.Usuario;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TurmaController - RF10 a RF14")
class TurmaControllerTest {

    private static final String SENHA = "senha";

    private Usuario coordenador;
    private Usuario professor;
    private Usuario outroProfessor;
    private Usuario aluno;
    private Disciplina disciplina;
    private PeriodoLetivo periodoLetivo;
    private AutenticacaoController autenticacaoController;
    private TurmaController turmaController;

    @BeforeEach
    void setUp() {
        coordenador = new Usuario(
                "coord-1",
                PerfilUsuario.COORDENADOR,
                "Coord Curso",
                "C001",
                "coord@classroompb.com",
                SENHA,
                true);
        professor = new Usuario(
                "prof-1",
                PerfilUsuario.PROFESSOR,
                "Professor Um",
                "P001",
                "prof1@classroompb.com",
                SENHA,
                true);
        outroProfessor = new Usuario(
                "prof-2",
                PerfilUsuario.PROFESSOR,
                "Professor Dois",
                "P002",
                "prof2@classroompb.com",
                SENHA,
                true);
        aluno = new Usuario(
                "aluno-1",
                PerfilUsuario.ALUNO,
                "Aluno Teste",
                "A001",
                "aluno@classroompb.com",
                SENHA,
                true);

        Curso curso = new Curso("curso-1", "Engenharia de Software", "ESW");
        disciplina = new Disciplina("disc-1", "ESW101", "Projeto de Software", 60, 4, curso.getId(), List.of());
        periodoLetivo = new PeriodoLetivo("periodo-1", "2026.2", true);
        autenticacaoController = new AutenticacaoController(List.of(coordenador, professor, outroProfessor, aluno));
        turmaController = new TurmaController(
                autenticacaoController,
                List.of(),
                List.of(disciplina),
                List.of(periodoLetivo),
                autenticacaoController.getUsuarios());
    }

    @Test
    @DisplayName("RF10: coordenador oferta turma para disciplina e periodo existentes")
    void coordenadorOfertaTurmaParaDisciplinaEPeriodoExistentes() {
        autenticacaoController.login("C001", SENHA);

        Turma turma = turmaController.ofertarTurma(
                disciplina.getId(),
                periodoLetivo.getId(),
                professor.getId(),
                30,
                "Sala 101",
                LocalDate.now().plusDays(10),
                horarios("08:00", "10:00"));

        assertAll(
                () -> assertEquals(disciplina.getId(), turma.getIdDisciplina()),
                () -> assertEquals(periodoLetivo.getId(), turma.getIdPeriodoLetivo()),
                () -> assertEquals(professor.getId(), turma.getIdProfessor()),
                () -> assertTrue(turmaController.getTurmas().contains(turma)));
    }

    @Test
    @DisplayName("RF10: usuario sem perfil coordenador nao oferta turma")
    void usuarioSemPerfilCoordenadorNaoOfertaTurma() {
        autenticacaoController.login("A001", SENHA);

        assertThrows(
                IllegalArgumentException.class,
                () -> turmaController.ofertarTurma(
                        disciplina.getId(),
                        periodoLetivo.getId(),
                        professor.getId(),
                        30,
                        "Sala 101",
                        LocalDate.now().plusDays(10),
                        horarios("08:00", "10:00")));
    }

    @Test
    @DisplayName("RF10: nao oferta turma com disciplina ou periodo inexistente")
    void naoOfertaTurmaComDisciplinaOuPeriodoInexistente() {
        autenticacaoController.login("C001", SENHA);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> turmaController.ofertarTurma(
                                "disciplina-inexistente",
                                periodoLetivo.getId(),
                                professor.getId(),
                                30,
                                "Sala 101",
                                LocalDate.now().plusDays(10),
                                horarios("08:00", "10:00"))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> turmaController.ofertarTurma(
                                disciplina.getId(),
                                "periodo-inexistente",
                                professor.getId(),
                                30,
                                "Sala 101",
                                LocalDate.now().plusDays(10),
                                horarios("08:00", "10:00"))));
    }

    @Test
    @DisplayName("RF11: turma exige professor, vagas, sala e horario validos")
    void turmaExigeProfessorVagasSalaEHorarioValidos() {
        autenticacaoController.login("C001", SENHA);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> turmaController.ofertarTurma(
                                disciplina.getId(),
                                periodoLetivo.getId(),
                                professor.getId(),
                                0,
                                "Sala 101",
                                LocalDate.now().plusDays(10),
                                horarios("08:00", "10:00"))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> turmaController.ofertarTurma(
                                disciplina.getId(),
                                periodoLetivo.getId(),
                                professor.getId(),
                                30,
                                "   ",
                                LocalDate.now().plusDays(10),
                                horarios("08:00", "10:00"))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> turmaController.ofertarTurma(
                                disciplina.getId(),
                                periodoLetivo.getId(),
                                professor.getId(),
                                30,
                                "Sala 101",
                                LocalDate.now().plusDays(10),
                                List.of())),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new BlocoHorario(DayOfWeek.MONDAY, LocalTime.parse("10:00"), LocalTime.parse("08:00"))));
    }

    @Test
    @DisplayName("RF12: impede choque de horario para o mesmo professor")
    void impedeChoqueDeHorarioParaMesmoProfessor() {
        autenticacaoController.login("C001", SENHA);
        turmaController.ofertarTurma(
                disciplina.getId(),
                periodoLetivo.getId(),
                professor.getId(),
                30,
                "Sala 101",
                LocalDate.now().plusDays(10),
                horarios("08:00", "10:00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> turmaController.ofertarTurma(
                        disciplina.getId(),
                        periodoLetivo.getId(),
                        professor.getId(),
                        30,
                        "Sala 102",
                        LocalDate.now().plusDays(10),
                        horarios("09:00", "11:00")));
    }

    @Test
    @DisplayName("RF12: permite professor diferente ou horarios sem sobreposicao")
    void permiteProfessorDiferenteOuHorariosSemSobreposicao() {
        autenticacaoController.login("C001", SENHA);
        turmaController.ofertarTurma(
                disciplina.getId(),
                periodoLetivo.getId(),
                professor.getId(),
                30,
                "Sala 101",
                LocalDate.now().plusDays(10),
                horarios("08:00", "10:00"));

        assertAll(
                () -> assertDoesNotThrow(() -> turmaController.ofertarTurma(
                        disciplina.getId(),
                        periodoLetivo.getId(),
                        professor.getId(),
                        30,
                        "Sala 102",
                        LocalDate.now().plusDays(10),
                        horarios("10:00", "12:00"))),
                () -> assertDoesNotThrow(() -> turmaController.ofertarTurma(
                        disciplina.getId(),
                        periodoLetivo.getId(),
                        outroProfessor.getId(),
                        30,
                        "Sala 103",
                        LocalDate.now().plusDays(10),
                        horarios("09:00", "11:00"))));
    }

    @Test
    @DisplayName("RF13: professor responsavel deve existir e ter perfil PROFESSOR")
    void professorResponsavelDeveExistirETerPerfilProfessor() {
        autenticacaoController.login("C001", SENHA);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> turmaController.ofertarTurma(
                                disciplina.getId(),
                                periodoLetivo.getId(),
                                null,
                                30,
                                "Sala 101",
                                LocalDate.now().plusDays(10),
                                horarios("08:00", "10:00"))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> turmaController.ofertarTurma(
                                disciplina.getId(),
                                periodoLetivo.getId(),
                                "professor-inexistente",
                                30,
                                "Sala 101",
                                LocalDate.now().plusDays(10),
                                horarios("08:00", "10:00"))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> turmaController.ofertarTurma(
                                disciplina.getId(),
                                periodoLetivo.getId(),
                                aluno.getId(),
                                30,
                                "Sala 101",
                                LocalDate.now().plusDays(10),
                                horarios("08:00", "10:00"))));
    }

    @Test
    @DisplayName("RF14: coordenador altera e cancela turma antes do inicio")
    void coordenadorAlteraECancelaTurmaAntesDoInicio() {
        autenticacaoController.login("C001", SENHA);
        Turma turma = turmaController.ofertarTurma(
                disciplina.getId(),
                periodoLetivo.getId(),
                professor.getId(),
                30,
                "Sala 101",
                LocalDate.now().plusDays(10),
                horarios("08:00", "10:00"));

        turmaController.alterarTurma(
                turma.getId(),
                outroProfessor.getId(),
                25,
                "Sala 202",
                LocalDate.now().plusDays(12),
                horarios("14:00", "16:00"));
        turmaController.cancelarTurma(turma.getId());

        assertAll(
                () -> assertEquals(outroProfessor.getId(), turma.getIdProfessor()),
                () -> assertEquals(25, turma.getLimiteVagas()),
                () -> assertEquals("Sala 202", turma.getSala()),
                () -> assertTrue(turma.isCancelada()));
    }

    @Test
    @DisplayName("RF14: nao altera nem cancela turma apos inicio")
    void naoAlteraNemCancelaTurmaAposInicio() {
        autenticacaoController.login("C001", SENHA);
        Turma turma = turmaController.ofertarTurma(
                disciplina.getId(),
                periodoLetivo.getId(),
                professor.getId(),
                30,
                "Sala 101",
                LocalDate.now().minusDays(1),
                horarios("08:00", "10:00"));

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> turmaController.alterarTurma(
                                turma.getId(),
                                outroProfessor.getId(),
                                25,
                                "Sala 202",
                                LocalDate.now().plusDays(12),
                                horarios("14:00", "16:00"))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> turmaController.cancelarTurma(turma.getId())));
    }

    @Test
    @DisplayName("RF14: nao altera nem cancela turma inexistente")
    void naoAlteraNemCancelaTurmaInexistente() {
        autenticacaoController.login("C001", SENHA);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> turmaController.alterarTurma(
                                "turma-inexistente",
                                professor.getId(),
                                30,
                                "Sala 101",
                                LocalDate.now().plusDays(10),
                                horarios("08:00", "10:00"))),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> turmaController.cancelarTurma("turma-inexistente")));
    }

    private List<BlocoHorario> horarios(String inicio, String fim) {
        return List.of(new BlocoHorario(DayOfWeek.MONDAY, LocalTime.parse(inicio), LocalTime.parse(fim)));
    }
}
