package rain.mocking.design.interview.book.brian;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author mao
 * @date 2024/3/9 17:09
 */
public class Renderer {

  private final ExecutorService executor;

  Renderer(ExecutorService executor) {
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
    CompletionService<ImageData> completionService = new ExecutorCompletionService<>(executor);

    for (final ImageInfo info : imageInfos) {
      completionService.submit(info::downloadImage);
    }

    renderText(source);

    try {
      for (int i = 0; i < imageInfos.size(); i++) {
        Future<ImageData> future = completionService.take();
        ImageData imageData = future.get();
        renderImage(imageData);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      throw launderThrowable(e.getCause());
    }
  }
}
