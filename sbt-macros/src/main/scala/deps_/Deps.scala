package deps_

import scala.language.dynamics
import scala.language.experimental.macros

trait Deps extends Dynamic {

  object V extends Dynamic {
    def selectDynamic(selector: String): Any = macro SbtDepsMacros.vImpl
  }

  def repositories: Seq[String] = macro SbtDepsMacros.repositoriesImpl

  def selectDynamic(selector: String): Any = macro SbtDepsMacros.impl

}

object Deps extends Deps
