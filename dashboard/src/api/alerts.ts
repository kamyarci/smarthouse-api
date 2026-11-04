import type { Alert, AlertAction } from "../types/alert";
import { request } from "./http";

export function acknowledgeAlert(
	id: number,
	action: AlertAction = {},
): Promise<Alert> {
	return request<Alert>(`/api/alerts/${id}/acknowledge`, {
		method: "PATCH",
		body: action,
		admin: true,
	});
}

export function resolveAlert(
	id: number,
	action: AlertAction = {},
): Promise<Alert> {
	return request<Alert>(`/api/alerts/${id}/resolve`, {
		method: "PATCH",
		body: action,
		admin: true,
	});
}
