package cardnews

/**
  * Created by Administrator on 2017-04-24.
  */

/**
  * Created by Administrator on 2017-04-13.
  */
import lib._
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.{FileReader, MouseEvent, Window}
import org.scalajs.dom.{DOMParser, Event, html}

import scala.scalajs.js
import scala.scalajs.js.URIUtils
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom.all.{alt, i}
//import parser.DiaObject
import scalatags.JsDom.all._



//import com.karasiq.bootstrap.Bootstrap.default._
//import scalaTags.all._


import org.scalajs.dom.ext._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
//import scala.scalajs.js.typedarray.ArrayBuffer


@JSExport
object cnview extends {
  var objMap = new ArrayBuffer[mutable.HashMap[String, String]]()
  var lineMap = new ArrayBuffer[mutable.HashMap[String, String]]()
  var myUrl : String = ""
  var winRef : Window =null
  val docBase = "http://localhost:12345/target/scala-2.11/classes/"

  var pageNum = 0

  @JSExport
  def main(target: html.Div,main_stream : html.Div): Unit = {

    var paraMap = new mutable.HashMap[String, String]
    var objNm, ibjId : String = ""

    //ListCard(target,main_stream)
    ListPreview(target,main_stream)


  } //main


  def ListPreview(target:html.Div,main_stream : html.Div): Unit = {
    val query = """{
      "query": {
        "match_all": {}
      },
      "_source" : ["name","type","imgsrc","sumText"],
      "size": 10,
      "sort": [
      {
        "_timestamp": {
          "order": "desc"
        }
      }
      ]
    }"""


    val urlBase = "" //s"${GlobalVars.dataBase}/_search"
    //dom.window.alert("called Tab:"+objNm+":"+objId+":"+tab)
    ///////////////////////////////////////////////
    //Ajax.get(urlBase).onSuccess { case xhr => ...}

    //Ajax.get(urlBase).onSuccess { case xhr =>
    Ajax.post(urlBase,query).onSuccess { case xhr =>
      //println(xhr.responseText)
      val res = js.JSON.parse(xhr.responseText)
      val total = res.hits.hits.length.asInstanceOf[Int]
      val hits = res.hits.hits.asInstanceOf[js.Array[js.Dynamic]]

      for (index <- 0 until total) {

        println(s"****************$index****************")
        println(index, hits(index)._id)
        val name = hits(index)._source.name
        val cardtype = hits(index)._source.`type`
        val sumText = URIUtils.decodeURIComponent(dom.window.atob(hits(index)._source.sumText.toString))
        val imgsrc = URIUtils.decodeURIComponent(dom.window.atob(hits(index)._source.imgsrc.toString))

        println("load:",name,cardtype)
        if (name.equals("My First Card") || (name.equals("QuizNews") && cardtype.equals("default")) ) {
          LoadCard(target,main_stream, hits(index)._id.toString)
        } else if (name.equals("QuizNews")) {
          val idstr = s"${hits(index)._id}"
          val cardnum = s"cardnum-$idstr-$index"

          val optiondiv = div(id := idstr, cls := "w3-cell-row").render
          val onecard = div(cls := cardnum).render
          val bodies = div(cls:="kj-container").render

          val summary = div(id := s"sum$idstr", cls := "summary",
            div(cls := "w3-padding", sumText, style := "height:100px;overflow:hidden"),
            img(cls := "awi-image ", src := imgsrc, alt := "Avatar")).render

          optiondiv.appendChild(summary)
          onecard.appendChild(div(cls := "w3-cell-row",optiondiv).render)

          bodies.appendChild(onecard)

          val NewPlayCard = div (cls:=s"kj-card-$idstr kj-preview-$idstr kj-card-2 w3-card-2 w3-center w3-round w3-white w3-margin-top",
            div(cls:="kj-container"),bodies).render

          main_stream.appendChild(NewPlayCard)  //dummy

          MultiCardAction(target, main_stream, idstr, optiondiv)
        }
      } //for each card
    } // onsuccess
  }


  def LoadCard(target:html.Div,main_stream : html.Div,cardNum:String)  {

    import upickle.default._

    val urlBase = "" //s"${GlobalVars.dataBase}/$cardNum"

    Ajax.get(urlBase).onSuccess { case xhr =>

      val res = js.JSON.parse(xhr.responseText)

      val name = res._source.name
      val cardtype = res._source.`type`
      val idstr = s"${res._id}"
      val iddiv = div(id := idstr, cls := "w3-cell-row").render
      println("reading",idstr,cardtype)

      if (name.equals("QuizNews") && cardtype.equals("default")) {
        val json_data = dom.window.atob(res._source.resume.asInstanceOf[String])
      //println("json_data",json_data)
      val (cardlen, cards) = read [(Int,ArrayBuffer[(Int,ArrayBuffer[String],ArrayBuffer[String])])](json_data)

        for (index <- 0 until cardlen) {
          val (length,saved_area1,saved_area2) = cards(index)
          println("My card", length)
          for (i <- 0 until length) {
            if (saved_area2(i).contains("awi-text-page")) {
              println("text-page")
              val page_div = div(cls := "w3-cell-row", div(cls := saved_area2(i))).render
              //println("12")
              page_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i)).replaceAll("awi-border", "")
              //println("13")
              iddiv.appendChild(page_div)
              //println("14")
            } else if (saved_area2(i).contains("awi-image-page")) {
              println("image-page")
              val image_div = div(cls := "w3-cell-row", div(cls := saved_area2(i))).render
              image_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
              new ImageDiv(image_div)
              iddiv.appendChild(image_div)
              //println("22")
            }
          }
          OneCardAction(target, idstr, iddiv)
        }
      } else if (name.equals("QuizNews") && !js.isUndefined(res._source.resume)) {
        //println("quiznews")
        val json_data = dom.window.atob(res._source.resume.asInstanceOf[String])
        //println("json_data",json_data)
        //("multicard news processing")
        if (js.isUndefined(cardtype)) {
          println("play default")
          val qcard = new QuizDiv(main_stream, json_data, idstr)
          MultiCardAction(target, main_stream, idstr, iddiv)
        } else if (cardtype.equals("typeaction")) {
          println("play typeaction")
          val qcard = new QuizDiv(main_stream, json_data, idstr)
          MultiCardAction(target,main_stream, idstr,iddiv)
        } else if (cardtype.equals("typetest")) {
          println("play typetest")
          val qcard = new TypeTest(main_stream, json_data, idstr)
          MultiCardAction(target,main_stream, idstr, iddiv)
        }
      }
        // loop for next post

        //PostProcessing
        println("6image size change")
        //img size
        val images = target.getElementsByTagName("img")
        for (tool <- images) {
          if (tool.asInstanceOf[html.Div].className != null && !tool.asInstanceOf[html.Div].className.contains("awi-option-image"))
          tool.asInstanceOf[html.Div].className += " awi-image"
        }
    } //onSuccess

  }


  def OneCardAction(target:html.Div,idstr:String,iddiv:html.Div): Unit =
  {
    //println("33")
    val button1 = button(cls := "w3-button w3-theme-d1 w3-margin-bottom w3-border", i(cls := "fa fa-thumbs-up"), "Like").render
    val button2 = button(cls := "w3-button w3-theme-d1 w3-margin-bottom w3-border", i(cls := "fa fa-comment"), " Comment").render
    val button3 = button(cls := "w3-button w3-theme-d1 w3-margin-bottom w3-border w3-right", i(cls := "fa fa-edit"), " Edit").render
    //val button4 = button(id:="ShareIt", cls:="w3-button w3-theme w3-border w3-right", i(cls:="fa fa-facebook")).render

    val baseURL = "%s".format(dom.window.location.toString.split('?')(0)).replace(dom.window.location.pathname, "/post")
    //println("baseURL",baseURL)
    val postURL = s"$baseURL$idstr"

    println("34")

    //link share ===============================================
    val linkButton = div(cls := "w3-button w3-text-theme w3-margin-bottom w3-border", "Link").render
    linkButton.onclick = (e: dom.Event) => {
      dom.window.document.getElementById("ShareIt").asInstanceOf[html.Div].style.display = "block"
      dom.window.document.getElementById("ShareURL").asInstanceOf[html.Input].value = s"$postURL"
    }
    //link share end ========================================

    //println("35")

    //println("imgsrc",imgsrc)
    //println("sumText",sumText)
    dom.window.document.getElementById("ShareURL").asInstanceOf[html.Input].value = s"$postURL"
    val facebutton = div(cls:="w3-button w3-text-theme w3-margin-bottom w3-border","Facebook").render
    facebutton.onclick = (e:dom.Event) => {
      val optstr =
        s"""{ "method" : "feed",
           |  "app_id" : "1719845414979964",
           |  "link" : "$postURL"
           |}""".stripMargin

      val options = js.JSON.parse(optstr)
      //println(optstr)

      FB.ui(options,(res:js.Any)=>{

      })
    }
    //facebook share end ===============================================
    //println("45")

      button3.onclick =  (e:dom.Event) => {dom.window.location.href = s"home3.html?a=$idstr"}
      println("46")
      val cardnews = div(cls:="kj-container w3-card-2 w3-white w3-round",br,iddiv,div(cls:="w3-container w3-margin-top",linkButton, facebutton,button3)).render
      target.insertBefore(cardnews,target.firstChild)
      println("47")

  }

  def MultiCardAction(target:html.Div, main_stream:html.Div,idstr:String,iddiv:html.Div): Unit =
  {
    //println("33")
    val button1 = button(cls := "w3-button w3-theme-d1 w3-margin-bottom w3-border", i(cls := "fa fa-thumbs-up"), "Like").render
    val button2 = button(cls := "w3-button w3-theme-d1 w3-margin-bottom w3-border", i(cls := "fa fa-comment"), " Comment").render
    val button3 = button(cls := "w3-button w3-theme-d1 w3-margin-bottom w3-border w3-right", i(cls := "fa fa-edit"), " Edit").render
    val button4 = button(cls := "w3-button w3-theme-d1 w3-margin-bottom w3-border w3-right", i(cls := "fa fa-edit"), " Play").render
    //val button4 = button(id:="ShareIt", cls:="w3-button w3-theme w3-border w3-right", i(cls:="fa fa-facebook")).render

    val baseURL = "%s".format(dom.window.location.toString.split('?')(0)).replace(dom.window.location.pathname, "/post")
    //println("baseURL",baseURL)
    val postURL = s"$baseURL$idstr"

    println("34")

    //link share ===============================================
    val linkButton = div(cls := "w3-button w3-text-theme w3-margin-bottom w3-border", "Link").render
    linkButton.onclick = (e: dom.Event) => {
      dom.window.document.getElementById("ShareIt").asInstanceOf[html.Div].style.display = "block"
      dom.window.document.getElementById("ShareURL").asInstanceOf[html.Input].value = s"$postURL"
    }
    //link share end ========================================

    //println("35")


    //println("imgsrc",imgsrc)
    //println("sumText",sumText)
    dom.window.document.getElementById("ShareURL").asInstanceOf[html.Input].value = s"$postURL"
    val facebutton = div(cls:="w3-button w3-text-theme w3-margin-bottom w3-border","Facebook").render
    facebutton.onclick = (e:dom.Event) => {
      val optstr =
        s"""{ "method" : "feed",
           |  "app_id" : "1719845414979964",
           |  "link" : "$postURL"
           |}""".stripMargin

      val options = js.JSON.parse(optstr)
      //println(optstr)

      FB.ui(options,(res:js.Any)=>{

      })
    }
    //facebook share end ===============================================
    //println("45")


    button3.onclick =  (e:dom.Event) => {dom.window.location.href = s"quiznews.html?a=$idstr"}
    button4.onclick =  (e:dom.Event) => {
      //keep dummy position for
      if (main_stream.getElementsByClassName(s"kj-preview-$idstr").length>0)
        main_stream.removeChild(main_stream.getElementsByClassName(s"kj-preview-$idstr")(0))
      if (main_stream.getElementsByClassName(s"kj-share-$idstr").length>0)
        main_stream.removeChild(main_stream.getElementsByClassName(s"kj-share-$idstr")(0))
      LoadCard(target,main_stream,idstr)

    }
    //println("46")
    val cardnews = div(cls:=s"kj-share-$idstr kj-card-2 w3-card-2 w3-center w3-white",br,
      iddiv,div(cls:="w3-container",linkButton, facebutton,button3,button4)).render

    val carddiv = main_stream.getElementsByClassName(s"kj-card-$idstr")(0).asInstanceOf[html.Div]
    main_stream.insertBefore(cardnews,carddiv.nextSibling)
    //println("47")

  }


}
