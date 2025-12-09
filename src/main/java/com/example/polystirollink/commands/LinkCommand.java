package com.example.polystirollink.commands;

import com.example.polystirollink.core.ProgressionCore;
import com.example.polystirollink.polystirollink;
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

		String linkCode = StringArgumentType.getString(context, "code");
		
		if (linkCode == null || linkCode.isEmpty()) {
			player.sendSystemMessage(Component.literal("❌ Код привязки не может быть пустым!"));
			return 0;
		}

		String gameId = player.getUUID().toString();
		String platformUsername = player.getName().getString();
		var server = source.getServer();

		player.sendSystemMessage(Component.literal("⏳ Обработка запроса привязки..."));

		ProgressionCore.linkAccount(linkCode, gameId, platformUsername)
				.thenAccept(result -> {
					// Выполняем в главном потоке сервера
					server.execute(() -> {
						player.sendSystemMessage(Component.literal(result.getMessage()));
						polystirollink.LOGGER.info("Link result for player {}: {} (status: {})", 
								platformUsername, result.getMessage(), result.getStatusCode());
					});
				})
				.exceptionally(throwable -> {
					polystirollink.LOGGER.error("Error in link command execution: ", throwable);
					// Выполняем в главном потоке сервера
					server.execute(() -> {
						player.sendSystemMessage(Component.literal("❌ Произошла ошибка при обработке запроса."));
					});
					return null;
				});

		return 1;
	}
}

