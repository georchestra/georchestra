/*
 * Copyright (C) 2009-2018 by the geOrchestra PSC
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

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import org.georchestra.console.dto.Account;
import org.locationtech.jts.geom.Geometry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 */
public interface AccountGDPRDao {

	static final String DELETED_ACCOUNT_USERNAME = "_deleted_account_";

	/**
	 * Deletes all GDPR sensitive records for the given account and returns a
	 * summary of records affected. Deleting here means making the records
	 * untraceable to the account owner, but keep them under a "ghost" user name
	 * ({@code _deleted_account_}) for statistical purposes.
	 */
	DeletedRecords deleteAccountRecords(@NonNull Account account) throws DataServiceException;

	void visitGeodocsRecords(@NonNull Account owner, @NonNull Consumer<GeodocRecord> consumer);

	void visitOgcStatsRecords(@NonNull Account owner, @NonNull Consumer<OgcStatisticsRecord> consumer);

	void visitExtractorRecords(@NonNull Account owner, @NonNull Consumer<ExtractorRecord> consumer);

	void visitMetadataRecords(@NonNull Account owner, @NonNull Consumer<MetadataRecord> consumer);

	@Value
	class DeletedRecords {
		private String accountId;
		private int metadataRecords;
		private int extractorRecords;
		private int geodocsRecords;
		private int ogcStatsRecords;
	}

	@Value
	@Builder
	@AllArgsConstructor
	class GeodocRecord {
		private String standard;
		private String rawFileContent;
		private String fileHash;
		private LocalDateTime createdAt;
		private LocalDateTime lastAccess;
		private int accessCount;
	}

	@Value
	@Builder
	@AllArgsConstructor
	class OgcStatisticsRecord {
		private LocalDateTime date;
		private String service;
		private String layer;
		private String request;
		private String org;
		private List<String> roles;
	}

	@Value
	@Builder
	@AllArgsConstructor
	class ExtractorRecord {
		private LocalDateTime creationDate;
		private Time duration;
		private List<String> roles;
		private String org;
		private String projection;
		private Integer resolution;
		private String format;
		private Geometry bbox;
		private String owstype;
		private String owsurl;
		private String layerName;
		private boolean success;

	}

	@Value
	@Builder
	@AllArgsConstructor
	class MetadataRecord {
		private long id;
		private LocalDateTime createdDate;
		private String schemaId;
		private String documentContent;
		private String userName;
		private String userSurname;
	}

}