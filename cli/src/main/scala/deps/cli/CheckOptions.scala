package deps.cli

import caseapp._

final case class CheckOptions(
  @Recurse
    shared: SharedOptions
)
