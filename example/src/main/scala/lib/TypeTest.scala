package lib

import org.scalajs.dom
import org.scalajs.dom.ext._
import org.scalajs.dom.html
import upickle.default.read

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map
import scala.scalajs.js
import scala.scalajs.js.{URIUtils, UndefOr}
import scala.util._
import scalatags.JsDom.all._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.runNow

/**
  * Created by Administrator on 2017-06-07.
  * Create Muticard from json-data
  * Show 1st multicard
  * Show next card when clicked next buttton
  *
  */
class TypeTest(main_stream: html.Div, json_data:String, idstr:String,real:Boolean=true) {

  var cur_play = 0
  var PlayResponse = new ArrayBuffer[(Int,String,String,String)]  //table of (cur_card#, response, typename, typescore)
  val playResultMap = Map.empty[String, Int]  // typetest Result Map : word (label) -> count
  val playResultSumMap = Map.empty[String, Int]  // typetest Result Map : word (label) -> Sum ( count * score)
  var playOptions = new ArrayBuffer[String]   // list of type name

  var final_sum_on = "off"
  var final_max_on = "off"
  var final_calc_sum : Int = 0
  var final_calc_max : String = ""



  var (cardLen, cards) = read [(Int,ArrayBuffer[(Int,ArrayBuffer[String],ArrayBuffer[String])])](json_data)

  //val typecard = ReadTypeCard()
  val playStart = ReadPlayCards()
  val resultStart = ReadResultCards()

  val showResultType = ReadShowResultType()

  println("now playing",playStart,resultStart)
  //for (cardnum <- 0 until playLength) {
    NewPlayCard(playStart)
  //}
  ShowPlayCard(playStart)  //initial card
  //println("6image size change")
  //img size

  def ReadPlayCards() : Int= {

    var start = 0
    for (i <- 0 until cards.length) {
      val (length,saved_area1,saved_area2) = cards(i)
      println("playingcard ",i,length)
      for (j <- 0 until length) {
        println("playingcard ",i,j,saved_area2(j))
        if (start==0 && saved_area2(j).contains("kj-typetest-start")) return (i+1)
      }
    }

    println("play start ",start)

    return start

  }

  def ReadResultCards() : Int = {
    var start = 0
    for (i <- 0 until cards.length) {
      val (length,saved_area1,saved_area2) = cards(i) //area2 is awi-data class
      for (j <- 0 until length) {
        if (start==0 && saved_area2(j).contains("kj-resultcard-start")) return (i+1)
      }
    }
    println("result start ",start)

    return start

  }

  def ReadShowResultType() : String = {
    for (i <- 0 until cards.length) {
      val (length, saved_area1, saved_area2) = cards(i) //area2 is awi-data class
      for (j <- 0 until length) {
        if (saved_area2(j).contains("awi-typecreate-page")) {
          println("found compare type 1")
          val option_div = div(cls := saved_area2(j)).render
          option_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(j))
          if (option_div.getElementsByClassName("kj-compare-type").length > 0) {
            val compareType = option_div.getElementsByClassName("kj-compare-type")(0).asInstanceOf[html.Select]
            println("found compare type", compareType.getAttribute("data-hold-value"))
            return compareType.getAttribute("data-hold-value")
          }
        }
      }
    }
    println("default compare type")

    return "score"
  }



  def IsSectionMark(index:Int) : Boolean = {
    if (index < cardLen) {
      val (length, saved_area1, saved_area2) = cards(index)
      for (j <- 0 until length) {
        if (saved_area2(j).contains("kj-section-line")) return true
        if (saved_area2(j).contains("kj-typequiz-add")) return true
      }
    }
    return false
  }

  def NewPlayCard(index : Int = 0): Unit = {
    val cardnum = s"cardnum-$idstr-$index"
    val onecard = div(cls := cardnum).render


    var playChartOn = false
    cur_play = index

    if(real) piwik.piwik_event_push("Play",cardnum,idstr)

    println("now playing :", cur_play)



    val (length,saved_area1,saved_area2) = cards(index)
    println(length,saved_area2(0))
    for (i <- 0 until length) {
      //println(length,saved_area1(i))
      if (saved_area2(i).contains("awi-text-page")) {
        //println("awi-text-page")
        val page_div = div(cls := saved_area2(i)).render

        page_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i)).replaceAll("w3-border", "").replaceAll("awi-border","")
        onecard.appendChild(div(cls := "w3-cell-row",page_div).render)
      } else if (saved_area2(i).contains("awi-image-page")) {
        //println("awi-image-page")
        if (saved_area2(i).contains("w3-left") && !saved_area2(i).contains("lyAlign")) {
          saved_area2(i) = saved_area2(i).replaceAll("w3-left", "")
        }

        val image_div = div(cls := saved_area2(i)).render
        image_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
        new ImageDiv(image_div)
        image_div.setAttribute("async","false")
        onecard.appendChild(div(cls := "w3-cell-row",image_div).render)
      } else if (saved_area2(i).contains("awi-flip-page")) {
        println("awi-flip-page")
        val option_div = div(cls := saved_area2(i)).render
        option_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
        PlayFlip(option_div,onecard)
        //onecard.appendChild(div(cls := "w3-cell-row",option_div).render)
      } else if (saved_area2(i).contains("awi-option-page")) {
        println("awi-option-page")
        val option_div = div(cls := saved_area2(i)).render
        option_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
        onecard.appendChild(div(cls := "w3-cell-row",option_div).render)
        PlayOption(option_div,onecard)
      } else if (saved_area2(i).contains("awi-typeres-page")) {
        println("awi-typeres-page")
        val option_div = div(cls := saved_area2(i)).render
        option_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
        onecard.appendChild(div(cls := "w3-cell-row",option_div).render)
        //move after bodies's form =>  PlayChart(option_div)
        playChartOn = true
      } else if (saved_area2(i).contains("awi-rule-page")) {
        println("awi-rule-page")
        val rule_div = div(cls := saved_area2(i)).render
        rule_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
        onecard.appendChild(div(cls := "w3-cell-row",rule_div).render)
        //println(rule_div.innerHTML)
        PlayRule(rule_div,onecard)
      } else if (saved_area2(i).contains("awi-typetest-page")) {
        //println("awi-option-page")
        ResetPlayMode()
        val option_div = div(cls := saved_area2(i)).render
        option_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
        onecard.appendChild(div(cls := "w3-cell-row",option_div).render)

        //create options
        PlayTypeOne(option_div,onecard)
      }
    }
    //println("appendChild")
    val bodies = div(cls:="kj-container").render

    val NewPlayCard = div (cls:=s"kj-card-$idstr kj-cardnum-$index kj-card-2 w3-round w3-white w3-margin-top",
      div(cls:="kj-container"),bodies).render

    // insert
    val sharedivs = main_stream.getElementsByClassName(s"kj-share-$idstr")
    if (sharedivs != null && !js.isUndefined(sharedivs(0))) {
      println("insert before")
      main_stream.insertBefore(NewPlayCard, sharedivs(0))

    } else {
      println("insert append")
      main_stream.appendChild(NewPlayCard)
    }


    bodies.appendChild(onecard)

    // move to card top
    dom.window.document.getElementsByTagName("body")(0).asInstanceOf[html.Body].scrollTop = main_stream.offsetTop - 40

    if (playChartOn) {
      PlayChart(onecard.getElementsByClassName("awi-typeres-page")(0).asInstanceOf[html.Div])
    }

      //CheckChart(bodies)
    CheckPlayBtn(bodies, onecard)

  }  // end of NewPlayCard

  //Only adjust image
  def ShowPlayCard (index:Int): Unit = {
    if (index < cardLen) {
      val cards = main_stream.getElementsByClassName(s"kj-card-$idstr")

      //post processing
      /* no more
      val images = main_stream.getElementsByTagName("img")
      for (tool <- images) {
        if (tool.asInstanceOf[html.Div].className != null && !tool.asInstanceOf[html.Div].className.contains("awi-option-image"))
          tool.asInstanceOf[html.Div].className += " awi-image"
      }
      */

    }
  }

  def NewFinalCard(label1 :String,result1:String,label2:String,result2:String, showType:String): Unit = {
    //val option_div = div(cls := saved_area2(i)).render
    //option_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
println("showType",showType)
println("result1",result1)
println("result2",result2)
    val guid1 = Util.guid()
    //val guid2 = Util.guid()

    val container1 =   div(cls:="w3-col",
      canvas(id:=guid1, style:="width:400px")).render
    //val container2 =   div(cls:="w3-col",
    //  canvas(id:=guid2, style:="width:400px")).render

    val resetBtn = div(cls := "kj-option-cell w3-cell-row", button(cls := "kj-option-next w3-button w3-block w3-blue w3-margin", "Reset")).render
    resetBtn.onclick = (e: dom.Event) => ResetCard()


      val totalSelect = div(label("Your score so far ", cls:="w-fixed2"),input( cls:="w-fixed2",value:=s"$final_calc_sum",disabled)).render
    val likelySelect = div(label("Your type is likely  ", cls:="w-fixed2"),input( cls:="w-fixed2",value:=s"$final_calc_max",disabled)).render

    val result = div(totalSelect,likelySelect,container1).render

    val optPage = div(cls := "w3-cell-row kj-row", div(cls := "awi-data awi-typeres-page", result, resetBtn)).render

    //println("appendChild")
    val bodies = div(cls:="kj-container").render

    val NewPlayCard = div (cls:=s"kj-card-$idstr kj-card-2 w3-round w3-white w3-margin-top",
      div(cls:="kj-container"),bodies).render

    // insert
    val sharedivs = main_stream.getElementsByClassName(s"kj-share-$idstr")
    if (sharedivs != null && !js.isUndefined(sharedivs(0))) {
      println("insert before")
      main_stream.insertBefore(NewPlayCard, sharedivs(0))

    } else {
      println("insert append")
      main_stream.appendChild(NewPlayCard)
    }


    bodies.appendChild(optPage)

    println(label1,result1)
    println(label2,result2)
    val jlabel1 = js.JSON.parse(label1)
    val jlabel2 = js.JSON.parse(label2)

    val jresult1 = js.JSON.parse(result1)
    val jresult2 = js.JSON.parse(result2)
println("***",result2)
    if (showType.equals("type")) {
      if (jresult2.toString == "")
        container1.appendChild(div("Congratulation!<br>You're the first submiter.").render)
      else
        myChart.ResultChart(guid1, "pie", jlabel2, jresult2, "All Choice")
    }
    else if (showType.equals("score")) {
      if (jresult1.toString == "")
        container1.appendChild(div("Congratulation!<br>You're the first submiter.").render)
      else
        myChart.ResultChart(guid1, "bar", jlabel1, jresult1, "Score Comparison")
    }


    // move to card top
    //dom.window.document.getElementsByTagName("body")(0).asInstanceOf[html.Body].scrollTop = onecard.offsetTop - 40

  }  // end of NewFinalCard
  //delete old card and create next card

  var playmode = 1 //"play", "result"//


  def ResetPlayMode () = {
    playmode =1
  }
  def SetReportMode () = {
    playmode = 2
  }

  def isTestMode () : Boolean = {
    if (playmode == 1) return true
    else return false
  }

  def isReportMode () : Boolean = {
    if (playmode ==2 ) return true
    else return false
  }

  def isNormalMode () : Boolean = {
    if (playmode >= 2 ) return true
    else return false
  }

  def NextPlayCard(incPage:String = "1") = {
    var inc = 1
    if (incPage !=null && !js.isUndefined(incPage)) inc = incPage.toInt

    println("current card:",cur_play)

    if ((cur_play + inc) < cardLen) {

      if (IsSectionMark(cur_play + inc)) {
        inc = resultStart - cur_play

        for (response <- PlayResponse) {
          val word = response._3   //typename
          val score = response._4   //typescore
          val oldCount =
            if (playResultMap.contains(word)) playResultMap(word)
                else 0
          val oldSum =
            if (playResultSumMap.contains(word)) playResultSumMap(word)
            else 0
          playResultMap += (word -> (oldCount + 1))
          playResultSumMap += (word -> ((oldCount + 1) * score.toInt))  //working data
        }
        val res = s"Your response caculated=> $playResultMap. \n Your type is ${playResultMap.max}"
        val sumres = s"Your SUM response caculated=> $playResultSumMap. \n Your type is ${playResultSumMap.max}"
        println(res,sumres)
        SetReportMode()

      }
        DelPlayCard(cur_play)
        cur_play += inc
        NewPlayCard(cur_play)
        ShowPlayCard(cur_play)
    }
  }

  def MarkPlayResponse(response:String,typename:String, typescore : String) : String = {
    PlayResponse.+=:(cur_play,response,typename,typescore)

    println(cur_play,response,typename,typescore)

    return "1"
  }


  def ResetCard() = { //bugs
    println("reset click",cur_play)
      PlayResponse.clear()
      playResultMap.clear()
      playResultSumMap.clear()

      DelPlayCard(cur_play)
      cur_play = 0
      ResetPlayMode()
      NewPlayCard(playStart)
      ShowPlayCard(playStart)
  }


  def DelPlayCard(index:Int): Unit ={
    val card = main_stream.getElementsByClassName(s"kj-card-$idstr")(0)
    main_stream.removeChild(card)
  }


  def PlayTypeOne(optiondiv: html.Div,onecard: html.Div): Unit = {
    var index = 0
    //var height = 100
    //var pause = init_pause
    import scala.util.Random

    var opt_data = new ArrayBuffer[String]()
    var opt_class = new ArrayBuffer[String]()

    var opt_typename = new ArrayBuffer[String]()
    var opt_typescore = new ArrayBuffer[String]()
    var opt_action = new ArrayBuffer[String]()
    //var opt_imgdata = new ArrayBuffer[String]()

    // save otion data & image
    val options = optiondiv.getElementsByClassName("kj-map-item")
    println("options",options.length)
    val length = options.length
    for (i <- 0 until options.length) {
      opt_data += options(i).asInstanceOf[html.Div].getElementsByClassName("kj-map-val")(0).asInstanceOf[html.Input].getAttribute("data-hold-value")
      val styleAttr = dataAttr(options(i).asInstanceOf[html.Div].getElementsByClassName("kj-map-val")(0).asInstanceOf[html.Input])
      if (styleAttr.isEmpty) opt_class += " w3-teal"
      else opt_class += styleAttr

      opt_typename += options(i).asInstanceOf[html.Div].getElementsByClassName("kj-map-type")(0).asInstanceOf[html.Label].getAttribute("data-hold-value")
      opt_typescore += options(i).asInstanceOf[html.Div].getElementsByClassName("kj-map-val")(0).asInstanceOf[html.Input].getAttribute("data-score-value")
    }
    //check random
    var option_random = true
    val randomDiv = optiondiv.getElementsByClassName("kj-random-check")
    if (randomDiv.length>0) {
      val randomStr = randomDiv(0).asInstanceOf[html.Div].getAttribute("data-randomCheck")
      if (randomStr == null || randomStr.equals("on")) option_random = true
      else option_random = false
    }


    //destroy all child
    while (optiondiv.firstChild != null) optiondiv.removeChild(optiondiv.firstChild)

    var shuffled_data = opt_data
    var shuffled_typename = opt_typename
    var shuffled_typescore = opt_typescore
    //shuffle opt_data & opt_typename
    println("random",Random.nextInt(2))
    if (option_random && Random.nextInt(2) == 0) {
      println("random reverse ",Random.nextInt(2))
      shuffled_data = opt_data.reverse
      shuffled_typename = opt_typename.reverse
      shuffled_typescore = opt_typescore.reverse
    }

    playOptions = shuffled_typename

    //create new option buttons
    for (i <- 0 until shuffled_data.length if shuffled_data.length > 0) {
      val page = index +1
      val Num = i + 1
      val optStyle = opt_class(i)

      val optBtn = button(cls:=s"kj-option-btn w3-button w3-block w3-margin-top $optStyle",shuffled_data(i),
                          data("hold-value"):=shuffled_typename(i),data("score-value"):=shuffled_typescore(i)).render
      optiondiv.appendChild(optBtn)

      // show result image when option btn clicked //
      optBtn.onclick = (e:dom.Event) => {
        println("option clicked",onecard.offsetTop)
        // check multiple awi-typetest-page in onecard has been disabled
        val tests = onecard.getElementsByClassName("awi-typetest-page")
        var remains = 0
        tests.foreach {
          p=> {
            val test = p.asInstanceOf[html.Div].getElementsByClassName("kj-option-btn")(0).asInstanceOf[html.Button].disabled
            if (!test.getOrElse(true)) remains += 1
          }
        }
        println("test remains", tests.length,remains)
        if (remains <= 1)  //last test in onecard
         dom.window.document.getElementsByTagName("body")(0).asInstanceOf[html.Body].scrollTop = onecard.offsetTop - 40

        optiondiv.setAttribute("data-response", shuffled_data(i))
        val nextcard = MarkPlayResponse(shuffled_data(i), shuffled_typename(i), shuffled_typescore(i))
        println("next clicked...", nextcard)
        if (remains <= 1) //last test in onecard
          NextPlayCard(nextcard)
        else {
          optiondiv.getElementsByClassName("kj-option-btn").foreach{
            p=>{
              p.asInstanceOf[html.Button].disabled=true
            }
          }
          optBtn.className = "kj-option-btn w3-block w3-margin-top w3-grey"
          //optBtn.className = optBtn.className.replaceAll("w3-button","")
        }
      }
    }

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
    println("options", options.length)
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
      val page = index + 1
      val Num = i + 1
      val optStyle = opt_class(i)

      val optBtn = button(cls := s"kj-option-btn w3-button w3-block w3-margin-top $optStyle", opt_data(i)).render
      optiondiv.appendChild(optBtn)

      // show result image when option btn clicked //
      optBtn.onclick = (e: dom.Event) => {
        println("option clicked", onecard.offsetTop)
        optiondiv.setAttribute("data-response", opt_data(i))
        NextPlayCard(opt_action(i))

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
      val styleAttr = dataAttr(options(i).asInstanceOf[html.Div].getElementsByClassName("kj-flip-val")(0).asInstanceOf[html.Input])
      if (styleAttr.isEmpty) opt_class += " w3-teal"
      else opt_class += styleAttr
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
    val flipdiv = optiondiv.cloneNode(false)
    val explain = div(cls:="kj-flip-explain").render
    val explainEditor = optiondiv.getElementsByClassName("ql-editor")
    var explainText : String = ""
    if (explainEditor.length > 0) explainText = explainEditor(0).asInstanceOf[html.Div].innerHTML

    //create new option buttons
    for (k <- 0 until opt_data.length if opt_data.length > 0) {
      val page = index +1
      val Num = k + 1
      val optStyle = opt_class(k)

      val optBtn = button(cls:=s"kj-option-btn w3-button w3-block w3-margin-top $optStyle",opt_data(k)).render
      val resetBtn = div(cls:="kj-reset-btn",label(i( cls:= "fa fa-refresh w3-large"),cls:=""," reset")).render
      val optImage = div(cls:="w3-animate-top",
        img(cls:="awi-image", src:=opt_image(k),alt:="Image"),
        resetBtn
      ).render

      flipdiv.appendChild(optBtn)

      // show result image when option btn clicked //
      optBtn.onclick = (e:dom.Event) => {
        while (flipdiv.firstChild != null)  flipdiv.removeChild(flipdiv.firstChild)
        flipdiv.appendChild(optImage)
        flipdiv.appendChild(explain)
        explain.innerHTML = explainText
        explain.appendChild(resetBtn)

      }
      resetBtn.onclick = (e:dom.Event) => {
        while (flipdiv.firstChild != null)  flipdiv.removeChild(flipdiv.firstChild)
        PlayFlip(optiondiv,onecard)
      }
    }
    onecard.appendChild(flipdiv)

  }


  def PlayChart(optiondiv: html.Div) = {
    //bodies.appendChild(div(canvas(id:="myChart",style:="width:400px;height:400px")).render)
    //val ctx = dom.window.document.getElementById("myChart")
    val ResultMap = Map.empty[String, Int]  // typetest Result Map : word (label) -> count
    val ResultSumMap = Map.empty[String, Int]  // typetest Result Map : word (label) -> Sum ( count * score)

    //check response
    for (response <- PlayResponse) {
      val word = response._3   //typename
      val score = response._4   //typescore
      val oldCount =
        if (ResultMap.contains(word)) ResultMap(word)
        else 0
      val oldSum =
        if (ResultSumMap.contains(word)) ResultSumMap(word)
        else 0
      ResultMap += (word -> (oldCount + 1))
      ResultSumMap += (word -> ((oldCount + 1) * score.toInt))  //working data
    }

    println(ResultMap,ResultSumMap)

    println("optiondiv",optiondiv.innerHTML)

    val typeDiv = optiondiv.asInstanceOf[html.Div].getElementsByClassName("kj-result-type")(0)
    val dataDiv = optiondiv.asInstanceOf[html.Div].getElementsByClassName("kj-result-data")(0)
    //typeDiv.asInstanceOf[html.Div].className += " w3-hide"
    //dataDiv.asInstanceOf[html.Div].className += " w3-hide"

    var ctype = typeDiv.asInstanceOf[html.Select].getAttribute("data-hold-value")
    var dtype = dataDiv.asInstanceOf[html.Select].getAttribute("data-hold-value")
    if (ctype == null) ctype = "bar"
    if (dtype == null) dtype = "Count"
    val guid = optiondiv.asInstanceOf[html.Div].getElementsByTagName("canvas")(0).asInstanceOf[html.Canvas].id

    var graphVal = typeDiv.asInstanceOf[html.Select].getAttribute("data-graphCheck")
    if (graphVal == null) graphVal = "on"

    var lastVal = typeDiv.asInstanceOf[html.Select].getAttribute("data-lastCheck")
    if (lastVal == null) lastVal = "on"

    var totalVal = typeDiv.asInstanceOf[html.Select].getAttribute("data-totalCheck")
    if (totalVal == null) totalVal = "on"

    var likelyVal = typeDiv.asInstanceOf[html.Select].getAttribute("data-likelyCheck")
    if (likelyVal == null) likelyVal = "on"

    // create new resukt page
    val container =   div(cls:="w3-padding",
      canvas(id:=guid, style:="width:400px")).render


    var labeldata: String = ""
    var resultdata: String = ""
    var resMap = Map.empty[String, Int]  // typetest Result Map : word (label) -> count
    if (dtype.equals("Count")) resMap = ResultMap
    else resMap = ResultSumMap



    println(resMap.keys)
    println(resMap.values)

    if (PlayResponse.length > 0) {
      while (optiondiv.firstChild != null) optiondiv.removeChild(optiondiv.firstChild)

      val last1 = PlayResponse.head._2 //response
      val last2 = PlayResponse.head._4 //score
      val calc_sum = ResultSumMap.foldLeft(0)(_ + _._2)
      val calc_max = ResultMap.maxBy(_._2)._1

      //save result to final
      final_calc_sum = calc_sum
      final_calc_max = calc_max

      if (totalVal.contains("on")) final_sum_on = "off"
      if (likelyVal.contains("on")) final_max_on = "off"



      val lastSelect = div(label("Your last select   ", cls:="w-fixed2"),input( cls:="w-fixed2",value:=s"$last1",disabled)).render
      val totalSelect = div(label("Your score so far ", cls:="w-fixed2"),input( cls:="w-fixed2",value:=s"$calc_sum",disabled)).render
      val likelySelect = div(label("Your type is likely  ", cls:="w-fixed2"),input( cls:="w-fixed2",value:=s"$calc_max",disabled)).render

      val result = div(lastSelect,totalSelect,likelySelect, container).render


      if (lastVal.contains("on")) optiondiv.appendChild(lastSelect)
      if (totalVal.contains("on")) optiondiv.appendChild(totalSelect)
      if (likelyVal.contains("on")) optiondiv.appendChild(likelySelect)

      if (graphVal.contains("on")) {
          optiondiv.appendChild(container)
        //var i = 0

        // made for lavelstr = [ "ABC" , "DEF", "EHF"]
        //      for resultstr  [ "10",   "20",  "30"]

        for (resOne <- resMap) {
          var option = resOne._1
          var res = resOne._2
          if (resOne != resMap.last) {
            labeldata += s""" "$option", """;
            resultdata += s""" "$res", """
          } else {
            labeldata += s""" "$option" """ ;
            resultdata += s""" "$res" """
          }
        }


        val labelstr = s""" [$labeldata] """
        println("labelstr", labelstr)
        val labels = js.JSON.parse(labelstr)


        val resultstr = s""" [$resultdata] """
        println("resultstr", resultstr)
        val results = js.JSON.parse(resultstr)
        println("found awi-typeres-page 3", guid, labels, results)

        myChart.ResultChart(guid, ctype, labels, results,"Choice")
      } //if graohVal
    } else {  // if platResponse
      val nulldata = div("No response data found").render
      optiondiv.appendChild(nulldata)
    }
  }


  def PlayRule(optiondiv: html.Div,onecard: html.Div): Unit = {
    var index = 0
    //var height = 100
    //var pause = init_pause

    // save otion data & image
    val rules = onecard.getElementsByClassName("kj-rule-result")
    println("options", rules.length)
    val length = rules.length
    var fire = false
    var stop = false


    for (rule <- rules if !stop) {
      val cond = rule.asInstanceOf[html.Div].getElementsByClassName("kj-rule-if")(0).asInstanceOf[html.Input].getAttribute("data-if-value")
      val state = rule.asInstanceOf[html.Div].getElementsByClassName("kj-rule-state")(0).asInstanceOf[html.Input].getAttribute("data-state-value")
      val value = rule.asInstanceOf[html.Div].getElementsByClassName("kj-rule-val")(0).asInstanceOf[html.Input].getAttribute("data-hold-value")

      val action = rule.asInstanceOf[html.Div].getElementsByClassName("kj-rule-action")(0).asInstanceOf[html.Input].getAttribute("data-action-value")

      var calc_sum = playResultSumMap.foldLeft(0)(_ + _._2)

      if (cond.equals("SUM")) println("If", calc_sum, state, value, "then goto :", action )
      println("If MAX Type is ", value, "Then goto ", action )
      println("playResultMap", playResultMap)
      println("MAX Chosen Type", playResultMap.maxBy(_._2))
      if (cond.equals("MAX") && playResultMap.maxBy(_._2)._1.equals(value)) fire = true
      else if (cond.equals("SUM")) {
        state match {
          case "EQ" =>
            if (value.toInt == calc_sum) fire = true
          case "GT" =>
            if (value.toInt < calc_sum) fire = true
          case "LT" =>
            if (value.toInt > calc_sum) fire = true

        }
      }

      if (fire) {
        println("fire")
        while (optiondiv.firstChild != null) optiondiv.removeChild(optiondiv.firstChild)

        //create new option buttons
        val optBtn = button(cls := "kj-option-btn w3-button w3-round w3-block w3-margin-top", "Check Result").render
        optiondiv.appendChild(optBtn)

        // show result image when option btn clicked //
        optBtn.onclick = (e: dom.Event) => {
          println("goto",action.toInt)
          DelPlayCard(cur_play)
          cur_play = cur_play + action.toInt
          NewPlayCard(cur_play)
          ShowPlayCard(cur_play)
        }
        stop = true
        return
      } //fired
    } // for loop
    if (!fire) {  // default action
      println("not fire")
      val optBtn = button(cls := "kj-option-btn w3-button w3-round w3-block w3-margin-top", "Next").render
      optiondiv.appendChild(optBtn)

      // show result image when option btn clicked //
      optBtn.onclick = (e: dom.Event) => {
        DelPlayCard(cur_play)
        cur_play +=  1
        NewPlayCard(cur_play)
        ShowPlayCard(cur_play)
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


  def CheckPlayBtn(bodies: html.Div, onecard: html.Div) : Unit = {
    import scala.collection.mutable.Map

    val num_btn = bodies.getElementsByClassName("kj-option-btn")
    println("num_options",num_btn.length)

    if(real) piwik.piwik_event_push("Play","Url",idstr)


    if (cur_play < (cardLen-1)) {

      println("next",cur_play,cardLen)

      if (num_btn.length <= 0) {
        val nextBtn = div(cls := "kj-option-cell w3-cell-row", button(cls := "kj-option-next w3-button w3-block w3-blue w3-margin", "Next")).render
        bodies.appendChild(nextBtn)
        nextBtn.onclick = (e: dom.Event) => {
          dom.window.document.getElementsByTagName("body")(0).asInstanceOf[html.Body].scrollTop = onecard.offsetTop - 40
          NextPlayCard("1")
          ShowPlayCard(cur_play)
        }
      }
    } else { //final card
      println("submit",cur_play,cardLen)
      val submitBtn = div(cls := "kj-option-cell w3-cell-row",
        button(cls := "kj-option-next w3-button w3-block w3-blue w3-margin", "Show Others")).render
      bodies.appendChild(submitBtn)
      submitBtn.onclick = (e: dom.Event) => {
        //dom.window.alert("Thank you!.")
        if (real) piwik.piwik_event_push("Play","Submit",idstr)
        //println(s"calc_sum-$final_sum_on-$final_calc_sum",s"calc_max-$final_max_on-$final_calc_max")
        if (real) piwik.piwik_event_push("Play",s"calc_sum_on-$final_calc_sum",idstr)
        if (real) piwik.piwik_event_push("Play",s"calc_max_on-$final_calc_max",idstr)

        //update stat value
        val piwikBase = s"php/piwik_playbyid.php?id=$idstr"

        //dom.window.alert("called Tab:"+objNm+":"+objId+":"+tab)
        ///////////////////////////////////////////////
        val request = Ajax.get(piwikBase)
        request.onComplete {
          case Success(xhr) =>
            println("piwik",xhr,xhr.responseText)
            if (xhr.responseText.length<=0) return
            val res = js.JSON.parse(xhr.responseText)
            val hits = res.asInstanceOf[js.Array[js.Dynamic]]

            var ResultCount = 0
            var ResultSum = 0
            var ResultAvg = 0
            var ResultMax = 0
            val ResultMap = Map.empty[String, Int]  // typetest Result Map : word (label) -> count

            for (index <- 0 until hits.length) {
              println(hits(index).label,hits(index).nb_events)

              if (hits(index).label.asInstanceOf[String].startsWith("calc_sum_on-")) {
                val score = hits(index).label.asInstanceOf[String].substring(12)
                var count = hits(index).nb_events.asInstanceOf[Int]
                ResultSum += score.toInt * count  //working data
                ResultCount += count
                if (score.toInt > ResultMax) ResultMax = score.toInt
                println("resultsum",score,ResultSum,ResultCount)
              } else if (hits(index).label.asInstanceOf[String].startsWith("calc_max_on-")) {
                val ctype = hits(index).label.asInstanceOf[String].substring(12)
                var count = hits(index).nb_events.asInstanceOf[Int]
                var oldCount = 0
                if (ResultMap.contains(ctype)) oldCount = ResultMap(ctype)
                ResultMap += (ctype -> (oldCount + count))
                println("resultmax",ctype,oldCount + count)
              }
            }  //for

            if (ResultCount > 0) ResultAvg = ResultSum / ResultCount
            else ResultAvg = ResultSum

            //val label1 = s"""["10" , "15" ]"""
            val label1 = s"""["Your's" , "$ResultCount people's Average", "Max Score" ]"""
            val result1 = s"""["$final_calc_sum" , "$ResultAvg", "$ResultMax" ]"""

            var labeldata: String = ""
            var resultdata: String = ""

            for (resOne <- ResultMap) {
              var option = resOne._1
              var res = resOne._2
              if (resOne != ResultMap.last) {
                labeldata += s""" "$option", """;
                resultdata += s""" "$res", """
              } else {
                labeldata += s""" "$option" """ ;
                resultdata += s""" "$res" """
              }
            }
            val label2 = s""" [$labeldata] """
            val result2 = s""" [$resultdata] """
            println("label2", label2)
            println("result2", result2)

            DelPlayCard(cur_play)
            NewFinalCard(label1,result1,label2,result2,showResultType)

          case Failure(e) =>
            println(e.toString)
        }


      }
    }

  }
}
