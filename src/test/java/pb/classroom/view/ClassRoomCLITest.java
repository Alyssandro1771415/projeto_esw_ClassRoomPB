package pb.classroom.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pb.classroom.repository.CursoRepository;
import pb.classroom.repository.DisciplinaRepository;
import pb.classroom.repository.MatriculaRepository;
import pb.classroom.repository.PeriodoLetivoRepository;
import pb.classroom.repository.TurmaRepository;
import pb.classroom.repository.UsuarioRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ClassRoomCLI - fluxos principais da interface")
class ClassRoomCLITest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("opcao zero encerra o sistema")
    void opcaoZeroEncerraSistema() {
        String entrada = "0\n";
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        PrintStream anterior = System.out;
        System.setOut(new PrintStream(saida));

        try {
            ClassRoomCLI cli = new ClassRoomCLI(
                    new Scanner(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8))),
                    new UsuarioRepository(tempDir.resolve("u.json")),
                    new DisciplinaRepository(tempDir.resolve("d.json")),
                    new CursoRepository(tempDir.resolve("c.json")),
                    new PeriodoLetivoRepository(tempDir.resolve("p.json")),
                    new TurmaRepository(tempDir.resolve("t.json")),
                    new MatriculaRepository(tempDir.resolve("m.json")));
            cli.iniciar();
        } finally {
            System.setOut(anterior);
        }

        assertTrue(saida.toString(StandardCharsets.UTF_8).contains("Sistema encerrado."));
    }

    @Test
    @DisplayName("opcao invalida exibe mensagem e continua ate sair")
    void opcaoInvalidaExibeMensagem() {
        String entrada = "99\n\n0\n";
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        PrintStream anterior = System.out;
        System.setOut(new PrintStream(saida));

        try {
            ClassRoomCLI cli = new ClassRoomCLI(
                    new Scanner(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8))),
                    new UsuarioRepository(tempDir.resolve("u2.json")),
                    new DisciplinaRepository(tempDir.resolve("d2.json")),
                    new CursoRepository(tempDir.resolve("c2.json")),
                    new PeriodoLetivoRepository(tempDir.resolve("p2.json")),
                    new TurmaRepository(tempDir.resolve("t2.json")),
                    new MatriculaRepository(tempDir.resolve("m2.json")));
            cli.iniciar();
        } finally {
            System.setOut(anterior);
        }

        String texto = saida.toString(StandardCharsets.UTF_8);
        assertTrue(texto.contains("Opção inválida."));
        assertTrue(texto.contains("Sistema encerrado."));
    }
}
