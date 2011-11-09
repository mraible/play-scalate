package play.modules.scalate

import play.PlayPlugin
import play.vfs.VirtualFile

class Plugin extends PlayPlugin {
  
  var templateLoader:PlayPlugin = null
  
  override def loadTemplate(file: VirtualFile): play.templates.Template = {
      if (templateLoader == null) {
        return null
      } 
      return templateLoader.loadTemplate(file);
  }
}