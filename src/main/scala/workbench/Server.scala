package com.lihaoyi.workbench


import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import sbt.{IO, Logger, settingKey}
import spray.httpx.encoding.Gzip
import spray.routing.SimpleRoutingApp
import akka.actor.ActorDSL._
import org.parboiled.common.FileUtils
import upickle.Js
import upickle.default.{Reader, Writer}
import spray.http.{AllOrigins, HttpEntity, HttpResponse, StatusCodes}
import spray.http.HttpHeaders.`Access-Control-Allow-Origin`

import concurrent.duration._
import scala.concurrent.Future
import scala.io.Source
import org.scalajs.core.tools.io._
import org.scalajs.core.tools.logging.Level
import spray.http.HttpData.Bytes

import scala.tools.nsc
import scala.tools.nsc.Settings
import scala.tools.nsc.backend.JavaPlatform
import scala.tools.nsc.util.ClassPath.JavaContext
import scala.collection.mutable
import scala.tools.nsc.typechecker.Analyzer
import scala.tools.nsc.util.{DirectoryClassPath, JavaClassPath}
import spray.http.HttpHeaders._
import spray.http.HttpMethods._

class Server(url: String, port: Int, domainName:String,previewDir: String) extends SimpleRoutingApp{
  val corsHeaders: List[ModeledHeader] =
    List(
      `Access-Control-Allow-Methods`(OPTIONS, GET, POST),
      `Access-Control-Allow-Origin`(AllOrigins),
      `Access-Control-Allow-Headers`("Origin, X-Requested-With, Content-Type, Accept, Accept-Encoding, Accept-Language, Host, Referer, User-Agent"),
      `Access-Control-Max-Age`(1728000)
    )


  implicit val system = ActorSystem(
    "Workbench-System",
    config = ConfigFactory.load(ActorSystem.getClass.getClassLoader),
    classLoader = ActorSystem.getClass.getClassLoader
  )

  /**
   * The connection from workbench server to the client
   */
  object Wire extends autowire.Client[Js.Value, Reader, Writer] with ReadWrite{
    def doCall(req: Request): Future[Js.Value] = {
      longPoll ! Js.Arr(upickle.default.writeJs(req.path), Js.Obj(req.args.toSeq:_*))
      Future.successful(Js.Null)
    }
  }

  /**
   * Actor meant to handle long polling, buffering messages or waiting actors
   */
  private val longPoll = actor(new Actor{
    var waitingActor: Option[ActorRef] = None
    var queuedMessages = List[Js.Value]()

    /**
     * Flushes returns nothing to any waiting actor every so often,
     * to prevent the connection from living too long.
     */
    case object Clear
    import system.dispatcher

    system.scheduler.schedule(0.seconds, 10.seconds, self, Clear)
    def respond(a: ActorRef, s: String) = {
      a ! HttpResponse(
        entity = s,
        headers = corsHeaders
      )
    }
    def receive = (x: Any) => (x, waitingActor, queuedMessages) match {
      case (a: ActorRef, _, Nil) =>
        // Even if there's someone already waiting,
        // a new actor waiting replaces the old one
        waitingActor = Some(a)

      case (a: ActorRef, None, msgs) =>
        respond(a, upickle.json.write(Js.Arr(msgs:_*)))
        queuedMessages = Nil

      case (msg: Js.Arr, None, msgs) =>
        queuedMessages = msg :: msgs

      case (msg: Js.Arr, Some(a), Nil) =>
        respond(a, upickle.json.write(Js.Arr(msg)))
        waitingActor = None

      case (Clear, waitingOpt, msgs) =>
        waitingOpt.foreach(respond(_, upickle.json.write(Js.Arr(msgs :_*))))
        waitingActor = None
    }
  })

  /**
   * Simple spray server:
   *
   * - /workbench.js is hardcoded to be the workbench javascript client
   * - Any other GET request just pulls from the local filesystem
   * - POSTs to /notifications get routed to the longPoll actor
   */
  startServer(url, port) {
    import spray.http._


    println(s"starting $domainName")


    get {

      path("workbench.js") {
        complete {
          val body = IO.readStream(
            getClass.getClassLoader.getResourceAsStream("client-opt.js")
          )
          s"""
          (function(){
            $body

            com.lihaoyi.workbench.WorkbenchClient().main(${upickle.default.write(url)}, ${upickle.default.write(port)})
          }).call(this)
          """
        }
      } ~
        path("post" ~ Segment) { pathRest =>
          println("post read :",url, s"$previewDir/htmltemp/$pathRest.html")
          //redirect("/target/scala-2.11/classes/post.html?a=" + pathRest, StatusCodes.MovedPermanently)
          respondWithMediaType(MediaTypes.`text/html`) {
            getFromFile(s"$previewDir/htmltemp/$pathRest.html")
          }
        } ~
        path("list" ~ Segment) { pathRest =>
          println("list read :",url, "=>",pathRest)
          respondWithMediaType(MediaTypes.`text/html`) {
            val html = FileUtils.readAllText("./target/scala-2.11/classes/list.html")
            complete { s""" $html """ }
          }
        } ~
        pathPrefixTest("example") {
          println("example_ :",url)
          getFromDirectory("./target/scala-2.11")
        } ~
        pathPrefix("preview") {
          println("preview :",url)
          getFromDirectory(s"$previewDir/preview")
        } ~
        getFromDirectory("./target/scala-2.11/classes")
    } ~
    post {
      //Aftrer save to ES, make htmltemp/{uuid.html} file genaration for facebook preview image
      // read data:image and save it to preview/XXX.jpg and
      // copy post.html to htmltemp/uuid.html with Title, SummaryText, Preview Image
      path("save" ~ Segment) { pathRest =>
        import scala.util.parsing.json._
        //import java.util.Base64

        println("save :",url, "=>",pathRest)
        //val idstr = pathRest
        entity(as[String]) { (Text) => {
            //println("parameter:",Text)
            val result = JSON.parseFull(Text)
            result match {
              // Matches if jsonStr is valid JSON and represents a Map of Strings to Any
              case Some(map: Map[String, Any]) => {

                val sumText = map("sumText")
                var imgsrc : String = map("imgsrc").toString
                val cardNum : String = map("cardNum").toString
                //println("parsed:",sumText,imgsrc)
                //save preimage to local
                if (imgsrc.startsWith("data:image/")) {
                  val Image = imgsrc.substring(imgsrc.indexOf(",")+1,imgsrc.length)
                  val Type = imgsrc.substring(11,imgsrc.indexOf(";"))
                  println("type",Type)
                  val bytes = new sun.misc.BASE64Decoder().decodeBuffer(Image)
                  println("bytes",bytes)
                  FileUtils.writeAllBytes(bytes,s"$previewDir/preview/$cardNum.$Type")
                  imgsrc = s"http://${domainName}/preview/$cardNum.$Type"
                }

                val htmlbody = FileUtils.readAllText("./target/scala-2.11/classes/post.html")
                FileUtils.writeAllText(htmlbody.replace("@StoryFactoryTitle",s"@StoryFactoryTitle")
                  .replace("@StoryFactoryDesc",s"$sumText")
                    .replace("@StoryFactorImage",s"$imgsrc"),s"$previewDir/htmltemp/$cardNum.html")
              }
              case None => println("Parsing failed")
              case other => println("Unknown data structure: " + other)
            }

            complete { StatusCodes.OK }
          }
        }
      } ~
      path("notifications") { ctx =>
      longPoll ! ctx.responder
      }
    }
  }
  def kill() = system.shutdown()

}
