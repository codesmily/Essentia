package br.com.fiap.essentia.adapters.in;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDate;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.*;

// Atenção: Essa classe de teste deve rodar no ambiente de Dev, portanto, recomendo rodar diretamente via linha de comando com $ mvn clean test -Dspring.profiles.active=dev
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmotionControllerTest extends BaseIntegrationTest{

    @LocalServerPort
    private int port;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private static final String BASE_PATH = "/emotions";


    private static String emotionIdCriado;




    @Test
    @Order(1)
    void deveListarTodasEmocoes() {
        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", anyOf(empty(), not(empty())));
    }

    @Test
    @Order(2)
    void deveCriarEmocaoComSucesso() {
        String payload = """
                    {
                      "emotionType": "MOTIVADO"
                    }
                """;

        ValidatableResponse response = RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("emotionType", equalTo("MOTIVADO"))
                .body("deleted", equalTo(false))
                .body(matchesJsonSchemaInClasspath("schemas/emotion-schema.json"));

        emotionIdCriado = response.extract().path("id");
        Assertions.assertNotNull(emotionIdCriado);
    }

    @Test
    @Order(3)
    void deveBuscarEmocaoPorId() {
        Assumptions.assumeTrue(emotionIdCriado != null);
        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .get(BASE_PATH + "/" + emotionIdCriado)
                .then()
                .statusCode(200)
                .body("id", equalTo(emotionIdCriado));
    }

    @Test
    @Order(4)
    void deveAtualizarEmocao() {
        Assumptions.assumeTrue(emotionIdCriado != null);
        String payload = """
                    { "emotionType": "SATISFEITO" }
                """;

        RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .put(BASE_PATH + "/" + emotionIdCriado)
                .then()
                .statusCode(200)
                .body("emotionType", equalTo("SATISFEITO"));
    }

    @Test
    @Order(5)
    void deveAplicarSoftDelete() {
        Assumptions.assumeTrue(emotionIdCriado != null);
        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .delete(BASE_PATH + "/" + emotionIdCriado)
                .then()
                .statusCode(204);
    }

    @Test
    @Order(6)
    void deveRestaurarEmocao() {
        Assumptions.assumeTrue(emotionIdCriado != null);
        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .patch(BASE_PATH + "/" + emotionIdCriado + "/restore")
                .then()
                .statusCode(200)
                .body("deleted", is(false));
    }

    @Test
    @Order(7)
    void deveAplicarHardDelete() {
        Assumptions.assumeTrue(emotionIdCriado != null);
        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .delete(BASE_PATH + "/" + emotionIdCriado + "/hard")
                .then()
                .statusCode(204);
    }


    @Test
    @Order(8)
    void deveListarMinhasEmocoesPaginadas() {
        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .get(BASE_PATH + "/me/paged?page=0&size=5")
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }

    @Test
    @Order(9)
    void deveListarMinhasEmocoesPorTipo() {
        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .get(BASE_PATH + "/me/type/MOTIVADO?page=0&size=5")
                .then()
                .statusCode(200);
    }

    @Test
    @Order(10)
    void deveFiltrarMinhasEmocoesPorPeriodo() {
        String hoje = LocalDate.now().toString();
        String inicio = LocalDate.now().minusDays(15).toString();

        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .get(BASE_PATH + "/me/period?startDate=" + inicio + "&endDate=" + hoje)
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }

    @Test
    @Order(11)
    void deveListarMinhasUltimasEmocoes() {
        String payload = """
                    { "emotionType": "MOTIVADO" }
                """;

        RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(anyOf(is(200), is(201)));

        // Agora busca as últimas emoções
        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .get(BASE_PATH + "/me/latest?limit=3")
                .then()
                .statusCode(200)
                .body("$", anyOf(empty(), not(empty())));
    }


    @Test
    @Order(12)
    void deveRetornar401SemToken() {
        RestAssured.given()
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(401);
    }

    @Test
    @Order(13)
    void deveRetornar400AoCriarComPayloadInvalido() {
        RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(anyOf(is(400), is(422)));
    }

    @Test
    @Order(14)
    void deveRetornar404ParaIdInexistente() {
        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .get(BASE_PATH + "/nao-existe-xyz")
                .then()
                .statusCode(404);
    }
}
