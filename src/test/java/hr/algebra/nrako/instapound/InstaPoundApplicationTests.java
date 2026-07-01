package hr.algebra.nrako.instapound;

import hr.algebra.nrako.instapound.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@ImportAutoConfiguration(
        value = org.springframework.boot.data.autoconfigure.web.DataWebAutoConfiguration.class,
        exclude = {
                org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration.class,
                org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration.class
        }
)
@Import(TestSecurityConfig.class)
class InstaPoundApplicationTests {

    @Test
    void contextLoads() {
    }

}
