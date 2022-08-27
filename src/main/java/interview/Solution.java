package interview;

import java.util.*;
public class Solution {
  /**
   * 有m个key，每个key有m个值，如何列出所有的k-v组合？
   * 例如：
   * {
   *    a: [1, 2, 3],
   *    b: [4, 5, 6],
   *    c: [7, 8, 9],
   *    d: [!, @, #],
   * }
   * 第一步：先获得参数名的子集
   * 第二步：根据某个参数名的组合（例如：{b, c}），从b对应的[4, 5, 6]中任选出一个值，从c对应的[7, 8, 9]中任选出一个值，
   * 拼出3*3=9中不同的组合输出
   */
  Map<String, List<String>> valuesMap = new HashMap<>();
  private List<List<String>> paramsSubset(List<String> keys) {
    List<List<String>> result = new ArrayList<>();
    backtrack_p(keys, 0, new ArrayList<>(), result);
    return result;
  }

  private void backtrack_p(List<String> keys, int k, List<String> path, List<List<String>> result) {
    if (k == keys.size()) {
      result.add(new ArrayList<>(path));
      return;
    }
    backtrack_p(keys, k+1, path, result);
    path.add(keys.get(k));
    backtrack_p(keys, k+1, path, result);
    path.remove(path.size()-1);
  }

  private List<Map<String, String>> assign(List<String> params) {
    List<Map<String, String>> result = new ArrayList<>();
    backtrack_v(params, 0, new ArrayList<>(), result);
    return result;
  }

  private void backtrack_v(List<String> params, int k, List<String> path, List<Map<String, String>> result) {
    if (k == params.size()) {
      Map<String, String> hashMap = new HashMap<>();
      for (int i = 0; i < path.size(); i++) {
        hashMap.put(params.get(i), path.get(i));
      }
      result.add(hashMap);
      return;
    }
    int n = valuesMap.get(params.get(k)).size();

    for (int i = 0; i < n; i++) {
      path.add(valuesMap.get(params.get(k)).get(i));
      backtrack_v(params, k+1, path, result);
      path.remove(path.size()-1);
    }
  }

  public List<Map<String, String>> combine(List<String> keys) {
    List<List<String>> paramResult = paramsSubset(keys);
    List<Map<String, String>> finalResult = new ArrayList<>();
    for (int i = 0; i < paramResult.size(); i++) {
      finalResult.addAll(assign(paramResult.get(i)));
    }
    return finalResult;
  }

  public static void main(String[] args) {
    Solution solution = new Solution();
    solution.valuesMap.put("a", Arrays.asList("1", "2", "3", "4", "5"));
    solution.valuesMap.put("b", Arrays.asList("4", "5", "6", "*"));
    solution.valuesMap.put("c", Arrays.asList("4", "5", "6", "*"));
    solution.valuesMap.put("d", Arrays.asList("!", "@", "#"));
    List<Map<String, String>> r = solution.combine(new ArrayList<>(solution.valuesMap.keySet()));
    System.out.println(r.toString());
    System.out.println(r.size());
  }
}
