import type { DashboardSummary } from "../types/dashboard";
import { formatElapsed, severityLabel } from "../lib/format";

interface Props {
	summary: DashboardSummary;
}

export function StatusBanner({ summary }: Props) {
	if (!summary.latestReading) {
		return (
			<div className="banner banner--muted">
				Nenhuma leitura recebida ainda. Verifique se a casinha está
				ligada.
			</div>
		);
	}

	if (summary.stale) {
		return (
			<div className="banner banner--offline">
				Casinha sem enviar dados há{" "}
				{formatElapsed(summary.secondsSinceLastReading)}. Os valores
				abaixo podem estar desatualizados.
			</div>
		);
	}

	if (summary.possibleAnimalExposure) {
		const heat = summary.activeAlerts.some(
			(alert) => alert.type === "HIGH_TEMPERATURE",
		);
		return (
			<div className="banner banner--critical">
				Possível animal exposto ao {heat ? "calor" : "frio"}: presença
				detectada com alerta grave de temperatura.
			</div>
		);
	}

	if (summary.highestActiveSeverity) {
		const count = summary.activeAlerts.length;
		return (
			<div
				className={`banner banner--${summary.highestActiveSeverity.toLowerCase()}`}
			>
				{count} {count === 1 ? "alerta ativo" : "alertas ativos"} ·
				maior severidade: {severityLabel[summary.highestActiveSeverity]}
			</div>
		);
	}

	return <div className="banner banner--ok">Tudo normal na casinha.</div>;
}
