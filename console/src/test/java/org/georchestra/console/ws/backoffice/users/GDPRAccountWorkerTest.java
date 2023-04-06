package org.georchestra.console.ws.backoffice.users;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.georchestra.console.ds.AccountGDPRDao;
import org.georchestra.console.ds.AccountGDPRDao.MetadataRecord;
import org.georchestra.console.ds.AccountGDPRDao.OgcStatisticsRecord;
import org.georchestra.console.ws.backoffice.users.GDPRAccountWorker.DeletedAccountSummary;
import org.georchestra.console.ws.backoffice.users.GDPRAccountWorker.UserDataBundle;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.mockito.Mockito;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.zeroturnaround.zip.ZipUtil;

import com.google.common.base.Charsets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;

import lombok.Cleanup;
import lombok.NonNull;

public class GDPRAccountWorkerTest {

    private Account account1, account2, ghostAccount;
    private GDPRAccountWorker worker;
    private AccountGDPRDaoStub daoStub;

    public @Rule TemporaryFolder tmpFolder = new TemporaryFolder();
    public @Rule ExpectedException ex = ExpectedException.none();

    private static String ogcstatsHeader = "date,organization,roles,layer,service,request";

    private static class AccountGDPRDaoStub implements AccountGDPRDao {

        private final ListMultimap<String, OgcStatisticsRecord> ogcstatsRecords = ArrayListMultimap.create();
        private final ListMultimap<String, MetadataRecord> metadataRecords = ArrayListMultimap.create();

        public @Override DeletedRecords deleteAccountRecords(@NonNull Account account) throws DataServiceException {
            List<OgcStatisticsRecord> ogcstats = ogcstatsRecords.removeAll(account.getUid());
            List<MetadataRecord> md = metadataRecords.removeAll(account.getUid());
            ogcstatsRecords.putAll(DELETED_ACCOUNT_USERNAME, ogcstats);
            metadataRecords.putAll(DELETED_ACCOUNT_USERNAME, md);
            DeletedRecords summary = new DeletedRecords(account.getUid(), md.size(), ogcstats.size());
            return summary;
        }

        public @Override void visitOgcStatsRecords(@NonNull Account owner,
                @NonNull Consumer<OgcStatisticsRecord> consumer) {
            ogcstatsRecords.get(owner.getUid()).forEach(consumer);
        }

        public @Override void visitMetadataRecords(@NonNull Account owner, @NonNull Consumer<MetadataRecord> consumer) {
            metadataRecords.get(owner.getUid()).forEach(consumer);
        }

    }

    public @Before void before() throws Exception {
        ghostAccount = new AccountImpl();
        ghostAccount.setUid(AccountGDPRDao.DELETED_ACCOUNT_USERNAME);
        account1 = new AccountImpl();
        account1.setUid("user1");
        account2 = new AccountImpl();
        account2.setUid("user2");

        daoStub = new AccountGDPRDaoStub();
        AccountGDPRDaoStub stub = (AccountGDPRDaoStub) daoStub;
        final String uid1 = account1.getUid();
        final String uid2 = account2.getUid();

        stub.metadataRecords.putAll(uid1, metadataRecords(uid1, 3));
        stub.metadataRecords.putAll(uid2, metadataRecords(uid2, 4));

        stub.ogcstatsRecords.putAll(uid1, ogcstatsRecords(uid1, 3));
        stub.ogcstatsRecords.putAll(uid2, ogcstatsRecords(uid2, 4));

        UserInfoExporter ldifExporter = Mockito.mock(UserInfoExporterImpl.class);
        worker = new GDPRAccountWorker();
        worker.setAccountGDPRDao(daoStub);
        worker.setUserInfoExporter(ldifExporter);
    }

    private Iterable<? extends OgcStatisticsRecord> ogcstatsRecords(String uid, int count) {
        List<String> roles = Lists.newArrayList(uid + "_role1", uid + "_role2");
        return IntStream.range(0, count).mapToObj(i -> //
        new OgcStatisticsRecord(LocalDateTime.now(), "WFS", "testlayer", "request body", uid + "_org", roles))//
                .collect(Collectors.toList());
    }

    private Iterable<? extends MetadataRecord> metadataRecords(String uid, int count) {
        return IntStream.range(0, count).mapToObj(i -> //
        new MetadataRecord(i, LocalDateTime.now(), "iso19139", "<MD_Metadata>" + uid + count + "</MD_Metadata>", "name",
                "surname"))//
                .collect(Collectors.toList());
    }

    public @Test void testBuildUserDataBundle() throws Exception {
        Path bundleFolder = tmpFolder.newFolder(account1.getUid()).toPath();
        UserDataBundle bundle = worker.buildUserDataBundle(account1, bundleFolder);
        assertNotNull(bundle);
        assertEquals(bundleFolder, bundle.getFolder());
        assertBundle(bundle, 3);
        assertEquals(bundleFolder, bundle.getFolder());

        bundleFolder = tmpFolder.newFolder(account2.getUid()).toPath();
        bundle = worker.buildUserDataBundle(account2, bundleFolder);
        assertNotNull(bundle);
        assertEquals(bundleFolder, bundle.getFolder());
        assertBundle(bundle, 4);

        Account nonexistentuser = new AccountImpl();
        nonexistentuser.setUid("nonexistentuser");
        bundleFolder = tmpFolder.newFolder("nonexistentuser").toPath();
        bundle = worker.buildUserDataBundle(nonexistentuser, bundleFolder);
        assertNotNull(bundle);
        assertEquals(bundleFolder, bundle.getFolder());
        assertBundle(bundle, 0);
    }

    // Test resiliency to unexpected null values, see GSHDF-291
    public @Test void testBuildUserDataBundleResiliencyToNulls() throws Exception {
        final String uid1 = account1.getUid();

        final AccountGDPRDaoStub stub = (AccountGDPRDaoStub) daoStub;
        stub.metadataRecords.clear();
        stub.ogcstatsRecords.clear();

        LocalDateTime createdAt = LocalDateTime.now();
        stub.ogcstatsRecords.put(uid1, new OgcStatisticsRecord(null, null, null, null, null, null));

        LocalDateTime mdCreatedAt = createdAt;
        String mdContent = "<MD_Metadata/>";
        stub.metadataRecords.put(uid1, new MetadataRecord(1, mdCreatedAt, null, mdContent, null, null));
        mdContent = null;
        stub.metadataRecords.put(uid1, new MetadataRecord(2, mdCreatedAt, null, mdContent, null, null));
        mdCreatedAt = null;
        mdContent = null;
        stub.metadataRecords.put(uid1, new MetadataRecord(3, mdCreatedAt, null, mdContent, null, null));

        Path bundleFolder = tmpFolder.newFolder(account1.getUid()).toPath();
        UserDataBundle bundle = worker.buildUserDataBundle(account1, bundleFolder);
        assertNotNull(bundle);

        Path ogcstatsCsvFile = bundle.getOgcstatsCsvFile();
        Path metadataDirectory = bundle.getMetadataDirectory();

        assertNumCsvRecords(ogcstatsCsvFile, 1, ogcstatsHeader);
        assertMetadtaRecords(metadataDirectory, 1);
    }

    public @Test void testGenerateUserData() throws IOException {
        Resource zipResource = worker.generateUserData(account2);
        assertNotNull(zipResource);
        File zipFile = zipResource.getFile();
        assertNotNull(zipFile);
        File outputDir = tmpFolder.newFolder("extracted");
        ZipUtil.unpack(zipFile, outputDir);
        assertBundle(outputDir.toPath(), 4);
    }

    private void assertBundle(Path bundleFolder, int recordsPerUnit) throws IOException {
        final Path metadataDirectory = bundleFolder.resolve("metadata");
        final Path ogcstatsCsvFile = bundleFolder.resolve("ogc_request_log.csv");
        assertBundle(ogcstatsCsvFile, metadataDirectory, recordsPerUnit);
    }

    private void assertBundle(UserDataBundle bundle, int recordsPerUnit) throws IOException {
        Path ogcstatsCsvFile = bundle.getOgcstatsCsvFile();
        Path metadataDirectory = bundle.getMetadataDirectory();

        assertBundle(ogcstatsCsvFile, metadataDirectory, recordsPerUnit);
    }

    private void assertBundle(Path ogcstatsCsvFile, Path metadataDirectory, int recordsPerUnit) throws IOException {
        assertNumCsvRecords(ogcstatsCsvFile, recordsPerUnit, ogcstatsHeader);
        assertMetadtaRecords(metadataDirectory, recordsPerUnit);
    }

    private void assertMetadtaRecords(Path metadataDirectory, int recordsPerUnit) throws IOException {
        final @Cleanup DirectoryStream<Path> mdfiles = Files.newDirectoryStream(metadataDirectory);
        List<Path> files = Streams.stream(mdfiles).map(Path::getFileName).filter(p -> p.toString().endsWith(".xml"))
                .collect(Collectors.toList());
        assertEquals(files.toString(), recordsPerUnit, files.size());
    }

    private void assertNumCsvRecords(Path csvFile, int expectedRecords, String expectedHeader) throws IOException {
        List<String> lines = Files.readAllLines(csvFile, Charsets.UTF_8);
        assertEquals(expectedHeader, lines.get(0));
        assertEquals(1 + expectedRecords, lines.size());
    }

    public @Test void testDeleteAccountRecords() throws DataServiceException {
        DeletedAccountSummary summary = worker.deleteAccountRecords(account2);
        String uid = account2.getUid();
        assertEquals(uid, summary.getAccountId());
        assertEquals(4, summary.getMetadataRecords());
        assertEquals(4, summary.getOgcStatsRecords());

        assertEquals(4, daoStub.metadataRecords.get(ghostAccount.getUid()).size());
        assertEquals(4, daoStub.ogcstatsRecords.get(ghostAccount.getUid()).size());

        summary = worker.deleteAccountRecords(account2);
        assertEquals(uid, summary.getAccountId());
        assertEquals(0, summary.getMetadataRecords());
        assertEquals(0, summary.getOgcStatsRecords());
    }

    public @Test void testDispose() throws IOException {
        Resource zipResource = worker.generateUserData(account2);
        assertTrue(zipResource.getFile().exists());
        worker.dispose(zipResource);
        assertFalse(zipResource.getFile().exists());
        worker.dispose(zipResource);
        assertFalse(zipResource.getFile().exists());
    }

    public @Test void testDisposeNotAZipFile() throws IOException {
        File file = tmpFolder.newFile("notAZipFile.zip");
        ex.expect(IllegalArgumentException.class);
        ex.expectMessage("provided resource is not a ZIP file");
        worker.dispose(new FileSystemResource(file));
    }
}
