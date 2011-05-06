class Artifact {
   final File file
   final def aliasFunction

   final String name
   final String simpleName
   final String ext
  
   Artifact(File file,String aliases) {
     this.file = file

     this.name = file.name
     this.simpleName = processName ({i -> return file.name.substring(0,i)},{return file.name})
     this.ext = processName ({i -> return file.name.substring(i+1)},{return null})
   }

   def getAlias() {aliasFunction(this)}
   
   private def processName(extClosure,noExtClosure) {
     def i = file.name.lastIndexOf('.')
     if(i > -1) return extClosure(i)
     else return noExtClosure()
   }
   
   def toString() {return $file.path}
   
   public boolean equals(Object o) {
       if (this == o) return true;
       if (o == null || getClass() != o.getClass()) return false;

       Artifact artifact = (Artifact) o;

       if (file != null ? !file.equals(artifact.file) : artifact.file != null) return false;

       return true;
   }

   public int hashCode() {
       return file != null ? file.hashCode() : 0;
   }
   
 }
