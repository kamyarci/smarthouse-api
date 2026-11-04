import type { Page } from "../types/page";
import type { SensorReading } from "../types/reading";
import { request } from "./http";

export async function fetchRecentReadings(
	deviceId: string,
	size: number,
	signal?: AbortSignal,
): Promise<SensorReading[]> {
	const params = new URLSearchParams({
		deviceId,
		page: "0",
		size: String(size),
	});
	const page = await request<Page<SensorReading>>(`/api/readings?${params}`, {
		signal,
	});
	return [...page.content].reverse();
}
