package deps_

import java.io.File
import java.nio.file.Paths

import scala.reflect.macros.whitebox

abstract class DepsMacros(val c: whitebox.Context) {

  import c._
  import c.universe.{Tree => _, _}

  protected def dependencyTree(dep: Dependency): Tree


  private val rootOpt = {
    val f = new File(c.enclosingPosition.source.path).getAbsoluteFile

    def root(f: File): Option[File] =
      if (f == null) None
      else {
        val deps = new File(f, "deps.json")
        if (deps.isFile)
          Some(f)
        else
          root(f.getParentFile)
      }

    root(f.getParentFile)
  }

  private val parser = new Parser(
    rootOpt.fold(Paths.get(sys.props("user.dir")))(_.toPath)
  )


  def impl(selector: Tree): Tree = {

    val q"${methodString: String}" = selector
    val deps = parser.deps()
    if (methodString.endsWith("Version")) {
      val name = methodString.stripSuffix("Version")
      deps.versions.get(name) match {
        case Some(versions) if versions.nonEmpty =>
          val ver = versions.last // FIXME Sort list before
          q"$ver"
        case _ =>
          throw new Exception(s"No versions found for '$name' (available: ${deps.versions.keySet.toVector.sorted.mkString(", ")})")
      }
    } else if (methodString.endsWith("VersionOpt")) {
      val name = methodString.stripSuffix("VersionOpt")
      deps.versions.get(name) match {
        case Some(versions) if versions.nonEmpty =>
          val ver = versions.last // FIXME Sort list before
          q"_root_.scala.Some($ver)"
        case _ =>
          q"_root_.scala.None"
      }
    } else if (methodString.endsWith("Versions")) {
      val name = methodString.stripSuffix("Versions")
      deps.versions.get(name) match {
        case Some(versions) =>
          val versions0 = versions.map(v => q"$v")
          q"_root_.scala.Seq(..$versions0)"
        case None =>
          throw new Exception(s"No versions found for '$name'")
      }
    } else if (methodString.endsWith("VersionsOpt")) {
      val name = methodString.stripSuffix("VersionsOpt")
      deps.versions.get(name) match {
        case Some(versions) =>
          val versions0 = versions.map(v => q"$v")
          q"_root_.scala.Some(_root_.scala.Seq(..$versions0))"
        case None =>
          q"_root_.scala.None"
      }
    } else {
      val dep = deps.dependencies.getOrElse(
        methodString,
        sys.error(s"not found: $methodString (available keys: ${deps.dependencies.keySet.toVector.sorted.mkString(", ")})")
      )
      dependencyTree(dep)
    }
  }

  def vImpl(selector: Tree): Tree = {
    val q"${methodString: String}" = selector
    val dep = parser.deps().dependencies.apply(methodString)
    q"${dep.version}"
  }

  def repositoriesImpl: Tree = {
    val l = parser.deps().repositories.map(r => q"$r")
    q"Seq(..$l)"
  }

}