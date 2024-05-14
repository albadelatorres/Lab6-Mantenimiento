package com.uma.example.springuma;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MedicoServiceIT extends AbstractIntegration {
    @Autowired
    private MedicoService medicoService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepositoryMedico repositoryMedico;

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
    @DisplayName("Crea un médico, lo inserta y devuelve de la bdd correctamente")
    public void getMedicoId_withMedicosInBdd_shouldReturnMedicoMVC() throws JsonProcessingException, Exception {
        // crea una persona
        this.mockMvc.perform(post("/medico")
        .contentType("application/json")
        .content(objectMapper.writeValueAsString(medico)))
        .andExpect(status().isCreated())
        .andExpect(status().is2xxSuccessful());

        // obtiene el listado de personas
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
    public void getAllMedicos_withMedicosInBdd_shouldReturnAllMedicos() {
        Medico medico = new Medico();
        medico.setDni("1");
        medico.setEspecialidad("Traumatologo");
        medico.setNombre("Medico1");

        medicoService.addMedico(medico);


        /*List<Medico> allMedicos = medicoService.getAllMedicos();
        assertFalse(allMedicos.isEmpty());
        assertTrue(allMedicos.contains(medico));*/
    }

    @Test
    public void getMedico_withMedicosInBdd_shouldReturnMedico() {
        Medico medico = new Medico();
        medico.setDni("1");
        medico.setEspecialidad("Traumatologo");
        medico.setNombre("Medico1");

        medicoService.addMedico(medico);

        Medico retrievedMedico = medicoService.getMedico(1L);
        assertNotNull(retrievedMedico);
        assertEquals(retrievedMedico.getId(), medico.getId());
    }

    @Test
    public void addMedico_withAllData_shouldAddMedico() {
        Medico medico = new Medico();
        medico.setDni("1");
        medico.setEspecialidad("Traumatologo");
        medico.setNombre("Medico1");

        medicoService.addMedico(medico);

        Medico retrievedMedico = medicoService.getMedico(1L);
        assertNotNull(retrievedMedico);
        assertEquals(retrievedMedico.getId(), medico.getId());
        assertEquals(retrievedMedico.getNombre(), medico.getNombre());
        assertEquals(retrievedMedico.getEspecialidad(), medico.getEspecialidad());
        assertEquals(retrievedMedico.getDni(), medico.getDni());
    }

    @Test
    public void updateMedico_shouldUpdateMedico() {
        Medico medico = new Medico();
        medico.setDni("1");
        medico.setEspecialidad("Traumatologo");
        medico.setNombre("Medico1");
        medicoService.addMedico(medico);

        Medico retrievedMedico = medicoService.getMedico(1L);
        retrievedMedico.setNombre("Medico2");
        medicoService.updateMedico(retrievedMedico);

        Medico updatedMedico = medicoService.getMedico(1L);
        assertEquals(updatedMedico.getNombre(), retrievedMedico.getNombre());
        assertEquals(updatedMedico.getEspecialidad(), retrievedMedico.getEspecialidad());
        assertEquals(updatedMedico.getDni(), retrievedMedico.getDni());
    }

    @Test
    public void deleteMedico_shouldDeleteMedico() {
        Medico medico = new Medico();
        medico.setDni("1");
        medico.setEspecialidad("Traumatologo");
        medicoService.addMedico(medico);

        Medico retrievedMedico = medicoService.getMedico(1L);
        medicoService.removeMedico(retrievedMedico);

        assertNull(medicoService.getMedico(1L));
    }

    @Test
    public void removeMedicoId_shouldRemoveMedicoById() {
        Medico medico = new Medico();
        medico.setDni("1");
        medico.setEspecialidad("Traumatologo");
        medicoService.addMedico(medico);

        Medico medico2 = new Medico();
        medico2.setDni("2");
        medico2.setEspecialidad("Traumatologo");
        medicoService.addMedico(medico2);

        medicoService.removeMedicoID(1L);

        assertNull(medicoService.getMedico(1L));
        assertNotNull(medicoService.getMedico(2L));
    }


    @Test
    public void getMedicoByDni_withMedicosInBdd_shouldReturnMedicoByDni(){
        Medico medico = new Medico();
        medico.setDni("1");
        medico.setEspecialidad("Traumatologo");
        medico.setNombre("Medico1");

        medicoService.addMedico(medico);

        Medico retrievedMedico = medicoService.getMedicoByDni("1");
        assertNotNull(retrievedMedico);
        assertEquals(retrievedMedico.getDni(), medico.getDni());
    }


}
