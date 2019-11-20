package deps.cli

import java.nio.file.Paths
import java.nio.file.Files
import java.nio.charset.StandardCharsets

import caseapp._
import coursier.cache.Cache

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import coursier.cache.FileCache
import coursier.cache.loggers.RefreshLogger
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ProgressMonitor

import scala.collection.JavaConverters._

object Update extends CaseApp[UpdateOptions] {

  private final class SimpleProgressMonitor extends ProgressMonitor {
    def start(totalTasks: Int): Unit =
      () // System.err.println(s"Starting work on $totalTasks tasks")
    def beginTask(title: String, totalWork: Int): Unit =
      System.err.println(s"$title: $totalWork")
    def update(completed: Int): Unit =
      () // System.err.print(".")
    def endTask(): Unit =
      System.err.println("Done")
    def isCancelled(): Boolean =
      false
  }

  private def updateGitRepository(upstream: String, originOpt: Option[String]): Unit = {

    val origin = originOpt.getOrElse(upstream)
    val repo = Paths.get("repo")
    if (Files.exists(repo))
      sys.error(s"$repo already exists")
    var cloneResult: Git = null
    try {
      cloneResult = Git.cloneRepository()
        .setRemote("upstream")
        .setURI(upstream)
        .setDirectory(repo.toFile())
        .setProgressMonitor(new SimpleProgressMonitor)
        .call()

      val branchList = cloneResult.branchList().call().asScala.toList
      val baseBranch = branchList.head.getName()
      val baseBranch0 =
        if (baseBranch.startsWith("refs/heads/")) baseBranch.stripPrefix("refs/heads/")
        else ???
      System.err.println(s"branch: $baseBranch0")


    } finally {
      if (cloneResult != null)
        cloneResult.close()
    }
  }

  private def updateInDirectory(dirOpt: Option[String], atomicOutputDirOpt: Option[String]): Unit = {
    val dir = Paths.get(dirOpt.getOrElse("."))
    val depsFile = dir.resolve("deps.json")
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
    options.upstream match {
      case Some(upstream) =>
        updateGitRepository(upstream, options.origin)
      case None =>
        updateInDirectory(options.shared.dir, options.atomic)
    }
}