import type { AlertSeverity } from "./alert";

export interface Settings {
	lowTemperatureWarningBelow: number;
	lowTemperatureHighBelow: number;
	lowTemperatureCriticalBelow: number;
	highTemperatureAbove: number;
	highTemperatureSeverity: AlertSeverity;
	waterWarningPercent: number;
	waterHighPercent: number;
	waterCriticalPercent: number;
	foodWarningPercent: number;
	foodHighPercent: number;
	foodCriticalPercent: number;
	readingIntervalSeconds: number;
	updatedAt: string;
}
