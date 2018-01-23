package older.parser

/**
  * Created by Administrator on 2017-04-24.
  */

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
object popupview4 extends {
  var objMap = new ArrayBuffer[mutable.HashMap[String, String]]()
  var lineMap = new ArrayBuffer[mutable.HashMap[String, String]]()
  var myUrl : String = ""
  var winRef : Window =null
  val docBase = "http://localhost:12345/target/scala-2.11/classes/"

  @JSExport
  def main(target: html.Div): Unit = {

    var paraMap = new mutable.HashMap[String, String]
    var objNm, ibjId : String = ""

    println(dom.window.location.search)
    val query = dom.window.location.search.substring(1).split("&")
    //loc.sea   .search.substring(1).split("&")
    for (q <- query) {
      var param = q.split("=")
      paraMap.put(param(0),param(1))
      println(param(0)+" : " +param(1))
    }
    ShowTab("Unknown", paraMap("objId"), "tab1")


  } //main

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



      tab  match {
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

          //OVERFLOW window size 보다 작게해여 스크롤바가 생김다.
          val height = dom.window.screen.height
          println(height)
          target.innerHTML = div(h3(objMap("OBJ_NM")),
            div(style:="height:80vh;overflow-y: auto;",table( cls := "w3-table-all",
              for (attrNode <- attrMap; key <- attrNode.keys)
                yield tr(th(width := 250, attrNode("ATTR_NM")), td(attrNode("ATTR_VAL")))
            ))
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


          target.innerHTML = div(h3(objMap("OBJ_NM")),
            div(style:="height:80vh;overflow-y: auto;",table(cls := "w3-table w3-striped",
              for (attrNode <- linkMap; key <- attrNode.keys)
                yield tr(th(width := 250, attrNode("CLASS_NM")),
                      td(a(cls:="OBJ_LINK", href:="_blank", data("objId"):= attrNode("OBJ_ID"), attrNode("OBJ_NM")))
                )
            ))
          ).toString()

          val tdList = target.getElementsByClassName("OBJ_LINK")
          for (tdOne <- tdList) {
            //println("tdOne"+tdOne.attributes.toString)
            val tdCell = tdOne.asInstanceOf[html.Element]
            tdCell.onclick = (e : dom.Event) => {
              e.preventDefault()
              ShowTab(objMap("OBJ_NM"),  tdCell.getAttribute("data-objId"), "tab1")
            }
          }
        }
        case "tab3" => {           //winRef.window.alert("tab2")
          var popList = doc.getElementsByTagName("EA_OBJ_PROD")
          for (pop <- popList) {
            val objAttr = new mutable.HashMap[String, String]()
            for (child <- pop.childNodes) objAttr.put(child.nodeName, child.textContent)
            prodMap += objAttr
            //println(">>>>>"  + diaobj.obj_id +":" +  diaobj.obj_nm)
            // println(">>>>>" + i + ">>>" + node.childNodes(i))
          }


          target.innerHTML = div(h3(style := "align:center", objMap("OBJ_NM")),
            div(style:="height:80vh;overflow-y: auto;",table(cls := "w3-table w3-striped",
              for (linkNode <- prodMap; key <- linkNode.keys)
                yield tr(th(width := 250, linkNode("PROD_TP_NM")),
                  td(cls := "OBJ_PROD", linkNode("PROD_NM"), attr("data-prodId") := linkNode("PROD_ID")))
            ))
          ).toString()

          val tdList = target.getElementsByClassName("OBJ_PROD")
          for (tdOne <- tdList) {
            //println("tdOne"+tdOne.attributes.toString)
            val tdCell = tdOne.asInstanceOf[html.Element]
            tdCell.onclick = (e : dom.Event) => {
              //ShowTab(objMap("OBJ_NM"),  tdCell.getAttribute("data-prodId"), "tab1")
            }
          }
        }
      } //match
      // target.
    } //Ajax

  } //showTab






}
