package pb.classroom.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pb.classroom.model.RegistroNota;

public class NotaRepository {

  private static final Pattern OBJETO_JSON = Pattern.compile("\\{([^{}]*)\\}");
  private static final String ARQUIVO_PADRAO = "armazenamento_interno.json";

  private final Path caminhoArquivo;

  public NotaRepository() {
    this(Paths.get(ARQUIVO_PADRAO));
  }

  public NotaRepository(Path caminhoArquivo) {
    this.caminhoArquivo = caminhoArquivo;
  }

  public List<RegistroNota> carregarNotas() {
    try {
      if (!Files.exists(caminhoArquivo)) {
        return new ArrayList<>();
      }

      String conteudo = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
      String notasJson = ArmazenamentoJson.extrairArrayOuVazio(conteudo, "notas");
      return converterJsonParaNotas(notasJson);
    } catch (IOException e) {
      throw new IllegalStateException("Não foi possível carregar as notas.", e);
    }
  }

  public void salvarNotas(List<RegistroNota> notas) {
    try {
      String conteudoAtual = "";
      if (Files.exists(caminhoArquivo)) {
        conteudoAtual = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
      }

      String documento =
          ArmazenamentoJson.montarDocumento(
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "usuarios"),
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "disciplinas"),
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "cursos"),
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "periodosLetivos"),
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "turmas"),
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "matriculas"),
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "presencas"),
              converterNotasParaJson(notas),
              ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "historicos"));
      Files.write(caminhoArquivo, documento.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new IllegalStateException("Não foi possível salvar as notas.", e);
    }
  }

  private List<RegistroNota> converterJsonParaNotas(String conteudo) {
    List<RegistroNota> notas = new ArrayList<>();
    Matcher matcher = OBJETO_JSON.matcher(conteudo);

    while (matcher.find()) {
      String objeto = matcher.group(1);
      if (!objeto.contains("\"idTurma\"") || !objeto.contains("\"idAluno\"")) {
        continue;
      }
      notas.add(
          new RegistroNota(
              obterTexto(objeto, "id"),
              obterTexto(objeto, "idTurma"),
              obterTexto(objeto, "idAluno"),
              obterDoubleOpcional(objeto, "notaEtapa1"),
              obterDoubleOpcional(objeto, "notaEtapa2"),
              obterDoubleOpcional(objeto, "notaRecuperacao")));
    }
    return notas;
  }

  private String converterNotasParaJson(List<RegistroNota> notas) {
    StringBuilder json = new StringBuilder();
    json.append("[\n");

    for (int i = 0; i < notas.size(); i++) {
      RegistroNota nota = notas.get(i);
      json.append("    {\n");
      json.append("      \"id\": \"").append(escapar(nota.getId())).append("\",\n");
      json.append("      \"idTurma\": \"").append(escapar(nota.getIdTurma())).append("\",\n");
      json.append("      \"idAluno\": \"").append(escapar(nota.getIdAluno())).append("\",\n");
      adicionarNotaOpcional(json, "notaEtapa1", nota.getNotaEtapa1());
      adicionarNotaOpcional(json, "notaEtapa2", nota.getNotaEtapa2());
      adicionarNotaOpcional(json, "notaRecuperacao", nota.getNotaRecuperacao(), true);
      json.append("    }");
      if (i < notas.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }

    json.append("  ]");
    return json.toString();
  }

  private void adicionarNotaOpcional(StringBuilder json, String campo, Double valor) {
    adicionarNotaOpcional(json, campo, valor, false);
  }

  private void adicionarNotaOpcional(
      StringBuilder json, String campo, Double valor, boolean ultimoCampo) {
    if (valor == null) {
      json.append("      \"").append(campo).append("\": null");
    } else {
      json.append("      \"").append(campo).append("\": ").append(valor);
    }
    if (!ultimoCampo) {
      json.append(",\n");
    } else {
      json.append("\n");
    }
  }

  private String obterTexto(String objeto, String campo) {
    Matcher matcher =
        Pattern.compile("\"" + campo + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(objeto);
    if (!matcher.find()) {
      throw new IllegalArgumentException("Campo obrigatório ausente no armazenamento: " + campo);
    }
    return desescapar(matcher.group(1));
  }

  private Double obterDoubleOpcional(String objeto, String campo) {
    Matcher matcherNulo = Pattern.compile("\"" + campo + "\"\\s*:\\s*null").matcher(objeto);
    if (matcherNulo.find()) {
      return null;
    }
    Matcher matcher =
        Pattern.compile("\"" + campo + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)").matcher(objeto);
    if (!matcher.find()) {
      return null;
    }
    return Double.parseDouble(matcher.group(1));
  }

  private String escapar(String valor) {
    return valor.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private String desescapar(String valor) {
    return valor.replace("\\\"", "\"").replace("\\\\", "\\");
  }
}
