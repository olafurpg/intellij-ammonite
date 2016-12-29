package ammonite.intellij

import javax.swing.Icon

import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.IconLoader
import org.jetbrains.plugins.scala.ScalaLanguage

object AmmoniteFileType extends LanguageFileType(ScalaLanguage.Instance) {
  override def getDefaultExtension: String = "amm"
  override def getIcon: Icon = IconLoader.getIcon("/scala32.png")
  override def getName: String = "Ammonite Scala Script"
  override def getDescription: String = "Ammonite Scala Script Description"
}

class AmmoniteFileTypeFactory extends FileTypeFactory {
  override def createFileTypes(consumer: FileTypeConsumer): Unit = {
    consumer.consume(AmmoniteFileType)
  }
}
