package com.uma.example.springuma;
import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.*;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;


import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.uma.example.springuma.model.Informe;
import reactor.core.publisher.Mono;

//Realizado por Alba de la Torre Segato y Jonatan Thorpe

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InformeServiceIT extends AbstractIntegration {

    @LocalServerPort
    private Integer port;
    private WebTestClient client;
    private Informe informe;
    private Medico medico;
    private Paciente paciente;

    @PostConstruct
    public void init() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:"+port)
                .responseTimeout(Duration.ofMillis(30000)).build();

        // Creamos y subimos un médico
        medico = new Medico();
        medico.setDni("11111111X");
        medico.setEspecialidad("Oncologia");
        medico.setNombre("Medico");
        medico.setId(1);

        client.post()
                .uri("/medico")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(medico), Medico.class)
                .exchange()
                .expectStatus().isCreated();

        // Creamos y subimos un paciente
        paciente = new Paciente();
        paciente.setNombre("Paciente");
        paciente.setDni("22222222X");
        paciente.setEdad(40);
        paciente.setMedico(medico);
        paciente.setId(1);

        client.post()
                .uri("/paciente")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(paciente), Paciente.class)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    public void saveInforme_shouldSaveInforme() throws Exception {
        // Guardamos la imagen healthy
        ObjectMapper objectMapper = new ObjectMapper();

        Resource imageResource = new ClassPathResource("healthy.png");
        MultiValueMap<String, Object> bodyBuilder = new LinkedMultiValueMap<>();
        bodyBuilder.add("image", imageResource);
        bodyBuilder.add("paciente", paciente);

        String uploadResponse = client.post().uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder))
                .exchange()
                .expectStatus().isOk().expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        assertNotNull(uploadResponse);

        // Creamos un informe de prueba en el que se detalla que el paciente no tiene cáncer
        Informe informe = new Informe();
        informe.setPrediccion("Not cancer (label 0),  score: 0.984481368213892");
        informe.setContenido("El paciente no tiene cancer.");
        Imagen imagen = new Imagen();
        imagen.setId(1);
        informe.setImagen(imagen);

        // Guardamos el informe
        client.post().uri("/informe")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(informe)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    public void deleteInforme_shouldDeleteInforme() throws Exception {

        // Creamos y guardamos un informe

        ObjectMapper objectMapper = new ObjectMapper();

        Resource imageResource = new ClassPathResource("healthy.png");
        MultiValueMap<String, Object> bodyBuilder = new LinkedMultiValueMap<>();
        bodyBuilder.add("image", imageResource);
        bodyBuilder.add("paciente", paciente);

        String uploadResponse = client.post().uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder))
                .exchange()
                .expectStatus().isOk().expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        assertNotNull(uploadResponse);

        Informe informe = new Informe();
        informe.setPrediccion("Not cancer (label 0),  score: 0.984481368213892");
        informe.setContenido("El paciente no tiene cancer.");
        Imagen imagen = new Imagen();
        imagen.setId(1);
        informe.setImagen(imagen);

        client.post().uri("/informe")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(informe)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .returnResult().getResponseBody();


        // Eliminamos el informe
        client.delete().uri("/informe/1")
                .exchange()
                .expectStatus().isNoContent();
    }
}
