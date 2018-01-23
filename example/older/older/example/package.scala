package older

/**
  * Created by Administrator on 2017-04-20.
  */
package object example {


   //import scalatags.Text.all._
  // OR
  import scalatags.JsDom.all._
  val xyz = html(
    head(
      script(src:="..."),
      script(
        "alert('Hello World')"
      )
    ),
    body(
      div(
        h1(id:="title", "This is a title"),
        p("This is a big paragraph of text")
      )
    )
  )

}
