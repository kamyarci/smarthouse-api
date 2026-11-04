import { env, missingEnv } from "./config/env";
import { DashboardPage } from "./pages/DashboardPage";

function App() {
	if (missingEnv.length > 0) {
		return (
			<main className="page">
				<div className="banner banner--critical">
					Configuração ausente: {missingEnv.join(", ")}. Copie o
					.env.example para .env.local, preencha e reinicie o yarn
					dev.
				</div>
			</main>
		);
	}

	return (
		<DashboardPage
			deviceId={env.deviceId}
			pollIntervalMs={env.pollIntervalMs}
		/>
	);
}

export default App;
