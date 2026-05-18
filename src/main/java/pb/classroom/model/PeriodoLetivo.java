package pb.classroom.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Periodo letivo cadastrado pelo coordenador (RF08, RF09).
 */
public class PeriodoLetivo {

    private final String id;
    private String codigo;
    private boolean ativo;

    public PeriodoLetivo(String codigo) {
        this(UUID.randomUUID().toString(), codigo, false);
    }

    public PeriodoLetivo(String id, String codigo, boolean ativo) {
        this.id = Objects.requireNonNull(id, "id").trim();
        if (this.id.isEmpty()) {
            throw new IllegalArgumentException("id não pode ser vazio");
        }
        setCodigo(codigo);
        this.ativo = ativo;
    }

    public String getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("codigo do período letivo é obrigatório");
        }
        if (!codigo.trim().matches("\\d{4}\\.\\d")) {
            throw new IllegalArgumentException("período letivo deve usar o formato 2026.2");
        }
        this.codigo = codigo.trim();
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void ativar() {
        ativo = true;
    }

    public void encerrar() {
        ativo = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PeriodoLetivo that = (PeriodoLetivo) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
