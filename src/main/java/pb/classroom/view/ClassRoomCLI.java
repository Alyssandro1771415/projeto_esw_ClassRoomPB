package pb.classroom.view;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import pb.classroom.controller.AutenticacaoController;
import pb.classroom.model.Administrador;
import pb.classroom.model.Aluno;
import pb.classroom.model.Coordenador;
import pb.classroom.model.Professor;
import pb.classroom.model.Usuario;

/**
 * Interface de comandos simplificada do ClassRoomPB.
 */
public class ClassRoomCLI {

    private final Scanner scanner;
    private final AutenticacaoController autenticacaoController;

    public ClassRoomCLI() {
        this(new Scanner(System.in), new AutenticacaoController(criarUsuariosDeDemonstracao()));
    }

    ClassRoomCLI(Scanner scanner, AutenticacaoController autenticacaoController) {
        this.scanner = scanner;
        this.autenticacaoController = autenticacaoController;
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
        } else {
            System.out.println("Sessão atual: nenhum usuário logado");
            System.out.println("1 - Login");
            System.out.println("2 - Ver dados do usuário logado");
            System.out.println("3 - Logout");
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

    private String lerLinha(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    private static List<Usuario> criarUsuariosDeDemonstracao() {
        return Arrays.asList(
                new Aluno("2026001", "aluno@pb.edu.br", "123456"),
                new Professor("2026002", "professor@pb.edu.br", "123456"),
                new Coordenador("2026003", "coordenador@pb.edu.br", "123456"),
                new Administrador("2026004", "admin@pb.edu.br", "123456"));
    }
}
