package tn.formini.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.formini.entities.Produit;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
}
