export type AlertType =
	| "LOW_TEMPERATURE"
	| "HIGH_TEMPERATURE"
	| "LOW_WATER"
	| "LOW_FOOD";
export type AlertSeverity = "WARNING" | "HIGH" | "CRITICAL";
export type AlertStatus = "PENDING" | "IN_PROGRESS" | "RESOLVED";

export interface Alert {
	id: number;
	deviceId: string;
	type: AlertType;
	severity: AlertSeverity;
	status: AlertStatus;
	message: string;
	triggerValue: number | null;
	sensorReadingId: number | null;
	createdAt: string;
	updatedAt: string;
	acknowledgedAt: string | null;
	resolvedAt: string | null;
}

export interface AlertAction {
	volunteerName?: string;
	note?: string;
}
