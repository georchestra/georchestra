/*
 * Copyright (C) 2021 by the geOrchestra PSC
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
package org.georchestra.datafeeder.autoconf;

import static java.lang.String.format;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.georchestra.datafeeder.config.DataFeederConfigurationProperties;
import org.georchestra.datafeeder.config.DataFeederConfigurationProperties.EmailConfig;
import org.georchestra.datafeeder.email.DatafeederEmailFactory;
import org.georchestra.datafeeder.model.BoundingBoxMetadata;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.Envelope;
import org.georchestra.datafeeder.model.PublishSettings;
import org.georchestra.datafeeder.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * A message template consists of a number of one-line header parts, followed by
 * the full message body.
 * <p>
 * Variables on a message template are specified using
 * <code>${variable-name}</code> notation, like in the following example:
 *
 * <pre>
 * <code>
 * to: ${user.email}
 * cc: ${administratorEmail}
 * bcc:
 * sender: ${administratorEmail}
 * from: Georchestra Importer Application
 * subject:
 * body:
 *
 * Dear ${user.name},
 * ....
 * </code>
 * </pre>
 *
 * The following variables are resolved against the job's user, dataset, or
 * publishing attributes:
 * <ul>
 * <li>${user.name}:
 * <li>${user.lastName}:
 * <li>${user.email}:
 * <li>${job.id}:
 * <li>${job.createdAt}:
 * <li>${job.error}:
 * <li>${job.analizeStatus}:
 * <li>${job.publishStatus}:
 * <li>${dataset.name}:
 * <li>${dataset.featureCount}:
 * <li>${dataset.encoding}:
 * <li>${dataset.nativeBounds}:
 * <li>${publish.tableName}:
 * <li>${publish.layerName}:
 * <li>${publish.workspace}:
 * <li>${publish.srs}:
 * <li>${publish.encoding}:
 * <li>${metadata.id}:
 * <li>${metadata.title}:
 * <li>${metadata.abstract}:
 * <li>${metadata.creationDate}:
 * <li>${metadata.lineage}:
 * <li>${metadata.latLonBoundingBox}:
 * <li>${metadata.keywords}:
 * <li>${metadata.scale}:
 * </ul>
 * <p>
 * Additionally, any other <code>${property}</code> will be resolved against the
 * application context (for example, any property specified in
 * {@code default.properties} or {@code datafeeder.properties}).
 */
@Slf4j(topic = "org.georchestra.datafeeder.email")
public class GeorchestraEmailFactory implements DatafeederEmailFactory {

    private static final String ACK_TEMPLATE = "datafeeder.email.ackTemplate";
    private static final String ANALYSIS_FAILED_TEMPLATE = "datafeeder.email.analysisFailedTemplate";
    private static final String PUBLISH_SUCCESS_TEMPLATE = "datafeeder.email.publishSuccessTemplate";
    private static final String PUBLISH_FAILED_TEMPLATE = "datafeeder.email.publishFailedTemplate";

    private @Autowired DataFeederConfigurationProperties config;
    private @Autowired Environment environment;

    final @PostConstruct void validateConfig() {
        EmailConfig emailConfig = config.getEmail();
        validateTemplate(emailConfig.getAckTemplate(), ACK_TEMPLATE);
        validateTemplate(emailConfig.getAnalysisFailedTemplate(), ANALYSIS_FAILED_TEMPLATE);
        validateTemplate(emailConfig.getPublishFailedTemplate(), PUBLISH_FAILED_TEMPLATE);
        validateTemplate(emailConfig.getPublishSuccessTemplate(), PUBLISH_SUCCESS_TEMPLATE);
    }

    private void validateTemplate(URI templateURI, String templatePropertyName) {
        Objects.requireNonNull(templateURI, () -> format("Email template %s is not provided", templatePropertyName));

        String template;
        try {
            template = config.loadResourceAsString(templateURI, templatePropertyName);
        } catch (RuntimeException e) {
            throw new IllegalStateException(format("Error loading template at %s for property %s: %s", templateURI,
                    templatePropertyName, e.getMessage()), e);
        }

        Set<String> variableNames = extractVariableNames(template);
        List<String> contextProperties = variableNames.stream().map(this::variableToPropertyName)
                .filter(property -> !DEFAULT_PROPERTY_MAPPINGS.containsKey(property)).collect(Collectors.toList());

        List<String> unresolvableProperties = new ArrayList<>();
        for (String prop : contextProperties) {
            String contextPropertyValue = environment.getProperty(prop);
            if (null == contextPropertyValue) {
                unresolvableProperties.add(contextPropertyValue);
            }
        }

        if (!unresolvableProperties.isEmpty()) {
            String message = format("Template %s at %s contains the following unresolvable properties: %s",
                    templatePropertyName, templateURI,
                    unresolvableProperties.stream().collect(Collectors.joining(",")));
            throw new IllegalStateException(message);
        }
    }

    @Override
    public Optional<MailMessage> createAckMessage(DataUploadJob job, UserInfo user) {
        URI templateResource = config.getEmail().getAckTemplate();
        MessageData data = new MessageData(job, user, null);
        return createMessage(templateResource, ACK_TEMPLATE, data);
    }

    @Override
    public Optional<MailMessage> createPublishFinishedMessage(DataUploadJob job, UserInfo user) {
        URI templateResource = config.getEmail().getPublishSuccessTemplate();
        MessageData data = new MessageData(job, user, null);
        return createMessage(templateResource, PUBLISH_SUCCESS_TEMPLATE, data);
    }

    @Override
    public Optional<MailMessage> createAnalysisFailureMessage(DataUploadJob job, UserInfo user, Exception cause) {
        URI templateResource = config.getEmail().getAnalysisFailedTemplate();
        MessageData data = new MessageData(job, user, null);
        return createMessage(templateResource, ANALYSIS_FAILED_TEMPLATE, data);
    }

    @Override
    public Optional<MailMessage> createPublishFailureMessage(DataUploadJob job, UserInfo user, Exception cause) {
        URI templateResource = config.getEmail().getPublishFailedTemplate();
        MessageData data = new MessageData(job, user, null);
        return createMessage(templateResource, PUBLISH_FAILED_TEMPLATE, data);
    }

    private Optional<MailMessage> createMessage(URI templateURI, String templatePropertyName, MessageData data) {
        Optional<String> template = loadTemplate(templateURI, templatePropertyName);
        if (!template.isPresent()) {
            return Optional.empty();
        }
        final String fullMessage = resolveTemplateVariables(template.get(), data);
        final SimpleMailMessage message = buildMessage(data, fullMessage);
        if (!StringUtils.hasText(message.getFrom())) {
            log.warn("Can't sent email from template %s, 'sender:' resolved to null. Full message:\n%s", templateURI,
                    fullMessage);
            return Optional.empty();
        }
        if (null == message.getTo() || message.getTo().length == 0) {
            log.warn("Can't sent email from template %s, 'to:' resolved to null. Full message:\n%s", templateURI,
                    fullMessage);
            return Optional.empty();
        }

        return Optional.of(message);
    }

    /**
     * Builds up a {@link MailMessage} out of a full message string like the
     * following, where all variable substiturions have already been resolved:
     *
     * <pre>
     * <code>
     * to: user1@test.com,user2@test.com
     * cc: admin1@test.com,admin2@test.com
     * bcc:
     * sender: admin@test.com
     * from: Georchestra Importer Application
     * subject: fully resolved subject line
     * body:
     *
     * Dear John Doe,
     * ....
     * </code>
     * </pre>
     */
    private SimpleMailMessage buildMessage(MessageData data, String messageBody) {
        final List<String> lines = new ArrayList<>(Splitter.onPattern("\\r?\\n").splitToList(messageBody));
        final String[] to = split(parseLineAndRemove("to:", lines));
        final String[] cc = split(parseLineAndRemove("cc:", lines));
        final String[] bcc = split(parseLineAndRemove("bcc:", lines));
        final String sender = parseLineAndRemove("sender:", lines);
        final String from = parseLineAndRemove("from:", lines);
        final String subject = parseLineAndRemove("subject:", lines);
        String body;
        {
            String bodyFirstLine = parseLineAndRemove("body:", lines);
            body = lines.stream().collect(Collectors.joining("\n"));
            if (StringUtils.hasText(bodyFirstLine)) {
                body = bodyFirstLine + "\n" + body;
            }
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setCc(cc);
        message.setBcc(bcc);
        message.setFrom(sender);
        message.setSubject(subject);
        message.setText(body);
        return message;
    }

    private String[] split(String line) {
        return line == null ? new String[0]
                : Splitter.on(",").omitEmptyStrings().trimResults().splitToStream(line).toArray(String[]::new);
    }

    private String parseLineAndRemove(String prefix, List<String> lines) {
        String match = lines.stream().filter(line -> line.startsWith(prefix)).findFirst().orElse(null);
        if (match != null) {
            lines.remove(match);
            return match.replaceAll(prefix, "");
        }
        return null;
    }

    /**
     * Resolves template variables from a message template like the following:
     *
     * <pre>
     * <code>
     * to: ${user.email}
     * cc: ${administratorEmail}
     * bcc:
     * sender: ${administratorEmail}
     * from: Georchestra Importer Application
     * subject:
     * body:
     *
     * Dear ${user.name},
     * ....
     * </code>
     * </pre>
     *
     * The following variables are resolved against the job's user, dataset, or
     * publishing attributes:
     * <ul>
     * <li>{user.name}:
     * <li>{user.lastName}:
     * <li>{user.email}:
     * <li>{job.id}:
     * <li>{job.createdAt}:
     * <li>{job.error}:
     * <li>{job.analizeStatus}:
     * <li>{job.publishStatus}:
     * <li>{dataset.name}:
     * <li>{dataset.featureCount}:
     * <li>{dataset.encoding}:
     * <li>{dataset.nativeBounds}:
     * <li>{publish.tableName}:
     * <li>{publish.layerName}:
     * <li>{publish.workspace}:
     * <li>{publish.srs}:
     * <li>{publish.encoding}:
     * <li>{metadata.id}:
     * <li>{metadata.title}:
     * <li>{metadata.abstract}:
     * <li>{metadata.creationDate}:
     * <li>{metadata.lineage}:
     * <li>{metadata.latLonBoundingBox}:
     * <li>{metadata.keywords}:
     * <li>{metadata.scale}:
     * </ul>
     * <p>
     * Additionally, any other <code>${property}</code> will be resolved against the
     * application context (for example, any property specified in
     * {@code default.properties} or {@code datafeeder.properties}) and converted to
     * string.
     */
    private String resolveTemplateVariables(final String messageTemplate, final MessageData data) {
        Set<String> variableNames = extractVariableNames(messageTemplate);
        String resolvedMessage = messageTemplate;
        for (String varName : variableNames) {
            String propertyName = variableToPropertyName(varName);
            String value = resolveProperty(propertyName, data);
            String propRegEx = String.format("\\$\\{%s\\}", propertyName);
            resolvedMessage = resolvedMessage.replaceAll(propRegEx, value);
        }
        return resolvedMessage;
    }

    private String resolveProperty(final String propertyName, MessageData data) {
        VariableResolver variableResolver = DEFAULT_PROPERTY_MAPPINGS.get(propertyName);
        if (variableResolver != null) {
            return variableResolver.apply(data);
        }
        String value = environment.getProperty(propertyName);
        return str(value);
    }

    private String variableToPropertyName(final String varName) {
        Assert.isTrue(varName.startsWith("${"),
                () -> format("Invalid variable name, expected ${varName}, got %s", varName));
        Assert.isTrue(varName.endsWith("}"),
                () -> format("Invalid variable name, expected ${varName}, got %s", varName));

        String propertyName = varName.substring("${".length());
        propertyName = propertyName.substring(0, propertyName.length() - 1);
        return propertyName;
    }

    static Set<String> extractVariableNames(String messageTemplate) {
        Set<String> varNames = new TreeSet<>();
        Matcher matcher = Pattern.compile("\\$(\\{(\\w+\\.)*\\w+\\})").matcher(messageTemplate);
        while (matcher.find()) {
            String varName = matcher.group(0);
            varNames.add(varName);
        }
        return varNames;
    }

    private static Map<String, VariableResolver> DEFAULT_PROPERTY_MAPPINGS = ImmutableMap
            .<String, VariableResolver>builder()//
            .put("user.name", d -> str(d.getUser().getFirstName()))//
            .put("user.lastName", d -> str(d.getUser().getLastName()))//
            .put("user.email", d -> str(d.getUser().getEmail()))//
            .put("job.id", d -> str(d.getJob().getJobId()))//
            .put("job.createdAt", d -> str(d.getJob().getCreatedDate()))//
            .put("job.error", d -> str(d.getJob().getError()))//
            .put("job.analizeStatus", d -> str(d.getJob().getAnalyzeStatus()))//
            .put("job.publishStatus", d -> str(d.getJob().getPublishStatus()))//
            .put("dataset.name", d -> str(dataset(d).map(DatasetUploadState::getName)))//
            .put("dataset.featureCount", d -> str(dataset(d).map(DatasetUploadState::getFeatureCount)))//
            .put("dataset.encoding", d -> str(dataset(d).map(DatasetUploadState::getEncoding)))//
            .put("dataset.nativeBounds", d -> str(dataset(d).map(DatasetUploadState::getNativeBounds).orElse(null)))//
            .put("publish.tableName", d -> str(publishing(d).map(PublishSettings::getImportedName)))//
            .put("publish.layerName", d -> str(publishing(d).map(PublishSettings::getPublishedName)))//
            .put("publish.workspace", d -> str(publishing(d).map(PublishSettings::getPublishedWorkspace)))//
            .put("publish.srs", d -> str(publishing(d).map(PublishSettings::getSrs)))//
            .put("publish.encoding", d -> str(publishing(d).map(PublishSettings::getEncoding)))//
            .put("metadata.id", d -> str(publishing(d).map(PublishSettings::getMetadataRecordId)))//
            .put("metadata.title", d -> str(publishing(d).map(PublishSettings::getTitle)))//
            .put("metadata.abstract", d -> str(publishing(d).map(PublishSettings::getAbstract)))//
            .put("metadata.creationDate", d -> str(publishing(d).map(PublishSettings::getDatasetCreationDate)))//
            .put("metadata.lineage", d -> str(publishing(d).map(PublishSettings::getDatasetCreationProcessDescription)))//
            .put("metadata.latLonBoundingBox",
                    d -> latLonBBox(publishing(d).map(PublishSettings::getGeographicBoundingBox)))//
            .put("metadata.keywords", d -> keywords(publishing(d).map(PublishSettings::getKeywords)))//
            .put("metadata.scale", d -> str(publishing(d).map(PublishSettings::getScale)))//
            .build();

    private static Optional<PublishSettings> publishing(MessageData d) {
        return dataset(d).map(DatasetUploadState::getPublishing);
    }

    private static Optional<DatasetUploadState> dataset(MessageData d) {
        DataUploadJob job = d.getJob();
        // REVISIT: we'll be returning the first published dataset if at least one has
        // been published, or the first uploaded one at all otherwise.
        // this could only be an issue if the user uploaded several shapefiles in a zip
        // file, which btw is out of scope for the current app, so although we do
        // support uploading and publishing multiple shapefiles, the UI doesn't. but
        // this would need to be revisited in the event it does.
        List<DatasetUploadState> publishableDatasets = job.getPublishableDatasets();
        if (!publishableDatasets.isEmpty()) {
            return Optional.of(publishableDatasets.get(0));
        }
        return job.firstDataset();
    }

    private static String str(Optional<?> o) {
        return str(o.orElse(null));
    }

    private static String str(Object o) {
        if (o instanceof Optional) {
            o = ((Optional<?>) o).orElse(null);
        }
        return o == null ? "<not yet computed or not provided>" : String.valueOf(o);
    }

    private static String keywords(Optional<List<String>> keywords) {
        if (!keywords.isPresent() || keywords.get().isEmpty())
            return "<no keyworkds specified>";

        return new HashSet<>(keywords.get()).stream().collect(Collectors.joining(","));
    }

    private static String latLonBBox(Optional<Envelope> bboxOpt) {
        return bboxOpt.map(bbox -> format(
                "west bound longitude=%f, south bound latitude=%f, east bound longitude=%f, north bound latitude=%f",
                bbox.getMinx(), bbox.getMiny(), bbox.getMaxx(), bbox.getMaxy()))
                .orElse("<geographic bounding box not set>");
    }

    private static String str(BoundingBoxMetadata bbox) {
        if (bbox == null)
            return "<bounding box not computed>";
        return format("Bounds[minx=%f, miny=%f, maxx=%f, maxy=%f, srs=%s]", bbox.getMinx(), bbox.getMiny(),
                bbox.getMaxx(), bbox.getMaxy(), bbox.getCrs().getSrs());
    }

    private static @FunctionalInterface interface VariableResolver extends Function<MessageData, String> {

    }

    private Optional<String> loadTemplate(URI templateURI, String configPropName) {
        if (templateURI == null)
            return Optional.empty();
        return Optional.of(config.loadResourceAsString(templateURI, configPropName));
    }

    private static @Value class MessageData {
        DataUploadJob job;
        UserInfo user;
        Exception error;
    }
}
