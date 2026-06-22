package hr.algebra.nrako.instapound.controller;

import hr.algebra.nrako.instapound.model.entity.Photo;
import hr.algebra.nrako.instapound.model.mappers.PhotoMapper;
import hr.algebra.nrako.instapound.repository.HashtagRepository;
import hr.algebra.nrako.instapound.repository.PhotoRepository;
import hr.algebra.nrako.instapound.repository.UserRepository;
import hr.algebra.nrako.instapound.service.implementations.ImageProcessorServiceImpl;
import hr.algebra.nrako.instapound.service.storage.StorageServiceImpl;
import hr.algebra.nrako.instapound.service.interfaces.ActionLogService;
import hr.algebra.nrako.instapound.service.interfaces.TokenService;
import hr.algebra.nrako.instapound.service.interfaces.UserPackageService;
import hr.algebra.nrako.instapound.utils.IpUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@ActiveProfiles("test")
@WebMvcTest(controllers = PhotoController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(
        value = org.springframework.boot.data.autoconfigure.web.DataWebAutoConfiguration.class,
        exclude = {
                org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration.class,
                org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration.class
        }
)
class PhotoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PhotoRepository photoRepository;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private HashtagRepository hashtagRepository;
    @MockitoBean
    private StorageServiceImpl storageService;
    @MockitoBean
    private ImageProcessorServiceImpl imageProcessorService;
    @MockitoBean
    private ActionLogService actionLogService;
    @MockitoBean
    private TokenService tokenService;
    @MockitoBean
    private UserPackageService userPackageService;
    @MockitoBean
    private PhotoMapper photoMapper;
    @MockitoBean
    private IpUtils ipUtils;

    @Test
    void testBrowsePhotos() throws Exception {
        Photo p = Photo.builder().id(1L).originalFileName("a.jpg").build();
        when(photoRepository.findAllByOrderByUploadedAtDesc(org.mockito.ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(Collections.singletonList(p), PageRequest.of(0,15), 1));
        when(photoMapper.toDto(p)).thenReturn(hr.algebra.nrako.instapound.model.dto.response.PhotoResponse.builder().id(1L).originalFileName("a.jpg").build());

        mvc.perform(get("/api/photos/browse").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].originalFileName").value("a.jpg"));
    }

    @Test
    void testGetPhotoById() throws Exception {
        Photo p = Photo.builder().id(1L).originalFileName("a.jpg").build();
        when(photoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(photoMapper.toDto(p)).thenReturn(hr.algebra.nrako.instapound.model.dto.response.PhotoResponse.builder().id(1L).originalFileName("a.jpg").build());

        mvc.perform(get("/api/photos/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalFileName").value("a.jpg"));
    }
}






