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
object cnpost_php extends {
  var objMap = new ArrayBuffer[mutable.HashMap[String, String]]()
  var lineMap = new ArrayBuffer[mutable.HashMap[String, String]]()
  var myUrl: String = ""
  var winRef: Window = null
  var viewmode = "post"
  var subview = "default"
  var cardOffset = 0
  var moreSize = 6

  var pageNum = 0

  @JSExport
  def main(_view : String, _viewsub : String, target: html.Div , main_stream : html.Div): Unit = {



    var paraMap = new mutable.HashMap[String, String]
    var objNm, ibjId: String = ""
    var cardNum = ""
    viewmode = _view
    subview = _viewsub

    println(dom.window.location.search)
    println("location", dom.window.location)
    println("pathname", dom.window.location.pathname)
    println("viewmode", viewmode, subview)
    val pathname = dom.window.location.pathname


    //check parameter
    println(dom.window.location.search)
    val query = dom.window.location.search.substring(1).split("&")
    //loc.sea   .search.substring(1).split("&")
    for (q <- query) {
      var param = q.split("=")
      paraMap += (param(0)->param(1))
      println(param(0)+" : " +param(1))
    }

    if (paraMap.contains("subview") && paraMap("subview")=="template") {
      subview = "template"
    }




    val mainidstr = pathname.substring(pathname.lastIndexOf('/') + 1).replace("post", "")


    //initial display
    DispatchPreview(target,main_stream, mainidstr,0,moreSize)
    cardOffset += moreSize

    // more display when mpre clicked
    val moreCard = dom.window.document.getElementById("MoreCard").asInstanceOf[html.Button]
    moreCard.onclick = (e:dom.Event) => {
      DispatchPreview(target,main_stream, mainidstr,cardOffset,moreSize)
      cardOffset += moreSize
    }

    //end scroll
    dom.window.onscroll = (e:dom.Event) => {
      //println(dom.window.innerHeight+dom.window.pageYOffset , dom.window.document.body.offsetHeight)
      if ((dom.window.innerHeight + dom.window.pageYOffset + 10) >= dom.window.document.body.offsetHeight) {
        DispatchPreview(target,main_stream, mainidstr,cardOffset,moreSize)
        cardOffset += moreSize
      }
    }

    //
  } //main


  def DispatchPreview(target: html.Div,main_stream : html.Div, mainidstr: String,offset:Int,size:Int): Unit = {
    //initial display
    if (viewmode.equals("myhome")) { //myhome
      if (subview.equals("default")) //post & default
        PostPreviewCard(target, main_stream, mainidstr, "myhome", offset, moreSize)
      else if (subview.equals("template"))
        PostPreviewCard(target, main_stream, mainidstr, "template", offset, moreSize)
    } else if (viewmode.equals("post")) {
      if (subview.equals("default")) //post & default
        PostPreviewCard(target, main_stream, mainidstr, "post", offset, moreSize)
      else if (subview.equals("hotcard") && offset == 0) //post & hotcard only once
        PostPreviewHotCard(target, main_stream, mainidstr, offset, moreSize)
    }
  }


  def PostPreviewCard(target: html.Div,main_stream : html.Div, mainidstr: String,view:String,offset:Int,size:Int) {

    var mainnews: html.Div = null

    val query = """{"moved to ajax_search.php"}"""

    //val urlBase = s"${GlobalVars.dataBase}/_search"
    val urlBase = s"php/ajax_search.php?view=$view&offset=$offset&size=$size"
    //dom.window.alert("called preview")
    ///////////////////////////////////////////////
    //Ajax.get(urlBase).onSuccess { case xhr => ...}

    //Ajax.get(urlBase).onSuccess { case xhr =>
    dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "block"
    val request = Ajax.post(urlBase, query)
      request.onComplete {
        case Success(xhr) =>
          //println(xhr)
          val res = js.JSON.parse(xhr.responseText)
          val total = res.hits.hits.length.asInstanceOf[Int]
          val hits = res.hits.hits.asInstanceOf[js.Array[js.Dynamic]]

          if (total < size) {
            val moreCard = dom.window.document.getElementById("MoreCard").asInstanceOf[html.Button]
            moreCard.className += " w3-hide"
            dom.window.onscroll = null
          }

          for (index <- 0 until total) {

            //println(s"****************$index****************")
            //println(index, hits(index)._id)
            val name = hits(index)._source.name
            val cardtype = hits(index)._source.`type`
            val _user_id = hits(index)._source.user_id
            val _user_name = hits(index)._source.user_name
            val _user_pic = hits(index)._source.user_pic
            val _updated = hits(index)._source.updated

println(hits(index)._id,name,cardtype,_user_id,_user_name,_updated)
            var user_id : String ="guest"
            var user_name : String ="guest"
            var user_pic : String ="./images/avatar3.png"
            var updated : String ="20170911"
            if (!js.isUndefined(_user_id)) {
              user_id = _user_id.asInstanceOf[String]
            }
            if (!js.isUndefined(_user_name)) {
              user_name = _user_name.asInstanceOf[String]
            }
            if (!js.isUndefined(_user_pic)) {
              user_pic = _user_pic.asInstanceOf[String]
            }
            if (!js.isUndefined(_updated)) {
              updated = _updated.asInstanceOf[String]
            }
            val sumText = URIUtils.decodeURIComponent(dom.window.atob(hits(index)._source.sumText.toString))
            val imgsrc = URIUtils.decodeURIComponent(dom.window.atob(hits(index)._source.imgsrc.toString))


            val idstr = s"${hits(index)._id}"
            val iddiv = div(id := idstr, cls := "w3-cell-row").render

            //println("preview**********", idstr, cardtype)

            if (name.equals("My First Card")) { // || (name.equals("QuizNews") && cardtype.equals("default"))) {
              //old version
              PostCard(target, main_stream, hits(index)._id.toString)
            } else if (name.equals("QuizNews")) {
              //storynews, quiznews, typeaction or typetest
              MultiCardPreview(target, main_stream, idstr, mainidstr, cardtype.toString, sumText, imgsrc.toString,
              user_id,updated,user_name,user_pic)

            } else {
              println(name,s"error no match card found($idstr)!!!!!!!!!!!!!")
            }

          } // end of data
          dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"
        //appen "more" button to end of main_stream


        case Failure(e) =>
          dom.window.alert("Try later")
          println(e.toString)
          dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"
    }
  }

  def PostPreviewHotCard(target: html.Div,main_stream : html.Div, mainidstr: String,offset:Int,size:Int) {

    var mainnews: html.Div = null

    //dom.window.alert("called hot preview")

    //val urlBase = s"${GlobalVars.dataBase}/_search"
    val urlBase = s"php/piwik_search.php?subview=$subview&offset=$offset&size=$size"
    //dom.window.alert("called Tab:"+objNm+":"+objId+":"+tab)
    ///////////////////////////////////////////////
    //Ajax.get(urlBase).onSuccess { case xhr => ...}

    //Ajax.get(urlBase).onSuccess { case xhr =>
    dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "block"
    val request = Ajax.get(urlBase)
    request.onComplete {
      case Success(xhr) =>
        println("piwik",xhr,xhr.responseText)
        if (xhr.responseText.length<=0) {
          dom.window.alert("Try later")
          //println(e.toString)
          dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"
          return
        }
        val res = js.JSON.parse(xhr.responseText)
        val total = res.docs.length.asInstanceOf[Int]
        val hits = res.docs.asInstanceOf[js.Array[js.Dynamic]]

        if (total < size) {
          val moreCard = dom.window.document.getElementById("MoreCard").asInstanceOf[html.Button]
          moreCard.className += " w3-hide"
          dom.window.onscroll = null
        }

        for (index <- 0 until total) {

          val found = hits(index).found.asInstanceOf[Boolean]

          if (found) {
            val name = hits(index)._source.name
            val cardtype = hits(index)._source.`type`
            val _user_id = hits(index)._source.user_id
            val _user_name = hits(index)._source.user_name
            val _user_pic = hits(index)._source.user_pic
            val _updated = hits(index)._source.updated

            println(name, hits(index)._id, _user_id, _user_name, _user_pic, _updated)
            var user_id: String = "guest"
            var user_name: String = "guest"
            var user_pic: String = "./images/avatar3.png"
            var updated: String = "20170911"
            if (!js.isUndefined(_user_id)) {
              user_id = _user_id.asInstanceOf[String]
            }
            if (!js.isUndefined(_user_name)) {
              user_name = _user_name.asInstanceOf[String]
            }
            if (!js.isUndefined(_user_pic)) {
              user_pic = _user_pic.asInstanceOf[String]
            }
            if (!js.isUndefined(_updated)) {
              updated = _updated.asInstanceOf[String]
            }
            val sumText = URIUtils.decodeURIComponent(dom.window.atob(hits(index)._source.sumText.toString))
            val imgsrc = URIUtils.decodeURIComponent(dom.window.atob(hits(index)._source.imgsrc.toString))


            val idstr = s"${hits(index)._id}"
            val iddiv = div(id := idstr, cls := "w3-cell-row").render


            if (name.equals("My First Card")) { // || (name.equals("QuizNews") && cardtype.equals("default"))) {
              //storynews
              PostCard(target, main_stream, hits(index)._id.toString)
            } else if (name.equals("QuizNews")) {
              MultiCardPreview(target, main_stream, idstr, mainidstr, cardtype.toString , sumText, imgsrc.toString,
                user_id, updated, user_name, user_pic)
            } else {
              println(name, s"error no match card found($idstr)!!!!!!!!!!!!!")
            }
          } // if found

        } //for
        dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"

      case Failure(e) =>
        dom.window.alert("Try later")
        println(e.toString)
        dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"
    }
  }


  def PostCard(target: html.Div, main_stream : html.Div,cardNum: String) {
    import upickle.default._

    var mainnews: html.Div = null

    //val urlBase = s"${GlobalVars.dataBase}/$cardNum"
    val urlBase = s"php/ajax_getcard.php?a=$cardNum"

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

        dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"
        if (name.equals("QuizNews") && !js.isUndefined(res._source.resume)) {
          val json_data = dom.window.atob(res._source.resume.asInstanceOf[String])

          //find existing card and remove its children
          val card_stream = dom.window.document.getElementById(s"kj-multi-card$idstr").asInstanceOf[html.Div]
          while(card_stream.firstChild!=null) card_stream.removeChild(card_stream.firstChild)

          if (js.isUndefined(cardtype) || cardtype.equals("default")) {
            val qcard = new QuizDiv(card_stream, json_data, idstr,"default")
            MultiCardAction(main_stream, idstr, cardNum, card_stream)
          }
          else if (cardtype.equals("quiznews") || cardtype.equals("typeaction")) {
            val qcard = new QuizDiv(card_stream, json_data, idstr,"action")
            MultiCardAction(main_stream, idstr, cardNum, card_stream)
          } else if (cardtype.equals("typetest")) {
            new TypeTest(card_stream, json_data, idstr)
            MultiCardAction(main_stream, idstr, cardNum, card_stream)
          } else {
            println("error no match cardtype!!!!!!!!!!!!!",name)
          }
          //spinner.className += " w3-hide"
        } else {
          println("error no match cardtype 2 !!!!!!!!!!!!!",name)
        }
        //dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"
        //main_stream.className = main_stream.className.replaceAll(" spinner","")

      case Failure(e) =>
        dom.window.alert("Try later")
        println(e.toString())
        dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"
        //main_stream.className = main_stream.className.replaceAll(" spinner","")
    }
  }

  def PostCardLocal(main_stream : html.Div, pages:String, idstr:String) {
    //import upickle.default._

    var mainnews: html.Div = null
    val cardNum = "card0001"


    dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "block"
        //println(xhr.responseText)
    val res = js.JSON.parse(pages)
    val name = res.name
    val cardtype = res.`type`
    //val idstr = "id0001" + res.updated.toString
    //val sumText = URIUtils.decodeURIComponent(dom.window.atob(res.sumText.toString))
    //val imgsrc = URIUtils.decodeURIComponent(dom.window.atob(res.imgsrc.toString))

    if (name.equals("QuizNews") && !js.isUndefined(res.resume)) {
      val json_data = dom.window.atob(res.resume.asInstanceOf[String])

      //find existing card and remove its children
      val card_stream = dom.window.document.getElementById(s"kj-multi-card$idstr").asInstanceOf[html.Div]
      while(card_stream.firstChild!=null) card_stream.removeChild(card_stream.firstChild)

      if (js.isUndefined(cardtype) || cardtype.equals("default")) {
        val qcard = new QuizDiv(card_stream, json_data, idstr,"default",false)
        MultiCardAction(main_stream, idstr, cardNum, card_stream)
      }
      else if (cardtype.equals("quiznews") || cardtype.equals("typeaction")) {
        val qcard = new QuizDiv(card_stream, json_data, idstr,"action",false)
        MultiCardAction(main_stream, idstr, cardNum, card_stream)
      } else if (cardtype.equals("typetest")) {
        new TypeTest(card_stream, json_data, idstr,false)
        MultiCardAction(main_stream, idstr, cardNum, card_stream)
      } else {
        println("error no match cardtype!!!!!!!!!!!!!",name)
      }
    } else {
      println("error no match cardtype 2 !!!!!!!!!!!!!",name)
    }
    dom.window.document.getElementsByClassName("loader")(0).asInstanceOf[html.Div].style.display = "none"
  }


  def MultiCardPreview(target:html.Div, main_stream:html.Div,idstr:String, mainidstr:String, cardtype:String,
                       sumText:String, imgsrc:String,user_id:String, updated:String,user_name:String,user_pic:String): Unit =
  {
    //println("33")
    val button1 = button(cls := "w3-button w3-small w3-theme-d1 w3-margin-bottom w3-border", i(cls := "fa fa-thumbs-up"), "Like").render
    val button2 = button(cls := "w3-button w3-small w3-theme-d1 w3-margin-bottom w3-border", i(cls := "fa fa-comment"), " Comment").render

    //println("location", dom.window.location)
    //println("pathname", dom.window.location.pathname)

    val baseURL = "%s".format(dom.window.location.toString.split('?')(0)).replace(dom.window.location.pathname, "/post")
    //println("baseURL", baseURL)

    val postURL = s"$baseURL$idstr"

    //println("postURL", postURL)

    //View Count only for post
    val viewStat = button(cls:="w3-button w3-white ", span(cls:=s"views-$idstr","? "), " views").render
    val dateNow = new js.Date()

    var saved = updated.toString
    if (updated.length != 10) saved = "1505441175"
println("saved",saved)
    val saveDate = new js.Date(saved.toDouble * 1000)

    var timeDiff = Math.abs(dateNow.getTime() - saveDate.getTime());
    var diffMin = Math.ceil(timeDiff / (1000 * 60 ));
    var diffHour = Math.floor(diffMin / 60 );
    var diffDay = Math.floor(diffHour / 24);
    var diffMonth = Math.floor(diffDay / 30);
    var diffYear =  Math.floor(diffMonth / 12);

    var duration = ""
    if (diffYear != 0)
      duration = s"$diffYear year(s) ago,"
    else if (diffMonth != 0)
      duration = s"$diffMonth month(s) ago,"
    else if (diffDay != 0)
      duration = s"$diffDay day(s) ago,"
    else if (diffHour != 0)
      duration = s"$diffHour hour(s) ago,"
    else if (diffMin != 0)
      duration = s"$diffMin Minute(s) ago,"


    var statView = div(cls:="w3-row w3-tiny", duration, viewStat).render

    //Play,Submit,detail Count
    val playStat = button(cls:="w3-button w3-white w3-tiny", span(cls:=s"plays-$idstr","? ")," plays").render
    val submitStat = button(cls:="w3-button w3-white w3-tiny", span(cls:=s"submits-$idstr","? ")," submits").render
    val detailStat = button(cls:="w3-button w3-white w3-tiny", "Details").render
    if (viewmode.equals("myhome")) {

      statView = div(cls:="w3-row w3-tiny",duration,viewStat, playStat, submitStat, detailStat).render
    }

    //update stat value
    val piwikBase = s"php/piwik_playbyid.php?id=$idstr"

    //dom.window.alert("called Tab:"+objNm+":"+objId+":"+tab)
    ///////////////////////////////////////////////
    val request = Ajax.get(piwikBase)
    request.onComplete {
      case Success(xhr) =>
        println("piwik",xhr,xhr.responseText)
        if (xhr.responseText.length>0) {
          val res = js.JSON.parse(xhr.responseText)
          val hits = res.asInstanceOf[js.Array[js.Dynamic]]

          for (index <- 0 until hits.length) {
            //println(hits(index).label,hits(index).nb_events)
            if (hits(index).label.equals("Load")) viewStat.getElementsByTagName("span")(0).asInstanceOf[html.Span].textContent = hits(index).nb_events.toString
            if (hits(index).label.equals("Play")) playStat.getElementsByTagName("span")(0).asInstanceOf[html.Span].textContent = hits(index).nb_events.toString
            if (hits(index).label.equals("Submit")) submitStat.getElementsByTagName("span")(0).asInstanceOf[html.Span].textContent = hits(index).nb_events.toString

          }
        }
        statView.className =  statView.className.replaceAll("kj-loader","")

      case Failure(e) =>
        //dom.window.alert("Try later")
        println(e.toString)
    }

        //link share ===============================================
    val linkButton = div(cls := "w3-button w3-small w3-text-theme w3-margin-top w3-margin-bottom w3-border", "Link").render
    linkButton.onclick = (e: dom.Event) => {
      dom.window.document.getElementById("ShareIt").asInstanceOf[html.Div].style.display = "block"
      dom.window.document.getElementById("ShareURL").asInstanceOf[html.Input].value = s"$postURL"
      piwik.piwik_event_push("Link","Url",idstr)

    }
    val editBtn = button(cls := "w3-button w3-small w3-text-theme w3-margin-top w3-margin-bottom w3-border", i(cls := "fa fa-edit"), " Edit").render
    if (viewmode.equals("post") ) editBtn.className += " w3-hide"
    editBtn.onclick =  (e:dom.Event) => {dom.window.location.href = s"quiznews.html?a=$idstr"}

    val cloneBtn = button(cls := "w3-button w3-small w3-text-theme w3-margin-top w3-margin-bottom w3-border", i(cls := "fa fa-edit"), " Make a Copy").render
    if (viewmode.equals("post")) cloneBtn.className += " w3-hide"
    cloneBtn.onclick =  (e:dom.Event) => {dom.window.location.href = s"quiznews.html?a=$idstr&mode=copy"}

    //if (viewmode.equals("myhome")) detailStat.className += " w3-hide"
    val refpage = s"http://192.168.1.222/index.php?module=CoreHome&action=index&idSite=1&period=day&date=yesterday&segment=eventName%3D%3D$idstr" +
      s"&updated=1#?idSite=1&period=month&date=today&category=Dashboard_Dashboard&subcategory=1&segment=eventName%3D%3D$idstr"
    detailStat.onclick =  (e:dom.Event) => {dom.window.location.href = refpage}

    //link share ===============================================
    val playBtn = div(id := s"kj-play-btn-$idstr", cls := "w3-hide kj-option-btn w3-hide w3-button w3-small w3-round w3-block w3-margin-top w3-padding", "Let's Play").render
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
      piwik.piwik_event_push("Link","Facebook",idstr)

      val options = js.JSON.parse(optstr)
      //println(optstr)

      FB.ui(options,(res:js.Any)=>{

      })
    }
    //facebook share end ===============================================
    //check template mode
    if (subview.equals("template")) {
      linkButton.className += " w3-hide"
      editBtn.className += " w3-hide"
      facebutton.className += " w3-hide"
    }
    val card_stream = div(id := s"kj-multi-card$idstr").render
    var summary =
      div(id := s"multi_sum$idstr", cls := "summary ",
        div(cls:= "awi-image-page w3-row-padding kj-row",
          img(cls := "awi-image-thumb w3-half", src := imgsrc, alt := "Avatar"),
          div(cls := "awi-sum-text w3-padding w3-half",
            img(cls:="w3-circle",style:="height:25px;width:25px",src:=user_pic), p(sumText), playBtn))
      ).render
    var cardnews = div(id := s"multi_news$idstr", cls := "kj-card-2 w3-card-2 w3-white w3-round ",
      summary, card_stream, div(cls:="w3-margin w3-center", statView,facebutton, linkButton,editBtn,cloneBtn)).render
    if (mainidstr.equals(idstr)) {
      main_stream.insertBefore(cardnews, main_stream.firstChild)
    } else {
      main_stream.insertBefore(cardnews, main_stream.getElementsByClassName("kj-card-more")(0))
      //main_stream.appendChild(cardnews)
    }
    //if (mainidstr.equals(idstr)) mainnews = cardnews

    //println("46")
    //cardnews.onclick = FocusCard(main_stream, s"multi_news$idstr", s"multi_sum$idstr")
    playBtn.onclick = (e: dom.Event) => {
      PostCard(target,main_stream,idstr)
      //summary.className += " w3-hide"
      piwik.piwik_event_push("Play","Start",idstr)
    }

    // check storynews then play it asap
    /*if (cardtype.equals("default")  && mainidstr.equals(idstr)) {
      PostCard(target, main_stream, idstr)
      summary.className += " spinner"
      piwik.piwik_event_push("Play", "Start", idstr)
    } else { */
    if (true) {
      // check storynews otherwise play it later
      val toolbar = div(cls := "kj-playbar w3-hover-opacity",
        label(i(cls := "fa fa-play-circle w3-white"))).render
      summary.getElementsByClassName("awi-image-page")(0).appendChild(toolbar)
      summary.onclick = (e: dom.Event) => {
        PostCard(target, main_stream, idstr)
        //summary.className += " w3-hide"
        piwik.piwik_event_push("Play", "Start", idstr)
      }
      PostProcess(main_stream)
    }
    //println("47")
  }

  def MultiCardAction(main_stream:html.Div,idstr:String, mainidstr:String,card_stream:html.Div): Unit =
  {
    //println("33")
    val button1 = button(cls := "w3-button w3-small w3-theme-d1 w3-margin-bottom w3-border", i(cls := "fa fa-thumbs-up"), "Like").render
    val button2 = button(cls := "w3-button w3-small w3-theme-d1 w3-margin-bottom w3-border", i(cls := "fa fa-comment"), " Comment").render
    //val button4 = button(id:="ShareIt", cls:="w3-button w3-small w3-theme w3-border w3-right", i(cls:="fa fa-facebook")).render

println("before",dom.window.document.getElementsByTagName("body")(0).asInstanceOf[html.Body].scrollTop, card_stream.offsetTop, 40)
    dom.window.document.getElementsByTagName("body")(0).asInstanceOf[html.Body].scrollTop = card_stream.offsetTop - 40

println("after",dom.window.document.getElementsByTagName("body")(0).asInstanceOf[html.Body].scrollTop, card_stream.offsetTop)

    //dom.window.document.getElementById(s"kj-play-btn-$idstr").asInstanceOf[html.Div].className += " w3-hide"
    val card_news = dom.window.document.getElementById(s"multi_news$idstr")
    if (card_news != null) card_news.asInstanceOf[html.Div].getElementsByClassName("summary")(0).asInstanceOf[html.Div].className += " w3-hide"
    else println("no card news",s"multi_news$idstr")

    PostProcess(main_stream)
    //println("47")

  }



  //def PageAdd(target:html.Div) : Function1[Event,_]  = (e:dom.Event) => {

  def FocusCard(target:html.Div,cardid:String,sumid:String) : Function1[Event,_]  = (e:dom.Event) => {
      val cardnews = dom.window.document.getElementById(cardid).asInstanceOf[html.Div]
      val summary = dom.window.document.getElementById(sumid).asInstanceOf[html.Div]
      //target.insertBefore(cardnews, target.firstChild)
      //println("summary style",summary.getAttribute("style"))
      val style = summary.getAttribute("style")
      //if (style == null || !style.contains("hidden"))
        //cardnews.setAttribute("style", "height:150px;overflow:hidden")
      //else
      cardnews.setAttribute("style", "")
      summary.className += " w3-hide"
  }


  def PostProcess(target: html.Div): Unit = {
    val containers = target.getElementsByClassName("awi-editor")
    for (tool <- containers) {
      tool.asInstanceOf[html.Div].className = tool.asInstanceOf[html.Div].className.replaceAll("w3-border","")
    }

    val actbars = target.getElementsByClassName("kj-actbar")
    for (tool <- actbars) {
      tool.asInstanceOf[html.Div].className += " w3-hide"
    }
    val topbars = target.getElementsByClassName("top-actbar")
    for (tool <- topbars) {
      tool.asInstanceOf[html.Div].className += " w3-hide"
    }
    val toolbars = target.getElementsByClassName("ql-toolbar")
    for (tool <- toolbars) {
      tool.asInstanceOf[html.Div].className += " w3-hide"
    }
    val inputs = target.getElementsByTagName("input")
    for (tool <- inputs) {
      tool.asInstanceOf[html.Input].setAttribute("readonly", "true")
    }
    val editors = target.getElementsByClassName("ql-editor")
    for (tool <- editors) {
      tool.asInstanceOf[html.Input].setAttribute("contenteditable", "false")
    }


    //img size
    /* no more
    val images = target.getElementsByTagName("img")
    for (tool <- images) {
      tool.asInstanceOf[html.Div].className += " awi-image"

    }
    */


  }
}