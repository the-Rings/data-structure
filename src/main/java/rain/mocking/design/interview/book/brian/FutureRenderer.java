package rain.mocking.design.interview.book.brian;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author mao
 * @date 2024/3/9 17:06
 */
public class FutureRenderer {

  private final ExecutorService executor;

  FutureRenderer(ExecutorService executor) {
    this.executor = executor;
  }

  static class ImageData {}

  static class ImageInfo {
    public ImageData downloadImage() {
      return new ImageData();
    }
  }

  Throwable launderThrowable(Throwable cause) {
    return cause;
  }

  List<ImageInfo> scanForImageInfo(CharSequence source) {
    return new ArrayList<>();
  }

  void renderText(CharSequence source) {}

  void renderImage(ImageData data) {}

  void renderPage(CharSequence source) throws Throwable {
    List<ImageInfo> imageInfos = scanForImageInfo(source);
    Callable<List<ImageData>> task =
        () -> {
          List<ImageData> result = new ArrayList<>();
          for (ImageInfo info : imageInfos) {
            result.add(info.downloadImage());
          }
          return result;
        };

    Future<List<ImageData>> future = executor.submit(task);
    renderText(source);
    try {
      List<ImageData> imageDataList = future.get();
      for (ImageData data : imageDataList) {
        renderImage(data);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      future.cancel(true);
    } catch (ExecutionException e) {
      throw launderThrowable(e.getCause());
    }
  }
}
