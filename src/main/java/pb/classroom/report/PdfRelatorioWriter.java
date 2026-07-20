package pb.classroom.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/** Gera arquivos PDF de relatórios acadêmicos (RF40–RF43). */
public final class PdfRelatorioWriter {

  private static final DateTimeFormatter FORMATO_DATA =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

  private PdfRelatorioWriter() {}

  public static Path escrever(Path destino, String titulo, List<String> linhas) {
    Objects.requireNonNull(destino, "destino é obrigatório");
    Objects.requireNonNull(titulo, "titulo é obrigatório");
    Objects.requireNonNull(linhas, "linhas são obrigatórias");

    try {
      Path pai = destino.getParent();
      if (pai != null) {
        Files.createDirectories(pai);
      }

      try (OutputStream output = Files.newOutputStream(destino)) {
        Document document = new Document();
        PdfWriter.getInstance(document, output);
        document.open();

        Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font corpoFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
        Font rodapeFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9);

        document.add(new Paragraph("ClassRoomPB", tituloFont));
        document.add(new Paragraph(titulo, tituloFont));
        document.add(
            new Paragraph("Gerado em: " + LocalDateTime.now().format(FORMATO_DATA), rodapeFont));
        document.add(new Paragraph(" "));

        if (linhas.isEmpty()) {
          document.add(new Paragraph("Nenhum registro encontrado.", corpoFont));
        } else {
          for (String linha : linhas) {
            document.add(new Paragraph(linha == null ? "" : linha, corpoFont));
          }
        }

        document.close();
      }
      return destino.toAbsolutePath().normalize();
    } catch (DocumentException | IOException e) {
      throw new IllegalStateException("Não foi possível gerar o PDF: " + e.getMessage(), e);
    }
  }
}
