import java.io.File

import sbt.Keys.{`package` => pack}
organization in ThisBuild := "com.geirsson"
scalaVersion in ThisBuild := "2.11.8"

addCommandAlias("pluginRun", "runner/run")
onLoad in Global :=
  ((s: State) => "updateIdea" :: s)
    .compose(onLoad.in(Global).value)

val pluginVer = "0.1.0"
val pluginName = "intellij-ammonite"
lazy val plugin: Project = project
  .enablePlugins(SbtIdeaPlugin)
  .settings(
    name := pluginName,
    autoScalaLibrary := false,
    version := pluginVer,
    assemblyOption in assembly :=
      (assemblyOption in assembly).value.copy(includeScala = false),
    ideaInternalPlugins := Seq(),
    ideaBuild := "2016.3.2",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-library" % scalaVersion.value,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value
    ),
    ideaExternalPlugins := Seq(IdeaPlugin.Zip(
      "scala-plugin",
      url(
        "https://plugins.jetbrains.com/plugin/download?pr=idea&updateId=28632"))),
    aggregate in updateIdea := false,
    assemblyExcludedJars in assembly := ideaFullJars.value,
    ideaBuild := "163.7743.17"
  )

lazy val runner = project
  .dependsOn(plugin % Provided)
  .settings(
    autoScalaLibrary := false,
    unmanagedJars in Compile := unmanagedJars.in(plugin, Compile).value,
    unmanagedJars in Compile +=
      file(System.getProperty("java.home")).getParentFile / "lib" / "tools.jar",
    fork in run := true,
    mainClass in (Compile, run) := Some("com.intellij.idea.Main"),
    javaOptions in run ++= Seq(
      "-Xmx800m",
      "-XX:ReservedCodeCacheSize=64m",
      "-XX:MaxPermSize=250m",
      "-XX:+HeapDumpOnOutOfMemoryError",
      "-ea",
      "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005",
      "-Didea.is.internal=true",
      "-Didea.debug.mode=true",
      "-Dapple.laf.useScreenMenuBar=true",
      s"-Dplugin.path=${packagedPluginDir.value}",
      "-Didea.ProcessCanceledException=disabled"
    ),
    run in Compile := run
      .in(Compile)
      .dependsOn(pack.in(packager))
      .evaluated
  )

lazy val packager = project.settings(
  artifactPath := packagedPluginDir.value,
  dependencyClasspath := dependencyClasspath.in(plugin, Compile).value,
  mappings := {
    val pluginMapping = pack
        .in(plugin, Compile)
        .value -> "lib/intellij-ammonite.jar"

    def compilerLibrary(file: Attributed[File]) = {
      file.get(moduleID.key).exists(_.name.contains("compiler"))
    }

    val dependenciesMapping = dependencyClasspath
      .in(plugin, Compile)
      .value
      .filter(_.get(moduleID.key).isDefined)
      .filterNot(compilerLibrary)
      .map { file =>
        val moduleId = file.get(moduleID.key).get
        file.data -> s"lib/${moduleId.name}.jar"
      }
      .toList

    pluginMapping :: dependenciesMapping
  },
  pack := {
    val destination = artifactPath.value
    IO.delete(destination)
    val (dirs, files) = mappings.value.partition(_._1.isDirectory)
    dirs foreach {
      case (from, to) =>
        IO.copyDirectory(from, destination / to, overwrite = true)
    }
    files foreach { case (from, to) => IO.copyFile(from, destination / to) }

    artifactPath.value
  }
)

lazy val packagePlugin = TaskKey[File](
  "package-plugin",
  "Create plugin's zip file ready to load into IDEA")

packagePlugin in plugin := {
  val ideaJar = (assembly in plugin).value
  val sources = Seq(ideaJar -> s"$pluginName/lib/${ideaJar.getName}")
  val out = plugin.base / "bin" / s"$pluginName-$pluginVer.zip"
  IO.zip(sources, out)
  out
}

lazy val packagedPluginDir =
  settingKey[File]("Path to packaged, but not yet compressed plugin")

packagedPluginDir in ThisBuild := baseDirectory
  .in(ThisBuild)
  .value / "target" / name.in(plugin).value
