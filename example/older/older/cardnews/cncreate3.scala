package cardnews

/**
  * Created by Administrator on 2017-04-24.
  */


import lib.{Quill}
import org.scalajs.dom.{DOMParser, Event, FileReader, MouseEvent}

import scala.scalajs.js.URIUtils

import org.scalajs.dom
import dom.html
import dom.ext._
import org.scalajs.dom.ext._


import scalajs.js
import scalajs.js.annotation.JSExport
import scalatags.JsDom.all._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

import scala.concurrent._
import ExecutionContext.Implicits.global

@JSExport
object cncreate3 extends {
  var objMap = new ArrayBuffer[mutable.HashMap[String, String]]()
  var lineMap = new ArrayBuffer[mutable.HashMap[String, String]]()
  var myUrl : String = ""
  val docBase = "http://localhost:12345/target/scala-2.11/classes/"
  var cardNum : String = null
  var pageNum = 0
  var curPage : html.Div  = null

  @JSExport
  def main(target: html.Div, saved_area:html.Div): Unit = {

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

    if (paraMap.contains("a")) {
      cardNum = paraMap("a")
      LoadCard(target, cardNum)
    } else {
      dom.window.onload = (e:dom.Event) => { PageAdd(target,true)(e); ImageAdd(target)(e); } // Title + Image + Body
    }
    //val container = dom.window.document.getElementById("pages")
    //val qle = dom.window.document.getElementsByClassName("ql-editor").asInstanceOf[html.Element]
    dom.window.document.getElementById("pageAdd").asInstanceOf[html.Button].onclick = PageAdd(target)
    dom.window.document.getElementById("imageAdd").asInstanceOf[html.Button].onclick = ImageAdd(target)
    dom.window.document.getElementById("CardDelete").asInstanceOf[html.Button].onclick = CardDelete(target)
    dom.window.document.getElementById("CardSave").asInstanceOf[html.Button].onclick = CardSave(target,saved_area)

    dom.window.document.getElementById("kj-main").asInstanceOf[html.Button].onclick = ActiveHide(target)


  } //main


  def LoadCard(target:html.Div,cardNum : String): Unit = {
    import upickle.default._

    val urlBase = "" // GlobalVars.dataBase + s"/$cardNum"

    println ("URL:",urlBase)
    //dom.window.alert("called Tab:"+objNm+":"+objId+":"+tab)
    ///////////////////////////////////////////////
    //Ajax.get(urlBase).onSuccess { case xhr => ...}
    Ajax.get(urlBase).onSuccess { case xhr =>
      //println(xhr.responseText)
      //var saved_area1 : new ArrayBuffer[String] = null
      //var length = 0
      val res = js.JSON.parse(xhr.responseText)
        val name = res._source.name
        val iddiv = div(id := cardNum).render

        //println("res",res._source.resume)
        val json_data = dom.window.atob(res._source.resume.asInstanceOf[String])
      //println("json_data",json_data)
        val (length, saved_area1,saved_area2) = read[(Int,ArrayBuffer[String],ArrayBuffer[String])](json_data)

        for (i <- 0 until length) {
          //println(length,saved_area1(i))
          //println(length,saved_area2(i))
          if (saved_area2(i).contains("awi-text-page")) {
            val page_div = div(cls := saved_area2(i)).render
            page_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
            PageLoad(target, page_div)
          } else if (saved_area2(i).contains("awi-image-page")) {
            val image_div = div(cls := saved_area2(i)).render
            image_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
            ImageLoad(target, image_div)
          }

          //target.appendChild(page_div)
        }

      println("6image size change")
      //img size
      val images = target.getElementsByTagName("img")
      for (tool <- images) {
        tool.asInstanceOf[html.Div].className += " awi-image"

      }
    }

  }



  def CardSave(target:html.Div,saved_area:html.Div) : Function1[MouseEvent,_]  = (e:dom.Event) => {
    //import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
    import upickle.default._


    val saved_area1 = new ArrayBuffer[String]()
    val saved_area2 = new ArrayBuffer[String]()

    val datas = target.getElementsByClassName("awi-data")
    //var i : Int = 0
    for( i <- 0 until datas.length) {
      val hdata = datas(i).asInstanceOf[dom.html.Div]
      //println("innerHtml",hdata.innerHTML)
      //println(i,datas(i))
      //println("classname",hdata.className)

      if (hdata != null && !js.isUndefined(hdata)) {
        if (hdata.className.contains("awi-text-page")) {
          //println("saving",hdata.className)
          saved_area1 += URIUtils.encodeURIComponent(hdata.innerHTML)
          saved_area2 += hdata.className
          //println("saved",hdata.className)
        } else if (hdata.className.contains("awi-image-page")) {
          val theImage = hdata.getElementsByClassName("awi-image")(0)
          saved_area1 += URIUtils.encodeURIComponent(hdata.innerHTML)
          saved_area2 += hdata.className
        }
      }
    }


    val json_data = write((saved_area1.length,saved_area1,saved_area2))
    val jsonb_data = dom.window.btoa(json_data)
    println("json",json_data)

    val pages = s"""{
      "name": "My First Card",
      "resume":  "$jsonb_data"
    }"""


    var urlBase = ""
    if (cardNum == null)
      urlBase = ""//s"${GlobalVars.dataBase}/?pipeline=timestamp"  //New card
    else
      urlBase = ""//s"${GlobalVars.dataBase}/$cardNum?pipeline=timestamp" //Existing Card

    println("request:",urlBase)
    //dom.window.alert("called Tab:"+objNm+":"+objId+":"+tab)
    ///////////////////////////////////////////////
    //Ajax.get(urlBase).onSuccess { case xhr => ...}

    Ajax.post(urlBase,pages).onSuccess { case xhr =>
      println(xhr.responseText)

      //SavePreview
      val res = js.JSON.parse(xhr.responseText)
      println("id:" , res._id)
      cardNum = res._id.toString
      PreviewCard(target,cardNum)
      dom.window.alert("Done")
      dom.window.location.href = "list.html"
    }
    //PreviewCard(target,cardNum)

  }




  def PreviewCard(iddiv:html.Div,cardNum : String): Unit = {
    import upickle.default._

    val baseURL = "%s".format(dom.window.location.href.replace(dom.window.location.pathname+dom.window.location.search,s"/save$cardNum"))
    println("baseURL",baseURL)

    var sumText = "No Text"
    var imgsrc = ""
    var imgdata = ""
    //***********testing : facebook share  ===============================================
    var sumimg = iddiv.getElementsByTagName("img")
    println("11")

    if (!js.isUndefined(sumimg(0)) && sumimg(0) != null ) {
      imgsrc = sumimg(0).asInstanceOf[html.Image].src
    } else {
      imgsrc = "./images/avatar2.png"
    }
    println("12")
    var maxlength = 120
    if (iddiv.textContent != null) {
      println("13")
      var pureText2 = iddiv.textContent.replaceAll("[^a-zA-Z0-9가-힝]", "--")
      var pureText3 = pureText2.replaceAll("----", "")
      var pureText = pureText3.replaceAll("--", " ")
      if (pureText.length < 120) maxlength = pureText.length
      sumText = pureText.substring(0,maxlength)
      println("14")

    }

    println("15",sumText)

    val pages = s"""{
      "sumText":"$sumText",
      "imgsrc":"$imgsrc",
      "cardNum":"$cardNum"
    }"""

    Ajax.post(baseURL,pages).onSuccess { case xhr =>
    }


  }


  def CardDelete(target:html.Div) : Function1[MouseEvent,_]  = (e:dom.Event) => {
    //import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

    if (cardNum != null) {
      val urlBase = ""//s"${GlobalVars.dataBase}/$cardNum"

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

  def PageAdd(target:html.Div, isTitle:Boolean = false) : Function1[Event,_]  = (e:dom.Event) => {

    //body compose
    val  theToolbar = "ToolNum" + pageNum
      pageNum += 1
    val editor = div( cls:="awi-editor awi-border").render
    val toolbar =  div( cls:="kj-pagebar" , id:=theToolbar,
      span(" "),
      button ("작게",cls:="ql-size",value:="small",style:="height:100%;font-size:12px"),
      button ("표준",cls:="ql-size",value:="false",style:="height:100%;font-size:13px"),
      button ("크게",cls:="ql-size",value:="large",style:="height:100%;font-size:14px"),
      button ("제목",cls:="ql-size",value:="huge",style:="height:100%;font-size:15px"),
      span(" "),
      button ("B",cls:="ql-bold",style:="font-style:Bold"),
      button ("I",cls:="ql-italic",style:="font-style:Italic"),
      button ("U",cls:="ql-underline",style:="text-decoration: underline;"),
      button( "H", cls:="ql-background", value:="yellow",style:="background-color:yellow"),
      button (i(cls:="fa fa-align-left"),cls:="ql-align w3-hide-small",value:="left",style:="height:90%;font-size:15px"),
      button (i(cls:="fa fa-align-center"),cls:="ql-align w3-hide-small",value:="center",style:="height:90%;font-size:15px"),
      button (i(cls:="fa fa-align-right"),cls:="ql-align w3-hide-small",value:="right",style:="height:90%;font-size:15px")
    ).render
    val pageAdd = button(id:="pageAdd",cls:="w3-button w3-black ", i(cls:="fa fa-file-text")).render
    val imageAdd = button(id:="imageAdd",cls:="w3-button w3-teal ", i(cls:="fa fa-image")).render
    val pageDel = button(id:="pageDel",cls:="w3-button  w3-red ", i(cls:="fa fa-trash")).render
    val pageBtn = div(cls:=" kj-pagetool",pageAdd,imageAdd,pageDel).render
    val title = div(input("TITLE",cls:="w3-xlarge")).render
    val newPage = div(cls:="w3-cell-row kj-row", div(cls:="awi-data awi-text-page",editor),div(cls:="kj-actbar w3-border",toolbar,pageBtn)).render

    //evnet processing
    pageAdd.onclick = PageAdd(target)
    imageAdd.onclick = ImageAdd(target)
    pageDel.onclick = PageDelete(target,newPage)
    newPage.onclick = ActiveToggle(target,newPage,editor.asInstanceOf[html.Div])

    //render
    if (curPage != null)
      target.insertBefore(newPage,curPage.nextSibling)
    else
      target.appendChild(newPage)
    title.click()
    CreateQuill(editor,"#"+theToolbar,isTitle)
  }

  def PageLoad(target:html.Div, text_page:html.Div)  {

    //body compose
    val  theToolbar = "ToolNum" + pageNum
    pageNum += 1
    val editor = text_page.getElementsByClassName("awi-editor")(0)
    editor.asInstanceOf[html.Div].className += " w3-border"
    val toolbar =  div( cls:="kj-pagebar" , id:=theToolbar,
      span(" "),
      button ("작게",cls:="ql-size",value:="small",style:="height:100%;font-size:12px"),
      button ("표준",cls:="ql-size",value:="false",style:="height:100%;font-size:13px"),
      button ("크게",cls:="ql-size",value:="large",style:="height:100%;font-size:14px"),
      button ("제목",cls:="ql-size",value:="huge",style:="height:100%;font-size:15px"),
      span(" "),
      button ("B",cls:="ql-bold",style:="font-style:Bold"),
      button ("I",cls:="ql-italic",style:="font-style:Italic"),
      button ("U",cls:="ql-underline",style:="text-decoration: underline;"),
      button( "H", cls:="ql-background", value:="yellow",style:="background-color:yellow"),
      button (i(cls:="fa fa-align-left"),cls:="ql-align w3-hide-small",value:="left",style:="height:90%;font-size:15px"),
      button (i(cls:="fa fa-align-center"),cls:="ql-align w3-hide-small",value:="center",style:="height:90%;font-size:15px"),
      button (i(cls:="fa fa-align-right"),cls:="ql-align w3-hide-small",value:="right",style:="height:90%;font-size:15px")
    ).render
    val pageAdd = button(id:="pageAdd",cls:="w3-button w3-black ", i(cls:="fa fa-file-text")).render
    val imageAdd = button(id:="imageAdd",cls:="w3-button w3-teal ", i(cls:="fa fa-image")).render
    val pageDel = button(id:="pageDel",cls:="w3-button  w3-red ", i(cls:="fa fa-trash")).render
    val pageBtn = div(cls:=" kj-pagetool",pageAdd,imageAdd,pageDel).render
    val title = div(input("TITLE",cls:="w3-xlarge")).render
    val newPage = div(cls:="w3-cell-row kj-row", text_page, div(cls:="kj-actbar w3-hide",toolbar,pageBtn)).render

    //evnet processing
    pageAdd.onclick = PageAdd(target)
    imageAdd.onclick = ImageAdd(target)
    pageDel.onclick = PageDelete(target,newPage)
    newPage.onclick = ActiveToggle(target,newPage,editor.asInstanceOf[html.Div])

    //render
    if (curPage != null)
      target.insertBefore(newPage,curPage.nextSibling)
    else
      target.appendChild(newPage)
    title.click()
    CreateQuill(editor,"#"+theToolbar)
  }



  def ImageAdd(target:html.Div) : Function1[Event,_]  = (e:dom.Event) => {

    //body compose
    val theImage = "ImgNum"+pageNum
    val thePreview = "PreNum"+pageNum
    pageNum += 1
    val image = div(cls:="w3-center",
                   img(cls:="awi-image", id:=thePreview, src:="./images/story_cover_gif.jpg",alt:="Image")
                ).render

    val  theToolbar = "ToolNum" + pageNum
    val toolbar =  div( cls:="kj-imagebar" , id:=theToolbar,
      label( i(cls:="fa fa-camera w3-xxlarge"), cls:="w3-xxlarge w3-blue", `for`:=theImage, input(id:=theImage, `type`:="file", multiple, hidden))).render

    val pageAdd = button(id:="pageAdd",cls:="w3-button w3-black ", i(cls:="fa fa-file-text")).render
    val imageAdd = button(id:="imageAdd",cls:="w3-button w3-teal ", i(cls:="fa fa-image")).render
    val pageDel = button(id:="pageDel",cls:="w3-button  w3-red ", i(cls:="fa fa-trash")).render
    val pageBtn = div(cls:="kj-pagetool",pageAdd,imageAdd,pageDel).render

    val title = div(input("TITLE",cls:="w3-xlarge")).render
    val imgPage = div(cls:="w3-cell-row kj-row" , div(cls:="awi-data awi-image-page w3-left" ,image),div(cls:="kj-actbar ",toolbar,pageBtn)).render

      //evnet processing


    pageAdd.onclick = PageAdd(target)
    imageAdd.onclick = ImageAdd(target)
    pageDel.onclick = PageDelete(target,imgPage)
    imgPage.onclick = ActiveToggle(target,imgPage,image)

    //render
    if (curPage != null)
      target.insertBefore(imgPage,curPage.nextSibling)
    else
      target.appendChild(imgPage)
    //post event
    val imageFile = dom.window.document.getElementById(theImage).asInstanceOf[html.Input]
    val preview = dom.window.document.getElementById(thePreview).asInstanceOf[html.Image]
    imageFile.onchange = RenderImage(image,preview,imageFile)
    title.click()
  }

  def ImageLoad(target:html.Div,new_image:html.Div)  {

    //body compose
    val theImage = "ImgNum"+pageNum
    val thePreview = "PreNum"+pageNum
    pageNum += 1

    /*val image = div(cls:="w3-center",
      img(cls:="awi-image", id:=thePreview, src:="./images/story_cover_gif.jpg",alt:="Image")
    ).render*/

    //assign new id
    val image_div = new_image.getElementsByClassName("awi-image")(0)
    image_div.asInstanceOf[dom.html.Div].id = thePreview
    // val theImage = "ImgNum"+pageNum
    val  theToolbar = "ToolNum" + pageNum

    val imageFile = input(id:=theImage, `type`:="file", multiple, hidden).render

    val toolbar =  div( cls:="kj-imagebar" , id:=theToolbar,
      label( i(cls:="fa fa-camera w3-xxlarge"), cls:="w3-xxlarge w3-blue", `for`:=theImage, imageFile)).render

    val pageAdd = button(id:="pageAdd",cls:="w3-button w3-black ", i(cls:="fa fa-file-text")).render
    val imageAdd = button(id:="imageAdd",cls:="w3-button w3-teal ", i(cls:="fa fa-image")).render
    val pageDel = button(id:="pageDel",cls:="w3-button  w3-red ", i(cls:="fa fa-trash")).render
    val pageBtn = div(cls:="kj-pagetool",pageAdd,imageAdd,pageDel).render
    val title = div(input("TITLE",cls:="w3-xlarge")).render

    val imgPage = div(cls:="w3-cell-row kj-row" , new_image, div(cls:="w3-cell kj-actbar w3-hide",toolbar,pageBtn)).render

    //evnet processing


    pageAdd.onclick = PageAdd(target)
    imageAdd.onclick = ImageAdd(target)
    pageDel.onclick = PageDelete(target,imgPage)
    imgPage.onclick = ActiveToggle(target,imgPage,new_image)

    //render
    if (curPage != null)
      target.insertBefore(imgPage,curPage.nextSibling)
    else
      target.appendChild(imgPage)
    //post event
    //val imageFile = dom.window.document.getElementById(theImage).asInstanceOf[html.Input]
    val preview = dom.window.document.getElementById(thePreview).asInstanceOf[html.Image]
    imageFile.onchange = RenderImage(new_image,preview,imageFile)
    title.click()
  }

  def PageDelete(target:html.Div,page:html.Div) : Function1[MouseEvent,_]  = (e:dom.Event) => {
    target.removeChild(page)
    curPage = null
  }




  def RenderImage(imagediv:html.Div, preview:html.Image,imageFiles:html.Input) : Function1[Event,_]  = (e:dom.Event) => {
    for (child <- imagediv.children) imagediv.removeChild(child)
    val length = e.srcElement.asInstanceOf[html.Input].files.length
    var column = 1
    if ( length > 1 ) column = 2
    if ( length > 4) column = 3
    if ( length > 9) column = 4
    for (i <- 0 until length ) {
      val width = 400 / column
      val file = e.srcElement.asInstanceOf[html.Input].files(i)
      println("reading:", file.name)
      val reader = new FileReader()
      reader.onload = (e: dom.Event) => {
        val the_url = e.target.asInstanceOf[FileReader].result
        //println("done:",the_url.toString)
        //preview.src = the_url.toString
        val newimage = img(cls:="awi-image",style:=s"width:$width",src := the_url.toString).render
        imagediv.appendChild(newimage)
      }
      if (file != null) reader.readAsDataURL(file)
    }

  }




  def ActiveToggle(target:html.Div,newPage:html.Div, ref : html.Div) : Function1[MouseEvent,_]  = (e:dom.Event) => {

    //set current page
    curPage = newPage
    // calc new position of selected page
    var rect = ref.getBoundingClientRect()
    var win_width = dom.window.innerWidth
    var win_height = dom.window.innerHeight
    println("clinet rect", rect.left,rect.top,rect.right,rect.bottom)
    // move pageBtn position to the right of the page
    for (action <- target.getElementsByClassName("kj-pagetool")) {
      //println("w3-moveto",rect.left,rect.top,rect.right,rect.bottom)
      if (win_width < 600) {
        action.asInstanceOf[html.Div].style.top = "50"
        action.asInstanceOf[html.Div].style.left = "0"
      }
      else {
        action.asInstanceOf[html.Div].style.left = (rect.right).toString
        action.asInstanceOf[html.Div].style.top = (rect.top + (rect.bottom - rect.top) / 2).toString
        if ((rect.top + (rect.bottom - rect.top) / 2) < 30)
          action.asInstanceOf[html.Div].style.top = "60"
        var myrect = action.asInstanceOf[html.Div].getBoundingClientRect()
        if (myrect.right > win_width)
          action.asInstanceOf[html.Div].style.left = (win_width - 60).toString
      }
    }

    for (action <- target.getElementsByClassName("kj-pagebar")) {
      if (win_width < 600) {
        action.asInstanceOf[html.Div].style.left = "0"
        action.asInstanceOf[html.Div].style.top = (win_height - 30).toString
      }else{
        action.asInstanceOf[html.Div].style.left = (rect.left).toString
        action.asInstanceOf[html.Div].style.top = (rect.top - 30).toString

        //var myrect = action.asInstanceOf[html.Div].getBoundingClientRect()
        //println("w3-actbar pos",myrect.left,myrect.top,myrect.right,myrect.bottom)
        if (rect.top < 30)
          action.asInstanceOf[html.Div].style.top = "30"
        //action.asInstanceOf[html.Div].style.top = (rect.top + (rect.bottom-rect.top)/2).toString
      }
    }

    //toggle show / hide
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
      println("emptypage",dom.window.document.getElementById("rtoolbar").asInstanceOf[html.Div].className)
      dom.window.document.getElementById("rtoolbar").asInstanceOf[html.Div].className =
          dom.window.document.getElementById("rtoolbar").asInstanceOf[html.Div].className.replaceAll("w3-hide","")
    } else {
      println("not_emptypage",dom.window.document.getElementById("rtoolbar").asInstanceOf[html.Div].className)
        dom.window.document.getElementById("rtoolbar").asInstanceOf[html.Div].className += " w3-hide"
    }
    e.stopPropagation()
  }


  def ActiveHide(target:html.Div) : Function1[MouseEvent,_]  = (e:dom.Event) => {
    println("active hide")
    for (action <- target.getElementsByClassName("kj-actbar")) {
      if (action.asInstanceOf[html.Div].className.indexOf("w3-hide") == -1)
        action.asInstanceOf[html.Div].className += " w3-hide"
    }
  }

  def CreateQuill(cont : js.Any, toolbarid:String, isTitle:Boolean = false) : Quill = {

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
    if (isTitle) editor.format("size","large")
    return editor
  }


}
