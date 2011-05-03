new Test (args) {
  "geoserver be available" inService "geoserver/welcome.do" withGet "" should (result => {
    result has ('responseCode -> 200)
  })
}
