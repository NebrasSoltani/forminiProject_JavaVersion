package tn.formini.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    /**
     * Upload une image sur Cloudinary (version pour MultipartFile - Spring REST)
     */
    public String uploadImage(MultipartFile multipartFile) throws IOException {
        try {
            Map uploadResult = cloudinary.uploader().upload(multipartFile.getBytes(), ObjectUtils.emptyMap());
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new IOException("Erreur Cloudinary: " + e.getMessage());
        }
    }

    /**
     * Upload une image sur Cloudinary (version pour File - JavaFX)
     */
    public String uploadImage(java.io.File file) throws IOException {
        try {
            Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new IOException("Erreur Cloudinary: " + e.getMessage());
        }
    }
}
