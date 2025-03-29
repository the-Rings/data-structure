package rain.mocking.design.interview.book.brian;

import java.util.List;
import java.util.concurrent.*;

/**
 * @author mao
 * @date 2024/3/9 18:45
 */
public class BlackListChecking {
  private final ExecutorService executor;
  private final CompletionService<Boolean> completionService;
  private final List<String> blackListName;

  BlackListChecking(ExecutorService executor, List<String> blackListName) {
    this.executor = executor;
    this.completionService = new ExecutorCompletionService<>(executor);
    this.blackListName = blackListName;
  }

  boolean check() throws Throwable {
    for (String name : blackListName) {
      completionService.submit(
          new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
              return doCheck(name);
            }
          });
    }

    int tasks = blackListName.size();
    try {
      while (tasks > 0) {
        Future<Boolean> future = completionService.take();
        boolean result = future.get();
        if (result) {
          return true;
        }
        tasks--;
      }

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      throw e.getCause();
    }
    return false;
  }

  // 模拟黑名单检查的业务方法
  boolean doCheck(String name) {
    return false;
  }
}
