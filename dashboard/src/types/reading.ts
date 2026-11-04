export type HeaterMode = "AUTO" | "ON" | "OFF";

export interface SensorReading {
	id: number;
	deviceId: string;
	temperature: number | null;
	humidity: number | null;
	motionDetected: boolean;
	foodWeightKg: number | null;
	foodPercentage: number | null;
	waterWeightKg: number | null;
	waterPercentage: number | null;
	heaterOn: boolean | null;
	heaterMode: HeaterMode | null;
	recordedAt: string;
}
