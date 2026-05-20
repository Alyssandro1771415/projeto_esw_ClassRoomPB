package pb.classroom.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pb.classroom.model.BlocoHorario;
import pb.classroom.model.Turma;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TurmaRepository - persistencia de turmas")
class TurmaRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("salva e carrega turmas preservando horarios e status")
    void salvaECarregaTurmasPreservandoHorariosEStatus() throws Exception {
        Path arquivo = tempDir.resolve("armazenamento.json");
        TurmaRepository repository = new TurmaRepository(arquivo);
        Turma turma = new Turma(
                "turma-1",
                "disc-1",
                "periodo-1",
                "prof-1",
                30,
                "Sala 101",
                LocalDate.of(2026, 8, 1),
                List.of(new BlocoHorario(DayOfWeek.MONDAY, LocalTime.parse("08:00"), LocalTime.parse("10:00"))),
                true);

        repository.salvarTurmas(List.of(turma));

        List<Turma> carregadas = repository.carregarTurmas();
        assertAll(
                () -> assertEquals(1, carregadas.size()),
                () -> assertEquals(turma.getId(), carregadas.get(0).getId()),
                () -> assertEquals(turma.getIdProfessor(), carregadas.get(0).getIdProfessor()),
                () -> assertEquals(turma.getHorarios(), carregadas.get(0).getHorarios()),
                () -> assertTrue(carregadas.get(0).isCancelada()));
    }

    @Test
    @DisplayName("salvar turmas preserva outros arrays do armazenamento")
    void salvarTurmasPreservaOutrosArraysDoArmazenamento() throws Exception {
        Path arquivo = tempDir.resolve("armazenamento.json");
        Files.writeString(
                arquivo,
                "{\n"
                        + "  \"usuarios\": [{\"id\":\"u1\"}],\n"
                        + "  \"disciplinas\": [{\"id\":\"d1\"}],\n"
                        + "  \"cursos\": [{\"id\":\"c1\"}],\n"
                        + "  \"periodosLetivos\": [{\"id\":\"p1\"}]\n"
                        + "}\n",
                StandardCharsets.UTF_8);
        TurmaRepository repository = new TurmaRepository(arquivo);

        repository.salvarTurmas(List.of(new Turma(
                "disc-1",
                "periodo-1",
                "prof-1",
                30,
                "Sala 101",
                LocalDate.of(2026, 8, 1),
                List.of(new BlocoHorario(DayOfWeek.MONDAY, LocalTime.parse("08:00"), LocalTime.parse("10:00"))))));

        String conteudo = Files.readString(arquivo, StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(conteudo.contains("\"usuarios\": [{\"id\":\"u1\"}]")),
                () -> assertTrue(conteudo.contains("\"disciplinas\": [{\"id\":\"d1\"}]")),
                () -> assertTrue(conteudo.contains("\"cursos\": [{\"id\":\"c1\"}]")),
                () -> assertTrue(conteudo.contains("\"periodosLetivos\": [{\"id\":\"p1\"}]")),
                () -> assertTrue(conteudo.contains("\"turmas\"")));
    }
}
