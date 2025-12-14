package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.controller.UploadController;
import nz.ac.canterbury.seng302.homehelper.repository.user.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.validation.user.ImageValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Tests for user profile picture upload validation
 * Test images are located in src/test/resources/test-files
 */
public class FileUploadValidationTests {

    MultipartFile file = mock(MultipartFile.class);
    @Mock
    private UserRepository userRepository;
    private UploadController uploadController;
    private UserService userService;

    /**
     * Run before each test
     */
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        uploadController = new UploadController(userService);
    }

    /**
     * Confirm that the file exists
     */
    @Test
    void checkFileExists_ValidFile_ReturnTrue() {
        Mockito.when(file.isEmpty()).thenReturn(false);
        boolean result = ImageValidation.checkFileExists(file);
        assertTrue(result);
    }

    /**
     * Properly confirm that user did not select a file
     */
    @Test
    void checkFileExists_EmptyFile_ReturnFalse() {
        Mockito.when(file.isEmpty()).thenReturn(true);
        boolean result = ImageValidation.checkFileExists(file);
        assertFalse(result);
    }

    /**
     * Check 9MB file is correctly accepted
     */
    @Test
    void checkFileSize_LessThan10MB_ReturnTrue() {
        Mockito.when(file.getSize()).thenReturn(9L * 1024 * 1024);
        boolean result = ImageValidation.checkFileSize(file);
        assertTrue(result);
    }

    /**
     * Check 10MB file is correctly accepted
     */
    @Test
    void checkFileSize_Exactly10MB_ReturnTrue() {
        Mockito.when(file.getSize()).thenReturn(10L * 1024 * 1024);
        boolean result = ImageValidation.checkFileSize(file);
        assertTrue(result);
    }

    /**
     * Check 11MB file is correctly rejected
     */
    @Test
    void checkFileSize_Over10MB_ReturnFalse() {
        Mockito.when(file.getSize()).thenReturn(11L * 1024 * 1024);
        boolean result = ImageValidation.checkFileSize(file);
        assertFalse(result);
    }

    /**
     * Check 110MB file is correctly rejected
     */
    @Test
    void checkFileSize_WayOver10MB_ReturnFalse() {
        Mockito.when(file.getSize()).thenReturn(110L * 1024 * 1024);
        boolean result = ImageValidation.checkFileSize(file);
        assertFalse(result);
    }

    /**
     * Check .png file is accepted
     */
    @Test
    void checkFileExtension_PNG_ReturnTrue() throws IOException {
        Mockito.when(file.getOriginalFilename()).thenReturn("PlaceholderIcon.png");
        InputStream inputStream = getClass().getResourceAsStream("/test-files/PlaceholderIcon.png");
        Mockito.when(file.getBytes()).thenReturn(inputStream.readAllBytes());
        boolean result = ImageValidation.checkFileExtension(file);
        assertTrue(result);
    }

    /**
     * Check filename with .PNG (in caps) is accepted
     */
    @Test
    void checkFileExtension_AllCaps_ReturnTrue() throws IOException {
        Mockito.when(file.getOriginalFilename()).thenReturn("PlaceholderIcon2.PNG");
        InputStream inputStream = getClass().getResourceAsStream("/test-files/PlaceholderIcon2.PNG");
        Mockito.when(file.getBytes()).thenReturn(inputStream.readAllBytes());
        boolean result = ImageValidation.checkFileExtension(file);
        assertTrue(result);
    }

    /**
     * Check .jpg file is accepted
     */
    @Test
    void checkFileExtension_JPG_ReturnTrue() throws IOException {
        Mockito.when(file.getOriginalFilename()).thenReturn("PlaceholderIcon.jpg");
        InputStream inputStream = getClass().getResourceAsStream("/test-files/PlaceholderIcon.jpg");
        Mockito.when(file.getBytes()).thenReturn(inputStream.readAllBytes());
        boolean result = ImageValidation.checkFileExtension(file);
        assertTrue(result);
    }

    /**
     * Check .svg file is accepted
     */
    @Test
    void checkFileExtension_SVG_ReturnTrue() throws IOException {
        Mockito.when(file.getOriginalFilename()).thenReturn("SVG_logo.svg");
        InputStream inputStream = getClass().getResourceAsStream("/test-files/SVG_logo.svg");
        Mockito.when(file.getBytes()).thenReturn(inputStream.readAllBytes());
        boolean result = ImageValidation.checkFileExtension(file);
        assertTrue(result);
    }

    /**
     * Check .zip file is rejected
     */
    @Test
    void checkFileExtension_ZIP_ReturnFalse() throws IOException {
        Mockito.when(file.getOriginalFilename()).thenReturn("TestZIP.zip");
        InputStream inputStream = getClass().getResourceAsStream("/test-files/TestZIP.zip");
        Mockito.when(file.getBytes()).thenReturn(inputStream.readAllBytes());
        boolean result = ImageValidation.checkFileExtension(file);
        assertFalse(result);
    }

    /**
     * Check renamed .zip file is rejected
     */
    @Test
    void checkFileExtension_RenamedZIP_ReturnFalse() throws IOException {
        Mockito.when(file.getOriginalFilename()).thenReturn("TestZIP.png");
        InputStream inputStream = getClass().getResourceAsStream("/test-files/TestZIP.png");
        Mockito.when(file.getBytes()).thenReturn(inputStream.readAllBytes());
        boolean result = ImageValidation.checkFileExtension(file);
        assertFalse(result);
    }

    /**
     * Check .pdf is rejected
     */
    @Test
    void checkFileExtension_PDF_ReturnFalse() throws IOException {
        Mockito.when(file.getOriginalFilename()).thenReturn("TestPDF.pdf");
        InputStream inputStream = getClass().getResourceAsStream("/test-files/TestPDF.pdf");
        Mockito.when(file.getBytes()).thenReturn(inputStream.readAllBytes());
        boolean result = ImageValidation.checkFileExtension(file);
        assertFalse(result);
    }

    /**
     * Check png with no extension is accepted
     */
    @Test
    void checkFileExtension_NoExtension_ReturnTrue() throws IOException {
        Mockito.when(file.getOriginalFilename()).thenReturn("PlaceholderIcon");
        InputStream inputStream = getClass().getResourceAsStream("/test-files/PlaceholderIcon");
        Mockito.when(file.getBytes()).thenReturn(inputStream.readAllBytes());
        boolean result = ImageValidation.checkFileExtension(file);
        assertTrue(result);
    }

    /**
     * Test expected filepath is generated for PNG
     */
    @Test
    void createFilePath_ValidIDPNG_Success() throws IOException {
        Mockito.when(file.getOriginalFilename()).thenReturn("PlaceholderIcon.png");
        InputStream inputStream = getClass().getResourceAsStream("/test-files/PlaceholderIcon.png");
        Mockito.when(file.getBytes()).thenReturn(inputStream.readAllBytes());
        String id = "1";
        String result = ImageValidation.createFilePath(file, id).toString();
        assertEquals("static" + File.separator + "images" + File.separator + "1.png", result);
    }

    /**
     * Test expected filepath is generated for JPG
     */
    @Test
    void createFilePath_ValidIDJPG_Success() throws IOException {
        Mockito.when(file.getOriginalFilename()).thenReturn("PlaceholderIcon.jpg");
        InputStream inputStream = getClass().getResourceAsStream("/test-files/PlaceholderIcon.jpg");
        Mockito.when(file.getBytes()).thenReturn(inputStream.readAllBytes());
        String id = "2";
        String result = ImageValidation.createFilePath(file, id).toString();
        assertEquals("static" + File.separator + "images" + File.separator + "2.jpeg", result);

    }

}
