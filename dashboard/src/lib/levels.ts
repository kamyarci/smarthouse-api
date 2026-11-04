import type { AlertSeverity } from "../types/alert";
import type { Settings } from "../types/settings";

export type Level = "ok" | "warning" | "high" | "critical";

const SEVERITY_LEVEL: Record<AlertSeverity, Level> = {
	WARNING: "warning",
	HIGH: "high",
	CRITICAL: "critical",
};

export function temperatureLevel(
	temperature: number | null,
	settings: Settings | null,
): Level {
	if (temperature === null || settings === null) return "ok";
	if (temperature < settings.lowTemperatureCriticalBelow) return "critical";
	if (temperature < settings.lowTemperatureHighBelow) return "high";
	if (temperature < settings.lowTemperatureWarningBelow) return "warning";
	if (temperature > settings.highTemperatureAbove)
		return SEVERITY_LEVEL[settings.highTemperatureSeverity];
	return "ok";
}

export function resourceLevel(
	percentage: number | null,
	warning: number,
	high: number,
	critical: number,
): Level {
	if (percentage === null) return "ok";
	if (percentage <= critical) return "critical";
	if (percentage <= high) return "high";
	if (percentage <= warning) return "warning";
	return "ok";
}
