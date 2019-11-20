package deps_

import utest._

object MillDepsTests extends TestSuite {

  final case class Dep(value: String)

  implicit class StringContextOps(private val sc: StringContext) extends AnyVal {
    def ivy(args: Any*): Dep = {
      val s = sc.s(args: _*)
      Dep(s)
    }
  }

  val tests = Tests {
    val expectedCirceVersion = "0.12.1"
    val expectedCirce = Dep(s"io.circe::circe-core:$expectedCirceVersion")

    "simple name" - {
      val circe = Deps.`circe-core`
      assert(circe == expectedCirce)
    }
    "full name" - {
      val circe = Deps.`io.circe::circe-core`
      assert(circe == expectedCirce)
    }

    "version" - {
      val circeVersion = Deps.V.`circe-core`
      assert(circeVersion == expectedCirceVersion)
    }

    "repositories" - {
      val repositories = Deps.repositories
      val expectedRepositories = Seq("typesafe:ivy-releases")
      assert(repositories == expectedRepositories)
    }

    "scala version" - {
      val scalaVersion = Deps.scalaVersion
      val expectedScalaVersion = "2.13.1"
      assert(scalaVersion == expectedScalaVersion)
    }

    "scala versions" - {
      val scalaVersions = Deps.scalaVersions
      val expectedScalaVersions = Seq("2.12.10", "2.13.1")
      assert(scalaVersions == expectedScalaVersions)
    }
  }

}
