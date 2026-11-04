import { env } from "../config/env";

export class ApiError extends Error {
	readonly status: number;

	constructor(status: number, message: string) {
		super(message);
		this.name = "ApiError";
		this.status = status;
	}
}

interface RequestOptions {
	method?: "GET" | "POST" | "PATCH";
	body?: unknown;
	admin?: boolean;
	signal?: AbortSignal;
}

export async function request<T>(
	path: string,
	options: RequestOptions = {},
): Promise<T> {
	const { method = "GET", body, admin = false, signal } = options;

	const headers: Record<string, string> = {};
	if (body !== undefined) headers["Content-Type"] = "application/json";
	if (admin) headers["X-Admin-Key"] = env.adminKey;

	let response: Response;
	try {
		response = await fetch(`${env.apiUrl}${path}`, {
			method,
			headers,
			body: body === undefined ? undefined : JSON.stringify(body),
			signal,
		});
	} catch (error) {
		if (error instanceof DOMException && error.name === "AbortError")
			throw error;
		throw new ApiError(0, "Não foi possível conectar à API");
	}

	if (!response.ok) {
		throw new ApiError(response.status, await readProblemDetail(response));
	}

	const text = await response.text();
	return (text ? JSON.parse(text) : undefined) as T;
}

async function readProblemDetail(response: Response): Promise<string> {
	try {
		const problem = (await response.json()) as {
			detail?: string;
			title?: string;
		};
		return problem.detail ?? problem.title ?? `Erro ${response.status}`;
	} catch {
		return `Erro ${response.status}`;
	}
}
