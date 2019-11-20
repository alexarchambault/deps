package deps_

import scala.reflect.macros.whitebox

class MillDepsMacros(c0: whitebox.Context) extends DepsMacros(c0) {

  import c._
  import c.universe.{Tree => _, _}

  protected def dependencyTree(dep: Dependency): Tree = {

    val orgNameSep = dep.cross match {
      case Cross.None => ":"
      case Cross.Binary => "::"
      case Cross.Full => ":::"
    }

    val nameVerSep =
      if (dep.platformSpecific) "::"
      else ":"

    val str = s"${dep.organization}$orgNameSep${dep.name}$nameVerSep${dep.version}"

    q""" _root_.scala.StringContext($str).ivy() """
  }

}
