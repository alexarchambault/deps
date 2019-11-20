package deps_

import java.util.regex.Matcher

import scala.collection.mutable.ArrayBuffer

final case class Dependencies(
  initialDependencies: Map[String, Dependency],
  initialCrossDependencies: Map[String, Dependency],
  repositories: Seq[String],
  versions: Map[String, Seq[String]]
) {
  lazy val crossDependencies = Dependencies.expandDependencies(initialCrossDependencies)
  lazy val dependencies = crossDependencies ++ Dependencies.expandDependencies(initialDependencies)
}

object Dependencies {

  private def expandDependencies(deps: Map[String, Dependency]): Map[String, Dependency] =
    deps.map { case (_, dep) => dep.moduleRepr -> dep } ++ deps.iterator.flatMap {
      case (k, dep) =>
        extra(dep).iterator
    }

  private val braceExpandRegex = "[^\\{]*(\\{[^\\}]*\\}).*".r.pattern

  private def extra(dep: Dependency): Seq[(String, Dependency)] = {

    var input0 = dep.name
    input0 = input0.drop(input0.lastIndexOf(':') + 1)
    input0 = input0.filter(!_.isSpaceChar)

    var matcher: Matcher = null
    val elements = List.newBuilder[Seq[String]]

    while ({
      matcher = braceExpandRegex.matcher(input0)
      matcher.matches()
    }) {

      assert(matcher.groupCount == 1)
      val start = matcher.start(1)
      val end = matcher.end(1)
      elements += Seq(input0.take(start))

      val braces = matcher.group(1)
      assert(braces.startsWith("{"))
      assert(braces.endsWith("}"))
      val alternatives = braces.substring(1, braces.length - 1).split(',').toSeq
      elements += alternatives

      input0 = input0.drop(end + 1)
    }

    val expanded =
      elements.result().foldLeft(Seq("")) { (acc, elems) =>
        for (s <- acc; elem <- elems) yield s + elem
      }

    ((dep.name -> dep) +: expanded.map(name0 => name0 -> dep.copy(name = name0))).distinct
  }

}
