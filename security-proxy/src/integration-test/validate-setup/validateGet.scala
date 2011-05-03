new Test (args) {
  "extractor be available" inService "extractorapp/" withGet "" should (result => {
    result has ('responseCode -> 200)
  }) 
}