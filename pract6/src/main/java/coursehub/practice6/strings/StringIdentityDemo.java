package coursehub.practice6.strings;

// == сравнивает ссылки, equals — содержимое
public class StringIdentityDemo {
  public static void main(String[] args) {
    // литералы: обе ссылки указывают на один объект из string pool
    String a = "JAVA-101";
    String b = "JAVA-101";
    System.out.println("literal == literal: " + (a == b));
    System.out.println("literal.equals(literal): " + a.equals(b));

    // new String всегда создаёт новый объект вне pool
    String c = new String("JAVA-101");
    System.out.println("literal == new String: " + (a == c));
    System.out.println("literal.equals(new String): " + a.equals(c));
  }
}
