package deps.cli

import coursier.{Dependency => _, _}
import coursier.cache.Cache
import coursier.core.Version
import coursier.util.Task
import deps_.{Cross, Dependency, Dependencies}

object Updates {

  sealed abstract class AtomicUpdate extends Product with Serializable {
    def apply(deps: Dependencies): Dependencies
    def shortName: String
  }

  final case class DependencyUpdate(
    key: String,
    dependency: Dependency
  ) extends AtomicUpdate {
    def apply(deps: Dependencies): Dependencies =
      deps.copy(
        initialDependencies = deps.initialDependencies + (key -> dependency)
      )
    def shortName: String =
      s"${dependency.name}-${dependency.version}"
  }

  final case class ShortDependencyUpdate(
    key: String,
    index: Int,
    version: String
  ) extends AtomicUpdate {
    def apply(deps: Dependencies): Dependencies =
      deps.copy(
        versions = deps.versions + (key -> deps.versions.getOrElse(key, Seq.fill(index)("")).updated(index, version))
      )
    def shortName: String =
      s"$key-$version"
  }

  def updateAll(deps: Dependencies, cache: Cache[Task] = Cache.default): Task[Option[Dependencies]] =
    updates(deps, cache).map { updates =>
      if (updates.isEmpty)
        None
      else
        Some(updates.foldLeft(deps)((deps0, update) => update(deps0)))
    }

  def updates(deps: Dependencies, cache: Cache[Task] = Cache.default): Task[Seq[AtomicUpdate]] = {

    val scalaVersionOpt = deps
      .versions
      .get("scala")
      .filter(_.nonEmpty)
      .map(l => l.map(Version(_)).max.repr)

    val depUpdates = deps.initialDependencies.toVector.sortBy(_._1).map {
      case (k, dep) =>
        if (scalaVersionOpt.isEmpty && dep.cross != Cross.None)
          Task.point(Nil)
        else {
          val mod = Module(Organization(dep.organization), ModuleName(dep.fullName(scalaVersionOpt.getOrElse(""))))
          Versions(cache).withModule(mod).versions().map { ver =>
            val latestOpt =
              if (ver.release.nonEmpty) Some(ver.release)
              else if (ver.available.nonEmpty) Some(ver.available.map(Version(_)).max.repr)
              else None
            val updatedOpt = latestOpt.filter(v => Version(v).compare(Version(dep.version)) > 0)
            updatedOpt.toSeq.map { updated =>
              val deps0 = deps.copy(
                initialDependencies = deps.initialDependencies
              )
              DependencyUpdate(k, dep.copy(version = updated))
            }
          }
        }
    }

    val shortDepsUpdates = deps.versions.toVector.sortBy(_._1).map {
      case ("scala", versions) =>
        val byBinVer = versions.groupBy(_.split('.').take(2).mkString("."))
        val multiVer = byBinVer.exists(_._2.lengthCompare(1) > 0)
        if (multiVer)
          ???
        else
          Versions(cache).withModule(mod"org.scala-lang:scala-library").versions().map { versions0 =>
            versions.zipWithIndex.flatMap {
              case (sv, idx) =>
                val prefix = sv.split('.').take(2).map(_ + ".").mkString
                val available = versions0.available.filter(_.startsWith(prefix))
                Some(available)
                  .filter(_.nonEmpty)
                  .map(_.map(Version(_)).max)
                  .map(latest => ShortDependencyUpdate("scala", idx, latest.repr))
                  .toSeq
            }
          }
      case ("sbt", Seq(ver)) =>
        Versions(cache).withModule(mod"org.scala-sbt:sbt").versions().map { versions =>
          val latest = versions.latest
          if (Version(latest).compare(Version(ver)) > 0)
            Seq(ShortDependencyUpdate("sbt", 0, latest))
          else
            Nil
        }
      case _ =>
        Task.point(Nil)
    }

    val builderTask = (depUpdates ++ shortDepsUpdates).foldLeft(Task.delay(List.newBuilder[Seq[AtomicUpdate]])) {
      (t, up) =>
        for (b <- t; up0 <- up) yield b += up0
    }
    builderTask.map(_.result().flatMap(_.toList))
  }
}