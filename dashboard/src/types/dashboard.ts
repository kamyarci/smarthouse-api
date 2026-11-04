import type { Alert, AlertSeverity } from "./alert";
import type { SensorReading } from "./reading";

export interface DashboardSummary {
	deviceId: string;
	latestReading: SensorReading | null;
	secondsSinceLastReading: number | null;
	stale: boolean;
	staleAfterSeconds: number;
	readingIntervalSeconds: number;
	activeAlerts: Alert[];
	highestActiveSeverity: AlertSeverity | null;
	possibleAnimalExposure: boolean;
}
