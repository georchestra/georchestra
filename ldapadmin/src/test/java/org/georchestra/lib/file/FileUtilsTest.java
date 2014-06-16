package org.georchestra.lib.file;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileUtilsTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private static String expectedString = "this string will be dumped into\nits own file then zipped.";

    @After
    public void tearDown() {
        folder.delete();
    }

    @Test
    public void testDelete() throws IOException {
        // Test delete on a file
        File testDelete = folder.newFile("testDelete");
        assertTrue(testDelete.exists());
        FileUtils.delete(testDelete);
        assertFalse(testDelete.exists());

        // Same on a directory
        File testDeleteF = folder.newFolder("testDeleteDir");
        File testDeleteF1 = new File(testDeleteF.getAbsolutePath() + File.separator + "yetAnotherFile");
        assertFalse(testDeleteF1.exists());
        testDeleteF1.createNewFile();
        assertTrue(testDeleteF1.exists());
        assertTrue(testDeleteF.exists());
        FileUtils.delete(testDeleteF);
        assertFalse(testDeleteF.exists());
        assertFalse(testDeleteF1.exists());
    }

    @Test
    public void testZipDirBadParameters() throws Exception {
        File zipf = folder.newFile("test.zip");
        File folder1 = folder.newFolder("folder1");
        File folder2 = folder.newFolder("folder2");
        FileOutputStream zipfo = null;
        ZipOutputStream zip = null;

        try {
            zipfo = new FileOutputStream(zipf);
            zip = new ZipOutputStream(zipfo);

            // Actually calls the FileUtils.zipDir() method
            FileUtils.zipDir(zip, folder1, folder2);

        } catch (Throwable e) {
            assertTrue (e instanceof IOException);
        } finally {
            if (zip != null) zip.close();
            if (zipfo != null) zipfo.close();
        }
    }

    private File generateSampleZip() throws Exception {

        File testF = folder.newFolder("testZipDir");
        File testF1 = new File(testF.getAbsolutePath() + File.separator + "yetAnotherFile");
        File subDir = new File(testF.getAbsolutePath() + File.separator + "subdir");
        subDir.mkdir();
        File testF2 = new File(testF.getAbsolutePath() + File.separator + "subdir" + File.separator + "emptyFile");
        testF2.createNewFile();

        // creates a file with some content
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(testF1);
            fo.write(expectedString.getBytes());
        } finally {
            if (fo != null) fo.close();
        }

        File zipF = folder.newFile();
        FileOutputStream zipFo = null;
        ZipOutputStream zip = null;
        try {
            zipFo = new FileOutputStream(zipF);
            zip = new ZipOutputStream(zipFo);

            // Actually tries to zip the folder
            FileUtils.zipDir(zip, testF, testF);

        } finally {
            zip.close();
            zipFo.close();
        }
        return zipF;
    }
    @Test
    public void testZipDir() throws Exception {
        File zipF = generateSampleZip();

        //Checks the generated file
        assertTrue(zipF.length() > 0);
        FileInputStream in = new FileInputStream(zipF);
        ZipInputStream zin = new ZipInputStream(in);
        List<String> actual = new ArrayList<String>();
        try {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                StringBuilder unzipped = new StringBuilder();
                int charRead = 0;
                while ((charRead = zin.read()) != -1) {
                    unzipped.append(Character.toChars(charRead));
                }
                actual.add(unzipped.toString());
            }
        } finally {
            if (zin != null) zin.close();
            if (in != null) in.close();
        }
        assertTrue(actual.contains(expectedString));
        assertTrue(actual.contains(""));
    }

    @Test
    public void testArchiveToZip() throws Exception {
        File f = folder.newFolder();
        File z = folder.newFile("test.zip");

        FileUtils.archiveToZip(f, z);

        assertTrue(z.length() > 0);
        try {
            ZipFile zif = new ZipFile(z);
            zif.close();
        } catch (ZipException ze) {
            // Checking the inner content of the file
            // has already been done in the previous
            // test method.
            fail("Generated zip file does not seem valid: " + ze.getMessage());
        }
    }

    @Test
    public void testListZip() throws Exception {
        File zip = generateSampleZip();

        List<String> list = FileUtils.listZip(zip);

        // should contain the following elements:
        //testZipDir/subdir/emptyFile, testZipDir/yetAnotherFile

        assertTrue(list.contains("testZipDir/subdir/emptyFile"));
        assertTrue(list.contains("testZipDir/yetAnotherFile"));
    }

    @Test
    public void testGetZipEntryAsString() throws Exception {
        File zip = generateSampleZip();

        String str = FileUtils.getZipEntryAsString(zip, "testZipDir/yetAnotherFile");

        // Extra '\n' at the end (added by asTring() method in FileUtils)
        assertTrue(str.startsWith(expectedString));
    }

    @Test
    public void testGetZipEntryAsStringNotExistingZipEntry() throws Exception {
        File zip = generateSampleZip();

        String str = FileUtils.getZipEntryAsString(zip, "testZipDir/notInZipFile");

        // Extra '\n' at the end (added by asTring() method in FileUtils)
        assertTrue(str == null);
    }

    private String getFileContentAsString(File f) throws Exception {
        StringBuilder b = new StringBuilder();
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(f);
            int charRead;
            while ((charRead = fi.read()) != -1) {
                b.append(Character.toChars(charRead));
            }
        } finally {
            if (fi != null) fi.close();
        }
        return b.toString();
    }

    private File createSampleFile(String name) throws Exception {
        File f = folder.newFile(name);
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(f);
            fo.write(expectedString.getBytes());
        } finally {
            if (fo != null) fo.close();
        }
        return f;
    }

    @Test
    public void testMoveFile() throws Exception {
        File f = createSampleFile("sample");
        File t = new File(f.getAbsolutePath() + ".1");

        // This should have no effect
        FileUtils.moveFile(f, f);
        assertTrue(f.exists());

        // Should create a file (by renaming f)
        FileUtils.moveFile(f, t);

        assertTrue(expectedString.equals(getFileContentAsString(t)));
        assertFalse(f.exists());
        assertTrue(t.exists());
        assertTrue(t.length() > 0);
        assertTrue(t.getAbsolutePath().equals(f.getAbsolutePath() + ".1"));
    }

    @Test
    public void testToSafeFileName() {
        String[] testedPaths = new String[] {
                "../../../../../../../etc/passwd",
                "soundslegitAsFilename",
                "C:\\windows\\system32\\kernel32.dll",
                "some<>inconvient***Characters\\\\///"
                };
        String[] expectedOutputPaths = new String[] {
                ".._.._.._.._.._.._.._etc_passwd",
                "soundslegitAsFilename",
                "C__windows_system32_kernel32.dll",
                "some__inconvient___Characters_____"
        };

        for (int i = 0 ; i < testedPaths.length; ++i) {
            assertTrue(FileUtils.toSafeFileName(testedPaths[i]).equals(expectedOutputPaths[i]));
        }
    }

}
