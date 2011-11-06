package play.modules.scalate

import play.PlayPlugin
import play.mvc.{Scope, Http}
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
    
    override def render(args: java.util.Map[String, AnyRef]) = {
      internalRender(args)
    }
    override def internalRender(args: java.util.Map[String, AnyRef]) = {
      var map:Map[Symbol, Any] = convertMap(args);
      ScalateTemplate(template).render(map.toSeq :_*)
    }
    
    def convertMap(in: java.util.Map[String, AnyRef]): Map[Symbol, Any] = {
      import scala.collection.JavaConversions._
      
      Map.empty ++ (in map { pair => (Symbol(pair._1) -> pair._2.asInstanceOf[Any]) })
    }
  }
}