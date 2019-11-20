package deps_

import sbt._
import sbt.Keys._
import java.nio.file.Paths

object DepsPlugin extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = sbt.plugins.JvmPlugin

  private lazy val deps = {
    val parser = new Parser(Paths.get(sys.props("user.dir")))
    parser.deps()
  }

  override def projectSettings: Seq[Setting[_]] = {
    val resolversSettings = Def.settings(
      resolvers ++= deps.repositories.map(RepositoryParser.repository)
    )
    val scalaVersionSettings =
      deps.versions.get("scala").toSeq.filter(_.nonEmpty).map { l =>
        val sv = l.last
        scalaVersion := sv
      }
    val crossScalaVersionsSettings =
      deps.versions.get("scala").toSeq.map { l =>
        crossScalaVersions := l
      }

    resolversSettings ++ scalaVersionSettings ++ crossScalaVersionsSettings
  }
}
