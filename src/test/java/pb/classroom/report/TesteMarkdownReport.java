package pb.classroom.report;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TesteMarkdownReport {

  private static final Path REPORTS_DIR = Path.of("target", "surefire-reports");
  private static final Path OUTPUT = Path.of("TESTE.md");

  public static void main(String[] args) throws Exception {
    List<SuiteResult> suites = carregarSuites();
    escreverMarkdown(suites);
  }

  private static List<SuiteResult> carregarSuites() throws Exception {
    if (!Files.exists(REPORTS_DIR)) {
      throw new IllegalStateException(
          "Diretorio de relatorios do Surefire nao encontrado: " + REPORTS_DIR);
    }

    List<SuiteResult> suites = new ArrayList<>();
    try (var paths = Files.list(REPORTS_DIR)) {
      for (Path path :
          paths
              .filter(p -> p.getFileName().toString().startsWith("TEST-"))
              .filter(p -> p.getFileName().toString().endsWith(".xml"))
              .sorted()
              .collect(Collectors.toList())) {
        suites.add(lerSuite(path));
      }
    }
    suites.sort(Comparator.comparing(SuiteResult::getName));
    return suites;
  }

  private static SuiteResult lerSuite(Path path) throws Exception {
    Document document =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(path.toFile());
    Element suite = document.getDocumentElement();

    return new SuiteResult(
        suite.getAttribute("name"),
        inteiro(suite, "tests"),
        inteiro(suite, "failures"),
        inteiro(suite, "errors"),
        inteiro(suite, "skipped"));
  }

  private static int inteiro(Element element, String atributo) {
    return Integer.parseInt(element.getAttribute(atributo));
  }

  private static void escreverMarkdown(List<SuiteResult> suites) throws IOException {
    int totalTests = suites.stream().mapToInt(SuiteResult::getTests).sum();
    int totalFailures = suites.stream().mapToInt(SuiteResult::getFailures).sum();
    int totalErrors = suites.stream().mapToInt(SuiteResult::getErrors).sum();
    int totalSkipped = suites.stream().mapToInt(SuiteResult::getSkipped).sum();
    String build = totalFailures == 0 && totalErrors == 0 ? "SUCCESS" : "FAILURE";

    StringBuilder markdown = new StringBuilder();
    markdown.append("# Relatorio TESTE\n\n");
    markdown.append("Gerado automaticamente apos a execucao dos testes Maven.\n\n");
    markdown
        .append("Data/hora: ")
        .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        .append("\n\n");
    markdown.append("Comando esperado:\n\n");
    markdown.append("```bash\n");
    markdown.append("mvn test\n");
    markdown.append("# ou\n");
    markdown.append("mvn clean test\n");
    markdown.append("```\n\n");
    markdown.append("Resultado:\n\n");
    markdown.append("- Build: ").append(build).append("\n");
    markdown.append("- Total de testes: ").append(totalTests).append("\n");
    markdown.append("- Falhas: ").append(totalFailures).append("\n");
    markdown.append("- Erros: ").append(totalErrors).append("\n");
    markdown.append("- Ignorados: ").append(totalSkipped).append("\n\n");
    markdown.append("Suites executadas:\n\n");

    for (SuiteResult suite : suites) {
      markdown
          .append("- ")
          .append(suite.getName())
          .append(": ")
          .append(suite.getTests())
          .append(" testes, ")
          .append(suite.getFailures())
          .append(" falhas, ")
          .append(suite.getErrors())
          .append(" erros, ")
          .append(suite.getSkipped())
          .append(" ignorados\n");
    }

    Files.writeString(OUTPUT, markdown.toString(), StandardCharsets.UTF_8);
  }

  private static final class SuiteResult {
    private final String name;
    private final int tests;
    private final int failures;
    private final int errors;
    private final int skipped;

    private SuiteResult(String name, int tests, int failures, int errors, int skipped) {
      this.name = name;
      this.tests = tests;
      this.failures = failures;
      this.errors = errors;
      this.skipped = skipped;
    }

    private String getName() {
      return name;
    }

    private int getTests() {
      return tests;
    }

    private int getFailures() {
      return failures;
    }

    private int getErrors() {
      return errors;
    }

    private int getSkipped() {
      return skipped;
    }
  }
}
