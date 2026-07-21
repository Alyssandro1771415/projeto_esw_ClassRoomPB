package pb.classroom.report;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("PdfRelatorioWriter")
class PdfRelatorioWriterTest {

  @TempDir Path tempDir;

  @Test
  @DisplayName("gera arquivo PDF válido com conteúdo")
  void geraArquivoPdfValido() throws Exception {
    Path destino = tempDir.resolve("subpasta/relatorio.pdf");
    Path gerado =
        PdfRelatorioWriter.escrever(destino, "Relatório de Teste", List.of("Linha 1", "Linha 2"));

    assertTrue(Files.exists(gerado));
    assertTrue(Files.size(gerado) > 0);
    byte[] cabecalho = Files.readAllBytes(gerado);
    assertEquals("%PDF", new String(cabecalho, 0, 4, StandardCharsets.US_ASCII));
  }

  @Test
  @DisplayName("aceita lista vazia sem falhar")
  void aceitaListaVazia() throws Exception {
    Path destino = tempDir.resolve("vazio.pdf");
    Path gerado = PdfRelatorioWriter.escrever(destino, "Vazio", List.of());
    assertTrue(Files.exists(gerado));
    assertTrue(Files.size(gerado) > 0);
  }
}
