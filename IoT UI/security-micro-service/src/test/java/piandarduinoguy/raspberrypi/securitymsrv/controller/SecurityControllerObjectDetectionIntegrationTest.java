package piandarduinoguy.raspberrypi.securitymsrv.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import piandarduinoguy.raspberrypi.securitymsrv.TestUtils;
import piandarduinoguy.raspberrypi.securitymsrv.domain.Problem;
import piandarduinoguy.raspberrypi.securitymsrv.service.SecurityService;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
class SecurityControllerObjectDetectionIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("Given a valid multipart file " +
            "when post to the /object-detect endpoint is made " +
            "then the image file gets saved at the expected location.")
    @Test
    void canUploadNewCaptureForProcessing() throws Exception {
        MockMultipartFile image = TestUtils.createMockMultipartFile();

        mockMvc.perform(
                multipart("/object-detect").file(image)).
                andExpect(status().isAccepted());

        testUtils.assertThatExpectedImageUploaded(image);
        // TODO: assertion that we can perform detection after it has been temporarily saved

        testUtils.deleteUploadedFileIfExists();
    }

    @DisplayName("Given an IOException thrown " +
            "when post to the /object-detect endpoint is made " +
            "then return expected Zalando problem")
    @Test
    void canReturnZalandoProblemIfSaveImageMethodThrowsIoException() throws Exception {
        MockMultipartFile image = TestUtils.createMockMultipartFile();

        try (MockedStatic<FileUtils> mockFileUtils = mockStatic(FileUtils.class)) {
            mockFileUtils.when(() -> FileUtils.writeByteArrayToFile(any(), any())).thenThrow(new IOException("I am an IOException"));
            MvcResult mvcResult = mockMvc.perform(
                    multipart("/object-detect").file(image)).
                    andExpect(status().isInternalServerError())
                    .andReturn();
            String actualZalandoProblemJsonString = mvcResult.getResponse().getContentAsString();
            Problem expectedZalandoProblem = createExpectedZalandoProblem("The image image could not be saved to the directory src/test/resources/application/. An IOException was thrown with message \"I am an IOException\".");
            String expectedZalandoProblemJsonString = objectMapper.writeValueAsString(expectedZalandoProblem);
            assertThat(actualZalandoProblemJsonString).isEqualToIgnoringCase(expectedZalandoProblemJsonString);
        }

        testUtils.deleteUploadedFileIfExists();
    }

    @DisplayName("Given an IOException thrown " +
            "when get to the /annotated-image endpoint is made " +
            "then return expected Zalando problem")
    @Test
    void canReturnZalandoProblemIfGetAnnotatedImageMethodThrowsIoException() throws Exception {
        testUtils.createExpectedAnnotatedImageFile();

        try (MockedStatic<FileUtils> mockFileUtils = mockStatic(FileUtils.class)) {
            mockFileUtils.when(() -> FileUtils.readFileToByteArray(any())).thenThrow(new IOException("I am an IOException"));
            MvcResult mvcResult = mockMvc.perform(
                    get("/annotated-image"))
                    .andReturn();
            String actualZalandoProblemJsonString = mvcResult.getResponse().getContentAsString();
            Problem expectedZalandoProblem = createExpectedZalandoProblem("The image test_new_capture_annotated.jpeg could not be encoded to base64 string due to an IOException being thrown with message \"I am an IOException\".");
            String expectedZalandoProblemJsonString = objectMapper.writeValueAsString(expectedZalandoProblem);
            assertThat(actualZalandoProblemJsonString).isEqualToIgnoringCase(expectedZalandoProblemJsonString);
        }

        testUtils.deleteAnnotatedImage();
    }

    private Problem createExpectedZalandoProblem(String detail) {
        Problem expectedZalandoProblem = new Problem();
        expectedZalandoProblem.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        expectedZalandoProblem.setDetail(detail);
        expectedZalandoProblem.setTitle(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        return expectedZalandoProblem;
    }
}
