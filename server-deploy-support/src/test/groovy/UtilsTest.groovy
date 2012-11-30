import org.junit.Test
import org.junit.Ignore
import static org.junit.Assert.*
import java.security.KeyStore


class UtilsTest {
    
    @Test
    void randomString() {
        def all = []
        30.times {
            def s = Utils.randomString(8)
            assertFalse(all.contains(s))
            all << s
        }
    }

    @Test
    void createEmptyKeystore() {
        def tmp = File.createTempFile("xxxx",".jks")
        tmp.delete()
        tmp.deleteOnExit()
        def keystore = Utils.createKeystore(null,tmp,"xyzabc".toCharArray(),false)
        assertEquals(0,keystore.size())
    }

    @Test @Ignore
    void createKeystoreWithCertificate() {
        def tmp = File.createTempFile("xxxx", ".jks")
        tmp.delete()
        tmp.deleteOnExit()
        def passphrase = "xyzabc".toCharArray()
        def keystore = Utils.createKeystore(null, tmp, passphrase, true)
        def cert = keystore.getCertificate("georchestra")
        def key = keystore.getKey("georchestra", passphrase)
        assertNotNull(cert)
        assertNotNull(key)

        cert = keystore.getCertificate("localhost")
        assertNotNull(cert)
        assertEquals(2, keystore.size())
        
        check(tmp)
    }
    void check(keyStore) {
        def store = KeyStore.getInstance(KeyStore.getDefaultType())
        def is = new FileInputStream(keyStore)
        def passphrase = "xyzabc".toCharArray()
        store.load(is, passphrase)
        is.close()
        def aliases = store.aliases()
        while(aliases.hasMoreElements()) {
            def alias = aliases.nextElement()
            println("=======================================================================")
            println(alias)
            println("*******************************")
            println(store.getKey(alias,passphrase))
            println("------------------------------------------------------------------------")
            println(store.getCertificate(alias))
            println("=======================================================================")
        }
    }
}
