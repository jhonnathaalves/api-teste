# 🚀 API de Exemplo Spring Boot com DevOps (Build Único & Configuração por Ambiente)

Este projeto demonstra uma API RESTful simples construída com Spring Boot, projetada para ser conteinerizada com Docker e configurada dinamicamente através de **variáveis de ambiente**. O objetivo principal é validar o conceito de **"build único"**, onde a mesma imagem Docker pode ser usada em diferentes ambientes (desenvolvimento, homologação, produção) apenas alterando suas configurações de ambiente no momento da execução.

Além disso, o projeto inclui exemplos de interação com um banco de dados em memória H2 e testes unitários para as camadas de Controller e Repositório.

---

## ✨ Funcionalidades

* **API RESTful:** Endpoints básicos para informações da aplicação e gerenciamento de produtos (GET, POST, PUT, DELETE).

* **Configuração Dinâmica:** Utiliza `@Value` do Spring Boot para injetar variáveis de ambiente (e propriedades do `application.properties`).

* **Build Único:** A imagem Docker gerada é independente do ambiente, sendo configurada em tempo de execução.

* **Banco de Dados H2 em Memória:** Simplesmente configurado para testes locais e desenvolvimento rápido sem necessidade de um DB externo.

* **Testes Unitários:** Exemplos de testes para o `InfoController` (com `MockMvc` e `Mockito`) e `ProductRepository` (com `@DataJpaTest`).

* **Pronto para Conteinerização:** `Dockerfile` otimizado para criar uma imagem Docker da API.

---

## 🛠️ Tecnologias Utilizadas

* **Java 17**

* **Spring Boot 3.2.5**

* **Apache Maven**

* **Docker**

* **H2 Database** (em memória)

* **JUnit 5**, **Mockito**, **AssertJ** (para testes)

---

## 📂 Estrutura do Projeto

```
.
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── example/
│   │               └── demo/
│   │                   ├── controllers/  (Controladores REST)
│   │                   ├── model/        (Entidades JPA)
│   │                   ├── repository/   (Repositórios JPA)
│   │                   └── DemoApplication.java (Classe principal)
│   ├── test/
│   │   └── java/
│   │       └── com/
│   │           └── example/
│   │               └── demo/
│   │                   ├── controllers/  (Testes do Controller)
│   │                   └── repository/   (Testes do Repositório)
│   └── resources/
│       └── application.properties (Configurações da aplicação e H2)
├── pom.xml         (Configurações e dependências Maven)
├── Dockerfile      (Instruções para criar a imagem Docker)
└── .env.example    (Exemplo de variáveis de ambiente - **NÃO** embarcar na imagem!)
```

---

## 🚀 Como Rodar o Projeto

### Pré-requisitos

Certifique-se de ter instalado em sua máquina:

* **JDK 17** ou superior

* **Apache Maven**

* **Docker Desktop** (ou motor Docker equivalente)

### 1. Compilar e Executar Testes (Localmente)

1. Navegue até o diretório raiz do projeto no seu terminal:

   ```bash
   cd /caminho/para/seu/projeto/spring-boot-api-devops-example
   ```

2. Compile o projeto e execute todos os testes unitários/de integração leve:

   ```bash
   mvn clean install
   ```

   Isso irá baixar as dependências, compilar o código, gerar o JAR executável em `target/demo-0.0.1-SNAPSHOT.jar` e rodar os testes configurados.

### 2. Executar a API Localmente (sem Docker)

Após o passo 1 (compilação), você pode rodar a aplicação diretamente:

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

A API estará disponível em `http://localhost:8080`.

### 3. Construir a Imagem Docker

No diretório raiz do projeto (onde está o `Dockerfile`):

```bash
docker build -t minha-api-spring:latest .
```

Isso criará uma imagem Docker chamada `minha-api-spring` com a tag `latest`.

### 4. Executar a Imagem Docker (com Variáveis de Ambiente)

Este passo demonstra o conceito de "build único" e configuração por ambiente. Use a **mesma imagem** `minha-api-spring:latest`, mas passe diferentes variáveis de ambiente para simular cenários:

#### Cenário: Ambiente de Desenvolvimento Local

```bash
docker run -p 8080:8080 \
  -e APP_ENVIRONMENT=desenvolvimento \
  -e APP_CUSTOM_MESSAGE="Mensagem do ambiente de desenvolvimento local via Docker!" \
  -e DB_HOST=localhost_dev_db \
  -e DB_PORT=5432 \
  minha-api-spring:latest
```

#### Cenário: Simulação de Ambiente de Produção

```bash
docker run -p 80:8080 \
  -e APP_ENVIRONMENT=producao \
  -e APP_CUSTOM_MESSAGE="API em Producao! Seja cuidadoso com os dados." \
  -e DB_HOST=prod.db.empresa.com \
  -e DB_PORT=5432 \
  minha-api-spring:latest
```

Note que na simulação de produção, o host mapeia a porta 80 para a 8080 do contêiner.

---

## 🌐 Endpoints da API

A API estará disponível na porta `8080` (ou a porta que você mapeou ao executar o Docker).

* **`GET /`**

    * Retorna uma mensagem de boas-vindas.

    * Ex: `http://localhost:8080/`

* **`GET /info`**

    * Retorna informações da aplicação, incluindo as variáveis de ambiente carregadas.

    * Ex: `http://localhost:8080/info`

* **`GET /products`**

    * Lista todos os produtos no banco de dados (H2 em memória). Insere produtos de exemplo se o banco estiver vazio.

    * Ex: `http://localhost:8080/products`

* **`POST /products`**

    * Cria um novo produto.

    * **Método:** `POST`

    * **Corpo da Requisição (JSON):**

        ```json
        {
          "name": "Novo Produto",
          "price": 99.99
        }
        ```

    * **Exemplo com `curl`:**

        ```bash
        curl -X POST -H "Content-Type: application/json" -d '{"name":"Fone de Ouvido","price":150.00}' http://localhost:8080/products
        ```

* **`GET /products/{id}`**

    * Busca um produto pelo ID.

    * Ex: `http://localhost:8080/products/1`

* **`PUT /products/{id}`**

    * Atualiza um produto existente.

    * **Método:** `PUT`

    * **Corpo da Requisição (JSON):**

        ```json
        {
          "name": "Nome Atualizado",
          "price": 299.99
        }
        ```

    * **Exemplo com `curl`:**

        ```bash
        curl -X PUT -H "Content-Type: application/json" -d '{"name":"Laptop PRO","price":1800.00}' http://localhost:8080/products/1
        ```

* **`DELETE /products/{id}`**

    * Exclui um produto pelo ID.

    * **Método:** `DELETE`

    * **Exemplo com `curl`:**

        ```bash
        curl -X DELETE http://localhost:8080/products/1
        ```

* **`GET /h2-console`**

    * Acessa o console web do banco de dados H2 (apenas em ambientes de desenvolvimento/teste).

    * **JDBC URL:** `jdbc:h2:mem:testdb`

    * **User Name:** `sa`

    * **Password:** (deixe em branco)

    * Ex: `http://localhost:8080/h2-console`

---

## 🐳 Uso com Kubernetes (Visão Geral)

Para implantar esta API em um cluster Kubernetes (local, como Minikube, ou em nuvem via Rancher/EKS/GKE/AKS), você utilizaria:

1.  **`Deployment`:** Para definir como seus Pods serão criados a partir da imagem `minha-api-spring:latest` e para gerenciar o número de réplicas.

2.  **`ConfigMaps`:** Para armazenar as variáveis de ambiente **não sensíveis** (`APP_ENVIRONMENT`, `APP_CUSTOM_MESSAGE`, `DB_HOST`, `DB_PORT`) específicas para cada ambiente (desenvolvimento, homologação, produção) e injetá-las nos Pods.

3.  **`Secrets`:** Para armazenar variáveis de ambiente **sensíveis** (como senhas de banco de dados) de forma segura e injetá-las nos Pods.

4.  **`Service`:** Para expor sua aplicação dentro do cluster e, opcionalmente, externamente (usando `LoadBalancer` ou `NodePort`).

5.  **`Ingress` (Opcional):** Para roteamento de tráfego HTTP/S externo, balanceamento de carga e gerenciamento de domínios.

A beleza é que a **mesma imagem Docker** seria usada em todos os ambientes do Kubernetes, com a diferenciação sendo feita pelos `ConfigMaps` e `Secrets`.

---
