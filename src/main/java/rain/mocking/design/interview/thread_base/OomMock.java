package rain.mocking.design.interview.thread_base;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mao
 * @date 2024/3/10 12:20
 */
public class OomMock {
    public static void main(String[] args){
        List<Object> a = new ArrayList<>();
        while (true) {
            a.add(new Object());
        }
    }
}
