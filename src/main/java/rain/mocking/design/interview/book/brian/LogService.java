package interview.book.brian;

import java.util.concurrent.BlockingQueue;

/**
 * @author mao
 * @date 2024/3/7 16:58
 */
public class LogService {
  private final BlockingQueue<String> queue;
  private final LoggerThread loggerThread;
  private final PrinterWriter writer;
  private boolean isShutdown;
  private int reservation;

  LogService(BlockingQueue<String> queue, LoggerThread loggerThread, PrinterWriter printerWriter) {
    this.queue = queue;
    this.loggerThread = loggerThread;
    this.writer = printerWriter;
  }

  public void start() {
    loggerThread.start();
  }

  public void stop() {
    synchronized (this) {
      isShutdown = true;
    }
    loggerThread.interrupt();
  }

  public void log(String msg) throws InterruptedException {
    synchronized (this) {
      if (isShutdown) {
        throw new IllegalStateException();
      }
      ++reservation;
    }
    queue.put(msg);
  }

  private class LoggerThread extends Thread {
    @Override
    public void run() {
      try {
        while (true) {
          if (isShutdown && reservation == 0) {
            break;
          }
          String msg = queue.take();
          synchronized (LogService.this) {
            --reservation;
          }
          writer.println(msg);
        }
      } catch (InterruptedException e) {
        // retry
      } finally {
        writer.close();
      }
    }
  }
}

class PrinterWriter {
  public void println(String msg) {}

  public void close() {}
}
