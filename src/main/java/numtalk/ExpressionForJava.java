package numtalk;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.IntStream;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * @author mao
 * @date 2023/7/20
 */
public class ExpressionForJava {
  public BigDecimal exprEval(String expr, Map<String, Double> variables) {
    /**
     * <dependency> <groupId>net.objecthunter</groupId> <artifactId>exp4j</artifactId>
     * <version>0.4.8</version> </dependency>
     */
    Expression e = new ExpressionBuilder(expr).variables(variables.keySet()).build();
    for (String key : variables.keySet()) {
      e.setVariable(key, variables.get(key));
    }
    double res = e.evaluate();
    return BigDecimal.valueOf(res);
  }

  public static void test1() {
    String expr = "2*1st_Active_CGB_1Y-y/2+z";
    String[] variableArr = new String[] {"1st_Active_CGB_1Y", "y", "z"};
    Expression e =
        new ExpressionBuilder(expr)
            .variables(variableArr)
            .build()
            .setVariable("1st_Active_CGB_1Y", 2.33)
            .setVariable("y", 2.33)
            .setVariable("z", 4.002);
    System.out.println(e.evaluate());
  }

  public static void main(String[] args) {
    String[] letters =
        IntStream.rangeClosed('A', 'Z')
            .mapToObj(ch -> String.valueOf((char) ch))
            .toArray(String[]::new);
    String expr = "2*1st_Active_CGB_1Y-2nd_Active_CDB_10Y/2+FR007_3M_6M";
    String[] variableArr = new String[] {"1st_Active_CGB_1Y", "2nd_Active_CDB_10Y", "FR007_3M_6M"};
    test1();
  }
}
