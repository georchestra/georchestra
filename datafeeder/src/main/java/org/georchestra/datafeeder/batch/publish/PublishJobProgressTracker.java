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
package org.georchestra.datafeeder.batch.publish;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.georchestra.datafeeder.model.JobStatus;
import org.hibernate.internal.util.collections.BoundedConcurrentHashMap;
import org.springframework.util.Assert;

import lombok.Getter;
import lombok.NonNull;

public class PublishJobProgressTracker {

    final ConcurrentMap<UUID, JobProgress> progress = new BoundedConcurrentHashMap<>();

    public JobProgress initialize(@NonNull DataUploadJob job) {
        UUID jobId = job.getJobId();
        JobProgress jobProgress = buildJobProgress(job);
        progress.put(jobId, jobProgress);
        return jobProgress;
    }

    public JobProgress dispose(@NonNull UUID jobId) {
        return progress.remove(jobId);
    }

    public Optional<JobProgress> find(@NonNull UUID jobId) {
        return Optional.ofNullable(progress.get(jobId));
    }

    public JobProgress get(@NonNull UUID jobId) {
        return find(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Progress for job " + jobId + " is not being tracked"));
    }

    public double getProgress(DataUploadJob job) {
        return find(job.getJobId()).map(JobProgress::getProgress)
                .orElseGet(() -> progressFromStatus(job.getPublishStatus()));
    }

    private double progressFromStatus(JobStatus status) {
        switch (status) {
        case DONE:
        case ERROR:
            return 1.0;
        default:
            return 0.0;
        }
    }

    public double getProgress(DatasetUploadState source) {
        Optional<JobProgress> job = find(source.getJob().getJobId());
        Optional<DatasetProgress> dataset = job.flatMap(j -> j.find(source.getId()));
        return dataset.map(DatasetProgress::getProgress).orElseGet(() -> progressFromStatus(source.getPublishStatus()));
    }

    public DatasetPublishingStep getCurrentStep(DatasetUploadState source) {
        Optional<JobProgress> job = find(source.getJob().getJobId());
        Optional<DatasetProgress> dataset = job.flatMap(j -> j.find(source.getId()));
        return dataset.map(DatasetProgress::getStep).orElseGet(() -> {
            JobStatus publishStatus = source.getPublishStatus();
            if (source.getPublishing().getPublish()
                    && (publishStatus == JobStatus.DONE || publishStatus == JobStatus.ERROR)) {
                return DatasetPublishingStep.COMPLETED;
            }
            return DatasetPublishingStep.SKIPPED;
        });
    }

    private JobProgress buildJobProgress(@NonNull DataUploadJob job) {
        List<DatasetUploadState> publishableDatasets = job.getPublishableDatasets();

        final long totalFeatures = publishableDatasets.stream().map(DatasetUploadState::getFeatureCount)
                .filter(Objects::nonNull).mapToLong(Integer::longValue).sum();

        JobProgress progress = new JobProgress();

        for (DatasetUploadState dataset : publishableDatasets) {
            int featureCount = dataset.getFeatureCount() == null ? 0 : dataset.getFeatureCount().intValue();
            DatasetProgress datasetProgress = DatasetProgress.valueOf(totalFeatures, featureCount);
            progress.add(dataset.getId(), datasetProgress);
        }
        return progress;
    }

    public static class JobProgress {
        private Map<Long, DatasetProgress> datasets = new ConcurrentHashMap<>();

        long totalEffort;

        void add(Long datasetId, DatasetProgress datasetProgress) {
            this.datasets.put(datasetId, datasetProgress);
            totalEffort = this.datasets.values().stream().map(DatasetProgress::getTotalEffort)
                    .mapToLong(Long::longValue).sum();

        }

        public double getProgress() {
            double sum = datasets.values().stream().mapToDouble(this::coalesceProgress).sum();
            if (sum < 0d || sum > 1d)
                throw new IllegalStateException("Coalesced progress out of bounds [0..1]: " + sum);
            return sum;
        }

        public Optional<DatasetProgress> find(@NonNull Long datasetId) {
            return Optional.ofNullable(datasets.get(datasetId));
        }

        public DatasetProgress getProgress(@NonNull Long datasetId) {
            return find(datasetId).orElseThrow(() -> new IllegalArgumentException("Progress for dataset " + datasetId
                    + " is not being tracked. Make sure it was a publishable dataset"));
        }

        private double coalesceProgress(DatasetProgress dataset) {
            double factor = (double) dataset.getTotalEffort() / (double) this.totalEffort;
            double progress = dataset.getProgress();
            return progress * factor;
        }
    }

    public static enum DatasetPublishingStep {
        SKIPPED, SCHEDULED, //
        DATA_IMPORT_STARTED, DATA_IMPORT_FINISHED, //
        OWS_PUBLISHING_STARTED, OWS_PUBLISHING_FINISHED, //
        METADATA_PUBLISHING_STARTED, METADATA_PUBLISHING_FINISHED, //
        OWS_METADATA_UPDATE_STARTED, OWS_METADATA_UPDATE_FINISHED, //
        COMPLETED
    }

    public static class DatasetProgress {
        private static final int OWS_PUBLISH_RELATIVE_IMPACT = 100;
        private static final int METADATA_PUBLISH_RELATIVE_IMPACT = 100;
        private static final int OWS_MD_UPDATE_RELATIVE_IMPACT = 100;

        private @Getter DatasetPublishingStep step = DatasetPublishingStep.SCHEDULED;
        private final double featureImportImpact, owsPublishImpact, mdPublishImpact, owsMdUpdateImpact;
        private final @Getter long totalEffort;

        private double dataImportProgress;
        private double nonDataImportProgress;

        public DatasetProgress(long totalEffort, double featureImportImpact, double owsPublishImpact,
                double mdPublishImpact, double owsMdUpdateImpact) {
            this.totalEffort = totalEffort;
            this.featureImportImpact = featureImportImpact;
            this.owsPublishImpact = owsPublishImpact;
            this.mdPublishImpact = mdPublishImpact;
            this.owsMdUpdateImpact = owsMdUpdateImpact;
        }

        public void setStep(@NonNull DatasetPublishingStep step) {
            this.step = step;
            switch (step) {
            case SCHEDULED:
                nonDataImportProgress = 0d;
                break;
            case DATA_IMPORT_STARTED:
                // no-upate, wait for setImportProgress() calls
                break;
            case DATA_IMPORT_FINISHED:
                dataImportProgress = 1.0;
                break;
            case OWS_PUBLISHING_STARTED:
                break;
            case OWS_PUBLISHING_FINISHED:
                nonDataImportProgress += owsMdUpdateImpact;
                break;
            case METADATA_PUBLISHING_STARTED:
                break;
            case METADATA_PUBLISHING_FINISHED:
                nonDataImportProgress += mdPublishImpact;
                break;
            case OWS_METADATA_UPDATE_STARTED:
                break;
            case OWS_METADATA_UPDATE_FINISHED:
                nonDataImportProgress += owsPublishImpact;
                break;
            case COMPLETED:
                nonDataImportProgress = owsPublishImpact + owsMdUpdateImpact + mdPublishImpact;
                break;
            default:
                throw new IllegalArgumentException();
            }
        }

        public void setImportProgress(double progress) {
            Assert.isTrue(progress >= 0d && progress <= 1d,
                    () -> "import progress must be between 0.0 and 1.0, got " + progress);
            if (step != DatasetPublishingStep.DATA_IMPORT_STARTED) {
                throw new IllegalStateException("Current step should be DATA_IMPORT_STARTED, but it's " + step);
            }
            this.dataImportProgress = progress;
        }

        public double getProgress() {
            double relativeDataImportProgress = this.dataImportProgress * this.featureImportImpact;
            return nonDataImportProgress + relativeDataImportProgress;
        }

        public static DatasetProgress valueOf(long totalFeatures, int featureCount) {
            final int nonImportStepsFeatureRelativeImpact = OWS_PUBLISH_RELATIVE_IMPACT
                    + METADATA_PUBLISH_RELATIVE_IMPACT + OWS_MD_UPDATE_RELATIVE_IMPACT;
            final long totalEffort = featureCount + nonImportStepsFeatureRelativeImpact;

            double featureImportImpact = featureCount / (double) totalEffort;
            double owsPublishImpact = OWS_PUBLISH_RELATIVE_IMPACT / (double) totalEffort;
            double mdPublishImpact = METADATA_PUBLISH_RELATIVE_IMPACT / (double) totalEffort;
            double owsMdUpdateImpact = OWS_MD_UPDATE_RELATIVE_IMPACT / (double) totalEffort;
            return new DatasetProgress(totalEffort, featureImportImpact, owsPublishImpact, mdPublishImpact,
                    owsMdUpdateImpact);
        }
    }

}
