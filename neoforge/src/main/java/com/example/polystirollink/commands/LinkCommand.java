package com.example.polystirollink.commands;

import com.example.polystirollink.core.ProgressionCore;
import com.example.polystirollink.core.PolystirolLinkCommon;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class LinkCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("link")
				.requires(source -> source.hasPermission(0))
				.then(Commands.argument("code", StringArgumentType.string())
						.executes(context -> executeLink(context))));
	}

	private static int executeLink(CommandContext<CommandSourceStack> context) {
		CommandSourceStack source = context.getSource();
		
		if (!(source.getEntity() instanceof ServerPlayer player)) {
			source.sendFailure(Component.literal("Эта команда может быть выполнена только игроком!"));
			return 0;
		}

		var server = source.getServer();
		if (server == null) {
			PolystirolLinkCommon.LOGGER.error("Server is null in link command");
			player.sendSystemMessage(Component.literal("❌ Ошибка: сервер недоступен."));
			return 0;
		}

		String linkCode = StringArgumentType.getString(context, "code");
		
		if (linkCode == null || linkCode.isEmpty()) {
			player.sendSystemMessage(Component.literal("❌ Код привязки не может быть пустым!"));
			return 0;
		}

		String gameId = player.getUUID().toString();
		var playerName = player.getName();
		if (playerName == null) {
			PolystirolLinkCommon.LOGGER.error("Player name is null for player {}", gameId);
			player.sendSystemMessage(Component.literal("❌ Ошибка: не удалось получить имя игрока."));
			return 0;
		}
		String platformUsername = playerName.getString();

		player.sendSystemMessage(Component.literal("⏳ Обработка запроса привязки..."));

		ProgressionCore.linkAccount(linkCode, gameId, platformUsername)
				.thenAccept(result -> {
					if (result == null) {
						PolystirolLinkCommon.LOGGER.error("Link result is null for player {}", platformUsername);
						server.execute(() -> {
							player.sendSystemMessage(Component.literal("❌ Произошла ошибка при обработке запроса."));
						});
						return;
					}
					// Выполняем в главном потоке сервера
					server.execute(() -> {
						player.sendSystemMessage(Component.literal(result.getMessage()));
						PolystirolLinkCommon.LOGGER.info("Link result for player {}: {} (status: {})", 
								platformUsername, result.getMessage(), result.getStatusCode());
					});
				})
				.exceptionally(throwable -> {
					PolystirolLinkCommon.LOGGER.error("Error in link command execution: ", throwable);
					// Выполняем в главном потоке сервера
					server.execute(() -> {
						player.sendSystemMessage(Component.literal("❌ Произошла ошибка при обработке запроса."));
					});
					return null;
				});

		return 1;
	}
}

