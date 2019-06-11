package youthm2.bootstrap.config;

import com.google.common.base.Preconditions;
import com.typesafe.config.Config;

/**
 * @author mrzhqiang
 */
public class FormConfig {
  private static final String FORM_X = "form.x";
  private static final String FORM_Y = "form.y";
  private static final String FORM_ENABLED = "form.enabled";
  private static final String FORM_FILENAME = "form.filename";

  private int x;
  private int y;
  private boolean enabled;
  private String filename;

  public void load(Config config) {
    Preconditions.checkNotNull(config, "config == null");
    x = config.getInt(FORM_X);
    y = config.getInt(FORM_Y);
    enabled = config.getBoolean(FORM_ENABLED);
    filename = config.getString(FORM_FILENAME);
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }
}
