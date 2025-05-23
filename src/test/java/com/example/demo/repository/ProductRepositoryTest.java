package com.example.demo.repository;

import com.example.demo.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // Testa apenas a camada JPA (Repositório)
@ActiveProfiles("test") // Ativa um perfil de teste. Usa o H2 em memória por padrão.
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager; // Ajuda a gerenciar entidades para testes de DB

    @Test
    void shouldFindNoProductsIfRepositoryIsEmpty() {
        Iterable<Product> products = productRepository.findAll();
        assertThat(products).isEmpty();
    }

    @Test
    void shouldStoreAProduct() {
        Product product = productRepository.save(new Product(null, "Test Product", 100.0));
        assertThat(product).hasFieldOrPropertyWithValue("name", "Test Product");
        assertThat(product).hasFieldOrPropertyWithValue("price", 100.0);
        assertThat(product.getId()).isNotNull();
    }

    @Test
    void shouldFindAllProducts() {
        Product p1 = new Product(null, "Product 1", 10.0);
        Product p2 = new Product(null, "Product 2", 20.0);
        Product p3 = new Product(null, "Product 3", 30.0);

        entityManager.persist(p1);
        entityManager.persist(p2);
        entityManager.persist(p3);
        entityManager.flush(); // Garante que as mudanças são persistidas

        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(3).contains(p1, p2, p3);
    }

    @Test
    void shouldFindProductById() {
        Product p1 = new Product(null, "Product 1", 10.0);
        entityManager.persist(p1);
        entityManager.flush();

        Optional<Product> foundProduct = productRepository.findById(p1.getId());
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Product 1");
    }

    @Test
    void shouldDeleteProductById() {
        Product p1 = new Product(null, "Product 1", 10.0);
        Product p2 = new Product(null, "Product 2", 20.0);
        entityManager.persist(p1);
        entityManager.persist(p2);
        entityManager.flush();

        productRepository.deleteById(p1.getId());
        assertThat(productRepository.findAll()).hasSize(1).contains(p2);
    }

    @Test
    void shouldUpdateProduct() {
        Product product = new Product(null, "Old Name", 50.0);
        entityManager.persist(product);
        entityManager.flush();

        Product updatedProduct = productRepository.findById(product.getId()).get();
        updatedProduct.setName("New Name");
        updatedProduct.setPrice(150.0);
        productRepository.save(updatedProduct);

        Product resultProduct = productRepository.findById(product.getId()).get();
        assertThat(resultProduct.getName()).isEqualTo("New Name");
        assertThat(resultProduct.getPrice()).isEqualTo(150.0);
    }
}