package extractorapp.ws.extractor.wcs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import extractorapp.ws.extractor.FileUtils;

public class GDALCommandLine {
    private static final Log LOG = LogFactory.getLog(GDALCommandLine.class.getPackage().getName());
    private static final String SEP = File.separator;
    static void gdalTransformation(File sourceFile, File file,
            WcsReaderRequest executedRequest,
            WcsReaderRequest targetRequest) throws IOException {
        LOG.info("using GDAL command line to tranform the coverage");
        
        File tmpDir = FileUtils.createTempDirectory();
        File outFile = new File(tmpDir, file.getName());
        
        reproject(sourceFile, executedRequest, targetRequest, outFile);
        transformFormat(outFile, targetRequest, file);
        LOG.info("Finished GDAL command line transform");
    }
    private static void reproject(File sourceFile, WcsReaderRequest executedRequest, WcsReaderRequest targetRequest, File outFile) throws IOException {
        List<String> command = new ArrayList<String>();
        command.add(findTranformBinary());
        
        command.add("-s_srs");
        command.add(executedRequest.getResponseEpsgCode());
        
        command.add("-t_srs");
        command.add(targetRequest.getResponseEpsgCode());
        
        command.add(sourceFile.getAbsolutePath());
        command.add(outFile.getAbsolutePath());
        
        executeCommand(command);
    }
    private static void transformFormat(File sourceFile, WcsReaderRequest requiredRequest, File outFile) throws IOException {
        List<String> command = new ArrayList<String>();
        command.add(findTranslateBinary());
        
        addOutputFormat(requiredRequest, command);
        
        if(!LOG.isDebugEnabled()) {
            command.add("-q");
        }
        command.add(sourceFile.getAbsolutePath());
        command.add(outFile.getAbsolutePath());
        
        executeCommand(command);
    }
    private static void executeCommand(List<String> command) throws IOException {
        LOG.info("Executing : "+command.toString());
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        try {
            String line;
            while((line = in.readLine()) != null) {
                if(LOG.isDebugEnabled()) {
                    LOG.debug("<GDAL_TRANSFORM> "+line);
                } else {
                    LOG.error("<GDAL_TRANSFORM> "+line);
                }
            }
        } finally {
            in.close();
        }
    }
    private static void addOutputFormat(WcsReaderRequest requiredRequest, List<String> command) {
        
        if(Formats.isGeotiff(requiredRequest.format)) {
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
        } else if(Formats.isJPEG2000(requiredRequest.format)) {
            command.add("-of");
            command.add("JPEG2000");
            command.add("-co");
            command.add("WORLDFILE=ON");
        } else if(requiredRequest.format.equalsIgnoreCase("ecw")) {
            command.add("-of");
            command.add("ECW");
            command.add("-co");
            command.add("LARGE_OK=YES");
        } else {
            command.add("-of");
            command.add(requiredRequest.format);
        }
    }
    private static String findTranslateBinary() {
        return findGdalBinary("gdal_translate");
    }
    private static String findTranformBinary() {
        return findGdalBinary("gdaltransform");
    }
    private static String findGdalBinary(String command) {
        String gdalHome = System.getProperty("gdal.home");
        if(gdalHome == null) {
            gdalHome = System.getProperty("GDAL_HOME");
        }
        if(gdalHome == null) {
            gdalHome = System.getenv("GDAL_HOME");
        }
        if(gdalHome == null) {
            return command;
        }  else {
            return gdalHome + SEP+"bin"+SEP+command;
        }
    }


}
