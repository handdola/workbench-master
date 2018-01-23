package older

import lib.Quill

import scala.scalajs.js

/**
  * Created by Administrator on 2017-05-26.
  */
class test {


  def CreateQuill(cont : js.Any, toolbarid:String) : Quill = {

    //val optstr = """{theme: snow, modules: {toolbar: toolbar}}"""
    val optstr =
      """{ "theme" : "bubble",
        |  "modules" : {
        |     "toolbar" : {
        |        "font" : ["Dotum","Gulim"]
        |      }
        |  }
        |}""".stripMargin

    val optstr2 = """ {"modules" : { "toolbar" : [
                    | {"container" : "#toolbar"},
                    | ["image", "code-block"],
                    | [{ "list": "ordered"}, { "list": "bullet" }],
                    | [{ "size": ["small", false, "large", "huge"] }],
                    | [{ "color": [] }, { "background": [] }],
                    | [{ "align": [] }],
                    | ["clean"]
                    | ]} ,
                    | "theme" : "snow"
                    | }""".stripMargin

    val optstr3 = """ {"modules" :  """ +
      """{ "toolbar" :" """ +
      toolbarid + """"}}"""

    println(optstr3)
    val options = js.JSON.parse(optstr3)
    val editor = new Quill(cont,options)
    return editor
  }

}
