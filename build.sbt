lazy val plugin = project
  .enablePlugins(SbtIdeaPlugin)
  .settings(
    ideaBuild := "2016.1.3",
    ideaEdition := IdeaEdition.Community,
    ideaDownloadDirectory in ThisBuild := baseDirectory.value / "idea",
    ideaInternalPlugins := Seq(),
    ideaExternalPlugins := Seq(IdeaPlugin.Zip(
      "scala-plugin",
      url(
        "https://plugins.jetbrains.com/plugin/download?pr=&updateId=29035"))),
    cleanFiles += ideaDownloadDirectory.value
  )
