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
            WcsReaderRequest requiredRequest,
            WcsReaderRequest inputRequest) throws IOException {
        LOG.info("using GDAL command line to tranform the coverage");
        
        File outFile = file;
        if(sourceFile.equals(file)) {
            File tmpDir = FileUtils.createTempDirectory();
            outFile = new File(tmpDir, file.getName());
        }
        
        List<String> command = new ArrayList<String>();
        command.add(findGdalBinary());
        
        addOutputFormat(requiredRequest, command);
        addOutputProjection(requiredRequest, command);
        
        if(!LOG.isDebugEnabled()) {
            command.add("-q");
        }
        command.add(sourceFile.getAbsolutePath());
        command.add(outFile.getAbsolutePath());
        
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
        if(sourceFile.equals(file)) {
            for (File f : outFile.getParentFile().listFiles()) {
                File dest = new File(file.getParentFile(), f.getName());
                FileUtils.moveFile(f, dest);
            }
        }
    }
    private static void addOutputProjection(WcsReaderRequest requiredRequest, List<String> command) {
        command.add("-a_srs");
        command.add(requiredRequest.getResponseEpsgCode());
        
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
    private static String findGdalBinary() {
        String gdalBinary = System.getProperty("gdal.binary");
        if(gdalBinary == null) {
            gdalBinary = System.getenv("GDAL_HOME");
            if(gdalBinary!=null) {
                gdalBinary += SEP+"bin"+SEP+"gdal_translate";
            }
        }
        if(gdalBinary == null) {
            gdalBinary = "gdal_translate";
        }
        return gdalBinary;
    }


}
