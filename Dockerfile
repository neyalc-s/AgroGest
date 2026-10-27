# Build reproducible do WAR com JDK 17 e execução em Tomcat 10.1.
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /build
COPY pom.xml .
COPY src ./src

# Compila e executa os testes JUnit durante o build da imagem.
RUN mvn -B clean package

FROM tomcat:10.1-jdk17-temurin

ENV AGROGEST_DATA_DIR=/tmp/agrogest-data \
    AGROGEST_DEMO_LOGIN=true

RUN rm -rf /usr/local/tomcat/webapps/* \
    && mkdir -p /tmp/agrogest-data

COPY --from=build /build/target/agrogest.war /usr/local/tomcat/webapps/agrogest.war
COPY docker/server.xml /usr/local/tomcat/conf/server.xml
COPY docker/entrypoint.sh /usr/local/bin/agrogest-entrypoint.sh

RUN chmod 0755 /usr/local/bin/agrogest-entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/usr/local/bin/agrogest-entrypoint.sh"]
