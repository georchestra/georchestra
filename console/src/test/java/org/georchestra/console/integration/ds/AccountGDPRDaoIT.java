package org.georchestra.console.integration.ds;

import static com.github.database.rider.core.api.dataset.SeedStrategy.CLEAN_INSERT;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.georchestra.console.ds.AccountGDPRDao;
import org.georchestra.console.ds.AccountGDPRDao.DeletedRecords;
import org.georchestra.console.ds.AccountGDPRDao.ExtractorRecord;
import org.georchestra.console.ds.AccountGDPRDao.MetadataRecord;
import org.georchestra.console.ds.AccountGDPRDao.OgcStatisticsRecord;
import org.georchestra.console.ds.AccountGDPRDaoImpl;
import org.georchestra.console.integration.ConsoleIntegrationTest;
import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.users.AccountImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;

/**
 * Integration test suite for {@link AccountGDPRDao}.
 * <p>
 * Requires running LDAP and PostgreSQL instances as set up using docker in the
 * project's pom.
 * <p>
 * This test suite uses {@link DBRider} and hence {@link DBUnit} to populate
 * records from .csv files in the test resources folder.
 * <p>
 * Note all tests annotated with <code>@DataSet</code> run a minimal geonetwork
 * schema set up script:
 * <code>@DataSet(executeScriptsBefore = "dbunit/geonetwork_ddl.sql"</code> so
 * we don't need a running geonetwork that creates its database schema at
 * startup.
 *
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath:/webmvc-config-test.xml" })
@DBRider
public class AccountGDPRDaoIT extends ConsoleIntegrationTest {

    private @Autowired AccountGDPRDao dao;
    private Account user1, user2;

    public @Before void before() {
        user1 = new AccountImpl();
        user1.setUid("user1");

        user2 = new AccountImpl();
        user2.setUid("user2");
    }

    @DBUnit(qualifiedTableNames = true, dataTypeFactoryClass = PostgresExtendedDataTypeFactory.class)
    @DataSet(executeScriptsBefore = "dbunit/geonetwork_ddl.sql", strategy = CLEAN_INSERT, value = { "dbunit/all.csv" })
    public @Test void testDeleteAccountRecords() throws DataServiceException {
        DeletedRecords summary = dao.deleteAccountRecords(user1);
        assertEquals(user1.getUid(), summary.getAccountId());
        assertEquals(3, summary.getOgcStatsRecords());
        assertEquals(2, summary.getExtractorRecords());
        assertEquals(2, summary.getMetadataRecords());

        summary = dao.deleteAccountRecords(user1);
        assertEquals(user1.getUid(), summary.getAccountId());
        assertEquals(0, summary.getOgcStatsRecords());
        assertEquals(0, summary.getExtractorRecords());
        assertEquals(0, summary.getMetadataRecords());
    }

    @DBUnit(qualifiedTableNames = true, dataTypeFactoryClass = PostgresExtendedDataTypeFactory.class)
    @DataSet(executeScriptsBefore = "dbunit/geonetwork_ddl.sql", strategy = CLEAN_INSERT, value = { "dbunit/all.csv" })
    public @Test void testDeleteMetadataRecordsObfuscatesUserNameAndSurname() throws DataServiceException {
        List<MetadataRecord> user1Records = new ArrayList<>();
        dao.visitMetadataRecords(user1, user1Records::add);
        assertEquals(2, user1Records.size());
        for (MetadataRecord r : user1Records) {
            assertEquals("Name1", r.getUserName());
            assertEquals("Surname1", r.getUserSurname());
        }

        DeletedRecords summary = dao.deleteAccountRecords(user1);
        assertEquals(user1.getUid(), summary.getAccountId());
        assertEquals(2, summary.getMetadataRecords());

        String userId = "1000";// as specified in the test data
        String expectedObfuscatedName = AccountGDPRDaoImpl.DELETED_ACCOUNT_USERNAME + userId;
        user1.setUid(expectedObfuscatedName);

        user1Records.clear();
        dao.visitMetadataRecords(user1, user1Records::add);
        assertEquals(2, user1Records.size());
        for (MetadataRecord r : user1Records) {
            assertEquals(expectedObfuscatedName, r.getUserName());
            assertEquals(expectedObfuscatedName, r.getUserSurname());
        }
    }

    @DBUnit(qualifiedTableNames = true, dataTypeFactoryClass = PostgresExtendedDataTypeFactory.class)
    @DataSet(executeScriptsBefore = "dbunit/geonetwork_ddl.sql", strategy = CLEAN_INSERT, value = "dbunit/ogcstatistics.ogc_services_log.csv")
    public @Test void testVisitOgcStatisticsRecords() {
        List<OgcStatisticsRecord> user1Records = new ArrayList<>();
        List<OgcStatisticsRecord> user2Records = new ArrayList<>();
        dao.visitOgcStatsRecords(user1, user1Records::add);
        dao.visitOgcStatsRecords(user2, user2Records::add);

        assertEquals(3, user1Records.size());
        assertEquals(3, user2Records.size());
    }

    @DBUnit(qualifiedTableNames = true, dataTypeFactoryClass = PostgresExtendedDataTypeFactory.class)
    @DataSet(executeScriptsBefore = "dbunit/geonetwork_ddl.sql", strategy = CLEAN_INSERT, value = "dbunit/extractorapp.extractor_log.csv")
    public @Test void testVisitExtractorRecords() {
        List<ExtractorRecord> user1Records = new ArrayList<>();
        List<ExtractorRecord> user2Records = new ArrayList<>();
        dao.visitExtractorRecords(user1, user1Records::add);
        dao.visitExtractorRecords(user2, user2Records::add);

        assertEquals(2, user1Records.size());
        assertEquals(2, user2Records.size());
    }

    @DBUnit(qualifiedTableNames = true, dataTypeFactoryClass = PostgresExtendedDataTypeFactory.class)
    @DataSet(executeScriptsBefore = "dbunit/geonetwork_ddl.sql", strategy = CLEAN_INSERT, value = "dbunit/geonetwork.metadata.csv")
    public @Test void testVisitMetadataRecords() {
        List<MetadataRecord> user1Records = new ArrayList<>();
        List<MetadataRecord> user2Records = new ArrayList<>();
        dao.visitMetadataRecords(user1, user1Records::add);
        dao.visitMetadataRecords(user2, user2Records::add);

        assertEquals(2, user1Records.size());
        assertEquals(2, user2Records.size());
    }
}
