package hr.algebra.nrako.instapound.repository;

import hr.algebra.nrako.instapound.model.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
    Hashtag findByTag(String tag);
    Boolean existsByTag(String tag);
}
