package rain.mocking.design.stack;

/** 浏览器前进后退功能 */
public class Browser {
  private final ArrayStack<String> X = new ArrayStack<>(3);
  private final ArrayStack<String> Y = new ArrayStack<>(3);

  public void forward() {
    X.push(Y.pop());
    System.out.println(X.toString() + "----->" + Y.toString());
  }

  public void back() {
    Y.push(X.pop());
    System.out.println(X.toString() + "------>" + Y.toString());
  }

  /**
   * 模拟浏览
   *
   * @param item
   */
  public void look(String item) {
    X.push(item);
  }

  public static void main(String[] args) {
    Browser b = new Browser();
    b.look("a");
    b.look("b");
    b.look("c");

    b.back();
    b.back();
    b.forward();
    b.forward();
  }
}
