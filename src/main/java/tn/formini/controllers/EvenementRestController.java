package tn.formini.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.formini.entities.evenements.Evenement;
import tn.formini.repositories.EvenementRepository;
import tn.formini.services.CloudinaryService;

import java.io.IOException;
import java.util.Date;

@RestController
@RequestMapping("/api/evenements")
@CrossOrigin(origins = "*")
public class EvenementRestController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private EvenementRepository evenementRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadEvenementImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("titre") String titre,
            @RequestParam("lieu") String lieu,
            @RequestParam("description") String description,
            @RequestParam("type") String type,
            @RequestParam("date_debut") @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateDebut,
            @RequestParam("date_fin") @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFin) {
        
        try {
            // 1. Upload sur Cloudinary
            String imageUrl = cloudinaryService.uploadImage(file);
            
            // 2. Création et sauvegarde de l'Evenement
            Evenement evenement = new Evenement();
            evenement.setTitre(titre);
            evenement.setLieu(lieu);
            evenement.setDescription(description);
            evenement.setType(type);
            evenement.setImage(imageUrl); // URL Cloudinary
            evenement.setDate_debut(dateDebut);
            evenement.setDate_fin(dateFin);
            evenement.setIs_actif(true);
            
            Evenement savedEvenement = evenementRepository.save(evenement);
            
            return new ResponseEntity<>(savedEvenement, HttpStatus.CREATED);
            
        } catch (IOException e) {
            return new ResponseEntity<>("Erreur Cloudinary : " + e.getMessage(), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
