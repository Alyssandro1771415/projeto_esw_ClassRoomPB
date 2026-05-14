package pb.classroom.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Curso cadastrado pelo administrador (RF05).
 */
public class Curso {

    private final String id;
    private String nome;
    /** Código opcional do curso (identificação curta). */
    private String codigo;

    public Curso(String nome) {
        this(UUID.randomUUID().toString(), nome, null);
    }

    public Curso(String id, String nome, String codigo) {
        this.id = Objects.requireNonNull(id, "id").trim();
        if (this.id.isEmpty()) {
            throw new IllegalArgumentException("id não pode ser vazio");
        }
        setNome(nome);
        setCodigo(codigo);
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("nome do curso é obrigatório");
        }
        this.nome = nome.trim();
    }

    /** Pode ser {@code null} ou vazio se o curso não tiver código. */
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            this.codigo = null;
        } else {
            this.codigo = codigo.trim();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Curso curso = (Curso) o;
        return id.equals(curso.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
