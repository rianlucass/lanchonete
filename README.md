Sistema de Gestão de Lanchonete

Este projeto é uma API Spring Boot para gestão de produtos, estoque e pedidos de uma lanchonete. Ele permite cadastrar, editar e remover produtos, registrar pedidos com múltiplos itens, atualizar automaticamente o estoque e gerar relatórios detalhados sobre vendas, produtos mais vendidos, faturamento diário e formas de pagamento mais utilizadas.

Como Rodar a Aplicação Localmente
Pré-requisitos

Antes de começar, certifique-se de ter instalado:

Java JDK 17+
IntelliJ IDEA(recomendo)

Abra o Intellij e busque pelo projeto no desktop

Instalar Dependências
Execute o Maven para baixar todas as dependências no bash:

   ```bash
   mvn clean install
   ```
ou na propria IDE, no arquivo pom.xml e clique na opção maven no canto superior direito

Configurar o Banco de Dados (se aplicável)

Abra o arquivo de configuração:

```bash
src/main/resources/application.properties
```

ou

```bash
src/main/resources/application.yml
```

Exemplo de configuração para MySQL:

spring.datasource.url=jdbc:mysql://localhost:3306/nome_do_banco
spring.datasource.username=root
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update

⚠️ Certifique-se de que o banco de dados está criado localmente.

Execute o arquivo LanchoneteApplication.java no diretorio src/main/java/com/br/api/lanchonete/LanchoneteApplication.java

Verifique se pediu para habilitar o Lombok, se sim, habilite
E projetoo vai ser executado



