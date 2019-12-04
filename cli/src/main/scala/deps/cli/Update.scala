package deps.cli

import java.nio.file.Paths
import java.nio.file.Files
import java.nio.charset.StandardCharsets

import caseapp._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import coursier.cache.FileCache
import coursier.cache.loggers.RefreshLogger

object Update extends CaseApp[UpdateOptions] {

  private def updateInDirectory(dirOpt: Option[String], atomicOutputDirOpt: Option[String]): Unit = {
    val dir = Paths.get(dirOpt.getOrElse("."))
    val depsFile = {
      val short = dir.resolve("deps.json")
      val long = dir.resolve("dependencies.json")
      if (Files.exists(long)) long
      else if (Files.exists(short)) short
      else long // report errors for this one
    }
    if (!Files.exists(depsFile))
      sys.error(s"Not found: $depsFile")
    else if (!Files.isRegularFile(depsFile))
      sys.error(s"Not a file: $depsFile")
    else {
      val b = Files.readAllBytes(depsFile)
      val s = new String(b, StandardCharsets.UTF_8)
      val deps = deps_.Parser.parse(s)
      val cache = FileCache().withLogger(RefreshLogger.create())

      atomicOutputDirOpt match {
        case None =>
          val f = Updates.updateAll(deps, cache).future()
          val updatedOpt = Await.result(f, Duration.Inf)
          updatedOpt match {
            case None =>
              System.err.println("Nothing to update")
            case Some(updated) =>
              val newContent = deps_.Parser.update(s, updated)
              println(newContent)
          }
        case Some(outputDir) =>
          val outputDir0 = Paths.get(outputDir)
          Files.createDirectories(outputDir0)
          val f = Updates.updates(deps, cache).future()
          val updates = Await.result(f, Duration.Inf)
          if (updates.isEmpty)
            System.err.println("Nothing to update")
          else if (updates.lengthCompare(1) == 0)
            System.err.println(s"Found one update")
          else
            System.err.println(s"Found ${updates.length} updates")
          for (u <- updates) {
            val name = u.shortName
            val dest = outputDir0.resolve(s"$name.json")
            val updated = u(deps)
            val newContent = deps_.Parser.update(s, updated)
            Files.write(dest, newContent.getBytes(StandardCharsets.UTF_8))
            System.err.println(s"Wrote $dest")
          }
      }
    }
  }

  def run(options: UpdateOptions, args: RemainingArgs): Unit =
    updateInDirectory(options.dir, options.atomic)
}
