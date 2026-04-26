package tn.formini.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.formini.entities.evenements.Blog;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Integer> {
}
