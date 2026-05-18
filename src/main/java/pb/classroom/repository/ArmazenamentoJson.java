package pb.classroom.repository;

final class ArmazenamentoJson {

    private ArmazenamentoJson() {
    }

    static String extrairArrayOuVazio(String conteudo, String campo) {
        if (conteudo == null || conteudo.trim().isEmpty()) {
            return "[]";
        }

        String chave = "\"" + campo + "\"";
        int indiceChave = conteudo.indexOf(chave);
        if (indiceChave < 0) {
            return "[]";
        }

        int indiceDoisPontos = conteudo.indexOf(':', indiceChave + chave.length());
        if (indiceDoisPontos < 0) {
            throw new IllegalArgumentException("Campo inválido no armazenamento: " + campo);
        }

        int inicioArray = conteudo.indexOf('[', indiceDoisPontos + 1);
        if (inicioArray < 0) {
            throw new IllegalArgumentException("Array ausente no armazenamento: " + campo);
        }

        return extrairArrayAPartirDe(conteudo, inicioArray, campo);
    }

    static String montarDocumento(String usuariosJson, String disciplinasJson) {
        return montarDocumento(usuariosJson, disciplinasJson, "[]", "[]");
    }

    static String montarDocumento(
            String usuariosJson,
            String disciplinasJson,
            String cursosJson,
            String periodosLetivosJson) {
        return "{\n"
                + "  \"usuarios\": " + normalizarArray(usuariosJson) + ",\n"
                + "  \"disciplinas\": " + normalizarArray(disciplinasJson) + ",\n"
                + "  \"cursos\": " + normalizarArray(cursosJson) + ",\n"
                + "  \"periodosLetivos\": " + normalizarArray(periodosLetivosJson) + "\n"
                + "}\n";
    }

    private static String extrairArrayAPartirDe(String conteudo, int inicioArray, String campo) {
        int profundidade = 0;
        boolean dentroDeTexto = false;
        boolean escapando = false;

        for (int i = inicioArray; i < conteudo.length(); i++) {
            char atual = conteudo.charAt(i);

            if (dentroDeTexto) {
                if (escapando) {
                    escapando = false;
                } else if (atual == '\\') {
                    escapando = true;
                } else if (atual == '"') {
                    dentroDeTexto = false;
                }
                continue;
            }

            if (atual == '"') {
                dentroDeTexto = true;
            } else if (atual == '[') {
                profundidade++;
            } else if (atual == ']') {
                profundidade--;
                if (profundidade == 0) {
                    return conteudo.substring(inicioArray, i + 1);
                }
            }
        }

        throw new IllegalArgumentException("Array não finalizado no armazenamento: " + campo);
    }

    private static String normalizarArray(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "[]";
        }
        String limpo = json.trim();
        if (!limpo.startsWith("[") || !limpo.endsWith("]")) {
            throw new IllegalArgumentException("Conteúdo de armazenamento deve ser um array JSON.");
        }
        return limpo;
    }
}
