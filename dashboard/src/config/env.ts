const apiUrl = import.meta.env.VITE_API_URL ?? "";
const adminKey = import.meta.env.VITE_ADMIN_KEY ?? "";

export const env = {
	apiUrl,
	adminKey,
	deviceId: import.meta.env.VITE_DEVICE_ID ?? "abrigo-01",
	pollIntervalMs: Number(import.meta.env.VITE_POLL_INTERVAL_MS ?? 5000),
};

export const missingEnv = [
	!apiUrl && "VITE_API_URL",
	!adminKey && "VITE_ADMIN_KEY",
].filter((name): name is string => Boolean(name));
