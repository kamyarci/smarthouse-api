# Notas para o relatório

Anotações dos testes e decisões, para usar na escrita do relatório.

## Decisões técnicas

- Wi-Fi + MQTT no protótipo; LoRaWAN continua sendo a proposta para implantação em escala. O Wokwi não simula LoRa.
- Broker público broker.hivemq.com. A API e a ESP32 conectam nele; nenhuma precisa saber o endereço da outra.
- Mensagens retidas nos tópicos de comando (config e manta): a ESP32 recebe o último comando ao ligar ou reconectar.
- A API republica o intervalo do banco sempre que conecta no broker, para corrigir comandos perdidos com o broker fora.
- A ESP32 envia a leitura na hora quando a manta muda de estado ou chega um comando; o resto segue o intervalo configurado.
- Capacidades adotadas: pote de ração 2 kg, pote de água 3 L. São constantes do firmware. A célula de carga mede até 5 kg; no físico, a tara com o pote vazio desconta o peso do recipiente.
- Alerta de frio começa em 15 °C (aviso cedo ao voluntário); a manta só liga abaixo de 10 °C (economia de energia).

## Atuador (manta térmica)

- Relé no GPIO 17 acionando uma manta de 12 V; no Wokwi a manta é representada por um LED no contato NO.
- AUTO: liga abaixo de 10 °C com animal presente; desliga acima de 13 °C ou sem animal (histerese 10–13 °C).
- ON/OFF: controle manual pela plataforma.
- Segurança: acima de 25 °C ou com falha no sensor, desliga em qualquer modo, inclusive no manual.
- Viabilidade: as casinhas não têm tomada, então a manta dependeria de painel solar com bateria. O consumo não foi dimensionado no protótipo. A regra por presença reduz o consumo.
- O atuador foi escolhido para demonstrar reação local a uma leitura (R1 e R2); não é parte da solução definitiva.

## Presença do animal

- No Wokwi, o PIR detecta só movimento. A presença é inferida por uma janela de 5 minutos após o último movimento. Se o animal ficar parado mais que isso, o firmware entende que saiu e desliga a manta.
- Aumentar a janela cobriria um animal dormindo, mas manteria a manta ligada mais tempo depois que ele sai.
- No físico, o LD2410C (radar mmWave) detecta presença estática e elimina essa limitação. A saída digital é a mesma, então o código não muda.

## Testes realizados

| Teste | Como | Resultado |
|---|---|---|
| Regra da manta (frio + animal, histerese, sem animal) | Sliders do DHT e Simulate motion no Wokwi | OK |
| Corte de segurança (testado com o limite antigo de 30 °C; refazer com 25 °C) | Modo ON e DHT acima do limite | Manta desligou |
| Funcionamento sem internet (R2) | SSID inexistente no firmware | Manta seguiu reagindo; leituras marcadas como offline |
| Leitura via MQTT até o banco | Wokwi publicando, consulta em /api/readings/latest | OK |
| Ciclo do alerta | Variação de temperatura e balanças | Criou, mudou de severidade sem duplicar, resolveu sozinho |
| Comando da manta | PATCH com ON, OFF, AUTO e XYZ | 202 nos válidos; 400 no inválido com mensagem clara |
| Comando com a ESP32 offline | PATCH com o Wokwi desconectado | Comando retido e aplicado ao reconectar |
| Chave de API | Sem chave, chave errada, chave certa | 401, 401, 202 |
| Testes unitários do AlertClassifier | ./mvnw test | 24 passaram |
| Dashboard: leitura, alertas, gráfico | Wokwi rodando | OK |
| Dashboard: Posso verificar / Resolver | Botões no alerta de frio | Em atendimento → resolvido; alerta novo quando o frio persiste |
| Dashboard: botões da manta | Ligar, Desligar, Automático | Confirmação quase imediata após o envio imediato |
| Dashboard com a casinha offline | Wokwi parado por 1 min | Aviso "sem enviar dados há 1 min" |
| Dashboard com a API fora | API parada | Aviso de conexão; voltou sozinho ao religar |
| Reconexão da API | API reiniciada com a ESP32 rodando | Config republicada e recebida; leituras voltaram |

## Falhas encontradas e correções

- Coluna `news_severity` na entidade não batia com `new_severity` da migration: a API não subia. Corrigido o mapeamento.
- Firmware travava sem internet (while esperando Wi-Fi e MQTT). Reescrito para reconectar sem bloquear o loop.
- Retentativa de Wi-Fi a cada 10 s interrompia a conexão em andamento no Wokwi. Aumentado para 30 s com WiFi.reconnect().
- .env salvo com quebra de linha do Windows: as chaves ficavam com \r e a autenticação falhava. Arquivo convertido e chaves com strip().
- Rede da faculdade bloqueia o broker no DNS. Testes feitos no 4G.
- Dashboard mostrava "exposto ao frio" com alerta de calor. Texto passou a considerar o tipo do alerta.
- Dashboard demorava até 20 s para refletir a manta, causando cliques repetidos. Firmware passou a enviar na hora quando a manta muda.

## Limitações conhecidas

- Broker público sem autenticação: quem souber o tópico pode publicar.
- Leituras enviadas com a API fora do ar se perdem (sem sessão persistente nem buffer na ESP32).
- A chave de admin fica no front e é visível no navegador.
- A simulação roda abaixo do tempo real: os intervalos medidos no servidor ficam maiores que os configurados.
- Energia da manta não dimensionada.

## Segurança (adicional)

- Credenciais fora do repositório: .env, .env.local e secrets.h no .gitignore, com arquivos de exemplo.
- Chave de API nas escritas, comparada em tempo constante; sem chave configurada, a escrita é recusada.
- deviceId validado (letras, números, _ e -), impedindo curingas MQTT (+, #) e / nos tópicos.
- Falta escrever o checklist OWASP IoT Top 10.

## Uso de IA

- Preencher: o que foi feito com apoio de IA e como o grupo verificou (testes acima, revisão do código).
