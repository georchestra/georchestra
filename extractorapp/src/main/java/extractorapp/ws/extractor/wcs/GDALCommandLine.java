package extractorapp.ws.extractor.wcs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import extractorapp.ws.ExtractorException;
import extractorapp.ws.extractor.FileUtils;

public class GDALCommandLine {
    private static final Log LOG = LogFactory.getLog(GDALCommandLine.class.getPackage().getName());
    private static final String SEP = File.separator;

    static void gdalTransformation(File sourceFile, File file, WcsReaderRequest executedRequest, WcsReaderRequest targetRequest)
            throws IOException {
        LOG.info("using GDAL command line to tranform the coverage");

        File outFile = file;
        if (sourceFile.equals(file)) {
            File tmpDir = FileUtils.createTempDirectory();
            outFile = new File(tmpDir, file.getName());
        }

        transformFormat(sourceFile, executedRequest, targetRequest, outFile);

        if (sourceFile.equals(file)) {
            for (File f : outFile.getParentFile().listFiles()) {
                File dest = new File(file.getParentFile(), f.getName());
                FileUtils.moveFile(f, dest);
            }
        }
    }

    private static void transformFormat(File sourceFile, WcsReaderRequest executedRequest, WcsReaderRequest targetRequest, File outFile)
            throws IOException {
        List<String> command = new ArrayList<String>();
        command.add(findWarpBinary());

        addInputProjection(executedRequest, command);

        addOutputFormat(targetRequest, command);
        addOutputProjection(targetRequest, command);

        addWarpParameters(command);

        if (!LOG.isDebugEnabled()) {
            command.add("-q");
        }
        command.add(sourceFile.getAbsolutePath());
        command.add(outFile.getAbsolutePath());

        executeCommand(command);
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
                throw new ExtractorException("GDAL commandline tranform failed. Output is as follows:\n" + gdalOutput.toString());
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
            command.add("-of");
            command.add("GTiff");
            command.add("-co");
            command.add("TILED=YES");
            command.add("-co");
            command.add("TFW=YES");
            command.add("-co");
            command.add("BIGTIFF=IF_SAFER");
            command.add("-co");
            command.add("BLOCKXSIZE=1024");
            command.add("-co");
            command.add("BLOCKYSIZE=1024");
        } else if (Formats.isJPEG2000(requiredRequest.format)) {
            command.add("-of");
            command.add("JPEG2000");
            command.add("-co");
            command.add("WORLDFILE=ON");
        } else if (requiredRequest.format.equalsIgnoreCase("ecw")) {
            command.add("-of");
            command.add("ECW");
            command.add("-co");
            command.add("LARGE_OK=YES");
        } else {
            command.add("-of");
            command.add(requiredRequest.format);
        }
    }

    private static String findWarpBinary() {
        return findGdalBinary("gdalwarp");
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
