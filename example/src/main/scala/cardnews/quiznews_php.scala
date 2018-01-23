package cardnews

/**
  * Created by Administrator on 2017-04-24.
  */


import lib.{rightOpts, Quill, JCrop, Util, myChart, contextMenu}
import org.scalajs.dom
import org.scalajs.dom.ext._
import org.scalajs.dom.{Event, FileReader, MouseEvent, html}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.URIUtils
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}
import scalatags.JsDom.all._


@JSExport
object quiznews_php extends {
  var objMap = new ArrayBuffer[mutable.HashMap[String, String]]()
  var lineMap = new ArrayBuffer[mutable.HashMap[String, String]]()
  var myUrl : String = ""
  var cardNum : String = null
  var pageNum = 0
  var curPage : html.Div  = null

  var main_saved : html.Div = null
  var cardType : String = "default"
  var copymode = false
  var option_random = true

  var user_id:String =""
  var user_name:String =""
  var user_pic:String =""

  @JSExport
  def main(card:html.Div,target: html.Div, main_area:html.Div,page_end:html.Div,
          _user_id:String, _user_name:String, _user_pic:String): Unit = {

    main_saved = main_area
    user_id = _user_id
    user_name = _user_name
    user_pic = _user_pic

    var paraMap = new mutable.HashMap[String, String]

    println(dom.window.location.search)
    val query = dom.window.location.search.substring(1).split("&")
    //loc.sea   .search.substring(1).split("&")
    for (q <- query) {
      var param = q.split("=")
      paraMap += (param(0)->param(1))
      println(param(0)+" : " +param(1))
    }

    println("GUID",Util.guid())
    if (paraMap.contains("mode") && paraMap("mode")=="copy") {
      copymode = true
    }


    if (paraMap.contains("a")) {
      cardNum = paraMap("a")
      LoadCard(main_area,page_end, cardNum)
    } else {
      if (paraMap.contains("type") && paraMap("type")=="quiznews") {
        dom.window.onload = NewQuizEval(main_area)
        cardType = "quiznews"
      }
      else if (paraMap.contains("type") && paraMap("type")=="typetest") {
        dom.window.onload = (e:dom.Event) => {
          NewSection(main_area,"kj-typecreate-start","Type Create");
          NewTypeCreate(main_area)(e)
          NewSection(main_area,"kj-typetest-start","Questions Start")
          NewTypeTestBtn(main_area);
          //NewTypeTest(main_area)(e);
          NewSection(main_area,"kj-resultcard-start","Result Card Start");
        }
        cardType = "typetest"
      } else
        dom.window.onload = (e:dom.Event) => {
          PageAdd(card,target,true)(e);
          ImageAdd(card,target)(e);
          PageAdd(card,target)(e); // cause error
          dom.window.document.getElementById("page-end").asInstanceOf[html.Div].className += " w3-hide"
        } // Title + Image + Body
    }
    //val container = dom.window.document.getElementById("pages")
    //val qle = dom.window.document.getElementsByClassName("ql-editor").asInstanceOf[html.Element]
    dom.window.document.getElementById("pageAdd").asInstanceOf[html.Button].onclick = PageAdd(card,target)
    dom.window.document.getElementById("imageAdd").asInstanceOf[html.Button].onclick = ImageAdd(card,target)
    dom.window.document.getElementById("CardDelete").asInstanceOf[html.Button].onclick = (e:dom.Event) => Util.fb_login("callback",CardDelete(main_area))
    dom.window.document.getElementById("CardSave").asInstanceOf[html.Button].onclick = (e:dom.Event) => Util.fb_login("callback",CardSave(main_area,"server"))
    dom.window.document.getElementById("CardPreview").asInstanceOf[html.Button].onclick = (e:dom.Event) => Util.fb_login("callback", CardSave(main_area,"preview"))

    //dom.window.document.getElementById("kj-main").asInstanceOf[html.Button].onclick = ActiveHide(target)
    dom.window.document.documentElement.asInstanceOf[html.Element].style.overflowY = "hidden"
    dom.window.document.body.onclick = ActiveHide(target)
    dom.window.document.body.onscroll = ActiveHide(target)

    dom.window.document.getElementById("NewQuiz").asInstanceOf[html.Button].onclick = NewQuizEval(main_area)

    //menuAction(target eleement, action-id)
    contextMenu.initAll(null,imagePasteAction)
    //contextMenu.initAll(contextMenuAction,null)

    //find page size
    val pageTab = dom.window.document.getElementById("kj-tab-pageid").asInstanceOf[html.Div]
    val pageXs = dom.window.document.getElementById("kj-pageid-xs").asInstanceOf[html.Button]
    val pageSm = dom.window.document.getElementById("kj-pageid-sm").asInstanceOf[html.Button]
    val pageMd = dom.window.document.getElementById("kj-pageid-md").asInstanceOf[html.Button]
    val pageLg = dom.window.document.getElementById("kj-pageid-lg").asInstanceOf[html.Button]
    pageXs.onclick = (e:dom.Event) => {
      changeSizeTab(pageTab,pageXs)
      dom.window.document.getElementById("main-stream").asInstanceOf[html.Div].style.width = "360px"
      updateChartSize(360)
    }
    pageSm.onclick = (e:dom.Event) => {
      changeSizeTab(pageTab,pageSm)
      dom.window.document.getElementById("main-stream").asInstanceOf[html.Div].style.width = "768px"
      updateChartSize(768)
    }
    pageMd.onclick = (e:dom.Event) => {
      changeSizeTab(pageTab,pageMd)
      dom.window.document.getElementById("main-stream").asInstanceOf[html.Div].style.width = "1024px"
      updateChartSize(1024)
    }
    pageLg.onclick = (e:dom.Event) => {
      changeSizeTab(pageTab,pageLg)
      dom.window.document.getElementById("main-stream").asInstanceOf[html.Div].style.width = "1200px"
      updateChartSize(1200)
    }


  } //main


  def NewSection(main_stream:html.Div,typename:String, comment:String) : Unit = {


    val line = div(cls:=s"awi-data kj-section-line $typename",hr,label(comment)).render
    val bodies = div(cls:="kj-container",line).render

    val newcard = div (cls:="kj-card-2 w3-card-2 w3-center w3-round w3-white w3-margin-top",
      div(cls:="kj-container"),bodies).render

    val last_page = dom.window.document.getElementById("page-end").asInstanceOf[html.Div]
    main_stream.insertBefore(newcard,last_page)


  }


  
  def SectionLoad (card : html.Div, target : html.Div, section : html.Div) : Unit = {
    target.appendChild(section)
  }



  // Create PageBtn at First then call PageBtnAction for event listening
  def CreatePageBtn(target:html.Div): html.Div = {
    val toolIcon = i(cls := "fa fa-minus-circle").render
    val toolTips = span(cls:="tooltiptext","Hide").render
    val toolShow = button(cls := "kj-tool-show tooltip w3-circle w3-left w3-blue", toolIcon,toolTips).render

    val newCard = button(cls := "kj-new-card tooltip w3-circle w3-hide", i(cls := "fa fa-credit-card"),span(cls:="tooltiptext","newCard")).render
    val newTestCard = button(cls := "kj-new-testcard tooltip w3-circle  w3-hide", i(cls := "fa fa-file-text "),span(cls:="tooltiptext","newTestCard")).render
    //val pageSep = button(cls := "kj-page-sepa tooltip w3-circle ", i(cls := "fa fa-file-text ")).render

    val pageAdd = button(cls := "kj-page-add tooltip w3-circle w3-hide", i(cls := "fa fa-file-text "),span(cls:="tooltiptext","pageAdd")).render
    val imageAdd = button(cls := "kj-image-add tooltip w3-circle w3-hide ", i(cls := "fa fa-image "),span(cls:="tooltiptext","imageAdd")).render
    val flipAdd = button(cls := "kj-flip-add tooltip w3-circle w3-hide", i(cls := "fa fa-times-circle-o "),span(cls:="tooltiptext","flipAdd")).render
    val optAdd = button(cls := "kj-opt-add tooltip w3-circle w3-hide", i(cls := "fa fa-check-square-o "),span(cls:="tooltiptext","optAdd")).render
    val pageDel = button(cls := "kj-page-del tooltip w3-circle w3-hide", i(cls := "fa fa-trash"),span(cls:="tooltiptext","pageDel")).render
    val typeTestAdd = button(cls := "kj-type-testadd tooltip w3-circle w3-hide", i(cls := "fa fa-quora"),span(cls:="tooltiptext","typeTestAdd")).render
    val typeTestRes = button(cls := "kj-type-testres tooltip w3-circle w3-hide", i(cls := "fa fa-line-chart"),span(cls:="tooltiptext","typeTestRes")).render
    val typeTestBranch = button(cls := "kj-type-testbranch tooltip w3-circle w3-hide", i(cls := "fa fa-code-fork"),span(cls:="tooltiptext","typeTestBranch")).render

    //val newQuiz = button(cls := "kj-quiz-add w3-circle", i(cls := "fa fa-plus-circle")).render

    // <button type="button" id="NewQuiz" class="w3-button w3-theme-d1 w3-circle"><i class="fa fa-plus"></i></button>
    //conetxmenu setting

    val toolCont = div(cls:="kj-tool-container w3-left",
      newCard, newTestCard, pageAdd, imageAdd, flipAdd, optAdd,
      typeTestAdd, typeTestRes,typeTestBranch, pageDel).render

    val pageBtn = div(cls := " kj-pagetool", toolShow,toolCont).render

    toolShow.onclick = (e:dom.Event) => {
      println("tool show click")
        if (toolCont.className.contains("w3-hide")) {
          toolCont.className = toolCont.className.replaceAll(" w3-hide","")
          toolIcon.className = "fa fa-minus-circle"
          toolTips.textContent = "Hide"
        }
        else {
          toolCont.className += " w3-hide"
          toolIcon.className = "fa fa-plus-circle"
          toolTips.textContent = "Show"
        }
    }


    return pageBtn
  }


  // Attach event to existing PageBtn and show/hide to page Context
  def PageBtnAction(pageBtn:html.Div,card:html.Div,target:html.Div, selPage:html.Div): Unit = {


    //find button
    val newCard = pageBtn.getElementsByClassName("kj-new-card")(0).asInstanceOf[html.Div]
    val newTestCard = pageBtn.getElementsByClassName("kj-new-testcard")(0).asInstanceOf[html.Div]
    //val pageSep = pageBtn.getElementsByClassName("kj-page-sepa w3-circle")(0).asInstanceOf[html.Div]

    val pageAdd = pageBtn.getElementsByClassName("kj-page-add")(0).asInstanceOf[html.Div]
    val imageAdd = pageBtn.getElementsByClassName("kj-image-add")(0).asInstanceOf[html.Div]
    val flipAdd = pageBtn.getElementsByClassName("kj-flip-add")(0).asInstanceOf[html.Div]
    val optAdd = pageBtn.getElementsByClassName("kj-opt-add")(0).asInstanceOf[html.Div]
    val pageDel = pageBtn.getElementsByClassName("kj-page-del")(0).asInstanceOf[html.Div]
    val typeTestAdd = pageBtn.getElementsByClassName("kj-type-testadd")(0).asInstanceOf[html.Div]
    val typeTestRes = pageBtn.getElementsByClassName("kj-type-testres")(0).asInstanceOf[html.Div]
    val typeTestBranch = pageBtn.getElementsByClassName("kj-type-testbranch")(0).asInstanceOf[html.Div]


    // Action
    newCard.onclick = NewQuizEvalAfter(main_saved,card)
    newTestCard.onclick = NewTypeTestAfter(main_saved,card)

    pageAdd.onclick = PageAdd(card,target)
    imageAdd.onclick = ImageAdd(card,target)
    flipAdd.onclick = FlipAdd(card,target)
    optAdd.onclick = OptionAdd(card,target)
    typeTestAdd.onclick = TypeTestAdd(main_saved,card,target)
    typeTestRes.onclick = TypeResAdd(main_saved,card,target)
    typeTestBranch.onclick = TypeRuleAdd(card,target)

    pageDel.onclick = PageDelete(card,target,selPage)

  }



  def PageBtnCheck(pageBtn:html.Div,card:html.Div,target:html.Div, selPage:html.Div): Unit = {
    // check context type
    var docType = "normal"
    val card2 = Util.findAncestor(selPage,"kj-card-2").asInstanceOf[html.Div]
    val objClassname = selPage.getElementsByClassName("awi-data")(0).asInstanceOf[html.Div].className
    val typecard = dom.window.document.getElementsByClassName("awi-typecreate-page")
    if (typecard.length == 0) docType = "normal"
    else docType = "awi-typecreate-page"
    var conType = "normal"
    if (card2.className.contains("kj-card-normal") ||
      card2.className.contains("kj-card-test"))
      conType = "normal"
    else if (card2.className.contains("kj-card-type"))
      conType = "type"
    else
      conType = "normal"


    println("pageBtn",docType,conType,objClassname)

    //find button
    val newCard = pageBtn.getElementsByClassName("kj-new-card")(0).asInstanceOf[html.Div]
    val newTestCard = pageBtn.getElementsByClassName("kj-new-testcard")(0).asInstanceOf[html.Div]
    //val pageSep = pageBtn.getElementsByClassName("kj-page-sepa w3-circle")(0).asInstanceOf[html.Div]

    val pageAdd = pageBtn.getElementsByClassName("kj-page-add")(0).asInstanceOf[html.Div]
    val imageAdd = pageBtn.getElementsByClassName("kj-image-add")(0).asInstanceOf[html.Div]
    val flipAdd = pageBtn.getElementsByClassName("kj-flip-add")(0).asInstanceOf[html.Div]
    val optAdd = pageBtn.getElementsByClassName("kj-opt-add")(0).asInstanceOf[html.Div]
    val pageDel = pageBtn.getElementsByClassName("kj-page-del")(0).asInstanceOf[html.Div]
    val typeTestAdd = pageBtn.getElementsByClassName("kj-type-testadd")(0).asInstanceOf[html.Div]
    val typeTestRes = pageBtn.getElementsByClassName("kj-type-testres")(0).asInstanceOf[html.Div]
    val typeTestBranch = pageBtn.getElementsByClassName("kj-type-testbranch")(0).asInstanceOf[html.Div]

    // apply context rule
    if (conType.equals("normal")) {
      newCard.className = newCard.className.replaceAll(" w3-hide","")
      newTestCard.className = newTestCard.className.replaceAll(" w3-hide","")
      pageAdd.className = pageAdd.className.replaceAll(" w3-hide","")
      imageAdd.className = imageAdd.className.replaceAll(" w3-hide","")
      flipAdd.className = flipAdd.className.replaceAll(" w3-hide","")
      optAdd.className = optAdd.className.replaceAll(" w3-hide","")
      typeTestAdd.className = typeTestAdd.className.replaceAll(" w3-hide","")
      typeTestRes.className = typeTestRes.className.replaceAll(" w3-hide","")
      typeTestBranch.className = typeTestBranch.className.replaceAll(" w3-hide","")
      pageDel.className = pageDel.className.replaceAll(" w3-hide","")
    }

    if(conType.equals("type")) {
      pageAdd.className = pageAdd.className.replaceAll(" w3-hide","")
      imageAdd.className = imageAdd.className.replaceAll(" w3-hide","")
      pageDel.className = pageDel.className.replaceAll(" w3-hide","")
    }

    if (objClassname != null && objClassname.contains("awi-typecreate-page")) {
      typeTestAdd.className += " w3-hide"
      pageDel.className += " w3-hide"
    }

    if (!docType.contains("awi-typecreate-page")) {
      newTestCard.className += " w3-hide"
      typeTestAdd.className += " w3-hide"
      typeTestRes.className += " w3-hide"
      typeTestBranch.className += " w3-hide"
    }

  }






    def LoadCard(main_stream:html.Div,page_end:html.Div,cardNum : String): Unit = {
    import upickle.default._


    //val urlBase = s"${GlobalVars.dataBase}/$cardNum"
    val urlBase = s"php/ajax_getcard.php?a=$cardNum"

    println ("URL:",urlBase)
    //dom.window.alert("called Tab:"+objNm+":"+objId+":"+tab)
    ///////////////////////////////////////////////
    dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "block"
    val request = Ajax.get(urlBase)
      request.onComplete {
        case Success(xhr) =>
      //println(xhr.responseText)
      //var saved_area1 : new ArrayBuffer[String] = null
      //var length = 0
      val res = js.JSON.parse(xhr.responseText)
        val name = res._source.name
        val cardtype = res._source.`type`
        val iddiv = div(id := cardNum).render

        println("res",res._source.name,res._source.`type`)
        val json_data = dom.window.atob(res._source.resume.asInstanceOf[String])
      //println("json_data",json_data)
        val (cardlen, cards) = read [(Int,ArrayBuffer[(Int,ArrayBuffer[String],ArrayBuffer[String])])](json_data)
        //val (length, saved_area1,saved_area2) = read[(Int,ArrayBuffer[String],ArrayBuffer[String])](json_data)
      // newQuizCard

        var typestr : String = "kj-card-normal"

        for (index <- 0 until cardlen) {
           val (length,saved_area1,saved_area2) = cards(index)
          //println(length,saved_area1,saved_area2)
          val bodies = div(cls:="kj-container").render
          val clsstr = s"kj-card-2 $typestr w3-card-2 w3-center w3-round w3-white w3-margin-top"
          var newcard = div (cls:=clsstr,
            div(cls:="kj-container"),bodies).render

          //*** !important : insert to actual document before call PageLoad
          // Exception : kj-section-line
          //if (!saved_area2(0).contains("kj-section-line"))
          main_stream.insertBefore(newcard,page_end)
          if (cardtype.equals("default"))
            dom.window.document.getElementById("page-end").asInstanceOf[html.Div].className += " w3-hide"


          for (i <- 0 until length) {
            //println(length,saved_area1(i))
            //println(length,saved_area2(i))
            if (saved_area2(i).contains("awi-text-page")) {
              val page_div = div(cls := saved_area2(i)).render
              page_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
              PageLoad(newcard,bodies, page_div)
            } else if (saved_area2(i).contains("awi-image-page")) {
              val image_div = div(cls := saved_area2(i)).render
              image_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
              ImageLoad(newcard,bodies, image_div)
            } else if (saved_area2(i).contains("awi-option-page")) {
              val option_div = div(cls := saved_area2(i)).render
              option_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
              OptionLoad(newcard,bodies, option_div)
            } else if (saved_area2(i).contains("awi-flip-page")) {
              val flip_div = div(cls := saved_area2(i)).render
              flip_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
              FlipLoad(newcard,bodies, flip_div)
            } else if (saved_area2(i).contains("awi-typecreate-page")) {
              val typecreate = div(cls := saved_area2(i)).render
              typecreate.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
              TypeCreateLoad(newcard,bodies, typecreate)
            } else if (saved_area2(i).contains("awi-typetest-page")) {
              val typetest = div(cls := saved_area2(i)).render
              typetest.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
              TypeTestLoad(newcard,bodies, typetest)
            } else if (saved_area2(i).contains("awi-typeres-page")) {
              val typetest = div(cls := saved_area2(i)).render
              typetest.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
              TypeResLoad(main_stream, newcard,bodies, typetest)
            } else if (saved_area2(i).contains("awi-rule-page")) {
              val typetest = div(cls := saved_area2(i)).render
              typetest.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
              TypeRuleLoad(newcard,bodies, typetest)
            } else if (saved_area2(i).contains("kj-typequiz-add")) {
              val section_div = div(cls := saved_area2(i)).render
              section_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
              NewTypeTestBtnLoad(newcard,bodies,section_div)
            } else if (saved_area2(i).contains("kj-section-line")) {
              val section_div = div(cls := saved_area2(i)).render
              section_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
              //check cardtype
              if (section_div.className.contains("kj-typecreate-start"))
                typestr = "kj-card-type"
              else if (section_div.className.contains("kj-typetest-start"))
                typestr = "kj-card-test"
              else if (section_div.className.contains("kj-resultcard-start"))
                typestr = "kj-card-normal"
              else typestr = "kj-card-normal"
              val newclsstr = s"kj-card-2 kj-card-section w3-card-2 w3-center w3-round w3-white w3-margin-top"
              newcard.className = newclsstr
              SectionLoad(newcard,bodies,section_div)
            }

            //main_stream.insertBefore(newcard,page_end)
          }
        }

      //println("6image size change")
      //img siz
      /* no more images
      val images = main_stream.getElementsByTagName("img")
      for (tool <- images) {
        if (tool.asInstanceOf[html.Div].className != null &&
          !tool.asInstanceOf[html.Div].className.contains("awi-image") &&
          !tool.asInstanceOf[html.Div].className.contains("awi-option-image"))
        tool.asInstanceOf[html.Div].className += " awi-image"

      }
      */
      dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"
      for (dirty <- dom.window.document.getElementsByClassName("dirty")) {
        dirty.asInstanceOf[html.Div].className = dirty.asInstanceOf[html.Div].className.replaceAll("dirty"," ")
      }


      case Failure(e) =>
        dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"
        dom.window.alert("Try later")

    }

  }

  @JSExport
  def CardSave(main_area:html.Div, storage:String = "server") : Function1[MouseEvent,_]  = (e:dom.Event) => {
    //import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow
    import upickle.default._


    var cardtype = cardType


    val saved_cards =
       new ArrayBuffer[(Int, ArrayBuffer[String], ArrayBuffer[String])]()

    val cards = dom.window.document.getElementsByClassName("kj-card-2")

    for (card <- cards) {
      val saved_area1 = new ArrayBuffer[String]()
      val saved_area2 = new ArrayBuffer[String]()

      val datas = card.asInstanceOf[html.Div].getElementsByClassName("awi-data")
      //var i : Int = 0
      for (i <- 0 until datas.length) {
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
          } else if (hdata.className.contains("awi-option-page")) {
            saved_area1 += URIUtils.encodeURIComponent(hdata.innerHTML)
            saved_area2 += hdata.className
            if (cardtype.equals("default")) cardtype = "typeaction"
          } else if (hdata.className.contains("awi-flip-page")) {
            saved_area1 += URIUtils.encodeURIComponent(hdata.innerHTML)
            saved_area2 += hdata.className
            if (cardtype.equals("default")) cardtype = "typeaction"
          } else if (hdata.className.contains("awi-typecreate-page")) {
            saved_area1 += URIUtils.encodeURIComponent(hdata.innerHTML)
            saved_area2 += hdata.className
            if (cardtype.equals("default")) cardtype = "typetest"
          } else if (hdata.className.contains("awi-typetest-page")) {
            saved_area1 += URIUtils.encodeURIComponent(hdata.innerHTML)
            saved_area2 += hdata.className
          } else if (hdata.className.contains("awi-typeres-page")) {
            saved_area1 += URIUtils.encodeURIComponent(hdata.innerHTML)
            saved_area2 += hdata.className
          } else if (hdata.className.contains("awi-rule-page")) {
            saved_area1 += URIUtils.encodeURIComponent(hdata.innerHTML)
            saved_area2 += hdata.className
          } else if (hdata.className.contains("kj-typequiz-add")) {
            saved_area1 += URIUtils.encodeURIComponent(hdata.innerHTML)
            saved_area2 += hdata.className
          } else if (hdata.className.contains("kj-section-line")) {
            saved_area1 += URIUtils.encodeURIComponent(hdata.innerHTML)
            saved_area2 += hdata.className
          }
        }
      }
      if (datas.length > 0) { //filter empty page
        //val new_card = new ArrayBuffer[(Int, ArrayBuffer[String], ArrayBuffer[String])]()
        //new_card.+=:(saved_area1.length,saved_area1,saved_area2)
        saved_cards.+=:(saved_area1.length,saved_area1,saved_area2)
      }
    }


    val json_data = write((saved_cards.length,saved_cards.reverse))   //reverse order
    val jsonb_data = dom.window.btoa(json_data)

    //*************************Summary gathering******************************
    var sumText = "No Title"
    var imgsrc = ""
    var imgdata = ""
    var sumimg = main_area.getElementsByTagName("img")
    println("11")

    if (!js.isUndefined(sumimg(0)) && sumimg(0) != null ) {
      imgsrc = sumimg(0).asInstanceOf[html.Image].src
    } else {
        imgsrc = "./images/story_cover_gif.jpg"
    }
    println("12")
    val sumTitle = main_area.getElementsByClassName("ql-editor")
    if (!js.isUndefined(sumTitle(0)) && sumTitle(0) != null) {
      println("13")
      sumText = sumTitle(0).textContent
    }

    val bsumText = dom.window.btoa(URIUtils.encodeURIComponent(sumText))
    val bimgsrc = dom.window.btoa(URIUtils.encodeURIComponent(imgsrc))
    //***********end summary gathering  ===============================================


    val pages = s"""{
      "user_id" : "guest_user",
      "user_name" : "$user_name",
      "user_pic" : "$user_pic",
      "updated" : "1505441175",
      "name": "QuizNews",
      "type": "$cardtype",
      "sumText":"$bsumText",
      "imgsrc":"$bimgsrc",
      "resume":  "$jsonb_data"
    }"""


    if (storage.equals("server")) SaveToServer(pages)
    else if (storage.equals("preview")) CreatePreviewModal(pages,cardtype,imgsrc,sumText)
  }


 def SaveToServer(pages:String) = {

   var urlBase = ""
   if (cardNum == null || copymode)
   //urlBase = s"${GlobalVars.dataBase}/?pipeline=timestamp"  //New card or Clone
     urlBase = s"php/ajax_postsave.php"  //New card or Clone
   else
   //urlBase = s"${GlobalVars.dataBase}/$cardNum?pipeline=timestamp" //Existing Card
     urlBase = s"php/ajax_postsave.php?a=$cardNum" //Existing Card

   println("request:",urlBase)
   //dom.window.alert("called Tab:"+objNm+":"+objId+":"+tab)
   ///////////////////////////////////////////////
   //Ajax.get(urlBase).onSuccess { case xhr => ...}
   dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "block"

   val request = Ajax.post(urlBase,pages)
   request.onComplete {
     case Success(xhr) =>
       //println(xhr.responseText); dom.window.alert("done")

       //SavePreview
       val res = js.JSON.parse(xhr.responseText)
       println("id:" , res._id)
       cardNum = res._id.toString

       // Saving Preview Img & Text
       //--PreviewCard(main_area,cardNum)
       dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"
       //dom.window.location.href = "myhome.html"
       for (dirty <- dom.window.document.getElementsByClassName("kj-row")) {
         println("kj-row",dirty.asInstanceOf[html.Div].className)
         if (dirty.asInstanceOf[html.Div].className.contains("dirty"))
          dirty.asInstanceOf[html.Div].className = dirty.asInstanceOf[html.Div].className.replaceAll("dirty"," ")
       }
       dom.window.alert("Saved")


      case Failure(e) =>
       dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"
       dom.window.alert("Retry Later")


   }
 }
/*
  def SaveToLocal(pages:String) = {

    dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "block"

    dom.window.localStorage.setItem("previewcard",pages)
    dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"
  }
*/



  @JSExport
  def CardDelete(target:html.Div) : Function1[MouseEvent,_]  = (e:dom.Event) => {
    //import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow


    if (cardNum != null && dom.window.confirm("Are you sure to delete?")) {
      //val urlBase = s"${GlobalVars.dataBase}/$cardNum"
      val urlBase = s"php/ajax_delete.php?a=$cardNum"

      println("request:", urlBase)
      //dom.window.alert("called Tab:"+objNm+":"+objId+":"+tab)
      ///////////////////////////////////////////////
      // ElasticCall => Ajax.delete(urlBase).onSuccess { case xhr =>
      Ajax.get(urlBase).onSuccess { case xhr =>
        println(xhr.responseText)
        //val res = js.JSON.parse(xhr.responseText)
        //println("id:" , res._id)
        dom.window.alert("Done")
        dom.window.location.href = "myhome.html"
      }
    }

  }

  /* NewQuiz Card */
  def NewQuizEval(main_stream:html.Div) : Function1[Event,_]  = (e:dom.Event) => {
    val bodies = div(cls:="kj-container").render
    val newcard = div (cls:="kj-card-2 kj-card-normal w3-card-2 w3-center w3-round w3-white w3-margin-top",
                    div(cls:="kj-container"),bodies).render
    val last_page = dom.window.document.getElementById("page-end").asInstanceOf[html.Div]
    main_stream.insertBefore(newcard,last_page)

    //reset curPage
    curPage = null

    PageAdd(newcard,bodies,true)(e);
    ImageAdd(newcard,bodies)(e);
    OptionAdd(newcard,bodies)(e)
    ActiveHide(main_stream)
  }

  /* NewQuiz Card After refer*/
  def NewQuizEvalAfter(main_stream:html.Div, refer:html.Div) : Function1[Event,_]  = (e:dom.Event) => {

    println(main_stream.className,refer.className)
    val bodies = div(cls:="kj-container").render
    val newcard = div (cls:="kj-card-2 kj-card-normal w3-card-2 w3-center w3-round w3-white w3-margin-top",
      div(cls:="kj-container"),bodies).render
    main_stream.insertBefore(newcard,refer.nextSibling)

    //reset curPage
    curPage = null

    PageAdd(newcard,bodies,true)(e);
    ImageAdd(newcard,bodies)(e);
    OptionAdd(newcard,bodies)(e)
    ActiveHide(main_stream)
  }


  /* New (Type Name , Score) Card */
  def NewTypeCreate(main_stream:html.Div) : Function1[Event,_]  = (e:dom.Event) => {
    val bodies = div(cls:="kj-container").render
    val newcard = div (cls:="kj-card-2 kj-card-type w3-card-2 w3-center w3-round w3-white w3-margin-top",
      div(cls:="kj-container"),bodies).render
    val last_page = dom.window.document.getElementById("page-end").asInstanceOf[html.Div]
    main_stream.insertBefore(newcard,last_page)

    //reset curPage
    curPage = null

    PageAdd(newcard,bodies,true)(e);
    ImageAdd(newcard,bodies)(e);
    TypeCreateAdd(newcard,bodies)(e)
    ActiveHide(main_stream)
  }


  /* New Type Test Card */
  def NewTypeTest(main_stream:html.Div) : Function1[Event,_]  = (e:dom.Event) => {
    val bodies = div(cls:="kj-container").render
    val newcard = div (cls:="kj-card-2 kj-card-test w3-card-2 w3-center w3-round w3-white w3-margin-top",
      div(cls:="kj-container"),bodies).render
    val last_page = dom.window.document.getElementById("kj-typequiz-plus").asInstanceOf[html.Div]
    main_stream.insertBefore(newcard,last_page)

    //reset curPage
    curPage = null

    PageAdd(newcard,bodies,true)(e);
    ImageAdd(newcard,bodies)(e);
    TypeTestAdd(main_stream,newcard,bodies)(e)
    ActiveHide(main_stream)
  }

  /* New Type Test Card */
  def NewTypeTestAfter(main_stream:html.Div, refer:html.Div) : Function1[Event,_]  = (e:dom.Event) => {
    val bodies = div(cls:="kj-container").render
    val newcard = div (cls:="kj-card-2 kj-card-test w3-card-2 w3-center w3-round w3-white w3-margin-top",
      div(cls:="kj-container"),bodies).render
    main_stream.insertBefore(newcard,refer.nextSibling)

    //reset curPage
    curPage = null

    PageAdd(newcard,bodies,true)(e);
    ImageAdd(newcard,bodies)(e);
    TypeTestAdd(main_stream,newcard,bodies)(e)
    ActiveHide(main_stream)
  }

  /* Add Type Test Card on button click*/
  def NewTypeTestBtn(main_stream:html.Div) : Unit = {

    val questionBtn = button(cls:="w3-button w3-theme-d1 w3-circle",i(cls:="fa fa-plus")).render
    val addQuestion = div(id:="kj-typequiz-plus", cls:="kj-card-2",div(cls:="w3-container w3-center",div(cls:="awi-data kj-typequiz-add w3-margin",questionBtn))).render

    val last_page = dom.window.document.getElementById("page-end").asInstanceOf[html.Div]
    main_stream.insertBefore(addQuestion,last_page)
    // add event handler afert attache
    questionBtn.onclick = (e:dom.Event) => {
      NewTypeTest(main_stream)(e)
    }
  }


  /* New Add Type Test Button */
  def NewTypeTestBtnLoad (card : html.Div, target : html.Div, section : html.Div) : Unit = {
    //reset card
    card.id = "kj-typequiz-plus"
    card.className = "kj-card-2"
    section.className = section.className + " w3-center"
    target.appendChild(section)
    val questionBtn = section.getElementsByTagName("button")(0)
    questionBtn.asInstanceOf[html.Button].onclick = (e:dom.Event) => {
      NewTypeTest(main_saved)(e)
    }
  }

  //--------------------------------------------------------------------------------------------------------
  //
  //  Object Create & Load Section
  //     Object : Page(awi-text-page), Image(awi-image-page), Type(awi-typecreate_page), ResultGraph(awi-typeres-page)
  //              Rule(awi-rule-page), Action(awi-option-page)
  //              kj-section-line (kj-typecreate-start, kj-typetest-start, kj-resultcard-start
  //--------------------------------------------------------------------------------------------------------

  /* Add Page(Quill Editor Page) */
  def PageAdd(card:html.Div,target:html.Div, isTitle:Boolean = false) : Function1[Event,_]  = (e:dom.Event) => {

    //body compose
    val  theToolbar = "ToolPage" + pageNum
      pageNum += 1
    val editor = div( cls:="awi-editor w3-border").render
    val toolbar =  div( cls:="kj-pagebar" , id:=theToolbar,
      span(" "),
      button ("작게",cls:="ql-size",value:="small"),
      button ("표준",cls:="ql-size"),
      button ("크게",cls:="ql-size",value:="large"),
      button ("제목",cls:="ql-size",value:="huge"),
      span(" "),
      button ("B",cls:="ql-bold",style:="font-style:Bold"),
      button ("I",cls:="ql-italic",style:="font-style:Italic"),
      button ("U",cls:="ql-underline",style:="text-decoration: underline;"),
      button( "H", cls:="ql-background", value:="yellow",style:="background-color:yellow"),
      button (i(cls:="fa fa-align-left"),cls:="ql-align w3-hide-small",style:="height:90%;font-size:15px"),
      button (i(cls:="fa fa-align-center"),cls:="ql-align w3-hide-small",value:="center",style:="height:90%;font-size:15px"),
      button (i(cls:="fa fa-align-right"),cls:="ql-align w3-hide-small",value:="right",style:="height:90%;font-size:15px")
    ).render

    val pageBtn = CreatePageBtn(target)

    val title = div(input("TITLE",cls:="w3-xlarge")).render
    val newPage = div(cls:="w3-cell-row kj-row", div(cls:="awi-data awi-text-page",editor),div(cls:="kj-actbar w3-border",toolbar,pageBtn)).render

    //evnet processing
    PageBtnAction(pageBtn,card,target,newPage)

    newPage.onclick = ActiveToggle(target,newPage,editor.asInstanceOf[html.Div],"awi-text-page")
    editor.onclick = ActiveToggle(target,newPage,editor.asInstanceOf[html.Div],"awi-editor")

    //render
    if (curPage != null)
      target.insertBefore(newPage,curPage.nextSibling)
    else
      target.appendChild(newPage)

    PageBtnCheck(pageBtn,card,target,newPage)
    title.click()
    CreateQuill(editor,"#"+theToolbar,isTitle)
    if (isTitle) editor.getElementsByClassName("ql-editor")(0).asInstanceOf[html.Div].style.fontSize = "26px"

  }

  def PageLoad(card:html.Div,target:html.Div, text_page:html.Div)  {

    //body compose
    val  theToolbar = "ToolPage" + pageNum
    pageNum += 1
    val editor = text_page.getElementsByClassName("awi-editor")(0).asInstanceOf[html.Div]
    editor.className += " w3-border"
    val toolbar =  div( cls:="kj-pagebar" , id:=theToolbar,
      span(" "),
      button ("작게",cls:="ql-size",value:="small"),
      button ("표준",cls:="ql-size"),
      button ("크게",cls:="ql-size",value:="large"),
      button ("제목",cls:="ql-size",value:="huge"),
      span(" "),
      button ("B",cls:="ql-bold",style:="font-style:Bold"),
      button ("I",cls:="ql-italic",style:="font-style:Italic"),
      button ("U",cls:="ql-underline",style:="text-decoration: underline;"),
      button( "H", cls:="ql-background", value:="yellow",style:="background-color:yellow"),
      button (i(cls:="fa fa-align-left"),cls:="ql-align w3-hide-small",style:="height:90%;font-size:15px"),
      button (i(cls:="fa fa-align-center"),cls:="ql-align w3-hide-small",value:="center",style:="height:90%;font-size:15px"),
      button (i(cls:="fa fa-align-right"),cls:="ql-align w3-hide-small",value:="right",style:="height:90%;font-size:15px")
    ).render

    val pageBtn = CreatePageBtn(target)

    val title = div(input("TITLE",cls:="w3-xlarge")).render
    val newPage = div(cls:="w3-cell-row kj-row", text_page, div(cls:="kj-actbar w3-hide",toolbar,pageBtn)).render

    PageBtnAction(pageBtn,card,target,newPage)

    newPage.onclick = ActiveToggle(target,newPage,editor.asInstanceOf[html.Div],"awi-text-page")
    editor.onclick = ActiveToggle(target,newPage,editor.asInstanceOf[html.Div],"awi-editor")


    //render
    if (curPage != null)
      target.insertBefore(newPage,curPage.nextSibling)
    else
      target.appendChild(newPage)
    PageBtnCheck(pageBtn,card,target,newPage)
    title.click()
    CreateQuill(editor,"#"+theToolbar)
  }



  def ImageAdd(card:html.Div,target:html.Div) : Function1[Event,_]  = (e:dom.Event) => {

    //body compose
    val theImage = "ImgNum"+pageNum
    val thePreview = "PreNum"+pageNum
    pageNum += 1

    /*
    val media = div(cls:="w3-center",
      video(cls:="awi-image", id:=theMedia, source(src:="./images/story_cover_gif.jpg"))
    ).render
    */

    val image = div(cls:="w3-center",
                   img(cls:="awi-image", id:=thePreview, src:="./images/story_cover_gif.jpg",alt:="Image")
                ).render

    val  theToolbar = "ToolImg" + pageNum
    //val  theMediabar = "MediaNum" + pageNum
    val toolbar =  div( cls:="kj-imagebar" , id:=theToolbar,
      label( i(cls:="fa fa-camera w3-xlarge"), cls:="w3-xxlarge w3-blue", `for`:=theImage, input(id:=theImage, `type`:="file", multiple, hidden))).render

    val editbar =  div( cls:="kj-clipbar" ,
      label( i(cls:="fa fa-cut w3-xlarge"), cls:="w3-xxlarge w3-blue")).render

    val ytbar =  div( cls:="kj-ytbar" ,
      label( i(cls:="fa fa-youtube-square w3-xlarge"), cls:="w3-xxlarge w3-blue")).render
    val mediabar =  div( cls:="kj-mediabar").render

    val pageBtn = CreatePageBtn(target)


    val title = div(input("TITLE",cls:="w3-xlarge")).render
    val imgPage = div(cls:="w3-cell-row kj-row" , div(cls:="awi-data awi-image-page " ,image),
                    div(cls:="kj-actbar ",toolbar,editbar,ytbar, mediabar,pageBtn)).render

      //evnet processing

    PageBtnAction(pageBtn,card,target,imgPage)

    imgPage.onclick = ActiveToggle(target,imgPage,image,"awi-image-page")
    image.onclick = ActiveToggle(target,imgPage,image,"awi-image")


    //render
    if (curPage != null)
      target.insertBefore(imgPage,curPage.nextSibling)
    else
      target.appendChild(imgPage)

    //post event
      println("image click and new croppie")
    editbar.onclick = (e:dom.Event) => {
      val cropIt = dom.window.document.getElementById("CropIt").asInstanceOf[html.Div]
      val imageIt = dom.window.document.getElementById("theCropImage").asInstanceOf[html.Image]
      val orgImage = image.getElementsByTagName("img")(0).asInstanceOf[html.Image]
      imageIt.src = orgImage.src
      orgImage.id = thePreview
      JCrop.toggleJCrop(orgImage.id)
    }

    CreateYtModal(ytbar,image)

    //Initialize ImagePage
    val imageFile = dom.window.document.getElementById(theImage).asInstanceOf[html.Input]
    val preview = dom.window.document.getElementById(thePreview).asInstanceOf[html.Image]
    imageFile.onchange = RenderImage(image,"awi-image",preview,imageFile)

    PageBtnCheck(pageBtn,card,target,imgPage)

    title.click()
  }



  def ImageLoad(card:html.Div,target:html.Div,new_image:html.Div)  {

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
    val  theToolbar = "ToolImg" + pageNum

    val imageFile = input(id:=theImage, `type`:="file", multiple, hidden).render

    val toolbar =  div( cls:="kj-imagebar" , id:=theToolbar,
      label( i(cls:="fa fa-camera w3-xlarge"), cls:="w3-xxlarge w3-blue", `for`:=theImage, imageFile)).render

    val editbar =  div( cls:="kj-clipbar" ,
      label( i(cls:="fa fa-cut w3-xlarge"), cls:="w3-xxlarge w3-blue")).render

    val ytbar =  div( cls:="kj-ytbar" ,
      label( i(cls:="fa fa-youtube-square w3-xlarge"), cls:="w3-xxlarge w3-blue")).render

    val mediabar =  div( cls:="kj-mediabar").render


    val pageBtn = CreatePageBtn(target)

    val title = div(input("TITLE",cls:="w3-xlarge")).render

    val imgPage = div(cls:="w3-cell-row kj-row" , new_image, div(cls:="w3-cell kj-actbar w3-hide",toolbar,editbar,ytbar, mediabar,pageBtn)).render

    //evnet processing

    PageBtnAction(pageBtn,card, target,imgPage)

    imgPage.onclick = ActiveToggle(target,imgPage,new_image,"awi-image-page")
    new_image.onclick = ActiveToggle(target,imgPage,new_image,"awi-image")

    //render
    imgPage.setAttribute("async","false")
    if (curPage != null)
      target.insertBefore(imgPage,curPage.nextSibling)
    else
      target.appendChild(imgPage)
    //post event
    val imageClass = image_div.asInstanceOf[html.Div].className
    if (imageClass.contains("awi-media")) {
      println("media click and new croppie")
      val media_div = image_div.asInstanceOf[html.Video]
      media_div.onclick = (e:dom.Event) => {
        if (media_div.paused) media_div.play()
        else media_div.pause()
      }
    } else {
      println("image click and new croppie")
      editbar.onclick = (e: dom.Event) => {
        val cropIt = dom.window.document.getElementById("CropIt").asInstanceOf[html.Div]
        val imageIt = dom.window.document.getElementById("theCropImage").asInstanceOf[html.Image]
        val orgImage = new_image.getElementsByTagName("img")(0).asInstanceOf[html.Image]
        orgImage.id = thePreview
        imageIt.src = orgImage.src
        JCrop.toggleJCrop(orgImage.id)
      }
    }

    CreateYtModal(ytbar,new_image)

    //val imageFile = dom.window.document.getElementById(theImage).asInstanceOf[html.Input]
    val preview = dom.window.document.getElementById(thePreview).asInstanceOf[html.Image]
    imageFile.onchange = RenderImage(new_image,"awi-image",preview,imageFile)
    PageBtnCheck(pageBtn,card,target,imgPage)

    title.click()
  }


  // Add OptionItem with Del button & Option Result
  def NewOptionDel(options : html.Div, optionText : String , optionAction : String, addBtn : html.Button): html.Input = {


    //create option label for display
    val closeBtn = button (i(cls:="fa fa-close"),cls:="kj-option-close  w3-hide-small",value:="center").render
    val inputBox = input(cls:="kj-option-val w3-cell w3-button w3-teal ",`type`:="text", data("hold-value"):=optionText,
      data("style-bgcolor"):="w3-teal" , value:=optionText, placeholder:="Option Text").render
    //val inputLabel = label(cls:="kj-option-hold", value := optionText).render

    //create results
    val actSelect = select(cls:= "kj-option-action", data("action-value"):= optionAction, option("+ 1",value:="1"),option("+ 2",value:="2"),option("+ 3",value:="3")
      ,option("+ 4",value:="4") ,option("+ 5",value:="5") ,option("+ 6",value:="6")
      ,option("+ 7",value:="7") ,option("+ 8",value:="8") ,option("+ 9",value:="9"),option("+ 10",value:="10") ).render
    actSelect.value = optionAction

    val nextAction = div(cls:="kj-option-prop w3-cell", label("Current"),actSelect).render

    val optionResult = div(cls:="kj-option-result w3-cell",nextAction).render


    // cretae option item
    val optionItem = div(cls:="kj-option-item w3-cell-row w3-padding",inputBox,closeBtn,optionResult).render


    // options & result add
    options.insertBefore(optionItem,addBtn)

    // other event processing
    inputBox.onchange = (e: dom.Event) => {
      println("text change", e.target.asInstanceOf[html.Input].value)
      e.srcElement.setAttribute("data-hold-value",e.target.asInstanceOf[html.Input].value)
    }

    actSelect.onchange = (e :dom.Event) => {
      println("select change", e.target.asInstanceOf[html.Select].value)
      e.srcElement.setAttribute("data-action-value",e.target.asInstanceOf[html.Select].value)
    }

    closeBtn.onclick = (e:dom.Event) => {
      println("*****remove child*****",optionItem)
      options.removeChild(optionItem)
    }

    return inputBox

  }

  // Add FlipItem with Del button & Flip Action
  def NewFlipDel(options : html.Div, optionText : String , imgsrc : String, addBtn : html.Button): html.Div = {

    val theImage = "FlipImg"+pageNum
    val thePreview = "FlipPre"+pageNum
    pageNum += 1

    //create option label for display
    val closeBtn = button (i(cls:="fa fa-close"),cls:="kj-option-close  w3-hide-small",value:="center").render
    val inputBox = input(cls:="kj-flip-val w3-cell w3-button w3-teal ",`type`:="text",
      data("hold-value"):=optionText, data("style-bgcolor"):="w3-teal" , value:=optionText, placeholder:="Option Text").render
    //val inputLabel = label(cls:="kj-option-hold", value := optionText).render

    //create results
    val image = div(cls:="w3-center",
      img(cls:="awi-flip-image", id:=thePreview, src:=imgsrc,`for`:=theImage, alt:="Image")
    ).render

    val  theToolbar = "FlipTool" + pageNum
    //val  theMediabar = "MediaNum" + pageNum
    val toolbar =  div( cls:="kj-flipbar" , id:=theToolbar,
      label( image,
            cls:="w3-xxlarge w3-blue",
            `for`:=theImage,
             input(id:=theImage, `type`:="file", multiple, hidden))).render

    val nextAction = div(cls:="kj-flip-prop w3-cell", toolbar).render

    val flipResult = div(cls:="kj-flip-result w3-cell",nextAction).render


    // cretae option item
    val flipItem = div(cls:="kj-flip-item w3-cell-row w3-padding",inputBox,closeBtn,flipResult).render


    // options & result add
    options.insertBefore(flipItem,addBtn)

    // other event processing
    inputBox.onchange = (e: dom.Event) => {
      println("text change", e.target.asInstanceOf[html.Input].value)
      e.srcElement.setAttribute("data-hold-value",e.target.asInstanceOf[html.Input].value)
    }


    val imageFile = dom.window.document.getElementById(theImage).asInstanceOf[html.Input]
    val preview = dom.window.document.getElementById(thePreview).asInstanceOf[html.Image]
    imageFile.onchange = RenderImage(image,"awi-flip-image",preview,imageFile)

    closeBtn.onclick = (e:dom.Event) => {
      println("*****remove child*****",flipItem)
      options.removeChild(flipItem)
    }

    return flipItem

  }

  // Add RuleItem with Del button & Rule Result
  def NewRuleDel(options : html.Div, optionValue : String , optionCond : String, optionState : String, optionAction : String, addBtn : html.Button): html.Div = {


    println(optionValue,optionCond,optionState,optionAction)
    //create option label for display
    val closeBtn = button (i(cls:="fa fa-close"),cls:="kj-option-close  w3-hide-small",value:="center").render


    val ifSelect = select(cls:= "kj-rule-if w3-col s6", data("if-value"):= optionCond,
      option("Sum of Score",value:="SUM"),option("Max Chosen Type",value:="MAX") ).render
    ifSelect.value = optionCond

    val stateSelect = select(cls:= "kj-rule-state w3-col s6", data("state-value"):= optionState,
      option("=",value:="EQ"),option(">",value:="GT") ,option("<",value:="LT") ).render
    stateSelect.value = optionState


    val inputBox = input(cls:="kj-rule-val w3-col s6",`type`:="text",
      data("hold-value"):=optionValue, value:=optionValue, placeholder:="Value").render
    //val inputLabel = label(cls:="kj-option-hold", value := optionText).render

    //create results
    val actSelect = select(cls:= "kj-rule-action w3-col s6", data("action-value"):= optionAction, option("+ 1",value:="1"),option("+ 2",value:="2"),option("+ 3",value:="3")
      ,option("+ 4",value:="4") ,option("+ 5",value:="5") ,option("+ 6",value:="6")
      ,option("+ 7",value:="7") ,option("+ 8",value:="8") ,option("+ 9",value:="9"),option("+ 10",value:="10") ).render
    actSelect.value = optionAction

    val nextAction = div(cls:="kj-rule-prop w3-row", label(cls:="w3-col s4", "Current"),actSelect).render

    val optionResult = div(cls:="kj-rule-result w3-cell",div(cls:="w3-row",label(cls:="w3-col s4","If  "),ifSelect),
                                                         div(cls:="w3-row",label(cls:="w3-col s4","STATE"),stateSelect),
                                                         div(cls:="w3-row",label(cls:="w3-col s4","Value"),inputBox),br,
                                                         div(cls:="w3-row",label(cls:="w3-col s12","Action")),

                                                         nextAction).render


    // cretae option item
    val optionItem = div(cls:="kj-rule-item w3-cell-row w3-padding",closeBtn,optionResult).render


    // options & result add
    options.insertBefore(optionItem,addBtn)

    // other event processing
    ifSelect.onchange = (e :dom.Event) => {
      //println("select change", e.target.asInstanceOf[html.Select].value)
      e.srcElement.setAttribute("data-if-value",e.target.asInstanceOf[html.Select].value)
    }

    stateSelect.onchange = (e :dom.Event) => {
      //println("select change", e.target.asInstanceOf[html.Select].value)
      e.srcElement.setAttribute("data-state-value",e.target.asInstanceOf[html.Select].value)
    }

    inputBox.onchange = (e: dom.Event) => {
      println("text change", e.target.asInstanceOf[html.Input].value)
      e.srcElement.setAttribute("data-hold-value",e.target.asInstanceOf[html.Input].value)
    }

    actSelect.onchange = (e :dom.Event) => {
      println("select change", e.target.asInstanceOf[html.Select].value)
      e.srcElement.setAttribute("data-action-value",e.target.asInstanceOf[html.Select].value)
    }

    closeBtn.onclick = (e:dom.Event) => {
      println("*****remove child*****",optionItem)
      options.removeChild(optionItem)
    }

    return optionItem

  }


  def OptionAdd(card:html.Div,target:html.Div) : Function1[Event,_]  = (e:dom.Event) => {

    //body compose
    val theImage = "OptNum"+pageNum
    val thePreview = "PreNum"+pageNum
    pageNum += 1

    val addBtn = button(cls:="w3-button w3-block w3-blue","Add Action").render

    //create results
    /*val actSelect = select(cls:= "kj-page-action", data("action-value"):= "1",option("+ 1",value:="1"),option("+ 2",value:="2"),option("+ 3",value:="3")
      ,option("+ 4",value:="4") ,option("+ 5",value:="5") ,option("+ 6",value:="6")
      ,option("+ 7",value:="7") ,option("+ 8",value:="8") ,option("+ 9",value:="9"),option("+ 10",value:="10") ).render */

    //****** currently nextAction not used. default action is just next page
    //val nextAction = div(cls:="w3-left w3-hide", label("Action Next Page = Current "),actSelect).render

    //val options = div(cls:="w3-center",addBtn,nextAction).render
    val options = div(cls:="w3-center",addBtn).render


    //toolbox create
    val pageBtn = CreatePageBtn(target)

    val title = div(input("TITLE",cls:="w3-xlarge")).render
    val optPage = div(cls:="w3-cell-row kj-row" , div(cls:="awi-data awi-option-page" ,options),div(cls:="kj-actbar ",pageBtn)).render

    //evnet processing

    PageBtnAction(pageBtn,card,target,optPage)

    optPage.onclick = ActiveToggle(target,optPage,options,"awi-option-page")

    //render
    if (curPage != null)
      target.insertBefore(optPage,curPage.nextSibling)
    else
      target.appendChild(optPage)


    //post event after append to target

    val inputBox = NewOptionDel(options,"Next","1",addBtn)
    inputBox.onclick = ActiveToggle(target,optPage,options,"kj-option-val")
    //NewOptionDel(options,"",addBtn)
    //NewOptionDel(options,"",addBtn)

    //Page Default Action
    /*actSelect.onchange = (e :dom.Event) => {
      println("select change", e.target.asInstanceOf[html.Select].value)
      e.srcElement.setAttribute("data-action-value",e.target.asInstanceOf[html.Select].value)
    }*/


    addBtn.onclick = (e:dom.Event) => {
      val inputBox = NewOptionDel(options,"Next", "1",addBtn)
      inputBox.onclick = ActiveToggle(target,optPage,options,"kj-option-val")
    }
    PageBtnCheck(pageBtn,card,target,optPage)

    title.click()
  }

  def FlipAdd(card:html.Div,target:html.Div) : Function1[Event,_]  = (e:dom.Event) => {

    //body compose
    val theImage = "FlipNum"+pageNum
    pageNum += 1

    val addBtn = button(cls:="w3-button w3-block w3-blue","Add Flip").render

    val options = div(cls:="w3-center",div(cls:="",label("Choice"), label(cls:="w3-right w3-margin-right","Result Image")),addBtn).render

    val editor = div( cls:="awi-editor w3-border").render

    val explain = div(label("Result Explain")).render

    val  theToolbar = "ToolPage" + pageNum
    pageNum += 1

    val toolbar =  div( cls:="kj-pagebar" , id:=theToolbar,
      span(" "),
      button ("작게",cls:="ql-size",value:="small"),
      button ("표준",cls:="ql-size"),
      button ("크게",cls:="ql-size",value:="large"),
      button ("제목",cls:="ql-size",value:="huge"),
      span(" "),
      button ("B",cls:="ql-bold",style:="font-style:Bold"),
      button ("I",cls:="ql-italic",style:="font-style:Italic"),
      button ("U",cls:="ql-underline",style:="text-decoration: underline;"),
      button( "H", cls:="ql-background", value:="yellow",style:="background-color:yellow"),
      button (i(cls:="fa fa-align-left"),cls:="ql-align w3-hide-small",style:="height:90%;font-size:15px"),
      button (i(cls:="fa fa-align-center"),cls:="ql-align w3-hide-small",value:="center",style:="height:90%;font-size:15px"),
      button (i(cls:="fa fa-align-right"),cls:="ql-align w3-hide-small",value:="right",style:="height:90%;font-size:15px")
    ).render


    //toolbox create
    val pageBtn = CreatePageBtn(target)

    val title = div(input("TITLE",cls:="w3-xlarge")).render
    val optPage = div(cls:="w3-cell-row kj-row" , div(cls:="awi-data awi-flip-page" ,options,explain,editor),div(cls:="kj-actbar ",toolbar,pageBtn)).render

    //evnet processing

    PageBtnAction(pageBtn,card,target,optPage)

    //editor.onclick = ActiveToggle(target,optPage,editor,true)
    optPage.onclick = ActiveToggle(target,optPage,editor,"awi-flip-page")
    editor.onclick = ActiveToggle(target,optPage,editor,"awi-edior")

    //render
    if (curPage != null)
      target.insertBefore(optPage,curPage.nextSibling)
    else
      target.appendChild(optPage)


    //post event after append to target


    val inputBox = NewFlipDel(options,"Next","./images/story_small.jpg",addBtn)
    inputBox.onclick = ActiveToggle(target,optPage,options,"kj-flip-val")

    //NewOptionDel(options,"",addBtn)
    //NewOptionDel(options,"",addBtn)

    //Page Default Action
    /*actSelect.onchange = (e :dom.Event) => {
      println("select change", e.target.asInstanceOf[html.Select].value)
      e.srcElement.setAttribute("data-action-value",e.target.asInstanceOf[html.Select].value)
    }*/


    addBtn.onclick = (e:dom.Event) => {
      val inputBox = NewFlipDel(options,"Next", "./images/story_small.jpg",addBtn)
      inputBox.onclick = ActiveToggle(target,optPage,options,"kj-flip-val")
    }
    PageBtnCheck(pageBtn,card,target,optPage)
    CreateQuill(editor,"#"+theToolbar,false)
    title.click()
  }



  def OptionLoad(card:html.Div,target:html.Div,old_option:html.Div)  {

    //body compose
    val theImage = "OptNum"+pageNum
    val thePreview = "PreNum"+pageNum
    pageNum += 1

    var defaultActVal : String = "1"

    val addBtn = button(cls:="w3-button w3-block w3-blue","Add Action").render

    val defaultAction = old_option.asInstanceOf[html.Div].getElementsByClassName("kj-page-action")(0)
    if (defaultAction==null || js.isUndefined(defaultAction)) defaultActVal = "1"
    else defaultActVal = defaultAction.asInstanceOf[html.Div].getAttribute("data-action-value")

println(defaultActVal)
    if (defaultActVal==null) defaultActVal = "1"


    //create results
    val actSelect = select(cls:= "kj-page-action", data("action-value"):= defaultActVal, option("+ 1",value:="1"),option("+ 2",value:="2"),option("+ 3",value:="3")
      ,option("+ 4",value:="4") ,option("+ 5",value:="5") ,option("+ 6",value:="6")
      ,option("+ 7",value:="7") ,option("+ 8",value:="8") ,option("+ 9",value:="9"),option("+ 10",value:="10") ).render
    actSelect.value = defaultActVal

    //****** currently nextAction not used. default action is just next page
    val nextAction = div(cls:="w3-left w3-hide", label("Action Next Page = Current "),actSelect).render

    val new_options = div(cls:="w3-center",addBtn,nextAction).render

    val pageBtn = CreatePageBtn(target)


    val title = div(input("TITLE",cls:="w3-xlarge")).render
    val optPage = div(cls:="w3-cell-row kj-row" , div(cls:="awi-data awi-option-page" ,new_options),div(cls:="kj-actbar ",pageBtn)).render

    PageBtnAction(pageBtn,card,target,optPage)

    optPage.onclick = ActiveToggle(target,optPage,new_options,"awi-option-page")

    //render
    if (curPage != null)
      target.insertBefore(optPage,curPage.nextSibling)
    else
      target.appendChild(optPage)

    //post event after append to target

    val option_items = old_option.getElementsByClassName("kj-option-item") //input text for option value
    for (option <- option_items) {
      val label = option.asInstanceOf[html.Div].getElementsByClassName("kj-option-val")(0)
      val optionText = label.asInstanceOf[html.Div].getAttribute("data-hold-value")

      val selAct = option.asInstanceOf[html.Div].getElementsByClassName("kj-option-action")(0)
      var actionText : String = "1"
      if (!js.isUndefined(selAct))
        actionText = selAct.asInstanceOf[html.Div].getAttribute("data-action-value")


      //if (actionText==null || js.isUndefined(actionText)) actionText = "1"

      println("options",actionText)

      // create option_item  -- add option before addBtn
      val inputBox = NewOptionDel(new_options,optionText,actionText,addBtn)
      inputBox.onclick = ActiveToggle(target,optPage,new_options,"kj-option-val")
    }

    //Page Default Action
    actSelect.onchange = (e :dom.Event) => {
      println("select change", e.target.asInstanceOf[html.Select].value)
      e.srcElement.setAttribute("data-action-value",e.target.asInstanceOf[html.Select].value)
    }


    addBtn.onclick = (e:dom.Event) => {
      val inputBox = NewOptionDel(new_options,"Next","1",addBtn) //-- add option before addBtn
      inputBox.onclick = ActiveToggle(target,optPage,new_options,"kj-option-val")

    }

    PageBtnCheck(pageBtn,card,target,optPage)

    title.click()
  }

  def FlipLoad(card:html.Div,target:html.Div,old_flip:html.Div)  {

    //body compose
    val theImage = "FlipNum"+pageNum
    val thePreview = "FlipPreNum"+pageNum
    pageNum += 1

    val  theToolbar = "ToolPage" + pageNum
    pageNum += 1

    var editor = div( cls:="awi-editor w3-border").render
    val oldeditor = old_flip.getElementsByClassName("awi-editor")
    if (oldeditor.length > 0) {
      editor = oldeditor(0).asInstanceOf[html.Div]
      editor.className += " w3-border"
    }


    val toolbar =  div( cls:="kj-pagebar" , id:=theToolbar,
      span(" "),
      button ("작게",cls:="ql-size",value:="small"),
      button ("표준",cls:="ql-size"),
      button ("크게",cls:="ql-size",value:="large"),
      button ("제목",cls:="ql-size",value:="huge"),
      span(" "),
      button ("B",cls:="ql-bold",style:="font-style:Bold"),
      button ("I",cls:="ql-italic",style:="font-style:Italic"),
      button ("U",cls:="ql-underline",style:="text-decoration: underline;"),
      button( "H", cls:="ql-background", value:="yellow",style:="background-color:yellow"),
      button (i(cls:="fa fa-align-left"),cls:="ql-align w3-hide-small",style:="height:90%;font-size:15px"),
      button (i(cls:="fa fa-align-center"),cls:="ql-align w3-hide-small",value:="center",style:="height:90%;font-size:15px"),
      button (i(cls:="fa fa-align-right"),cls:="ql-align w3-hide-small",value:="right",style:="height:90%;font-size:15px")
    ).render


    val addBtn = button(cls:="w3-button w3-block w3-blue","Add Flip").render

    val explain = div(label("Result Explain")).render

    val new_options = div(cls:="w3-center",div(cls:="",label("Choice"), label(cls:="w3-right w3-margin-right","Result Image")),addBtn).render

    val pageBtn = CreatePageBtn(target)


    val title = div(input("TITLE",cls:="w3-xlarge")).render
    val optPage = div(cls:="w3-cell-row kj-row" , div(cls:="awi-data awi-flip-page" ,new_options,explain,editor),div(cls:="kj-actbar ",toolbar,pageBtn)).render

    PageBtnAction(pageBtn,card,target,optPage)

    optPage.onclick = ActiveToggle(target,optPage,new_options,"awi-flip-page")
    editor.onclick = ActiveToggle(target,optPage,editor,"awi-editor")

    //render
    if (curPage != null)
      target.insertBefore(optPage,curPage.nextSibling)
    else
      target.appendChild(optPage)

    //post event after append to target

    val flip_items = old_flip.getElementsByClassName("kj-flip-item") //input text for option value
    for (flip <- flip_items) {
      val label = flip.asInstanceOf[html.Div].getElementsByClassName("kj-flip-val")(0)
      val optionText = label.asInstanceOf[html.Div].getAttribute("data-hold-value")

      val image = flip.asInstanceOf[html.Div].getElementsByClassName("awi-flip-image")(0).asInstanceOf[html.Image]
      // create option_item  -- add option before addBtn
      val inputBox = NewFlipDel(new_options,optionText,image.src,addBtn)
      inputBox.onclick = ActiveToggle(target,optPage,new_options,"kj-flip-val")
    }

    addBtn.onclick = (e:dom.Event) => {
      NewFlipDel(new_options,"Next","./images/story_small.jpg",addBtn) //-- add option before addBtn
    }

    PageBtnCheck(pageBtn,card,target,optPage)

    title.click()
    CreateQuill(editor,"#"+theToolbar)
  }



  def TypeRuleAdd(card:html.Div,target:html.Div) : Function1[Event,_]  = (e:dom.Event) => {

    //body compose
    val addBtn = button(cls:="w3-button w3-block w3-blue","Add Rule").render

    //create results
    /*val actSelect = select(cls:= "kj-page-action", data("action-value"):= "1",option("+ 1",value:="1"),option("+ 2",value:="2"),option("+ 3",value:="3")
      ,option("+ 4",value:="4") ,option("+ 5",value:="5") ,option("+ 6",value:="6")
      ,option("+ 7",value:="7") ,option("+ 8",value:="8") ,option("+ 9",value:="9"),option("+ 10",value:="10") ).render*/

    //****** currently nextAction not used. default action is just next page
    //val nextAction = div(cls:="w3-left w3-hide", label("Action Next Page = Current "),actSelect).render

    //val options = div(cls:="w3-center",addBtn,nextAction).render
    val options = div(cls:="w3-center",addBtn).render

    //toolbox create
    val pageBtn = CreatePageBtn(target)

    val title = div(input("TITLE",cls:="w3-xlarge")).render
    val optPage = div(cls:="w3-cell-row kj-row" , div(cls:="awi-data awi-rule-page" ,options),div(cls:="kj-actbar ",pageBtn)).render

    //evnet processing


    PageBtnAction(pageBtn,card,target,optPage)

    optPage.onclick = ActiveToggle(target,optPage,options,"awi-rule-page")

    //render
    if (curPage != null)
      target.insertBefore(optPage,curPage.nextSibling)
    else
      target.appendChild(optPage)


    //post event after append to target

    NewRuleDel(options,"","SUM","EQ","1",addBtn)

    addBtn.onclick = (e:dom.Event) => {
      NewRuleDel(options,"","SUM","EQ","1",addBtn)
    }
    PageBtnCheck(pageBtn,card,target,optPage)

    title.click()
  }




  def TypeRuleLoad(card:html.Div,target:html.Div,old_option:html.Div)  {

    //body compose

    val addBtn = button(cls:="w3-button w3-block w3-blue","Add Rule").render


    //****** currently nextAction not used. default action is just next page
    //val nextAction = div(cls:="w3-left w3-hide", label("Action Next Page = Current "),actSelect).render

    val new_options = div(cls:="w3-center",addBtn).render

    val pageBtn = CreatePageBtn(target)


    val title = div(input("TITLE",cls:="w3-xlarge")).render
    val optPage = div(cls:="w3-cell-row kj-row" , div(cls:="awi-data awi-rule-page" ,new_options),div(cls:="kj-actbar ",pageBtn)).render

    PageBtnAction(pageBtn,card,target,optPage)

    optPage.onclick = ActiveToggle(target,optPage,new_options,"awi-rule-page")

    //render
    if (curPage != null)
      target.insertBefore(optPage,curPage.nextSibling)
    else
      target.appendChild(optPage)

    //post event after append to target

    val option_items = old_option.getElementsByClassName("kj-rule-item") //input text for option value
    for (option <- option_items) {
      val ruleIf = option.asInstanceOf[html.Div].getElementsByClassName("kj-rule-if")(0)
      var ruleIfStr : String = "SUM"
      if (!js.isUndefined(ruleIf))
        ruleIfStr = ruleIf.asInstanceOf[html.Div].getAttribute("data-if-value")

      val ruleState = option.asInstanceOf[html.Div].getElementsByClassName("kj-rule-state")(0)
      var ruleStateStr : String = "EQ"
      if (!js.isUndefined(ruleState))
        ruleStateStr = ruleState.asInstanceOf[html.Div].getAttribute("data-state-value")

      val ruleValue = option.asInstanceOf[html.Div].getElementsByClassName("kj-rule-val")(0)
      var ruleValueStr : String = ""
      if (!js.isUndefined(ruleValue))
        ruleValueStr = ruleValue.asInstanceOf[html.Div].getAttribute("data-hold-value")

      //val optionText = label.asInstanceOf[html.Div].getAttribute("data-hold-value")

      val selAct = option.asInstanceOf[html.Div].getElementsByClassName("kj-rule-action")(0)
      var actionText : String = "1"
      if (!js.isUndefined(selAct))
        actionText = selAct.asInstanceOf[html.Div].getAttribute("data-action-value")


      //if (actionText==null || js.isUndefined(actionText)) actionText = "1"

      //println("options",actionText)

      // create option_item  -- add option before addBtn
      val option_item = NewRuleDel(new_options,ruleValueStr,ruleIfStr,ruleStateStr,actionText,addBtn)


    }


    addBtn.onclick = (e:dom.Event) => {
      NewRuleDel(new_options,"","SUM","EQ","1",addBtn)
    }
    PageBtnCheck(pageBtn,card,target,optPage)


    title.click()
  }

  // Add TypeItem with Del button & Option Score before addBtn
  def AddTypeDel(types : html.Div, optionText : String , optionId : String ,optionScore : String, scoreId : String, addBtn : html.Button): html.Div = {

    //create option label for display
    val closeBtn = button (i(cls:="fa fa-close"),cls:="kj-option-close  w3-hide-small",value:="center").render
    //println("optionId",scoreId)

    val typeid = Util.guid()
    val inputBox = input(id:=typeid, cls:="kj-type-val  w3-col s8 w3-input w3-padding",`type`:="text", data("hold-value"):=optionText, value:=optionText, placeholder:="Type Name").render
    if (!optionId.equals(""))
      inputBox.id = optionId
    //val inputLabel = label(cls:="kj-option-hold", value := optionText).render

    val scoreIdNew = Util.guid()
    val scoreBox = input(id:=scoreIdNew, cls:="kj-score-val w3-border w3-col s4 w3-padding",`type`:="text", data("hold-value"):=optionScore, data("type-id"):=optionId, value:=optionScore, placeholder:="Score").render
    //println("scoreId",scoreId)
    if (!scoreId.equals(""))
      scoreBox.id = scoreId

    //create results
    /*
    val actSelect = select(cls:= "kj-type-action", data("action-value"):= optionAction, option("+ 1",value:="1"),option("+ 2",value:="2"),option("+ 3",value:="3")
      ,option("+ 4",value:="4") ,option("+ 5",value:="5") ,option("+ 6",value:="6")
      ,option("+ 7",value:="7") ,option("+ 8",value:="8") ,option("+ 9",value:="9"),option("+ 10",value:="10") ).render
    actSelect.value = optionAction */

    //val nextAction = div(cls:="kj-type-prop w3-cell", scoreBox).render

    //val optionResult = div(cls:="kj-type-score w3-col s4",scoreBox).render


    // cretae option item
    val optionItem = div(cls:="kj-type-item w3-row w3-padding ",inputBox,scoreBox,closeBtn).render


    // options & result add
    types.insertBefore(optionItem,addBtn)

    // other event processing
    inputBox.onchange = (e: dom.Event) => {
      e.srcElement.setAttribute("data-hold-value",e.target.asInstanceOf[html.Input].value)
      UpdateAllTypeVal(e.srcElement.id,e.target.asInstanceOf[html.Input].value)
    }

    scoreBox.onchange = (e: dom.Event) => {
      e.srcElement.setAttribute("data-hold-value",e.target.asInstanceOf[html.Input].value)
      val datatypeid = e.srcElement.getAttribute("data-type-id")
      UpdateAllScoreVal(datatypeid,e.target.asInstanceOf[html.Input].value)
    }

    closeBtn.onclick = (e:dom.Event) => {
      println("*****remove child*****",optionItem)
      types.removeChild(optionItem)
    }

    return optionItem

  }

  def TypeCreateAdd(card:html.Div,target:html.Div) : Function1[Event,_]  = (e:dom.Event) => {

    //body compose

    val header =   div(cls:="w3-cell-row w3-padding",label("Type Name",cls:="w3-cell"),label("Type Score",cls:="w3-cell")).render
    val addTypeBtn = button(cls:="w3-button w3-block w3-blue","Add Type").render
    val typeSelect = select(cls:="w3-cell kj-compare-type", data("hold-value"):="score",
      option("Score Bar",value:="score"), option("Type Pie",value:="type")).render

    val compareType = div(cls:="w3-cell-row w3-padding",label("Compare Type",cls:="w3-cell"),typeSelect).render


    val types = div(cls:="w3-center",header,addTypeBtn,compareType).render

    //toolbox create
    val pageBtn = CreatePageBtn(target)

    val optPage = div(cls:="w3-cell-row kj-row" , div(cls:="awi-data awi-typecreate-page" ,types),div(cls:="kj-actbar ",pageBtn)).render

    //evnet processing

    typeSelect.onchange = (e:dom.Event) => {
      e.srcElement.setAttribute("data-hold-value",e.target.asInstanceOf[html.Select].value)
    }
    PageBtnAction(pageBtn,card,target,optPage)

    optPage.onclick = ActiveToggle(target,optPage,types)

    //render
    if (curPage != null)
      target.insertBefore(optPage,curPage.nextSibling)
    else
      target.appendChild(optPage)


    //post event after append to target


    AddTypeDel(types,"","","0","",addTypeBtn)  //create default one type -- before addTypeBtn


    addTypeBtn.onclick = (e:dom.Event) => {
      AddTypeDel(types,"","","0","",addTypeBtn)  //create default one type -- before addTypeBtn
    }

    PageBtnCheck(pageBtn,card,target,optPage)

  }

  def TypeResAdd(main_stream : html.Div,card:html.Div,target:html.Div,ctype:String="bar",dataType:String="Count") : Function1[Event,_]  = (e:dom.Event) => {

    import js.JSConverters._
    //body compose

    val typepage = main_stream.getElementsByClassName("awi-typecreate-page")
    if (typepage.length > 0 ) {
      val guid = Util.guid()
      val container =   div(cls:="kj-chart-result w3-padding",
           canvas(id:=guid , style:="width:100%")).render
      val typeSelect = select(cls:= "kj-result-type",
        option("Bar",value:="bar"),option("Pie",value:="pie"),
        option("horizontalBar",value:="horizontalBar"),option("polarArea",value:="polarArea"),
        option("line",value:="line")).render
      typeSelect.value = ctype

      val dataSelect = select(cls:= "kj-result-data",
        option("Count",value:="Count"),option("Sum",value:="Sum")).render
      dataSelect.value = dataType
      val graphCheck = input(`type`:="checkbox", checked, cls:="graphCheck").render
      val lastCheck = input(`type`:="checkbox", checked, cls:="lastCheck").render
      val totalCheck = input(`type`:="checkbox", checked, cls:="totalCheck").render
      val likelyCheck = input(`type`:="checkbox", checked, cls:="typeCheck").render

      val lastSelect = div(label("Last Select  ", cls:="w-fixed2"),input( cls:="w-fixed2",disabled)).render
      val totalSelect = div(label("Total Score  ", cls:="w-fixed2"),input( cls:="w-fixed2",disabled)).render
      val likelySelect = div(label("Your type is likely  ", cls:="w-fixed2"),input( cls:="w-fixed2",disabled)).render

      val result = div(cls:="w3-center",
        div(cls:="kj-result-check",graphCheck, label("Graph", cls:="w-fixed"),lastCheck, label("Last Selection", cls:="w-fixed")),
        div(cls:="kj-result-check",totalCheck, label("total Score", cls:="w-fixed"), likelyCheck, label("Type result", cls:="w-fixed")),
        lastSelect,totalSelect,likelySelect,
        container,typeSelect,dataSelect).render

      val typevals = typepage(0).asInstanceOf[html.Div].getElementsByClassName("kj-type-val")
      val typenum = typevals.length
      var label_data : mutable.Seq[String] = mutable.Seq.empty[String]
      //find # of type label
      //add label & input box for each type
      for (i <- 0 until typenum) {
        val typeId = typevals(i).asInstanceOf[html.Input].id
        val typeName = typevals(i).asInstanceOf[html.Input].getAttribute("data-hold-value")
        label_data = label_data :+ typeName
      }
      //toolbox create
      val pageBtn = CreatePageBtn(target)

      val optPage = div(cls := "w3-cell-row kj-row", div(cls := "awi-data awi-typeres-page", result), div(cls := "kj-actbar ", pageBtn)).render

      //evnet processing
      graphCheck.onclick = (e:dom.Event) => {
        if (graphCheck.checked) {
          container.className = container.className.replaceAll(" w3-hide", "")
          typeSelect.className = typeSelect.className.replaceAll(" w3-hide", "")
          dataSelect.className = dataSelect.className.replaceAll(" w3-hide", "")
          typeSelect.setAttribute("data-graphCheck","on")
        }else {
          container.className += " w3-hide"
          typeSelect.className += " w3-hide"
          dataSelect.className += " w3-hide"
          typeSelect.setAttribute("data-graphCheck","off")
        }
      }
      lastCheck.onclick = (e:dom.Event) => {
        if (lastCheck.checked) {
          lastSelect.className = lastSelect.className.replaceAll(" w3-hide", "")
          typeSelect.setAttribute("data-lastCheck","on")
        }else {
          lastSelect.className += " w3-hide"
          typeSelect.setAttribute("data-lastCheck","off")
        }
      }
      totalCheck.onclick = (e:dom.Event) => {
        if (totalCheck.checked) {
          totalSelect.className = totalSelect.className.replaceAll(" w3-hide", "")
          typeSelect.setAttribute("data-totalCheck","on")
        }else {
          totalSelect.className += " w3-hide"
          typeSelect.setAttribute("data-totalCheck","off")
        }
      }
      likelyCheck.onclick = (e:dom.Event) => {
        if (likelyCheck.checked) {
          likelySelect.className = likelySelect.className.replaceAll(" w3-hide", "")
          typeSelect.setAttribute("data-likelyCheck","on")
        }else {
          likelySelect.className += " w3-hide"
          typeSelect.setAttribute("data-likelyCheck","off")
        }
      }


      PageBtnAction(pageBtn, card, target, optPage)

      optPage.onclick = ActiveToggle(target, optPage, result,"awi-typeres-page")

      //render
      if (curPage != null)
        target.insertBefore(optPage, curPage.nextSibling)
      else
        target.appendChild(optPage)


      //post event after append to target
      //var label_data = js.Array("Red", "Blue", "Yellow", "Green", "Purple", "Orange")
      var datas = js.Array(7, 9, 3, 5, 2, 3)
      var sums = js.Array(17, 19, 13, 15, 12, 13)
      var chart = myChart.ResultChart(guid.toString, ctype, label_data.toJSArray, datas, "Score")

      typeSelect.onchange = (e :dom.Event) => {
        println("select change", e.target.asInstanceOf[html.Select].value)
        //chart.changeOpt(e.target.asInstanceOf[html.Select].value)
        //myChart.ChangeChart(chart,"bar")
        typeSelect.setAttribute("data-hold-value",e.target.asInstanceOf[html.Select].value)
        chart = myChart.UpdateChart(chart,guid.toString, e.target.asInstanceOf[html.Select].value, label_data.toJSArray, datas)
      }
      dataSelect.onchange = (e :dom.Event) => {
        println("select change", e.target.asInstanceOf[html.Select].value)
        //chart.changeOpt(e.target.asInstanceOf[html.Select].value)
        //myChart.ChangeChart(chart,"bar")
        dataSelect.setAttribute("data-hold-value",e.target.asInstanceOf[html.Select].value)
        chart = myChart.UpdateData(chart,guid.toString, label_data.toJSArray, sums)
      }

      PageBtnCheck(pageBtn,card,target,optPage)

    } // type length > 0
  }


  def TypeCreateLoad(card:html.Div,target:html.Div,old_option:html.Div)  {

    val header =   div(cls:="w3-cell-row w3-padding",label("Type Name",cls:="w3-cell"),label("Type Score",cls:="w3-cell")).render

    val addTypeBtn = button(cls:="w3-button w3-block w3-blue","Add Type").render

    val typeSelect = select(cls:="w3-cell kj-compare-type", data("hold-value"):="score", option("Score Bar",value:="score"), option("Type Pie",value:="type")).render

    val compareType = div(cls:="w3-cell-row w3-padding",label("Compare Type",cls:="w3-cell"),typeSelect).render


    val new_types = div(cls:="w3-center",header,addTypeBtn,compareType).render


    val pageBtn = CreatePageBtn(target)

    val optPage = div(cls:="w3-cell-row kj-row" , div(cls:="awi-data awi-typecreate-page" ,new_types),div(cls:="kj-actbar ",pageBtn)).render

    PageBtnAction(pageBtn,card,target,optPage)

    optPage.onclick = ActiveToggle(target,optPage,new_types)

    //render
    if (curPage != null)
      target.insertBefore(optPage,curPage.nextSibling)
    else
      target.appendChild(optPage)

    //post event after append to target

    val option_items = old_option.getElementsByClassName("kj-type-item") //input text for option value
    for (option <- option_items) {
      //chekc score value
      var optionScore : String = ""
      var scoreId : String = null
      val scoreBoxes = option.asInstanceOf[html.Div].getElementsByClassName("kj-score-val")
      if (scoreBoxes.length >0) {
        val scoreBox = scoreBoxes(0).asInstanceOf[html.Input]
        optionScore = scoreBox.getAttribute("data-hold-value")
        scoreId = scoreBox.id
      }

      //check type value
      val inputBox = option.asInstanceOf[html.Div].getElementsByClassName("kj-type-val")(0)
      val optionText = inputBox.asInstanceOf[html.Input].getAttribute("data-hold-value")
      val optionId = inputBox.asInstanceOf[html.Input].id

      /*val selAct = option.asInstanceOf[html.Div].getElementsByClassName("kj-type-action")(0)
      var actionText : String = "1"
      if (!js.isUndefined(selAct))
        actionText = selAct.asInstanceOf[html.Div].getAttribute("data-action-value") */


      // create option_item  -- add option before addBtn
      println("create new option",optionId)
      val option_item = AddTypeDel(new_types,optionText,optionId,optionScore,scoreId,addTypeBtn)
    }

    val oldTypes = old_option.getElementsByClassName("kj-compare-type")
    if (oldTypes.length > 0) {
      val oldType = oldTypes(0).asInstanceOf[html.Select].getAttribute("data-hold-value")
      typeSelect.setAttribute("data-hold-value",oldType)
      typeSelect.value = oldType
    }

    typeSelect.onchange = (e:dom.Event) => {
      e.srcElement.setAttribute("data-hold-value",e.target.asInstanceOf[html.Select].value)
    }



    //Page Default Action -- not used
    /*
    actSelect.onchange = (e :dom.Event) => {
      println("select change", e.target.asInstanceOf[html.Select].value)
      e.srcElement.setAttribute("data-action-value",e.target.asInstanceOf[html.Select].value)
    }*/


    addTypeBtn.onclick = (e:dom.Event) => {
      AddTypeDel(new_types,"","","","",addTypeBtn) //-- add option before addBtn
    }

    PageBtnCheck(pageBtn,card,target,optPage)

  }


  // Add testItem without Del button & Result before addBtn
  def AddTestOne(types : html.Div, typeId:String, typeName:String, typeScore: String, optionText : String ,addBtn : html.Button): html.Input = {

    //create option label for display
    val labelText = label(typeName,cls:="kj-map-type w3-cell",data("hold-value"):=typeName,data("map-id"):= typeId).render
    val inputBox = input( cls:=s"kj-map-val w3-button w3-cell w3-teal",`type`:="text", data("map-id"):= typeId,
        data("hold-value"):=optionText, data("score-value"):=typeScore, data("style-bgcolor"):="w3-teal" , value:=optionText, placeholder:="Type Here").render
    val scoreBox = input( cls:=s"kj-map-score w3-input w3-border w3-cell",`type`:="text", data("map-id"):= typeId,
      data("hold-value"):=typeScore, value:=typeScore).render
    //val inputLabel = label(cls:="kj-option-hold", value := optionText).render

    //val mapResult = div(cls:="kj-map-result").render


    // cretae option item
    val optionItem = div(cls:="kj-map-item w3-cell-row w3-padding",labelText,inputBox,scoreBox).render


    // options & result add
    types.insertBefore(optionItem,addBtn)


    // other event processing
    inputBox.onchange = (e: dom.Event) => {
      println("text change", e.target.asInstanceOf[html.Input].value)
      e.srcElement.setAttribute("data-hold-value",e.target.asInstanceOf[html.Input].value)
    }

    scoreBox.onchange = (e: dom.Event) => {
      println("score change", e.target.asInstanceOf[html.Input].value)
      e.srcElement.setAttribute("data-hold-value",e.target.asInstanceOf[html.Input].value)
      inputBox.setAttribute("data-score-value",e.target.asInstanceOf[html.Input].value)
    }

    return inputBox

  }


  def UpdateAllTypeVal(id:String,newValue:String) {

    println("label change",id,newValue)

    val maptypes = main_saved.getElementsByClassName("kj-map-type")
    for (mapval <-maptypes) {
      println(mapval.asInstanceOf[html.Label].getAttribute("data-map-id"))
      if (mapval.asInstanceOf[html.Label].getAttribute("data-map-id").equals(id)) {
        mapval.asInstanceOf[html.Label].textContent = newValue
        mapval.asInstanceOf[html.Label].setAttribute("data-hold-value",newValue)
      }
    }
  }

  def UpdateAllScoreVal(id:String,newValue:String) {

    println(id,newValue)

    val maptypes = main_saved.getElementsByClassName("kj-map-val")
    for (mapval <-maptypes) {
      println(mapval.asInstanceOf[html.Input].getAttribute("data-map-id"))
      if (mapval.asInstanceOf[html.Input].getAttribute("data-map-id").equals(id)) {
        //mapval.asInstanceOf[html.Label].textContent = newValue
        mapval.asInstanceOf[html.Input].setAttribute("data-score-value",newValue)
      }
    }
  }


  def TypeTestAdd(main_stream : html.Div,card:html.Div,target:html.Div) : Function1[Event,_]  = (e:dom.Event) => {

    //find type element from the main stream
    println("TypeTestAdd")

    val typepage = main_stream.getElementsByClassName("awi-typecreate-page")
    if (typepage.length > 0 ) {

      //find # of type label

      // do not show -- not needed but used for anchor
      val addTypeBtn = button(cls := "w3-button w3-block w3-hide", "Add Question").render

      val randomCheck = input(`type`:="checkbox", checked, cls:="randomCheck").render
      if (option_random) randomCheck.checked = true
      else randomCheck.checked = false

      val randomDiv = div(cls:="kj-random-check w3-right",randomCheck, label("Shuffle", cls:="w-fixed")).render
      val header =   div(cls:="w3-cell-row w3-padding",label("Type Name",cls:="kj-type-name w3-cell"),
        label("Choice or Trait",cls:="kj-type-choice w3-cell"),
        label("Score",cls:="kj-type-score w3-cell")).render

      if (option_random) randomDiv.setAttribute("data-randomCheck","on")
      else randomDiv.setAttribute("data-randomCheck","off")

      val types = div(cls := "w3-center", header,addTypeBtn,randomDiv).render

      //toolbox create
      val pageBtn = CreatePageBtn(target)

      val title = div(input("TITLE", cls := "w3-xlarge")).render
      val optPage = div(cls := "w3-cell-row kj-row", div(cls := "awi-data awi-typetest-page", header, types), div(cls := "kj-actbar ", pageBtn)).render

      //evnet processing

      randomCheck.onclick = (e:dom.Event) => {
        if (randomCheck.checked) {
          randomDiv.setAttribute("data-randomCheck","on")
          option_random = true
        }else {
          randomDiv.setAttribute("data-randomCheck","off")
          option_random = false
        }
      }

      PageBtnAction(pageBtn, card, target, optPage)

      optPage.onclick = ActiveToggle(target, optPage, types,"awi-typetest-page")

      //render
      if (curPage != null)
        target.insertBefore(optPage, curPage.nextSibling)
      else
        target.appendChild(optPage)

      // check type name & score
      val typeitems = typepage(0).asInstanceOf[html.Div].getElementsByClassName("kj-type-item")
      val typenum = typeitems.length


      //get & copy typeName & score from the typecreae-page
      for (typeitem <- typeitems) {
        val typeval = typeitem.asInstanceOf[html.Div].getElementsByClassName("kj-type-val")
        val typeId  = typeval(0).asInstanceOf[html.Input].id
        val typeName = typeval(0).asInstanceOf[html.Input].getAttribute("data-hold-value")

        val scoreval = typeitem.asInstanceOf[html.Div].getElementsByClassName("kj-score-val")
        val score = scoreval(0).asInstanceOf[html.Input].getAttribute("data-hold-value")

        val inputBox = AddTestOne(types, typeId, typeName, score, typeName,addTypeBtn) //create default one type -- before addTypeBtn
        inputBox.onclick = ActiveToggle(target, optPage, types,"kj-map-val")
      }

      PageBtnCheck(pageBtn,card,target,optPage)

    }//length >0
  }





  def TypeTestLoad(card:html.Div,target:html.Div,old_option:html.Div)  {

    val header =   div(cls:="w3-cell-row w3-padding",label("Type Name",cls:="kj-type-name w3-cell"),
      label("Choice or Trait",cls:="kj-type-choice w3-cell"),
      label("Score",cls:="kj-type-score w3-cell")).render

    val randomDiv = old_option.asInstanceOf[html.Div].getElementsByClassName("kj-random-check")
    if (randomDiv.length>0) {
      val randomStr = randomDiv(0).asInstanceOf[html.Div].getAttribute("data-randomCheck")
      if (randomStr == null || randomStr.equals("on")) option_random = true
      else option_random = false
    }
    val randomCheck = input(`type`:="checkbox", checked, cls:="randomCheck").render
    if (option_random) randomCheck.checked = true
    else randomCheck.checked = false

    val randomNewDiv = div(cls:="kj-random-check w3-right",randomCheck, label("Shuffle", cls:="w-fixed")).render

    if (option_random) randomNewDiv.setAttribute("data-randomCheck","on")
    else randomNewDiv.setAttribute("data-randomCheck","off")

    // do not show -- not needed but used for anchor
    val addTypeBtn = button(cls := "w3-button w3-block w3-hide", "Add Test").render

    val new_test = div(cls:="w3-center",header,addTypeBtn,randomNewDiv).render

    val pageBtn = CreatePageBtn(target)

    val optPage = div(cls := "w3-cell-row kj-row", div(cls := "awi-data awi-typetest-page", header, new_test), div(cls := "kj-actbar ", pageBtn)).render

    //event processing

    randomCheck.onclick = (e:dom.Event) => {
      if (randomCheck.checked) {
        randomNewDiv.setAttribute("data-randomCheck","on")
        option_random = true
      }else {
        randomNewDiv.setAttribute("data-randomCheck","off")
        option_random = false
      }
    }

    PageBtnAction(pageBtn,card,target,optPage)

    optPage.onclick = ActiveToggle(target,optPage,new_test,"awi-typetest-page")

    //renderBBBBBB
    if (curPage != null)
      target.insertBefore(optPage,curPage.nextSibling)
    else
      target.appendChild(optPage)

    //post event after append to target

    val option_items = old_option.getElementsByClassName("kj-map-item") //input text for option value
    for (option <- option_items) {
      val input_val = option.asInstanceOf[html.Div].getElementsByClassName("kj-map-val")(0)
      val maptype = option.asInstanceOf[html.Div].getElementsByClassName("kj-map-type")(0)

      val typeText = maptype.asInstanceOf[html.Label].getAttribute("data-hold-value")
      val typeId = maptype.asInstanceOf[html.Label].getAttribute("data-map-id")
      val typeMap = input_val.asInstanceOf[html.Input].getAttribute("data-hold-value")

      val typeScore = input_val.asInstanceOf[html.Input].getAttribute("data-score-value")
      if (typeScore == null) {
        val inputBox = AddTestOne(new_test, typeId, typeText, "1", typeMap, addTypeBtn)
        inputBox.onclick = ActiveToggle(target, optPage, new_test,"kj-map-val")
      }
      else {
        val inputBox = AddTestOne(new_test, typeId, typeText, typeScore, typeMap, addTypeBtn)
        inputBox.onclick = ActiveToggle(target, optPage, new_test,"kj-map-val")
      }

      // create option_item  -- add option before addBtn
    }
    PageBtnCheck(pageBtn,card,target,optPage)


  }


  def TypeResLoad(main_stream:html.Div,card:html.Div,target:html.Div,old_res:html.Div)  {

    import js.JSConverters._

    // do not show -- not needed but used for anchor
    val guid = Util.guid()
    val container =   div(cls:="kj-chart-result w3-padding",
      canvas(id:=guid/*, style:="width:400px;height:400px"*/)).render



    val graphCheck = input(`type`:="checkbox", checked, cls:="graphCheck").render
    val lastCheck = input(`type`:="checkbox", checked, cls:="lastCheck").render
    val totalCheck = input(`type`:="checkbox", checked, cls:="totalCheck").render
    val likelyCheck = input(`type`:="checkbox", checked, cls:="typeCheck").render

    val lastSelect = div(label("Last Select  ", cls:="w-fixed2"),input( cls:="w-fixed2",disabled)).render
    val totalSelect = div(label("Total Score  ", cls:="w-fixed2"),input( cls:="w-fixed2",disabled)).render
    val likelySelect = div(label("Your type is likely  ", cls:="w-fixed2"),input( cls:="w-fixed2",disabled)).render


    var graphVal = old_res.getElementsByClassName("kj-result-type")(0).asInstanceOf[html.Select].getAttribute("data-graphCheck")
    if (graphVal == null) graphVal = "on"
    if (graphVal.contains("off")) {
      graphCheck.checked = false; container.className += " w3-hide"
    }

    var lastVal = old_res.getElementsByClassName("kj-result-type")(0).asInstanceOf[html.Select].getAttribute("data-lastCheck")
    if (lastVal == null) lastVal = "on"
    if ( lastVal.contains("off")) {
      lastCheck.checked = false; lastSelect.className += " w3-hide"
    }

    var totalVal = old_res.getElementsByClassName("kj-result-type")(0).asInstanceOf[html.Select].getAttribute("data-totalCheck")
    if (totalVal == null) totalVal = "on"
    if (totalVal.contains("off")) {
      totalCheck.checked = false; totalSelect.className += " w3-hide"
    }

    var likelyVal = old_res.getElementsByClassName("kj-result-type")(0).asInstanceOf[html.Select].getAttribute("data-likelyCheck")
    if (likelyVal == null) likelyVal = "on"
    if (likelyVal.contains("off")) {
      likelyCheck.checked = false;  likelySelect.className += " w3-hide"
    }

    //graph type
    val typeSelect = select(cls:= "kj-result-type",
      data("graphCheck"):=graphVal,data("lastCheck"):=lastVal,data("totalCheck"):=totalVal,data("likelyCheck"):=likelyVal,
      option("Bar",value:="bar"),option("Pie",value:="pie"),
      option("horizontalBar",value:="horizontalBar"),option("polarArea",value:="polarArea"),
      option("line",value:="line")).render
    var ctype = old_res.getElementsByClassName("kj-result-type")(0).asInstanceOf[html.Select].getAttribute("data-hold-value")
    if (ctype == null) ctype = "bar"
    typeSelect.value = ctype

    // graph sum data
    val dataSelect = select(cls:= "kj-result-data",
      option("Count",value:="Count"),option("Sum",value:="Sum")).render
    if (graphVal != null && graphVal.contains("off")) {
      typeSelect.className += " w3-hide"; dataSelect.className += " w3-hide"
    }

    var dtype = "Count"
    if (old_res.getElementsByClassName("kj-result-data").length > 0)
        dtype = old_res.getElementsByClassName("kj-result-data")(0).asInstanceOf[html.Select].getAttribute("data-hold-value")
    dataSelect.value = dtype

    val new_res = div(cls:="w3-center",
      div(cls:="kj-result-check",graphCheck, label("Graph", cls:="w-fixed"),lastCheck, label("Last Selection", cls:="w-fixed")),
      div(cls:="kj-result-check",totalCheck, label("total Score", cls:="w-fixed"), likelyCheck, label("Type result", cls:="w-fixed")),
      lastSelect,totalSelect,likelySelect,
      container,typeSelect,dataSelect).render

    val typepage = main_stream.getElementsByClassName("awi-typecreate-page")
    if (typepage.length > 0 ) {
      val typevals = typepage(0).asInstanceOf[html.Div].getElementsByClassName("kj-type-val")
      val typenum = typevals.length
      var label_data: mutable.Seq[String] = mutable.Seq.empty[String]
      //find # of type label
      //add label & input box for each type
      for (i <- 0 until typenum) {
        val typeId = typevals(i).asInstanceOf[html.Input].id
        val typeName = typevals(i).asInstanceOf[html.Input].getAttribute("data-hold-value")
        label_data = label_data :+ typeName
      }
      //toolbox create

      val pageBtn = CreatePageBtn(target)
      val optPage = div(cls := "w3-cell-row kj-row", div(cls := "awi-data awi-typeres-page", new_res), div(cls := "kj-actbar ", pageBtn)).render

      //evnet processing
      graphCheck.onclick = (e:dom.Event) => {
        if (graphCheck.checked) {
          container.className = container.className.replaceAll(" w3-hide", "")
          typeSelect.className = typeSelect.className.replaceAll(" w3-hide", "")
          dataSelect.className = dataSelect.className.replaceAll(" w3-hide", "")
          typeSelect.setAttribute("data-graphCheck","on")
        }else {
          container.className += " w3-hide"
          typeSelect.className += " w3-hide"
          dataSelect.className += " w3-hide"
          typeSelect.setAttribute("data-graphCheck","off")
        }
      }
      lastCheck.onclick = (e:dom.Event) => {
        if (lastCheck.checked) {
          lastSelect.className = lastSelect.className.replaceAll(" w3-hide", "")
          typeSelect.setAttribute("data-lastCheck","on")
        }else {
          lastSelect.className += " w3-hide"
          typeSelect.setAttribute("data-lastCheck","off")
        }
      }
      totalCheck.onclick = (e:dom.Event) => {
        if (totalCheck.checked) {
          totalSelect.className = totalSelect.className.replaceAll(" w3-hide", "")
          typeSelect.setAttribute("data-totalCheck","on")
        }else {
          totalSelect.className += " w3-hide"
          typeSelect.setAttribute("data-totalCheck","off")
        }
      }
      likelyCheck.onclick = (e:dom.Event) => {
        if (likelyCheck.checked) {
          likelySelect.className = likelySelect.className.replaceAll(" w3-hide", "")
          typeSelect.setAttribute("data-likelyCheck","on")
        }else {
          likelySelect.className += " w3-hide"
          typeSelect.setAttribute("data-likelyCheck","off")
        }
      }


      PageBtnAction(pageBtn,card,target,optPage)

      optPage.onclick = ActiveToggle(target,optPage,new_res)

      //renderBBBBBB
      if (curPage != null)
        target.insertBefore(optPage,curPage.nextSibling)
      else
        target.appendChild(optPage)

      //post event after append to target
      //var label_data = js.Array("Red", "Blue", "Yellow", "Green", "Purple", "Orange")
      var datas = js.Array(7, 9, 3, 5, 2, 3)
      var sums = js.Array(17, 19, 13, 15, 12, 13)
      var chart = myChart.ResultChart(guid.toString, typeSelect.value, label_data.toJSArray, datas,"Score")

        typeSelect.onchange = (e :dom.Event) => {
        println("select change", e.target.asInstanceOf[html.Select].value)
        //chart.changeOpt(e.target.asInstanceOf[html.Select].value)
        //myChart.ChangeChart(chart,"bar")
          typeSelect.setAttribute("data-hold-value",e.target.asInstanceOf[html.Select].value)
        chart = myChart.UpdateChart(chart,guid.toString, e.target.asInstanceOf[html.Select].value, label_data.toJSArray, datas)
      }
      dataSelect.onchange = (e :dom.Event) => {
        println("select change", e.target.asInstanceOf[html.Select].value)
        //chart.changeOpt(e.target.asInstanceOf[html.Select].value)
        //myChart.ChangeChart(chart,"bar")
        dataSelect.setAttribute("data-hold-value",e.target.asInstanceOf[html.Select].value)
        chart = myChart.UpdateData(chart,guid.toString, label_data.toJSArray, sums)
      }
      PageBtnCheck(pageBtn,card,target,optPage)


    } //length > 0
  }

  def PageDelete(card:html.Div,target:html.Div,delPage:html.Div) : Function1[MouseEvent,_]  = (e:dom.Event) => {

    println("Page delete called" ,card.className,target.className,delPage.className)
    target.removeChild(delPage)
    curPage = null
  }


  // first remove all image child from awi-image-page
  // then create newImage and append it to the awi-image-page (imagediv)
  def RenderImage(imagediv:html.Div, image_class : String , preview:html.Image,imageFiles:html.Input, flex:Boolean = true) : Function1[Event,_] =
     (e:dom.Event) => {
    val length = e.srcElement.asInstanceOf[html.Input].files.length
    var column = 1
    if ( length > 1 ) column = 2
    if ( length > 4) column = 3
    if ( length > 9) column = 4
    for (i <- 0 until length ) {
      val width = 400 / column
      val file = e.srcElement.asInstanceOf[html.Input].files(i)
      println("reading:", file.name, file.`type`)
      val reader = new FileReader()
      file.`type` match {
        case filetype if filetype.startsWith("image/") =>
          reader.onload = (e: dom.Event) => {
            val the_url = e.target.asInstanceOf[FileReader].result
            if (flex) {
              val newimage = img (cls := s"$image_class", /*style := s"width:$width",*/ src := the_url.toString).render
              for (child <- imagediv.children) imagediv.removeChild(child)
              imagediv.appendChild (newimage)
            } else {
              val newimage = img (cls := s"$image_class", src := the_url.toString).render
              for (child <- imagediv.children) imagediv.removeChild(child)
              imagediv.appendChild (newimage)
            }
          } // end of onload
        case filetype if filetype.startsWith("video/") =>
          reader.onload = (e: dom.Event) => {
            val the_url = e.target.asInstanceOf[FileReader].result
            val newVideo = video (cls := s"$image_class awi-media awi-video",
                            source(src := the_url.toString, `type` := filetype)).render
            newVideo.setAttribute("controls","controls")
            newVideo.onclick = (e:dom.Event) => {
              if (newVideo.paused) newVideo.play()
              else newVideo.pause()
            }
            for (child <- imagediv.children) imagediv.removeChild(child)
            imagediv.appendChild (newVideo)
          } // end of onload
        case filetype if filetype.startsWith("audio/") =>
          reader.onload = (e: dom.Event) => {
            val the_url = e.target.asInstanceOf[FileReader].result
            val newAudio = audio (cls := s"$image_class awi-media awi-audio",
              source(src := the_url.toString, `type` := filetype)).render
            newAudio.setAttribute("controls","controls")
            newAudio.onclick = (e:dom.Event) => {
              if (newAudio.paused) newAudio.play()
              else newAudio.pause()
            }
            for (child <- imagediv.children) imagediv.removeChild(child)
            imagediv.appendChild (newAudio)
          } // end of onload
      }
      if (file != null) reader.readAsDataURL(file)
    }

  }

  // first remove all image child from imagediv(awi-image-page)
  // then create newImage form yhe clipboard and append it to the awi-image-page (imagediv)
  def PasteImage(imagediv:html.Div, image_class : String, image_url:String) : Function1[Event,_]  = (e:dom.Event) => {
    for (child <- imagediv.children) imagediv.removeChild(child)
    val newimage = img(cls := s"$image_class", style := s"width:$width", src := image_url).render
    imagediv.appendChild(newimage)
  }

  //yt_url : https://youtu.be/mwHK8ju_k1o
  //iframe : https://www.youtube.com/embed/mwHK8ju_k1o"
  def YoutubeImage(imagediv:html.Div, image_class : String, yt_url:String) : Function1[Event,_]  = (e:dom.Event) => {
    for (child <- imagediv.children) imagediv.removeChild(child)
    val iframe_src = "https://www.youtube.com/embed/" + yt_url.substring(17,yt_url.length)
    println("substring",yt_url,iframe_src)
    val ytimage = iframe(id:= yt_url, cls := s"$image_class", style := s"width:400px;height:225px", src := iframe_src).render
    ytimage.setAttribute("frameborder","0")
    ytimage.setAttribute("allowFullScreen","")
    //ytimage.appendChild(div(cls:="kj-yturl-header",s"$iframe_src").render)
    imagediv.appendChild(ytimage)
  }



  def ActiveToggle(target:html.Div,newPage:html.Div, ref : html.Div,focused:String=null) : Function1[MouseEvent,_]  = (e:dom.Event) => {

    e.stopPropagation();

    //set current page
    /*----------------------------------------*/
    //e.preventDefault() - nothing effect
    /*----------------------------------------*/
    //println("***********ActiveToggle==========>",newPage)
    curPage = newPage
    // calc new position of selected page
    var rect = ref.getBoundingClientRect()
    var win_width = dom.window.innerWidth
    var win_height = dom.window.innerHeight
    println("clinet rect", rect.left,rect.top,rect.right,rect.bottom)
    println("focused =>", focused)


    // page right menu bar position recovered ---------------------------------------------------------------
    /*
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
    */


    // text top toolbar position  ----------------------------------------------------------------------------------

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


    //hide text actbar except youtube ---------------------------------------------------------------------
    for (row <- dom.window.document.getElementsByClassName("kj-row")) {
      for (action <- row.asInstanceOf[html.Div].getElementsByClassName("kj-actbar")) {
        //println("w3-hide")

        val rowdata = row.asInstanceOf[html.Div].getElementsByClassName("awi-media")
        val isMedia = (rowdata.length > 0 && rowdata(0).asInstanceOf[html.Div].className.contains("awi-ytvideo"))
        if ( !isMedia && action.asInstanceOf[html.Div].className.indexOf("w3-hide") == -1)
          action.asInstanceOf[html.Div].className += " w3-hide"
      }
    }


    //show selected actbar except image
    println("show page focused")
    for (action <- newPage.getElementsByClassName("kj-actbar")){
      println("w3-show",action.asInstanceOf[html.Div].className)
      /*  if (action.asInstanceOf[html.Div].getElementsByClassName("kj-imagebar").length == 0 ) */
      //show actbar
      action.asInstanceOf[html.Div].className = action.asInstanceOf[html.Div].className.replaceAll("w3-hide", "")

      val editor = newPage.getElementsByClassName("kj-pagebar")
      val tagname = e.srcElement.tagName //foucused element
println("check page bar",editor.length,tagname)
      if (editor.length > 0) {
        if (tagname.contains("INPUT"))  // awi-flip-page & focus on INPUT
          editor(0).asInstanceOf[html.Div].className += " w3-hide"
        else
          editor(0).asInstanceOf[html.Div].className = editor(0).asInstanceOf[html.Div].className.replaceAll("w3-hide", "")
      }
    }

    //toggle selected border
    //  .awi-border

    //hide all kj-row border change  -------------------------------------------------------
    for (action <- dom.window.document.getElementsByClassName("kj-row")) {
      if (action.asInstanceOf[html.Div].className != null)
        action.asInstanceOf[html.Div].className = action.asInstanceOf[html.Div].className.replaceAll("awi-border","")
    }


    //show / activate newpage's border
    println("newPage",newPage.className)
    newPage.className += " awi-border"
    if (!newPage.className.contains("dirty")) newPage.className += " dirty"


    //Check right properties
    val rightTool = dom.window.document.getElementById("kj-right").asInstanceOf[html.Div]
    val rightObject = dom.window.document.getElementsByClassName("kj-right-object")(0).asInstanceOf[html.Div]
    val rightTabs = dom.window.document.getElementsByClassName("kj-right-tabs")(0).asInstanceOf[html.Div]
    val rightContents = dom.window.document.getElementsByClassName("kj-right-content")(0).asInstanceOf[html.Div]

    rightTool.className = rightTool.className.replaceAll(" w3-hide","")
    rightOpts.initOpts(rightObject,rightTabs,rightContents,newPage,focused)


    // ---------------------------------------------------------------------

    // check if emptypage then show rtoolbar
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
      if (!dom.window.document.getElementById("rtoolbar").asInstanceOf[html.Div].className.contains("w3-hide"))
        dom.window.document.getElementById("rtoolbar").asInstanceOf[html.Div].className += " w3-hide"
    }

    /*-------------------*/
    //e.stopPropagation()
    /*-------------------*/
  }


  def ActiveHide(target:html.Div) : Function1[Event,_]  = (e:dom.Event) => {
    println("********* Active hide")
    for (action <- dom.window.document.getElementsByClassName("kj-actbar")) {
      if (action.asInstanceOf[html.Div].className.indexOf("w3-hide") == -1)
        action.asInstanceOf[html.Div].className += " w3-hide"
    }
  }


  @JSExport
  def contextMenuAction  = (page_row : html.Div, action : String , e:dom.MouseEvent) => {
    println("context menu clicked")
    println (page_row.className,action)

    val card = Util.findAncestor(page_row,"kj-card-2").asInstanceOf[html.Div]
    val target = Util.findAncestor(page_row,"kj-container").asInstanceOf[html.Div]
    println(action)
    action match {
      case "newCard" =>
        NewQuizEvalAfter(main_saved,card)(e)
      case "newTestCard" =>
        NewTypeTestAfter(main_saved,card)(e)
      case "pageAdd" =>
        PageAdd(card,target)(e)
      case "imageAdd" =>
        ImageAdd(card,target)(e)
      case "flipAdd" =>
        FlipAdd(card,target)(e)
      case "optAdd" =>
        OptionAdd(card,target)(e)
      case "typeTestAdd" =>
        TypeTestAdd(main_saved,card,target)(e)
      case "typeTestRes" =>
        TypeResAdd(main_saved,card,target)(e)
      case "typeTestBranch" =>
        TypeRuleAdd(card,target)(e)
      case "Delete" =>
        println("page delete call")
        PageDelete(card,target,page_row)(e)
    }
    contextMenu.toggleMenuOff()
  }

  /* -------------------------------------------------------------------------------*/
  @JSExport
  def imagePasteAction  = (image_url : String , e:dom.MouseEvent) => {
    println("*************image paste clicked")
    //println (page_row.className,action)

    for (action <- main_saved.getElementsByClassName("awi-border")) {
      for(image <- action.asInstanceOf[html.Div].getElementsByClassName("awi-image-page")) {
        PasteImage(image.asInstanceOf[html.Div], "awi-image", image_url)(e)
      }
    }
    //if (!js.isUndefined(imagediv))
     // PasteImage(imagediv(0).asInstanceOf[html.Div],"awi-image", image_url)(e)
  }
  /*------------------------------------------------------------------------------------------------*/


  def CreateQuill(cont : js.Any, toolbarid:String, isTitle:Boolean = false) : Quill = {

    //val optstr = """{theme: snow, modules: {toolbar: toolbar}}"""
    /* -------------- not used anymore ------------------------------------
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
    | [{ "size": ["small", "false", "large", "huge"] }],
    | [{ "color": [] }, { "background": [] }],
    | [{ "align": [] }],
    | ["clean"] 
    | ]} ,
    | "theme" : "snow"
    | }""".stripMargin

    */

    val optstr3 = """ {"modules" :  """ +
      """{ "toolbar" :"""" +
      toolbarid + """"}}""".stripMargin

    //println(optstr3)
    val options = js.JSON.parse(optstr3)

    val editor = new Quill(cont,options)
    if (isTitle) {
      editor.setText("Title Here")
      //editor.formatText(0,10,"size","huge")
      //editor.formatLine(1,2,"align","center")
    }
    return editor
  }


  def CreateYtModal (ytbar : html.Div, mediadiv : html.Div) = {
    ytbar.onclick = (e:dom.Event) => {
      var modalDialog = dom.window.document.getElementById("ytModal")
      if (modalDialog == null) {
        modalDialog = div(cls := "w3-modal", id := "ytModal",
          div(cls := "w3-modal-content",
            div(cls := "w3-container w3-center kj-modal-header","YouTube",
              span(cls := "w3-button w3-border w3-right", "x",
                onclick := "document.getElementById('ytModal').style.display='none'")),
            div(cls := "w3-container w3-center",
              input(id := "ytUrl", cls := "w3-margin-top w3-margin-bottom w3-col m12",
                placeholder:="https://youtu.be/XYWZABCDEF")),
            div(cls := "w3-container w3-center",
              button(id := "ytOKBtn", cls := "w3-margin-top w3-margin-bottom","OK"),
              button(cls := "w3-margin-top w3-margin-bottom","Cancel",
                onclick := "document.getElementById('ytModal').style.display='none'"))
          )).render
        dom.window.document.body.appendChild(modalDialog)
      } else {
        val ytUrl = dom.window.document.getElementById("ytUrl").asInstanceOf[html.Input]
        ytUrl.value = ""
      }
      modalDialog.asInstanceOf[html.Div].style.display = "block"
      val ytUrl = dom.window.document.getElementById("ytUrl").asInstanceOf[html.Input]
      val ytOKBtn = dom.window.document.getElementById("ytOKBtn").asInstanceOf[html.Button]
      ytOKBtn.onclick = (e:dom.Event) => {
        if (ytUrl.value.indexOf("https://youtu.be/") >= 0 ) {
          YoutubeImage(mediadiv, "awi-image awi-media awi-ytvideo", ytUrl.value)(e)
          modalDialog.asInstanceOf[html.Div].style.display = "none"
        } else {
          dom.window.alert("Valid Youtube URL reuired!")
        }
      }
    }
  }

  def CreatePreviewModal (pages:String,cardtype:String, imgsrc:String, sumText:String) = {

      val idstr = "pid0001"
      var modalDialog = dom.window.document.getElementById("previewModal")
      if (modalDialog == null) {
        modalDialog = div(cls := "w3-modal kj-modal", id := "previewModal",
          div(cls := "kj-modal-content",
            div(cls := "w3-container w3-center kj-modal-header","Preview",
              span(cls := "kj-preview-closex w3-button w3-border w3-right", "x")),
            div(cls := "kj-modal-main"),
            div(cls := "w3-container w3-center",
              button(id := "previewOKBtn", cls := "w3-margin-top w3-margin-bottom","OK"))
          )).render
        dom.window.document.body.appendChild(modalDialog)
      }

      val card_stream = div(id := s"kj-multi-card$idstr").render
      var summary = div(id := s"multi_sum$idstr", cls := "summary ",
        div(cls:= "awi-image-page kj-row", img(cls := "awi-image", src := imgsrc, alt := "Avatar")),
        div(cls := "awi-sum-text w3-padding w3-center",
          img(cls:="w3-circle w3-left ",style:="height:25px;width:25px",src:=user_pic), sumText)).render
      var cardnews = div(id := s"multi_news$idstr", cls := "kj-preview-modal kj-card-2 w3-card-2 w3-white w3-round ",
        summary, card_stream).render

      val main_stream = modalDialog.getElementsByClassName("kj-modal-main")(0).asInstanceOf[html.Div]
      //while(main_stream.firstChild!=null) main_stream.removeChild(main_stream.firstChild)

      // insert modal content to the first of kj-modal-main
      main_stream.appendChild(cardnews)

    // check storynews then play it asap
      if (cardtype.equals("default")) {
        summary.className += " w3-hide"
        cnpost_php.PostCardLocal(main_stream, pages,idstr)
      } else {
        // check storynews otherwise play it later
        val toolbar = div(cls := "kj-playbar w3-hover-opacity",
          label(i(cls := "fa fa-play-circle w3-white"))).render
        summary.getElementsByClassName("awi-image-page")(0).appendChild(toolbar)
        summary.onclick = (e: dom.Event) => {
          summary.className += " w3-hide"
          cnpost_php.PostCardLocal(main_stream, pages,idstr)
        }
      }

      modalDialog.asInstanceOf[html.Div].style.display = "block"
      val previewOKBtn = dom.window.document.getElementById("previewOKBtn").asInstanceOf[html.Button]
      previewOKBtn.onclick = (e:dom.Event) => {
        while(main_stream.firstChild!=null) main_stream.removeChild(main_stream.firstChild)
        modalDialog.asInstanceOf[html.Div].style.display = "none"
      }
     val previewCloseBtn = dom.window.document.getElementsByClassName("kj-preview-closex")(0).asInstanceOf[html.Button]
      previewCloseBtn.onclick = (e:dom.Event) => {
        while(main_stream.firstChild!=null) main_stream.removeChild(main_stream.firstChild)
        modalDialog.asInstanceOf[html.Div].style.display = "none"
      }

  }

  def changeSizeTab(Object : html.Div,current : html.Element): Unit =
  {
    val tabs = Object.getElementsByClassName("tabsize")
    println("change tab size",tabs.length)
    for (i <- 0 until tabs.length)
      tabs(i).asInstanceOf[html.Element].className = tabs(i).asInstanceOf[html.Element].className.replaceAll("w3-teal", "w3-grey")
    current.className = current.className.replaceAll("w3-grey","w3-teal")

  }

  //------ change chart width according to the page size dynamically
  def updateChartSize(w:Int) = {
    //post event after append to target
    //var label_data = js.Array("Red", "Blue", "Yellow", "Green", "Purple", "Orange")
    val chartAll = dom.window.document.getElementsByClassName("kj-chart-result")
    for (i <- 0 until chartAll.length) {
      val oneCanvas = chartAll(i).asInstanceOf[html.Div].getElementsByTagName("canvas")
      if (oneCanvas.length>0) {
        oneCanvas(0).asInstanceOf[html.Canvas].style.width = (w-62) + "px"
      }
    }

  }


}
