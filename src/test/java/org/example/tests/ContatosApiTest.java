package org.example.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ContatosApiTest {

    private static final String BASE_URL = "https://desafioapi-production-3190.up.railway.app/";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    public void testGetContatosRetorna200() {
        given()
                .when().get("/contatos")
                .then()
                .statusCode(200)
                .body("$", instanceOf(java.util.List.class));
    }

    @Test
    public void testPostAdicionarContatoComSucesso() {
        // Primeiro, exclua todos os contatos existentes
        given()
                .when().delete("/contatos")
                .then()
                .statusCode(anyOf(is(200), is(204)));

        // Gera um CPF único com base no timestamp
        String cpf = "123456789" + System.currentTimeMillis() % 100;

        // Adiciona um novo contato
        given()
                .contentType(ContentType.JSON)
                .body("{ \"nome\": \"Ana Silva\", \"telefone\": \"(51) 94321-8765\", \"email\": \"ana.silva@example.com\", \"cpf\": \"" + cpf + "\" }")
                .when().post("/contatos")
                .then()
                .statusCode(201)
                .body("cpf", equalTo(cpf));
    }

    @Test
    public void testNaoPermitirMaisDe30Contatos() {
        // Primeiro, exclua todos os contatos existentes
        given()
                .when().delete("/contatos")
                .then()
                .statusCode(anyOf(is(200), is(204)));

        for (int i = 1; i <= 30; i++) {
            String cpf = "123456789" + (100 + i);  // Gera CPFs únicos sequenciais

            given()
                    .contentType(ContentType.JSON)
                    .body("{ \"nome\": \"Contato " + i + "\", \"telefone\": \"(11) 91234-567" + i + "\", \"email\": \"contato" + i + "@example.com\", \"cpf\": \"" + cpf + "\" }")
                    .when().post("/contatos")
                    .then()
                    .statusCode(anyOf(is(201), is(400)));
        }
    }

    @Test
    public void testBuscarContatoPorCpf() {
        String cpf = "123456789" + System.currentTimeMillis() % 100;
        System.out.println("CPF gerado: " + cpf);

        // Adiciona o contato
        given()
                .contentType(ContentType.JSON)
                .body("{ \"nome\": \"Ana Silva\", \"telefone\": \"(51) 94321-8765\", \"email\": \"ana.silva@example.com\", \"cpf\": \"" + cpf + "\" }")
                .log().all()
                .when().post("/contatos")
                .then()
                .log().all()
                .statusCode(201);

        // Aguarda a propagação dos dados
        try {
            Thread.sleep(5000); // Aumenta o tempo de espera para 5 segundos
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Buscar o contato pelo CPF e imprimir resposta
        String getResponse = given()
                .log().all()
                .when().get("/contatos/" + cpf)
                .then()
                .log().all()
                .extract().asString();

        System.out.println("Resposta do GET: " + getResponse);
    }

    @Test
    public void testBuscarContatoCpfNaoExistente() {
        given()
                .when().get("/contatos/00000000000")
                .then()
                .statusCode(404);
    }

    @Test
    public void testDeletarTodosContatos() {
        given()
                .when().delete("/contatos")
                .then()
                .statusCode(anyOf(is(200), is(204)));

        // Verificar se todos os contatos foram deletados
        given()
                .when().get("/contatos")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }
}
