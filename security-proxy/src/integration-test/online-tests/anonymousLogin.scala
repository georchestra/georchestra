new Test (args) {
  "extractor be available via anonymous login" inService "extractorapp" doGet "" should (result => {
    result has ('responseCode -> 200)
  }) 
}