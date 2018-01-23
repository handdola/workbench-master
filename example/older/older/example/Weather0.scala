package example

import org.scalajs.dom
import org.scalajs.dom.{Node, Element}
import scalajs.js
import scalajs.js.annotation.JSExport
import scalatags.JsDom.all._
import dom.html
@JSExport
object Weather0 extends{
  @JSExport
  def main(target: html.Div) = {
    val xhr = new dom.XMLHttpRequest()
    xhr.open("GET",
      "http://192.168.1.222:7070/" +
        "/ea/diagram/getDiagramInfoData.do?PROD_ID=fd93c0d3-de02-44b6-b5ce-9c85b45d01de"
    )
    xhr.onload = (e: dom.Event) => {
      if (xhr.status == 200) {
        target.appendChild(
          pre(xhr.responseText).render
        )
      }
    }
    xhr.send()
  }
}