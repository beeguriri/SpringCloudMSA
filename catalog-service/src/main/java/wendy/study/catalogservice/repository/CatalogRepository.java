package wendy.study.catalogservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wendy.study.catalogservice.entity.CatalogEntity;

public interface CatalogRepository extends JpaRepository<CatalogEntity, Long> {
    CatalogEntity findByProductId(String productId);
}
