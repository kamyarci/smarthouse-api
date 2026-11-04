import type { DashboardSummary } from "../types/dashboard";
import { request } from "./http";

export function fetchSummary(
	deviceId: string,
	signal?: AbortSignal,
): Promise<DashboardSummary> {
	return request<DashboardSummary>(
		`/api/dashboard/summary?deviceId=${encodeURIComponent(deviceId)}`,
		{ signal },
	);
}
