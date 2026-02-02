package hr.algebra.nrako.instapound.repository;

import hr.algebra.nrako.instapound.model.entity.Hashtag;
import hr.algebra.nrako.instapound.model.entity.Photo;
import hr.algebra.nrako.instapound.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long>, JpaSpecificationExecutor<Photo> {
    Page<Photo> findByUserOrderByUploadedAtDesc(User user, Pageable pageable);
    List<Photo> findByUser(User user);
    Page<Photo> findAllByOrderByUploadedAtDesc(Pageable pageable);

    Page<Photo> findByUploadedAtBetween(LocalDateTime uploadedAtAfter, LocalDateTime uploadedAtBefore, Pageable pageable);

    Page<Photo> findByHashtagsIn(List<Hashtag> hashtags, Pageable pageable);

    Long countByUser(User user);
    
    @Modifying
    @Transactional
    void deleteByUser(User user);
}
