package tn.formini.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class FileUploadService {

    private static final String UPLOAD_DIR = "uploads";
    private static final String PHOTO_DIR = "photos";
    private static final String CV_DIR = "cv";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final Path baseUploadPath;

    public FileUploadService() {
        // Create uploads directory in project root
        this.baseUploadPath = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR);
        initializeDirectories();
    }

    public FileUploadService(String customUploadDir) {
        this.baseUploadPath = Paths.get(customUploadDir);
        initializeDirectories();
    }

    private void initializeDirectories() {
        try {
            Files.createDirectories(baseUploadPath);
            Files.createDirectories(baseUploadPath.resolve(PHOTO_DIR));
            Files.createDirectories(baseUploadPath.resolve(CV_DIR));
            System.out.println("Upload directories initialized: " + baseUploadPath);
        } catch (IOException e) {
            System.err.println("Failed to create upload directories: " + e.getMessage());
        }
    }

    /**
     * Upload a photo file and return the relative path
     * @param sourceFile The file to upload
     * @return Relative path to the uploaded file, or null if failed
     */
    public String uploadPhoto(File sourceFile) {
        return uploadFile(sourceFile, PHOTO_DIR, "photo");
    }

    /**
     * Upload a CV file and return the relative path
     * @param sourceFile The file to upload
     * @return Relative path to the uploaded file, or null if failed
     */
    public String uploadCv(File sourceFile) {
        return uploadFile(sourceFile, CV_DIR, "cv");
    }

    /**
     * Generic file upload method
     * @param sourceFile The file to upload
     * @param subDirectory Subdirectory (photos, cv, etc.)
     * @param prefix File name prefix
     * @return Relative path to the uploaded file, or null if failed
     */
    private String uploadFile(File sourceFile, String subDirectory, String prefix) {
        if (sourceFile == null || !sourceFile.exists()) {
            System.err.println("File does not exist: " + sourceFile);
            return null;
        }

        // Check file size
        if (sourceFile.length() > MAX_FILE_SIZE) {
            System.err.println("File too large: " + sourceFile.length() + " bytes (max: " + MAX_FILE_SIZE + ")");
            return null;
        }

        try {
            // Generate unique filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            String originalExtension = getFileExtension(sourceFile.getName());
            String newFileName = prefix + "_" + timestamp + "_" + uuid + originalExtension;

            Path targetDir = baseUploadPath.resolve(subDirectory);
            Path targetPath = targetDir.resolve(newFileName);

            // Copy file
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path for storage in database
            String relativePath = UPLOAD_DIR + "/" + subDirectory + "/" + newFileName;
            System.out.println("File uploaded successfully: " + relativePath);
            return relativePath;

        } catch (IOException e) {
            System.err.println("Failed to upload file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get file extension from filename
     * @param filename The filename
     * @return File extension including dot (e.g., ".jpg")
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot);
        }
        return "";
    }

    /**
     * Get absolute path from relative path
     * @param relativePath Relative path from upload directory
     * @return Absolute File object
     */
    public File getFile(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        Path absolutePath = Paths.get(System.getProperty("user.dir"), relativePath);
        File file = absolutePath.toFile();
        return file.exists() ? file : null;
    }

    /**
     * Delete a file by its relative path
     * @param relativePath Relative path to the file
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return false;
        }
        try {
            Path absolutePath = Paths.get(System.getProperty("user.dir"), relativePath);
            if (Files.exists(absolutePath)) {
                Files.delete(absolutePath);
                System.out.println("File deleted: " + relativePath);
                return true;
            }
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get the base upload directory path
     * @return Path to base upload directory
     */
    public Path getBaseUploadPath() {
        return baseUploadPath;
    }
}
