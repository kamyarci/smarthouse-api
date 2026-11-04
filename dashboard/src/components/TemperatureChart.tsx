import { useCallback } from "react";
import {
	CartesianGrid,
	Line,
	LineChart,
	ReferenceArea,
	ReferenceLine,
	ResponsiveContainer,
	Tooltip,
	XAxis,
	YAxis,
} from "recharts";
import { fetchRecentReadings } from "../api/readings";
import { usePolling } from "../hooks/usePolling";
import { formatNumber, formatTime } from "../lib/format";
import { HEATER_LIMITS } from "../lib/heater";

interface Props {
	deviceId: string;
	intervalMs: number;
}

interface ChartPoint {
	index: number;
	time: string;
	temperature: number;
	heaterOn: boolean;
}

const HISTORY_SIZE = 60;

export function TemperatureChart({ deviceId, intervalMs }: Props) {
	const fetcher = useCallback(
		(signal: AbortSignal) =>
			fetchRecentReadings(deviceId, HISTORY_SIZE, signal),
		[deviceId],
	);
	const { data, error } = usePolling(fetcher, intervalMs);

	const points: ChartPoint[] = (data ?? [])
		.filter((reading) => reading.temperature !== null)
		.map((reading, index) => ({
			index,
			time: formatTime(reading.recordedAt),
			temperature: reading.temperature as number,
			heaterOn: reading.heaterOn === true,
		}));

	return (
		<section className="card card--wide">
			<header className="card__header">
				<h2>Temperatura</h2>
				<span className="card__hint">
					Últimas {points.length} leituras · faixa sombreada: manta
					ligada
				</span>
			</header>

			{error && <p className="error">{error}</p>}
			{points.length < 2 ? (
				<p className="empty">Leituras insuficientes para o gráfico.</p>
			) : (
				<Chart points={points} />
			)}
		</section>
	);
}

function Chart({ points }: { points: ChartPoint[] }) {
	const timeAt = (index: number) => points[index]?.time ?? "";
	const ticks = temperatureTicks(points);

	return (
		<ResponsiveContainer width="100%" height={260}>
			<LineChart
				data={points}
				margin={{ top: 8, right: 16, bottom: 0, left: 0 }}
			>
				<CartesianGrid
					stroke="var(--border)"
					strokeDasharray="3 3"
					vertical={false}
				/>

				{heaterIntervals(points).map(([start, end]) => (
					<ReferenceArea
						key={start}
						x1={start - 0.5}
						x2={end + 0.5}
						fill="var(--high-bg)"
						fillOpacity={1}
						ifOverflow="hidden"
					/>
				))}

				<XAxis
					dataKey="index"
					type="number"
					domain={[0, points.length - 1]}
					tickFormatter={timeAt}
					tick={{ fontSize: 12, fill: "var(--muted)" }}
					minTickGap={40}
				/>
				<YAxis
					unit="°"
					width={44}
					domain={[ticks[0], ticks[ticks.length - 1]]}
					ticks={ticks}
					allowDecimals={false}
					tick={{ fontSize: 12, fill: "var(--muted)" }}
				/>

				<ReferenceLine
					y={HEATER_LIMITS.onBelow}
					stroke="var(--muted)"
					strokeDasharray="4 4"
					label={{
						value: `${HEATER_LIMITS.onBelow} °C`,
						position: "insideTopRight",
						fontSize: 11,
						fill: "var(--muted)",
					}}
				/>

				<Tooltip
					separator=": "
					labelFormatter={(index) => timeAt(Number(index))}
					formatter={(value) => [
						formatNumber(Number(value), " °C"),
						"Temperatura",
					]}
				/>

				<Line
					type="monotone"
					dataKey="temperature"
					stroke="var(--primary)"
					strokeWidth={2}
					dot={{ r: 3 }}
					isAnimationActive={false}
				/>
			</LineChart>
		</ResponsiveContainer>
	);
}

const MAX_TICKS = 8;

function temperatureTicks(points: ChartPoint[]): number[] {
	const temperatures = points.map((point) => point.temperature);
	const min = Math.min(...temperatures) - 1;
	const max = Math.max(...temperatures) + 1;
	const step =
		[5, 10, 20, 50].find(
			(candidate) => (max - min) / candidate <= MAX_TICKS - 1,
		) ?? 100;

	const ticks: number[] = [];
	for (
		let tick = Math.floor(min / step) * step;
		tick < max + step;
		tick += step
	) {
		ticks.push(tick);
		if (tick >= max) break;
	}
	return ticks;
}

function heaterIntervals(points: ChartPoint[]): Array<[number, number]> {
	const intervals: Array<[number, number]> = [];
	let start: number | null = null;

	points.forEach((point, i) => {
		if (point.heaterOn && start === null) start = point.index;
		if (!point.heaterOn && start !== null) {
			intervals.push([start, points[i - 1].index]);
			start = null;
		}
	});

	if (start !== null)
		intervals.push([start, points[points.length - 1].index]);
	return intervals;
}
