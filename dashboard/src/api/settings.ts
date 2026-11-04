import type { Settings } from "../types/settings";
import { request } from "./http";

export function fetchSettings(signal?: AbortSignal): Promise<Settings> {
	return request<Settings>("/api/settings", { signal });
}
