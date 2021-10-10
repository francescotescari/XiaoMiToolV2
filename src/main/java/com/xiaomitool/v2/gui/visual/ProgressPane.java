package com.xiaomitool.v2.gui.visual;

import static com.xiaomitool.v2.tasks.AdvancedUpdateListener.UNITS;
import static com.xiaomitool.v2.utility.utils.StrUtils.DECIMAL_FORMAT;

import com.xiaomitool.v2.gui.WindowManager;
import com.xiaomitool.v2.language.LRes;
import com.xiaomitool.v2.tasks.AdvancedUpdateListener;
import com.xiaomitool.v2.tasks.UpdateListener;
import java.time.temporal.ChronoUnit;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class ProgressPane extends VBox {
  protected StackPane content, overBar;
  protected ProgressBar progress;

  public ProgressPane() {
    build();
  }

  private void build() {
    content = new StackPane();
    progress = new ProgressBar(0);
    overBar = new StackPane();
    VBox barvbox = new VBox(overBar, progress);
    barvbox.setSpacing(10);
    barvbox.setAlignment(Pos.CENTER);
    StackPane barzone = new StackPane(barvbox);
    super.getChildren().addAll(content, barzone);
    super.setAlignment(Pos.CENTER);
    super.setSpacing(10);
  }

  public void setContent(Node node) {
    if (!Platform.isFxApplicationThread()) {
      Platform.runLater(() -> setContent(node));
      return;
    }
    content.getChildren().clear();
    content.getChildren().add(node);
  }

  public void setProgress(Double value) {
    if (!Platform.isFxApplicationThread()) {
      Platform.runLater(() -> setProgress(value));
      return;
    }
    if (value >= 0 && value <= 1) {
      progress.setProgress(value);
    } else {
      progress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
    }
  }

  public void setOverBar(Node node) {
    if (!Platform.isFxApplicationThread()) {
      Platform.runLater(() -> setOverBar(node));
      return;
    }
    overBar.getChildren().clear();
    overBar.getChildren().add(node);
  }

  public static class DefProgressPane extends ProgressPane {
    private Text ovbText = null;

    public DefProgressPane() {
      super();
      this.progress.setPrefWidth(WindowManager.getMainPane().getWidth() - 200);
      this.progress.setStyle("-fx-accent: #" + WindowManager.XIAOMI_COLOR_HEX);
    }

    public void setText(String text) {
      if (!Platform.isFxApplicationThread()) {
        Platform.runLater(() -> setText(text));
        return;
      }
      if (ovbText == null) {
        ovbText = new Text(text);
        ovbText.setFont(Font.font(13));
        ovbText.setTextAlignment(TextAlignment.CENTER);
        setOverBar(ovbText);
      }
      ovbText.setText(text);
    }

    public void setText(LRes lres) {
      setText(lres.toString());
    }

    public UpdateListener getUpdateListener(int updateEveryMillis) {
      AdvancedUpdateListener advancedUpdateListener = new AdvancedUpdateListener(updateEveryMillis);
      advancedUpdateListener.addOnStart(
          new UpdateListener.OnStart() {
            @Override
            public void run(long totalSize) {
              setText(LRes.STARTING_TASK);
            }
          });
      advancedUpdateListener.addOnAdvancedUpdate(
          new AdvancedUpdateListener.OnAdvancedUpdate() {
            @Override
            public void run(
                long downloaded,
                long totalSize,
                AdvancedUpdateListener.DownloadSpeed currentSpeed,
                AdvancedUpdateListener.DownloadSpeed averageSpeed,
                AdvancedUpdateListener.TimeRemaining missingTime) {
              if (downloaded < 0 || totalSize <= 0) {
                setProgress(-1d);
                return;
              }
              double progress = (double) downloaded / totalSize;
              setProgress(progress);
              int index = 0;
              double sp = totalSize, sp2 = downloaded;
              if (totalSize != 100) {
                while (sp > 9999) {
                  sp = sp / 1000;
                  sp2 = sp2 / 1000;
                  ++index;
                  if (Double.isInfinite(sp)) {
                    break;
                  }
                }
                if (index >= UNITS.length) {
                  sp = totalSize;
                  sp2 = downloaded;
                  index = 0;
                }
              }
              LRes measure = LRes.SECONDS;
              if (ChronoUnit.SECONDS.equals(missingTime.getUnit())) {
                measure = LRes.SECONDS;
              } else if (ChronoUnit.MINUTES.equals(missingTime.getUnit())) {
                measure = LRes.MINUTES;
              } else if (ChronoUnit.HOURS.equals(missingTime.getUnit())) {
                measure = LRes.HOURS;
              }
              String text;
              if (totalSize != 100) {
                text =
                    LRes.PROGRESS_TEXT.toString(
                        DECIMAL_FORMAT.format(sp2),
                        DECIMAL_FORMAT.format(sp),
                        UNITS[index] + "B",
                        (int) (progress * 100),
                        averageSpeed.toString(),
                        missingTime.getQuantity(),
                        measure.toString());
              } else {
                text =
                    LRes.PROGRESS_TEXT.toString(
                        DECIMAL_FORMAT.format(sp2),
                        DECIMAL_FORMAT.format(sp),
                        "%",
                        (int) (progress * 100),
                        "",
                        missingTime.getQuantity(),
                        measure.toString());
              }
              setText(text);
            }
          });
      advancedUpdateListener.addOnFinished(
          new UpdateListener.OnFinished() {
            @Override
            public void run(Object subject) {
              setText(LRes.TASK_FINISHED);
              setProgress(1d);
            }
          });
      return advancedUpdateListener;
    }

    public void setContentText(LRes lRes) {
      setContentText(lRes.toString());
    }

    public void setContentText(String msg) {
      Text text = new Text(msg);
      text.setTextAlignment(TextAlignment.CENTER);
      text.setFont(Font.font(17));
      text.setWrappingWidth(WindowManager.getContentWidth());
      setContent(text);
    }
  }
}
