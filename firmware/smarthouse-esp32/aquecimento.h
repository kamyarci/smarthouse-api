#pragma once
#include <Arduino.h>

enum class ModoAquecimento { AUTOMATICO, LIGADO, DESLIGADO };

struct LimitesAquecimento {
  float ligarAbaixoDe;
  float desligarAcimaDe;
  float temperaturaSeguranca;
};

class ControleAquecimento {
public:
  explicit ControleAquecimento(LimitesAquecimento limites) : limites(limites) {}

  void definirModo(ModoAquecimento novoModo) { modo = novoModo; }
  ModoAquecimento modoAtual() const { return modo; }
  bool ligado() const { return aquecendo; }

  bool atualizar(float temperatura, bool animalPresente) {
    if (isnan(temperatura) || temperatura >= limites.temperaturaSeguranca) {
      aquecendo = false;
      return aquecendo;
    }

    switch (modo) {
      case ModoAquecimento::LIGADO:
        aquecendo = true;
        break;
      case ModoAquecimento::DESLIGADO:
        aquecendo = false;
        break;
      case ModoAquecimento::AUTOMATICO:
        if (!animalPresente) {
          aquecendo = false;
        } else if (temperatura < limites.ligarAbaixoDe) {
          aquecendo = true;
        } else if (temperatura > limites.desligarAcimaDe) {
          aquecendo = false;
        }
        break;
    }
    return aquecendo;
  }

private:
  LimitesAquecimento limites;
  ModoAquecimento modo = ModoAquecimento::AUTOMATICO;
  bool aquecendo = false;
};

inline const char* modoParaTexto(ModoAquecimento modo) {
  switch (modo) {
    case ModoAquecimento::LIGADO: return "ON";
    case ModoAquecimento::DESLIGADO: return "OFF";
    default: return "AUTO";
  }
}

inline bool textoParaModo(const char* texto, ModoAquecimento& modo) {
  if (strcmp(texto, "AUTO") == 0) { modo = ModoAquecimento::AUTOMATICO; return true; }
  if (strcmp(texto, "ON") == 0)   { modo = ModoAquecimento::LIGADO; return true; }
  if (strcmp(texto, "OFF") == 0)  { modo = ModoAquecimento::DESLIGADO; return true; }
  return false;
}
