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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.annotations.VisibleForTesting;

import lombok.NonNull;

public class FileStorageService {

    private Path baseDirectory;

    public FileStorageService(@NonNull Path baseDirectory) {
        this.baseDirectory = baseDirectory;
        Assert.isTrue(Files.isDirectory(baseDirectory), baseDirectory + " is not a directory");
        Assert.isTrue(Files.isWritable(baseDirectory), baseDirectory + " is not writable");
    }

    /**
     */
    public UploadPackage createPackageFromUpload(@NonNull List<? extends MultipartFile> received) throws IOException {
        UploadPackage pack = createEmptyPackage();
        try {
            for (MultipartFile mpf : received) {
                addFileToPackage(pack, mpf);
            }
        } catch (IllegalStateException | IOException e) {
            deletePackage(pack.getId());
            throw e;
        }
        return pack;
    }

    public void addFileToPackage(@NonNull UploadPackage pack, @NonNull MultipartFile mpf) throws IOException {
        String fileName = mpf.getOriginalFilename();
        File dest = pack.resolve(fileName).toFile();
        mpf.transferTo(dest);
        if (pack.isArchive(fileName)) {
            pack.unpack(fileName);
        }
    }

    public UploadPackage createEmptyPackage() throws IOException {
        UUID packageId = UUID.randomUUID();
        Path root = resolve(packageId);
        Files.createDirectory(root);
        return new UploadPackage(this, packageId);
    }

    public UploadPackage find(@NonNull UUID packageId) throws IOException {
        Path path = resolve(packageId);
        if (!Files.isDirectory(path)) {
            throw new FileNotFoundException("Package " + packageId + " does not exist");
        }
        return new UploadPackage(this, packageId);
    }

    public void deletePackage(@NonNull UUID packageId) {
        File root = resolve(packageId).toFile();
        if (root.exists()) {
            FileSystemUtils.deleteRecursively(root);
        }
    }

    @VisibleForTesting
    Path resolve(@NonNull UUID id) {
        return baseDirectory.resolve(id.toString());
    }
}
