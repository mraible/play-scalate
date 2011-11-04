package play.modules.scalate

import play.Logger
import play.PlayPlugin
import play.exceptions.UnexpectedException
import play.mvc.{Scope, Http}
import play.mvc.Http.Header
import play.mvc.Http.Request
import play.mvc.Http.Response
import play.mvc.Scope.RenderArgs
import play.mvc.results.Result
import play.templates.Template
import play.vfs.VirtualFile

class Plugin extends PlayPlugin {
  
  var templateLoader:PlayPlugin = null
  
  override def loadTemplate(file: VirtualFile): play.templates.Template = {
      if (null == templateLoader) return new FancyTemplate(file)
      return templateLoader.loadTemplate(file);
  }
  
  case class FancyTemplate(file: VirtualFile) extends play.templates.Template with Scalate {
    var template:String = null
    
    def apply(file: VirtualFile) {
      this.name = file.getName
      this.source = file.getRealFile.getAbsolutePath
      this.template = Scope.RenderArgs.current().get("template").toString
      if (this.template == null) {
        this.template = Http.Request.current().action.replace(".", "/")
      }
    }
    
    override def compile(): Unit = {} 
    
    override def internalRender(args: java.util.Map[String, AnyRef]): String = {

      ScalateTemplate(template).render(args)
    }
  }
}