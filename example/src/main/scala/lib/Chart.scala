package lib


import scala.scalajs.js
import js.annotation._



//***************pls include story.common.js to html
@js.native
@JSGlobalScope
object myChart extends js.Object {
  def testChart(label:js.Any,datas:js.Any): js.Any = js.native
  def ResultChart(id:js.Any, ctype:js.Any,labels:js.Any,datas:js.Any,title:js.Any): js.Any = js.native
  def UpdateChart(chart:js.Any, id:js.Any, ctype:js.Any,labels:js.Any,datas:js.Any): js.Any = js.native
  def UpdateData(chart:js.Any, id:js.Any, labels:js.Any,datas:js.Any): js.Any = js.native
}

//***************pls include story.common.js to html
@js.native
class ReportChart(id:js.Any, ctype:js.Any,labels:js.Any,datas:js.Any) extends js.Any {
  def setType(ctype:js.Any) : js.Any = js.native
}
