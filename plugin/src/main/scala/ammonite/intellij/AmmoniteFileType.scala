package ammonite.intellij

import javax.swing.Icon

import com.intellij.ide.util.projectWizard.EmptyModuleBuilder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.fileTypes.FileTypeConsumer
import com.intellij.openapi.fileTypes.FileTypeFactory
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import org.jetbrains.plugins.scala.ScalaLanguage
import org.jetbrains.plugins.scala.lang.parser.ScalaFileFactory
import org.jetbrains.plugins.scala.lang.psi.ScDeclarationSequenceHolder
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaFileImpl
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.imports.ScImportStmtImpl
import org.jetbrains.plugins.scala.lang.resolve.processor.ResolveProcessor

object AmmoniteFileType extends LanguageFileType(ScalaLanguage.Instance) {
  val logger: Logger = Logger.getInstance(this.getClass)
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

class AmmoniteFileFactory extends ScalaFileFactory {
  def createFile(provider: FileViewProvider): Option[AmmoniteFileImpl] = {
    AmmoniteFileType.logger.debug("File factory")
    Option(provider.getVirtualFile.getFileType) collect {
      case AmmoniteFileType => new AmmoniteFileImpl(provider)
    }
  }
}

class AmmoniteFileImpl(provider: FileViewProvider)
    extends ScalaFileImpl(provider, AmmoniteFileType)
    with ScDeclarationSequenceHolder {

  override def processDeclarations(processor: PsiScopeProcessor,
                                   state: ResolveState,
                                   lastParent: PsiElement,
                                   place: PsiElement): Boolean =
    super[ScalaFileImpl].processDeclarations(processor,
                                             state,
                                             lastParent,
                                             place) &&
      super[ScDeclarationSequenceHolder].processDeclarations(processor,
                                                             state,
                                                             lastParent,
                                                             place) &&
      processAmmoniteDeclarations(processor, state, lastParent, place)

  private def processAmmoniteDeclarations(processor: PsiScopeProcessor,
                                          state: ResolveState,
                                          lastParent: PsiElement,
                                          place: PsiElement): Boolean = {
    (processor, lastParent) match {
      case (rp: ResolveProcessor, imp: ScImportStmtImpl)
          if rp.name == "$ivy" =>
        println("BOOM!")
        true
      case _ =>
        true
    }
  }
}

class AmmoniteModuleType
    extends ModuleType[EmptyModuleBuilder]("AMMONITE_MODULE") {
  def createModuleBuilder() = new EmptyModuleBuilder()
  def getName = AmmoniteFileType.getName
  def getDescription = AmmoniteFileType.getDescription
  def getBigIcon = AmmoniteFileType.getIcon
  override def getNodeIcon(isOpened: Boolean) = getBigIcon
}

object AmmoniteModuleType {
  val instance: AmmoniteModuleType =
    Class
      .forName("ammonite.intellij.AmmoniteModuleType")
      .newInstance
      .asInstanceOf[AmmoniteModuleType]

  def unapply(m: Module): Option[Module] =
    if (ModuleType.get(m).isInstanceOf[AmmoniteModuleType]) Some(m)
    else None
}

object AmmoniteProjectSystem {
  val name = "Ammonite"
  val Id = new ProjectSystemId(name, name)
}
