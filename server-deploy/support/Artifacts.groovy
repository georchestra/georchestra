class Artifacts {
  def project
  Artifacts(project) {
    this.project = project
  }
  def artifacts = new File(project.properties['warsDir']).listFiles()
  def nameMap = toNameMap()
  def simpleNameMap = toSimpleNameMap()
  def foreach(closure) {
    artifacts.each {closure(it)}
  }
  
  private def toSimpleNameMap() {
    def map = [:]
    artifacts.each{
      def name = it.name.substring(0,it.name.lastIndexOf("."))
      map[name] = it
    }
    return map
  }
  private def toNameMap() {
    def map = [:]
    artifacts.each{
      map[it.name] = it
    }
    return map
  }
  
}