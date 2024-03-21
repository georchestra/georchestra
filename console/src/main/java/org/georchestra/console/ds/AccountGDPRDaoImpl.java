/*
 * Copyright (C) 2009 by the geOrchestra PSC
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

package org.georchestra.console.ds;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.sql.DataSource;

import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.users.Account;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.annotations.VisibleForTesting;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 */
@Slf4j
public class AccountGDPRDaoImpl implements AccountGDPRDao {

    /**
     * DatTimeFormatter to parse timestamps we they come in geonetworks' text column
     */
    @VisibleForTesting
    public static final DateTimeFormatter GEONETWORK_DATE_FORMAT = new DateTimeFormatterBuilder()//
            .parseCaseInsensitive()//
            .append(DateTimeFormatter.ISO_LOCAL_DATE)//
            .appendLiteral('T')// 'T' instead of space (http://jkorpela.fi/iso8601.html)
            .append(DateTimeFormatter.ISO_LOCAL_TIME)//
            .toFormatter();

    private static final String QUERY_METADATA_RECORDS = "select md.id, md.createdate, md.schemaid, md.data, u.name, u.surname"
            + " from geonetwork.metadata md left join geonetwork.users u on md.owner = u.id"//
            + " where u.username = ?";

    private static final String DELETE_METADATA_RECORDS = "update geonetwork.users set username = ?, name = ?, surname = ? where username = ?";
    private static final String QUERY_USER_ID_METADATA_RECORDS = "select id from geonetwork.users where username = ?";
    private static final String COUNT_USER_ID_METADATA_RECORDS = "select count(*) from geonetwork.metadata where owner = ?";

    private static final String QUERY_OGCSTATS_RECORDS = "select date, service, layer, id, request, org, roles from ogcstatistics.ogc_services_log where user_name = ?";
    private static final String DELETE_OGCSTATS_RECORDS = "update ogcstatistics.ogc_services_log set user_name = ? where user_name = ?";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DataSource dataSourceGeonetwork;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setDataSourceGN(DataSource dataSource) {
        this.dataSourceGeonetwork = dataSource;
    }

    /**
     * s Deletes (obfuscates) all GDPR sensitive records for the given account and
     * returns a summary of records affected. Deleting here means making the records
     * untraceable to the account owner, but keep them under a "ghost" user name
     * ({@code _deleted_account_}) for statistical purposes.
     */
    public @Override DeletedRecords deleteAccountRecords(@NonNull Account account) throws DataServiceException {
        try (Connection conn = dataSource.getConnection(); Connection connGN = dataSourceGeonetwork.getConnection()) {
            int metadataRecords;
            int ogcStatsRecords = 0;
            conn.setAutoCommit(false);
            connGN.setAutoCommit(false);
            try {
                metadataRecords = deleteUserMetadataRecords(connGN, account);
                connGN.commit();
            } catch (SQLException e) {
                connGN.rollback();
                throw new DataServiceException(e);
            } finally {
                connGN.setAutoCommit(true);
            }
            conn.setAutoCommit(false);
            try {
                ogcStatsRecords = deleteUserOgcStatsRecords(conn, account);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
            DeletedRecords ret = new DeletedRecords(account.getUid(), metadataRecords, ogcStatsRecords);
            log.info("Deleted records: {}", ret);
            return ret;
        } catch (SQLException e) {
            throw new DataServiceException(e);
        }
    }

    private int deleteUserOgcStatsRecords(Connection conn, @NonNull Account account) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_OGCSTATS_RECORDS)) {
            ps.setString(1, DELETED_ACCOUNT_USERNAME);
            ps.setString(2, account.getUid());
            return ps.executeUpdate();
        }
    }

    private int deleteUserMetadataRecords(Connection conn, @NonNull Account account) throws SQLException {
        final long userID;
        try (PreparedStatement ps = conn.prepareStatement(QUERY_USER_ID_METADATA_RECORDS)) {
            ps.setString(1, account.getUid());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    userID = rs.getLong(1);
                } else {
                    return 0;
                }
            }
        }
        int count = 0;
        try (PreparedStatement ps = conn.prepareStatement(COUNT_USER_ID_METADATA_RECORDS)) {
            ps.setLong(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                } else {
                    return 0;
                }
            }
        }
        final String ghostUsername = DELETED_ACCOUNT_USERNAME + userID;
        try (PreparedStatement ps = conn.prepareStatement(DELETE_METADATA_RECORDS)) {
            ps.setString(1, ghostUsername);// username
            ps.setString(2, ghostUsername);// name
            ps.setString(3, ghostUsername);// surname
            ps.setString(4, account.getUid());
            ps.executeUpdate();
        }
        return count;
    }

    public @Override void visitOgcStatsRecords(@NonNull Account owner,
            @NonNull Consumer<OgcStatisticsRecord> consumer) {

        final String userName = owner.getUid();
        try {
            int reccount = visitRecords(QUERY_OGCSTATS_RECORDS, ps -> ps.setString(1, userName),
                    AccountGDPRDaoImpl::createOgcStatisticsRecord, consumer, dataSource.getConnection());
            log.info("Extracted {} OGC statistics records for user {}", reccount, userName);
        } catch (DataServiceException | SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public @Override void visitMetadataRecords(@NonNull Account owner, @NonNull Consumer<MetadataRecord> consumer) {

        final String userName = owner.getUid();
        try {
            int reccount = visitRecords(QUERY_METADATA_RECORDS, ps -> ps.setString(1, userName),
                    AccountGDPRDaoImpl::createMetadataRecord, consumer, dataSourceGeonetwork.getConnection());
            log.info("Extracted {} metadata records for user {}", reccount, userName);
        } catch (DataServiceException | SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @FunctionalInterface
    interface PreparedStatementBuilder {
        void accept(PreparedStatement ps) throws SQLException;
    }

    private <R> int visitRecords(String psQuery, PreparedStatementBuilder psBuilder, Function<ResultSet, R> mapper,
            @NonNull Consumer<R> consumer, Connection c) throws DataServiceException {

        try (PreparedStatement ps = c.prepareStatement(psQuery)) {

            psBuilder.accept(ps);

            int count = 0;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    R record = mapper.apply(rs);
                    consumer.accept(record);
                    count++;
                }
            }
            return count;
        } catch (IllegalStateException e) {
            throw new DataServiceException(e.getCause() == null ? e : e.getCause());
        } catch (SQLException e) {
            throw new DataServiceException(e);
        }
    }

    public static MetadataRecord createMetadataRecord(ResultSet rs) {

        MetadataRecord.MetadataRecordBuilder builder = MetadataRecord.builder();
        try {
            builder.id(rs.getLong("id"));
            // createdate character varying(30)
            // e.g. "2019-05-07T00:05:56"
            String createDateString = rs.getString("createdate");
            LocalDateTime dateTime = ifNonNull(createDateString, value -> {
                TemporalAccessor parsed = GEONETWORK_DATE_FORMAT.parse(value);
                return LocalDateTime.from(parsed);
            });

            builder.createdDate(dateTime);
            builder.schemaId(rs.getString("schemaid"));
            builder.documentContent(rs.getString("data"));
            builder.userName(rs.getString("name"));
            builder.userSurname(rs.getString("surname"));
            return builder.build();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static OgcStatisticsRecord createOgcStatisticsRecord(ResultSet rs) {
        OgcStatisticsRecord.OgcStatisticsRecordBuilder builder = OgcStatisticsRecord.builder();
        try {
            builder.date(ifNonNull(rs.getTimestamp("date"), Timestamp::toLocalDateTime));
            builder.service(rs.getString("service"));
            builder.layer(rs.getString("layer"));
            builder.request(rs.getString("request"));
            builder.org(rs.getString("org"));
            builder.roles(getStringArray(rs, "roles"));
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return builder.build();
    }

    private static @NonNull List<String> getStringArray(ResultSet rs, String column) throws SQLException {
        java.sql.Array array = rs.getArray(column);
        if (array != null) {
            String[] javaArray = (String[]) array.getArray();
            if (javaArray != null) {
                return Arrays.asList(javaArray);
            }
        }
        return Collections.emptyList();
    }

    private static <V, R> R ifNonNull(@Nullable V value, Function<V, R> mapper) {
        return value == null ? null : mapper.apply(value);
    }

}