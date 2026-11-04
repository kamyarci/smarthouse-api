#include <WiFi.h>
#include <DHT.h>
#include <HX711.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include "secrets.h"
#include "leitura.h"
#include "aquecimento.h"

#define DHT_PIN 15
#define DHT_TYPE DHT22
#define PRESENCA_PIN 13
#define RELE_PIN 17

#define RACAO_DT 32
#define RACAO_SCK 33

#define AGUA_DT 25
#define AGUA_SCK 26

const char* WIFI_SSID = SECRET_WIFI_SSID;
const char* WIFI_PASSWORD = SECRET_WIFI_PASSWORD;
const int WIFI_CANAL = 6;

const char* MQTT_BROKER = SECRET_MQTT_BROKER;
const int MQTT_PORTA = SECRET_MQTT_PORTA;
const char* MQTT_CLIENT_ID = "smarthouse-abrigo-01";
const char* TOPICO_LEITURAS = "kamyla-fag/smarthouse/abrigo-01/readings";
const char* TOPICO_CONFIG = "kamyla-fag/smarthouse/config";
const char* TOPICO_AQUECIMENTO = "kamyla-fag/smarthouse/abrigo-01/heater/set";

const char* SMARTHOUSE_ID = "abrigo-01";
const float CAPACIDADE_RACAO_KG = 2.0;
const float CAPACIDADE_AGUA_KG = 3.0;
const float ESCALA_BALANCA = 420.0;

const unsigned long CICLO_CONTROLE_MS = 2000;
const unsigned long JANELA_PRESENCA_MS = 5UL * 60 * 1000;
const unsigned long RETENTATIVA_WIFI_MS = 30000;
const unsigned long RETENTATIVA_MQTT_MS = 5000;

DHT dht(DHT_PIN, DHT_TYPE);
HX711 balancaRacao;
HX711 balancaAgua;

WiFiClient wifiClient;
PubSubClient mqtt(wifiClient);

ControleAquecimento aquecimento({10.0, 13.0, 25.0});

unsigned long intervaloEnvioMs = 10000;
unsigned long ultimoEnvio = 0;
unsigned long ultimoControle = 0;
unsigned long ultimaTentativaWifi = 0;
unsigned long ultimaTentativaMqtt = 0;
unsigned long ultimaPresencaMs = 0;

bool presencaJaDetectada = false;
bool wifiEstavaConectado = false;
bool mqttEstavaConectado = false;

Leitura ultimaLeitura;

void manterWiFi() {
  bool conectado = WiFi.status() == WL_CONNECTED;

  if (conectado != wifiEstavaConectado) {
    wifiEstavaConectado = conectado;
    if (conectado) {
      Serial.print("[REDE] Wi-Fi conectado. IP: ");
      Serial.println(WiFi.localIP());
    } else {
      Serial.println("[REDE] Wi-Fi desconectado; operando offline");
    }
  }

  if (!conectado && millis() - ultimaTentativaWifi >= RETENTATIVA_WIFI_MS) {
    ultimaTentativaWifi = millis();
    Serial.println("[REDE] Tentando reconectar o Wi-Fi...");
    WiFi.reconnect();
  }
}

void manterMqtt() {
  if (WiFi.status() != WL_CONNECTED) return;

  if (mqtt.connected()) {
    mqttEstavaConectado = true;
    mqtt.loop();
    return;
  }

  if (mqttEstavaConectado) {
    mqttEstavaConectado = false;
    Serial.print("[REDE] MQTT caiu (rc=");
    Serial.print(mqtt.state());
    Serial.println(")");
  }

  if (millis() - ultimaTentativaMqtt < RETENTATIVA_MQTT_MS) return;
  ultimaTentativaMqtt = millis();

  if (mqtt.connect(MQTT_CLIENT_ID)) {
    mqtt.subscribe(TOPICO_CONFIG);
    mqtt.subscribe(TOPICO_AQUECIMENTO);
    Serial.println("[REDE] MQTT conectado");
  } else {
    Serial.print("[REDE] Falha no MQTT (rc=");
    Serial.print(mqtt.state());
    Serial.println("); nova tentativa em breve");
  }
}

void aplicarConfig(const JsonDocument& doc) {
  int segundos = doc["readingIntervalSeconds"] | 0;
  if (segundos < 5) {
    Serial.println("[CONFIG] Intervalo inválido, ignorado");
    return;
  }

  intervaloEnvioMs = segundos * 1000UL;
  Serial.print("[CONFIG] Novo intervalo: ");
  Serial.print(segundos);
  Serial.println(" s");
}

void aplicarModoAquecimento(const JsonDocument& doc) {
  const char* texto = doc["mode"] | "";
  ModoAquecimento modo;

  if (!textoParaModo(texto, modo)) {
    Serial.print("[AQUECIMENTO] Modo inválido, ignorado: ");
    Serial.println(texto);
    return;
  }

  aquecimento.definirModo(modo);
  Serial.print("[AQUECIMENTO] Modo: ");
  Serial.println(modoParaTexto(modo));

  controlarAquecimento(ultimaLeitura);
  ultimoEnvio = 0;
}

void aoReceberMensagem(char* topico, byte* payload, unsigned int tamanho) {
  JsonDocument doc;
  if (deserializeJson(doc, payload, tamanho)) {
    Serial.println("[MQTT] JSON inválido, ignorado");
    return;
  }

  if (strcmp(topico, TOPICO_CONFIG) == 0) {
    aplicarConfig(doc);
  } else if (strcmp(topico, TOPICO_AQUECIMENTO) == 0) {
    aplicarModoAquecimento(doc);
  }
}

float lerPeso(HX711& balanca) {
  if (!balanca.is_ready()) return NAN;
  return max(0.0f, balanca.get_units(1));
}

Leitura lerSensores() {
  Leitura leitura;
  leitura.temperatura = dht.readTemperature();
  leitura.umidade = dht.readHumidity();
  leitura.presenca = digitalRead(PRESENCA_PIN) == HIGH;
  leitura.racaoKg = lerPeso(balancaRacao);
  leitura.aguaKg = lerPeso(balancaAgua);
  return leitura;
}

bool animalPresente(bool movimentoAgora) {
  if (movimentoAgora) {
    ultimaPresencaMs = millis();
    presencaJaDetectada = true;
  }
  return presencaJaDetectada && millis() - ultimaPresencaMs < JANELA_PRESENCA_MS;
}

void controlarAquecimento(const Leitura& leitura) {
  bool estavaLigado = aquecimento.ligado();
  bool ligar = aquecimento.atualizar(leitura.temperatura, animalPresente(leitura.presenca));

  digitalWrite(RELE_PIN, ligar ? HIGH : LOW);

  if (ligar != estavaLigado) {
    Serial.print("[AQUECIMENTO] Manta ");
    Serial.println(ligar ? "LIGADA" : "DESLIGADA");
    ultimoEnvio = 0;
  }
}

float calcularPercentual(float peso, float capacidade) {
  if (isnan(peso)) return NAN;
  return constrain((peso / capacidade) * 100.0f, 0.0f, 100.0f);
}

String numeroOuNull(float valor, int casas) {
  return isnan(valor) ? String("null") : String(valor, casas);
}

String montarPayload(const Leitura& leitura) {
  String json = "{";
  json += "\"deviceId\":\"" + String(SMARTHOUSE_ID) + "\",";
  json += "\"temperature\":" + numeroOuNull(leitura.temperatura, 1) + ",";
  json += "\"humidity\":" + numeroOuNull(leitura.umidade, 1) + ",";
  json += "\"motionDetected\":" + String(animalPresente(leitura.presenca) ? "true" : "false") + ",";
  json += "\"foodWeightKg\":" + numeroOuNull(leitura.racaoKg, 2) + ",";
  json += "\"foodPercentage\":" + numeroOuNull(calcularPercentual(leitura.racaoKg, CAPACIDADE_RACAO_KG), 1) + ",";
  json += "\"waterWeightKg\":" + numeroOuNull(leitura.aguaKg, 2) + ",";
  json += "\"waterPercentage\":" + numeroOuNull(calcularPercentual(leitura.aguaKg, CAPACIDADE_AGUA_KG), 1) + ",";
  json += "\"heaterOn\":" + String(aquecimento.ligado() ? "true" : "false") + ",";
  json += "\"heaterMode\":\"" + String(modoParaTexto(aquecimento.modoAtual())) + "\"";
  json += "}";
  return json;
}

void imprimirLeitura(const Leitura& leitura) {
  Serial.println();
  Serial.println("===== SMARTHOUSE =====");
  Serial.print("Temperatura (C): "); Serial.println(numeroOuNull(leitura.temperatura, 1));
  Serial.print("Umidade (%): ");     Serial.println(numeroOuNull(leitura.umidade, 1));
  Serial.print("Animal presente: "); Serial.println(animalPresente(leitura.presenca) ? "SIM" : "NAO");
  Serial.print("Racao (kg): ");      Serial.println(numeroOuNull(leitura.racaoKg, 2));
  Serial.print("Agua (kg): ");       Serial.println(numeroOuNull(leitura.aguaKg, 2));
  Serial.print("Manta: ");           Serial.print(aquecimento.ligado() ? "LIGADA" : "DESLIGADA");
  Serial.print(" (modo ");           Serial.print(modoParaTexto(aquecimento.modoAtual())); Serial.println(")");
}

void publicarLeitura(const Leitura& leitura) {
  if (!mqtt.connected()) {
    Serial.println("[OFFLINE] Leitura não enviada; controle local segue ativo");
    return;
  }

  String payload = montarPayload(leitura);
  bool enviado = mqtt.publish(TOPICO_LEITURAS, payload.c_str());

  Serial.print(enviado ? "[ENVIADO] " : "[FALHA] ");
  Serial.println(payload);
}

void setup() {
  Serial.begin(115200);

  dht.begin();
  pinMode(PRESENCA_PIN, INPUT);
  pinMode(RELE_PIN, OUTPUT);
  digitalWrite(RELE_PIN, LOW);

  balancaRacao.begin(RACAO_DT, RACAO_SCK);
  balancaAgua.begin(AGUA_DT, AGUA_SCK);
  balancaRacao.set_scale(ESCALA_BALANCA);
  balancaAgua.set_scale(ESCALA_BALANCA);

  mqtt.setServer(MQTT_BROKER, MQTT_PORTA);
  mqtt.setCallback(aoReceberMensagem);
  mqtt.setBufferSize(512);

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD, WIFI_CANAL);
  ultimaTentativaWifi = millis();

  Serial.println("SmartHouse iniciada!");
}

void loop() {
  manterWiFi();
  manterMqtt();

  if (millis() - ultimoControle >= CICLO_CONTROLE_MS) {
    ultimoControle = millis();
    ultimaLeitura = lerSensores();
    controlarAquecimento(ultimaLeitura);
  }

  if (millis() - ultimoEnvio >= intervaloEnvioMs) {
    ultimoEnvio = millis();
    imprimirLeitura(ultimaLeitura);
    publicarLeitura(ultimaLeitura);
  }
}
}
