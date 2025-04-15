# ToolWindow Manager Advanced for IntelliJ IDEA

***Rework of [ToolWindow Manager](https://plugins.jetbrains.com/plugin/1489-toolwindow-manager) plugin for IntelliJ IDEA
2023+***

Plugin allows you to set visibility preferences to known Tool Windows like
*Bookmarks*, *Notifications* etc. and automatically show or hide them for
each project.

![image info](screen.png)

### How to build / run / install plugin

If you open the project in Intellij IDEA, it will be recognized as IDE plugin. **Gradle (>=8.3)** is used to build the
plugin and manage its dependencies. If necessary, you can build plugin from sources using command:

```shell script
gradle buildPlugin
``` 

It will produce packaged zip file at:

```
build/distributions/twm-advanced-plugin-{version}.zip
```

and it can be later manually installed into Intellij IDEA using its *Settings... -> Plugins -> Cog icon on top ->
Install Plugin from Disk...* option.

You can run plugin within sand-boxed IDE instance. IDEA will provide run configuration by-default, but you can also use
command:

```shell script
gradle runIde
```
