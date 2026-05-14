package pb.classroom.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Atribuicao de perfis aos tipos de usuario")
class UsuarioAtribuicaoTest {

    // Cada classe concreta deve expor o perfil usado nas regras de acesso.
    @Test
    @DisplayName("Aluno recebe perfil ALUNO")
    void alunoRecebePerfilAluno() {
        Aluno aluno = new Aluno("2024001", "aluno@classroompb.com", "senha");

        assertEquals(PerfilUsuario.ALUNO, aluno.getPerfil());
    }

    @Test
    @DisplayName("Professor recebe perfil PROFESSOR")
    void professorRecebePerfilProfessor() {
        Professor professor = new Professor("P001", "professor@classroompb.com", "senha");

        assertEquals(PerfilUsuario.PROFESSOR, professor.getPerfil());
    }

    @Test
    @DisplayName("Coordenador recebe perfil COORDENADOR")
    void coordenadorRecebePerfilCoordenador() {
        Coordenador coordenador = new Coordenador("C001", "coordenador@classroompb.com", "senha");

        assertEquals(PerfilUsuario.COORDENADOR, coordenador.getPerfil());
    }

    @Test
    @DisplayName("Administrador recebe perfil ADMINISTRADOR")
    void administradorRecebePerfilAdministrador() {
        Administrador administrador = new Administrador("0001", "admin@classroompb.com", "admin123");

        assertEquals(PerfilUsuario.ADMINISTRADOR, administrador.getPerfil());
    }

    @Test
    @DisplayName("Cada tipo concreto mantem o perfil esperado")
    void cadaTipoConcretoMantemPerfilEsperado() {
        assertAll(
                () -> assertInstanceOf(Aluno.class, new Aluno("A001", "a@classroompb.com", "senha")),
                () -> assertInstanceOf(Professor.class, new Professor("P001", "p@classroompb.com", "senha")),
                () -> assertInstanceOf(Coordenador.class, new Coordenador("C001", "c@classroompb.com", "senha")),
                () -> assertInstanceOf(Administrador.class,
                        new Administrador("ADM001", "adm@classroompb.com", "senha")));
    }
}
