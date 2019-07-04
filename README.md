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

#### 1.1 开发环境需求
- Maven 3.5 (旧版或许也可以运行)
- JDK 1.8（至少 40 以上版本）

#### 1.2 操作系统需求
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

#### 2.1 jar 打包
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

#### 2.2 native 打包
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

## 声明
本仓库仅供学习交流使用，请勿用于任何商业活动。请于下载24小时内删除。^_^!


[1]:https://github.com/mrzhqiang/mir2-applem2