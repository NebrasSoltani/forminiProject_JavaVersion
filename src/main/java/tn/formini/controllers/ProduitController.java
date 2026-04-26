package tn.formini.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.formini.entities.Produit;
import tn.formini.repositories.ProduitRepository;
import tn.formini.services.CloudinaryService;

import java.io.IOException;

@RestController
@RequestMapping("/api/produits")
@CrossOrigin(origins = "*") // Permet les requêtes de différents domaines
public class ProduitController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ProduitRepository produitRepository;

    /**
     * Endpoint pour uploader une image et créer un produit
     * URL: POST /api/produits/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("nom") String nom,
            @RequestParam("description") String description) {
        
        try {
            // 1. Uploader l'image sur Cloudinary
            String imageUrl = cloudinaryService.uploadImage(file);
            
            // 2. Créer l'objet Produit et sauvegarder l'URL en base
            Produit produit = new Produit();
            produit.setNom(nom);
            produit.setDescription(description);
            produit.setImageUrl(imageUrl);
            
            Produit savedProduit = produitRepository.save(produit);
            
            // 3. Retourner le produit sauvegardé avec l'URL Cloudinary
            return new ResponseEntity<>(savedProduit, HttpStatus.CREATED);
            
        } catch (IOException e) {
            return new ResponseEntity<>("Erreur lors de l'upload : " + e.getMessage(), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Une erreur est survenue : " + e.getMessage(), 
                    HttpStatus.BAD_REQUEST);
        }
    }
}
