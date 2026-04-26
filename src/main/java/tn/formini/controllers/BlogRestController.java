package tn.formini.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.formini.entities.evenements.Blog;
import tn.formini.repositories.BlogRepository;
import tn.formini.services.CloudinaryService;

import java.io.IOException;
import java.util.Date;

@RestController
@RequestMapping("/api/blogs")
@CrossOrigin(origins = "*")
public class BlogRestController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private BlogRepository blogRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadBlogImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("titre") String titre,
            @RequestParam("contenu") String contenu,
            @RequestParam("categorie") String categorie) {
        
        try {
            // 1. Upload sur Cloudinary
            String imageUrl = cloudinaryService.uploadImage(file);
            
            // 2. Création et sauvegarde du Blog
            Blog blog = new Blog();
            blog.setTitre(titre);
            blog.setContenu(contenu);
            blog.setCategorie(categorie);
            blog.setImage(imageUrl); // On sauve l'URL Cloudinary ici
            blog.setDate_publication(new Date());
            blog.setIs_publie(true);
            
            Blog savedBlog = blogRepository.save(blog);
            
            return new ResponseEntity<>(savedBlog, HttpStatus.CREATED);
            
        } catch (IOException e) {
            return new ResponseEntity<>("Erreur Cloudinary : " + e.getMessage(), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
