package nz.ac.canterbury.seng302.homehelper.integration.service;

import nz.ac.canterbury.seng302.homehelper.controller.UploadController;
import nz.ac.canterbury.seng302.homehelper.entity.user.User;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@SpringBootTest
public class FileUploadIntegrationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UploadController uploadController;

    @BeforeEach
    public void setUp() {

    }

    /**
     * Successfully upload valid image and save path to database
     * (with help from ChatGPT to prepare the necessary data)
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws IllegalArgumentException
     */
    @Test
    @Transactional
    void saveImage_IntegrationTest_Success() throws IOException, IllegalArgumentException, InvalidKeySpecException, NoSuchAlgorithmException {
        // Prepare test data
        User user = new User("First", "Last", "t@e.st", "Abc123!!", "Abc123!!");
        userService.addUser(user);
        // Might need these later, leaving in for now
        // byte[] imageBytes = { (byte) 137, 80, 78, 71, 13, 10, 26, 10 };
        // MultipartFile file = new MockMultipartFile("testImage.png", "testImage.png", "image/png", imageBytes);
        String id = String.valueOf(userService.findUserByEmail("t@e.st").getId());
        Path fileNameAndPath = Paths.get("static", "images", id + ".png");

        MultipartFile file = mock(MultipartFile.class);

        InputStream inputStream = getClass().getResourceAsStream("/test-files/PlaceholderIcon.png");
        Mockito.when(file.getBytes()).thenReturn(inputStream.readAllBytes());

        // Upload
        boolean result = userService.saveFile(file, fileNameAndPath, id);

        // Verify
        assertTrue(result);
        String savedPath = userRepository.getUserImage("t@e.st").get(0);
        Path correctPath = Paths.get("images", id + ".png");
        assertTrue(Files.exists(fileNameAndPath));
        assertEquals(correctPath.toString(), savedPath);

        // Cleanup
        Files.deleteIfExists(fileNameAndPath);
    }

}
