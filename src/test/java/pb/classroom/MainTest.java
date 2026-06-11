package pb.classroom;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Main - ponto de entrada")
class MainTest {

  @Test
  @DisplayName("main encerra quando usuario escolhe sair")
  void mainEncerraComOpcaoSair() {
    var entrada = new ByteArrayInputStream("0\n".getBytes(StandardCharsets.UTF_8));
    var entradaAnterior = System.in;
    System.setIn(entrada);
    try {
      assertDoesNotThrow(() -> Main.main(new String[0]));
    } finally {
      System.setIn(entradaAnterior);
    }
  }

  @Test
  @DisplayName("construtor privado impede instanciacao direta")
  void construtorPrivado() throws Exception {
    var construtor = Main.class.getDeclaredConstructor();
    construtor.setAccessible(true);
    assertDoesNotThrow(() -> construtor.newInstance());
  }
}
