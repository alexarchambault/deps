package deps_

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}
import java.util.concurrent.ConcurrentHashMap

import ujson.{Arr, Obj, Str, read}
import ujson.Value

class Parser(root: Path) {

  private val cache = new ConcurrentHashMap[String, Dependencies]

  private def load(prefix: String): Dependencies = {
    val path = {
      val short = root.resolve(s"${prefix}deps.json").toAbsolutePath
      val long = root.resolve(s"${prefix}dependencies.json").toAbsolutePath
      if (Files.exists(long)) long
      else if (Files.exists(short)) short
      else long
    }
    val b = Files.readAllBytes(path)
    val s = new String(b, StandardCharsets.UTF_8)
    Parser.parse(s)
  }

  def deps(prefix: String = ""): Dependencies = {

    if (!cache.contains(prefix)) {
      val map = load(prefix)
      cache.putIfAbsent(prefix, map)
    }

    cache.get(prefix)
  }

}

object Parser {

  private def parseDep(input: String, version: String): Dependency = {
    val (input0, platformSpecific) =
      if (input.endsWith(":")) (input.stripSuffix(":"), true)
      else (input, false)

    val dep = input0.split(":", -1).toSeq match {
      case Seq(org, name) =>
        Dependency(org, name, version)
      case Seq(org, "", name) =>
        Dependency(org, name, version, cross = Cross.Binary)
      case Seq(org, "", "", name) =>
        Dependency(org, name, version, cross = Cross.Full)
      case _ =>
        sys.error(s"Malformed module '$input'")
    }

    dep.copy(platformSpecific = platformSpecific)
  }

  def update(input: String, deps: Dependencies): String = {

    val json = read(input).obj

    val orig = json.getOrElse("dependencies", Obj()).obj
    for ((k, v) <- deps.initialDependencies) {
      if (orig.contains(k))
        orig.update(k, Str(v.version))
      else
        orig.put(k, Str(v.version))
    }

    for ((k, l) <- deps.versions) {
      val elem =
        if (l.lengthCompare(1) == 0)
          Str(l.head)
        else
          Arr(l.map(Str(_)): _*)
      if (json.contains(k))
        json.update(k, elem)
      else
        json.put(k, elem)
    }

    json.render(indent = 2)
  }

  def parse(input: String): Dependencies = {

    val json = read(input).obj

    def deps(key: String) = json
      .getOrElse(key, Obj())
      .obj
      .toMap
      .map {
        case (k, v) =>
          k -> v.str
      }
      .iterator
      .map {
        case (k, v) =>
          val dep = parseDep(k.filter(!_.isSpaceChar), v)
          k -> dep
      }
      .toMap

    val deps0 = deps("dependencies")
    val crossDeps = deps("crossDependencies").map {
      case (k, dep) =>
        k -> dep.copy(platformSpecific = true)
    }

    val repositories = json
      .getOrElse("repositories", Arr())
      .arr
      .map(_.str)
      .toVector

    val versions = json
      .iterator
      .filter(kv => kv._1 != "dependencies" && kv._1 != "repositories")
      .collect {
        case (k, Str(str)) =>
          k -> Seq(str)
        case (k, Arr(a)) if a.nonEmpty && a.forall { case Str(_) => true; case _ => false } =>
          k -> a.toVector.map(_.str)
      }
      .toMap

    Dependencies(deps0, crossDeps, repositories, versions)
  }

}
