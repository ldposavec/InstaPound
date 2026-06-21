package hr.algebra.nrako.instapound.controller;

import hr.algebra.nrako.instapound.enums.UserRole;
import hr.algebra.nrako.instapound.model.dto.response.TokenPairResponse;
import hr.algebra.nrako.instapound.model.entity.User;
import hr.algebra.nrako.instapound.model.mappers.UserMapper;
import hr.algebra.nrako.instapound.service.interfaces.ActionLogService;
import hr.algebra.nrako.instapound.service.interfaces.TokenService;
import hr.algebra.nrako.instapound.utils.IpUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private hr.algebra.nrako.instapound.repository.UserRepository userRepository;
    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private ActionLogService actionLogService;
    @MockitoBean
    private TokenService tokenService;
    @MockitoBean
    private UserMapper userMapper;
    @MockitoBean
    private IpUtils ipUtils;

    @Test
    void loginReturnsAccessTokenResponse() throws Exception {
        User user = User.builder()
                .id(1L)
                .username("alice")
                .role(UserRole.REGISTERED)
                .createdAt(LocalDateTime.now())
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(org.mockito.Mockito.mock(Authentication.class));
        when(userRepository.findByUsername("alice")).thenReturn(user);
        when(tokenService.issueTokenPair(user)).thenReturn(TokenPairResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .accessTokenExpiresInSeconds(900)
                .refreshTokenExpiresInSeconds(604800)
                .build());
        when(ipUtils.getClientIp(any())).thenReturn("127.0.0.1");
        doNothing().when(actionLogService).logAction(any(), any(), any(), any());

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"alice\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessTokenExpiresInSeconds").value(900))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        Cookie refreshCookie = result.getResponse().getCookie("refresh_token");
        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.getValue()).isEqualTo("refresh-token");
        assertThat(refreshCookie.isHttpOnly()).isTrue();
        assertThat(refreshCookie.getSecure()).isTrue();
        assertThat(refreshCookie.getPath()).isEqualTo("/api/auth/token");
        assertThat(refreshCookie.getMaxAge()).isEqualTo(7 * 24 * 60 * 60);
//        mockMvc.perform(post("/api/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"username\":\"alice\",\"password\":\"secret\"}"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.accessToken").value("access-token"))
//                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
//                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }
}



