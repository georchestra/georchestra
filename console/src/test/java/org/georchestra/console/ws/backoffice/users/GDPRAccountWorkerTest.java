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
import org.georchestra.console.ds.AccountGDPRDao.ExtractorRecord;
import org.georchestra.console.ds.AccountGDPRDao.GeodocRecord;
import org.georchestra.console.ds.AccountGDPRDao.MetadataRecord;
import org.georchestra.console.ds.AccountGDPRDao.OgcStatisticsRecord;
import org.georchestra.console.ds.DataServiceException;
import org.georchestra.console.dto.Account;
import org.georchestra.console.dto.AccountImpl;
import org.georchestra.console.ws.backoffice.users.GDPRAccountWorker.DeletedAccountSummary;
import org.georchestra.console.ws.backoffice.users.GDPRAccountWorker.UserDataBundle;
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

	private static class AccountGDPRDaoStub implements AccountGDPRDao {

		private final ListMultimap<String, GeodocRecord> geodocRecords = ArrayListMultimap.create();
		private final ListMultimap<String, OgcStatisticsRecord> ogcstatsRecords = ArrayListMultimap.create();
		private final ListMultimap<String, ExtractorRecord> extractorRecords = ArrayListMultimap.create();
		private final ListMultimap<String, MetadataRecord> metadataRecords = ArrayListMultimap.create();

		public @Override DeletedRecords deleteAccountRecords(@NonNull Account account) throws DataServiceException {
			List<GeodocRecord> geodocs = geodocRecords.removeAll(account.getUid());
			List<OgcStatisticsRecord> ogcstats = ogcstatsRecords.removeAll(account.getUid());
			List<MetadataRecord> md = metadataRecords.removeAll(account.getUid());
			List<ExtractorRecord> extractor = extractorRecords.removeAll(account.getUid());
			geodocRecords.putAll(DELETED_ACCOUNT_USERNAME, geodocs);
			ogcstatsRecords.putAll(DELETED_ACCOUNT_USERNAME, ogcstats);
			metadataRecords.putAll(DELETED_ACCOUNT_USERNAME, md);
			extractorRecords.putAll(DELETED_ACCOUNT_USERNAME, extractor);
			return new DeletedRecords(account.getUid(), md.size(), extractor.size(), geodocs.size(), ogcstats.size());
		}

		public @Override void visitGeodocsRecords(@NonNull Account owner, @NonNull Consumer<GeodocRecord> consumer) {
			geodocRecords.get(owner.getUid()).forEach(consumer);
		}

		public @Override void visitOgcStatsRecords(@NonNull Account owner,
				@NonNull Consumer<OgcStatisticsRecord> consumer) {
			ogcstatsRecords.get(owner.getUid()).forEach(consumer);
		}

		public @Override void visitExtractorRecords(@NonNull Account owner,
				@NonNull Consumer<ExtractorRecord> consumer) {
			extractorRecords.get(owner.getUid()).forEach(consumer);
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
		stub.extractorRecords.putAll(uid1, extractorRecords(uid1, 3));
		stub.extractorRecords.putAll(uid2, extractorRecords(uid2, 4));

		stub.metadataRecords.putAll(uid1, metadataRecords(uid1, 3));
		stub.metadataRecords.putAll(uid2, metadataRecords(uid2, 4));

		stub.geodocRecords.putAll(uid1, geodocRecords(uid1, 3));
		stub.geodocRecords.putAll(uid2, geodocRecords(uid2, 4));

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

	private Iterable<? extends GeodocRecord> geodocRecords(String uid, int count) {
		return IntStream.range(0, count).mapToObj(i -> //
		new GeodocRecord("WMS", uid + count + "_content", uid + count, LocalDateTime.now(), LocalDateTime.now(), i))//
				.collect(Collectors.toList());
	}

	private Iterable<? extends MetadataRecord> metadataRecords(String uid, int count) {
		return IntStream.range(0, count).mapToObj(i -> //
		new MetadataRecord(i, LocalDateTime.now(), "iso19139", "<MD_Metadata>" + uid + count + "</MD_Metadata>", "name",
				"surname"))//
				.collect(Collectors.toList());
	}

	private Iterable<? extends ExtractorRecord> extractorRecords(String accountId, int count) throws Exception {
		LocalDateTime creationDate = LocalDateTime.now();
		Time duration = new Time((long) 1e9);
		List<String> roles = Lists.newArrayList(accountId + "_role1", accountId + "_role2");
		String org = "org_" + accountId;
		String projection = "EPSG:3857";
		Integer resolution = 2000;
		String format = "shp";
		Geometry bbox = new WKTReader().read("POLYGON((-180 -90, -180 90, 180 90, 180 -90, -180 -90))");
		String owstype = "WFS";
		String owsurl = "http://test.com";
		String layerName = "testlayer";
		boolean success = true;
		return IntStream.range(0, count).mapToObj(i -> //
		new ExtractorRecord(creationDate, duration, roles, org, projection, resolution, format, bbox, owstype, owsurl,
				layerName, success)).collect(Collectors.toList());
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
		final Path geodocsDirectory = bundleFolder.resolve("geodocs");
		final Path extractorCsvFile = bundleFolder.resolve("data_extractions_log.csv");
		final Path ogcstatsCsvFile = bundleFolder.resolve("ogc_request_log.csv");
		assertBundle(extractorCsvFile, ogcstatsCsvFile, geodocsDirectory, metadataDirectory, recordsPerUnit);
	}

	private void assertBundle(UserDataBundle bundle, int recordsPerUnit) throws IOException {
		Path extractorCsvFile = bundle.getExtractorCsvFile();
		Path ogcstatsCsvFile = bundle.getOgcstatsCsvFile();
		Path geodocsDirectory = bundle.getGeodocsDirectory();
		Path metadataDirectory = bundle.getMetadataDirectory();

		assertBundle(extractorCsvFile, ogcstatsCsvFile, geodocsDirectory, metadataDirectory, recordsPerUnit);
	}

	private void assertBundle(Path extractorCsvFile, Path ogcstatsCsvFile, Path geodocsDirectory,
			Path metadataDirectory, int recordsPerUnit) throws IOException {
		String extractorHeader = "creation_date,duration,organization,roles,success,layer_name,format,projection,resolution,bounding_box,OWS_type,URL";
		assertNumCsvRecords(extractorCsvFile, recordsPerUnit, extractorHeader);

		String ogcstatsHeader = "date,organization,roles,layer,service,request";
		assertNumCsvRecords(ogcstatsCsvFile, recordsPerUnit, ogcstatsHeader);

		String geodocsHeader = "created_at,last_access,standard,access_count,file_hash";
		assertNumCsvRecords(geodocsDirectory.resolve("geodocs.csv"), recordsPerUnit, geodocsHeader);

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
		assertEquals(4, summary.getExtractorRecords());
		assertEquals(4, summary.getGeodocsRecords());
		assertEquals(4, summary.getMetadataRecords());
		assertEquals(4, summary.getOgcStatsRecords());

		assertEquals(4, daoStub.extractorRecords.get(ghostAccount.getUid()).size());
		assertEquals(4, daoStub.geodocRecords.get(ghostAccount.getUid()).size());
		assertEquals(4, daoStub.metadataRecords.get(ghostAccount.getUid()).size());
		assertEquals(4, daoStub.ogcstatsRecords.get(ghostAccount.getUid()).size());

		summary = worker.deleteAccountRecords(account2);
		assertEquals(uid, summary.getAccountId());
		assertEquals(0, summary.getExtractorRecords());
		assertEquals(0, summary.getGeodocsRecords());
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
