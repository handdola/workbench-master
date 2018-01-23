package lib

import javax.swing.text.Highlighter.Highlight

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

/**
  * Created by Administrator on 2017-10-10.
  */
object rightOpts {
  var selClsName = "awi-data"
  var selTab :String = null
  var selPageId = ""
  var selPage : html.Div = null
  var selElement : html.Element = null
  var objectTab : html.Div = null

  var isMouseDown : Boolean = false
  var offsetX = 0.0
  var offsetY = 0.0

  def initOpts(Object : html.Div, Tabs : html.Div, Content: html.Div,newPage:html.Div,focused:String, newPageId: String =""): Unit = {

    // check vaild call
    if (Object == null) return
println("focused",focused)
    objectTab = Object

    //if (selPageId.equals(newPageId)) return
    
    //init static variable
    selPageId = newPageId
    selPage = newPage

    /****** not yet implement *********
    val newPage = dom.window.document.getElementById(selPageId).asInstanceOf[html.Div] */
    
    
    //while (Tabs.firstChild != null) Tabs.removeChild(Tabs.firstChild)
    //find roghtOpts
    val optRight = dom.window.document.getElementById("kj-right").asInstanceOf[html.Div]
    optRight.className = optRight.className.replaceAll("w3-hide","")

    val optBar = dom.window.document.getElementById("kj-right-barid").asInstanceOf[html.Div]
    optBar.onmousedown = (e:dom.MouseEvent) => {
      println("mouse down")
      isMouseDown=true
      offsetX = optRight.offsetLeft - e.clientX
      offsetY = optRight.offsetTop - e.clientY
    }
    optBar.onmouseup = (e:dom.MouseEvent) => {
      println("mouse up")
      isMouseDown=false
    }
    optBar.onmousemove = (e:dom.MouseEvent) => {
      e.preventDefault()
      if (isMouseDown) {
        println("mouse moved")
        optRight.style.left = (e.clientX + offsetX) + "px"
        optRight.style.top = (e.clientY + offsetY) + "px"
      }
    }
    optBar.onmouseleave = (e:dom.MouseEvent) => {
      e.preventDefault()
      if (isMouseDown) {
        println("mouse leave")
        isMouseDown=false
        optRight.style.left = (e.clientX + offsetX) + "px"
        optRight.style.top = (e.clientY + offsetY) + "px"
      }
    }

    //Find Tabs Content
    val Style = Tabs.getElementsByClassName("kj-right-style")(0).asInstanceOf[html.Button]
    val Option = Tabs.getElementsByClassName("kj-right-option")(0).asInstanceOf[html.Button]
    val Animate = Tabs.getElementsByClassName("kj-right-animate")(0).asInstanceOf[html.Button]
    Style.onclick = (e:dom.Event) => {
      changeActiveTool(Tabs,Style)
      changeFillContent(selClsName,"kj-right-style",Content,newPage,Tabs)
    }
    Option.onclick = (e:dom.Event) => {
      changeActiveTool(Tabs,Option)
      changeFillContent(selClsName,"kj-right-option",Content,newPage,Tabs)
    }
    Animate.onclick = (e:dom.Event) => {
      changeActiveTool(Tabs,Animate)
      changeFillContent(selClsName,"kj-right-animate",Content,newPage,Tabs)
    }

    //Create Object Buttons
    while (Object.firstChild != null) Object.removeChild(Object.firstChild)
    val pageTypes = newPage.getElementsByClassName("awi-data")(0).asInstanceOf[html.Div].className


    // end of Object

    if (selTab == null) selTab = "kj-right-style"
    //append compomnent button for each pagetypes
    if (pageTypes.contains("awi-flip-page")) {
      if (focused != null) selClsName = focused
      else selClsName = "kj-flip-val"

      // Container for all
      val container = button(cls:="tab-awi-data w3-bar-item tabobj w3-small","Flip Box").render
      Object.appendChild(container)
      container.onclick = (e:dom.Event) => {
        //change active
        changeObjectTab(Object,container)
        changeFillContent("awi-flip-page",selTab,Content,newPage,Tabs)
      }
      //container.onmousedown = (e:dom.Event) => HighlightOn(newPage,"awi-flip-page")
      //container.onmouseup = (e:dom.Event) => HighlightOff(newPage,"awi-flip-page")

      val choiceButton = button(cls:="tab-kj-flip-val w3-bar-item tabobj w3-small", "Action Button").render
      Object.appendChild(choiceButton)
      choiceButton.onclick = (e:dom.Event) => {
        changeObjectTab(Object,choiceButton)
        changeFillContent("kj-flip-val",selTab,Content,newPage,Tabs)
      }
      //choiceButton.onmousedown = (e:dom.Event) => choiceButton.className += " w3-border"
      //choiceButton.onmouseup = (e:dom.Event) => choiceButton.className = choiceButton.className.replace(" w3-border","")

      val resImage = button(cls:="tab-awi-flip-image w3-bar-item tabobj w3-small", "Result Image").render
      Object.appendChild(resImage)
      resImage.onclick = (e:dom.Event) => {
        changeObjectTab(Object,resImage)
        changeFillContent("awi-flip-image",selTab,Content,newPage,Tabs)
      }

      val resExp = button(cls:="tab-awi-editor w3-bar-item tabobj w3-small", "Result Text").render
      Object.appendChild(resExp)
      resExp.onclick = (e:dom.Event) => {
        changeObjectTab(Object,resExp)
        changeFillContent("awi-editor",selTab,Content,newPage,Tabs)
      }

    } else if (pageTypes.contains("awi-image-page")) {
      if (focused != null) selClsName = focused
      else selClsName = "awi-image"
      //selTab = "kj-right-style"
      // Container for all
      val container = button(cls:="tab-awi-data w3-bar-item tabobj w3-small", "Multimedia Box").render
      Object.appendChild(container)

      val resImage = button(cls:="tab-awi-image w3-bar-item tabobj w3-small", "Image").render

      Object.appendChild(resImage)

      container.onclick = (e:dom.Event) => {
        //change active
        changeObjectTab(Object,container)
        changeFillContent("awi-image-page",selTab,Content,newPage,Tabs)
      }

      resImage.onclick = (e:dom.Event) => {
        changeObjectTab(Object,resImage)
        changeFillContent("awi-image",selTab,Content,newPage,Tabs)
      }

    } else if (pageTypes.contains("awi-text-page")) {
      if (focused != null) selClsName = focused
      else selClsName = "awi-text-page"
      //selTab = "kj-right-style"
      val container = button(cls:="tab-awi-data w3-bar-item tabobj w3-small", "Text Box").render
      Object.appendChild(container)
      container.onclick = (e:dom.Event) => {
        //change active
        changeObjectTab(Object,container)
        changeFillContent("awi-text-page",selTab,Content,newPage,Tabs)
      }


    } else if (pageTypes.contains("awi-option-page")) {
      if (focused != null) selClsName = focused
      else selClsName = "kj-option-val"
      //selTab = "kj-right-style"
      val container = button(cls:="tab-awi-data w3-bar-item tabobj  w3-small", "Action Box").render
      Object.appendChild(container)
      container.onclick = (e:dom.Event) => {
        //change active
        changeObjectTab(Object,container)
        changeFillContent("awi-option-page",selTab,Content,newPage,Tabs)
      }


      val choiceButton = button(cls:="tab-kj-option-val w3-bar-item tabobj w3-small", "Action Button").render
      Object.appendChild(choiceButton)
      choiceButton.onclick = (e:dom.Event) => {
        changeObjectTab(Object,choiceButton)
        changeFillContent("kj-option-val",selTab,Content,newPage,Tabs)
      }
      //choiceButton.onmousedown = (e:dom.Event) => choiceButton.className += " w3-border"
      //choiceButton.onmouseup = (e:dom.Event) => choiceButton.className = choiceButton.className.replace(" w3-border","")

    } else if (pageTypes.contains("awi-typecreate-page")) {
      if (focused != null) selClsName = focused
      else selClsName = "kj-type-val"
      //selTab = "kj-right-style"
      val container = button(cls:="tab-awi-data w3-bar-item tabobj  w3-small", "Type Box").render
      Object.appendChild(container)
      container.onclick = (e:dom.Event) => {
        //change active
        changeObjectTab(Object,container)
        changeFillContent("awi-typecreate-page",selTab,Content,newPage,Tabs)
      }
      val choiceButton = button(cls:="tab-kj-type-val w3-bar-item tabobj w3-small", "Choice Button").render
      Object.appendChild(choiceButton)
      choiceButton.onclick = (e:dom.Event) => {
        changeObjectTab(Object,choiceButton)
        changeFillContent("kj-type-val",selTab,Content,newPage,Tabs)
      }
      //choiceButton.onmousedown = (e:dom.Event) => choiceButton.className += " w3-border"
      //choiceButton.onmouseup = (e:dom.Event) => choiceButton.className = choiceButton.className.replace(" w3-border","")
    } else if (pageTypes.contains("awi-typetest-page")) {
      if (focused != null) selClsName = focused
      else selClsName = "kj-map-val"
      //selTab = "kj-right-style"
      val container = button(cls:="tab-awi-data w3-bar-item tabobj  w3-small", "Choice Box").render
      Object.appendChild(container)
      container.onclick = (e:dom.Event) => {
        //change active
        changeObjectTab(Object,container)
        changeFillContent("awi-typetest-page",selTab,Content,newPage,Tabs)
      }
      val choiceButton = button(cls:="tab-kj-map-val w3-bar-item tabobj w3-small", "Choice Button").render
      Object.appendChild(choiceButton)
      choiceButton.onclick = (e:dom.Event) => {
        changeObjectTab(Object,choiceButton)
        changeFillContent("kj-map-val",selTab,Content,newPage,Tabs)
      }
      //choiceButton.onmousedown = (e:dom.Event) => choiceButton.className += " w3-border"
      //choiceButton.onmouseup = (e:dom.Event) => choiceButton.className = choiceButton.className.replace(" w3-border","")
    } else if (pageTypes.contains("awi-typeres-page")) {
      if (focused != null) selClsName = focused
      else selClsName = "kj-chart-result"
      //selTab = "kj-right-style"
      val container = button(cls:="tab-awi-data w3-bar-item tabobj w3-small", "Test result").render
      Object.appendChild(container)
      container.onclick = (e:dom.Event) => {
        //change active
        changeObjectTab(Object,container)
        changeFillContent("awi-typeres-page",selTab,Content,newPage,Tabs)
      }
      val resChart = button(cls:="w3-bar-item tabobj w3-small", "Result Chart").render
      Object.appendChild(resChart)
      resChart.onclick = (e:dom.Event) => {
        changeObjectTab(Object,resChart)
        changeFillContent("kj-chart-result",selTab,Content,newPage,Tabs)
      }
    } else if (pageTypes.contains("awi-rule-page")) {
      if (focused != null) selClsName = focused
      else selClsName = "awi-rule-page"
      //selTab = "kj-right-style"
      val container = button(cls:="tab-kj-chart-result w3-bar-item tabobj w3-small", "Rule Box").render
      Object.appendChild(container)
      container.onclick = (e:dom.Event) => {
        //change active
        changeObjectTab(Object,container)
        changeFillContent("awi-rule-page",selTab,Content,newPage,Tabs)
      }

    }

    //initialize Content Dialog
    initFillContent(selClsName,selTab,Content,newPage,Object,Tabs)

  }



  /*----------------- Object / ActiveTool / Size Tab Bar Management -----------------------*/
  def changeObjectTab(Object : html.Div,current : html.Element): Unit =
  {
    val tabs = Object.getElementsByClassName("tabobj")
    println("change obj tab",tabs.length,Object.className,current.className)
    for (i <- 0 until tabs.length) {
      tabs(i).asInstanceOf[html.Element].className = tabs(i).asInstanceOf[html.Element].className.replaceAll("w3-teal", "")
      tabs(i).asInstanceOf[html.Element].className += " w3-grey"
    }
    current.className = current.className.replaceAll(" w3-grey", "w3-teal")

  }
  def changeActiveTool(Object : html.Div,current : html.Element): Unit =
  {
    val tabs = Object.getElementsByClassName("tablink")
    println("change tab",tabs.length)
    for (i <- 0 until tabs.length)
      tabs(i).asInstanceOf[html.Element].className = tabs(i).asInstanceOf[html.Element].className.replaceAll("w3-teal", "w3-grey")
    current.className = current.className.replaceAll("w3-grey","w3-teal")

  }

  // Content Tab init
  def initFillContent(ClsName : String, newTab : String , Content:html.Div,newPage : html.Div, Object:html.Div ,Tabs : html.Div) = {

    while (Content.firstChild != null) Content.removeChild(Content.firstChild)
    println("current tab:",ClsName,Object,newTab)
    /*----init Object Tab --*/
    val activeObj = Object.getElementsByClassName("tab-" + ClsName)
    println("current tab:",activeObj.length,ClsName,Object,newTab)
    if (activeObj.length>0) activeObj(0).asInstanceOf[html.Button].className += " w3-teal"
    else Object.getElementsByClassName("tab-" + "awi-data")(0).asInstanceOf[html.Button].className += " w3-teal"
    /*---------------*/

    //change object tab
    newTab match {
      case "kj-right-style" => newFillStyle (ClsName,Content)
      case "kj-right-option" => newFillOption(ClsName,Content)
      //case "kj-right-animate" => newFillAnimate(newObj,Content)
    }

  }

  // Content Tab Change
  def changeFillContent(ClsName : String, newTab : String , Content:html.Div,newPage : html.Div, Tabs : html.Div): Unit = {

    //if (newObj == selClsName && newTab == selTab) return
    println("chane fill",ClsName,newTab)
    while (Content.firstChild != null) Content.removeChild(Content.firstChild)
    selClsName = ClsName
    selTab = newTab
    newTab match {
      case "kj-right-style" => newFillStyle (ClsName,Content)
      case "kj-right-option" => newFillOption(ClsName,Content)
      //case "kj-right-animate" => newFillAnimate(ClsName,Content)
      case _ => {}
    }

  }

  //Style Tab Management
  def newFillStyle (clsName:String,Content:html.Div) = {
    println("new fill",clsName,selPage.className)
    val Elements = selPage.getElementsByClassName(clsName)
    println("new fill",Elements.length,clsName,"class:",selPage.className)
    selElement = Elements(0).asInstanceOf[html.Element]


    val lyWidth = input(cls:=" w3-half w3-border ",placeholder:="100").render
    //val lyHeight = input(cls:=" w3-half w3-border ",placeholder:="120px").render
    //val lyMargin = input(cls:=" w3-half w3-border ",placeholder:="12px").render
    val lyPadding = input(cls:=" w3-half w3-border ",placeholder:="10").render
    //------ backgroud color ----------------//
    val lyAlign = select(cls:="kj-layout-prop w3-half",
      option(value:="",disabled,selected,"Choose Object Alignment"),
      option(cls:="w3-layout-none", value:="lyAlign w3-none","None"),
      option(cls:="w3-layout-left", value:="lyAlign w3-left","Left"),
      option(cls:="w3-layout-center", value:="lyAlign w3-center","Center"),
      option(cls:="w3-layout-right", value:="lyAlign w3-right","Right")
    ).render

    //------ backgroud color ----------------//
    val bgColor = select(cls:="w3-half",
                  option(value:="",disabled,selected,"Choose backgroud color"),
                  option(cls:="w3-white", value:="w3-white","white"),
                  option(cls:="w3-red", value:="w3-red","red"),
                  option(cls:="w3-pink",value:="w3-pink","pink"),
                  option(cls:="w3-orange",value:="w3-orange","orange"),
                  option(cls:="w3-yellow",value:="w3-yellow","yellow"),
                  option(cls:="w3-green",value:="w3-green","green"),
                  option(cls:="w3-teal",value:="w3-teal","teal"),
                  option(cls:="w3-cyan",value:="w3-cyan","cyan"),
                  option(cls:="w3-lime",value:="w3-lime","lime"),
                  option(cls:="w3-blue",value:="w3-blue","blue"),
                  option(cls:="w3-indigo",value:="w3-indigo","indigo"),
                  option(cls:="w3-purple",value:="w3-purple","purple"),
                  option(cls:="w3-khaki",value:="w3-khaki","khaki"),
                  option(cls:="w3-black",value:="w3-black","black"),
                  option(cls:="w3-brown",value:="w3-brown","brown"),
                  option(cls:="w3-gray",value:="w3-gray","gray"),
                  option(cls:="w3-light-gray",value:="w3-light-gray","light gray")
    ).render

    //------ font color ----------------//
    val ftColor = select(cls:="w3-half",
      option(value:="",disabled,selected,"Choose font color"),
      option(cls:="w3-text-white", value:="w3-text-white","white"),
      option(cls:="w3-text-red", value:="w3-text-red","red"),
      option(cls:="w3-text-pink",value:="w3-text-pink","pink"),
      option(cls:="w3-text-orange",value:="w3-text-orange","orange"),
      option(cls:="w3-text-yellow",value:="w3-text-yellow","yellow"),
      option(cls:="w3-text-green",value:="w3-text-green","green"),
      option(cls:="w3-text-teal",value:="w3-text-teal","teal"),
      option(cls:="w3-text-cyan",value:="w3-text-cyan","cyan"),
      option(cls:="w3-text-lime",value:="w3-text-lime","lime"),
      option(cls:="w3-text-blue",value:="w3-text-blue","blue"),
      option(cls:="w3-text-indigo",value:="w3-text-indigo","indigo"),
      option(cls:="w3-text-purple",value:="w3-text-purple","purple"),
      option(cls:="w3-text-khaki",value:="w3-text-khaki","khaki"),
      option(cls:="w3-text-black",value:="w3-text-black","black"),
      option(cls:="w3-text-brown",value:="w3-text-brown","brown"),
      option(cls:="w3-text-gray",value:="w3-text-gray","gray"),
      option(cls:="w3-text-light-gray",value:="w3-text-light-gray","light gray")
    ).render

    val ftSize = select(cls:="w3-half",
      option(value:="",disabled,selected,"Choose font size"),
      option(value:="w3-tiny", "tiny"),
      option(value:="w3-small","small"),
      option(value:="w3-medium","medium"),
      option(value:="w3-large","large"),
      option(value:="w3-xlarge","xlarge"),
      option(value:="w3-xxlarge","xxlarge"),
      option(value:="w3-xxxlarge","xxxlarge"),
      option(value:="w3-jumbo","jumbo")
    ).render

    val ftFamily = select(cls:="w3-half",
      option(value:="",disabled,selected,"Choose font family"),
      option(value:="w3-tiny", "tiny"),
      option(value:="w3-small","small"),
      option(value:="w3-medium","medium"),
      option(value:="w3-large","large"),
      option(value:="w3-xlarge","xlarge")
    ).render


    val layout = div(cls:="w3-row ",
      div(cls:="w3-row w3-padding-small",label(cls:="w3-third w3-right-align","Width"),lyWidth," %"),
      //div(cls:="w3-row w3-padding-small",label(cls:="w3-third w3-right-align","Height"),lyHeight),
      //div(cls:="w3-row w3-padding-small",label(cls:="w3-third w3-right-align","Margin"),lyMargin),
      div(cls:="w3-row w3-padding-small",label(cls:="w3-third w3-right-align","Padding"),lyPadding," px"),
      div(cls:="w3-row w3-padding-small",label(cls:="w3-third w3-right-align","Alignment"),lyAlign)
    ).render

    val bg = div(cls:="w3-row w3-padding-small",
      div(cls:="w3-row w3-padding-small",label(cls:="w3-third w3-right-align","Bg Color"),bgColor)).render
    val font = div(cls:="w3-row w3-padding-small",
      div(cls:="w3-row w3-padding-small",label(cls:="w3-third w3-right-align","Font Color"),ftColor),
      div(cls:="w3-row w3-padding-small",label(cls:="w3-third w3-right-align","Font Size"),ftSize),
      div(cls:="w3-row w3-padding-small",label(cls:="w3-third w3-right-align","Font Family"),ftFamily)).render

    //----------- Style Init and Event Handling ------------------------------//

    val oldWidth = selElement.getAttribute("data-style-width")
    if (oldWidth!=null) lyWidth.value = oldWidth
    lyWidth.onchange = (e:dom.Event) => {

      for (i <- 0 until Elements.length) {
        val nodeOne = Elements(i).asInstanceOf[html.Element]
        nodeOne.style.width = e.target.asInstanceOf[html.Input].value + "%"
        nodeOne.setAttribute("data-style-width",e.target.asInstanceOf[html.Input].value)
        //ftColor.className = e.target.asInstanceOf[html.Input].value
      }
    }

    val oldPadding = selElement.getAttribute("data-style-padding")
    if (oldPadding!=null) lyPadding.value = oldPadding
    lyPadding.onchange = (e:dom.Event) => {

      for (i <- 0 until Elements.length) {
        val nodeOne = Elements(i).asInstanceOf[html.Element]
        nodeOne.style.padding = e.target.asInstanceOf[html.Input].value + "px"
        nodeOne.setAttribute("data-style-padding",e.target.asInstanceOf[html.Input].value)
        //ftColor.className = e.target.asInstanceOf[html.Input].value
      }
    }

    val oldAlign = selElement.getAttribute("data-style-lyAlign")
    if (oldAlign!=null) lyAlign.value = oldAlign

    lyAlign.onchange = (e:dom.Event) => {
      //delete old bg color
      for (i <- 0 until Elements.length) {
        val nodeOne = Elements(i).asInstanceOf[html.Element]
        val oldAlign = nodeOne.getAttribute("data-style-lyAlign")
        if (oldAlign!=null)
          nodeOne.className = nodeOne.className.replaceAll(oldAlign, "")
        //apply new bg color
        nodeOne.className += " " + e.target.asInstanceOf[html.Input].value
        nodeOne.setAttribute("data-style-lyAlign",e.target.asInstanceOf[html.Input].value)
        //ftColor.className = e.target.asInstanceOf[html.Input].value
      }
    }

    val oldBgColor = selElement.getAttribute("data-style-bgcolor")
    if (oldBgColor!=null) bgColor.value = oldBgColor

    bgColor.onchange = (e:dom.Event) => {
      //delete old bg color
      for (i <- 0 until Elements.length) {
        val nodeOne = Elements(i).asInstanceOf[html.Element]
        val oldBgColor = nodeOne.getAttribute("data-style-bgcolor")
        if (oldBgColor!=null)
          nodeOne.className = nodeOne.className.replaceAll(oldBgColor, "")
        //apply new bg color
        nodeOne.className += " " + e.target.asInstanceOf[html.Input].value
        nodeOne.setAttribute("data-style-bgcolor",e.target.asInstanceOf[html.Input].value)
        //ftColor.className = e.target.asInstanceOf[html.Input].value
      }
    }


    val oldFgColor = selElement.getAttribute("data-style-fgcolor")
    if (oldFgColor!=null) ftColor.value = oldFgColor

    ftColor.onchange = (e:dom.Event) => {
      //delete old bg color
      for (i <- 0 until Elements.length) {
        val nodeOne = Elements(i).asInstanceOf[html.Element]
        val oldFgColor = nodeOne.getAttribute("data-style-fgcolor")
        if (oldFgColor!=null)
          nodeOne.className = nodeOne.className.replaceAll(oldFgColor, "")
        //apply new bg color
        nodeOne.className += " " + e.target.asInstanceOf[html.Input].value
        nodeOne.setAttribute("data-style-fgcolor",e.target.asInstanceOf[html.Input].value)
        //bgColor.className = e.target.asInstanceOf[html.Input].value
      }
    }

    val oldFtSize = selElement.getAttribute("data-style-ftsize")
    if (oldFtSize!=null) ftSize.value = oldFtSize

    ftSize.onchange = (e:dom.Event) => {
      //delete old bg color
      for (i <- 0 until Elements.length) {
        val nodeOne = Elements(i).asInstanceOf[html.Element]
        val oldFtSize = nodeOne.getAttribute("data-style-ftsize")
        if (oldFtSize!=null)
          nodeOne.className = nodeOne.className.replaceAll(oldFtSize, "")
        //apply new bg color
        nodeOne.className += " " + e.target.asInstanceOf[html.Input].value
        nodeOne.setAttribute("data-style-ftsize",e.target.asInstanceOf[html.Input].value)
        //bgColor.className = e.target.asInstanceOf[html.Input].value
      }
    }



    // Final Append to Content

    Content.appendChild(layout)
    Content.appendChild(bg)
    Content.appendChild(font)
  }
  //Style Tab Management
  def newFillOption (clsName:String,Content:html.Div) = {
    println("new fill option",clsName,selPage.className)
    val Elements = selPage.getElementsByClassName(clsName)
    println("new fill option",Elements.length,clsName,"class:",selPage.className)
    selElement = Elements(0).asInstanceOf[html.Element]


    //------ btn alignment color ----------------//
    val btnAlign = select(cls:="kj-button-prop w3-half",
      option(value:="",disabled,selected,"Choose Button Align"),
      option(cls:="w3-default-align", value:="w3-default-align","Center"),
      option(cls:="w3-center-left", value:="w3-left-align","Left"),
      option(cls:="w3-center-right", value:="w3-right-align","Right")
    ).render

    //------ image option ----------------//
    val imgShape = select(cls:="kj-image-prop w3-half",
      option(value:="",disabled,selected,"Choose Image Shape"),
      option(cls:="w3-image-none", value:="imageShape w3-none","None"),
      option(cls:="w3-image-round", value:="imageShape w3-round-small","Round Small"),
      option(cls:="w3-image-round", value:="imageShape w3-round","Round"),
      option(cls:="w3-image-round", value:="imageShape w3-round-large","Round Large"),
      option(cls:="w3-image-round", value:="imageShape w3-round-xlarge","Round xLarge"),
      option(cls:="w3-image-round", value:="imageShape w3-round-xxlarge","Round 2xLarge"),
      option(cls:="w3-image-circle", value:="imageShape w3-circle","Circle"),
      option(cls:="w3-image-border", value:="imageShape w3-border","Bordered")
    ).render

    val prop = div(cls:="w3-row ",
      div(cls:="w3-row w3-padding-small",label(cls:="w3-third w3-right-align","Text Align"),btnAlign),
      div(cls:="w3-row w3-padding-small",label(cls:="w3-third w3-right-align","Image Shape"),imgShape)).render

    //----------- Style Init and Event Handling ------------------------------//

    val oldbtnAlign = selElement.getAttribute("data-style-btnAlign")
    if (oldbtnAlign!=null) btnAlign.value = oldbtnAlign

    btnAlign.onchange = (e:dom.Event) => {
      //delete old bg color
      for (i <- 0 until Elements.length) {
        val nodeOne = Elements(i).asInstanceOf[html.Element]
        val oldbtnAlign = nodeOne.getAttribute("data-style-btnAlign")
        if (oldbtnAlign!=null)
          nodeOne.className = nodeOne.className.replaceAll(oldbtnAlign, "")
        //apply new bg color
        nodeOne.className += " " + e.target.asInstanceOf[html.Input].value
        nodeOne.setAttribute("data-style-btnAlign",e.target.asInstanceOf[html.Input].value)
        //ftColor.className = e.target.asInstanceOf[html.Input].value
      }
    }

    val oldimgShape = selElement.getAttribute("data-style-imgShape")
    if (oldimgShape!=null) imgShape.value = oldimgShape

    imgShape.onchange = (e:dom.Event) => {
      //delete old bg color
      for (i <- 0 until Elements.length) {
        val nodeOne = Elements(i).asInstanceOf[html.Element]
        val oldimgShape = nodeOne.getAttribute("data-style-imgShape")
        if (oldimgShape!=null)
          nodeOne.className = nodeOne.className.replaceAll(oldimgShape, "")
        //apply new bg color
        nodeOne.className += " " + e.target.asInstanceOf[html.Input].value
        nodeOne.setAttribute("data-style-imgShape",e.target.asInstanceOf[html.Input].value)
        //ftColor.className = e.target.asInstanceOf[html.Input].value
      }
    }

    // Final Append to Content

    Content.appendChild(prop)
  }

}
