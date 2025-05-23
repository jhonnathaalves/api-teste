package com.example.demo.controllers;

//import com.example.demo.model.Product;
//import com.example.demo.repository.ProductRepository;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; //Importar todos os @Mapping e @RequestBody

import java.util.HashMap;
//import java.util.List;
import java.util.Map;
//import java.util.Optional;

@RestController
public class InfoController {

    // Injeção de variáveis de ambiente usando @Value
    // Os valores após ":" são defaults caso a propriedade não seja encontrada
    @Value("${app.environment:desenvolvimento_default}")
    private String environment;

    @Value("${app.customMessage:Esta é uma mensagem padrão do Spring Boot.}")
    private String customMessage;

    @Value("${db.host:localhost_db_default}")
    private String dbHost;

    @Value("${db.port:5432_db_default}")
    private String dbPort;   

    @GetMapping("/")
    public String home() {
        return "Bem-vindo à nossa API Spring Boot de Exemplo!";
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("ambiente", environment);
        response.put("mensagem", customMessage);

        Map<String, String> dbConfig = new HashMap<>();
        dbConfig.put("host", dbHost);
        dbConfig.put("port", dbPort);
        response.put("configuracao_db", dbConfig);

        return response;
    }
    
}