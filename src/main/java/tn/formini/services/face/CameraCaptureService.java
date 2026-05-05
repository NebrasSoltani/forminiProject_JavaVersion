package tn.formini.services.face;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Service for capturing video from webcam and converting to JavaFX images
 */
public class CameraCaptureService {
    
    private static CameraCaptureService instance;
    private VideoCapture camera;
    private boolean isRunning = false;
    private ImageView imageView;
    private CaptureTask captureTask;
    private Thread captureThread;
    
    private CameraCaptureService() {
        // Load OpenCV native library
        try {
            OpenCV.loadShared();
        } catch (Exception e) {
            System.err.println("Failed to load OpenCV: " + e.getMessage());
        }
    }
    
    public static synchronized CameraCaptureService getInstance() {
        if (instance == null) {
            instance = new CameraCaptureService();
        }
        return instance;
    }
    
    /**
     * Start capturing from webcam
     * @param cameraIndex Camera index (usually 0 for default webcam)
     * @param imageView ImageView to display the video feed
     * @return true if camera started successfully
     */
    public boolean startCamera(int cameraIndex, ImageView imageView) {
        if (isRunning) {
            System.err.println("Camera is already running");
            return false;
        }
        
        this.imageView = imageView;
        
        try {
            camera = new VideoCapture(cameraIndex);
            
            if (!camera.isOpened()) {
                System.err.println("Failed to open camera at index " + cameraIndex);
                return false;
            }
            
            isRunning = true;
            captureTask = new CaptureTask();
            captureThread = new Thread(captureTask);
            captureThread.setDaemon(true);
            captureThread.start();
            
            System.out.println("Camera started successfully");
            return true;
            
        } catch (Exception e) {
            System.err.println("Error starting camera: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Stop capturing from webcam
     */
    public void stopCamera() {
        isRunning = false;
        
        if (captureTask != null) {
            captureTask.cancel();
        }
        
        if (camera != null && camera.isOpened()) {
            camera.release();
        }
        
        System.out.println("Camera stopped");
    }
    
    /**
     * Capture a single frame from the camera
     * @return Captured image as File, or null if failed
     */
    public File captureFrame() {
        if (camera == null || !camera.isOpened()) {
            System.err.println("Camera is not open");
            return null;
        }
        
        try {
            Mat frame = new Mat();
            camera.read(frame);
            
            if (frame.empty()) {
                System.err.println("Failed to capture frame");
                frame.release();
                return null;
            }
            
            // Convert to BufferedImage
            BufferedImage image = matToBufferedImage(frame);
            frame.release();
            
            if (image == null) {
                return null;
            }
            
            // Save to temporary file
            File tempFile = File.createTempFile("face_capture_", ".jpg");
            ImageIO.write(image, "jpg", tempFile);
            
            return tempFile;
            
        } catch (Exception e) {
            System.err.println("Error capturing frame: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Capture a single frame and return as byte array
     * @return Captured image as byte array, or null if failed
     */
    public byte[] captureFrameAsBytes() {
        File frameFile = captureFrame();
        if (frameFile == null) {
            return null;
        }
        
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(frameFile.toPath());
            frameFile.delete();
            return bytes;
        } catch (IOException e) {
            System.err.println("Error reading frame bytes: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if camera is available
     * @return true if camera is available
     */
    public boolean isCameraAvailable() {
        if (camera == null) {
            camera = new VideoCapture(0);
            boolean available = camera.isOpened();
            if (available) {
                camera.release();
            }
            return available;
        }
        return camera.isOpened();
    }
    
    /**
     * Get the number of available cameras
     * @return Number of available cameras
     */
    public int getAvailableCameraCount() {
        int count = 0;
        for (int i = 0; i < 10; i++) {
            VideoCapture testCamera = new VideoCapture(i);
            if (testCamera.isOpened()) {
                count++;
                testCamera.release();
            }
        }
        return count;
    }
    
    /**
     * Convert OpenCV Mat to JavaFX Image
     * @param frame OpenCV Mat frame
     * @return JavaFX Image
     */
    private Image matToJavaFXImage(Mat frame) {
        try {
            BufferedImage image = matToBufferedImage(frame);
            if (image == null) {
                return null;
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            
            return new Image(new java.io.ByteArrayInputStream(imageBytes));
        } catch (Exception e) {
            System.err.println("Error converting Mat to JavaFX Image: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Convert OpenCV Mat to BufferedImage
     * @param frame OpenCV Mat frame
     * @return BufferedImage
     */
    private BufferedImage matToBufferedImage(Mat frame) {
        try {
            int type = BufferedImage.TYPE_BYTE_GRAY;
            if (frame.channels() > 1) {
                type = BufferedImage.TYPE_3BYTE_BGR;
            }
            
            BufferedImage image = new BufferedImage(frame.cols(), frame.rows(), type);
            DataBufferByte dataBuffer = (DataBufferByte) image.getRaster().getDataBuffer();
            byte[] data = dataBuffer.getData();
            frame.get(0, 0, data);
            
            return image;
        } catch (Exception e) {
            System.err.println("Error converting Mat to BufferedImage: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Task for continuous camera capture
     */
    private class CaptureTask extends Task<Void> {
        @Override
        protected Void call() throws Exception {
            while (isRunning && !isCancelled()) {
                if (camera != null && camera.isOpened()) {
                    Mat frame = new Mat();
                    camera.read(frame);
                    
                    if (!frame.empty()) {
                        Image fxImage = matToJavaFXImage(frame);
                        frame.release();
                        
                        if (fxImage != null && imageView != null) {
                            Platform.runLater(() -> {
                                imageView.setImage(fxImage);
                            });
                        }
                    } else {
                        frame.release();
                    }
                }
                
                Thread.sleep(33); // ~30 FPS
            }
            return null;
        }
    }
    
    public boolean isRunning() {
        return isRunning;
    }
}
