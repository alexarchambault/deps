package deps.cli

import java.nio.file.{Files, Path, Paths}

import scala.collection.JavaConverters._

object Util {
  def dir(dirOpt: Option[String]): Path =
    dirOpt match {
      case Some(path) => Paths.get(path)
      case None => Paths.get(".")
    }

  def depFiles(dir: Path): Seq[Path] = {
    var s: java.util.stream.Stream[Path] = null
    try {
      s = Files.list(dir)
      s.iterator()
        .asScala
        .filter { p =>
          val name = p.getFileName().toString()
          name == "deps.json" || name.endsWith("-deps.json")
        }
        .toVector
    } finally {
      s.close()
    }
  }
}