/*
 * Copyright (C) 2020, 2021 by the geOrchestra PSC
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
package org.georchestra.datafeeder.service.publish.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLResult;

import com.google.common.io.CharStreams;

import lombok.NonNull;

public class TemplateMapper {

    public Supplier<String> apply(@NonNull MetadataRecordProperties mdProps) {

        final String templateRecord = loadTemplateRecord();
        final String xslTransform = loadTransform();
        final StringWriter target = new StringWriter();
        requireNonNull(templateRecord, "templateRecord is null");
        requireNonNull(xslTransform, "xslTransform is null");
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Source xslt = new StreamSource(new StringReader(xslTransform));
            Transformer transformer = factory.newTransformer(xslt);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            // the document used as the single XSLT parameter "props"
            final JAXBContext context = JAXBContext.newInstance(mdProps.getClass());
            Source paramDocumentSource = new JAXBSource(context, mdProps);
            transformer.setParameter("props", paramDocumentSource);

            // the record template to apply the xslt to
            Source templateRecordSource = new StreamSource(new StringReader(templateRecord));

            // xslt target
            Result result = new XMLResult(target, OutputFormat.createPrettyPrint());

            transformer.transform(templateRecordSource, result);
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException("Error creating transformer", e);
        } catch (JAXBException e) {
            throw new IllegalStateException("Error creating transformation source", e);
        } catch (TransformerException e) {
            throw new IllegalStateException("Error applying transformation", e);
        }

        return target::toString;
    }

    protected String loadTransform() {
        return loadResource("/default_iso_2005_gmd.xsl");
    }

    protected String loadTemplateRecord() {
        return loadResource("/md_record_template.xml");
    }

    protected String loadResource(@NonNull String resource) {
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            requireNonNull(in, () -> "Resource not found: " + resource);
            return CharStreams.toString(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
