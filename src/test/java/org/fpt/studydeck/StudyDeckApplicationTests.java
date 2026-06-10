package org.fpt.studydeck;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootTest
class StudyDeckApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext.getBean(OpenAPI.class)).isNotNull();
        assertThat(applicationContext.getBean(SecurityFilterChain.class)).isNotNull();
    }

}
