package cm.atlanticpalace.pressing.repository;

import cm.atlanticpalace.pressing.domain.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Integer> {
    List<Article> findByCommande_NumFactureOrderByIdArticleAsc(String numFacture);
}
