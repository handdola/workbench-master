package older.parser

/**
  * Created by Administrator on 2017-04-13.
  */
import org.scalajs.dom
import org.scalajs.dom.DOMParser
import org.scalajs.dom.{Element, Node}

import scalajs.js
import scalajs.js.annotation.JSExport
import dom.html
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.{HTMLDocument, HTMLElement, Window}
//import parser.DiaObject
import scalatags.JsDom.all._


//import com.karasiq.bootstrap.Bootstrap.default._
//import scalaTags.all._


import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import dom.ext._
import scala.runtime.Nothing$
import scala.scalajs
.concurrent
.JSExecutionContext
.Implicits
.runNow
//import scala.scalajs.js.typedarray.ArrayBuffer


@JSExport
object diaview3 extends {
  var objMap = new ArrayBuffer[mutable.HashMap[String, String]]()
  var lineMap = new ArrayBuffer[mutable.HashMap[String, String]]()
  var myUrl : String = ""
  var winRef : Window =null
  val docBase = "http://localhost:12345/target/scala-2.11/classes/"

  @JSExport
  def main(target: html.Div, canvas: html.Canvas , url :String): Unit = {




    val brush =
      canvas.getContext("2d")
        .asInstanceOf[dom.CanvasRenderingContext2D]
    canvas.width = canvas.parentElement.clientWidth
    canvas.height = canvas.parentElement.clientHeight

// Important to initialize
    brush.fillStyle = "white"
    brush.fillRect(0, 0, canvas.width, canvas.height)

    /*val url =
      s"http://192.168.1.222:7070/" +
        s"/ea/diagram/getDiagramInfoData.do?PROD_ID=$prodId"*/
    myUrl = url

    ///////////////////////////////////////////////
    Ajax.get(url).onSuccess { case xhr =>

      val parser = new DOMParser()
      val doc = parser.parseFromString(xhr.responseText, "application/xml")
      ///////////////////////////////Draw Object
      var objList = doc.getElementsByTagName("DIA_OBJECT") ++ doc.getElementsByTagName("DIA_NOTE")
      for (node <- objList) {
        //for(i <- 0 to node.childNodes.length-1)  println(i + ":" + node.childNodes(i).nodeName + ":" + node.childNodes(i).textContent)
        val diaobj = new mutable.HashMap[String, String]()
        for (child <- node.childNodes) diaobj.put(child.nodeName, child.textContent)
        objMap += diaobj
        //println(">>>>>"  + diaobj.obj_id +":" +  diaobj.obj_nm)
        // println(">>>>>" + i + ">>>" + node.childNodes(i))
      } //for
      for (diaobj <- objMap) {
        println("##############")
        for (key <- diaobj.keys)
          println("         "  + key + ":" + diaobj(key))
        DrawObject(target,diaobj)
      }
      /////////////////////////////////Draw Line
      var lineList = doc.getElementsByTagName("DIA_NOTE_LINE")
      for (node <- lineList) {
        //for(i <- 0 to node.childNodes.length-1)  println(i + ":" + node.childNodes(i).nodeName + ":" + node.childNodes(i).textContent)
        val lineobj = new mutable.HashMap[String, String]()
        for (child <- node.childNodes) lineobj.put(child.nodeName, child.textContent)
        lineMap += lineobj
        //println(">>>>>"  + diaobj.obj_id +":" +  diaobj.obj_nm)
        // println(">>>>>" + i + ">>>" + node.childNodes(i))
      } //for
      for (lineobj <- lineMap) {
        println("*************")
        for (key <- lineobj.keys)
          println("         "  + key + ":" + lineobj(key))
        DrawLineObject(brush,lineobj)
      }
        ////////////////////////////////
    } //onsuccess
  } //main

  def ShowPopup(objNm:String , objId : String) = {

    //import scalatags.JsDom.all._

    /*val url =
      s"http://192.168.1.222:7070/ea/diagram/getDiagramInfoData.do?PROD_ID=$prodId"*/
    // extract web base url
      if (winRef != null ) winRef.close()
      winRef = dom.window.open(docBase + "test/show-popup.html","SHOW_DIALOG","width=500;height=300")
      winRef.focus()
      //if (winRef.document.cl)

      val tabList = List(("tab1","London"), ("tab2","Paris"), ("tab3", "Tokyo"))
      for (tabOne <- tabList) {
        val tabs = winRef.document.getElementById(tabOne._1).asInstanceOf[html.Div]
        tabs.onclick = (e: dom.Event) => {
            //winRef.window.alert(e.toString)
          for (link <- winRef.document.getElementsByClassName("tablink"))
            link.asInstanceOf[html.Button].className = link.asInstanceOf[html.Button].className.replace("w3-blue","")
            e.currentTarget.asInstanceOf[html.Button].className += " w3-blue"
            ShowTab(objNm, objId, winRef, "cont", e.currentTarget.asInstanceOf[html.Button])
          }
      }
      //ShowTab(objId, winRef.document.getElementById(tabOne._1))
      //DrawObject(target, diaobj)
  } //showPopup


  def ShowTab(objNm : String, objId : String, win: Window, cont : String, tab : html.Button) : Unit = {

    val urlBase = s"http://192.168.1.222:7070/ea/object/getObjectInfoListData.do?OBJ_ID=$objId"
    ///////////////////////////////////////////////
    Ajax.get(urlBase).onSuccess { case xhr =>

      var attrMap = new ArrayBuffer[mutable.HashMap[String, String]]()
      var linkMap = new ArrayBuffer[mutable.HashMap[String, String]]()
      val parser = new DOMParser()
      val doc = parser.parseFromString(xhr.responseText, "application/xml")
      val target = win.document.getElementById(cont)
      ///////////////////////////////Draw Object/////////////////////////////////
      tab.id match {
        case "tab1" => {
          //winRef.window.alert("tab2")
          var popList = doc.getElementsByTagName("EA_OBJ_ATTR")
          for (pop <- popList) {
            val objAttr = new mutable.HashMap[String, String]()
            for (child <- pop.childNodes) objAttr.put(child.nodeName, child.textContent)
            attrMap += objAttr
            //println(">>>>>"  + diaobj.obj_id +":" +  diaobj.obj_nm)
            // println(">>>>>" + i + ">>>" + node.childNodes(i))
          }

          target.innerHTML = div(h3(style := "align:center", objNm),
            table(cls := "w3-table w3-striped",
              for (attrNode <- attrMap; key <- attrNode.keys)
                yield tr(th(width := 250, attrNode("ATTR_NM")), td(attrNode("ATTR_VAL")))
            )
          ).toString()
        }
        case "tab2" => {
          //winRef.window.alert("tab2")
          var popList = doc.getElementsByTagName("EA_OBJ_REL")
          for (pop <- popList) {
            val objAttr = new mutable.HashMap[String, String]()
            for (child <- pop.childNodes) objAttr.put(child.nodeName, child.textContent)
            linkMap += objAttr
            //println(">>>>>"  + diaobj.obj_id +":" +  diaobj.obj_nm)
            // println(">>>>>" + i + ">>>" + node.childNodes(i))
          }


          target.innerHTML = div(h3(style := "align:center", objNm),
            table(cls := "w3-table w3-striped",
              for (linkNode <- linkMap; key <- linkNode.keys)
                yield tr(th(width := 250, linkNode("CLASS_NM")),
                      td(cls := "OBJ_LINK", linkNode("OBJ_NM"), data("objNm") := linkNode("OBJ_NM"), data("objId") := linkNode("OBJ_ID")))
            )
          ).toString()

          val tdList = target.getElementsByClassName("OBJ_LINK")
          for (tdOne <- tdList) {
            println("tdOne"+tdOne.attributes.toString)
            val tdCell = tdOne.asInstanceOf[html.Element]
            tdCell.onclick = (e : dom.Event) => {
              ShowPopup("OBJ_NM","OBJ_ID")
              }
            tdCell.render
          }
        }
        case "tab3" => { target.innerHTML = "" }
      } //match
       // target.
    } //Ajax

  }


  def DrawObject(target : html.Div,  obj : mutable.HashMap[String,String]): Unit = {
    //brush.font = obj("FONT_NM")
    //brush.textAlign = obj("NM_ALIGN")
    //brush.lineWidth = obj("LINE_THICK").toDouble

    var xy1 = obj("GEOMT_VERTEX").split("\\|")(0)
    var xy2 = obj("GEOMT_VERTEX").split("\\|")(2)
    var x1 = xy1.split(",")(0).toInt ;   var y1 = xy1.split(",")(1).toInt
    var x2 = xy2.split(",")(0).toInt ;   var y2 = xy2.split(",")(1).toInt
    var (w,h) = (x2-x1, y2-y1)
    println(x1+" "+ y1+" "+x2+" "+y2)

    var bgColorStyle = "#FFFFFF"
    try {
      var bgcolor = obj("FILL_COLOR").toInt.toHexString //FFBBGGRR => #RRGGBB
      bgColorStyle = "#" + bgcolor(2) + bgcolor(3) + bgcolor(4) + bgcolor(5) + bgcolor(6) + bgcolor(7)
    } catch  {
      case e =>
        bgColorStyle = "#FFFFFF"
    }

  //draw text

    var fontColorStyle = "#000000"
    try {
      var fontColor = obj("FONT_COLOR").toInt.toHexString //FFBBGGRR => #RRGGBB
      fontColorStyle = "#" + fontColor(2) + fontColor(3) + fontColor(4) + fontColor(5) + fontColor(6) + fontColor(7)
    } catch {
      case e =>
        fontColorStyle = "#000000"
    }

    var lineColorStyle = "Transparent"
    try {
      var lineColor = obj("LINE_COLOR").toInt.toHexString //FFBBGGRR => #RRGGBB
      lineColorStyle = "#" + lineColor(2) + lineColor(3) + lineColor(4) + lineColor(5) + lineColor(6) + lineColor(7)
    } catch {
      case e =>
        lineColorStyle = "Transparent"
    }

    var fs = obj("FONT_SIZE").toInt

    //check symbol image
    //var symimg: html.Div
    var sym_img = obj("SYMB_NM")

    if (!sym_img.isEmpty) {
        val diadiv = div(position := "absolute"
          //,`type` := obj("TYPE")
          ,left := x1, top := y1, width := w, height := h
          ,borderStyle := "solid"
          ,borderWidth := obj("LINE_THICK") + "px"
          ,borderColor := lineColorStyle
          , backgroundColor := bgColorStyle
          ,img(src := sym_img, position:= "absolute", top := 0, width := w, height := h)
          , p(obj("OBJ_NM")
            , textAlign := "center"
            , verticalAlign := "center"
            , fontSize := obj("FONT_SIZE") + "px"
            , fontFamily := obj("FONT_NM")
            , position := "relative"
            , top := h / 2, width := w, height := h, margin := -fs
          )
        ).render
        diadiv.onmousedown = (e: dom.Event) => {
          ShowPopup(obj("OBJ_NM"),obj("OBJ_ID"))
        }
       target.appendChild(diadiv)

    } else {
        val diadiv = div(position := "absolute"
          //,`type` := obj("TYPE")
          ,left := x1, top := y1, width := w, height := h
          ,borderStyle := "solid"
          ,borderWidth := obj("LINE_THICK") + "px"
          ,borderColor := lineColorStyle
          , backgroundColor := bgColorStyle
          , p(obj("OBJ_NM")
            , textAlign := "center"
            , verticalAlign := "center"
            , fontSize := obj("FONT_SIZE") + "px"
            , fontFamily := obj("FONT_NM")
            , position := "relative"
            , top := h / 2, width := w, height := h, margin := -fs / 2
          )
        ).render
      diadiv.onmousedown = (e: dom.Event) => {
          //dom.window.alert(obj("OBJ_NM") + ":" + obj("OBJ_ID"));
          ShowPopup(obj("OBJ_NM"),obj("OBJ_ID"))
      }
      target.appendChild(diadiv)
    }
  } // DrawObject

  def DrawLineObject(brush : dom.CanvasRenderingContext2D, obj : mutable.HashMap[String,String]): Unit = {

  import org.scalajs.dom.html.Div
  //brush.font = obj("FONT_NM")
    //brush.textAlign = obj("NM_ALIGN")
    //brush.lineWidth = obj("LINE_THICK").toDouble
    var xy1 = obj("GEOMT_VERTEX").split("\\|")(0)
    var xy2 = obj("GEOMT_VERTEX").split("\\|")(1)
    var x1 = xy1.split(",")(0).toInt ;   var y1 = xy1.split(",")(1).toInt
    var x2 = xy2.split(",")(0).toInt ;   var y2 = xy2.split(",")(1).toInt

    var bgColorStyle = "#FFFF00"
    try {0
      var bgcolor = obj("LINE_COLOR").toInt.toHexString //FFBBGGRR => #RRGGBB
      bgColorStyle = "#" + bgcolor(2) + bgcolor(3) + bgcolor(4) + bgcolor(5) + bgcolor(6) + bgcolor(7)
    } catch  {
      case e =>
        bgColorStyle = "#FFFF00"
    }

    //draw line to canvas
    brush.strokeStyle = "black"
    brush.lineWidth=obj("LINE_THICK").toInt
    brush.beginPath()
    brush.moveTo(x1,y1)
    brush.lineTo(x2,y2)
    brush.stroke()

  } // DrawObject




} //diaview