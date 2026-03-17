package br.com.fiap.essentia.adapters.in;

import br.com.fiap.essentia.adapters.out.mongo.doc.UserDocument;
import br.com.fiap.essentia.adapters.out.mongo.repo.SpringDataUserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("dev")
public abstract class BaseIntegrationTest {

    @Autowired
    private SpringDataUserRepository userRepository;

    @LocalServerPort
    protected int port;

    protected String token;

    protected final String EMAIL = "temp_user@teste.com";
    protected final String PASSWORD = "123456";

    @BeforeAll
    void setupUserAndToken() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        userRepository.findByEmail(EMAIL).or(() -> {
            UserDocument user = new UserDocument();
            user.setEmail(EMAIL);
            user.setName("Usuário Teste Temporário");
            user.setActive(true);
            user.setPasswordHash(new BCryptPasswordEncoder().encode(PASSWORD));
            userRepository.save(user);
            System.out.println("✅ Persistido: " + EMAIL);
            return java.util.Optional.of(user);
        });

        Map<String, Object> payload = new HashMap<>();
        payload.put("email", EMAIL);
        payload.put("password", PASSWORD);

        var response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract();

        token = response.path("token");
        System.out.println("🔑 Login bem-sucedido para: " + EMAIL);
        System.out.println("🪪 Token capturado: " + (token != null ? token.substring(0, 15) + "..." : "null"));
    }
}
