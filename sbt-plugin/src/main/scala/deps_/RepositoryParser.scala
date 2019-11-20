package deps_

import java.util.Locale
import java.util.regex.Pattern

import sbt.librarymanagement.{MavenRepository, Resolver}

object RepositoryParser {

  private lazy val useSecureResolvers = {
    // adapted from https://github.com/sbt/librarymanagement/blob/60b4356de16073a87c8df297301d95de8ce7ecf5/core/src/main/scala/sbt/internal/librarymanagement/LMSysProp.scala#L15
    val opt = sys.props.get("sbt.repository.secure").flatMap { x =>
      x.toLowerCase(Locale.ENGLISH) match {
        case "1" | "always" | "true" => Some(true)
        case "0" | "never" | "false" => Some(false)
        case "auto"                  => None
        case _                       => None
      }
    }

    opt.getOrElse(true)
  }

  // Same as the repository parsers found in coursier, but returning sbt.Resolver-s

  private def bintrayOwnerId(s: String): (String, String) =
    if (s.contains("/"))
      s.split(Pattern.quote("/"), 2) match {
        case Array(o, i) => (o, i)
        case _ => sys.error(s"Malformed bintray repository '$s' (expected owner/id)")
      }
    else
      (s, "maven")

  private def https: String =
    if (useSecureResolvers) "https" else "http"

  // adapted from https://github.com/coursier/coursier/blob/718f5139d6915f28f5c7c262210ed729ffc661d5/modules/coursier/shared/src/main/scala/coursier/internal/SharedRepositoryParser.scala
  def repository(s: String): Resolver =
    if (s == "central")
      Resolver.mavenCentral
    else if (s.startsWith("sonatype:"))
      Resolver.sonatypeRepo(s.stripPrefix("sonatype:"))
    else if (s.startsWith("bintray:")) {
      val s0 = s.stripPrefix("bintray:")
      val (owner, repo) = bintrayOwnerId(s0)
      Resolver.bintrayRepo(owner, repo)
    } else if (s.startsWith("bintray-ivy:")) {
      val s0 = s.stripPrefix("bintray-ivy:")
      val (owner, repo) = bintrayOwnerId(s0)
      Resolver.bintrayIvyRepo(owner, repo)
    } else if (s.startsWith("typesafe:ivy-"))
      Resolver.typesafeIvyRepo(s.stripPrefix("typesafe:ivy-"))
    else if (s.startsWith("typesafe:"))
      Resolver.typesafeRepo(s.stripPrefix("typesafe:"))
    else if (s.startsWith("sbt-maven:"))
      Resolver.sbtPluginRepo(s.stripPrefix("sbt-maven:"))
    else if (s.startsWith("sbt-plugin:"))
      Resolver.sbtPluginRepo(s.stripPrefix("sbt-plugin:"))
    else if (s.startsWith("ivy:")) {
      // val s0 = s.stripPrefix("ivy:")
      // val sepIdx = s0.indexOf('|')
      // if (sepIdx < 0)
      //   IvyRepository.parse(s0)
      // else {
      //   val mainPart = s0.substring(0, sepIdx)
      //   val metadataPart = s0.substring(sepIdx + 1)
      //   IvyRepository.parse(mainPart, Some(metadataPart))
      // }
      sys.error("Literal Ivy repositories not supported yet")
    } else if (s == "jitpack")
      MavenRepository("jitpack", s"$https://jitpack.io")
    else
      MavenRepository(s.stripPrefix("http://").stripPrefix("https://").replace('/', '-').replace('.', '-'), s)


}