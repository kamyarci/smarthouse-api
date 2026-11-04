import { formatDateTime, formatNumber } from "../lib/format";
import { resourceLevel, temperatureLevel, type Level } from "../lib/levels";
import type { SensorReading } from "../types/reading";
import type { Settings } from "../types/settings";

interface Props {
	reading: SensorReading | null;
	settings: Settings | null;
}

interface MetricProps {
	label: string;
	value: string;
	level?: Level;
}

function Metric({ label, value, level = "ok" }: MetricProps) {
	return (
		<div className={`metric metric--${level}`}>
			<span className="metric__label">{label}</span>
			<span className="metric__value">{value}</span>
		</div>
	);
}

export function StatusCard({ reading, settings }: Props) {
	return (
		<section className="card">
			<header className="card__header">
				<h2>Condições da casinha</h2>
				{reading && (
					<span className="card__hint">
						Atualizado em {formatDateTime(reading.recordedAt)}
					</span>
				)}
			</header>

			{reading ? (
				<div className="metrics">
					<Metric
						label="Temperatura"
						value={formatNumber(reading.temperature, " °C")}
						level={temperatureLevel(reading.temperature, settings)}
					/>
					<Metric
						label="Umidade"
						value={formatNumber(reading.humidity, "%", 0)}
					/>
					<Metric
						label="Animal"
						value={reading.motionDetected ? "Presente" : "Ausente"}
					/>
					<Metric
						label="Ração"
						value={formatNumber(reading.foodPercentage, "%", 0)}
						level={
							settings
								? resourceLevel(
										reading.foodPercentage,
										settings.foodWarningPercent,
										settings.foodHighPercent,
										settings.foodCriticalPercent,
									)
								: "ok"
						}
					/>
					<Metric
						label="Água"
						value={formatNumber(reading.waterPercentage, "%", 0)}
						level={
							settings
								? resourceLevel(
										reading.waterPercentage,
										settings.waterWarningPercent,
										settings.waterHighPercent,
										settings.waterCriticalPercent,
									)
								: "ok"
						}
					/>
					<Metric
						label="Manta térmica"
						value={
							reading.heaterOn === null
								? "—"
								: reading.heaterOn
									? "Ligada"
									: "Desligada"
						}
					/>
				</div>
			) : (
				<p className="empty">Sem leituras.</p>
			)}
		</section>
	);
}
