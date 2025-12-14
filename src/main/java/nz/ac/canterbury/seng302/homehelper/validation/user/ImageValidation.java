package nz.ac.canterbury.seng302.homehelper.validation.user;

import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class ImageValidation {

    public static String UPLOAD_DIRECTORY = Paths.get("static", "images").toString();

    /**
     * Check if a file was actually uploaded or if the user preemptively pressed submit
     *
     * @param file File to check
     * @return Boolean result of check
     */
    public static boolean checkFileExists(MultipartFile file) {
        return !file.isEmpty();
    }

    /**
     * Check uploaded file is less than 10MB
     *
     * @param file File to check
     * @return Boolean result of check
     */
    public static boolean checkFileSize(MultipartFile file) {
        return file.getSize() <= 10 * 1024 * 1024;
    }

    /**
     * Check extension of uploaded file is allowed (i.e. png, jpg/jpeg, or svg)
     * Now uses Apache Tika for better type checking
     *
     * @param file File to check
     * @return Boolean result of check
     */
    public static boolean checkFileExtension(MultipartFile file) {
        // Get file type
        Tika tika = new Tika();
        String type = null;
        try {
            type = tika.detect(file.getBytes(), file.getOriginalFilename());
        } catch (IOException e) {
            // Something went wrong, abort
            return false;
        }

        // Check if file format is allowed
        Set<String> allowedFiletypes = Set.of("image/png", "image/jpeg", "image/svg+xml");
        return allowedFiletypes.contains(type.toLowerCase());
    }

    /**
     * Generates a unique file path for the user based on their ID number
     * Since IDs are unique, this ensures they will not overwrite another user's image
     *
     * @param file User's uploaded file
     * @param id   User's ID number
     * @return File path to save image to
     */
    public static Path createFilePath(MultipartFile file, String id) throws IOException {
        // Get file extension
        Tika tika = new Tika();
        String type = null;
        type = tika.detect(file.getBytes(), file.getOriginalFilename());
        String ext = type.split("/")[1];
        if (ext.equals("svg+xml")) {
            ext = "svg";
        }
        // Create new filepath
        return Paths.get(UPLOAD_DIRECTORY, id + "." + ext);
    }

}
