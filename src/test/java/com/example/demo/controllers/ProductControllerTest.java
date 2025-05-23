package com.example.demo.controllers;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.demo.config.SecurityConfig;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnEmptyProductListWhenNoProductsExist() throws Exception {
        when(productRepository.count()).thenReturn(0L);
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnProductsWhenProductsExist() throws Exception {
        Product p1 = new Product(1L, "Test Product 1", 10.0);
        Product p2 = new Product(2L, "Test Product 2", 20.0);

        when(productRepository.count()).thenReturn(2L);
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("Test Product 1")))
                .andExpect(jsonPath("$[1].price", is(20.0)))
                .andExpect(jsonPath("$.length()", is(2)));
    }

    @Test
    void shouldReturnUnauthorizedWhenNoUser() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldCreateProductSuccessfully() throws Exception {
        Product savedProduct = new Product(1L, "Novo Produto", 99.99);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        mockMvc.perform(post("/products")
                .contentType("application/json")
                .content("""
                    {
                        "name": "Novo Produto",
                        "price": 99.99
                    }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Novo Produto")))
                .andExpect(jsonPath("$.price", is(99.99)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnProductByIdWhenExists() throws Exception {
        Product product = new Product(1L, "Produto Teste", 10.0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Produto Teste")))
                .andExpect(jsonPath("$.price", is(10.0)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldUpdateProductWhenExists() throws Exception {
        Product existing = new Product(1L, "Antigo", 50.0);
        Product updated = new Product(1L, "Novo", 99.0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(any(Product.class))).thenReturn(updated);

        mockMvc.perform(put("/products/1")
                .contentType("application/json")
                .content("""
                    {
                        "name": "Novo",
                        "price": 99.0
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Novo")))
                .andExpect(jsonPath("$.price", is(99.0)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnNotFoundWhenUpdatingNonexistentProduct() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/products/1")
                .contentType("application/json")
                .content("""
                    {
                        "name": "Inexistente",
                        "price": 88.0
                    }
                """))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldDeleteProductSuccessfully() throws Exception {
        doNothing().when(productRepository).deleteById(1L);

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void shouldReturnInternalServerErrorOnDeleteFailure() throws Exception {
        doThrow(new RuntimeException("Erro")).when(productRepository).deleteById(1L);

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isInternalServerError());
    }
}