package pb.classroom.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pb.classroom.model.BlocoHorario;
import pb.classroom.model.Curso;
import pb.classroom.model.Disciplina;
import pb.classroom.model.Matricula;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.PeriodoLetivo;
import pb.classroom.model.StatusMatricula;
import pb.classroom.model.Turma;
import pb.classroom.model.Usuario;

@DisplayName("MatriculaRepository - persistencia de matriculas")
class MatriculaRepositoryTest {

  @TempDir Path tempDir;

  @Test
  @DisplayName("salva e carrega matriculas")
  void salvaECarregaMatriculas() {
    Path arquivo = tempDir.resolve("armazenamento.json");
    MatriculaRepository repository = new MatriculaRepository(arquivo);
    Matricula matricula = new Matricula("mat-1", "aluno-1", "turma-1");

    repository.salvarMatriculas(List.of(matricula));

    List<Matricula> carregadas = repository.carregarMatriculas();
    assertAll(
        () -> assertEquals(1, carregadas.size()),
        () -> assertEquals(matricula.getId(), carregadas.get(0).getId()),
        () -> assertEquals(matricula.getIdAluno(), carregadas.get(0).getIdAluno()),
        () -> assertEquals(matricula.getIdTurma(), carregadas.get(0).getIdTurma()),
        () -> assertEquals(StatusMatricula.CONFIRMADA, carregadas.get(0).getStatus()));
  }

  @Test
  @DisplayName("salva e carrega status de lista de espera")
  void salvaECarregaStatusDeListaDeEspera() {
    Path arquivo = tempDir.resolve("armazenamento-status.json");
    MatriculaRepository repository = new MatriculaRepository(arquivo);
    Matricula matricula = new Matricula("mat-1", "aluno-1", "turma-1", StatusMatricula.EM_ESPERA);

    repository.salvarMatriculas(List.of(matricula));

    List<Matricula> carregadas = repository.carregarMatriculas();
    assertEquals(StatusMatricula.EM_ESPERA, carregadas.get(0).getStatus());
  }

  @Test
  @DisplayName("salvar matriculas preserva demais colecoes")
  void salvarMatriculasPreservaDemaisColecoes() throws Exception {
    Path arquivo = tempDir.resolve("armazenamento.json");
    Files.writeString(
        arquivo,
        "{\"usuarios\":[{\"id\":\"u1\"}],\"disciplinas\":[{\"id\":\"d1\"}],"
            + "\"cursos\":[{\"id\":\"c1\"}],\"periodosLetivos\":[{\"id\":\"p1\"}],"
            + "\"turmas\":[{\"id\":\"t1\"}]}",
        StandardCharsets.UTF_8);

    new MatriculaRepository(arquivo)
        .salvarMatriculas(List.of(new Matricula("mat-1", "aluno-1", "turma-1")));

    String conteudo = Files.readString(arquivo, StandardCharsets.UTF_8);
    assertAll(
        () -> assertTrue(conteudo.contains("\"usuarios\": [{\"id\":\"u1\"}]")),
        () -> assertTrue(conteudo.contains("\"disciplinas\": [{\"id\":\"d1\"}]")),
        () -> assertTrue(conteudo.contains("\"cursos\": [{\"id\":\"c1\"}]")),
        () -> assertTrue(conteudo.contains("\"periodosLetivos\": [{\"id\":\"p1\"}]")),
        () -> assertTrue(conteudo.contains("\"turmas\": [{\"id\":\"t1\"}]")),
        () -> assertTrue(conteudo.contains("\"matriculas\"")));
  }

  @Test
  @DisplayName("demais repositories preservam matriculas existentes")
  void demaisRepositoriesPreservamMatriculasExistentes() {
    Path arquivo = tempDir.resolve("armazenamento-compartilhado.json");
    MatriculaRepository matriculaRepository = new MatriculaRepository(arquivo);
    matriculaRepository.salvarMatriculas(List.of(new Matricula("mat-1", "aluno-1", "turma-1")));

    new UsuarioRepository(arquivo)
        .salvarUsuarios(
            List.of(
                new Usuario(
                    "aluno-1",
                    PerfilUsuario.ALUNO,
                    "Aluno",
                    "A001",
                    "aluno@classroompb.com",
                    "senha",
                    true)));
    assertEquals(1, matriculaRepository.carregarMatriculas().size());

    new CursoRepository(arquivo).salvarCursos(List.of(new Curso("curso-1", "Curso", "C")));
    assertEquals(1, matriculaRepository.carregarMatriculas().size());

    new DisciplinaRepository(arquivo)
        .salvarDisciplinas(
            List.of(new Disciplina("disc-1", "D01", "Disciplina", 60, 4, "curso-1", List.of())));
    assertEquals(1, matriculaRepository.carregarMatriculas().size());

    new PeriodoLetivoRepository(arquivo)
        .salvarPeriodosLetivos(List.of(new PeriodoLetivo("periodo-1", "2026.2", true)));
    assertEquals(1, matriculaRepository.carregarMatriculas().size());

    new TurmaRepository(arquivo)
        .salvarTurmas(
            List.of(
                new Turma(
                    "turma-1",
                    "disc-1",
                    "periodo-1",
                    "prof-1",
                    30,
                    "Sala 101",
                    LocalDate.of(2026, 8, 1),
                    List.of(
                        new BlocoHorario(
                            DayOfWeek.MONDAY, LocalTime.parse("08:00"), LocalTime.parse("10:00"))),
                    false)));
    assertEquals(1, matriculaRepository.carregarMatriculas().size());
  }
}
