class Artifact {
    final File file

    final String name
    final String simpleName
    final String ext

    Artifact(File file, aliasFunc) {
        this.file = file
        this.name = aliasFunc(file)
        if(this.name == null) this.name = file.name

        this.simpleName = processName({i -> return name.substring(0, i)}, {return name})
        this.ext = processName({i -> return name.substring(i + 1)}, {return null})
    }

    private def processName(extClosure, noExtClosure) {
        def i = name.lastIndexOf('.')
        if (i > -1) return extClosure(i)
        else return noExtClosure()
    }

    String toString() {return "$name -> file.path"}


    boolean equals(o) {
        if (this.is(o)) return true;
        if (getClass() != o.class) return false;

        Artifact artifact = (Artifact) o;

        if (file != artifact.file) return false;
        if (name != artifact.name) return false;

        return true;
    }

    int hashCode() {
        int result;
        result = (file != null ? file.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
