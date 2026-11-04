## SmartHouse

O projeto tem o intuito de adaptar casinhas para animais de rua com ESP32, possibilitando a medição de temperatura e umidade, a verificação de presença e de níveis de ração e água. Também com a possibilidade de ligar uma manta térmica para os dias que faz frio e tem animal dentro da casinha. Esses dados são coletados e enviados por MQTT para uma API que gera alertas para voluntários que se disporão de ir manualmente verificar as casinhas.

## Como rodar (Docker)

Requisito: Docker instalado e aberto.

1. Copie o .env.example para .env e preencha a senha do banco e as chaves de API (gere com openssl rand -hex 24).
2. Na raiz do projeto, rode:
docker compose up --build

Sobe o banco, a API (http://localhost:8080) e o dashboard (http://localhost:5173). As tabelas são criadas pelo Flyway na primeira subida.

Para parar: Ctrl+C. Para subir de novo sem recompilar: docker compose up

A rede precisa liberar o broker MQTT (broker.hivemq.com, porta 1883). Algumas redes podem bloquear.d

## Como rodar sem Docker

Requisitos: Java 21, PostgreSQL e Node 22.

API:
1. Crie o banco smarthouse no PostgreSQL.
2. Copie o .env.example para .env e preencha.
3. Rode:
./mvnw spring-boot:run

Dashboard:
1. Na pasta dashboard, copie o .env.example para .env.local e coloque em VITE_ADMIN_KEY o mesmo valor de ADMIN_API_KEY.
2. Rode:
yarn
yarn dev

As requisições de escrita na API precisam do header X-Admin-Key (ou X-Device-Key no POST /api/readings).

## Firmware

Simulação no Wokwi: https://wokwi.com/projects/479770593159350273

Para rodar a partir do repositório, copie firmware/smarthouse-esp32/secrets.example.h para secrets.h e preencha. No Wokwi a rede é Wokwi-GUEST, sem senha. A montagem está em firmware/smarthouse-esp32/diagram.json.

## Testes
./mvnw test -Dtest=AlertClassifierTest
