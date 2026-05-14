package pb.classroom.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Disciplina ofertada no âmbito de um curso (RF06, RF07).
 * Pré-requisitos são opcionais (RN04) e referenciam outras disciplinas por id.
 */
public class Disciplina {

    private final String id;
    private String codigo;
    private String nome;
    private int cargaHoraria;
    private int creditos;
    private String idCurso;
    private final List<String> preRequisitosIds;

    public Disciplina(String codigo, String nome, int cargaHoraria, int creditos, String idCurso) {
        this(UUID.randomUUID().toString(), codigo, nome, cargaHoraria, creditos, idCurso, List.of());
    }

    public Disciplina(
            String id,
            String codigo,
            String nome,
            int cargaHoraria,
            int creditos,
            String idCurso,
            List<String> preRequisitosIds) {
        this.id = Objects.requireNonNull(id, "id").trim();
        if (this.id.isEmpty()) {
            throw new IllegalArgumentException("id não pode ser vazio");
        }
        setCodigo(codigo);
        setNome(nome);
        setCargaHoraria(cargaHoraria);
        setCreditos(creditos);
        setIdCurso(idCurso);
        this.preRequisitosIds = new ArrayList<>(validarListaPreRequisitos(this.id, preRequisitosIds));
    }

    public String getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("código da disciplina é obrigatório");
        }
        this.codigo = codigo.trim();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("nome da disciplina é obrigatório");
        }
        this.nome = nome.trim();
    }

    public int getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(int cargaHoraria) {
        if (cargaHoraria <= 0) {
            throw new IllegalArgumentException("carga horária deve ser positiva");
        }
        this.cargaHoraria = cargaHoraria;
    }

    public int getCreditos() {
        return creditos;
    }

    public void setCreditos(int creditos) {
        if (creditos <= 0) {
            throw new IllegalArgumentException("créditos devem ser positivos");
        }
        this.creditos = creditos;
    }

    public String getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(String idCurso) {
        if (idCurso == null || idCurso.trim().isEmpty()) {
            throw new IllegalArgumentException("id do curso é obrigatório");
        }
        this.idCurso = idCurso.trim();
    }

    /** Lista mutável interna; não expõe referência direta. */
    public List<String> getPreRequisitosIds() {
        return Collections.unmodifiableList(preRequisitosIds);
    }

    public void setPreRequisitosIds(List<String> ids) {
        preRequisitosIds.clear();
        preRequisitosIds.addAll(validarListaPreRequisitos(getId(), ids));
    }

    private static List<String> validarListaPreRequisitos(String idDisciplina, List<String> ids) {
        if (ids == null) {
            return List.of();
        }
        List<String> limpos = new ArrayList<>();
        Set<String> vistos = new HashSet<>();
        for (String rid : ids) {
            if (rid == null || rid.trim().isEmpty()) {
                throw new IllegalArgumentException("id de pré-requisito inválido");
            }
            String t = rid.trim();
            if (t.equals(idDisciplina)) {
                throw new IllegalArgumentException("disciplina não pode ser pré-requisito dela mesma");
            }
            if (!vistos.add(t)) {
                throw new IllegalArgumentException("pré-requisito duplicado: " + t);
            }
            limpos.add(t);
        }
        return limpos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Disciplina that = (Disciplina) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
