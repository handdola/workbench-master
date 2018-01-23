package lib


import scala.scalajs.js
import js.annotation._
/**
  * Created by Administrator on 2017-07-14.
  */


//***************pls include context.menu.js to html
@js.native
@JSGlobalScope
object contextMenu extends js.Object {
  def initAll(menuClickAction:js.Any,menuPasteAction:js.Any): js.Any = js.native
  def toggleMenuOn() : js.Any = js.native
  def toggleMenuOff() : js.Any = js.native
  def filterContextMenu(mode :js.Any,objtype :js.Any,doctype :js.Any) : js.Any = js.native
}