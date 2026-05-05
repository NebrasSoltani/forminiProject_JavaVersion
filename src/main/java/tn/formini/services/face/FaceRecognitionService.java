package tn.formini.services.face;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for facial recognition using OpenCV
 * Handles face detection, encoding, and recognition
 */
public class FaceRecognitionService {
    
    private static FaceRecognitionService instance;
    private CascadeClassifier faceDetector;
    private boolean initialized = false;
    
    private static final String CASCADE_PATH = "haarcascade_frontalface_alt.xml";
    private static final String MODELS_DIR = "face_models";
    
    private FaceRecognitionService() {
        initialize();
    }
    
    public static synchronized FaceRecognitionService getInstance() {
        if (instance == null) {
            instance = new FaceRecognitionService();
        }
        return instance;
    }
    
    private void initialize() {
        try {
            // Load OpenCV native library
            OpenCV.loadShared();

            // Load face cascade classifier
            File cascadeFile = new File(CASCADE_PATH);
            if (!cascadeFile.exists()) {
                // Try to load from resources
                cascadeFile = extractCascadeFromResources();
            }

            if (cascadeFile != null && cascadeFile.exists()) {
                faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());
                if (faceDetector.empty()) {
                    System.err.println("Failed to load face cascade classifier");
                }
            } else {
                System.err.println("Face cascade file not found: " + CASCADE_PATH);
            }

            initialized = true;
            System.out.println("FaceRecognitionService initialized successfully");

        } catch (Exception e) {
            System.err.println("Failed to initialize FaceRecognitionService: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private File extractCascadeFromResources() {
        try {
            // Create models directory if it doesn't exist
            File modelsDir = new File(MODELS_DIR);
            if (!modelsDir.exists()) {
                modelsDir.mkdirs();
            }
            
            // Copy cascade file from resources if available
            InputStream is = getClass().getClassLoader().getResourceAsStream("haarcascade_frontalface_alt.xml");
            if (is != null) {
                File targetFile = new File(MODELS_DIR + "/haarcascade_frontalface_alt.xml");
                Files.copy(is, targetFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return targetFile;
            }
        } catch (Exception e) {
            System.err.println("Failed to extract cascade from resources: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Detect faces in an image
     * @param imageFilePath Path to the image file
     * @return List of Rect representing detected faces
     */
    public List<Rect> detectFaces(String imageFilePath) {
        List<Rect> faces = new ArrayList<>();
        
        if (!initialized || faceDetector == null) {
            System.err.println("FaceRecognitionService not initialized");
            return faces;
        }
        
        try {
            Mat image = Imgcodecs.imread(imageFilePath);
            if (image.empty()) {
                System.err.println("Failed to load image: " + imageFilePath);
                return faces;
            }
            
            // Convert to grayscale
            Mat grayImage = new Mat();
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(grayImage, grayImage);
            
            // Detect faces
            MatOfRect faceDetections = new MatOfRect();
            faceDetector.detectMultiScale(grayImage, faceDetections, 1.1, 3, 
                Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30), new Size());
            
            for (Rect rect : faceDetections.toArray()) {
                faces.add(rect);
            }
            
            // Release resources
            grayImage.release();
            image.release();
            
        } catch (Exception e) {
            System.err.println("Error detecting faces: " + e.getMessage());
            e.printStackTrace();
        }
        
        return faces;
    }
    
    /**
     * Extract face encoding from an image
     * @param imageFilePath Path to the image file
     * @return Face encoding as byte array, or null if no face detected
     */
    public byte[] extractFaceEncoding(String imageFilePath) {
        if (!initialized) {
            return null;
        }
        
        try {
            List<Rect> faces = detectFaces(imageFilePath);
            if (faces.isEmpty()) {
                System.err.println("No face detected in image");
                return null;
            }
            
            // Use the first detected face
            Rect faceRect = faces.get(0);
            
            Mat image = Imgcodecs.imread(imageFilePath);
            Mat grayImage = new Mat();
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
            
            // Extract face region
            Mat faceRegion = new Mat(grayImage, faceRect);
            
            // Resize to standard size for recognition
            Mat resizedFace = new Mat();
            Size standardSize = new Size(100, 100);
            Imgproc.resize(faceRegion, resizedFace, standardSize);
            
            // Convert to byte array
            byte[] encoding = new byte[(int) resizedFace.total() * resizedFace.channels()];
            resizedFace.get(0, 0, encoding);
            
            // Release resources
            resizedFace.release();
            faceRegion.release();
            grayImage.release();
            image.release();
            
            return encoding;
            
        } catch (Exception e) {
            System.err.println("Error extracting face encoding: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Train the face recognizer with face encodings
     * @param faceEncodings List of face encodings
     * @param labels Corresponding user IDs
     * NOTE: Face recognition training requires opencv_contrib modules which are not included
     * This method is a placeholder for future implementation with proper OpenCV build
     */
    public void trainRecognizer(List<Mat> faceEncodings, List<Integer> labels) {
        System.err.println("Face recognition training requires opencv_contrib modules");
        System.err.println("Please use a custom OpenCV build with opencv_contrib or implement alternative recognition");
    }
    
    /**
     * Recognize a face from an image
     * @param imageFilePath Path to the image file
     * @return User ID if recognized, -1 otherwise
     * NOTE: Face recognition requires opencv_contrib modules which are not included
     * This method is a placeholder for future implementation with proper OpenCV build
     */
    public int recognizeFace(String imageFilePath) {
        System.err.println("Face recognition requires opencv_contrib modules");
        System.err.println("Please use a custom OpenCV build with opencv_contrib or implement alternative recognition");
        return -1;
    }
    
    /**
     * Compare two face encodings for similarity
     * @param encoding1 First face encoding
     * @param encoding2 Second face encoding
     * @return Similarity score (0-1, higher is more similar)
     */
    public double compareFaces(byte[] encoding1, byte[] encoding2) {
        if (encoding1 == null || encoding2 == null || encoding1.length != encoding2.length) {
            return 0.0;
        }
        
        try {
            // Calculate Euclidean distance
            double sum = 0;
            for (int i = 0; i < encoding1.length; i++) {
                double diff = (encoding1[i] & 0xFF) - (encoding2[i] & 0xFF);
                sum += diff * diff;
            }
            double distance = Math.sqrt(sum);
            
            // Convert distance to similarity score (0-1)
            double maxDistance = Math.sqrt(255 * 255 * encoding1.length);
            double similarity = 1.0 - (distance / maxDistance);
            
            return Math.max(0.0, Math.min(1.0, similarity));
            
        } catch (Exception e) {
            System.err.println("Error comparing faces: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Check if a face encoding matches a stored encoding with given threshold
     * @param encoding1 First face encoding
     * @param encoding2 Second face encoding
     * @param threshold Similarity threshold (0-1)
     * @return true if faces match above threshold
     */
    public boolean facesMatch(byte[] encoding1, byte[] encoding2, double threshold) {
        double similarity = compareFaces(encoding1, encoding2);
        return similarity >= threshold;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
}
