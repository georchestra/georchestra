import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*
import sun.security.x509.*

/**
 * Several useful methods
 */
class Utils {
    static def randomString(length) {
        def list = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        list = list * (1 + length / list.size())
        Collections.shuffle(list)
        return length > 0 ? list[0..length - 1].join() : ''
    }

    static def createKeystore(log, outFile, char[] passphrase, boolean shouldAddSelfSignedCertificate) {
        if(log==null) {
            log = new Object(){
                def info(msg){System.out.println(msg)}
                def error(msg){System.err.println(msg)}
            }
        }

        KeyStore ks

        if(outFile instanceof File) {
            outFile.delete()
        } else {
            new File(outFile).delete()
        }
        if (shouldAddSelfSignedCertificate) {
            def alias = "georchestra"
            def pass = new String(passphrase)
            def cmd = "keytool -genkey -alias $alias -keystore ${outFile} -storepass ${pass} -keypass ${pass} -keyalg RSA -keysize 2048"

            if(System.getProperty("os.name").toLowerCase().startsWith("windows") ){
                cmd = cmd.replace("keytool -genkey","keytool.exe -genkey")
                assert(cmd.contains(".exe"))
            }
            def process=cmd.execute()
            process.withWriter {writer ->
                writer.write("localhost\n")
                writer.write("C2C\n")
                writer.write("Deploy mechanism\n")
                writer.write("EPFL\n")
                writer.write("Vaud\n")
                writer.write("CH\n")
                writer.write("yes\n")
            }
            process.waitFor()
            def out = new StringBuffer()
            def err = new StringBuffer()
            process.consumeProcessOutput(out,err)

            println(process.exitValue())
            assert(process.exitValue() == 0)
            if(out.size()>0) {
                log.info(out.toString())
            }
            if(err.size()>0) {
                def msg = err.toString()
                if(msg.trim().startsWith("What is your first and last name")) {
                    log.info(msg)
                } else {
                    log.error(err.toString())
                }
            }

            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            def input = new FileInputStream(outFile)
            ks.load(input, passphrase)
            input.close()

            ks.setCertificateEntry("localhost",ks.getCertificate(alias))
        } else {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, passphrase)
        }
        OutputStream out = new FileOutputStream(outFile);
        ks.store(out, passphrase);
        out.close();
        return ks
    }

    static def importCertificate(log, keystoreFile, char[] passphrase, String host, int port) {

        char SEP = File.separatorChar;
        def file;
        if (keystoreFile instanceof File) {
            file = keystoreFile
        } else {
            file = new File(keystoreFile);
        }

        if (file.isFile() == false) {
            File dir = new File(System.getProperty("java.home") + SEP
                    + "lib" + SEP + "security");
            file = new File(dir, "jssecacerts");
            if (file.isFile() == false) {
                file = new File(dir, "cacerts");
            }
        }

        log.info("Loading KeyStore " + file + "...");

        InputStream inputStream = new FileInputStream(file);
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(inputStream, passphrase);
        inputStream.close();

        SSLContext context = SSLContext.getInstance("TLS");
        TrustManagerFactory tmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
        SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
        TrustManager[] tms = new TrustManager[1]
        tms[0] = tm
        context.init(null, tms, null);
        SSLSocketFactory factory = context.getSocketFactory();

        log.info("Opening connection to " + host + ":" + port + "...");
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.setSoTimeout(10000);
        try {
            log.info("Starting SSL handshake...");
            socket.startHandshake();
            socket.close();

            log.info("No errors, certificate is already trusted");
        } catch (SSLException e) {
            log.info("Found certificates that need to be imported");
        }

        X509Certificate[] chain = tm.chain;
        if (chain == null) {
            log.info("Could not obtain server certificate chain");
            return;
        }


        log.info("Server sent " + chain.length + " certificate(s):");

        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        for (int i = 0; i < chain.length; i++) {
            X509Certificate cert = chain[i];
            log.info("Adding certificate to keystore: ");
            log.info(" " + (i + 1) + " Subject " + cert.getSubjectDN());
            log.info("   Issuer  " + cert.getIssuerDN());
            sha1.update(cert.getEncoded());
            log.info("   sha1    " + toHexString(sha1.digest()));
            md5.update(cert.getEncoded());
            log.info("   md5     " + toHexString(md5.digest()));


            String alias = host + "-" + (i + 1);
            ks.setCertificateEntry(alias, cert);
        }


        OutputStream out = new FileOutputStream(file);
        ks.store(out, passphrase);
        out.close();
    }

    private static final char[] HEXDIGITS = "0123456789abcdef".toCharArray();

    private static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int b: bytes) {
            b &= 0xff;
            sb.append(HEXDIGITS[b >> 4]);
            sb.append(HEXDIGITS[b & 15]);
            sb.append(' ');
        }
        return sb.toString();
    }

    private static class SavingTrustManager implements X509TrustManager {

        private final X509TrustManager tm;
        private X509Certificate[] chain;

        SavingTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        public X509Certificate[] getAcceptedIssuers() {
            throw new UnsupportedOperationException();
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
            throw new UnsupportedOperationException();
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
            this.chain = chain;
            tm.checkServerTrusted(chain, authType);
        }
    }
}

