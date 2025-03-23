package interview.thread_base;

import java.util.concurrent.*;

public class ThreadDeadLock {
  static final ExecutorService exec = Executors.newSingleThreadExecutor();

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    Future<String> page = exec.submit(new RenderPageTask());
    System.out.println(page.get());
  }

  static class RenderPageTask implements Callable<String> {
    @Override
    public String call() throws Exception {
      Future<String> header, footer;
      header = exec.submit(new LoadFileTask("header.html"));
      // footer = exec.submit(new LoadFileTask("footer.html"));

      String page = "body";

      return header.get() + page;
    }
  }

  static class LoadFileTask implements Callable<String> {
    private final String file;

    LoadFileTask(String file) {
      this.file = file;
    }

    @Override
    public String call() {
      return file;
    }
  }
}
