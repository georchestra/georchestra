import java.net._
 
new Test (args) with Parallel {
  val postData = "username=name"
  
  parallel ( 
    ServerFuture("server", 43999) { r =>
      assert(r.isInstanceOf[Post], r +" is not a Post request")

      val post = r.asInstanceOf[Post]
      assert(post.post == postData, "expected %s but got %s".format (postData, post.post))
    },
    
    thread ("client") {
      Thread.sleep(500)
      "post must pass through proxy" inService "test" doPost postData should (result => {
        result has ('responseCode -> 200)
      })
    }
  )
}