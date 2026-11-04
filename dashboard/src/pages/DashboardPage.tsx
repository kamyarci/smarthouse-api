import { useCallback } from "react";
import { fetchSummary } from "../api/dashboard";
import { fetchSettings } from "../api/settings";
import { AlertList } from "../components/AlertList";
import { HeaterControl } from "../components/HeaterControl";
import { StatusBanner } from "../components/StatusBanner";
import { StatusCard } from "../components/StatusCard";
import { TemperatureChart } from "../components/TemperatureChart";
import { usePolling } from "../hooks/usePolling";

interface Props {
	deviceId: string;
	pollIntervalMs: number;
}

const CHART_INTERVAL_FACTOR = 3;
const SETTINGS_INTERVAL_MS = 60_000;

export function DashboardPage({ deviceId, pollIntervalMs }: Props) {
	const summaryFetcher = useCallback(
		(signal: AbortSignal) => fetchSummary(deviceId, signal),
		[deviceId],
	);
	const {
		data: summary,
		error,
		loading,
		refresh,
	} = usePolling(summaryFetcher, pollIntervalMs);
	const { data: settings } = usePolling(fetchSettings, SETTINGS_INTERVAL_MS);

	return (
		<main className="page">
			<header className="page__header">
				<div>
					<h1 className="page__brand">
						SmartHouse
						<img
							src="/favicon.png"
							alt="cachorrinho"
							className="page__logo"
						/>
					</h1>
					<p className="page__subtitle">Casinha {deviceId}</p>
				</div>
			</header>

			{error && (
				<div className="banner banner--offline">
					{summary
						? `Sem conexão com a API (${error}). Mostrando os últimos dados.`
						: error}
				</div>
			)}

			{loading && !summary && <p className="empty">Carregando…</p>}

			{summary && (
				<>
					<StatusBanner summary={summary} />
					<div className="grid">
						<StatusCard
							reading={summary.latestReading}
							settings={settings}
						/>
						<HeaterControl
							deviceId={deviceId}
							currentMode={
								summary.latestReading?.heaterMode ?? null
							}
							heaterOn={summary.latestReading?.heaterOn ?? null}
							onCommandSent={refresh}
						/>
						<AlertList
							alerts={summary.activeAlerts}
							onChange={refresh}
						/>
						<TemperatureChart
							deviceId={deviceId}
							intervalMs={pollIntervalMs * CHART_INTERVAL_FACTOR}
						/>
					</div>
				</>
			)}
		</main>
	);
}
