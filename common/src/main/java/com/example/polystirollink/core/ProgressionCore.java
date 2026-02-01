package com.example.polystirollink.core;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ProgressionCore {
	private static final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build();

	private static final ExecutorService executorService = Executors.newCachedThreadPool(r -> {
		Thread thread = new Thread(r, "ProgressionCore-HTTP");
		thread.setDaemon(true);
		return thread;
	});

	private static final Gson gson = new Gson();

	public static CompletableFuture<LinkResult> linkAccount(String linkCode, String gameId, String platformUsername) {
		return CompletableFuture.supplyAsync(() -> {
			ModConfig config = PolystirolLinkCommon.getConfig();
			String backendUrl = config != null ? config.getBackendUrl() : null;
			if (backendUrl == null || backendUrl.isEmpty()) {
				PolystirolLinkCommon.LOGGER.error("Backend URL is not configured!");
				return new LinkResult(500, "Ошибка конфигурации: URL бэкенда не задан.");
			}

			try {
				URI uri = URI.create(backendUrl + "/api/v1/auth/link");

				JsonObject requestBody = new JsonObject();
				requestBody.addProperty("link_code", linkCode);
				requestBody.addProperty("game_id", gameId);
				requestBody.addProperty("platform", "MC");
				requestBody.addProperty("platform_username", platformUsername);

				HttpRequest request = HttpRequest.newBuilder()
						.uri(uri)
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
						.timeout(Duration.ofSeconds(30))
						.build();

				HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
				int statusCode = response.statusCode();

				return switch (statusCode) {
					case 200 -> new LinkResult(200, "✅ Аккаунт успешно привязан! Спасибо!");
					case 400 -> new LinkResult(400, "❌ Неверный или истекший код привязки.");
					case 409 -> new LinkResult(409, "❌ Ваш Minecraft аккаунт уже привязан к другому Master ID.");
					default -> new LinkResult(statusCode, "❌ Произошла ошибка при привязке аккаунта. Код: " + statusCode);
				};
			} catch (java.net.http.HttpTimeoutException e) {
				PolystirolLinkCommon.LOGGER.error("HTTP request timeout: {}", e.getMessage());
				return new LinkResult(408, "❌ Превышено время ожидания ответа от сервера.");
			} catch (java.net.ConnectException e) {
				PolystirolLinkCommon.LOGGER.error("Connection error: {}", e.getMessage());
				return new LinkResult(503, "❌ Не удалось подключиться к серверу.");
			} catch (Exception e) {
				PolystirolLinkCommon.LOGGER.error("Error during account linking: ", e);
				return new LinkResult(500, "❌ Произошла ошибка при отправке запроса: " + e.getMessage());
			}
		}, executorService);
	}

	public static class LinkResult {
		private final int statusCode;
		private final String message;

		public LinkResult(int statusCode, String message) {
			this.statusCode = statusCode;
			this.message = message;
		}

		public int getStatusCode() {
			return statusCode;
		}

		public String getMessage() {
			return message;
		}
	}
}

