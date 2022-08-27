package interview;

import java.util.*;
public class Solution {
  /**
   * ��m��key��ÿ��key��m��ֵ������г����е�k-v��ϣ�
   * ���磺
   * {
   *    a: [1, 2, 3],
   *    b: [4, 5, 6],
   *    c: [7, 8, 9],
   *    d: [!, @, #],
   * }
   * ��һ�����Ȼ�ò��������Ӽ�
   * �ڶ���������ĳ������������ϣ����磺{b, c}������b��Ӧ��[4, 5, 6]����ѡ��һ��ֵ����c��Ӧ��[7, 8, 9]����ѡ��һ��ֵ��
   * ƴ��3*3=9�в�ͬ��������
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
