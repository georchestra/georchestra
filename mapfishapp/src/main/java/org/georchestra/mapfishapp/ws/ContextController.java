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

package org.georchestra.mapfishapp.ws;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
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
import org.georchestra.commons.configuration.GeorchestraConfiguration;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public GeorchestraConfiguration georchestraConfiguration;

    private JSONObject getContextInfo(File f) throws Exception {
        JSONObject info = new JSONObject();
        String pathCtx = FilenameUtils.getFullPath(f.getAbsolutePath());
        // title
        String title = FilenameUtils.getBaseName(f.getName());
        // image
        // defaulting to default.png
        String image = "context/image/default.png";

        File imagePath = new File(pathCtx, "images");
        if (! imagePath.isDirectory()) {
            LOG.error("No \"images\" subdirectory found into \"" + pathCtx + "\". Please check your setup, using default image for context \"" + title + "\".");
        } else {
            Collection<File> files = FileUtils.listFiles(imagePath, new String[] { "PNG", "png", "jpeg", "JPEG", "jpg", "JPG" }, false);
            for (File curImgFile : files) {
                if (FilenameUtils.getBaseName(curImgFile.toString()).equalsIgnoreCase(title)) {
                    image = "context/image/" + FilenameUtils.getName(curImgFile.toString());
                    break;
                }
            }
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

        String ctxDir = georchestraConfiguration.getContextDataDir();
        // if null, falling back on the classic contexts directory
        if (ctxDir == null) {
            if (context == null) {
                return null;
            }
            ctxDir = context.getRealPath(File.separator);
            if (new File(ctxDir).exists()) {
                return ctxDir;
            } else {
                return null;
            }
        } else {
            return ctxDir;
        }
    }

    @RequestMapping(value= "/contexts")
    public void getContexts(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/json");

        response.getOutputStream().write(getContexts().toString(4).getBytes());
    }

    public JSONArray getContexts() throws Exception {
        JSONArray ret = new JSONArray();
        String ctxDir = guessContextDirectory();

        if (ctxDir != null) {
            File ctxCtxPath = new File(ctxDir, "contexts");
            if (! ctxCtxPath.isDirectory()) {
                LOG.error("No context sub-directory found in \"" + ctxDir + "\". Returning an empty array of contexts. Please check your setup.");
                return ret;
            }
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
        return ret;
    }


    @RequestMapping(value = "/context/{contextName}.wmc")
    public void getContext(HttpServletRequest request, HttpServletResponse response, @PathVariable String contextName)
            throws Exception {
        String ctxDir = guessContextDirectory();
        response.setContentType("application/vnd.ogc.context+xml");
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
    @RequestMapping(value="/context/image/{contextName}.{imgFmt}")
    public void getContextImage(HttpServletRequest request, HttpServletResponse response,
            @PathVariable String contextName, @PathVariable String imgFmt) throws Exception {
        if (! imgFmt.equalsIgnoreCase("png")
                && ! imgFmt.equalsIgnoreCase("jpg")
                && ! imgFmt.equalsIgnoreCase("jpeg")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String ctxDir = guessContextDirectory();
        if (imgFmt.equalsIgnoreCase("png")) {
            response.setContentType("image/png");
        } else {
            response.setContentType("image/jpeg");
        }
        if (ctxDir != null) {
            try {
             File imgFile = new File(ctxDir, File.separator + "contexts" + File.separator + "images" + File.separator + contextName + "." + imgFmt);
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
