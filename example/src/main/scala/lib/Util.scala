package lib

import org.scalajs.dom
import org.scalajs.dom.ext._
import org.scalajs.dom.html

import scala.scalajs.js
import js.annotation.{JSExport, _}




/**
  * Created by Administrator on 2017-05-09.
  * Actual script defined in each *.html
  */

@js.native
@JSGlobalScope
object Util extends js.Any {

  def guid(): js.Any = js.native

  def findAncestor (el : js.Any, cls : js.Any) : js.Any = js.native

  def fb_login(action: js.Any, cb : js.Any) : js.Any = js.native

}
