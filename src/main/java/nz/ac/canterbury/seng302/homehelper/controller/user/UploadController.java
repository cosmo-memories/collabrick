package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.controller.advice.UserAdvice;
import nz.ac.canterbury.seng302.homehelper.service.user.UserService;
import nz.ac.canterbury.seng302.homehelper.validation.user.ImageValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller for file uploads
 * With significant reference to: https://www.baeldung.com/spring-boot-thymeleaf-image-upload
 */
@Controller
public class UploadController {

    public static String UPLOAD_DIRECTORY = Paths.get("static", "images").toString();
    private final UserService userService;
    Logger logger = LoggerFactory.getLogger(UserAdvice.class);

    @Autowired
    public UploadController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint for uploading user profile images
     *
     * @param model Model (for saving error messages)
     * @param file  File to be uploaded
     * @return Redirect to userPage
     * @throws IOException Something went wrong
     */
    @PostMapping("/iconUpload")
    public String uploadImage(Model model, @RequestParam("image") MultipartFile file) throws IOException {
        logger.info("POST: /iconUpload");

        // Check if the user hit upload without selecting a file
        if (!ImageValidation.checkFileExists(file)) {
            model.addAttribute("uploadError", "Please select a file to upload.");
            return "pages/user/editUserPage";
        }

        // Check if file is too large
        if (!ImageValidation.checkFileSize(file)) {
            model.addAttribute("uploadError", "Image must be less than 10MB.");
            return "pages/user/editUserPage";
        }

        // Check if file format is allowed (png, jpg, svg)
        if (!ImageValidation.checkFileExtension(file)) {
            model.addAttribute("uploadError", "Image must be of type png, jpg, or svg.");
            return "pages/user/editUserPage";
        }

        // Get the user's id from the security context
        String id = SecurityContextHolder.getContext().getAuthentication().getName();

        // Create unique file name for image
        Path fileNameAndPath = ImageValidation.createFilePath(file, id);

        // Create upload directory if it does not exist
        Files.createDirectories(Paths.get(UPLOAD_DIRECTORY));

        // Save file to directory; automatically overwrites old file if it exists
        boolean result = userService.saveFile(file, fileNameAndPath, id);
        if (!result) {
            // Update failed for some reason
            model.addAttribute("uploadError", "There was an error adding your image to the database.");
            return "pages/user/editUserPage";
        }

        // Redirect to make sure image is updated on the page immediately
        return "redirect:/userPage";
    }
}