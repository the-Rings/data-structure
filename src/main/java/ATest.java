import java.util.Random;

public class ATest {
  public static void main(String[] args) {
    Random random = new Random(56);
    int i = 1000;
    while (i > 0) {
      System.out.print(" " + random.nextInt(20000));
      --i;
    }
  }
}
