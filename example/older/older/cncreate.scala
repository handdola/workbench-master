package older

/**
  * Created by Administrator on 2017-04-24.
  */

/**
  * Created by Administrator on 2017-04-13.
  */
//import cardnews.cncreate.ShowTab
import lib.Quill
import org.scalajs.dom
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.{FileReader, MouseEvent, Window}
import org.scalajs.dom.{DOMParser, Event, html}

import scala.scalajs.js
import scala.scalajs.js.URIUtils
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom.all.alt
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
object cncreate extends {
  var objMap = new ArrayBuffer[mutable.HashMap[String, String]]()
  var lineMap = new ArrayBuffer[mutable.HashMap[String, String]]()
  var myUrl : String = ""
  var winRef : Window =null
  val docBase = "http://localhost:12345/target/scala-2.11/classes/"
  var cardNum : String = null
  var pageNum = 0

  @JSExport
  def main(target: html.Div): Unit = {

    var paraMap = new mutable.HashMap[String, String]

    println(dom.window.location.search)
    val query = dom.window.location.search.substring(1).split("&")
    //loc.sea   .search.substring(1).split("&")
    for (q <- query) {
      var param = q.split("=")
      paraMap += (param(0)->param(1))
      println(param(0)+" : " +param(1))
    }

    //println(paraMap("abc"))

    if (paraMap.contains("cardnum")) {
      cardNum = paraMap("cardnum")
      LoadCard(target, cardNum)
    } else {
      dom.window.onload = PageAdd(target)
    }
    //val container = dom.window.document.getElementById("pages")
    //val qle = dom.window.document.getElementsByClassName("ql-editor").asInstanceOf[html.Element]
    dom.window.document.getElementById("pageAdd").asInstanceOf[html.Button].onclick = PageAdd(target)
    dom.window.document.getElementById("imageAdd").asInstanceOf[html.Button].onclick = ImageAdd(target)
    dom.window.document.getElementById("CardDelete").asInstanceOf[html.Button].onclick = CardDelete(target)
    dom.window.document.getElementById("CardSave").asInstanceOf[html.Button].onclick = CardSave(target)

    //val imageFile = dom.window.document.getElementById("thephoto").asInstanceOf[html.Input]
    //val preview = dom.window.document.getElementById("preview").asInstanceOf[html.Image]
    //imageFile.onchange = RenderImage(preview,imageFile)

    //ShowTab("Unknown", paraMap("objId"), "tab1")


  } //main


  def LoadCard(target:html.Div,cardNum : String): Unit = {
    val urlBase = s"http://211.238.201.25:9200/cardnews/employee/$cardNum"

    println ("URL:",urlBase)
    //dom.window.alert("called Tab:"+objNm+":"+objId+":"+tab)
    ///////////////////////////////////////////////
    //Ajax.get(urlBase).onSuccess { case xhr => ...}
    Ajax.get(urlBase).onSuccess { case xhr =>
      //println(xhr.responseText)
      val res = js.JSON.parse(xhr.responseText)
        val name = res._source.name
        val iddiv = div(id := cardNum).render

        //val cardnews = div(cls:="w3-margin", div(cls:="w3-col m12", div(cls:="w3-card-2 w3-round w3-white ",
        //   div(id:="kj-main",cls:="kj-container w3-padding",iddiv)))).render
        val htmlstr = dom.window.atob(res._source.resume.asInstanceOf[String])

        iddiv.innerHTML = URIUtils.decodeURIComponent(htmlstr)
        println(htmlstr)
        //val parser = new DOMParser()
        //val htmldoc = parser.parseFromString(htmlstr, "application/xml")
        //println(htmlcont)
        target.appendChild(iddiv)
    }

  }

  def CardSave(target:html.Div) : Function1[MouseEvent,_]  = (e:dom.Event) => {

    var html = target.innerHTML

    println(html)

    val bhtml = dom.window.btoa(URIUtils.encodeURIComponent(html))
    //val titles = target.getElementsByTagName("Input")

    val pages = s"""{
      "name": "My First Card",
      "resume":  "$bhtml"
    }"""


    /*for (title <- target.getElementsByClassName("Input")) {
      //var stext = title.asInstanceOf[html.Div]
      title.asInstanceOf[html.Div].setAttribute("data-text","hahaha")
      //title1.setAttribute("data-text",stext)
    }*/

    var urlBase = ""
    if (cardNum == null)
      urlBase = s"http://211.238.201.25:9200/cardnews/employee/"
    else
      urlBase = s"http://211.238.201.25:9200/cardnews/employee/$cardNum"

    println("request:",urlBase)
    //dom.window.alert("called Tab:"+objNm+":"+objId+":"+tab)
    ///////////////////////////////////////////////
    //Ajax.get(urlBase).onSuccess { case xhr => ...}
    Ajax.post(urlBase,pages).onSuccess { case xhr =>
      println(xhr.responseText)
      //val res = js.JSON.parse(xhr.responseText)
      //println("id:" , res._id)
      dom.window.alert("Done")
      dom.window.location.href = "list.html"
    }

  }

  def CardDelete(target:html.Div) : Function1[MouseEvent,_]  = (e:dom.Event) => {

    if (cardNum != null) {
      val urlBase = s"http://211.238.201.25:9200/cardnews/employee/$cardNum"

      println("request:", urlBase)
      //dom.window.alert("called Tab:"+objNm+":"+objId+":"+tab)
      ///////////////////////////////////////////////
      //Ajax.get(urlBase).onSuccess { case xhr => ...}
      Ajax.delete(urlBase).onSuccess { case xhr =>
        println(xhr.responseText)
        //val res = js.JSON.parse(xhr.responseText)
        //println("id:" , res._id)
        dom.window.alert("Done")
        dom.window.location.href = "list.html"
      }
    }

  }

  def PageAdd(target:html.Div) : Function1[Event,_]  = (e:dom.Event) => {

    //body compose
    val  theToolbar = "ToolNum" + pageNum
      pageNum += 1
    val editor = div( cls:="w3-mobile w3-border", style:="margin-left:100px;max-width:85%").render
    val toolbar =  div( cls:="w3-mobile" , id:=theToolbar, style:="position:absolute;width:100px;top:30px;left:-20px",
      select( cls:="ql-size",
        option( "Small", value:="small"),
        option( "Normal", value:="normal"),
        option( "Large",value:="large"),
        option( "Huge",value:="huge")),
      button ("B",cls:="ql-bold",style:="font-style:Bold"),
      button ("I",cls:="ql-italic",style:="font-style:Italic"),
      button ("U",cls:="ql-underline",style:="text-decoration: underline;"),
      button( "H", cls:="ql-background", value:="yellow",style:="background-color:yellow")).render
      val pageAdd = button(id:="pageAdd",cls:="w3-button w3-circle w3-red w3-small", i(cls:="fa fa-file-text")).render
      val imageAdd = button(id:="imageAdd",cls:="w3-button w3-circle w3-red w3-small", i(cls:="fa fa-image")).render
      val pageDel = button(id:="pageDel",cls:="w3-button w3-circle w3-red w3-small", i(cls:="fa fa-trash")).render
    val pageBtn = div(cls:="kj-actbar w3-mobile",pageAdd,imageAdd,pageDel).render
    val title = div(input("TITLE",cls:="w3-mobile w3-xlarge",style:="margin-left:100px;width:80%")).render
    val newPage = div(cls:="w3-cell-row w3-padding-small", div(cls:="w3-cell",style:="position:relative;height:100px",editor,pageBtn,toolbar)).render

    //evnet processing
    pageAdd.onclick = PageAdd(target)
    imageAdd.onclick = ImageAdd(target)
    pageDel.onclick = PageDelete(target,newPage)
    newPage.onclick = ActiveToggle(target,newPage)

    //render
    target.appendChild(newPage)
    title.click()
    CreateQuill(editor,"#"+theToolbar)
  }


  def ImageAdd(target:html.Div) : Function1[Event,_]  = (e:dom.Event) => {

    //body compose
    val theImage = "ImgNum"+pageNum
    val thePreview = "PreNum"+pageNum
    pageNum += 1
    val image = div(cls:="image-upload",
                 label(`for`:=theImage,
                   img(id:=thePreview,cls:="", src:="./images/story_cover_gif.jpg",style:="margin-left:100px;max-width:85%",alt:="Image")
                 ),
                 input(id:=theImage, `type`:="file", hidden)
                ).render
    val pageAdd = button(id:="pageAdd",cls:="w3-button w3-circle w3-red w3-small", i(cls:="fa fa-file-text")).render
    val imageAdd = button(id:="imageAdd",cls:="w3-button w3-circle w3-red w3-small", i(cls:="fa fa-image")).render
    val pageDel = button(id:="pageDel",cls:="w3-button w3-circle w3-red w3-small", i(cls:="fa fa-trash")).render
    val pageBtn = div(cls:="kj-actbar w3-mobile",pageAdd,imageAdd,pageDel).render
    val title = div(input("TITLE",cls:="w3-xlarge",style:="margin-left:100px;width:80%")).render
    val imgPage = div(cls:="w3-cell-row w3-padding-small", div(cls:="w3-cell",style:="position:relative",image,pageBtn)).render

      //evnet processing


    pageAdd.onclick = PageAdd(target)
    imageAdd.onclick = ImageAdd(target)
    pageDel.onclick = PageDelete(target,imgPage)
    imgPage.onclick = ActiveToggle(target,imgPage)

    //render
    target.appendChild(imgPage)
    //post event
    val imageFile = dom.window.document.getElementById(theImage).asInstanceOf[html.Input]
    val preview = dom.window.document.getElementById(thePreview).asInstanceOf[html.Image]
    imageFile.onchange = RenderImage(preview,imageFile)
    title.click()
  }

  def PageDelete(target:html.Div,page:html.Div) : Function1[MouseEvent,_]  = (e:dom.Event) => {
    target.removeChild(page)
    var emptyPage = true
    for (action <- dom.window.document.getElementsByClassName("kj-actbar")){
      emptyPage = false
    }
    if(emptyPage) {
      println("emptyPage")
      for (action <- dom.window.document.getElementsByClassName("top-actbar")) {
        println("emptyPage show")
        action.asInstanceOf[html.Div].className = action.asInstanceOf[html.Div].className.replaceAll("w3-hide", "")
      }
    } else {
      for (action <- dom.window.document.getElementsByClassName("top-actbar")) {
        println("emptyPage hide")
        action.asInstanceOf[html.Div].className += " w3-hide"
      }

    }
  }



  def RenderImage(preview:html.Image,imageFiles:html.Input) : Function1[Event,_]  = (e:dom.Event) => {
    val file = e.srcElement.asInstanceOf[html.Input].files(0)
    println("reading:",file)
    val reader = new FileReader()
    reader.onload = (e:dom.Event) => {
      val the_url = e.target.asInstanceOf[FileReader].result
      //println("done:",the_url.toString)
      preview.src = the_url.toString
    }
    if (file!=null) reader.readAsDataURL(file)

  }




  def ActiveToggle(target:html.Div,newPage:html.Div) : Function1[MouseEvent,_]  = (e:dom.Event) => {
    for (action <- target.getElementsByClassName("kj-actbar")) {
      println("w3-hide")
      if (action.asInstanceOf[html.Div].className.indexOf("w3-hide") == -1)
        action.asInstanceOf[html.Div].className += " w3-hide"
    }
    println("focused")
    for (action <- newPage.getElementsByClassName("kj-actbar")){
      println("w3-show",action.asInstanceOf[html.Div].className)
      action.asInstanceOf[html.Div].className = action.asInstanceOf[html.Div].className.replaceAll("w3-hide","")
    }
    var emptyPage = true
    for (action <- dom.window.document.getElementsByClassName("kj-actbar")){
      emptyPage = false
    }
    if(emptyPage) {
      println("emptyPage")
      for (action <- dom.window.document.getElementsByClassName("top-actbar")) {
        action.asInstanceOf[html.Div].className = action.asInstanceOf[html.Div].className.replaceAll("w3-hide", "")
      }
    } else {
      for (action <- dom.window.document.getElementsByClassName("top-actbar")) {
        action.asInstanceOf[html.Div].className += " w3-hide"
      }

    }
  }

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

  def ShowTab(objNm : String, objId : String, tab : String) : Unit = {


    val urlBase = s"http://192.168.1.222:7070/ea/object/getObjectInfoListData.do?OBJ_ID=$objId"
    val tabList = List(("tab1","London"), ("tab2","Paris"), ("tab3", "Tokyo"))
    for (tabOne <- tabList) {
      val tabs = dom.window.document.getElementById(tabOne._1).asInstanceOf[html.Div]
      tabs.onclick = (e: dom.Event) => {
        //winRef.window.alert(e.toString)
        ShowTab("Unknown", objId, tabOne._1)
      }
    }

    //dom.window.alert("called Tab:"+objNm+":"+objId+":"+tab)
    ///////////////////////////////////////////////
    Ajax.get(urlBase).onSuccess { case xhr =>

      var objMap = new mutable.HashMap[String, String]()
      var attrMap = new ArrayBuffer[mutable.HashMap[String, String]]()
      var linkMap = new ArrayBuffer[mutable.HashMap[String, String]]()
      var prodMap = new ArrayBuffer[mutable.HashMap[String, String]]()
      val parser = new DOMParser()
      val doc = parser.parseFromString(xhr.responseText, "application/xml")
      val target = dom.window.document.getElementById("cont")
      ///////////////////////////////Draw Object/////////////////////////////////

      for (link <- dom.window.document.getElementsByClassName("tablink"))
        link.asInstanceOf[html.Button].className = link.asInstanceOf[html.Button].className.replace("w3-blue","")
      dom.window.document.getElementById(tab).asInstanceOf[html.Button].className += " w3-blue"


      var popList = doc.getElementsByTagName("EA_OBJ")
      for (pop <- popList) {
        for (child <- pop.childNodes) objMap.put(child.nodeName, child.textContent)
      }


    } //Ajax

  } //showTab






}
