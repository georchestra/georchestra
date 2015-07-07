package org.georchestra.mapfishapp.ws;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.ServletContextAware;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


/**
 * This controller allows to retrieve the list of the WMC contexts
 * hosted in the mapfishapp datadir.
 *
 * It requires the GeorchestraConfiguration bean to be correctly instantiated.
 *
 * @author pmauduit
 *
 */
@Controller
public class ContextController implements ServletContextAware {


    private static final Log LOG = LogFactory.getLog(ContextController.class.getPackage().getName());

    private ServletContext context;

    private JSONObject getContextInfo(File f) throws Exception {
        JSONObject info = new JSONObject();
        String pathCtx = FilenameUtils.getFullPath(f.getAbsolutePath());
        // title
        String title = FilenameUtils.getBaseName(f.getName());
        // image
        // defaulting to default.png
        String image = "context/image/default.png";
        String imagePath = "images" + File.separator + title + ".png";
        File imageF = new File(pathCtx, imagePath);
        if (imageF.exists()) {
            image = "context/image/"+ title +".png";
        }

        // filename
        String wmcUrl = "context/" + f.getName();

        // description
        JSONObject xmlInfos = getXmlInfos(f);

        info.put("label", xmlInfos.get("label").equals("unset") ? title : xmlInfos.get("label"));
        info.put("thumbnail", image);
        info.put("wmc", wmcUrl);
        info.put("tip", xmlInfos.get("tip").equals("unset") ? title : xmlInfos.get("tip"));
        info.put("keywords", xmlInfos.getJSONArray("keywords").put(title));

        return info;
    }

    private JSONObject getXmlInfos(File f) throws Exception {
        JSONObject xmlInfos = new JSONObject();
        String label = "unset";
        String tip = "unset";
        JSONArray keywords = new JSONArray();

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(f);
        XPath xpath = XPathFactory.newInstance().newXPath();

        // Parsing label
        XPathExpression xpTitle = xpath.compile("//ViewContext/General/Title/text()");
        Object oTitle = xpTitle.evaluate(doc, XPathConstants.STRING);

        // Parsing tip (abstract)
        XPathExpression xpAbstract = xpath.compile("//ViewContext/General/Abstract/text()");
        Object oAbstract = xpAbstract.evaluate(doc, XPathConstants.STRING);

        // Parsing keywords
        XPathExpression xpKeyword = xpath.compile("//ViewContext/General/KeywordList/Keyword");
        Object oKeyword = xpKeyword.evaluate(doc, XPathConstants.NODESET);

        if (!StringUtils.isEmpty(oTitle.toString())) {
            label = oTitle.toString();
        }
        if (!StringUtils.isEmpty(oAbstract.toString())) {
            tip = oAbstract.toString();
        }
        if (oKeyword instanceof NodeList) {
            NodeList nl = (NodeList) oKeyword;
            for (int i = 0; i < nl.getLength(); ++i) {
                keywords.put(nl.item(i).getTextContent());
            }
        }

        xmlInfos.put("label", label);
        xmlInfos.put("tip", tip);
        xmlInfos.put("keywords", keywords);

        return xmlInfos;
    }

    private String guessContextDirectory() {

        String ctxDir = null;

        if (context == null) {
            return null;
        }
        ctxDir = context.getRealPath(File.separator);
        if (new File(ctxDir).exists()) {
            return ctxDir;
        } else {
            return null;
        }
    }

    @RequestMapping(value= "/contexts")
    public void getContexts(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/javascript; charset=UTF-8");
        JSONArray ret = new JSONArray();
        String ctxDir = guessContextDirectory();

        if (ctxDir != null) {
            Iterator<File> wmcs = FileUtils.iterateFiles(new File(ctxDir, "contexts"), new String[] {"wmc"}, false);
            while (wmcs.hasNext()) {
                File f = wmcs.next();
                try {
                    ret.put(getContextInfo(f));
                } catch (Exception e) {
                    LOG.error("Unable to parse context file \"" + f.getAbsolutePath() + "\". Skipping it.");
                }
            }
        }
        response.getOutputStream().write(ret.toString(4).getBytes());
    }

    @RequestMapping(value = "/context/{contextName}.wmc")
    public void getContext(HttpServletRequest request, HttpServletResponse response, @PathVariable String contextName)
            throws Exception {
        String ctxDir = guessContextDirectory();
        response.setContentType("application/xml; charset=UTF-8");
        if (ctxDir != null) {
            try {
                byte[] ret = FileUtils.readFileToByteArray(new File(ctxDir, File.separator + "contexts"
                        + File.separator + contextName + ".wmc"));
                response.getOutputStream().write(ret);
            } catch (IOException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
    }
    @RequestMapping(value="/context/image/{contextName}.png")
    public void getContextImage(HttpServletRequest request, HttpServletResponse response, @PathVariable String contextName) throws Exception {
        String ctxDir = guessContextDirectory();
        response.setContentType("image/png");
        if (ctxDir != null) {
            try {
             File imgFile = new File(ctxDir, File.separator + "contexts" + File.separator + "images" + File.separator + contextName + ".png");
             byte[] ret = FileUtils.readFileToByteArray(imgFile);
             response.getOutputStream().write(ret);
            } catch (IOException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
 }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.context = servletContext;
    }

}
