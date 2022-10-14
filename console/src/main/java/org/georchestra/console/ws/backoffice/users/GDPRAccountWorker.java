package org.georchestra.console.ws.backoffice.users;

import static java.util.concurrent.CompletableFuture.runAsync;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.annotation.Nullable;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.io.FileUtils;
import org.georchestra.console.ds.AccountGDPRDao;
import org.georchestra.console.ds.AccountGDPRDao.DeletedRecords;
import org.georchestra.console.ds.AccountGDPRDao.ExtractorRecord;
import org.georchestra.console.ds.AccountGDPRDao.GeodocRecord;
import org.georchestra.console.ds.AccountGDPRDao.MetadataRecord;
import org.georchestra.console.ds.AccountGDPRDao.OgcStatisticsRecord;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountDao;
import org.locationtech.jts.io.WKTWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.ldap.NameNotFoundException;
import org.zeroturnaround.zip.ZipUtil;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * GDPR (EU <a href="https://eugdpr.org/">General Data Protection
 * Regulation</a>) compliance data gatherer and mangler.
 * <p>
 * For a given account, all it's GDPR sensitive data is gathered as a zip file
 * bundle and returned as a {@link Resource}.
 *
 */
@Slf4j
public class GDPRAccountWorker {

    private static final DateTimeFormatter FILENAME_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter CONTENT_DATE_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

    private @Autowired @Setter(AccessLevel.PACKAGE) AccountGDPRDao accountGDPRDao;
    private @Autowired @Setter(AccessLevel.PACKAGE) UserInfoExporter userInfoExporter;
    private @Autowired @Setter(AccessLevel.PACKAGE) AccountDao accountDao;

    public static @Value @Builder class DeletedAccountSummary {
        private String accountId;
        private int metadataRecords;
        private int extractorRecords;
        private int geodocsRecords;
        private int ogcStatsRecords;
    }

    public DeletedAccountSummary deleteAccountRecords(@NonNull Account account) throws DataServiceException {
        log.info("GDPR: deleting or obfuscating all activity records related to the deleted account {}",
                account.getUid());

        DeletedRecords recs = accountGDPRDao.deleteAccountRecords(account);
        DeletedAccountSummary summary = DeletedAccountSummary.builder()//
                .accountId(recs.getAccountId())//
                .metadataRecords(recs.getMetadataRecords())//
                .extractorRecords(recs.getExtractorRecords())//
                .geodocsRecords(recs.getGeodocsRecords())//
                .ogcStatsRecords(recs.getOgcStatsRecords())//
                .build();
        log.info("GDPR: deleted info summary: {}", summary);
        return summary;
    }

    /**
     * Creates a ZIP file resource containing all downloadable data for the given
     * user.
     * <p>
     * Once the returned resource has been dealt with, {@link #dispose(Resource)}
     * shall be called in order to clean up any temporary content it may be holding
     * up.
     *
     * @throws DataServiceException
     * @throws NameNotFoundException
     * @throws IOException
     */
    public Resource generateUserData(@NonNull String userId)
            throws NameNotFoundException, DataServiceException, IOException {
        Account account = accountDao.findByUID(userId);
        return generateUserData(account);
    }

    @VisibleForTesting
    Resource generateUserData(@NonNull Account account) throws IOException {
        try {
            Path zipFile = buildCompressedUserDataResource(account);
            return new FileSystemResource(zipFile.toFile());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            Throwables.propagateIfPossible(e, IOException.class);
            throw new IOException(e);
        }
    }

    public void dispose(@NonNull Resource accountData) {
        try {
            File file = accountData.getFile();
            if (!file.exists()) {
                return;
            }
            try {
                ZipFile zipFile = new ZipFile(file);
                zipFile.close();
            } catch (ZipException notAZipFile) {
                throw new IllegalArgumentException("The provided resource is not a ZIP file");
            }
            log.debug("Deleting user data bundle {}", file.getAbsolutePath());
            file.delete();
        } catch (FileNotFoundException notAFileSystemResource) {
            // ignore
        } catch (IOException e) {
            log.warn("Unable to dereference resource to delete", e);
        }
    }

    public @VisibleForTesting static @Value class UserDataBundle {
        private Path folder;
        private Path metadataDirectory;
        private Path geodocsDirectory;
        private Path extractorCsvFile;
        private Path ogcstatsCsvFile;
    }

    private Path buildCompressedUserDataResource(@NonNull Account account) throws Exception {

        final Path bundleFolder = Files.createTempDirectory(account.getUid());
        try {
            final UserDataBundle uncompressedBundle = buildUserDataBundle(account, bundleFolder);
            final Path compressedBundle = GDPRAccountWorker.createZipFile(uncompressedBundle.getFolder());

            return compressedBundle;
        } finally {
            try {
                FileUtils.deleteDirectory(bundleFolder.toFile());
                log.info("Directory {} deleted", bundleFolder.toAbsolutePath());
            } catch (IOException ioe) {
                log.warn("Error deleting directory {}", bundleFolder.toAbsolutePath(), ioe);
            }
        }
    }

    /**
     * <pre>
     * {@code
     * .
     * ├── account_info.ldif -> Text file in LDAP's LDIF format with the available user details
     * ├── data_extractions_log.csv -> CSV file with details of data extractions performed by the user
     * ├── ogc_request_log.csv -> CSV file with details of OGC service requests performed by the user
     * ├── metadata -> Directory of metadata records contributed by the user, one file per record.
     * │    ├── YYYY-MM-DD_<MD_Schema>_<recordid>.xml (e.g. 2019-07-01_ISO19139_1000.xml)
     * │    ├── YYYY-MM-DD_<MD_Schema>_<recordid>.xml (e.g. 2019-07-01_ISO19139_1001.xml)
     * │    └── ...
     * ├── geodocs -> Directory of documents saved by the user, one file per doc plus one CSV file with additional info for each doc
     *      ├── geodocs.csv -> CSV file with per-file hash record statistics
     *      ├── YYYY-MM-DD_<standard>_<hash>.xml (e.g. 2019-07-01_abc123.sld)
     *      ├── YYYY-MM-DD_<standard>_<hash>.xml (e.g. 2019-07-01_abc124.gml)
     *      └── ...
     * }
     * </pre>
     */
    public @VisibleForTesting UserDataBundle buildUserDataBundle(@NonNull Account account, @NonNull Path bundleFolder)
            throws Exception {
        log.info("Creating GDPR data bundle for account {}", account.getUid());
        final String lidfContent = userInfoExporter.exportAsLdif(account);

        Files.write(bundleFolder.resolve("account_info.ldif"), Collections.singleton(lidfContent));

        final Path metadataDirectory = bundleFolder.resolve("metadata");
        final Path geodocsDirectory = bundleFolder.resolve("geodocs");
        final Path extractorCsvFile = bundleFolder.resolve("data_extractions_log.csv");
        final Path ogcstatsCsvFile = bundleFolder.resolve("ogc_request_log.csv");

        try {
            Files.createDirectory(metadataDirectory);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        final @Cleanup ContentProducer<MetadataRecord> metadata = new MetadataProducer(metadataDirectory);
        final @Cleanup ContentProducer<GeodocRecord> docs = new GeodocsProducer(geodocsDirectory);
        final @Cleanup ContentProducer<ExtractorRecord> extractor = new ExtractorProducer(extractorCsvFile);
        final @Cleanup ContentProducer<OgcStatisticsRecord> ogcStats = new OgcStatisticsProducer(ogcstatsCsvFile);

        CompletableFuture<Void> extractorTask;
        CompletableFuture<Void> geodocsTask;
        CompletableFuture<Void> mdTask;
        CompletableFuture<Void> statsTask;

        extractorTask = runAsync(() -> accountGDPRDao.visitExtractorRecords(account, extractor));
        geodocsTask = runAsync(() -> accountGDPRDao.visitGeodocsRecords(account, docs));
        mdTask = runAsync(() -> accountGDPRDao.visitMetadataRecords(account, metadata));
        statsTask = runAsync(() -> accountGDPRDao.visitOgcStatsRecords(account, ogcStats));

        CompletableFuture<Void> allTasks = CompletableFuture.allOf(extractorTask, geodocsTask, mdTask, statsTask);

        CompletableFuture<UserDataBundle> bundleFuture = allTasks.thenApply(v -> new UserDataBundle(bundleFolder,
                metadataDirectory, geodocsDirectory, extractorCsvFile, ogcstatsCsvFile));

        return bundleFuture.join();
    }

    private static Path createZipFile(@NonNull Path bundleFolder) {
        File rootDir = bundleFolder.toFile();
        File zip = bundleFolder.getParent().resolve(bundleFolder.getFileName() + ".zip").toFile();
        log.info("Compressing user data from {} to {}", rootDir.getAbsolutePath(), zip.getAbsolutePath());
        ZipUtil.pack(rootDir, zip);
        log.info("User data zip file created successfully");
        return zip.toPath();
    }

    private static interface ContentProducer<T> extends Consumer<T>, AutoCloseable {
        static <V, R> R ifNonNull(@Nullable V value, Function<V, R> mapper) {
            return value == null ? null : mapper.apply(value);
        }
    }

    private static abstract class CsvContentProducer<T> implements ContentProducer<T> {

        private Path csvFile;
        private CSVPrinter csvWriter;

        static final CSVFormat FORMAT = CSVFormat.RFC4180.withQuoteMode(QuoteMode.MINIMAL);

        protected CsvContentProducer(@NonNull Path target) {
            this.csvFile = target;
            try {
                Files.createDirectories(target.getParent());
                Files.createFile(csvFile);
                this.csvWriter = FORMAT.print(target, StandardCharsets.UTF_8);
                this.csvWriter.printRecord(createHeader());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        public @Override void close() {
            try {
                csvWriter.close();
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
            }
        }

        public @Override void accept(T record) {
            try {
                List<Object> values = encode(record);
                if (!values.isEmpty())
                    csvWriter.printRecord(values);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        protected abstract List<String> createHeader();

        protected abstract List<Object> encode(T record);
    }

    private @RequiredArgsConstructor static class MetadataProducer implements ContentProducer<MetadataRecord> {

        /**
         * Directory where to save one metadata file per record
         */
        private final Path directory;

        public @Override void accept(MetadataRecord record) {
            LocalDateTime createdDate = record.getCreatedDate();
            if (createdDate == null || record.getDocumentContent() == null) {
                return;
            }
            LocalDateTime createdAt = createdDate.atZone(ZoneId.systemDefault()).toLocalDateTime();
            String date = FILENAME_DATE_FORMAT.format(createdAt);
            String fileName = String.format("%s_%s_%d.xml", date, record.getSchemaId(), record.getId());
            Path targetFile = directory.resolve(fileName);
            try {
                Files.write(targetFile, Collections.singleton(record.getDocumentContent()));
            } catch (IOException e) {
                log.error("Error writing medatata document {}", targetFile.toAbsolutePath(), e);
            }
        }

        public @Override void close() throws Exception {
            // nothing to close, each md file is opened and closed atomically at accept()
        }
    }

    /**
     * Receives a {@link Path} denoting a directory where to save a
     * {@code geodocs.csv} file with one row per user's document, as well as one
     * file with the actual document for each record.
     */
    private static class GeodocsProducer extends CsvContentProducer<GeodocRecord> {

        /**
         * Directory where to save one the {@code geodocs.csv} file as well as one
         * document (the actual geodoc) per record
         */
        private final Path directory;

        public GeodocsProducer(Path directory) {
            super(directory.resolve("geodocs.csv"));
            this.directory = directory;
        }

        protected @Override List<String> createHeader() {
            return Arrays.asList("created_at", "last_access", "standard", "access_count", "file_hash");
        }

        @Override
        protected List<Object> encode(GeodocRecord record) {
            LocalDateTime createdAt = record.getCreatedAt();
            if (null == createdAt) {
                return Collections.emptyList();
            }
            String date = FILENAME_DATE_FORMAT.format(createdAt);
            String standard = record.getStandard() == null ? "unknown" : record.getStandard().toLowerCase();
            String fileName = String.format("%s_%s.%s", date, record.getFileHash(), standard);
            Path docFile = directory.resolve(fileName);
            try {
                Files.write(docFile, Collections.singleton(record.getRawFileContent()));
            } catch (IOException e) {
                log.error("Error writing medatata document {}", docFile.toAbsolutePath(), e);
            }
            return Arrays.asList(//
                    ContentProducer.ifNonNull(record.getCreatedAt(), CONTENT_DATE_FORMAT::format), //
                    ContentProducer.ifNonNull(record.getLastAccess(), CONTENT_DATE_FORMAT::format), //
                    record.getStandard(), //
                    record.getAccessCount(), //
                    record.getFileHash()//
            );
        }
    }

    private static class ExtractorProducer extends CsvContentProducer<ExtractorRecord> {
        private static final WKTWriter WKT_WRITER = new WKTWriter();

        protected ExtractorProducer(@NonNull Path target) {
            super(target);
        }

        protected @Override List<String> createHeader() {
            return Arrays.asList("creation_date", "duration", "organization", "roles", "success", "layer_name",
                    "format", "projection", "resolution", "bounding_box", "OWS_type", "URL");
        }

        protected @Override List<Object> encode(ExtractorRecord record) {
            String bbox = ContentProducer.ifNonNull(record.getBbox(), box -> WKT_WRITER.write(box));
            String owsurl = record.getOwsurl();
            return Arrays.asList(//
                    ContentProducer.ifNonNull(record.getCreationDate(), CONTENT_DATE_FORMAT::format), //
                    ContentProducer.ifNonNull(record.getDuration(),
                            d -> DateTimeFormatter.ISO_TIME.format(d.toLocalTime())), //
                    record.getOrg(), //
                    ContentProducer.ifNonNull(record.getRoles(),
                            roles -> roles.stream().collect(Collectors.joining("|"))), //
                    record.isSuccess(), //
                    record.getLayerName(), //
                    record.getFormat(), //
                    record.getProjection(), //
                    record.getResolution(), //
                    bbox, //
                    record.getOwstype(), //
                    owsurl);
        }
    }

    private static class OgcStatisticsProducer extends CsvContentProducer<OgcStatisticsRecord> {

        protected OgcStatisticsProducer(@NonNull Path target) {
            super(target);
        }

        protected @Override List<String> createHeader() {
            return Arrays.asList("date", "organization", "roles", "layer", "service", "request");
        }

        protected @Override List<Object> encode(OgcStatisticsRecord record) {
            return Arrays.asList(//
                    ContentProducer.ifNonNull(record.getDate(), CONTENT_DATE_FORMAT::format), //
                    record.getOrg(), //
                    ContentProducer.ifNonNull(record.getRoles(),
                            roles -> roles.stream().collect(Collectors.joining("|"))), //
                    record.getLayer(), //
                    record.getService(), //
                    record.getRequest());
        }

    }
}
