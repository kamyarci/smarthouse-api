import type { AlertSeverity, AlertStatus, AlertType } from "../types/alert";
import type { HeaterMode } from "../types/reading";

const dateTime = new Intl.DateTimeFormat("pt-BR", {
	dateStyle: "short",
	timeStyle: "medium",
});
const time = new Intl.DateTimeFormat("pt-BR", { timeStyle: "short" });

export function formatNumber(
	value: number | null,
	unit: string,
	digits = 1,
): string {
	if (value === null) return "—";
	return `${value.toLocaleString("pt-BR", { minimumFractionDigits: digits, maximumFractionDigits: digits })}${unit}`;
}

export function formatDateTime(iso: string): string {
	return dateTime.format(new Date(iso));
}

export function formatTime(iso: string): string {
	return time.format(new Date(iso));
}

export function formatElapsed(seconds: number | null): string {
	if (seconds === null) return "—";
	if (seconds < 60) return `${seconds} s`;
	if (seconds < 3600) return `${Math.floor(seconds / 60)} min`;
	return `${Math.floor(seconds / 3600)} h`;
}

export const alertTypeLabel: Record<AlertType, string> = {
	LOW_TEMPERATURE: "Frio",
	HIGH_TEMPERATURE: "Calor",
	LOW_WATER: "Água baixa",
	LOW_FOOD: "Ração baixa",
};

export const severityLabel: Record<AlertSeverity, string> = {
	WARNING: "Atenção",
	HIGH: "Alto",
	CRITICAL: "Crítico",
};

export const statusLabel: Record<AlertStatus, string> = {
	PENDING: "Aguardando voluntário",
	IN_PROGRESS: "Em atendimento",
	RESOLVED: "Resolvido",
};

export const heaterModeLabel: Record<HeaterMode, string> = {
	AUTO: "Automático",
	ON: "Ligada (manual)",
	OFF: "Desligada (manual)",
};
