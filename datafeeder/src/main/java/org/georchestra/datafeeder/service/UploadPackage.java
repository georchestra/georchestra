/*
 * Copyright (C) 2020 by the geOrchestra PSC
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
package org.georchestra.datafeeder.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

import com.google.common.annotations.VisibleForTesting;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UploadPackage {

    private FileStorageService service;
    private @Getter UUID id;

    private static final Pattern ARCHIVE_FILENAME_PATTERN = Pattern
            .compile(".+(\\.(?i)(zip|gz|bz2|tar|tgz|tar\\.gz|tar\\.bz2))$");

    private static final Pattern DATASET_FILENAME_PATTERN = Pattern.compile(".+(\\.(?i)(shp|gpkg|geojson|csv))$");

    public UploadPackage(@NonNull FileStorageService service, @NonNull UUID id) throws IOException {
        this.service = service;
        this.id = id;
    }

    @VisibleForTesting
    Path root() {
        return service.resolve(id);
    }

    public Set<String> findDatasetFiles() throws IOException {
        return relativeFileNames().filter(this::isDataset).collect(Collectors.toCollection(TreeSet::new));
    }

    public Set<String> findAll() throws IOException {
        return relativeFileNames().collect(Collectors.toCollection(TreeSet::new));
    }

    public Path resolve(@NonNull String fileName) {
        return root().resolve(fileName).toAbsolutePath();
    }

    public boolean isArchive(@NonNull String fileName) {
        return ARCHIVE_FILENAME_PATTERN.matcher(fileName).matches();
    }

    public boolean isDataset(@NonNull String fileName) {
        return DATASET_FILENAME_PATTERN.matcher(fileName).matches();
    }

    public void unpack(@NonNull String archiveRelativeFileName) throws IOException {
        log.info("Unpacking {}", archiveRelativeFileName);
        Path archive = resolve(archiveRelativeFileName);
        if (!Files.exists(archive)) {
            throw new FileNotFoundException(archiveRelativeFileName + " not found under " + id);
        }
        FileSystemManager fsManager = VFS.getManager();
        try (FileObject fsRoot = fsManager.resolveFile(archive.toUri())) {
            try (FileObject localFileSystem = fsManager.createFileSystem(fsRoot)) {
                FileObject[] children = localFileSystem.getChildren();
                unpack(children);
            }
        }
    }

    private void unpack(FileObject[] children) throws IOException {
        for (FileObject fo : children) {
            unpack(fo);
        }
    }

    private void unpack(@NonNull final FileObject fo) throws IOException {
        final FileName name = fo.getName();
        final FileName root = name.getRoot();
        final String relativeName = root.getRelativeName(name);
        final Path path = resolve(relativeName);

        if (fo.isFolder()) {
            Files.createDirectory(path);
            unpack(fo.getChildren());
        } else {
            try (InputStream in = fo.getContent().getInputStream()) {
                Files.copy(in, path);
            }
        }
    }

    private Stream<String> relativeFileNames() throws IOException {
        final Path root = root();
        return files().map(root::relativize).map(Path::toString);
    }

    private Stream<Path> files() throws IOException {
        return Files.walk(root()).filter(Files::isRegularFile);
    }
}
