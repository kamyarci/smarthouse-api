import type { HeaterMode } from "../types/reading";
import { request } from "./http";

export interface HeaterCommandResult {
	deviceId: string;
	requestedMode: HeaterMode;
}

export function setHeaterMode(
	deviceId: string,
	mode: HeaterMode,
): Promise<HeaterCommandResult> {
	return request<HeaterCommandResult>(
		`/api/device/${encodeURIComponent(deviceId)}/heater`,
		{
			method: "PATCH",
			body: { mode },
			admin: true,
		},
	);
}
