package com.uma.example.springuma.integration.base;
import com.uma.example.springuma.model.Medico;
import com.uma.example.springuma.model.RepositoryMedico;
import com.uma.example.springuma.model.MedicoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Random;
import java.util.UUID;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MedicoServiceIT extends AbstractIntegration{
    @Autowired
    private MedicoService medicoService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepositoryMedico repositoryMedico;

    Medico medico;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @BeforeEach
    public void setup() {
        //repositoryMedico.deleteAll();
        int dni = (int) (Math.random()*100);
        medico = new Medico(String.valueOf(dni), "Medico1","Traumatologo");
    }

    @Test
    @DisplayName("Cuando intento obtener un medico que si existe, lo retorna correctamente")
    void getMedico_inDB_returnTrue() throws Exception {
        // crea un medico
        this.mockMvc.perform(post("/medico")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(medico)))
                .andExpect(status().isCreated())
                .andExpect(status().is2xxSuccessful());

        // Fetch the Medico by its DNI to get its ID
        Medico savedMedico = medicoService.getMedico(medico.getId());

        // obtiene el medico
        this.mockMvc.perform(get("/medico/" + savedMedico.getId()))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(savedMedico.getId()))
                .andExpect(jsonPath("$.nombre").value(savedMedico.getNombre()))
                .andExpect(jsonPath("$.especialidad").value(savedMedico.getEspecialidad()));
    }

    @Test
    public void getMedicoId_withMedicosInBdd_shouldReturnMedicoMVC() throws Exception {
        String dni = UUID.randomUUID().toString();
        String medicoJson = String.format("{\"dni\": \"%s\", \"especialidad\": \"Traumatologo\", \"nombre\": \"Medico1\"}", dni);

        mockMvc.perform(post("/medico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(medicoJson))
                .andExpect(status().isCreated());

        Medico savedMedico = medicoService.getMedico(1L);
        assertNotNull(savedMedico, "Medico no debería ser null");

        mockMvc.perform(get("/medico/{id}", savedMedico.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value(dni));
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
