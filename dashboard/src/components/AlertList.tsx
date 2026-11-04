import { useState } from "react";
import { acknowledgeAlert, resolveAlert } from "../api/alerts";
import {
	alertTypeLabel,
	formatDateTime,
	severityLabel,
	statusLabel,
} from "../lib/format";
import type { Alert } from "../types/alert";

interface Props {
	alerts: Alert[];
	onChange: () => void;
}

export function AlertList({ alerts, onChange }: Props) {
	const [volunteerName, setVolunteerName] = useState("");
	const [busyId, setBusyId] = useState<number | null>(null);
	const [error, setError] = useState<string | null>(null);

	async function run(id: number, action: typeof acknowledgeAlert) {
		setBusyId(id);
		setError(null);
		try {
			await action(
				id,
				volunteerName.trim()
					? { volunteerName: volunteerName.trim() }
					: {},
			);
			onChange();
		} catch (err) {
			setError(
				err instanceof Error
					? err.message
					: "Falha ao atualizar o alerta",
			);
		} finally {
			setBusyId(null);
		}
	}

	return (
		<section className="card card--wide">
			<header className="card__header">
				<h2>Alertas ativos</h2>
				<input
					className="input"
					placeholder="Seu nome (opcional)"
					value={volunteerName}
					maxLength={100}
					onChange={(event) => setVolunteerName(event.target.value)}
				/>
			</header>

			{error && <p className="error">{error}</p>}

			{alerts.length === 0 ? (
				<p className="empty">Nenhum alerta ativo.</p>
			) : (
				<ul className="alerts">
					{alerts.map((alert) => (
						<li
							key={alert.id}
							className={`alert alert--${alert.severity.toLowerCase()}`}
						>
							<div className="alert__main">
								<div className="alert__title">
									<span className="badge">
										{severityLabel[alert.severity]}
									</span>
									<strong>
										{alertTypeLabel[alert.type]}
									</strong>
									<span className="alert__status">
										{statusLabel[alert.status]}
									</span>
								</div>
								<p className="alert__message">
									{alert.message}
								</p>
								<span className="card__hint">
									Desde {formatDateTime(alert.createdAt)}
								</span>
							</div>

							<div className="alert__actions">
								{alert.status === "PENDING" && (
									<button
										type="button"
										className="button"
										disabled={busyId === alert.id}
										onClick={() =>
											run(alert.id, acknowledgeAlert)
										}
									>
										Posso verificar
									</button>
								)}
								{alert.status === "IN_PROGRESS" && (
									<button
										type="button"
										className="button button--secondary"
										disabled={busyId === alert.id}
										onClick={() =>
											run(alert.id, resolveAlert)
										}
									>
										Marcar como resolvido
									</button>
								)}
							</div>
						</li>
					))}
				</ul>
			)}
		</section>
	);
}
