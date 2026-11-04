import { useCallback, useEffect, useRef, useState } from "react";

interface PollingState<T> {
	data: T | null;
	error: string | null;
	loading: boolean;
	refresh: () => void;
}

export function usePolling<T>(
	fetcher: (signal: AbortSignal) => Promise<T>,
	intervalMs: number,
): PollingState<T> {
	const [data, setData] = useState<T | null>(null);
	const [error, setError] = useState<string | null>(null);
	const [loading, setLoading] = useState(true);
	const fetcherRef = useRef(fetcher);
	const [tick, setTick] = useState(0);

	useEffect(() => {
		fetcherRef.current = fetcher;
	}, [fetcher]);

	const refresh = useCallback(() => setTick((value) => value + 1), []);

	useEffect(() => {
		const controller = new AbortController();

		async function load() {
			try {
				const result = await fetcherRef.current(controller.signal);
				setData(result);
				setError(null);
			} catch (err) {
				if (controller.signal.aborted) return;
				setError(
					err instanceof Error ? err.message : "Erro inesperado",
				);
			} finally {
				if (!controller.signal.aborted) setLoading(false);
			}
		}

		load();
		const id = window.setInterval(load, intervalMs);
		return () => {
			controller.abort();
			window.clearInterval(id);
		};
	}, [intervalMs, tick]);

	return { data, error, loading, refresh };
}
