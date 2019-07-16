package youthm2.bootstrap.model.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import youthm2.common.Json;

/**
 * 引导配置。
 *
 * @author qiang.zhang
 */
public final class BootstrapConfig {
  public final StringProperty home = new SimpleStringProperty("");
  public final StringProperty dbName = new SimpleStringProperty("");
  public final StringProperty gameName = new SimpleStringProperty("");
  public final StringProperty gameAddress = new SimpleStringProperty("");
  public final BooleanProperty backupAction = new SimpleBooleanProperty(false);
  public final BooleanProperty compoundAction = new SimpleBooleanProperty(false);

  public final ServerConfig database = new ServerConfig();
  public final PublicServerConfig account = new PublicServerConfig();
  public final ServerConfig logger = new ServerConfig();
  public final ServerConfig core = new ServerConfig();
  public final ProgramConfig game = new ProgramConfig();
  public final ProgramConfig role = new ProgramConfig();
  public final ProgramConfig login = new ProgramConfig();
  public final ProgramConfig rank = new ProgramConfig();

  public String getHome() {
    return home.get();
  }

  public String getDbName() {
    return dbName.get();
  }

  public String getGameName() {
    return gameName.get();
  }

  public String getGameAddress() {
    return gameAddress.get();
  }

  public boolean isBackupAction() {
    return backupAction.get();
  }

  public boolean isCompoundAction() {
    return compoundAction.get();
  }

  public JsonNode toJsonNode() {
    ObjectNode bootstrap = Json.newObject()
        .put("home", home.getValue())
        .put("dbName", dbName.getValue())
        .put("gameName", gameName.getValue())
        .put("gameAddress", gameAddress.getValue())
        .put("backupAction", backupAction.getValue())
        .put("compoundAction", compoundAction.getValue());
    bootstrap.set("database", database.objectNode());
    bootstrap.set("account", account.objectNode());
    bootstrap.set("logger", logger.objectNode());
    bootstrap.set("core", core.objectNode());
    bootstrap.set("game", game.objectNode());
    bootstrap.set("role", role.objectNode());
    bootstrap.set("login", login.objectNode());
    bootstrap.set("rank", rank.objectNode());
    return Json.newObject().set("bootstrap", bootstrap);
  }
}
