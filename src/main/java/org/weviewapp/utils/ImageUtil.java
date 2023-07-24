package org.weviewapp.utils;

import com.google.gson.Gson;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.weviewapp.dto.ClassificationResult;
import org.weviewapp.enums.ImageCategory;
import org.weviewapp.exception.WeviewAPIException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

public final class ImageUtil {
    private static final String projectRoot = System.getProperty("user.dir");
    private static final String imagesFolderPath = projectRoot + "/images/";
    public static String uploadImage(MultipartFile file, ImageCategory category) {
        try {
            // Get the file name and extension
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = FilenameUtils.getExtension(fileName);

            // Generate a unique file name
            String uniqueFileName = category.name() + "_" + UUID.randomUUID() + "." + fileExtension;

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
        }
    }

    public static byte[] loadImage(String fileName) throws IOException {
        try {
            File img = new File(imagesFolderPath + fileName);
            return Files.readAllBytes(img.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void deleteImage(String fileName) {
        if(!fileName.isEmpty()) {
            try {
                // Set the delete directory path
                String deleteDirectory = imagesFolderPath + File.separator;

                // Create the delete directory if it doesn't exist
                File directory = new File(deleteDirectory);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Delete the file from the delete directory
                Path filePath = Paths.get(deleteDirectory + fileName);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String imageAPICheck(String img, int i, CloseableHttpClient httpClient) throws IOException {
        String apiUrl = "http://localhost:5000/api/image";
        HttpPost request = new HttpPost(apiUrl);
        String requestBody = "{\"image\":\"" + img + "\"}";
        StringEntity params = new StringEntity(requestBody);
        request.addHeader("content-type", "application/json");
        request.setEntity(params);

        HttpResponse response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        String responseData = EntityUtils.toString(response.getEntity());

        if (statusCode >= 200 && statusCode < 300) {
            Gson gson = new Gson();

            ClassificationResult[] results = gson.fromJson(responseData, ClassificationResult[].class);
            double safeProbability = 0.0;
            for (ClassificationResult classifications : results) {
                if (classifications.getLabel().equals("safe")) {
                    safeProbability = classifications.getScore();
                    if (safeProbability > 0.5) {
                        return "";
                    }

                    Arrays.sort(results, (a, b) -> Double.compare(b.getScore(), a.getScore()));
                    double totalScore = 0;
                    for (ClassificationResult result : results) {
                        totalScore += result.getScore();
                    }

                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Image " + i + ":\n");
                    for (ClassificationResult result : results) {
                        double percentage = (result.getScore() / totalScore) * 100;
                        stringBuilder.append("- ")
                                .append(result.getLabel().toUpperCase())
                                .append(": ")
                                .append(String.format("%.2f%%", percentage))
                                .append("\n");
                    }
                    return stringBuilder.toString();
                }
            }
        } else {
            throw new WeviewAPIException(HttpStatus.BAD_REQUEST, "Image verification API failed! Error: " + responseData);
        }
        return "";
    }
}
