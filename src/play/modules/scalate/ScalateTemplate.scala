package play.modules.scalate

import play.Play
import play.data.validation.Validation
import play.mvc.{Http, Scope}

object ScalateTemplate {

  import org.fusesource.scalate._
  import org.fusesource.scalate.util._
  import org.fusesource.scalate.layout._

  val scalateType = "." + Play.configuration.get("scalate")

  lazy val scalateEngine = {
    val engine = new TemplateEngine
    engine.resourceLoader = new FileResourceLoader(Some(Play.getFile("/app/views")))
    engine.classpath = Play.getFile("/tmp/classes").getAbsolutePath
    engine.workingDirectory = Play.getFile("tmp")
    engine.combinedClassPath = true
    engine.classLoader = Play.classloader
    engine.layoutStrategy = new DefaultLayoutStrategy(engine,
      Play.getFile("/app/views/layouts/default" + scalateType).getAbsolutePath)
    engine
  }

  case class Template(name: String) {

    def render(args: (Symbol, Any)*) = {
      val argsMap = populateRenderArgs(args: _*)

      scalateEngine.layout(name + scalateType, argsMap)
    }
    
    def render(args: java.util.Map[String, AnyRef]) = {
      import scala.collection.JavaConverters._
      var argsAsScala = Map.empty[String,Any] ++ args.asScala.map( e => (e.toString, e) )
      
      scalateEngine.layout(name + scalateType, argsAsScala)
    }
  }

  def apply(template: String) = Template(template)

  import scala.collection.JavaConversions._

  def populateRenderArgs(args: (Symbol, Any)*): Map[String, Any] = {
    val renderArgs = Scope.RenderArgs.current();

    args.foreach {
      o =>
        renderArgs.put(o._1.name, o._2)
    }

    renderArgs.put("session", Scope.Session.current())
    renderArgs.put("request", Http.Request.current())
    renderArgs.put("flash", Scope.Flash.current())
    renderArgs.put("params", Scope.Params.current())
    renderArgs.put("errors", validationErrors)
    renderArgs.put("config", Play.configuration)

    // CSS class to add to body
    renderArgs.put("bodyClass", Http.Request.current().action.replace(".", " ").toLowerCase)
    renderArgs.data.toMap
  }

  // --- ROUTERS
  def action(action: => Any) = {
    new play.mvc.results.ScalaAction(action).actionDefinition.url
  }

  implicit def validationErrors:Map[String,play.data.validation.Error] = {
    import scala.collection.JavaConverters._
    Map.empty[String,play.data.validation.Error] ++ Validation.errors.asScala.map( e => (e.getKey, e) )
  }

  def asset(path:String) = play.mvc.Router.reverse(play.Play.getVirtualFile(path))
}