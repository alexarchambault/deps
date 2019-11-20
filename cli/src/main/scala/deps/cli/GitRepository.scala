package deps.cli

import java.nio.file.Path
import java.time.ZonedDateTime
import java.nio.ByteBuffer

object GitRepository {

  def cloneAndCheckout(
    repo: String,
    startCommitId: String,
    checkoutIn: Path
  ): Unit = {
    ???
  }

  def createBranch(
    dir: Path,
    branchName: String,
    commitAuthor: String,
    commitDate: ZonedDateTime,
    commitMessage: String,
    updateFiles: Seq[(String, ByteBuffer)],
    push: Boolean
  ): Unit = {
    ???
  }
}