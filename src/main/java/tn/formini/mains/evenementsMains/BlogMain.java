package tn.formini.mains.evenementsMains;

import tn.formini.entities.evenements.Blog;
import tn.formini.services.evenementsService.BlogService;

import java.util.Date;
import java.util.List;

public class BlogMain {
    public static void main(String[] args) {
        BlogService bs = new BlogService();

        // 1. Ajouter un blog
        Blog b1 = new Blog();
        b1.setTitre("Nouveau Blog");
        b1.setContenu("Ceci est le contenu du blog.");
        b1.setImage("image.jpg");
        b1.setDate_publication(new Date());
        b1.setCategorie("Technologie");
        b1.setAuteur_id(1);
        b1.setIs_publie(true);
        b1.setTags("[\"test\"]");

        System.out.println("--- Ajout d'un blog ---");
        bs.ajouter(b1);

        // 2. Afficher les blogs
        System.out.println("--- Liste des blogs ---");
        List<Blog> blogs = bs.afficher();
        for (Blog b : blogs) {
            System.out.println(b);
        }

        if (!blogs.isEmpty()) {
            Blog lastBlog = blogs.get(blogs.size() - 1);
            
            // 3. Modifier le dernier blog
            lastBlog.setTitre("Titre Modifié");
            System.out.println("--- Modification du blog ID: " + lastBlog.getId() + " ---");
            bs.modifier(lastBlog);
        }
    }
}
