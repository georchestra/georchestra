/*
 * Copyright (C) 2009-2025 by the geOrchestra PSC
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

import static org.georchestra.datafeeder.batch.publish.PublishJobProgressTracker.DatasetPublishingStep.COMPLETED;
import static org.georchestra.datafeeder.batch.publish.PublishJobProgressTracker.DatasetPublishingStep.DATA_IMPORT_FINISHED;
import static org.georchestra.datafeeder.batch.publish.PublishJobProgressTracker.DatasetPublishingStep.DATA_IMPORT_STARTED;
import static org.georchestra.datafeeder.batch.publish.PublishJobProgressTracker.DatasetPublishingStep.METADATA_PUBLISHING_FINISHED;
import static org.georchestra.datafeeder.batch.publish.PublishJobProgressTracker.DatasetPublishingStep.METADATA_PUBLISHING_STARTED;
import static org.georchestra.datafeeder.batch.publish.PublishJobProgressTracker.DatasetPublishingStep.OWS_METADATA_UPDATE_FINISHED;
import static org.georchestra.datafeeder.batch.publish.PublishJobProgressTracker.DatasetPublishingStep.OWS_METADATA_UPDATE_STARTED;
import static org.georchestra.datafeeder.batch.publish.PublishJobProgressTracker.DatasetPublishingStep.OWS_PUBLISHING_FINISHED;
import static org.georchestra.datafeeder.batch.publish.PublishJobProgressTracker.DatasetPublishingStep.OWS_PUBLISHING_STARTED;
import static org.georchestra.datafeeder.batch.publish.PublishJobProgressTracker.DatasetPublishingStep.SCHEDULED;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.georchestra.datafeeder.batch.publish.PublishJobProgressTracker.DatasetProgress;
import org.georchestra.datafeeder.batch.publish.PublishJobProgressTracker.JobProgress;
import org.georchestra.datafeeder.model.DataUploadJob;
import org.georchestra.datafeeder.model.DatasetUploadState;
import org.junit.Before;
import org.junit.Test;

public class PublishJobProgressTrackerTest {

    private PublishJobProgressTracker tracker;

    DataUploadJob job;

    private DatasetUploadState dataset1;

    private DatasetUploadState dataset2;

    public @Before void before() {
        tracker = new PublishJobProgressTracker();
        job = new DataUploadJob();
        job.setJobId(UUID.randomUUID());
        dataset1 = new DatasetUploadState();
        dataset2 = new DatasetUploadState();
        dataset1.setId(1L);
        dataset2.setId(2L);
        job.getDatasets().add(dataset1);
        job.getDatasets().add(dataset2);
    }

    @Test
    public void testInitialize_NoPublishableDatasets() {
        dataset1.getPublishing().setPublish(false);
        dataset2.getPublishing().setPublish(false);

        JobProgress progress = tracker.initialize(job);
        assertEquals(0d, progress.getProgress(), 1e-9);
        try {
            progress.getProgress(dataset1.getId());
            fail("expected IAE");
        } catch (IllegalArgumentException expected) {
            assertEquals("Progress for dataset 1 is not being tracked. Make sure it was a publishable dataset",
                    expected.getMessage());
        }
        try {
            progress.getProgress(dataset2.getId());
            fail("expected IAE");
        } catch (IllegalArgumentException expected) {
            assertEquals("Progress for dataset 2 is not being tracked. Make sure it was a publishable dataset",
                    expected.getMessage());
        }
    }

    @Test
    public void testInitialize() {
        JobProgress progress;

        dataset1.getPublishing().setPublish(true);
        dataset1.setFeatureCount(0);
        progress = tracker.initialize(job);
        assertEquals(300L, progress.totalEffort);
        assertEquals(300L, progress.getProgress(dataset1.getId()).getTotalEffort());

        dataset1.setFeatureCount(100);
        progress = tracker.initialize(job);
        assertEquals(400L, progress.totalEffort);
        assertEquals(400L, progress.getProgress(dataset1.getId()).getTotalEffort());

        dataset2.getPublishing().setPublish(true);
        dataset2.setFeatureCount(0);
        progress = tracker.initialize(job);
        assertEquals(700L, progress.totalEffort);
        assertEquals(300L, progress.getProgress(dataset2.getId()).getTotalEffort());

        dataset2.setFeatureCount(1_000);
        progress = tracker.initialize(job);
        assertEquals(1700L, progress.totalEffort);
        assertEquals(1300L, progress.getProgress(dataset2.getId()).getTotalEffort());
    }

    @Test
    public void testDispose() {
        tracker.initialize(job);
        assertNotNull(tracker.get(job.getJobId()));
        tracker.dispose(job.getJobId());
        try {
            tracker.get(job.getJobId());
            fail("expected IAE");
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), containsString("Progress for job"));
            assertThat(expected.getMessage(), containsString("is not being tracked"));
        }
    }

    @Test
    public void testGetProgress_singleDataset_zeroFeatures() {
        JobProgress progress;

        dataset1.getPublishing().setPublish(true);
        dataset1.setFeatureCount(0);
        progress = tracker.initialize(job);

        DatasetProgress dp = progress.getProgress(dataset1.getId());
        assertEquals(300, dp.getTotalEffort());
        assertEquals(SCHEDULED, dp.getStep());

        assertEquals(0d, dp.getProgress(), 1e-9);

        dp.setStep(DATA_IMPORT_STARTED);
        assertEquals(0d, dp.getProgress(), 1e-9);
        dp.setStep(DATA_IMPORT_FINISHED);

        dp.setStep(OWS_PUBLISHING_STARTED);
        assertEquals("no change expected upon started event", 0d, dp.getProgress(), 1e-9);
        dp.setStep(OWS_PUBLISHING_FINISHED);
        assertEquals(1 / 3d, dp.getProgress(), 1e-9);

        dp.setStep(METADATA_PUBLISHING_STARTED);
        assertEquals("no change expected upon started event", 1 / 3d, dp.getProgress(), 1e-9);
        dp.setStep(METADATA_PUBLISHING_FINISHED);
        assertEquals(2 / 3d, dp.getProgress(), 1e-9);

        dp.setStep(OWS_METADATA_UPDATE_STARTED);
        assertEquals("no change expected upon started event", 2 / 3d, dp.getProgress(), 1e-9);
        dp.setStep(OWS_METADATA_UPDATE_FINISHED);
        assertEquals(3 / 3d, dp.getProgress(), 1e-9);

        dp.setStep(COMPLETED);
        assertEquals(1.0, dp.getProgress(), 1e-9);
    }

    @Test
    public void testGetProgress_singleDataset() {
        JobProgress jobProgress;

        final int featureCount = 100;

        dataset1.getPublishing().setPublish(true);
        dataset1.setFeatureCount(featureCount);
        jobProgress = tracker.initialize(job);

        DatasetProgress dp = jobProgress.getProgress(dataset1.getId());
        assertEquals(400, dp.getTotalEffort());
        assertEquals(SCHEDULED, dp.getStep());

        assertEquals(0d, dp.getProgress(), 1e-9);

        dp.setStep(DATA_IMPORT_STARTED);
        assertEquals(0d, dp.getProgress(), 1e-9);

        dp.setImportProgress(0);
        assertEquals(0, dp.getProgress(), 1e-9);

        dp.setImportProgress(0.5);
        assertEquals(0.125, dp.getProgress(), 1e-9);
        assertEquals(0.125, jobProgress.getProgress(), 1e-9);

        dp.setImportProgress(1.0);
        dp.setStep(DATA_IMPORT_FINISHED);
        assertEquals(0.25, dp.getProgress(), 1e-9);
        assertEquals(0.25, jobProgress.getProgress(), 1e-9);

        dp.setStep(OWS_PUBLISHING_STARTED);
        assertEquals("no change expected upon started evend", 0.25, dp.getProgress(), 1e-9);
        dp.setStep(OWS_PUBLISHING_FINISHED);
        assertEquals(0.5, dp.getProgress(), 1e-9);
        assertEquals(0.5, jobProgress.getProgress(), 1e-9);

        dp.setStep(METADATA_PUBLISHING_STARTED);
        assertEquals("no change expected upon started evend", 0.5, dp.getProgress(), 1e-9);
        dp.setStep(METADATA_PUBLISHING_FINISHED);
        assertEquals(0.75, dp.getProgress(), 1e-9);
        assertEquals(0.75, jobProgress.getProgress(), 1e-9);

        dp.setStep(OWS_METADATA_UPDATE_STARTED);
        assertEquals("no change expected upon started evend", 0.75, dp.getProgress(), 1e-9);
        dp.setStep(OWS_METADATA_UPDATE_FINISHED);
        assertEquals(1.0, dp.getProgress(), 1e-9);
        assertEquals(1.0, jobProgress.getProgress(), 1e-9);

        dp.setStep(COMPLETED);
        assertEquals(1.0, dp.getProgress(), 1e-9);
        assertEquals(1.0, jobProgress.getProgress(), 1e-9);
    }

    @Test
    public void testGetProgress_multipleDatasets() {
        dataset1.getPublishing().setPublish(true);
        dataset2.getPublishing().setPublish(true);

        dataset1.setFeatureCount(100);
        dataset2.setFeatureCount(100);

        JobProgress jobProgress = tracker.initialize(job);
        DatasetProgress dp1 = jobProgress.getProgress(dataset1.getId());
        DatasetProgress dp2 = jobProgress.getProgress(dataset2.getId());
        assertEquals(SCHEDULED, dp1.getStep());
        assertEquals(SCHEDULED, dp2.getStep());

        final double totalEffort = 800;
        assertEquals(totalEffort, jobProgress.totalEffort, 1e-9);

        assertEquals(0d, jobProgress.getProgress(), 1e-9);

        /////
        dp1.setStep(OWS_PUBLISHING_STARTED);
        assertEquals("no change expected", 0d, dp1.getProgress(), 1e-9);

        dp2.setStep(OWS_PUBLISHING_STARTED);
        assertEquals("no change expected", 0d, dp2.getProgress(), 1e-9);

        dp1.setStep(OWS_PUBLISHING_FINISHED);
        assertEquals(0.25, dp1.getProgress(), 1e-9);
        assertEquals(0.125, jobProgress.getProgress(), 1e-9);

        dp2.setStep(OWS_PUBLISHING_FINISHED);
        assertEquals(0.25, dp2.getProgress(), 1e-9);
        assertEquals(0.25, jobProgress.getProgress(), 1e-9);

        /////

        dp1.setStep(METADATA_PUBLISHING_STARTED);
        assertEquals("no change expected", 0.25, dp1.getProgress(), 1e-9);

        dp2.setStep(METADATA_PUBLISHING_STARTED);
        assertEquals("no change expected", 0.25, dp2.getProgress(), 1e-9);

        dp1.setStep(METADATA_PUBLISHING_FINISHED);
        assertEquals(0.5, dp1.getProgress(), 1e-9);
        assertEquals(0.375, jobProgress.getProgress(), 1e-9);

        dp2.setStep(METADATA_PUBLISHING_FINISHED);
        assertEquals(0.5, dp2.getProgress(), 1e-9);
        assertEquals(0.5, jobProgress.getProgress(), 1e-9);

        /////

        dp1.setStep(OWS_METADATA_UPDATE_STARTED);
        assertEquals("no change expected", 0.5, dp1.getProgress(), 1e-9);

        dp2.setStep(OWS_METADATA_UPDATE_STARTED);
        assertEquals("no change expected", 0.5, dp2.getProgress(), 1e-9);

        dp1.setStep(OWS_METADATA_UPDATE_FINISHED);
        assertEquals(0.75, dp1.getProgress(), 1e-9);
        assertEquals(0.625, jobProgress.getProgress(), 1e-9);

        dp2.setStep(OWS_METADATA_UPDATE_FINISHED);
        assertEquals(0.75, dp2.getProgress(), 1e-9);
        assertEquals(0.75, jobProgress.getProgress(), 1e-9);

        ///////////

        dp1.setStep(DATA_IMPORT_STARTED);
        dp2.setStep(DATA_IMPORT_STARTED);
        assertEquals("no change expected", 0.75, jobProgress.getProgress(), 1e-9);

        dp1.setImportProgress(0.5);
        assertEquals(0.875, dp1.getProgress(), 1e-9);
        assertEquals(0.75 + 0.125 / 2, jobProgress.getProgress(), 1e-9);

        dp2.setImportProgress(0.5);
        assertEquals(0.875, dp2.getProgress(), 1e-9);
        assertEquals(0.875, jobProgress.getProgress(), 1e-9);

        dp1.setImportProgress(1.0);
        assertEquals(1, dp1.getProgress(), 1e-9);
        assertEquals(1 - 0.125 / 2, jobProgress.getProgress(), 1e-9);

        dp2.setImportProgress(1.0);
        assertEquals(1, dp2.getProgress(), 1e-9);
        assertEquals(1, jobProgress.getProgress(), 1e-9);

        dp1.setStep(DATA_IMPORT_FINISHED);
        dp2.setStep(DATA_IMPORT_FINISHED);
        assertEquals(1, dp1.getProgress(), 1e-9);
        assertEquals(1, dp2.getProgress(), 1e-9);
        assertEquals(1, jobProgress.getProgress(), 1e-9);
    }
}
