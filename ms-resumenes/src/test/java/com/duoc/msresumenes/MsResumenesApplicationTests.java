package com.duoc.msresumenes;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Usa src/test/resources/application-test.properties (credenciales falsas: no se conecta a AWS).
@SpringBootTest
@ActiveProfiles("test")
class MsResumenesApplicationTests {

    @Test
    void contextLoads() {
    }
}
