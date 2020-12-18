package org.georchestra.datafeeder.api;

import java.util.Collections;

import org.georchestra.datafeeder.service.DataUploadService;
import org.georchestra.datafeeder.service.FileStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = { DataFeederApiConfiguration.class }, webEnvironment = WebEnvironment.MOCK)
@RunWith(SpringRunner.class)
@ActiveProfiles(value = { "georchestra", "test" })
public class FileUploadApiControllerSecurityTest {

    private @MockBean FileStorageService storageService;
    private @MockBean DataUploadService uploadService;
    private @MockBean ApiResponseMapper mapper;

    private @Autowired FileUploadApi controller;

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "testuser", roles = "INVALIDROLE")
    public void testUploadFilesInvalidUser() {
        controller.uploadFiles(Collections.emptyList());
    }

    @Test(expected = AuthenticationCredentialsNotFoundException.class)
    public void testUploadFiles_unauthenticated() {
        controller.uploadFiles(Collections.emptyList());
    }

    @Test(expected = AuthenticationCredentialsNotFoundException.class)
    public void testFindAllUploadJobs_unauthenticated() {
        controller.findAllUploadJobs();
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "testuser", roles = "USER")
    public void testFindAllUploadJobs_user_is_not_administrator() {
        controller.findAllUploadJobs();
    }

    @Test(expected = AuthenticationCredentialsNotFoundException.class)
    public void testFindUserUploadJobs_unauthenticated() {
        controller.findUserUploadJobs();
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "testuser", roles = "SOMEROLE")
    public void testFindUserUploadJobs_invalid_role() {
        controller.findUserUploadJobs();
    }

    @Test(expected = AuthenticationCredentialsNotFoundException.class)
    public void testRemoveJob_unauthenticated() {
        controller.removeJob(null, null);
    }

    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "testuser", roles = "SOMEROLE")
    public void testRemoveJob_invalid_role() {
        controller.removeJob(null, null);
    }
}
