package pb.classroom.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import pb.classroom.controller.AutenticacaoController;
import pb.classroom.controller.DisciplinaController;
import pb.classroom.model.Disciplina;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.Usuario;
import pb.classroom.repository.DisciplinaRepository;
import pb.classroom.repository.UsuarioRepository;

public class ClassRoomCLI {

    private final Scanner scanner;
    private final AutenticacaoController autenticacaoController;
    private final DisciplinaController disciplinaController;
    private final UsuarioRepository usuarioRepository;
    private final DisciplinaRepository disciplinaRepository;

    public ClassRoomCLI() {
        this(new Scanner(System.in), new UsuarioRepository(), new DisciplinaRepository());
    }

    ClassRoomCLI(Scanner scanner, UsuarioRepository usuarioRepository) {
        this(scanner, usuarioRepository, new DisciplinaRepository());
    }

    ClassRoomCLI(Scanner scanner, UsuarioRepository usuarioRepository, DisciplinaRepository disciplinaRepository) {
        this.scanner = scanner;
        this.usuarioRepository = usuarioRepository;
        this.disciplinaRepository = disciplinaRepository;
        this.autenticacaoController = new AutenticacaoController(usuarioRepository.carregarUsuarios());
        this.disciplinaController = new DisciplinaController(
                autenticacaoController,
                disciplinaRepository.carregarDisciplinas());
    }

    public void iniciar() {
        boolean executando = true;
        limparTerminal();
        while (executando) {
            exibirMenu();
            String opcao = lerLinha("Opção: ");
            limparTerminal();

            switch (opcao) {
                case "1":
                    realizarLogin();
                    break;
                case "2":
                    exibirUsuarioLogado();
                    break;
                case "3":
                    realizarLogout();
                    break;
                case "4":
                    cadastrarUsuario();
                    break;
                case "5":
                    cadastrarDisciplina();
                    break;
                case "6":
                    listarDisciplinas();
                    break;
                case "0":
                    executando = false;
                    System.out.println("Sistema encerrado.");
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
            }

            if (executando) {
                System.out.println();
                lerLinha("Pressione Enter para continuar...");
                limparTerminal();
            }
        }
    }

    private void exibirMenu() {
        System.out.println();
        System.out.println("===== ClassRoomPB =====");
        if (autenticacaoController.isAutenticado()) {
            Usuario usuario = autenticacaoController.getUsuarioLogado();
            System.out.println("Sessão atual: " + usuario.getEmail() + " (" + usuario.getPerfil() + ")");
            System.out.println("1 - Trocar login");
            System.out.println("2 - Ver dados do usuário logado");
            System.out.println("3 - Logout");
            System.out.println("4 - Cadastrar usuário");
            System.out.println("5 - Cadastrar disciplina");
            System.out.println("6 - Listar disciplinas");
        } else {
            System.out.println("Sessão atual: nenhum usuário logado");
            System.out.println("1 - Login");
            System.out.println("2 - Ver dados do usuário logado");
            System.out.println("3 - Logout");
            System.out.println("4 - Cadastrar usuário");
            System.out.println("5 - Cadastrar disciplina");
            System.out.println("6 - Listar disciplinas");
        }
        System.out.println("0 - Sair");
        System.out.println();
    }

    private void realizarLogin() {
        String identificador = lerLinha("Matrícula/e-mail: ");
        String senha = lerLinha("Senha: ");

        try {
            Usuario usuario = autenticacaoController.login(identificador, senha);
            System.out.println("Login realizado com sucesso.");
            System.out.println("Perfil: " + usuario.getPerfil());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void exibirUsuarioLogado() {
        if (!autenticacaoController.isAutenticado()) {
            System.out.println("Nenhum usuário logado. Use a opção 1 para fazer login primeiro.");
            return;
        }

        Usuario usuario = autenticacaoController.getUsuarioLogado();
        System.out.println("Usuário logado:");
        System.out.println("Matrícula: " + usuario.getMatricula());
        System.out.println("E-mail: " + usuario.getEmail());
        System.out.println("Perfil: " + usuario.getPerfil());
    }

    private void realizarLogout() {
        if (!autenticacaoController.isAutenticado()) {
            System.out.println("Nenhum usuário logado. Use a opção 1 para fazer login primeiro.");
            return;
        }

        autenticacaoController.logout();
        System.out.println("Logout realizado com sucesso.");
    }

    private void cadastrarUsuario() {
        if (!autenticacaoController.isAutenticado()
                || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.ADMINISTRADOR) {
            System.out.println("Apenas administradores podem cadastrar usuários.");
            return;
        }

        PerfilUsuario perfil = lerPerfil();
        if (perfil == null) {
            System.out.println("Tipo de usuário inválido.");
            return;
        }

        String matricula = lerLinha("Matrícula: ");
        String email = lerLinha("E-mail: ");
        String senha = lerLinha("Senha: ");

        try {
            Usuario usuario = autenticacaoController.cadastrarUsuario(perfil, matricula, email, senha);
            usuarioRepository.salvarUsuarios(autenticacaoController.getUsuarios());
            System.out.println("Usuário cadastrado com sucesso.");
            System.out.println("Perfil: " + usuario.getPerfil());
            System.out.println("Matrícula: " + usuario.getMatricula());
            System.out.println("E-mail: " + usuario.getEmail());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    private void cadastrarDisciplina() {
        if (!autenticacaoController.isAutenticado()
                || autenticacaoController.getUsuarioLogado().getPerfil() != PerfilUsuario.COORDENADOR) {
            System.out.println("Apenas coordenadores podem cadastrar disciplinas.");
            return;
        }

        exibirDisciplinasParaPreRequisito();
        String codigo = lerLinha("Código da disciplina: ");
        String nome = lerLinha("Nome: ");
        String cargaHorariaTexto = lerLinha("Carga horária: ");
        String creditosTexto = lerLinha("Créditos: ");
        String idCurso = lerLinha("ID do curso: ");
        String preRequisitosTexto = lerLinha("IDs dos pré-requisitos separados por vírgula (opcional): ");

        try {
            int cargaHoraria = Integer.parseInt(cargaHorariaTexto);
            int creditos = Integer.parseInt(creditosTexto);
            List<String> preRequisitosIds = converterTextoParaLista(preRequisitosTexto);

            Disciplina disciplina = disciplinaController.cadastrarDisciplina(
                    codigo,
                    nome,
                    cargaHoraria,
                    creditos,
                    idCurso,
                    preRequisitosIds);
            disciplinaRepository.salvarDisciplinas(disciplinaController.getDisciplinas());

            System.out.println("Disciplina cadastrada com sucesso.");
            System.out.println("ID: " + disciplina.getId());
            System.out.println("Código: " + disciplina.getCodigo());
            System.out.println("Nome: " + disciplina.getNome());
            System.out.println("Carga horária: " + disciplina.getCargaHoraria());
            System.out.println("Créditos: " + disciplina.getCreditos());
        } catch (NumberFormatException e) {
            System.out.println("Carga horária e créditos devem ser números inteiros.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }

    private void listarDisciplinas() {
        List<Disciplina> disciplinas = disciplinaController.getDisciplinas();
        if (disciplinas.isEmpty()) {
            System.out.println("Nenhuma disciplina cadastrada.");
            return;
        }

        System.out.println("Disciplinas cadastradas:");
        for (Disciplina disciplina : disciplinas) {
            System.out.println();
            System.out.println("ID: " + disciplina.getId());
            System.out.println("Código: " + disciplina.getCodigo());
            System.out.println("Nome: " + disciplina.getNome());
            System.out.println("Carga horária: " + disciplina.getCargaHoraria());
            System.out.println("Créditos: " + disciplina.getCreditos());
            System.out.println("ID do curso: " + disciplina.getIdCurso());
            System.out.println("Pré-requisitos: " + formatarPreRequisitos(disciplina.getPreRequisitosIds()));
        }
    }

    private void exibirDisciplinasParaPreRequisito() {
        List<Disciplina> disciplinas = disciplinaController.getDisciplinas();
        if (disciplinas.isEmpty()) {
            System.out.println("Nenhuma disciplina cadastrada para usar como pré-requisito.");
            System.out.println();
            return;
        }

        System.out.println("Disciplinas disponíveis para pré-requisito:");
        for (Disciplina disciplina : disciplinas) {
            System.out.println(disciplina.getId() + " - " + disciplina.getCodigo() + " - " + disciplina.getNome());
        }
        System.out.println();
    }

    private List<String> converterTextoParaLista(String texto) {
        List<String> valores = new ArrayList<>();
        if (texto == null || texto.trim().isEmpty()) {
            return valores;
        }

        String[] partes = texto.split(",");
        for (String parte : partes) {
            String valor = parte.trim();
            if (!valor.isEmpty()) {
                valores.add(valor);
            }
        }
        return valores;
    }

    private String formatarPreRequisitos(List<String> preRequisitosIds) {
        if (preRequisitosIds.isEmpty()) {
            return "nenhum";
        }
        return String.join(", ", preRequisitosIds);
    }

    private PerfilUsuario lerPerfil() {
        System.out.println("Tipo de usuário:");
        System.out.println("1 - Aluno");
        System.out.println("2 - Professor");
        System.out.println("3 - Coordenador");
        System.out.println("4 - Administrador");

        String opcao = lerLinha("Opção: ");
        switch (opcao) {
            case "1":
                return PerfilUsuario.ALUNO;
            case "2":
                return PerfilUsuario.PROFESSOR;
            case "3":
                return PerfilUsuario.COORDENADOR;
            case "4":
                return PerfilUsuario.ADMINISTRADOR;
            default:
                return null;
        }
    }

    private String lerLinha(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    private void limparTerminal() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
