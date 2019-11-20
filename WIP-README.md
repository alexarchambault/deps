# deps

*deps* allows to centralize your dependencies versions in a single JSON file at the
root of your project, making it easier to keep them in check and update them.

It works from both [mill](https://www.lihaoyi.com/mill) and
[sbt](https://www.scala-sbt.org), and allows to keep track of
- dependencies,
- repositories,
- scala versions,
- and sbt version and sbt plugins are in the work.

## sbt

Put dependencies, scala versions, and repositories, in a file named `deps.json` at the
root of your project:
```json
$ cat deps.json
{
  "dependencies": {
    "com.lihaoyi::fastparse": "2.1.3"
  },
  "repositories": [
    "central",
    "typesafe:ivy-releases"
  ],
  "scala": ["2.12.10", "2.13.1"]
}
```

Add to your `project/plugins.sbt`
```scala
addSbtPlugin("io.github.alexarchambault.deps" % "sbt-deps" % "0.1.0")
```
The latest version is
[![Maven Central](https://img.shields.io/maven-central/v/io.github.alexarchambault.deps/sbt-macros_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.alexarchambault.deps/sbt-macros_2.12)

The sbt-deps plugin automatically sets `scalaVersion` and `resolvers` according to
the `"scala"` and `"repositories"` fields of `deps.json`.

To refer to dependencies from the `"dependencies"` field, you can do
```scala
libraryDependencies += Deps.fastparse
libraryDependencies += Deps.`com.lihaoyi::fastparse`
```

You can also refer to the scala versions with
```scala
Deps.scalaVersions // Seq[String]
```
and directly to dependency versions with
```scala
Deps.V.fastparse
Deps.V.`com.lihaoyi::fastparse`
```

## mill

From your `build.sc`, add the `mill-macros` dependency
```scala
import $ivy.`io.github.alexarchambault.deps::mill-macros:0.1.0`, deps_.Deps
```
The latest version is
[![Maven Central](https://img.shields.io/maven-central/v/io.github.alexarchambault.deps/mill-macros_2.13.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.alexarchambault.deps/mill-macros_2.13)

Then refer to the dependencies from the `"dependencies"` field with
```scala
def ivyDeps = Agg(Deps.fastparse)
def ivyDeps = Agg(Deps.`com.lihaoyi::fastparse`)
```

Add the scala versions with
```scala
def scalaVersion = Deps.scalaVersion // String, last version from the list
Deps.scalaVersions // Seq[String], full list
```

