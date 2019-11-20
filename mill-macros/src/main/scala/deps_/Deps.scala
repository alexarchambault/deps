package deps_

import scala.language.dynamics
import scala.language.experimental.macros

trait Deps extends Dynamic {

  object V extends Dynamic {
    def selectDynamic(selector: String): Any = macro MillDepsMacros.vImpl
  }

  def repositories: Seq[String] = macro MillDepsMacros.repositoriesImpl

  def selectDynamic(selector: String): Any = macro MillDepsMacros.impl

}

object Deps extends Deps
