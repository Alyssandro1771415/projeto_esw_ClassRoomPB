package pb.classroom.controller;

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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Confirmar Matrícula Automática")
class ConfirmarMatriculaAutomaticaTest {

        private static final String SENHA = "senha";

        private Usuario aluno;
        private AutenticacaoController autenticacaoController;
        private PeriodoLetivo periodoAtivo;
        private Disciplina discCalculo;
        private Disciplina discFisica;
        private Disciplina discMecanica;

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
                autenticacaoController = new AutenticacaoController(List.of(aluno));
                autenticacaoController.login("A001", SENHA);

                periodoAtivo = new PeriodoLetivo("periodo-1", "2026.1", true);
                discCalculo = new Disciplina("disc-calculo", "CAL01", "Cálculo I", 60, 4, "curso-1", List.of());
                discFisica = new Disciplina(
                                "disc-fisica",
                                "FIS01",
                                "Física I",
                                60,
                                4,
                                "curso-1",
                                List.of("disc-calculo"));
                discMecanica = new Disciplina(
                                "disc-mecanica",
                                "MEC01",
                                "Mecânica",
                                60,
                                4,
                                "curso-1",
                                List.of("disc-calculo", "disc-fisica"));
        }

        @Test
        @DisplayName("RF20: matrícula confirmada quando há vagas disponíveis")
        void matriculaConfirmadaQuandoHaVagas() {
                Turma turma = criarTurma("turma-1", "disc-calculo", 30);
                MatriculaController controller = criarController(List.of(), List.of(turma), List.of(discCalculo));

                Matricula matricula = controller.solicitarMatricula(turma.getId());

                assertAll(
                                () -> assertEquals(StatusMatricula.CONFIRMADA, matricula.getStatus()),
                                () -> assertEquals(aluno.getId(), matricula.getIdAluno()),
                                () -> assertEquals(turma.getId(), matricula.getIdTurma()),
                                () -> assertEquals(1, controller.getMatriculas().size()));
        }

        @Test
        @DisplayName("RF21: matrícula fica em lista de espera quando turma está lotada")
        void matriculaEmEsperaQuandoTurmaLotada() {
                Turma turma = criarTurma("turma-1", "disc-calculo", 2);
                Matricula mat1 = new Matricula("mat-1", "outro-aluno-1", turma.getId());
                Matricula mat2 = new Matricula("mat-2", "outro-aluno-2", turma.getId());
                MatriculaController controller = criarController(
                                List.of(mat1, mat2),
                                List.of(turma),
                                List.of(discCalculo));

                Matricula matricula = controller.solicitarMatricula(turma.getId());

                assertAll(
                                () -> assertEquals(StatusMatricula.EM_ESPERA, matricula.getStatus()),
                                () -> assertEquals(List.of(matricula), controller.consultarListaEspera(turma.getId())));
        }

        @Test
        @DisplayName("RF20: matrícula confirmada no limite exato de vagas")
        void matriculaConfirmadaNoLimiteExatoDeVagas() {
                Turma turma = criarTurma("turma-1", "disc-calculo", 1);
                MatriculaController controller = criarController(List.of(), List.of(turma), List.of(discCalculo));

                Matricula matricula = controller.solicitarMatricula(turma.getId());

                assertAll(
                                () -> assertEquals(StatusMatricula.CONFIRMADA, matricula.getStatus()),
                                () -> assertEquals(1, controller.getMatriculas().size()));
        }

        @Test
        @DisplayName("RF21: segunda matrícula fica em espera quando última vaga foi preenchida")
        void segundaMatriculaFicaEmEsperaQuandoUltimaVagaPreenchida() {
                Turma turma = criarTurma("turma-1", "disc-calculo", 1);
                Matricula existente = new Matricula("mat-1", "outro-aluno-1", turma.getId());
                MatriculaController controller = criarController(
                                List.of(existente),
                                List.of(turma),
                                List.of(discCalculo));

                Matricula matricula = controller.solicitarMatricula(turma.getId());

                assertEquals(StatusMatricula.EM_ESPERA, matricula.getStatus());
        }

        @Test
        @DisplayName("RF18/RF20: matrícula confirmada quando disciplina não possui pre-requisitos")
        void matriculaConfirmadaSemPreRequisitos() {
                Turma turmaCalculo = criarTurma("turma-cal", "disc-calculo", 30);
                MatriculaController controller = criarController(List.of(), List.of(turmaCalculo), List.of(discCalculo));

                Matricula matricula = controller.solicitarMatricula(turmaCalculo.getId());

                assertEquals(StatusMatricula.CONFIRMADA, matricula.getStatus());
        }

        @Test
        @DisplayName("RF18/RF20: matrícula confirmada quando pre-requisito atendido")
        void matriculaConfirmadaComPreRequisitoAtendido() {
                Turma turmaCalculo = criarTurma("turma-cal", "disc-calculo", 30);
                Turma turmaFisica = criarTurmaDiaDiferente("turma-fis", "disc-fisica", 30);
                Matricula matCalculo = new Matricula("mat-cal", aluno.getId(), turmaCalculo.getId());
                MatriculaController controller = criarController(
                                List.of(matCalculo),
                                List.of(turmaCalculo, turmaFisica),
                                List.of(discCalculo, discFisica));

                Matricula matricula = controller.solicitarMatricula(turmaFisica.getId());

                assertEquals(StatusMatricula.CONFIRMADA, matricula.getStatus());
        }

        @Test
        @DisplayName("RF18: matrícula rejeitada quando pre-requisito não atendido")
        void matriculaRejeitadaComPreRequisitoNaoAtendido() {
                Turma turmaFisica = criarTurma("turma-fis", "disc-fisica", 30);
                MatriculaController controller = criarController(
                                List.of(),
                                List.of(turmaFisica),
                                List.of(discCalculo, discFisica));

                IllegalArgumentException erro = assertThrows(
                                IllegalArgumentException.class,
                                () -> controller.solicitarMatricula(turmaFisica.getId()));

                assertAll(
                                () -> assertTrue(erro.getMessage().contains("Pré-requisito não atendido")),
                                () -> assertTrue(erro.getMessage().contains("CAL01")));
        }

        @Test
        @DisplayName("RF18/RF20: matrícula confirmada com múltiplos pre-requisitos atendidos")
        void matriculaConfirmadaComMultiplosPreRequisitosAtendidos() {
                Turma turmaCalculo = criarTurma("turma-cal", "disc-calculo", 30);
                Turma turmaFisica = criarTurmaDiaDiferente("turma-fis", "disc-fisica", 30);
                Turma turmaMecanica = criarTurma("turma-mec", "disc-mecanica", 30,
                                DayOfWeek.WEDNESDAY, "14:00", "16:00");
                Matricula matCalculo = new Matricula("mat-cal", aluno.getId(), turmaCalculo.getId());
                Matricula matFisica = new Matricula("mat-fis", aluno.getId(), turmaFisica.getId());
                MatriculaController controller = criarController(
                                List.of(matCalculo, matFisica),
                                List.of(turmaCalculo, turmaFisica, turmaMecanica),
                                List.of(discCalculo, discFisica, discMecanica));

                Matricula matricula = controller.solicitarMatricula(turmaMecanica.getId());

                assertEquals(StatusMatricula.CONFIRMADA, matricula.getStatus());
        }

        @Test
        @DisplayName("RF18: matrícula rejeitada com múltiplos pre-requisitos quando um não atendido")
        void matriculaRejeitadaComMultiplosPreRequisitosUmNaoAtendido() {
                Turma turmaCalculo = criarTurma("turma-cal", "disc-calculo", 30);
                Turma turmaMecanica = criarTurmaDiaDiferente("turma-mec", "disc-mecanica", 30);
                Matricula matCalculo = new Matricula("mat-cal", aluno.getId(), turmaCalculo.getId());
                MatriculaController controller = criarController(
                                List.of(matCalculo),
                                List.of(turmaCalculo, turmaMecanica),
                                List.of(discCalculo, discFisica, discMecanica));

                IllegalArgumentException erro = assertThrows(
                                IllegalArgumentException.class,
                                () -> controller.solicitarMatricula(turmaMecanica.getId()));

                assertAll(
                                () -> assertTrue(erro.getMessage().contains("Pré-requisito não atendido")),
                                () -> assertTrue(erro.getMessage().contains("FIS01")));
        }

        private MatriculaController criarController(
                        List<Matricula> matriculas,
                        List<Turma> turmas,
                        List<Disciplina> disciplinas) {
                return new MatriculaController(
                                autenticacaoController,
                                matriculas,
                                turmas,
                                List.of(periodoAtivo),
                                disciplinas);
        }

        private Turma criarTurma(String id, String idDisciplina, int limiteVagas) {
                return criarTurma(id, idDisciplina, limiteVagas, DayOfWeek.MONDAY, "08:00", "10:00");
        }

        private Turma criarTurmaDiaDiferente(String id, String idDisciplina, int limiteVagas) {
                return criarTurma(id, idDisciplina, limiteVagas, DayOfWeek.TUESDAY, "08:00", "10:00");
        }

        private Turma criarTurma(
                        String id,
                        String idDisciplina,
                        int limiteVagas,
                        DayOfWeek dia,
                        String inicio,
                        String fim) {
                return new Turma(
                                id,
                                idDisciplina,
                                periodoAtivo.getId(),
                                "prof-1",
                                limiteVagas,
                                "Sala 101",
                                LocalDate.of(2026, 8, 1),
                                List.of(new BlocoHorario(dia, LocalTime.parse(inicio), LocalTime.parse(fim))),
                                false);
        }
}
