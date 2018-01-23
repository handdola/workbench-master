package older.parser

/**
  * Created by Administrator on 2017-04-13.
  */
import org.scalajs.dom
import org.scalajs.dom.DOMParser
import org.scalajs.dom.{Element, Node}

import scalajs.js
import scalajs.js.annotation.JSExport
import scalatags.JsDom.all._
import dom.html
//import parser.DiaObject

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.runtime.Nothing$
//import scala.scalajs.js.typedarray.ArrayBuffer


@JSExport
object diaview extends {
  @JSExport
  def main(canvas: html.Canvas): Unit = {
    val ctx = canvas.getContext("2d")
      .asInstanceOf[dom.CanvasRenderingContext2D]
    import dom.ext._
    import scala.scalajs
    .concurrent
    .JSExecutionContext
    .Implicits
    .runNow

    lazy val output = div(
      height := "400px",
      overflowY := "scroll"
    ).render

    val url =
      "http://192.168.1.222:7070/" +
        "/ea/diagram/getDiagramInfoData.do?PROD_ID=fd93c0d3-de02-44b6-b5ce-9c85b45d01de"
    //GET /ea/diagram/getDiagramInfoData.do?PROD_ID=fd93c0d3-de02-44b6-b5ce-9c85b45d01de HTTP/1.1

    val brush =
      canvas.getContext("2d")
        .asInstanceOf[dom.CanvasRenderingContext2D]

    var objMap = new ArrayBuffer[mutable.HashMap[String, String]]()

    ///////////////////////////////////////////////
    Ajax.get(url).onSuccess { case xhr =>

      val parser = new DOMParser()
      val doc = parser.parseFromString(xhr.responseText, "application/xml")
      for (node <- doc.getElementsByTagName("DIA_OBJECT")) {
        //for(i <- 0 to node.childNodes.length-1)  println(i + ":" + node.childNodes(i).nodeName + ":" + node.childNodes(i).textContent)
        val diaobj = new mutable.HashMap[String, String]()
        for (child <- node.childNodes) diaobj.put(child.nodeName, child.textContent)
        objMap += diaobj
        //println(">>>>>"  + diaobj.obj_id +":" +  diaobj.obj_nm)
        // println(">>>>>" + i + ">>>" + node.childNodes(i))
      } //for
      for (diaobj <- objMap) {
        println("<<<<>>>>>>>")
        for (key <- diaobj.keys)
          println("         "  + key + ":" + diaobj(key))
        DrawObject(brush,diaobj)
      }
    } //onsuccess
  } //main


  def DrawObject(brush : dom.CanvasRenderingContext2D, obj : mutable.HashMap[String,String]): Unit = {
     //brush.font = obj("FONT_NM")
     //brush.textAlign = obj("NM_ALIGN")
     //brush.lineWidth = obj("LINE_THICK").toDouble
     var xy1 = obj("GEOMT_VERTEX").split("\\|")(0)
     var xy2 = obj("GEOMT_VERTEX").split("\\|")(2)
     var x1 = xy1.split(",")(0).toInt ;   var y1 = xy1.split(",")(1).toInt
     var x2 = xy2.split(",")(0).toInt ;   var y2 = xy2.split(",")(1).toInt
     var (w,h) = (x2-x1, y2-y1)
     //println(x1+" "+ y1+" "+x2+" "+y2)
     //brush.fillStyle(obj("FILL_COLOR"))
    //ctx.fillStyle = "#FF0000"
     var color = obj("FILL_COLOR").toInt.toHexString  //FFBBGGRR => #RRGGBB
     var colorStyle = "#" + color(2) + color(3) + color(4) + color(5) + color(6) + color(7)
     brush.fillStyle = colorStyle
     brush.fillRect(x1,y1,w,h)

     //draw text
    color = obj("FONT_COLOR").toInt.toHexString  //FFBBGGRR => #RRGGBB
    colorStyle = "#" + color(2) + color(3) + color(4) + color(5) + color(6) + color(7)
    brush.fillStyle = colorStyle
    brush.font = obj("FONT_SIZE") + "px " + obj("FONT_NM")
    println(brush.font)
    var tw = brush.measureText(obj("OBJ_NM")).width
    var fs = obj("FONT_SIZE").toInt
    brush.fillText(obj("OBJ_NM"),x1+(w-tw)/2,y1+(h-fs),w)


  }
} //diaview