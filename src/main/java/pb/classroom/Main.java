package pb.classroom;

import pb.classroom.view.ClassRoomCLI;

/**
 * Ponto de entrada da aplicação (Maven: {@code src/main/java}). A CLI será acoplada em {@link
 * pb.classroom.view} e os fluxos em {@link pb.classroom.controller}.
 */
public final class Main {

  private Main() {}

  public static void main(String[] args) {
    new ClassRoomCLI().iniciar();
  }
}
