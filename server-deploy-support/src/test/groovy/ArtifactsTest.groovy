import org.junit.Test
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertNotNull

class ArtifactsTest {
    def project = new Object(){
        def version = "1.0"
        def warDir = ArtifactsTest.class.getClassLoader().getResource(".").getFile() + "wardir"
        def properties = ["warsDir":warDir]
    }
    @Test
    void artifacts() {
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

        artifacts.each {a ->
            assert a.name == aliasFunction(a.file)
            if(a.file.name.endsWith(".war"))
                assert a.name.endsWith(".war")
        }
    }
    @Test
    void find() {
        def artifacts = new Artifacts(project)

        assertEquals artifacts.artifacts.find{return it.name == "versioned-private.war"}, artifacts.find{return it.name == "versioned-private.war"}
    }
    @Test
    void findAll() {
        def artifacts = new Artifacts(project)

        assertEquals artifacts.artifacts.findAll{return it.name.endsWith(".war")}, artifacts.findAll{return it.name.endsWith(".war")}
    }

    @Test
    void standardGeorchestraMapping() {
        def artifacts = new Artifacts(project, Artifacts.standardGeorchestraAliasFunction)

        assertNotNull artifacts.artifacts.find{it.name == "doc.war"}
        assertNotNull artifacts.artifacts.find{it.name == "cas.war"}
        assertNotNull artifacts.artifacts.find{it.name == "ROOT.war"}
        assertNotNull artifacts.artifacts.find{it.name == "geonetwork-private.war"}
    }
    @Test
    void each() {
        def artifacts = new Artifacts(project)
        def foundByEach = [] as Set

        artifacts.each {
            assert it instanceof Artifact
            foundByEach << it
        }

        assertEquals(artifacts.artifacts as Set, foundByEach)
    }
}
