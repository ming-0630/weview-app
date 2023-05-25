package utils;

import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.weviewapp.enums.ImageCategory;
import org.weviewapp.exception.WeviewAPIException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class ImageUtil {
    private static final String projectRoot = System.getProperty("user.dir");
    private static final String imagesFolderPath = projectRoot + "/images/";
    public static String uploadImage(MultipartFile file, ImageCategory category, Integer index) {
        try {
            // Get the file name and extension
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = FilenameUtils.getExtension(fileName);

            // Generate a unique file name
            String uniqueFileName;
            if (index == null) {
                uniqueFileName = category.name() + "_" + UUID.randomUUID() + "." + fileExtension;
            } else {
                uniqueFileName = category.name() + "_" + UUID.randomUUID() + "_" + index +  "." + fileExtension;
            }


            // Set the upload directory path
            String uploadDirectory = imagesFolderPath + File.separator;

            // Create the upload directory if it doesn't exist
            File directory = new File(uploadDirectory);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Save the file to the upload directory
            Path filePath = Paths.get(uploadDirectory + uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return the file path or any other response as needed
//            String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
//                    .path("/api/images/")
//                    .path(uniqueFileName)
//                    .toUriString();

            return uniqueFileName;
//            return ResponseEntity.ok(fileUrl);
        } catch (IOException e) {
                throw new WeviewAPIException(HttpStatus.BAD_REQUEST ,"Upload failed for " + file.getOriginalFilename());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload the file.");
        }
    }

    public static String uploadImage(MultipartFile file, ImageCategory category) {
        return uploadImage(file, category, null);
    }

    public static byte[] loadImage(String fileName) throws IOException {
        String projectRoot = System.getProperty("user.dir");
        String imagesFolderPath = projectRoot + "/images/";

        try {
            File img = new File(imagesFolderPath + fileName);
            return Files.readAllBytes(img.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
