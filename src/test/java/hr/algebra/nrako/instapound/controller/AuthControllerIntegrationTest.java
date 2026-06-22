package hr.algebra.nrako.instapound.controller;

import hr.algebra.nrako.instapound.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class AuthControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void loginEndpointShouldReturn200() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"admin\", \"password\": \"admin123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void checkUsernameOnNonExistingUsernameShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/auth/check-username/non-existing"))
                .andExpect(status().isOk());
    }
}


