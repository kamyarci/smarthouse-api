import { useState } from "react";
import { setHeaterMode } from "../api/device";
import { heaterModeLabel } from "../lib/format";
import { HEATER_LIMITS } from "../lib/heater";
import type { HeaterMode } from "../types/reading";

interface Props {
	deviceId: string;
	currentMode: HeaterMode | null;
	heaterOn: boolean | null;
	onCommandSent: () => void;
}

const MODES: HeaterMode[] = ["AUTO", "ON", "OFF"];

const BUTTON_LABEL: Record<HeaterMode, string> = {
	AUTO: "Automático",
	ON: "Ligar",
	OFF: "Desligar",
};

export function HeaterControl({
	deviceId,
	currentMode,
	heaterOn,
	onCommandSent,
}: Props) {
	const [requestedMode, setRequestedMode] = useState<HeaterMode | null>(null);
	const [sending, setSending] = useState(false);
	const [error, setError] = useState<string | null>(null);

	const waitingConfirmation =
		requestedMode !== null && requestedMode !== currentMode;

	async function send(mode: HeaterMode) {
		setSending(true);
		setError(null);
		try {
			await setHeaterMode(deviceId, mode);
			setRequestedMode(mode);
			onCommandSent();
		} catch (err) {
			setError(
				err instanceof Error
					? err.message
					: "Falha ao enviar o comando",
			);
		} finally {
			setSending(false);
		}
	}

	return (
		<section className="card">
			<header className="card__header">
				<h2>Manta térmica</h2>
				<span className={`pill ${heaterOn ? "pill--on" : "pill--off"}`}>
					{heaterOn ? "Aquecendo" : "Desligada"}
				</span>
			</header>

			<p className="card__text">
				Modo atual:{" "}
				<strong>
					{currentMode ? heaterModeLabel[currentMode] : "—"}
				</strong>
			</p>
			<p className="card__hint">
				No automático, a manta liga abaixo de {HEATER_LIMITS.onBelow} °C
				com animal presente. Acima de {HEATER_LIMITS.safetyCutoff} °C
				ela desliga em qualquer modo.
			</p>

			<div className="segmented" role="group" aria-label="Modo da manta">
				{MODES.map((mode) => (
					<button
						key={mode}
						type="button"
						className={
							mode === currentMode
								? "segmented__option is-active"
								: "segmented__option"
						}
						disabled={sending}
						onClick={() => send(mode)}
					>
						{BUTTON_LABEL[mode]}
					</button>
				))}
			</div>

			{waitingConfirmation && (
				<p className="card__hint">
					Comando enviado. Aguardando a casinha confirmar…
				</p>
			)}
			{error && <p className="error">{error}</p>}
		</section>
	);
}
