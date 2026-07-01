package hr.algebra.nrako.instapound.controller;

import hr.algebra.nrako.instapound.model.entity.Photo;
import hr.algebra.nrako.instapound.model.mappers.PhotoMapper;
import hr.algebra.nrako.instapound.repository.PhotoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/vuln")
public class VulnController {

    private PhotoMapper photoMapper;

    @PersistenceContext
    private EntityManager em;

    @GetMapping("/photo/search")
    public ResponseEntity<Object> searchPhotoVulnerability(@RequestParam String filename) {
        String sql = "select * from PHOTOS where stored_file_name = :filename";

        List<Photo> photos = em.createNativeQuery(sql, Photo.class)
                .setParameter("filename", filename).getResultList();

        return ResponseEntity.ok(photos.stream().map(photoMapper::toDto).toList());
    }

    @GetMapping("/ssrf")
    public ResponseEntity<Object> ssrfVulnerability(@RequestParam String url) {
        URI uri = URI.create(url);
        Set<String> allowedHosts = Set.of("example.com", "api.example.com");

        if (!allowedHosts.contains(uri.getHost())) {
            return ResponseEntity.badRequest().body("Host is not allowed: " + uri.getHost());
        }

        return ResponseEntity.ok(new RestTemplate().getForObject(uri, String.class));
    }
}
