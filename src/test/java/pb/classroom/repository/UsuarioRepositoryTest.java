package pb.classroom.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.Usuario;

@DisplayName("UsuarioRepository - persistencia de usuarios")
class UsuarioRepositoryTest {

  @TempDir Path tempDir;

  @Test
  @DisplayName("cria administrador padrao quando arquivo nao existe")
  void criaAdministradorPadrao() {
    Path arquivo = tempDir.resolve("armazenamento.json");
    UsuarioRepository repository = new UsuarioRepository(arquivo);

    List<Usuario> usuarios = repository.carregarUsuarios();

    assertEquals(1, usuarios.size());
    assertEquals(PerfilUsuario.ADMINISTRADOR, usuarios.get(0).getPerfil());
    assertTrue(Files.exists(arquivo));
  }

  @Test
  @DisplayName("salva e recarrega usuarios cadastrados")
  void salvaERecarregaUsuarios() throws Exception {
    Path arquivo = tempDir.resolve("armazenamento.json");
    UsuarioRepository repository = new UsuarioRepository(arquivo);
    Usuario professor =
        new Usuario(PerfilUsuario.PROFESSOR, "Prof", "P001", "prof@uepb.edu.br", "segredo");

    repository.salvarUsuarios(List.of(professor));

    List<Usuario> carregados = repository.carregarUsuarios();
    assertEquals(1, carregados.size());
    assertEquals("P001", carregados.get(0).getMatricula());
    assertEquals("prof@uepb.edu.br", carregados.get(0).getEmail());
  }

  @Test
  @DisplayName("preserva outras colecoes ao salvar usuarios")
  void preservaOutrasColecoes() throws Exception {
    Path arquivo = tempDir.resolve("armazenamento.json");
    Files.writeString(
        arquivo,
        "{\"disciplinas\":[{\"id\":\"d1\"}],\"cursos\":[],\"periodosLetivos\":[],\"turmas\":[]}",
        StandardCharsets.UTF_8);
    UsuarioRepository repository = new UsuarioRepository(arquivo);
    repository.salvarUsuarios(
        List.of(new Usuario(PerfilUsuario.ALUNO, "Aluno", "A001", "aluno@uepb.edu.br", "123")));

    String conteudo = Files.readString(arquivo, StandardCharsets.UTF_8);
    assertTrue(conteudo.contains("\"disciplinas\""));
    assertTrue(conteudo.contains("\"usuarios\""));
  }
}
