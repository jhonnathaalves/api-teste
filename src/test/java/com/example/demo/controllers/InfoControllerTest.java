package com.example.demo.controllers;

import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser; // ✅ IMPORTANTE
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;


import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InfoController.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.environment=teste",
        "app.customMessage=Mensagem de Teste",
        "db.host=mock_db_host",
        "db.port=9999"
})
@WithMockUser(username = "testuser", roles = {"USER"}) // ✅ Autentica todos os testes com esse usuário
public class InfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;

    @Test
    void shouldReturnDefaultMessageFromHomeEndpoint() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bem-vindo à nossa API Spring Boot de Exemplo!")));
    }

    @Test
    void shouldReturnInfoWithTestEnvironmentVariables() throws Exception {
        mockMvc.perform(get("/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ambiente", is("teste")))
                .andExpect(jsonPath("$.mensagem", is("Mensagem de Teste")))
                .andExpect(jsonPath("$.configuracao_db.host", is("mock_db_host")))
                .andExpect(jsonPath("$.configuracao_db.port", is("9999")));
    }   
}