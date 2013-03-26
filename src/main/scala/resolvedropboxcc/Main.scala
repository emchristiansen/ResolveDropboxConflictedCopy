package resolvedropboxcc

import nebula._
import org.rogach.scallop.ScallopConf
import nebula.ExistingFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

///////////////////////////////

class Conf(args: Seq[String]) extends ScallopConf(args) {
  banner("ResolveDropboxConflictedCopy: Resolves Dropbox conflicts by taking the newer file.")

  val conflictedCopy = opt[String](
    descr = "Conflicted copy of file. Must have 'conflicted copy' in name.",
    required = true) map ExistingFile.apply
}

object Main {
  def main(unparsedArgs: Array[String]) {
    val args = new Conf(unparsedArgs)

    val conflictedCopy = args.conflictedCopy()
    asserty(conflictedCopy.toString.contains("conflicted copy"))
    
    val original = {
      val conflictedName = conflictedCopy.getPath
      val originalName = 
        """ \(.*conflicted copy.*\)""".r replaceAllIn (conflictedName, "")
      ExistingFile(originalName)
    }

    if (original.lastModified > conflictedCopy.lastModified) {
      // The original is newer, so delete the conflicted copy.
      println(s"Deleting ${conflictedCopy.getPath}.")
      conflictedCopy.delete
    } else {
      // The conflicted copy is newer, so delete the original and rename
      // the conflicted copy.
      println(s"Moving ${conflictedCopy.getPath} to ${original.getPath}.")
      Files.move(
        conflictedCopy.toPath,
        original.toPath,
        StandardCopyOption.REPLACE_EXISTING)      
    }
  }
}
