package older

/**
  * Created by Administrator on 2017-04-24.
  */

/**
  * Created by Administrator on 2017-04-13.
  */
import lib._
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.Window
import org.scalajs.dom.{Event, html}

import scala.scalajs.js
import scala.scalajs.js.URIUtils
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom.all.{alt, i}
//import parser.DiaObject
import scala.util._
import scalatags.JsDom.all._



//import com.karasiq.bootstrap.Bootstrap.default._
//import scalaTags.all._


import org.scalajs.dom.ext._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
//import scala.scalajs.js.typedarray.ArrayBuffer


@JSExport
object cnpost extends {
  var objMap = new ArrayBuffer[mutable.HashMap[String, String]]()
  var lineMap = new ArrayBuffer[mutable.HashMap[String, String]]()
  var myUrl: String = ""
  var winRef: Window = null
  var viewmode = "post"

  var pageNum = 0

  @JSExport
  def main(view : String, target: html.Div , main_stream : html.Div): Unit = {


    var paraMap = new mutable.HashMap[String, String]
    var objNm, ibjId: String = ""
    var cardNum = ""
    viewmode = view

    println(dom.window.location.search)
    println("location", dom.window.location)
    println("pathname", dom.window.location.pathname)
    println("viewmode", viewmode)
    val pathname = dom.window.location.pathname


    val mainidstr = pathname.substring(pathname.lastIndexOf('/') + 1).replace("post", "")


    //PostCard(target,main_stream, mainidstr)
    PostPreviewCard(target,main_stream, mainidstr)


  } //main


  def PostPreviewCard(target: html.Div,main_stream : html.Div, mainidstr: String) {

    var mainnews: html.Div = null

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

    val urlBase = ""//s"${GlobalVars.dataBase}/_search"
    //dom.window.alert("called Tab:"+objNm+":"+objId+":"+tab)
    ///////////////////////////////////////////////
    //Ajax.get(urlBase).onSuccess { case xhr => ...}

    //Ajax.get(urlBase).onSuccess { case xhr =>
    dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "block"
    val request = Ajax.post(urlBase, query)
      request.onComplete {
        case Success(xhr) =>
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


            val idstr = s"${hits(index)._id}"
            val iddiv = div(id := idstr, cls := "w3-cell-row").render

            println("preview**********", idstr, cardtype)

            if (name.equals("My First Card") || (name.equals("QuizNews") && cardtype.equals("default"))) {
              PostCard(target, main_stream, hits(index)._id.toString)
            } else if (name.equals("QuizNews")) {
              MultiCardPreview(target, main_stream, idstr, mainidstr, sumText, imgsrc.toString)
            }

          }
          dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"

        case Failure(e) =>
          println(e.toString)
          dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"
    }
  }


  def PostCard(target: html.Div, main_stream : html.Div,cardNum: String) {
    import upickle.default._

    var mainnews: html.Div = null

    val urlBase = ""//s"${GlobalVars.dataBase}/$cardNum"

    dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "block"
    val request = Ajax.get(urlBase)
    request.onComplete {
      case Success(xhr) =>
      //println(xhr.responseText)
      val res = js.JSON.parse(xhr.responseText)
      val name = res._source.name
      val cardtype = res._source.`type`
      val idstr = s"${res._id}"
      val sumText = URIUtils.decodeURIComponent(dom.window.atob(res._source.sumText.toString))
      val imgsrc = URIUtils.decodeURIComponent(dom.window.atob(res._source.imgsrc.toString))


        val iddiv = div(id := idstr, cls := "w3-cell-row").render
        println(idstr,name,cardtype)

        if (name.equals("My First Card") ||  (name.equals("QuizNews") && cardtype.equals("default") )) {
          val json_data = dom.window.atob(res._source.resume.asInstanceOf[String])
          //println("json_data",json_data)
          val (cardlen, cards) = read [(Int,ArrayBuffer[(Int,ArrayBuffer[String],ArrayBuffer[String])])](json_data)

          for (index <- 0 until cardlen) {
            val (length, saved_area1, saved_area2) = cards(index)
            for (i <- 0 until length) {
              if (saved_area2(i).contains("awi-text-page")) {
                println("11")
                val page_div = div(cls := "w3-cell-row kj-row", div(cls := saved_area2(i))).render
                println("12")
                page_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i)).replaceAll("w3-border", "").replaceAll("awi-border","")
                println("13")
                iddiv.appendChild(page_div)
                println("14")
              } else if (saved_area2(i).contains("awi-image-page")) {
                println("21")
                val image_div = div(cls := "w3-cell-row kj-row awi-image-page", div(cls := saved_area2(i))).render
                image_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
                new ImageDiv(image_div, init_pause = false)
                iddiv.appendChild(image_div)
                println("22")
              }
            }
            OneCardAction(target, idstr, cardNum, iddiv, sumText,imgsrc)
          }
        } else if (name.equals("QuizNews") && !js.isUndefined(res._source.resume)) {
          println("play quiznews")
          val json_data = dom.window.atob(res._source.resume.asInstanceOf[String])
          //find existing card and remove its children
          val card_stream = dom.window.document.getElementById(s"kj-multi-card$idstr").asInstanceOf[html.Div]
          while(card_stream.firstChild!=null) card_stream.removeChild(card_stream.firstChild)
          if (js.isUndefined(cardtype) || cardtype.equals("default")
                || cardtype.equals("quiznews") || cardtype.equals("typeaction")) {
            val qcard = new QuizDiv(card_stream, json_data, idstr)
            MultiCardAction(main_stream, idstr, cardNum, card_stream)
          } else if (cardtype.equals("typetest")) {
            println("play typetest")
            new TypeTest(card_stream, json_data, idstr)
            MultiCardAction(main_stream, idstr, cardNum, card_stream)
          } else {
            println("no match cardtype",cardtype)
          }
        }
        dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"

      case Failure(e) =>
        dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"
    }
  }


  def OneCardAction(target: html.Div, idstr: String, mainidstr:String, iddiv: html.Div, sumText:String, imgsrc:String): Unit = {
    val button1 = button(cls := "w3-button w3-small w3-theme-d1 w3-margin-bottom w3-border", i(cls := "fa fa-thumbs-up"), "Like").render
    val button2 = button(cls := "w3-button w3-small w3-theme-d1 w3-margin-bottom w3-border", i(cls := "fa fa-comment"), " Comment").render

    //val button4 = button(id := "ShareIt", cls := "w3-button w3-small w3-theme w3-border w3-right", i(cls := "fa fa-facebook")).render
    println("location", dom.window.location)
    println("pathname", dom.window.location.pathname)

    val baseURL = "%s".format(dom.window.location.toString.split('?')(0)).replace(dom.window.location.pathname, "/post")
    println("baseURL", baseURL)

    val postURL = s"$baseURL$idstr"

    println("postURL", postURL)




    val linkButton = div(cls := "w3-button w3-small w3-text-theme w3-margin-top w3-margin-bottom w3-border", "Link").render
    linkButton.onclick = (e: dom.Event) => {
      dom.window.document.getElementById("ShareIt").asInstanceOf[html.Div].style.display = "block"
      dom.window.document.getElementById("ShareURL").asInstanceOf[html.Input].value = s"$postURL"
    }

    val editBtn = button(cls := "w3-button w3-small w3-text-theme w3-margin-top w3-margin-bottom w3-border", i(cls := "fa fa-edit"), " Edit").render
    if (viewmode.equals("post")) editBtn.className += " w3-hide"
    editBtn.onclick =  (e:dom.Event) => {dom.window.location.href = s"quiznews.html?a=$idstr"}

    val cloneBtn = button(cls := "w3-button w3-small w3-text-theme w3-margin-top w3-margin-bottom w3-border", i(cls := "fa fa-edit"), " Make a Copy").render
    if (viewmode.equals("post")) cloneBtn.className += " w3-hide"
    cloneBtn.onclick =  (e:dom.Event) => {dom.window.location.href = s"quiznews.html?a=$idstr&mode=copy"}

    dom.window.document.getElementById("ShareURL").asInstanceOf[html.Input].value = s"$postURL"
    val facebutton = div(cls := "w3-button w3-small w3-text-theme w3-margin-top w3-margin-bottom w3-border", "Facebook").render
    facebutton.onclick = (e: dom.Event) => {
      val optstr =
        s"""{ "method" : "feed",
           |  "app_id" : "1719845414979964",
           |  "link" : "$postURL"
           |}""".stripMargin

      val options = js.JSON.parse(optstr)

      FB.ui(options, (res: js.Any) => {

      })
    }

    println("compare", mainidstr, idstr)
    var cardnews: html.Div = null
    var summary: html.Div = null
    if (mainidstr.equals(idstr)) {
      summary = div(id := s"sum$idstr", cls := "summary w3-cell-row w3-hide", img(cls := "w3-cell ", src := imgsrc, alt := "Avatar"),
        div(cls := "awi-sum-text w3-cell w3-padding ", sumText)).render
      cardnews = div(id := s"news$idstr", cls := "kj-container kj-card-2 w3-white w3-round w3-margin-top w3-margin-bottom",
        summary, iddiv, div(cls:="w3-margin kj-container w3-center",facebutton, linkButton,editBtn,cloneBtn)).render
      target.insertBefore(cardnews, target.firstChild)
    } else {
      summary = div(id := s"sum$idstr", cls := "summary w3-cell-row", img(cls := "w3-cell ", src := imgsrc, alt := "Avatar", style := "width:150px"),
        div(cls := "awi-sum-text w3-cell w3-padding", sumText)).render
      cardnews = div(id := s"news$idstr", cls := "kj-container kj-card-2 w3-white w3-round w3-margin-top w3-margin-bottom",
        summary, iddiv, div(cls:="w3-margin kj-container w3-center",facebutton, linkButton,editBtn,cloneBtn)).render
      target.appendChild(cardnews)
    }
    //if (mainidstr.equals(idstr)) mainnews = cardnews

    //cardnews.onclick = FocusCard(target, s"news$idstr", s"sum$idstr")

    PostProcess(target)
  }


  def MultiCardPreview(target:html.Div, main_stream:html.Div,idstr:String, mainidstr:String, sumText:String, imgsrc:String): Unit =
  {
    //println("33")
    val button1 = button(cls := "w3-button w3-small w3-theme-d1 w3-margin-bottom w3-border", i(cls := "fa fa-thumbs-up"), "Like").render
    val button2 = button(cls := "w3-button w3-small w3-theme-d1 w3-margin-bottom w3-border", i(cls := "fa fa-comment"), " Comment").render

    println("location", dom.window.location)
    println("pathname", dom.window.location.pathname)

    val baseURL = "%s".format(dom.window.location.toString.split('?')(0)).replace(dom.window.location.pathname, "/post")
    println("baseURL", baseURL)

    val postURL = s"$baseURL$idstr"

    println("postURL", postURL)


    //link share ===============================================
    val linkButton = div(cls := "w3-button w3-small w3-text-theme w3-margin-top w3-margin-bottom w3-border", "Link").render
    linkButton.onclick = (e: dom.Event) => {
      dom.window.document.getElementById("ShareIt").asInstanceOf[html.Div].style.display = "block"
      dom.window.document.getElementById("ShareURL").asInstanceOf[html.Input].value = s"$postURL"
    }
    val editBtn = button(cls := "w3-button w3-small w3-text-theme w3-margin-top w3-margin-bottom w3-border", i(cls := "fa fa-edit"), " Edit").render
    if (viewmode.equals("post")) editBtn.className += " w3-hide"
    editBtn.onclick =  (e:dom.Event) => {dom.window.location.href = s"quiznews.html?a=$idstr"}

    val cloneBtn = button(cls := "w3-button w3-small w3-text-theme w3-margin-top w3-margin-bottom w3-border", i(cls := "fa fa-edit"), " Make a Copy").render
    if (viewmode.equals("post")) cloneBtn.className += " w3-hide"
    cloneBtn.onclick =  (e:dom.Event) => {dom.window.location.href = s"quiznews.html?a=$idstr&mode=copy"}

    //link share ===============================================
    val playBtn = div(id := s"kj-play-btn-$idstr", cls := "kj-option-btn w3-button w3-small w3-round w3-block w3-teal w3-margin-top w3-padding", "Let's Play").render
    //link share end ========================================

    //println("imgsrc",imgsrc)
    //println("sumText",sumText)
    dom.window.document.getElementById("ShareURL").asInstanceOf[html.Input].value = s"$postURL"
    val facebutton = div(cls:="w3-button w3-small w3-text-theme w3-margin-top w3-margin-bottom w3-border","Facebook").render
    facebutton.onclick = (e:dom.Event) => {
      val optstr =
        s"""{ "method" : "feed",
           |  "app_id" : "1719845414979964",
           |  "link" : "$postURL"
           |}""".stripMargin

      //"description" : "$sumText",
      //"picture" : "$imgsrc"

      val options = js.JSON.parse(optstr)
      //println(optstr)

      FB.ui(options,(res:js.Any)=>{

      })
    }
    //facebook share end ===============================================
    //println("45")
    val card_stream = div(id := s"kj-multi-card$idstr").render
    var summary = div(id := s"multi_sum$idstr", cls := "summary",
      div(cls:= "awi-image-page", img(cls := "awi-image", src := imgsrc, alt := "Avatar")),
      div(cls := "awi-sum-text w3-padding w3-center", sumText)).render
    var cardnews = div(id := s"multi_news$idstr", cls := "kj-card-2 w3-white w3-round ",
      summary, card_stream, div(cls:="w3-margin w3-center",playBtn,facebutton, linkButton,editBtn,cloneBtn)).render
    if (mainidstr.equals(idstr)) {
      main_stream.insertBefore(cardnews, main_stream.firstChild)
    } else {
      main_stream.appendChild(cardnews)
    }
    //if (mainidstr.equals(idstr)) mainnews = cardnews

    //println("46")
    //cardnews.onclick = FocusCard(main_stream, s"multi_news$idstr", s"multi_sum$idstr")
    playBtn.onclick = (e: dom.Event) => {
      summary.className += " w3-hide"
      PostCard(target,main_stream,idstr)
    }
    PostProcess(main_stream)
    //println("47")

  }

  def MultiCardAction(main_stream:html.Div,idstr:String, mainidstr:String,card_stream:html.Div): Unit =
  {
    //println("33")
    val button1 = button(cls := "w3-button w3-small w3-theme-d1 w3-margin-bottom w3-border", i(cls := "fa fa-thumbs-up"), "Like").render
    val button2 = button(cls := "w3-button w3-small w3-theme-d1 w3-margin-bottom w3-border", i(cls := "fa fa-comment"), " Comment").render
    //val button4 = button(id:="ShareIt", cls:="w3-button w3-small w3-theme w3-border w3-right", i(cls:="fa fa-facebook")).render

    println("location", dom.window.location)
    println("pathname", dom.window.location.pathname)

/*
    var cardnews: html.Div = null
    var summary: html.Div = null
    if (mainidstr.equals(idstr)) {
      summary = div(id := s"multi_sum$idstr", cls := "summary w3-cell-row w3-hide", img(cls := "w3-cell ", src := imgsrc, alt := "Avatar"),
        div(cls := "w3-cell w3-padding", sumText, style := "height:100px;overflow:hidden")).render

      cardnews = div(id := s"multi_news$idstr", cls := "kj-card-2 w3-white w3-round ", br,
                    summary, card_stream, div(cls:="w3-margin w3-center",facebutton, linkButton)).render

      main_stream.insertBefore(cardnews, main_stream.firstChild)
    } else {
      summary = div(id := s"multi_sum$idstr", cls := "summary w3-cell-row", img(cls := "w3-cell ", src := imgsrc, alt := "Avatar", style := "width:150px"),
        div(cls := "w3-cell w3-padding", sumText, style := "height:100px;overflow:hidden")).render

      cardnews = div(id := s"multi_news$idstr", cls := "kj-card-2 w3-white w3-round ", style := "height:100px;overflow:hidden", br,
                  summary, card_stream, div(cls:="w3-margin kj-container w3-center",facebutton, linkButton)).render

      main_stream.appendChild(cardnews)
    }
    //if (mainidstr.equals(idstr)) mainnews = cardnews


    //println("46")
    cardnews.onclick = FocusCard(main_stream, s"multi_news$idstr", s"multi_sum$idstr")
*/
    dom.window.document.getElementsByTagName("body")(0).asInstanceOf[html.Body].scrollTop = card_stream.offsetTop - 40
    dom.window.document.getElementById(s"kj-play-btn-$idstr").asInstanceOf[html.Div].className += " w3-hide"

    PostProcess(main_stream)
    //println("47")

  }



  //def PageAdd(target:html.Div) : Function1[Event,_]  = (e:dom.Event) => {

  def FocusCard(target:html.Div,cardid:String,sumid:String) : Function1[Event,_]  = (e:dom.Event) => {
    println("cardid",cardid)
    println("sumid",sumid)
      val cardnews = dom.window.document.getElementById(cardid).asInstanceOf[html.Div]
      val summary = dom.window.document.getElementById(sumid).asInstanceOf[html.Div]
      //target.insertBefore(cardnews, target.firstChild)
      println("summary style",summary.getAttribute("style"))
      val style = summary.getAttribute("style")
      //if (style == null || !style.contains("hidden"))
        //cardnews.setAttribute("style", "height:150px;overflow:hidden")
      //else
      cardnews.setAttribute("style", "")
      summary.className += " w3-hide"
  }


  def PostProcess(target: html.Div): Unit = {
    println("1image size change")
    val containers = target.getElementsByClassName("awi-editor")
    for (tool <- containers) {
      tool.asInstanceOf[html.Div].className = tool.asInstanceOf[html.Div].className.replaceAll("w3-border","")
    }

    val actbars = target.getElementsByClassName("kj-actbar")
    for (tool <- actbars) {
      tool.asInstanceOf[html.Div].className += " w3-hide"
    }
    println("2image size change")
    val topbars = target.getElementsByClassName("top-actbar")
    for (tool <- topbars) {
      tool.asInstanceOf[html.Div].className += " w3-hide"
    }
    println("3image size change")
    val toolbars = target.getElementsByClassName("ql-toolbar")
    for (tool <- toolbars) {
      tool.asInstanceOf[html.Div].className += " w3-hide"
    }
    println("4image size change")
    val inputs = target.getElementsByTagName("input")
    for (tool <- inputs) {
      tool.asInstanceOf[html.Input].setAttribute("readonly", "true")
    }
    println("5image size change")
    val editors = target.getElementsByClassName("ql-editor")
    for (tool <- editors) {
      tool.asInstanceOf[html.Input].setAttribute("contenteditable", "false")
    }


    println("6image size change")
    //img size
    val images = target.getElementsByTagName("img")
    for (tool <- images) {
      tool.asInstanceOf[html.Div].className += " awi-image"

    }


  }
}