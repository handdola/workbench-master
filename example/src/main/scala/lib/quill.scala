package lib

import org.scalajs.dom.{Element, Node}

import scala.scalajs.js

/**
  * Created by Administrator on 2017-05-09.
  */


@js.native
class Quill(element: js.Any,options: js.Any) extends js.Any {
  def format(name: String, value: js.Any): js.Any = js.native
  def formatLine(index : js.Any, length: js.Any, name: String, value: js.Any): js.Any = js.native
  def formatText(index : js.Any, length: js.Any, name: String, value: js.Any): js.Any = js.native
  def setText(text: String): js.Any = js.native
}