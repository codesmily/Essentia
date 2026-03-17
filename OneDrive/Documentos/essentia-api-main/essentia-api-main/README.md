
<div id="top">

<p align="center" style="font-size: 4rem; font-weight: bold">
    API ESSENTIA
<div align="center">
<a href="https://github.com/lemos000/essentia-api">

</a>
</div>
<p align="center">
  <a href="https://github.com/lemos000/readme-ai/actions">
    <img src="https://img.shields.io/badge/version-1.0-blue" alt="Github Actions">
  </a>
  <a href="https://opensource.org/license/mit/">
    <img src="https://img.shields.io/github/license/eli64s/readme-ai?logo=opensourceinitiative&logoColor=white&label=License&color=8A2BE2" alt="MIT License">
  </a>
</p>


<p align="center">
 <em>Projeto acadêmico realizado em parceria com a instituição FIAP</em>
</p>

# API Essentia - Documentação Técnica

## 1. Visão Geral do Projeto

Este documento fornece uma especificação técnica detalhada para o sistema de backend Essentia. A aplicação é uma API RESTful desenvolvida em Java, projetada para gerenciar autenticação de usuários, registro de estados emocionais e anotações pessoais. A arquitetura do sistema prioriza a modularidade, escalabilidade e manutenibilidade através de um modelo de implantação em contêineres.

## 2. Arquitetura do Sistema

A aplicação implementa uma Arquitetura Hexagonal (Ports and Adapters) para desacoplar a lógica de negócio principal das preocupações de infraestrutura externa.

* **Núcleo de Domínio (`domain`)**: Esta camada encapsula as entidades de negócio centrais (`User`, `Note`, `Emotion`), objetos de valor e regras de negócio. Ela define as portas primárias (interfaces) para persistência de dados (`UserRepository`, `NoteRepository`, etc.), permanecendo completamente independente de qualquer framework ou tecnologia específica.
* **Camada de Aplicação (`application`)**: Esta camada contém os serviços de aplicação (casos de uso) que orquestram a lógica de negócio definida no domínio. Ela depende das interfaces de porta do domínio para interagir com a camada de persistência.
* **Camada de Adaptadores (`adapters`)**: Esta camada contém as implementações concretas das portas e os mecanismos para interação com sistemas externos.
    * **Adaptadores de Entrada/Driving (`in`)**: Estes lidam com as requisições de entrada. Isso inclui os controladores REST (`@RestController`) que expõem os endpoints da API e a cadeia de filtros de segurança (`JwtAuthenticationFilter`) que processa as requisições HTTP de entrada.
    * **Adaptadores de Saída/Driven (`out`)**: Estas são as implementações das portas de saída. Isso inclui as implementações dos repositórios do MongoDB (`MongoUserRepository`, `MongoNoteRepository`) que interagem com o banco de dados e o provedor de JWT (`JwtTokenProvider`) para o gerenciamento de tokens de segurança.

## 3. Stack de Tecnologias

O projeto é construído sobre a seguinte stack de tecnologias e bibliotecas, conforme definido no arquivo `pom.xml`:

| Categoria | Tecnologia / Biblioteca | Versão | Propósito |
| --- | --- | --- | --- |
| **Plataforma / Framework** | Java | 21 | Linguagem de programação principal |
| | Spring Boot | 3.5.5 | Framework de aplicação para construir apps autônomos e de nível de produção |
| **Web / API** | Spring Web | 3.5.5 | Framework para construção de APIs RESTful |
| | SpringDoc OpenAPI | 2.8.13 | Geração automatizada de documentação OpenAPI 3.0 |
| **Persistência de Dados** | Spring Data MongoDB | 3.5.5 | Fornece integração com o banco de dados MongoDB |
| | MongoDB | 7.0 | Banco de dados NoSQL orientado a documentos |
| **Segurança** | Spring Security | 3.5.5 | Framework de autenticação e autorização |
| | JJWT (JSON Web Token for Java) | 0.11.5 | Biblioteca para criação e verificação de JWTs |
| **Build / Dependências** | Apache Maven | 4.0.0 | Gerenciamento de dependências e automação de build de projetos |
| **Containerização** | Docker | - | Plataforma para desenvolver, enviar e executar aplicações em contêineres |
| | Docker Compose | - | Ferramenta para definir e executar aplicações Docker multi-contêiner |
| **Utilitários** | Project Lombok | - | Reduz código boilerplate (getters, setters, etc.) via anotações |
| | Spring Boot Actuator | 3.5.5 | Fornece recursos prontos para produção (monitoramento, métricas) |


## 4. Configuração e Execução do Ambiente

### 4.1. Pré-requisitos

* Docker Engine
* Docker Compose

### 4.2. Processo de Build

A aplicação é construída usando um `Dockerfile` de múltiplos estágios.
* **Estágio 1 (Build)**: Utiliza uma imagem `maven:3.9.9-eclipse-temurin-21`. Primeiramente, resolve as dependências do `pom.xml` e, em seguida, compila e empacota o código-fonte da aplicação em um arquivo JAR (`essentia-0.0.1-SNAPSHOT.jar`).
* **Estágio 2 (Runtime)**: Utiliza uma imagem mínima `eclipse-temurin:21-jre-alpine` para um tamanho menor. O arquivo JAR do estágio de build é copiado para este estágio. A aplicação é executada como um usuário não-root (`USER 1001`) para maior segurança.

### 4.3. Implantação

A implantação é gerenciada via `docker-compose.yml`. A configuração consiste em dois serviços:
1.  **`app`**: O contêiner da aplicação Spring Boot.
    * Constrói a imagem a partir do `Dockerfile` local.
    * Mapeia a porta do host (padrão: 8080) para a porta 8080 exposta do contêiner.
    * Depende que o serviço `mongo` esteja no estado `service_healthy` antes de iniciar.
    * Recebe variáveis de ambiente para o perfil Spring ativo (`APP_ENV`) e a URI de conexão do MongoDB.
2.  **`mongo`**: O contêiner do banco de dados MongoDB.
    * Usa a imagem oficial `mongo:7.0`.
    * Mapeia a porta do host (padrão: 27017) para a porta 27017 do contêiner.
    * Persiste os dados em um volume local (`./mongo_data_${APP_ENV}`) para evitar a perda de dados ao reiniciar o contêiner.
    * Inclui uma verificação de saúde (`healthcheck`) que periodicamente executa um ping no banco de dados para garantir que ele esteja operacional.

### 4.4. Comando de Execução

Para construir as imagens e iniciar os contêineres em modo desacoplado (`detached`), execute o seguinte comando a partir do diretório raiz do projeto:

```bash
bash run.sh
```

Para rodar os testes, é necessário utilizar os seguinte comando:
```bash
mvn clean test -Dspring.profiles.active=dev
```
Lembre-se de alterar suas credencias do banco de dados no arquivo application-dev.properties


Isso é o suficiente, baseado no seu ambiente ele irá iniciar o container.

## 5. Especificação dos Endpoints da API

A API é documentada via OpenAPI em `/swagger-ui/index.html`.

### 5.1. Autenticação (`/auth`)

* **`POST /auth/register`**: Registra um novo principal de usuário.
    * **Corpo da Requisição**: `UserCreateRequestDto` (`{ "username": "string", "email": "string", "password": "string" }`)
* **`POST /auth/login`**: Autentica um usuário e retorna um JWT.
    * **Corpo da Requisição**: `LoginRequestDto` (`{ "email": "string", "password": "string" }`)
    * **Resposta**: `AuthResponseDto` (`{ "token": "string" }`)

### 5.2. Emoções (`/emotions`)

*Requer cabeçalho `Authorization: Bearer <JWT>`.*

* **`POST /emotions`**: Cria um novo registro de emoção.
* **`GET /emotions`**: Recupera todos os registros de emoção para o principal autenticado.
* **`GET /emotions/{id}`**: Recupera um registro de emoção específico por seu identificador único.
* **`DELETE /emotions/{id}`**: Exclui um registro de emoção específico.

### 5.3. Notas (`/notes`)

*Requer cabeçalho `Authorization: Bearer <JWT>`.*

* **`POST /notes`**: Cria uma nova entidade de nota.
    * **Corpo da Requisição**: `NoteRequestDTO` (`{ "title": "string", "content": "string" }`)
* **`GET /notes`**: Recupera todas as entidades de nota para o principal autenticado.
* **`GET /notes/{id}`**: Recupera uma nota específica por seu identificador único.
* **`PUT /notes/{id}`**: Atualiza uma entidade de nota existente.
* **`DELETE /notes/{id}`**: Exclui uma entidade de nota específica.

## 6. Implementação de Segurança

A autenticação é `stateless` (sem estado) e tratada por JSON Web Tokens (JWT).

1.  **Autenticação**: O usuário submete as credenciais ao endpoint `/auth/login`.
2.  **Geração de Token**: Após a autenticação bem-sucedida, o serviço `JwtTokenProvider` gera um JWT assinado contendo a identidade do usuário (subject) e `claims`.
3.  **Autorização**: Para requisições subsequentes a endpoints protegidos, o cliente deve incluir o JWT no cabeçalho `Authorization` com o esquema `Bearer`.
4.  **Validação do Token**: O `JwtAuthenticationFilter`, um filtro personalizado na cadeia de filtros do Spring Security, intercepta cada requisição. Ele valida a assinatura e a expiração do token. Se for válido, ele define o objeto `Authentication` no `SecurityContextHolder`, concedendo acesso ao recurso solicitado.

## 7. Camada de Persistência

A persistência de dados é gerenciada pelo Spring Data MongoDB.

* **Documentos**: Cada entidade de domínio (`User`, `Note`, `Emotion`) tem uma classe de documento correspondente (`UserDocument`, `NoteDocument`, etc.) anotada com `@Document`. Essas classes são mapeadas para coleções no banco de dados MongoDB.
* **Repositórios**: As interfaces do Spring Data (`SpringDataUserRepository`, etc.) são usadas para definir operações padrão de acesso a dados (CRUD).
* **Implementação do Adaptador**: As classes do adaptador de saída (`MongoUserRepository`, etc.) implementam as interfaces de porta do domínio e utilizam as interfaces do Spring Data para executar as operações no banco de dados.





## 🎗 Licença

Copyright © 2025 [essentia-api](https://github.com/lemos000/essentia-api). <br />
Lançado sob a licença [MIT](https://github.com/lemos000/essentia-api/blob/main/LICENSE).

<div align="left">

[Voltar ao topo](#top)

</div>

<img src="https://raw.githubusercontent.com/eli64s/readme-ai/eb2a0b4778c633911303f3c00f87874f398b5180/docs/docs/assets/svg/line-gradient.svg" alt="line break" width="100%" height="3px">
