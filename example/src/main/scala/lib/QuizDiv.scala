package lib

//import older.quiznews.{ImageLoad, OptionLoad, PageLoad}
import org.scalajs.dom
import org.scalajs.dom.ext._
import org.scalajs.dom.html
import upickle.default.read

import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js
import scala.scalajs.js.URIUtils
import scalatags.JsDom.all._
import lib.piwik

/**
  * Created by Administrator on 2017-06-07.
  * Create Muticard from json-data
  * Show 1st multicard
  * Show next card when clicked next buttton
  *
  */
class QuizDiv(main_stream: html.Div, json_data:String,idstr:String,ctype: String, real:Boolean=true) {

  var cur_card = 0 //do not use. it's wrong
  var num_options = 0
  val cardtype = ctype

  var (cardlen, cards) = read [(Int,ArrayBuffer[(Int,ArrayBuffer[String],ArrayBuffer[String])])](json_data)
  //println("carddata",cardlen,cards)

  //for (cardnum <- 0 until cardlen) {
    NewCard(cur_card)
  //}
  ShowCard(cur_card)  //initial card
  //println("6image size change")
  //img size

  def NewCard(index : Int = 0): Unit = {
    val cardnum = s"cardnum-$idstr-$index"
    val onecard = div(cls := cardnum).render
    num_options = 0

println("NewCard ",index, cardnum,idstr)
    if (real) piwik.piwik_event_push("Play",cardnum,idstr)


    val (length,saved_area1,saved_area2) = cards(index)
    //println("carddata",index,length)
    for (i <- 0 until length) {
      //println(length,saved_area1(i))
      //println(length,saved_area2(i))
      if (saved_area2(i).contains("awi-text-page")) {
        //println("awi-text-page")
        val page_div = div(cls := saved_area2(i)).render
        page_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i)).replaceAll("w3-border", "").replaceAll("awi-border","")
        onecard.appendChild(div(cls := "w3-cell-row", page_div).render)
      } else if (saved_area2(i).contains("awi-image-page")) {
        //println("awi-image-page")
        // check old data
        if (saved_area2(i).contains("w3-left") && !saved_area2(i).contains("lyAlign")) {
          saved_area2(i) = saved_area2(i).replaceAll("w3-left", "")
        }
        val image_div = div(cls := saved_area2(i)).render
        image_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
        new ImageDiv(image_div)
        image_div.setAttribute("async","false")
        onecard.appendChild(div(cls := "w3-cell-row", image_div).render)
      } else if (saved_area2(i).contains("awi-flip-page")) {
        //println("awi-option-page")
        val option_div = div(cls := saved_area2(i)).render
        option_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
        PlayFlip(option_div, onecard)
        //onecard.appendChild(div(cls := "w3-cell-row", option_div).render)
        //create options
        // And check
      } else if (saved_area2(i).contains("awi-option-page")) {
        //println("awi-option-page")
        val option_div = div(cls := saved_area2(i)).render
        option_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
        onecard.appendChild(div(cls := "w3-cell-row", option_div).render)

        //create options
        PlayOption(option_div, onecard)
        // And check
        num_options = option_div.getElementsByClassName("kj-option-btn").length
        println("num_options",num_options)
        println("next",cur_card,cards.length)
      }
    }


    /*------------------check Play Button
    if (cur_card < (cards.length-1)) {

      println("next",cur_card,cards.length)

      val nextBtn = div(cls := "kj-option-cell w3-cell-row w3-hide", button(cls := "kj-option-next w3-button w3-block w3-blue w3-margin", "Next")).render
      onecard.appendChild(nextBtn)
      if (num_options.length == 0) nextBtn.className = nextBtn.className.replaceAll("w3-hide","")


      nextBtn.onclick = (e: dom.Event) => {
        dom.window.document.getElementsByTagName("body")(0).asInstanceOf[html.Body].scrollTop = onecard.offsetTop - 40
        NextCard()
        ShowCard(cur_card)
      }
    } else {
      println("submit",cur_card,cards.length)
      val submitBtn = div(cls := "kj-option-cell w3-cell-row w3-hide", button(cls := "kj-option-next w3-button w3-block w3-blue w3-margin", "Submit")).render
      onecard.appendChild(submitBtn)
      submitBtn.className = submitBtn.className.replaceAll("w3-hide","")
      submitBtn.onclick = (e: dom.Event) => {
        dom.window.alert("Thank you!.")
        ResetCard()
      }
    }
    ----------------------------------*/

    //println("appendChild")
    val bodies = div(cls:="kj-container").render

    val newcard = div (cls:=s"kj-card-$idstr kj-cardnum-$index w3-round w3-white w3-margin-top",
      div(cls:="kj-container"),bodies).render

    // insert
    val sharedivs = main_stream.getElementsByClassName(s"kj-share-$idstr")
    if (sharedivs != null && !js.isUndefined(sharedivs(0))) {
      println("insert before")
      main_stream.insertBefore(newcard, sharedivs(0))

    } else {
      println("insert append")
      main_stream.appendChild(newcard)
    }


    bodies.appendChild(onecard)

    //check new position
    dom.window.document.getElementsByTagName("body")(0).asInstanceOf[html.Body].scrollTop = main_stream.offsetTop - 40

    CheckPlayBtn(num_options, bodies, onecard)
  }  // end of Newcard

  //Only adjust image
  def ShowCard (index:Int): Unit = {
    if (index < cardlen) {
      val cards = main_stream.getElementsByClassName(s"kj-card-$idstr")

      //post processing
      val images = main_stream.getElementsByTagName("img")
      for (tool <- images) {
        if (tool.asInstanceOf[html.Div].className != null && !tool.asInstanceOf[html.Div].className.contains("awi-option-image"))
          tool.asInstanceOf[html.Div].className += " awi-image"
      }

    }
  }

  //delete old card and create next card

  def NextCard(incPage:String = "1") = {
    println("next click",cur_card)
    var inc = 1
    if (incPage !=null && !js.isUndefined(incPage)) inc = incPage.toInt
    if ((cur_card + inc) < cards.length) {
      DelCard(cur_card)
      cur_card += inc
      NewCard(cur_card)
      ShowCard(cur_card)
    }
  }

  def ResetCard() = { //bugs
    println("reset click",cur_card)
      DelCard(cur_card)
      cur_card = 0
      NewCard(cur_card)
      ShowCard(cur_card)
  }


  def DelCard(index:Int): Unit ={
    val card = main_stream.getElementsByClassName(s"kj-card-$idstr")(0)
    main_stream.removeChild(card)
  }


  def PlayOption(optiondiv: html.Div,onecard: html.Div): Unit = {
    var index = 0
    //var height = 100
    //var pause = init_pause

    var opt_data = new ArrayBuffer[String]()
    var opt_class = new ArrayBuffer[String]()
    var opt_action = new ArrayBuffer[String]()
    //var opt_imgdata = new ArrayBuffer[String]()

    // save otion data & image
    val options = optiondiv.getElementsByClassName("kj-option-item")
    println("options",options.length)
    val length = options.length
    for (i <- 0 until options.length) {
      opt_data += options(i).asInstanceOf[html.Div].getElementsByClassName("kj-option-val")(0).asInstanceOf[html.Input].getAttribute("data-hold-value")
      val styleAttr = dataAttr(options(i).asInstanceOf[html.Div].getElementsByClassName("kj-option-val")(0).asInstanceOf[html.Input])
      if (styleAttr.isEmpty) opt_class += " w3-teal"
      else opt_class += styleAttr
      if (!js.isUndefined(options(i).asInstanceOf[html.Div].getElementsByClassName("kj-option-action")(0)))
        opt_action += options(i).asInstanceOf[html.Div].getElementsByClassName("kj-option-action")(0).asInstanceOf[html.Select].getAttribute("data-action-value")
      else
        opt_action += "1"
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
      val optStyle = opt_class(i)

      val optBtn = button(cls:=s"kj-option-btn w3-button w3-block w3-margin-top $optStyle",opt_data(i)).render
      optiondiv.appendChild(optBtn)

      // show result image when option btn clicked //
      optBtn.onclick = (e:dom.Event) => {
        println("option clicked",onecard.offsetTop)
        optiondiv.setAttribute("data-response",opt_data(i))
        NextCard(opt_action(i))

      }
    }

  }

  def PlayFlip(optiondiv: html.Div,onecard: html.Div): Unit = {
    var index = 0
    //var height = 100
    //var pause = init_pause

    var opt_data = new ArrayBuffer[String]()
    var opt_class = new ArrayBuffer[String]()
    var opt_image = new ArrayBuffer[String]()
    //var opt_imgdata = new ArrayBuffer[String]()

    // save otion data & image
    val options = optiondiv.getElementsByClassName("kj-flip-item")
    println("options",options.length)
    val length = options.length
    for (i <- 0 until options.length) {
      opt_data += options(i).asInstanceOf[html.Div].getElementsByClassName("kj-flip-val")(0).asInstanceOf[html.Input].getAttribute("data-hold-value")
      println("start attr check")
      //if (i==0) {
      val styleAttr = dataAttr(options(i).asInstanceOf[html.Div].getElementsByClassName("kj-flip-val")(0).asInstanceOf[html.Input])
      if (styleAttr.isEmpty) opt_class += " w3-teal"
      else opt_class += styleAttr
      //}
      if (!js.isUndefined(options(i).asInstanceOf[html.Div].getElementsByClassName("awi-flip-image")(0)))
        opt_image += options(i).asInstanceOf[html.Div].getElementsByClassName("awi-flip-image")(0).asInstanceOf[html.Image].src
      else
        opt_image += "./images/story_small.jpg"
      //opt_imgdata += options(i).asInstanceOf[html.Div].getElementsByClassName("kj-option-img")(0).asInstanceOf[html.Image].src
      //opt_imgdata += options(i).asInstanceOf[html.Div].getElementsByClassName("awi-option-image")(0).asInstanceOf[html.Image].src
      //  println("imglength",images.length)
    }
    //destroy all child
    //while (optiondiv.firstChild != null) optiondiv.removeChild(optiondiv.firstChild)
    val flipdiv = optiondiv.cloneNode(false) //copy w/o child item

    val explain = div(cls:="kj-flip-explain").render
    val explainEditor = optiondiv.getElementsByClassName("ql-editor")
    var explainText : String = ""
    if (explainEditor.length > 0) explainText = explainEditor(0).asInstanceOf[html.Div].innerHTML
    println(explain.innerHTML)
    //create new option buttons
    for (k <- 0 until opt_data.length if opt_data.length > 0) {
      val page = index +1
      val Num = k + 1
      val optStyle = opt_class(k)
      val optBtn = button(cls:=s"kj-option-btn w3-button w3-block w3-margin-top $optStyle",opt_data(k)).render
      val resetBtn = div(cls:="kj-reset-btn w3-button w3-right",label(i( cls:= "fa fa-refresh w3-large"),cls:=""," reset")).render
      val optImage = div(cls:="w3-animate-top",
        img(cls:="awi-image", src:=opt_image(k),alt:="Image"),
        explain
      ).render

      flipdiv.appendChild(optBtn)

      // show result image when option btn clicked //
      optBtn.onclick = (e:dom.Event) => {
        while (flipdiv.firstChild != null)  flipdiv.removeChild(flipdiv.firstChild)
        flipdiv.appendChild(optImage)
        flipdiv.appendChild(explain)
        explain.innerHTML = explainText
        explain.appendChild(resetBtn)
        //flipdiv.appendChild(resetBtn)
      }
      resetBtn.onclick = (e:dom.Event) => {
        while (flipdiv.firstChild != null)  flipdiv.removeChild(flipdiv.firstChild)
        PlayFlip(optiondiv,onecard)
      }
    }
    onecard.appendChild(flipdiv)

  }



  def CheckPlayBtn(num_options:Int, bodies: html.Div, onecard: html.Div) = {

    //val num_options = optiondiv.getElementsByClassName("kj-option-btn")
    println("check num_options",num_options)

    if (cur_card < (cardlen-1)) {

      println("next",cur_card,cardlen)

      val nextBtn = div(cls := "kj-option-cell w3-cell-row w3-hide", button(cls := "kj-option-next w3-button w3-block w3-blue w3-margin", "Next")).render
      bodies.appendChild(nextBtn)
      if (num_options == 0) nextBtn.className = nextBtn.className.replaceAll("w3-hide","")


      nextBtn.onclick = (e: dom.Event) => {
        dom.window.document.getElementsByTagName("body")(0).asInstanceOf[html.Body].scrollTop = onecard.offsetTop - 40
        NextCard()
        ShowCard(cur_card)
      }
    } else if (cardtype.equals("action")) {
      println("submit",cur_card,cardlen)
      val submitBtn = div(cls := "kj-option-cell w3-cell-row", button(cls := "kj-option-next w3-button w3-block w3-blue w3-margin", "Submit")).render
      bodies.appendChild(submitBtn)
      submitBtn.onclick = (e: dom.Event) => {
        dom.window.alert("Thank you!.")
        if (real) piwik.piwik_event_push("Play","Submit",idstr)
        ResetCard()
      }
    }

  }

  def dataAttr (options : html.Element) : String = {
    var styleAttr = ""
    for (attr <- options.attributes) {
      println("attr class",attr._1.toString ,attr._2.value)
      if (attr._1.contains("data-style")) styleAttr += " " + attr._2.value
    }
    return styleAttr
  }



}
