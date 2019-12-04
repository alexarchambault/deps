package deps.cli

import caseapp._

final case class UpdateOptions(
  dir: Option[String] = None,
  atomic: Option[String] = None
)
