package rain.mocking.design.sort;

public class Recursion {
  static int f(int n) {
    if(n == 1){
      return 1;
    }
    if(n == 2) {
      return 2;
    }
    return f(n - 1) + f(n - 2);

  };
  public static void main(String[] args) {
    System.out.println(f(4));
  }
}
