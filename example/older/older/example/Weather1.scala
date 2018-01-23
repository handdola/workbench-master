package example

import org.scalajs.dom
import org.scalajs.dom.DOMParser
import org.scalajs.dom.{Element, Node}

import scalajs.js
import scalajs.js.annotation.JSExport
import scalatags.JsDom.all._
import dom.html

import scala.runtime.Nothing$
@JSExport
object Weather1 extends{
  @JSExport
  def main(target: html.Div) = {
    import dom.ext._
    import scala.scalajs
    .concurrent
    .JSExecutionContext
    .Implicits
    .runNow

    val url =
    "http://192.168.1.222:7070/" +
      "/ea/diagram/getDiagramInfoData.do?PROD_ID=fd93c0d3-de02-44b6-b5ce-9c85b45d01de"
    //GET /ea/diagram/getDiagramInfoData.do?PROD_ID=fd93c0d3-de02-44b6-b5ce-9c85b45d01de HTTP/1.1

    ///////////////////////////////////////////////
    Ajax.get(url).onSuccess{ case xhr =>

      val parser = new DOMParser()
      val doc = parser.parseFromString(xhr.responseText, "application/xml")
      for (node <- doc.getElementsByTagName("DIA_OBJECT")) {
        println(node.nodeName + ">" + node.nodeType )
        for (child <- node.childNodes) {
          println(">>>>>" + child.nodeName + ">" + child.textContent)
        }
      }
      for (node <- doc.getElementsByTagName("DIA_NOTE")) {
        println(node.nodeName + ">" + node.nodeType )
        for (child <- node.childNodes)
          println(">>>>>" + child.nodeName + ">" + child.textContent)
      }
      for (node <- doc.getElementsByTagName("DIA_PROPERTY")) {
        println(node.nodeName + ">" + node.nodeType )
        for (child <- node.childNodes)
          println(">>>>>" + child.nodeName + ">" + child.textContent)
      }
      target.appendChild(pre(xhr.responseText).render)
    }
  }


}