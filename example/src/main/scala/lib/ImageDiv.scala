package lib

import org.scalajs.dom
import org.scalajs.dom.html

import scala.scalajs.js
import scalatags.JsDom.all._

/**
  * Created by Administrator on 2017-06-07.
  * enhanced to process image + video + audio
  */
class ImageDiv(imagediv: html.Div, init_pause:Boolean = true) {

  var index = 0
  var height = 100
  var pause = init_pause

  val images = imagediv.getElementsByClassName("awi-image")
  val length = images.length

  val imageClass = images(0).asInstanceOf[html.Div].className
  if (imageClass.contains("awi-media")) {  //audio or video
    val media_div = images(0).asInstanceOf[html.Video]
    val toolbar = div(cls := "kj-playbar",
      label(i(cls := "fa fa-play-circle w3-white"))).render
    media_div.appendChild(toolbar)
    media_div.onclick = (e:dom.Event) => {
      if (media_div.paused) {
        toolbar.style.display = "none"
        media_div.play()
      }
      else {
        media_div.pause()
        toolbar.style.display = "block"
      }
    }
    //media_div.play()

  } else {

    // Image Processing

    DisplayImage(index)

    val toolbar = div(cls := "kj-playbar",
      label(i(cls := "fa fa-play-circle w3-white"))).render
    imagediv.appendChild(toolbar)
    toolbar.style.display = "none"

    for (i <- 0 until images.length if images.length > 1) {
      val page = index + 1
      //pagemark.textContent = s"$page/$length"
      toolbar.style.display = "block"
      images(i).asInstanceOf[html.Image].onload = (e: dom.Event) => {
        //println("height",i,images(i).asInstanceOf[html.Image].height)
        /***** no more adjusting
        if (images(i).asInstanceOf[html.Image].height * 400 / images(i).asInstanceOf[html.Image].width > height) {
          height = images(i).asInstanceOf[html.Image].height * 400 / images(i).asInstanceOf[html.Image].width
          imagediv.style.height = height.toString
        }
        */
      }
      // toggle pause event
      images(i).asInstanceOf[html.Image].onclick = (e: dom.Event) => {
        pause = !pause
        println("pause", pause)
      }
    }


    js.timers.setInterval(2000) {  //auto play multi image
        //imagediv.setAttribute("data-current","0")
      if (!pause) {
        index += 1;
        toolbar.style.display = "none"
        if (index == images.length) index = 0
        DisplayImage(index)
        val page = index +1
        //pagemark.textContent = s"$page/$length"
      } else {
        if (length>1) toolbar.style.display = "block"
      }
    }
  } // end of image process


  def DisplayImage(next:Int): Unit = {
    val images = imagediv.getElementsByClassName("awi-image")
    for (i <- 0 until images.length) {
      val current = images(i).asInstanceOf[html.Image]
      if (i == next ) {
        current.className = current.className.replaceAll("w3-hide", "")
        //current.className += " w3-animate-fading"
        //***** no more adjusting : current.style.width = "400px"
      } else {
        current.className += " w3-hide"
        //current.className = current.className.replaceAll(" w3-display-middle", "")
      }
    }
  }

}
