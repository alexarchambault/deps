package deps_

final case class Dependency(
  organization: String,
  name: String,
  version: String,
  cross: Cross = Cross.None,
  platformSpecific: Boolean = false
) {
  def moduleRepr: String = {
    val orgNameSep = cross match {
      case Cross.None => ":"
      case Cross.Binary => "::"
      case Cross.Full => ":::"
    }
    val platformSuffix =
      if (platformSpecific) ":"
      else ""
    s"$organization$orgNameSep$name$platformSuffix"
  }
  def fullName(scalaVersion: String): String = {
    val sbv = scalaVersion.split('.').take(2).mkString(".") // FIXME Won't work with RCs / milestones
    fullName(scalaVersion, sbv, "")
  }
  def fullName(scalaVersion: String, scalaBinaryVersion: String, platformSuffix: String): String = {
    val scalaSuffix = cross match {
      case Cross.None => ""
      case Cross.Binary => "_" + scalaBinaryVersion
      case Cross.Full => "_" + scalaVersion
    }
    val platformSuffix0 =
      if (platformSpecific) platformSuffix
      else ""
    s"$name$scalaSuffix$platformSuffix0"
  }
  def repr: String =
    s"$moduleRepr:$version"
}