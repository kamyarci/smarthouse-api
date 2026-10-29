## SmartHouse

O projeto tem o intuito de adaptar casinhas para animais de rua com ESP32, possibilitando a medição de temperatura e umidade, a verificação de presença e de níveis de ração e água. Também com a possibilidade de ligar uma manta térmica para os dias que faz frio e tem animal dentro da casinha. Esses dados são coletados e enviados por MQTT para uma API que gera alertas para voluntários que se disporão de ir manualmente verificar as casinhas.

## Requisitos

- Java 21
- PostgreSQL

## API
1. Crie o banco smarthouse no PostgreSQL.
2. Copie o .env.example para .env e preencha a senha do banco e as chaves de API.
3. Rode:
set -a; source .env; set +a
./mvnw spring-boot:run

A API sobe em http://localhost:8080. As tabelas são criadas pelo Flyway. As requisições de escrita precisam do header X-Admin-Key (ou X-Device-Key no POST /api/readings).

## Firmware

Simulação no Wokwi: https://wokwi.com/projects/479149241604169729
Para rodar a partir do repositório, copie firmware/smarthouse-esp32/secrets.example.h para secrets.h e preencha. No Wokwi a rede é Wokwi-GUEST, sem senha.

## Testes
./mvnw test -Dtest=AlertClassifierTest

