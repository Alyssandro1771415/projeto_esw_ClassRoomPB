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
import pb.classroom.model.Disciplina;

public class DisciplinaRepository {

    private static final Pattern OBJETO_JSON = Pattern.compile("\\{([^{}]*)\\}");
    private static final String ARQUIVO_PADRAO = "armazenamento_interno.json";

    private final Path caminhoArquivo;

    public DisciplinaRepository() {
        this(Paths.get(ARQUIVO_PADRAO));
    }

    public DisciplinaRepository(Path caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }

    public List<Disciplina> carregarDisciplinas() {
        try {
            if (!Files.exists(caminhoArquivo)) {
                return new ArrayList<>();
            }

            String conteudo = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
            String disciplinasJson = ArmazenamentoJson.extrairArrayOuVazio(conteudo, "disciplinas");
            return converterJsonParaDisciplinas(disciplinasJson);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível carregar as disciplinas.", e);
        }
    }

    public void salvarDisciplinas(List<Disciplina> disciplinas) {
        try {
            String conteudoAtual = "";
            if (Files.exists(caminhoArquivo)) {
                conteudoAtual = new String(Files.readAllBytes(caminhoArquivo), StandardCharsets.UTF_8);
            }

            String usuariosJson = ArmazenamentoJson.extrairArrayOuVazio(conteudoAtual, "usuarios");
            String disciplinasJson = converterDisciplinasParaJson(disciplinas);
            String documento = ArmazenamentoJson.montarDocumento(usuariosJson, disciplinasJson);
            Files.write(caminhoArquivo, documento.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar as disciplinas.", e);
        }
    }

    private List<Disciplina> converterJsonParaDisciplinas(String conteudo) {
        List<Disciplina> disciplinas = new ArrayList<>();
        Matcher matcher = OBJETO_JSON.matcher(conteudo);

        while (matcher.find()) {
            String objeto = matcher.group(1);
            if (!objeto.contains("\"codigo\"") || !objeto.contains("\"cargaHoraria\"")) {
                continue;
            }

            String id = obterTexto(objeto, "id");
            String codigo = obterTexto(objeto, "codigo");
            String nome = obterTexto(objeto, "nome");
            int cargaHoraria = obterInteiro(objeto, "cargaHoraria");
            int creditos = obterInteiro(objeto, "creditos");
            String idCurso = obterTexto(objeto, "idCurso");
            List<String> preRequisitosIds = obterListaTexto(objeto, "preRequisitosIds");

            disciplinas.add(new Disciplina(id, codigo, nome, cargaHoraria, creditos, idCurso, preRequisitosIds));
        }

        return disciplinas;
    }

    private String converterDisciplinasParaJson(List<Disciplina> disciplinas) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        for (int i = 0; i < disciplinas.size(); i++) {
            Disciplina disciplina = disciplinas.get(i);
            json.append("    {\n");
            json.append("      \"id\": \"").append(escapar(disciplina.getId())).append("\",\n");
            json.append("      \"codigo\": \"").append(escapar(disciplina.getCodigo())).append("\",\n");
            json.append("      \"nome\": \"").append(escapar(disciplina.getNome())).append("\",\n");
            json.append("      \"cargaHoraria\": ").append(disciplina.getCargaHoraria()).append(",\n");
            json.append("      \"creditos\": ").append(disciplina.getCreditos()).append(",\n");
            json.append("      \"idCurso\": \"").append(escapar(disciplina.getIdCurso())).append("\",\n");
            json.append("      \"preRequisitosIds\": ");
            adicionarListaTexto(json, disciplina.getPreRequisitosIds());
            json.append("\n");
            json.append("    }");

            if (i < disciplinas.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]");
        return json.toString();
    }

    private String obterTexto(String objeto, String campo) {
        Matcher matcher = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"").matcher(objeto);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Campo obrigatório ausente no armazenamento: " + campo);
        }
        return desescapar(matcher.group(1));
    }

    private int obterInteiro(String objeto, String campo) {
        Matcher matcher = Pattern.compile("\"" + campo + "\"\\s*:\\s*(-?\\d+)").matcher(objeto);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Campo obrigatório ausente no armazenamento: " + campo);
        }
        return Integer.parseInt(matcher.group(1));
    }

    private List<String> obterListaTexto(String objeto, String campo) {
        String array = ArmazenamentoJson.extrairArrayOuVazio(objeto, campo);
        List<String> textos = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"((?:\\\\.|[^\"])*)\"").matcher(array);

        while (matcher.find()) {
            textos.add(desescapar(matcher.group(1)));
        }

        return textos;
    }

    private void adicionarListaTexto(StringBuilder json, List<String> valores) {
        if (valores.isEmpty()) {
            json.append("[]");
            return;
        }

        json.append("[\n");
        for (int i = 0; i < valores.size(); i++) {
            json.append("        \"").append(escapar(valores.get(i))).append("\"");
            if (i < valores.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("      ]");
    }

    private String escapar(String valor) {
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String desescapar(String valor) {
        return valor.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
