package pb.classroom.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ArmazenamentoJson - utilitarios de JSON")
class ArmazenamentoJsonTest {

    @Test
    @DisplayName("extrai array existente ou retorna vazio")
    void extraiArrayExistenteOuVazio() {
        String doc = "{\"usuarios\":[{\"id\":\"1\"}],\"cursos\":[]}";
        assertEquals("[{\"id\":\"1\"}]", ArmazenamentoJson.extrairArrayOuVazio(doc, "usuarios"));
        assertEquals("[]", ArmazenamentoJson.extrairArrayOuVazio(doc, "turmas"));
        assertEquals("[]", ArmazenamentoJson.extrairArrayOuVazio(null, "usuarios"));
    }

    @Test
    @DisplayName("monta documento completo com arrays normalizados")
    void montaDocumentoCompleto() {
        String doc = ArmazenamentoJson.montarDocumento(
                "[{\"id\":\"u1\"}]",
                "[{\"id\":\"d1\"}]",
                "[{\"id\":\"c1\"}]",
                "[{\"id\":\"p1\"}]",
                "[{\"id\":\"t1\"}]");
        assertTrue(doc.contains("\"usuarios\""));
        assertTrue(doc.contains("\"turmas\""));
    }

    @Test
    @DisplayName("rejeita campo malformado no armazenamento")
    void rejeitaCampoMalformado() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ArmazenamentoJson.extrairArrayOuVazio("{\"usuarios\": }", "usuarios"));
    }
}
