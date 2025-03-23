package hash;

public class JavaHashMap {
  static int hash(Object key) {
    int h;
    int hv = (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    System.out.print(key + "----" + hv + "-----");
    return hv;
  }

  static void tabIndex(Object key) {
    int i = 2 & hash(key);
    System.out.println(i);
  }

  public static void main(String[] args) {
    JavaHashMap javaHashMap = new JavaHashMap();
    tabIndex("a");
    tabIndex("b");
    tabIndex(3);
  }
}
