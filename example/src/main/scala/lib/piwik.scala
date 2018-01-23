package lib

/**
  * Created by Administrator on 2017-08-22.
  */

import scala.scalajs.js
import js.annotation._



@js.native
@JSGlobalScope
object piwik extends js.Object { // defined in post.html actually
  def piwik_event_push(category:js.Any, action:js.Any, name:js.Any):js.Any = js.native
  def piwik_content_impress(name:js.Any, piece:js.Any, target:js.Any):js.Any = js.native
  def piwik_content_action(action:js.Any, name:js.Any, piece:js.Any, target:js.Any) :js.Any = js.native
}


