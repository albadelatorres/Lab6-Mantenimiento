package com.uma.example.springuma;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Imagen;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.Paciente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;


import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ImagenServiceIT extends AbstractIntegration{

    @LocalServerPort
    private Integer port;
    private WebTestClient client;
    private Imagen imagen;

    private Medico medico;
    private Paciente paciente;
    @PostConstruct
    public void init() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:"+port)
                .responseTimeout(Duration.ofMillis(300000000)).build();

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
    @DisplayName("Subir una foto debería subirla correctamente")
    public void uploadImage_shouldUploadImage() throws Exception{
        // Creamos una imagen de prueba
        Resource imageResource = new ClassPathResource("healthy.png");
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("image", imageResource);
        bodyBuilder.part("paciente", paciente);

        // Subimos la imagen
        client.post().uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(bodyBuilder.build())
                .exchange()
                .expectStatus().isOk().expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String uploadResponse = response.getResponseBody();
                    assertNotNull(uploadResponse);
                });
    }

    @Test
    @DisplayName("Subir una imagen debería devolver un mensaje de aviso de imagen subida correctamente")
    public void uploadImage_shouldReturnFileUploadedSuccesfully() throws Exception {
        // Creamos una imagen de prueba
        Resource imageResource = new ClassPathResource("healthy.png");
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("image", imageResource);
        bodyBuilder.part("paciente", paciente);

        String uploadResponse = client.post().uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(bodyBuilder.build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        assertNotNull(uploadResponse);
        assertEquals("{\"response\" : \"file uploaded successfully : " + imageResource.getFilename() + "\"}", uploadResponse);
    }

    @Test
    @DisplayName("Subir y predecir una imagen de tejido sano debería predecir correctamente el resultado (no es cancer)")
    public void predictImage_withoutCancer_shouldReturnNotCancerPrediction() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        // Primero subimos la imagen como en el test anterior

        Resource imageResource = new ClassPathResource("healthy.png");
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("image", imageResource);
        bodyBuilder.part("paciente", paciente);

        client.post().uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .exchange()
                .expectStatus().isOk().expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        // Realizar la predicción
        client.get().uri("/imagen/predict/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String predictionResponse = response.getResponseBody();
                    assertNotNull(predictionResponse);
                    assertTrue(predictionResponse.contains("Not cancer (label 0),  score: 0.984481368213892"));
                });
    }

    @Test
    @DisplayName("Subir y predecir una imagen de tejido no sano debería predecir correctamente el resultado (es cancer)")
    public void predictImage_withCancer_shouldReturnCancerPrediction() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        // Primero subimos la imagen como en el test anterior

        Resource imageResource = new ClassPathResource("no_healthy.png");
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("image", imageResource);
        bodyBuilder.part("paciente", paciente);

        client.post().uri("/imagen")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .exchange()
                .expectStatus().isOk().expectStatus().is2xxSuccessful()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        // Realizar la predicción
        client.get().uri("/imagen/predict/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String predictionResponse = response.getResponseBody();
                    assertNotNull(predictionResponse);
                    assertTrue(predictionResponse.contains("Cancer (label 1), score: 0.6412607431411743"));
                });
    }
}
