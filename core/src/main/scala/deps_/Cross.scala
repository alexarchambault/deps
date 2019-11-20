package deps_

sealed abstract class Cross extends Product with Serializable

object Cross {
  case object None extends Cross
  case object Binary extends Cross
  case object Full extends Cross
}