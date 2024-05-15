package com.uma.example.springuma;
import com.uma.example.springuma.integration.base.AbstractIntegration;
import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.RepositoryMedico;
import com.uma.example.springuma.model.MedicoService;
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

//Realizado por Alba de la Torre Segato y Jonatan Thorpe

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MedicoServiceIT extends AbstractIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Medico medico;

    @BeforeEach
    public void setUp() {
        medico = new Medico();
        medico.setDni("11111111X");
        medico.setNombre("MedicoName");
        medico.setEspecialidad("Traumatologo");
    }

    @Test
    @DisplayName("Crea un médico, lo inserta y devuelve de la bdd a partir de su ID correctamente")
    public void getMedico_withMedicosInBdd_shouldReturnMedico() throws Exception {
        // insertamos el médico
        this.mockMvc.perform(post("/medico")
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(medico)))
        .andExpect(status().isCreated())
        .andExpect(status().is2xxSuccessful());

        // obtenemos el médico recién insertado
        this.mockMvc.perform(get("/medico/1"))
        .andDo(print())
        .andExpect(status().is2xxSuccessful())
        .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", instanceOf(LinkedHashMap.class)))
                .andExpect(jsonPath("$.dni").value(medico.getDni()))
                .andExpect(jsonPath("$.nombre").value(medico.getNombre()))
                .andExpect(jsonPath("$.especialidad").value(medico.getEspecialidad()));
    }

    @Test
    @DisplayName("Crear un médico con DNI repetido e insertarlo en la bdd hará que salte un error")
    public void saveMedico_withIncorrectData_shouldReturnError() throws Exception {
        Medico incorrectData = new Medico();
        incorrectData.setDni("11111111X");
        incorrectData.setNombre("Medico");
        incorrectData.setEspecialidad("Traumatologo");

        // insertamos el primer médico
        this.mockMvc.perform(post("/medico")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isCreated())
                .andExpect(status().is2xxSuccessful());

        // insertamos el médico repetido
        this.mockMvc.perform(post("/medico")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(incorrectData)))
                .andExpect(status().isInternalServerError()); // esperamos un 500 Internal Server Error
    }


    @Test
    @DisplayName("Inserta un médico en la bdd, actualiza sus datos y comprueba que han sido actualizados correctamente")
    public void updateMedico_shouldUpdateMedico() throws Exception {

        // insertamos el médico
        this.mockMvc.perform(post("/medico")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isCreated())
                .andExpect(status().is2xxSuccessful());

        // cambiamos los datos del médico
        medico.setNombre("MedicoUpdated");
        medico.setDni("11111112X");
        medico.setId(1);

        //llamamos a update con url /medico
        this.mockMvc.perform(put("/medico")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().is2xxSuccessful());

        //comprobamos que el nombre se ha cambiado y el resto de datos siguen igual
        this.mockMvc.perform(get("/medico/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.dni").value("11111112X"))
                .andExpect(jsonPath("$.nombre").value("MedicoUpdated"))
                .andExpect(jsonPath("$.especialidad").value("Traumatologo"));
    }

    @Test
    @DisplayName("Inserta un médico en la bdd y lo borra correctamente")
    public void deleteMedico_shouldDeleteMedico() throws Exception {
        // insertamos el médico
        this.mockMvc.perform(post("/medico")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isCreated())
                .andExpect(status().is2xxSuccessful());

        //borramos el médico
        this.mockMvc.perform(delete("/medico/1")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void getMedicoByDni_withMedicosInBdd_shouldReturnMedicoByDni() throws Exception {
        // insertamos el médico
        this.mockMvc.perform(post("/medico")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isCreated())
                .andExpect(status().is2xxSuccessful());

        //obtenemos el médico a partir de su dni
        this.mockMvc.perform(get("/medico/dni/11111111X")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isOk())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.dni").value("11111111X"))
                .andExpect(jsonPath("$.especialidad").value("Traumatologo"))
                .andExpect(jsonPath("$.nombre").value("MedicoName"));
    }

}
