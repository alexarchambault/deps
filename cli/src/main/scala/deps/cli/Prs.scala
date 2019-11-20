package deps.cli

import caseapp._
import deps_.Parser

import java.nio.file.Files
import java.nio.charset.StandardCharsets
import scala.concurrent.duration.Duration
import scala.concurrent.Await

object Prs extends CaseApp[PrsOptions] {
  def run(options: PrsOptions, args: RemainingArgs): Unit = {

    val dir = Util.dir(options.shared.dir)
    val depFiles = Util.depFiles(dir)

    val updatesTask = depFiles.map { f =>
      val b = Files.readAllBytes(f)
      val s = new String(b, StandardCharsets.UTF_8)
      val deps = Parser.parse(s)
      val updates = Updates.updates(deps, ???)
      updates.map(_.map(f -> _))
    }

    val updates: Seq[Any] = ??? // Await.result(updatesTask.future(), Duration.Inf)

    if (updates.isEmpty)
      println("Nothing to update")
    else {

      GitRepository.cloneAndCheckout(
        ???,
        ???,
        ???
      )
    }

    ???
  }
}
