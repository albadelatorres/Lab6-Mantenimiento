package com.uma.example.springuma;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.Paciente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PacienteServiceIT  extends AbstractIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Paciente paciente;
    private Medico medico;

    @BeforeEach
    public void setUp() throws Exception {
        medico = new Medico();
        medico.setDni("11111111X");
        medico.setNombre("MedicoName");
        medico.setEspecialidad("Traumatologo");
        medico.setId(1);

        // insertamos el médico
        this.mockMvc.perform(post("/medico")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isCreated())
                .andExpect(status().is2xxSuccessful());

        paciente = new Paciente();
        paciente.setCita("Lunes");
        paciente.setDni("123456789L");
        paciente.setEdad(19);
        paciente.setMedico(medico);
        paciente.setNombre("PacienteName");
        paciente.setId(1);
    }

    @Test
    @DisplayName("Crea un paciente, lo inserta y devuelve de la bdd correctamente")
    public void getPacienteId_withPacientesInBdd_shouldReturnPaciente() throws Exception {
        // crea una persona
        this.mockMvc.perform(post("/paciente")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(paciente)))
            .andExpect(status().isCreated())
            .andExpect(status().is2xxSuccessful());

        // obtiene el listado de personas
        this.mockMvc.perform(get("/paciente/1"))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", instanceOf(LinkedHashMap.class)))
                .andExpect(jsonPath("$.nombre").value(paciente.getNombre()))
                .andExpect(jsonPath("$.dni").value(paciente.getDni()))
                .andExpect(jsonPath("$.edad").value(paciente.getEdad()))
                .andExpect(jsonPath("$.cita").value(paciente.getCita()))
                .andExpect(jsonPath("$.medico.dni").value(paciente.getMedico().getDni()));
    }

    @Test
    @DisplayName("Crear un paciente con DNI repetido e insertarlo en la bdd hará que salte un error")
    public void savePaciente_withIncorrectData_shouldReturnError() throws Exception {
        Paciente incorrectData = new Paciente();
        incorrectData.setCita("Lunes");
        incorrectData.setDni("123456789L");
        incorrectData.setEdad(19);
        incorrectData.setMedico(medico);
        incorrectData.setNombre("PacienteName");
        incorrectData.setId(2);

        // insertamos el primer paciente
        this.mockMvc.perform(post("/paciente")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(paciente)))
                .andExpect(status().isCreated())
                .andExpect(status().is2xxSuccessful());

        // insertamos el paciente repetido
        this.mockMvc.perform(post("/paciente")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(incorrectData)))
                .andExpect(status().isInternalServerError()); // esperamos un 500 Internal Server Error
    }

    @Test
    @DisplayName("Inserta un paciente en la bdd, actualiza sus datos y comprueba que han sido actualizados correctamente")
    public void updatePaciente_shouldUpdatePaciente() throws Exception {

        // insertamos el paciente
        this.mockMvc.perform(post("/paciente")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(paciente)))
                .andExpect(status().isCreated())
                .andExpect(status().is2xxSuccessful());

        // cambiamos los datos del paciente
        paciente.setNombre("PacienteUpdated");
        paciente.setDni("123456788X");
        paciente.setId(1);

        //llamamos a update con url /paciente
        this.mockMvc.perform(put("/paciente")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(paciente)))
                .andExpect(status().is2xxSuccessful());

        //comprobamos que el nombre se ha cambiado y el resto de datos siguen igual
        this.mockMvc.perform(get("/paciente/1"))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", instanceOf(LinkedHashMap.class)))
                .andExpect(jsonPath("$.nombre").value("PacienteUpdated"))
                .andExpect(jsonPath("$.dni").value("123456788X"))
                .andExpect(jsonPath("$.edad").value(19))
                .andExpect(jsonPath("$.cita").value("Lunes"))
                .andExpect(jsonPath("$.medico.dni").value(medico.getDni()));
    }

}
