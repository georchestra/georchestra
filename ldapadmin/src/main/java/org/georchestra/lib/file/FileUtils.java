/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
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

package org.georchestra.lib.file;

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


/**
 * <p>
 * Common file operations
 * </p>
 * <p>
 * Note : this utility class was copied from extractorapp, the specific functions where removed
 * </p>
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
            throw new IOException("When performing a Zip all files must be within the baseFile: "+file+" not contained by "+baseFile);
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
              in.getChannel().transferTo(0, file.length(), Channels.newChannel(zip));
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
        ZipInputStream zip = null;
        try {
            fileIn = new FileInputStream(archive);
            BufferedInputStream buffered = new BufferedInputStream(fileIn);
            zip = new ZipInputStream(buffered);
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
            if (zip != null) {
                zip.close();
            }
            if (fileIn != null) {
                fileIn.close();
            }
        }
    }

    public static void moveFile(File from, File to) throws FileNotFoundException,
            IOException {
        if (!from.renameTo(to)) {
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

    // TODO: Not sure it handles every special cases,
    // we should use a lib to manage the safe transformation instead
    public static String toSafeFileName(String filename) {
        return filename.replaceAll("\\\\|/|:|\\||<|>|\\*|\"", "_");
    }
}
