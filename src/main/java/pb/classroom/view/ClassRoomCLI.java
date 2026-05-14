package pb.classroom.view;

import java.util.Scanner;
import pb.classroom.controller.AutenticacaoController;
import pb.classroom.model.PerfilUsuario;
import pb.classroom.model.Usuario;
import pb.classroom.repository.UsuarioRepository;

public class ClassRoomCLI {

    private final Scanner scanner;
    private final AutenticacaoController autenticacaoController;
    private final UsuarioRepository usuarioRepository;

    public ClassRoomCLI() {
        this(new Scanner(System.in), new UsuarioRepository());
    }

    ClassRoomCLI(Scanner scanner, UsuarioRepository usuarioRepository) {
        this.scanner = scanner;
        this.usuarioRepository = usuarioRepository;
        this.autenticacaoController = new AutenticacaoController(usuarioRepository.carregarUsuarios());
    }

    public void iniciar() {
        boolean executando = true;
        while (executando) {
            exibirMenu();
            String opcao = lerLinha("Opção: ");

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
                case "0":
                    executando = false;
                    System.out.println("Sistema encerrado.");
                    break;
                default:
                    System.out.println("Opção inválida.");
                    break;
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
        } else {
            System.out.println("Sessão atual: nenhum usuário logado");
            System.out.println("1 - Login");
            System.out.println("2 - Ver dados do usuário logado");
            System.out.println("3 - Logout");
            System.out.println("4 - Cadastrar usuário");
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
}
