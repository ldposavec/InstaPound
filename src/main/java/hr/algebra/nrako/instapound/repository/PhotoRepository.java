package hr.algebra.nrako.instapound.repository;

import hr.algebra.nrako.instapound.model.entity.Hashtag;
import hr.algebra.nrako.instapound.model.entity.Photo;
import hr.algebra.nrako.instapound.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long>, JpaSpecificationExecutor<Photo> {
    Page<Photo> findByUserOrderByUploadedAtDesc(User user, Pageable pageable);
    Page<Photo> findAllByOrderByUploadedAtDesc(Pageable pageable);

    Page<Photo> findByUploadedAtBetween(LocalDateTime uploadedAtAfter, LocalDateTime uploadedAtBefore, Pageable pageable);

    Page<Photo> findByHashtagsIn(List<Hashtag> hashtags, Pageable pageable);

    Long countByUser(User user);
}
