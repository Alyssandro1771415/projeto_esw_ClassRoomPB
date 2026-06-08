package pb.classroom.model;

import java.util.Objects;
import java.util.UUID;

public class Matricula {

    private final String id;
    private final String idAluno;
    private final String idTurma;

    public Matricula(String idAluno, String idTurma) {
        this(UUID.randomUUID().toString(), idAluno, idTurma);
    }

    public Matricula(String id, String idAluno, String idTurma) {
        this.id = validarCampoObrigatorio(id, "id");
        this.idAluno = validarCampoObrigatorio(idAluno, "id do aluno");
        this.idTurma = validarCampoObrigatorio(idTurma, "id da turma");
    }

    public String getId() {
        return id;
    }

    public String getIdAluno() {
        return idAluno;
    }

    public String getIdTurma() {
        return idTurma;
    }

    private static String validarCampoObrigatorio(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " é obrigatório");
        }
        return valor.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Matricula matricula = (Matricula) o;
        return id.equals(matricula.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
