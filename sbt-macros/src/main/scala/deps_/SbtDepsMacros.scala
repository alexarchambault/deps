package deps_

import scala.reflect.macros.whitebox

class SbtDepsMacros(c0: whitebox.Context) extends DepsMacros(c0) {

  import c._
  import c.universe.{Tree => _, _}

  protected def dependencyTree(dep: Dependency): Tree =
    if (dep.platformSpecific)
      // FIXME We're ignoring dep.cross here
      q""" ${dep.organization} %%% ${dep.name} % ${dep.version} """
    else
      dep.cross match {
        case Cross.None =>
          q""" ${dep.organization} % ${dep.name} % ${dep.version} """
        case Cross.Binary =>
          q""" ${dep.organization} %% ${dep.name} % ${dep.version} """
        case Cross.Full =>
          q""" (${dep.organization} % ${dep.name} % ${dep.version}).cross(CrossVersion.full) """
      }

}
