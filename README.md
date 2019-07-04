[mir2-applem2]:https://github.com/mrzhqiang/mir2-applem2

Youth Mir 2
-----------
~~这是一个基于 Java 语言的 GUI 图形程序项目~~，有意于移植 [mir2-applem2][1] 代码到 Java 平台，可能存在诸多问题，希望不会有放弃的一天。

**现在 Java GUI 从 swing 重构为 JavaFx，则可视化界面由 GUI Form 改为 `*.fxml`，基本上使用方式不变。**

## 背景
个人喜欢玩传奇，从 2016 年至今，独立运营一个公益私服。

在 2017 年 8 月，给力引擎的 QQ 群不再提供延期的 M2Server 主程序，公益私服被迫转向 GEE 引擎。

GEE 引擎很新颖，功能也很强大，唯一的缺点就是经常报毒。

为了免受木马病毒的干扰，最终决定改写 [mir2-applem2][1] 引擎。

但 delphi 2007 所使用的 Object Pascal 语言，终究引发了水土不服，所以我开始重构为 JavaFx 程序。

## 一、需求
编译运行这个项目的基础需求。

### 1.1 开发环境需求
- Maven 3.5 (旧版或许也可以运行)
- JDK 1.8（至少 40 以上版本）

### 1.2 操作系统需求（可忽略）
- (Windows) EXE installers: Inno Setup
- (Windows) MSI installers: WiX (at least version 3.7)
- (Linux) DEB installers: dpkg-deb
- (Linux) RPM installers: rpmbuild
- (Mac) DMG installers: hdiutil
- (Mac) PKG installers: pkgbuild


## 二、打包
由于 Youth M2 是 JavaFX 程序，打包方式与普通的 Java 有点区别。

而使用 IDEA 的 Artifacts 只能构建正经 JavaFX 程序，对于 Maven 多模块结构无能为力。

所以我们要利用 Maven 插件进行一键打包。

### 2.1 jar 打包
添加以下插件到顶级模块（root 模块）的 `pom.xml` 文件：
```xml
<plugin>
    <groupId>com.zenjava</groupId>
    <artifactId>javafx-maven-plugin</artifactId>
    <version>8.8.3</version>
    <configuration>
        <mainClass>your.package.with.Launcher</mainClass>
    </configuration>
</plugin>
```

*提示：如果顶级模块不包含启动程序，则随意选择一个 Main 入口即可。*

使用 `mvn jfx:jar` 命令进行打包，编译输出的目录位于：`target/jfx/app`。

### 2.2 native 打包
添加以下插件到顶级模块（root 模块）的 `pom.xml` 文件：
```xml
<plugin>
    <groupId>com.zenjava</groupId>
    <artifactId>javafx-maven-plugin</artifactId>
    <version>8.8.3</version>
    <configuration>
        <vendor>YourCompany</vendor>
        <mainClass>your.package.with.Launcher</mainClass>
    </configuration>
</plugin>
```

*提示：native 实际上包含 jar 打包。*

使用 `mvn jfx:native` 命令进行打包，编译输出的目录位于：`target/jfx/native`。

### 2.3 exe4j 打包
native 打包出来的只是按不同操作系统区分的安装包，实际上这种方式对于需要移植的引擎体系来说，非常不合理。

**TODO：未来应该设计为多模块单应用的架构，安装一次就可以启动所有服务、网关、插件等等。**

我们暂时使用 exe4j 工具来逐个集成 jar 打包出来的 `*.jar`，每个 Java 程序对应一个 `*.exe`。

1. 首先找到 `./tool/exe4.exe` 工具，安装到你的系统中。

2. 随后在 `./exe4j/` 目录中选一个 `*.exe4j` 文件，点击 `9.Compile executable` 进行打包。

3. 可以按照第二步逐个打包所有程序，也可以进行一定的参数修改后，再打包所有程序。

**注意：如果需要测试运行的话，必须将 `./tool/jre.zip` 解压到程序目录下。也可以安装 JDK1.8u40 以上版本，并将环境变量 `JAVA_HOME` 或 `JDK_HOME` 指向此目录。**


## 声明
本仓库仅供学习交流使用，请勿用于任何商业活动。请于下载24小时内删除。^_^!


[1]:https://github.com/mrzhqiang/mir2-applem2