package deps.cli

final case class Options(
  check: Boolean = false,
  update: Option[Boolean] = None,
  pr: String = "",
  dir: Option[String] = None
)
