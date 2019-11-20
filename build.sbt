
inThisBuild(List(
  organization := "io.github.alexarchambault.deps",
  homepage := Some(url("https://github.com/alexarchambault/deps")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "alexarchambault",
      "Alex Archambault",
      "",
      url("https://github.com/alexarchambault")
    )
  )
))


lazy val scala212 = "2.12.10"
lazy val scala213 = "2.13.1"

lazy val shared = Def.settings(
  scalaVersion := scala212,
  crossScalaVersions := Seq(scala213, scala212),
  scalacOptions += "-deprecation",
  sonatypeProfileName := "io.github.alexarchambault"
)

lazy val utest = Def.settings(
  libraryDependencies += "com.lihaoyi" %% "utest" % "0.6.9" % Test,
  testFrameworks += new TestFramework("utest.runner.Framework")
)

lazy val core = project
  .settings(
    shared,
    utest,
    libraryDependencies ++= {
      val ujsonVer =
        if (scalaVersion.value.startsWith("2.12.")) "0.7.1"
        else "0.7.5"
      Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value,
        "com.lihaoyi" %% "ujson" % ujsonVer
      )
    }
  )

// Waiting for coursier#1439
// lazy val cli = project
//   .dependsOn(core)
//   .enablePlugins(PackPlugin)
//   .settings(
//     shared,
//     libraryDependencies ++= Seq(
//       "com.github.alexarchambault" %% "case-app" % "2.0.0-M9",
//       "io.get-coursier" %% "coursier" % "2.0.0-RC5-2",
//       "org.eclipse.jgit" % "org.eclipse.jgit" % "5.5.0.201909110433-r",
//       "org.slf4j" % "slf4j-nop" % "1.7.28"
//     )
//   )

lazy val `deps-macros` = project
  .dependsOn(core)
  .settings(
    shared,
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
  )

lazy val `mill-macros` = project
  .dependsOn(core, `deps-macros`)
  .settings(
    shared,
    utest
  )

lazy val `sbt-macros` = project
  .dependsOn(core, `deps-macros`)
  .settings(
    shared,
    utest
  )

lazy val `sbt-plugin` = project
  .enablePlugins(ScriptedPlugin)
  .dependsOn(`sbt-macros`)
  .settings(
    shared,
    scalaVersion := scala212,
    crossScalaVersions := Seq(scala212),
    name := "sbt-deps",
    sbtPlugin := true,
    scriptedLaunchOpts ++= Seq(
      "-Dplugin.name=" + name.value,
      "-Dplugin.version=" + version.value,
      "-Dsbttest.base=" + (sourceDirectory.value / "sbt-test").getAbsolutePath // necessary?
    ),
    scriptedDependencies := {
      scriptedDependencies.value
      publishLocal.in(core).value
      publishLocal.in(`deps-macros`).value
      publishLocal.in(`sbt-macros`).value
    },
    // https://github.com/sbt/sbt/issues/5049#issuecomment-528960415
    dependencyOverrides := "org.scala-sbt" % "sbt" % "1.2.8" :: Nil
  )

skip.in(publish) := true
shared
crossScalaVersions := Nil
