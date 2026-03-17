package br.com.fiap.essentia.adapters.in;

import br.com.fiap.essentia.domain.ports.auth.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// Atenção: Essa classe de teste deve rodar no ambiente de Dev, portanto, recomendo rodar diretamente via linha de comando com $ mvn clean test -Dspring.profiles.active=dev

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Autowired
    private UserRepository userRepository;

    private static final String EMAIL_TEMP = "temp_user@teste.com";
    private static final String PASSWORD_TEMP = "Senha123!";
    private static String USER_ID_CRIADO;
    private static String TOKEN_VALIDO;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        System.out.println("🔧 Ambiente ativo: " + activeProfile + " | Porta: " + port);
    }

    @Test
    @Order(1)
    void deveRegistrarNovoUsuario() {
        String payload = """
                    {
                      "name": "Usuário Teste",
                      "email": "userteste@teste.com",
                      "password": "User1234!eee"
                    }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(anyOf(is(200), is(201), is(409))); // <- 409 é válido
    }

    @Test
    @Order(2)
    void deveAutenticarComSucesso() {
        String payload = """
                    {
                      "email": "userteste@teste.com",
                      "password": "User1234!eee"
                    }
                """;

        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue());
    }


    @Test
    @Order(3)
    void deveRetornar401ParaCredenciaisInvalidas() {
        String payload = """
                {
                  "email": "%s",
                  "password": "senhaErrada"
                }
                """.formatted(EMAIL_TEMP);

        RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/auth/login")
                .then()
                .log().all()
                .statusCode(401);

        System.out.println("❌ Teste de credenciais inválidas verificado com sucesso");
    }

    @AfterAll
    void cleanup() {
        try {
            if (userRepository.existsByEmail(EMAIL_TEMP)) {
                userRepository.deleteByEmail(EMAIL_TEMP);
                System.out.println("🧹 Usuário temporário removido do banco: " + EMAIL_TEMP);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Falha ao limpar usuário temporário: " + e.getMessage());
        }
    }
}
