import static org.junit.Assert.*
import org.junit.Test

class ArtifactTest {
    @Test
    void basicArtifact() {
        def a = new Artifact(new File("./files/testfile.war"),{return it.name})
        assertEquals "testfile.war",a.name
        assertEquals "testfile",a.simpleName
        assertEquals "war",a.ext
    }
    @Test
    void aliasing() {
        def a = new Artifact(new File("./files/testfile.war"),{return "name.ext"})
        assertEquals "name.ext",a.name
        assertEquals "name",a.simpleName
        assertEquals "ext",a.ext
    }
    @Test
    void cornerCasesArtifact() {
        def a = new Artifact(new File("./files/testfile"),{return null})
        assertEquals "testfile",a.name
        assertEquals "testfile",a.simpleName
        assertNull "war",a.ext
    }
}
