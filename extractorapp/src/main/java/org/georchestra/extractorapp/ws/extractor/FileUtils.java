/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.extractorapp.ws.extractor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.georchestra.extractorapp.ws.ExtractorException;
import org.georchestra.extractorapp.ws.extractor.OGRFeatureWriter.FileFormat;
import org.georchestra.extractorapp.ws.extractor.WfsExtractor.GeomType;
import org.opengis.feature.simple.SimpleFeatureType;


/**
 * Common file operations
 * @author jeichar
 */
public final class FileUtils {

    private FileUtils() {
        // a utility class is not intended to be instantiated
    }

    public static void delete(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                delete(child);
            }
        }
        file.delete();
    }

    public static void zipDir(ZipOutputStream zip,File baseFile, File file) throws IOException {
        if(!file.getPath().startsWith(baseFile.getPath())) {
            throw new ExtractorException("When performing a Zip all files must be within the baseFile: "+file+" not contained by "+baseFile);
        }

        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                zipDir(zip, baseFile, f);
            }
        } else {
            String relativeName = file.getPath().substring(baseFile.getParent().length()+1);
            ZipEntry next = new ZipEntry(relativeName);
            zip.putNextEntry(next);
            FileInputStream in = new FileInputStream(file);
            try {
                long pos = 0;
                pos = in.getChannel().transferTo(pos, file.length(), Channels.newChannel(zip));
                while(pos < file.length())
                    pos += in.getChannel().transferTo(pos, file.length(), Channels.newChannel(zip));
            } finally {
              in.close();
            }
        }
    }

    public static void archiveToZip(File tmpExtractionBundle, File storageFile) throws IOException {
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(storageFile);
            BufferedOutputStream buffered = new BufferedOutputStream(fileOut);
            ZipOutputStream zip = new ZipOutputStream(buffered);
            zipDir(zip, tmpExtractionBundle, tmpExtractionBundle);
            zip.close();
        } finally {
            if (fileOut != null) {
                fileOut.close();
            }
        }
    }

    public static List<String> listZip(File archive) throws IOException {
        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream(archive);
            BufferedInputStream buffered = new BufferedInputStream(fileIn);
            ZipInputStream zip = new ZipInputStream(buffered);
            ArrayList<String> entries = new ArrayList<String>();
            ZipEntry next = zip.getNextEntry();
            while (next != null) {
                entries.add(next.getName());
                next = zip.getNextEntry();
            }
            zip.close();
            return entries;
        } finally {
            if (fileIn != null) {
                fileIn.close();
            }
        }
    }

    public static String getZipEntryAsString(File archive, String name) throws IOException {
        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream(archive);
            BufferedInputStream buffered = new BufferedInputStream(fileIn);
            ZipInputStream zip = new ZipInputStream(buffered);
            ArrayList<String> entries = new ArrayList<String>();
            ZipEntry next = zip.getNextEntry();
            while (next != null) {
                if (name.equals(next.getName())) {
                    return asString(zip);
                }
                entries.add(next.getName());
                next = zip.getNextEntry();
            }
            zip.close();
            return null;
        } finally {
            if (fileIn != null) {
                fileIn.close();
            }
        }
    }

    public static void moveFile(File from, File to) throws FileNotFoundException,
            IOException {
        to.getParentFile().mkdirs();
        if (!from.renameTo(to)) {
            if (from.isDirectory()) {
                to.mkdirs();
                for (File file: from.listFiles()) {
                    moveFile(file, new File(to, file.getName()));
                }
            } else {
                FileInputStream in = new FileInputStream(from);
                FileOutputStream out = new FileOutputStream(to);
                try {
                    in.getChannel().transferTo(0, from.length(), out.getChannel());
                } finally {
                    try {
                        in.close();
                    } finally {
                        out.close();
                    }
                }
            }
        }
    }

    public static String asString(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder buffer = new StringBuilder();
        try {
            String line = in.readLine();
            while(line!=null){
                buffer.append(line);
                buffer.append("\n");
                line=in.readLine();
            }
        } finally {
            in.close();
        }

        return buffer.toString();
    }

    public static File storageFile(String filename) {
        String baseStorageDir = getExtractorStorageDir();
        if(filename==null || filename.length()==0){
            return new File(baseStorageDir);
        } else {
            String safeFilename = toSafeFileName(filename);
            File storageFile = new File(baseStorageDir, safeFilename );
            return storageFile;
        }
    }

    public static String getExtractorStorageDir() {
        String baseStorageDir = System.getProperty("extractor.storage.dir");

        if(baseStorageDir==null){
            baseStorageDir = System.getenv("extractor.storage.dir");
        }
        /* still null ? let's try something else */
        if (baseStorageDir == null){
            baseStorageDir = System.getProperty("java.io.tmpdir")+File.separator+"extractorStorage";
        }
        return baseStorageDir;
    }

    public static String toSafeFileName(String filename) {
        return filename.replaceAll("\\\\|/|:|\\||<|>|\\*|\"", "_");
    }

    public static File createTempDirectory() {
        File tmpDir = new File(getExtractorStorageDir(),"temp");
        tmpDir.mkdirs();
        int tries = 0;
        final int MAX_TRIES = 30;
        while (tries < MAX_TRIES) {
            try {
                File baseDir = File.createTempFile("WcsCoverageReader", null, tmpDir);
                if (!baseDir.delete() || !baseDir.mkdirs()) {
                    tries++;
                    continue;
                } else {
                    return baseDir;
                }
            } catch (IOException ioe) {
                tries++;
                if (tries > MAX_TRIES) {
                    throw new RuntimeException(ioe);
                }
            }
        }
        throw new AssertionError("Unable to make a temporary director in base: "+tmpDir);
    }

	public static String createFileName(final String baseDir, final SimpleFeatureType type, final FileFormat ext){

		String layerName = type.getTypeName();
		Class<?> geomClass = type.getGeometryDescriptor().getType().getBinding();

        GeomType geomType = WfsExtractor.GeomType.lookup (geomClass);

        String newName = FileUtils.toSafeFileName(layerName + "_" + geomType + "." + ext);

        File file = new File(newName);
        for (int i = 1; file.exists(); i++) {
            newName = layerName + "_" + geomType + i;
            newName = FileUtils.toSafeFileName(newName + "." + ext);
            file = new File(baseDir, newName + "." + ext);
        }

        return newName;
	}

    /**
     * Get the file extension.
     * @param file the file
     */
    public static String extension(File file) {
        return file.getName().substring(file.getName().lastIndexOf('.'));
    }
}
