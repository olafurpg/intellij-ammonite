resolvers ++= Seq(
  Classpaths.sbtPluginReleases,
  Resolver.bintrayIvyRepo("dancingrobot84","sbt-plugins")
)

addSbtPlugin("com.eed3si9n"       % "sbt-buildinfo"       % "0.6.1")
addSbtPlugin("io.get-coursier"    % "sbt-coursier"        % "1.0.0-M15-1")
addSbtPlugin("com.eed3si9n"       % "sbt-assembly"        % "0.14.3")
addSbtPlugin("com.dancingrobot84" % "sbt-idea-plugin"     % "0.4.2")
