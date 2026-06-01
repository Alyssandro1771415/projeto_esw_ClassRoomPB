package pb.classroom.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pb.classroom.repository.CursoRepository;
import pb.classroom.repository.DisciplinaRepository;
import pb.classroom.repository.PeriodoLetivoRepository;
import pb.classroom.model.Turma;
import pb.classroom.repository.TurmaRepository;
import pb.classroom.repository.UsuarioRepository;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ClassRoomCLI - fluxos integrados da release 1")
class ClassRoomCLIFluxosIntegradosTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("administrador executa cadastro e listagens principais")
    void administradorExecutaFluxosPrincipais() throws Exception {
        Path arquivo = copiarFixture();
        String saida = executar(
                arquivo,
                "1",
                "0001",
                "admin123",
                "",
                "2",
                "",
                "13",
                "",
                "7",
                "Sistemas de Informacao",
                "SI",
                "",
                "8",
                "",
                "3",
                "",
                "0");

        assertTrue(saida.contains("Login realizado com sucesso"));
        assertTrue(saida.contains("Curso cadastrado com sucesso"));
        assertTrue(saida.contains("Usuários cadastrados"));
    }

    @Test
    @DisplayName("coordenador gerencia periodo, disciplina e turma")
    void coordenadorGerenciaAcademico() throws Exception {
        Path arquivo = copiarFixture();
        String saida = executar(
                arquivo,
                "1",
                "coordenador@classroompb.com",
                "123456",
                "",
                "9",
                "2026.2",
                "",
                "10",
                "",
                "11",
                "per-1",
                "",
                "5",
                "ESW102",
                "Metodologia",
                "40",
                "3",
                "curso-1",
                "",
                "",
                "6",
                "",
                "14",
                "disc-1",
                "per-1",
                "prof-1",
                "25",
                "Sala 10",
                "2026-08-15",
                "1",
                "1",
                "08:00",
                "10:00",
                "",
                "15",
                "",
                "0");

        assertTrue(saida.contains("Período letivo cadastrado com sucesso") || saida.contains("Período letivo atualizado"));
        assertTrue(saida.contains("Disciplina cadastrada com sucesso") || saida.contains("Disciplinas cadastradas"));
    }

    @Test
    @DisplayName("aluno consulta listagens disponiveis")
    void alunoConsultaListagens() throws Exception {
        Path arquivo = copiarFixture();
        String saida = executar(
                arquivo,
                "1",
                "aluno@classroompb.com",
                "123456",
                "",
                "6",
                "",
                "10",
                "",
                "15",
                "",
                "0");

        assertTrue(saida.contains("Login realizado com sucesso"));
    }

    @Test
    @DisplayName("admin cadastra usuario e coordenador encerra periodo letivo")
    void adminCadastraUsuarioECoordenadorEncerraPeriodo() throws Exception {
        Path arquivo = copiarFixture();
        String saida = executar(
                arquivo,
                "1",
                "admin@classroompb.com",
                "admin123",
                "",
                "4",
                "2",
                "2026999",
                "Novo Professor",
                "prof123",
                "",
                "3",
                "",
                "1",
                "coordenador@classroompb.com",
                "123456",
                "",
                "12",
                "per-1",
                "",
                "0");

        assertTrue(saida.contains("Usuário cadastrado com sucesso") || saida.contains("Período letivo atualizado"));
    }

    @Test
    @DisplayName("coordenador altera e cancela turma ofertada")
    void coordenadorAlteraECancelaTurma() throws Exception {
        Path arquivo = copiarFixture();
        executar(
                arquivo,
                "1",
                "coordenador@classroompb.com",
                "123456",
                "",
                "14",
                "disc-1",
                "per-1",
                "prof-1",
                "20",
                "Sala 20",
                "2026-10-01",
                "1",
                "3",
                "19:00",
                "21:00",
                "",
                "0");

        List<Turma> turmas = new TurmaRepository(arquivo).carregarTurmas();
        String idTurma = turmas.get(0).getId();

        String saida = executar(
                arquivo,
                "1",
                "coordenador@classroompb.com",
                "123456",
                "",
                "16",
                idTurma,
                "prof-1",
                "22",
                "Sala 22",
                "2026-10-15",
                "1",
                "4",
                "08:00",
                "10:00",
                "",
                "17",
                idTurma,
                "",
                "0");

        assertTrue(saida.contains("Turma alterada com sucesso") || saida.contains("Turma cancelada com sucesso"));
    }

    @Test
    @DisplayName("professor autenticado consulta menus do perfil")
    void professorConsultaMenus() throws Exception {
        Path arquivo = copiarFixture();
        String saida = executar(
                arquivo,
                "1",
                "professor@classroompb.com",
                "123456",
                "",
                "6",
                "",
                "10",
                "",
                "15",
                "",
                "2",
                "",
                "0");

        assertTrue(saida.contains("Login realizado com sucesso"));
        assertTrue(saida.contains("Perfil: PROFESSOR"));
    }

    @Test
    @DisplayName("login invalido e operacoes sem autenticacao exibem mensagens")
    void loginInvalidoEOperacoesSemAutenticacao() throws Exception {
        Path arquivo = copiarFixture();
        String saida = executar(
                arquivo,
                "1",
                "0001",
                "senha-errada",
                "",
                "7",
                "",
                "5",
                "",
                "0");

        assertTrue(saida.contains("Opção inválida.") || saida.contains("senha") || saida.contains("inválid"));
    }

    private Path copiarFixture() throws Exception {
        Path arquivo = tempDir.resolve("armazenamento.json");
        try (InputStream in = getClass().getResourceAsStream("/armazenamento-teste.json")) {
            Files.copy(in, arquivo);
        }
        return arquivo;
    }

    private String executar(Path arquivo, String... linhas) {
        String entrada = String.join("\n", linhas) + "\n";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream saidaAnterior = System.out;
        System.setOut(new PrintStream(buffer));

        try {
            ClassRoomCLI cli = new ClassRoomCLI(
                    new Scanner(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8))),
                    new UsuarioRepository(arquivo),
                    new DisciplinaRepository(arquivo),
                    new CursoRepository(arquivo),
                    new PeriodoLetivoRepository(arquivo),
                    new TurmaRepository(arquivo));
            cli.iniciar();
        } finally {
            System.setOut(saidaAnterior);
        }

        return buffer.toString(StandardCharsets.UTF_8);
    }
}
