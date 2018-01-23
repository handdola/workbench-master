  val res = js.JSON.parse(xhr.responseText)
        val name = res._source.name
        val iddiv = div(id := cardNum).render

        println("res",res._source.resume)
        val json_data = dom.window.atob(res._source.resume.asInstanceOf[String])
      println("json_data",json_data)
        val (length, saved_area1,saved_area2) = read[(Int,ArrayBuffer[String],ArrayBuffer[String])](json_data)

        for (i <- 0 until length) {
          println(length,saved_area1(i))
          println(length,saved_area2(i))
          if (saved_area2(i).contains("awi-text-page")) {
            val page_div = div(cls := saved_area2(i)).render
            page_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
            PageLoad(target, page_div)
          } else if (saved_area2(i).contains("awi-image-page")) {
            val image_div = div(cls := saved_area2(i)).render
            image_div.innerHTML = URIUtils.decodeURIComponent(saved_area1(i))
            ImageLoad(target, image_div)
          }

          //target.appendChild(page_div)
        }
