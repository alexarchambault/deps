package deps.cli

import caseapp._

final case class UpdateOptions(
  @Recurse
    shared: SharedOptions = SharedOptions(),
  atomic: Option[String] = None,
  upstream: Option[String] = None,
  origin: Option[String] = None
)
