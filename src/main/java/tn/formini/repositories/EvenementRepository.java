package tn.formini.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.formini.entities.evenements.Evenement;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, Integer> {
}
