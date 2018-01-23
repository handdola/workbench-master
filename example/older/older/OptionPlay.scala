package lib

import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.NodeList

import scala.scalajs.js
//import scala.scalajs.js.typedarray.ArrayBuffer
import scalatags.JsDom.all._

import scala.collection.mutable.ArrayBuffer


/** Merged to QuizDiv
  * Created by Administrator on 2017-06-07.
class OptionPlay(main_stream: html.Div, optiondiv: html.Div,onecard: html.Div, init_pause:Boolean = true) {

  var index = 0
  var height = 100
  var pause = init_pause

  var opt_data = new ArrayBuffer[String]()
  var opt_action = new ArrayBuffer[String]()
  //var opt_imgdata = new ArrayBuffer[String]()

  // save otion data & image
  val options = optiondiv.getElementsByClassName("kj-option-item")
  println("options",options.length)
  val length = options.length
  for (i <- 0 until options.length) {
       opt_data += options(i).asInstanceOf[html.Div].getElementsByClassName("kj-option-val")(0).asInstanceOf[html.Input].getAttribute("data-hold-value")
       opt_action += options(i).asInstanceOf[html.Div].getElementsByClassName("kj-option-action")(0).asInstanceOf[html.Input].getAttribute("action-hold-value")
       //opt_imgdata += options(i).asInstanceOf[html.Div].getElementsByClassName("kj-option-img")(0).asInstanceOf[html.Image].src
       //opt_imgdata += options(i).asInstanceOf[html.Div].getElementsByClassName("awi-option-image")(0).asInstanceOf[html.Image].src
       //  println("imglength",images.length)
  }
  //destroy all child
  while (optiondiv.firstChild != null) optiondiv.removeChild(optiondiv.firstChild)

  //create new option buttons
  for (i <- 0 until opt_data.length if opt_data.length > 0) {
    val page = index +1
    val Num = i + 1
    val optBtn = button(cls:="kj-option-btn w3-button w3-round w3-block w3-teal w3-margin-top",opt_data(i)).render
    optiondiv.appendChild(optBtn)

    // show result image when option btn clicked //
    optBtn.onclick = (e:dom.Event) => {
      println("option clicked",onecard.offsetTop)
      optiondiv.setAttribute("data-response",opt_data(i))

      /* change image
      val img = onecard.getElementsByClassName("awi-image")(0).asInstanceOf[html.Image]

      //print("img",img.src)

      if (!js.isUndefined(img)) {
        println("change img")
        dom.window.document.getElementsByTagName("body")(0).asInstanceOf[html.Body].scrollTop = onecard.offsetTop - 40
        img.src = opt_imgdata(i)
        while (optiondiv.firstChild != null) optiondiv.removeChild(optiondiv.firstChild)
      }
      */

      val nextSubmitBtn = onecard.getElementsByClassName("kj-option-cell")
      if (nextSubmitBtn(0)!=null) {
        println("found nextBtn",nextSubmitBtn(0))
        nextSubmitBtn(0).asInstanceOf[html.Div].className = nextSubmitBtn(0).asInstanceOf[html.Div].className.replaceAll("w3-hide","")
      }


    }
  }


}

===========================================================================  */
