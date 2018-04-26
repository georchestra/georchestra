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

package org.georchestra.extractorapp.ws.extractor.wcs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.georchestra.extractorapp.ws.ExtractorException;
import org.georchestra.extractorapp.ws.extractor.FileUtils;


class GDALCommandLine {
    private static final Log LOG = LogFactory.getLog(GDALCommandLine.class.getPackage().getName());
    private static final String SEP = File.separator;

    static void gdalTransformation(File sourceFile, File file, WcsReaderRequest executedRequest, WcsReaderRequest targetRequest)
            throws IOException {
        LOG.info("using GDAL command line to tranform the coverage");

        File tmpDir = FileUtils.createTempDirectory();
        try {
            File outFile = new File(tmpDir, sourceFile.getName());

            reproject(sourceFile, executedRequest, targetRequest, outFile);
            transformFormat(outFile, executedRequest, targetRequest, file);

        } finally {
            FileUtils.delete(tmpDir);
        }
    }

    private static void transformFormat(File sourceFile, WcsReaderRequest executedRequest, WcsReaderRequest targetRequest, File outFile) throws IOException {
        if(Formats.isGeotiff(executedRequest.format) && Formats.isGeotiff(targetRequest.format)) {
            for (File f : sourceFile.getParentFile().listFiles()) {
                File dest = new File(outFile.getParentFile(), f.getName());
                FileUtils.moveFile(f, dest);
            }
        } else {
            List<String> command = new ArrayList<String>();
            command.add(findTranslateBinary());

            addOutputFormat(targetRequest, command);
            addQuietParam(command);
            
            command.add(sourceFile.getAbsolutePath());
            command.add(outFile.getAbsolutePath());
            
            executeCommand(command);
        }
        
    }

    private static void reproject(File sourceFile, WcsReaderRequest executedRequest, WcsReaderRequest targetRequest, File outFile)
            throws IOException {
        List<String> command = new ArrayList<String>();
        command.add(findWarpBinary());

        addInputProjection(executedRequest, command);

        addGeotiffOutputFormat(command, true);
        addOutputProjection(targetRequest, command);

        addWarpParameters(command);

        addQuietParam(command);
        command.add(sourceFile.getAbsolutePath());
        command.add(outFile.getAbsolutePath());

        executeCommand(command);
    }

    private static void addQuietParam(List<String> command) {
        if (!LOG.isDebugEnabled()) {
            command.add("-q");
        }
    }

    private static void addWarpParameters(List<String> command) {
        command.add("-r");
        command.add("cubic");
    }

    private static void addInputProjection(WcsReaderRequest executedRequest, List<String> command) {
        command.add("-s_srs");
        command.add(executedRequest.getResponseEpsgCode());
    }

    private static void addOutputProjection(WcsReaderRequest targetRequest, List<String> command) {
        command.add("-t_srs");
        command.add(targetRequest.getResponseEpsgCode());
    }

    private static void executeCommand(List<String> command) throws IOException {
        LOG.info("Executing : " + command.toString());
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        final StringBuilder gdalOutput = new StringBuilder();
        readProcessOutput(process, gdalOutput);

        try {
            int exitCode = process.waitFor();
            if (LOG.isDebugEnabled()) {
                LOG.debug("GDAL_OUTPUT:\n" + gdalOutput.toString());
            } else {
                LOG.error("GDAL_OUTPUT:\n" + gdalOutput.toString());
            }

            if (exitCode != 0) {
                throw new ExtractorException(command.toString() + " failed. Output is as follows:\n" + gdalOutput.toString());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void readProcessOutput(final Process process, final StringBuilder gdalOutput) throws IOException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (!line.trim().isEmpty()) {
                            gdalOutput.append("\t");
                            gdalOutput.append(line);
                            gdalOutput.append("\n");
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    IOUtils.closeQuietly(in);
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    private static void addOutputFormat(WcsReaderRequest requiredRequest, List<String> command) {
        if (Formats.isGeotiff(requiredRequest.format)) {
            addGeotiffOutputFormat(command, false);
        } else if (Formats.isJPEG2000(requiredRequest.format)) {
            command.add("-of");
            command.add("JPEG2000");
        } else if (requiredRequest.format.equalsIgnoreCase("ecw")) {
            command.add("-of");
            command.add("ECW");
            command.add("-co");
            command.add("LARGE_OK=NO");
        } else {
            command.add("-of");
            command.add(requiredRequest.format);
        }
    }

    private static void addGeotiffOutputFormat(List<String> command, boolean simple) {
        command.add("-of");
        command.add("GTiff");
        command.add("-co");
        command.add("BIGTIFF=NO");
        if(!simple) {
            command.add("-co");
            command.add("TILED=YES");
        }
    }

    private static String findWarpBinary() {
        return findGdalBinary("gdalwarp");
    }
    private static String findTranslateBinary() {
        return findGdalBinary("gdal_translate");
    }

    private static String findGdalBinary(String command) {
        String gdalHome = System.getProperty("gdal.home");
        if (gdalHome == null) {
            gdalHome = System.getProperty("GDAL_HOME");
        }
        if (gdalHome == null) {
            gdalHome = System.getenv("GDAL_HOME");
        }
        if (gdalHome == null) {
            return command;
        } else {
            return gdalHome + SEP + "bin" + SEP + command;
        }
    }

}
