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
import pb.classroom.model.Curso;

public class CursoRepository {

  private static final Pattern OBJETO_JSON = Pattern.compile("\\{([^{}]*)\\}");
  private static final String ARQUIVO_PADRAO = "armazenamento_interno.json";

  private final Path caminhoArquivo;

  public CursoRepository() {
    this(Paths.get(ARQUIVO_PADRAO));
  }

  public CursoRepository(Path caminhoArquivo) {
    this.caminhoArquivo = caminhoArquivo;
  }

  public List<Curso> carregarCursos() {
    try {
      if (!Files.exists(caminhoArquivo)) {
        return new ArrayList<>();
      }

      String conteudo = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
      String cursosJson = ArmazenamentoJson.extrairArrayOuVazio(conteudo, "cursos");
      return converterJsonParaCursos(cursosJson);
    } catch (IOException e) {
      throw new IllegalStateException("Não foi possível carregar os cursos.", e);
    }
  }

  public void salvarCursos(List<Curso> cursos) {
    try {
      String conteudoAtual = "";
      if (Files.exists(caminhoArquivo)) {
        conteudoAtual = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
      }

      String usuariosJson = ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "usuarios");
      String disciplinasJson = ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "disciplinas");
      String periodosLetivosJson =
          ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "periodosLetivos");
      String turmasJson = ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "turmas");
      String matriculasJson = ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "matriculas");
      String documento =
          ArmazenamentoJson.montarDocumento(
              usuariosJson,
              disciplinasJson,
              converterCursosParaJson(cursos),
              periodosLetivosJson,
              turmasJson,
              matriculasJson);
      Files.write(caminhoArquivo, documento.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new IllegalStateException("Não foi possível salvar os cursos.", e);
    }
  }

  private List<Curso> converterJsonParaCursos(String conteudo) {
    List<Curso> cursos = new ArrayList<>();
    Matcher matcher = OBJETO_JSON.matcher(conteudo);

    while (matcher.find()) {
      String objeto = matcher.group(1);
      if (!objeto.contains("\"nome\"")) {
        continue;
      }

      String id = obterTexto(objeto, "id");
      String nome = obterTexto(objeto, "nome");
      String codigo = obterTextoOpcional(objeto, "codigo");
      cursos.add(new Curso(id, nome, codigo));
    }

    return cursos;
  }

  private String converterCursosParaJson(List<Curso> cursos) {
    StringBuilder json = new StringBuilder();
    json.append("[\n");

    for (int i = 0; i < cursos.size(); i++) {
      Curso curso = cursos.get(i);
      json.append("    {\n");
      json.append("      \"id\": \"").append(escapar(curso.getId())).append("\",\n");
      json.append("      \"nome\": \"").append(escapar(curso.getNome())).append("\",\n");
      json.append("      \"codigo\": ");
      if (curso.getCodigo() == null) {
        json.append("null\n");
      } else {
        json.append("\"").append(escapar(curso.getCodigo())).append("\"\n");
      }
      json.append("    }");

      if (i < cursos.size() - 1) {
        json.append(",");
      }
      json.append("\n");
    }

    json.append("  ]");
    return json.toString();
  }

  private String obterTexto(String objeto, String campo) {
    Matcher matcher =
        Pattern.compile("\"" + campo + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(objeto);
    if (!matcher.find()) {
      throw new IllegalArgumentException("Campo obrigatório ausente no armazenamento: " + campo);
    }
    return desescapar(matcher.group(1));
  }

  private String obterTextoOpcional(String objeto, String campo) {
    Matcher nulo = Pattern.compile("\"" + campo + "\"\\s*:\\s*null").matcher(objeto);
    if (nulo.find()) {
      return null;
    }

    Matcher matcher =
        Pattern.compile("\"" + campo + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(objeto);
    if (!matcher.find()) {
      return null;
    }
    return desescapar(matcher.group(1));
  }

  private String escapar(String valor) {
    return valor.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private String desescapar(String valor) {
    return valor.replace("\\\"", "\"").replace("\\\\", "\\");
  }
}
