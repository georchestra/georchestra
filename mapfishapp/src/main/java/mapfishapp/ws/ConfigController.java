/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mapfishapp.ws;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;

/**
 *
 * @author Davis Mendoza <dmendoza@geo.gob.bo>
 */
@Controller
public class ConfigController {

    protected static final Log LOG = LogFactory.getLog(ConfigController.class.getPackage().getName());
    
    Properties properties = null;
     
    public ConfigController() {
        String dirConfig = System.getProperty("dirConfigGeorchestra");
        String dirConfigMapfishapp = null;
        String fileProperties = "config.properties";

        if (dirConfig == null) {
            dirConfig = System.getProperty("user.home") + "/.georchestra";
        } 
        dirConfigMapfishapp = dirConfig + "/mapfishapp";
        fileProperties = dirConfigMapfishapp +"/"+ fileProperties;
        
        File fProperties = new File(fileProperties);
       
        InputStream stream;
        try {
            stream = new FileInputStream(fProperties);
            try {
                properties = new Properties();
                if (stream != null) {
                    properties.load(stream);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    public String getPropertyValue(String key){
        String value = null;
        if(key != null){
            value = properties.getProperty(key);
        } else {
            value = "???"+key+"???";
        }
        return value;
    }
}
