import org.junit.Test
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertNotNull

class ArtifactsTest {
    @Test
    void artifacts() {
        def project = new Object(){
            def version = "1.0"
            def warDir = ArtifactsTest.class.getClassLoader().getResource(".").getFile() + "wardir"
            def properties = ["warsDir":warDir]
        }
        def aliasFunction = Artifacts.versionNumToPrivateMapping {
            if(it.name.startsWith("anotherfile"))
                return "aliased.war"
            else
            return null
        }
        def artifacts = new Artifacts(project,aliasFunction)

        def files = new File(project.warDir).listFiles() as Set

        assert files == artifacts.artifacts.collect{it.file} as Set

        assert artifacts.simpleNameMap['somefile'] != null
        assert artifacts.simpleNameMap['aliased'] != null
        assert artifacts.nameMap['aliased.war'] != null

        assertNotNull artifacts.artifacts.find{it.name == "versioned-private.war"}

        artifacts.foreach {a ->
            assert a.name == aliasFunction(a.file)
            if(a.file.name.endsWith(".war"))
                assert a.name.endsWith(".war")
        }
    }
}
