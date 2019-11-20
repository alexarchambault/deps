package deps_

import utest._

object SbtDepsTest extends TestSuite {

  val sbv = scala.util.Properties.versionNumberString
    .split('.')
    .take(2)
    .mkString(".")

  final case class OrgName(org: String, name: String) {
    def %(ver: String): (String, String, String) =
      (org, name, ver)
  }

  implicit class PctStringOps(private val org: String) extends AnyVal {
    def %(name: String): OrgName =
      OrgName(org, name)
    def %%(name: String): OrgName =
      OrgName(org, name + "_" + sbv)
  }


  val tests = Tests {
    val expectedCirce = ("io.circe", "circe-core_" + sbv, "0.12.1")

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
      assert(circeVersion == expectedCirce._3)
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