package br.com.fiap.essentia.adapters.in;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;




// Atenção: Essa classe de teste deve rodar no ambiente de Dev, portanto, recomendo rodar diretamente via linha de comando com $ mvn clean test -Dspring.profiles.active=dev
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NotesControllerTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    private static final String BASE_PATH = "/notes";
    private static String noteIdCriada;




    @Test @Order(1)
    void deveListarTodasNotas() {
        RestAssured.given()
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(200)
                .body("$", anyOf(empty(), not(empty())));
    }

    @Test @Order(2)
    void deveCriarNovaNota() {
        String payload = """
            {
              "title": "Nota automatizada teste completo",
              "content": "Conteúdo de teste RestAssured",
              "currentEmotion": "MOTIVADO"
            }
        """;

        ValidatableResponse response = RestAssured.given()
                .contentType(ContentType.JSON)
                .auth().oauth2(token)
                .body(payload)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("title", equalTo("Nota automatizada teste completo"))
                .body("currentEmotion", anyOf(equalTo("MOTIVADO"), nullValue()));

        noteIdCriada = response.extract().path("id");
        Assert.assertNotNull(noteIdCriada);
    }

    @Test @Order(3)
    void deveBuscarNotaPorId() {
        Assumptions.assumeTrue(noteIdCriada != null);

        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .get(BASE_PATH + "/" + noteIdCriada)
                .then()
                .statusCode(200)
                .body("id", equalTo(noteIdCriada));
    }

    @Test @Order(4)
    void deveAtualizarNota() {
        Assumptions.assumeTrue(noteIdCriada != null);

        String payload = """
            {
              "title": "Nota atualizada via teste",
              "content": "Conteúdo atualizado com sucesso"
            }
        """;

        RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .put(BASE_PATH + "/" + noteIdCriada)
                .then()
                .statusCode(200)
                .body("title", equalTo("Nota atualizada via teste"));
    }

    @Test @Order(5)
    void deveAplicarSoftDelete() {
        Assumptions.assumeTrue(noteIdCriada != null);

        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .delete(BASE_PATH + "/" + noteIdCriada)
                .then()
                .statusCode(anyOf(is(200), is(204)));
    }

    @Test @Order(6)
    void deveRestaurarNota() {
        Assumptions.assumeTrue(noteIdCriada != null);

        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .patch(BASE_PATH + "/" + noteIdCriada + "/restore")
                .then()
                .statusCode(200)
                .body("deleted", is(false));
    }

    @Test @Order(7)
    void deveAplicarHardDelete() {
        Assumptions.assumeTrue(noteIdCriada != null);

        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .delete(BASE_PATH + "/" + noteIdCriada + "/hard")
                .then()
                .statusCode(anyOf(is(200), is(204)));
    }

    // =================== ROTAS “MEUS” (usuário autenticado) =================== //

    @Test @Order(8)
    void deveListarMinhasNotasPaginadas() {
        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .get(BASE_PATH + "/me/paged?page=0&size=5")
                .then()
                .statusCode(200)
                .body("content", notNullValue());
    }

    @Test @Order(9)
    void deveListarMinhasNotasPorEmocao() {
        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .get(BASE_PATH + "/me/emotion/MOTIVADO?page=0&size=5")
                .then()
                .statusCode(200);
    }

    @Test @Order(10)
    void deveFiltrarMinhasNotasPorPeriodo() {
        String hoje = LocalDate.now().toString();
        String inicio = LocalDate.now().minusDays(30).toString();

        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .get(BASE_PATH + "/me/period?startDate=" + inicio + "&endDate=" + hoje)
                .then()
                .statusCode(200);
    }

    @Test @Order(11)
    void deveListarUltimasNotas() {
        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .get(BASE_PATH + "/me/latest?limit=3")
                .then()
                .statusCode(200)
                .body("$", anyOf(empty(), not(empty())));
    }

    @Test @Order(12)
    void deveContarNotasPorEmocao() {
        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .get(BASE_PATH + "/me/countByEmotion")
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }

    @Test @Order(13)
    void deveRetornarTotaisDiarios() {
        String hoje = LocalDate.now().toString();
        String inicio = LocalDate.now().minusDays(7).toString();

        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .get(BASE_PATH + "/me/dailyTotals?startDate=" + inicio + "&endDate=" + hoje)
                .then()
                .statusCode(200)
                .body("$", notNullValue());
    }

    // ===================== CENÁRIOS DE ERRO / AUTENTICAÇÃO ===================== //

    @Test @Order(14)
    void deveRetornar401SemToken() {
        RestAssured.given()
                .when()
                .get(BASE_PATH)
                .then()
                .statusCode(401);
    }

    @Test @Order(15)
    void deveRetornar400AoCriarNotaSemPayload() {
        RestAssured.given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(anyOf(is(400), is(422)));
    }

    @Test @Order(16)
    void deveRetornar404AoBuscarNotaInexistente() {
        RestAssured.given()
                .auth().oauth2(token)
                .when()
                .get(BASE_PATH + "/id-invalido-999")
                .then()
                .statusCode(404);
    }
}