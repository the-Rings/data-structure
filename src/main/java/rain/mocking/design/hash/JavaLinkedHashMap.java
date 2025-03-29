package rain.mocking.design.hash;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class JavaLinkedHashMap {
  public static void main(String[] args) {
    HashMap<Integer, Integer> m = new LinkedHashMap<>(10, 0.75f, true);
    m.put(3, 11);
    m.put(1, 12);
    m.put(5, 23);
    m.put(2, 22);
    m.put(3, 26);
    m.get(5);
    for (Map.Entry e: m.entrySet()) {
      System.out.println(e.getKey());
    }
  }
}
