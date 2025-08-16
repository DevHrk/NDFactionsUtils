package me.nd.factionsutils.messages;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.nd.factionsutils.api.ActionbarAPI;
import me.nd.factionsutils.api.TitleAPI;

public class MessageUtils {

	public static void send(CommandSender sender, Object... rawmsg) {
		Stream.of(rawmsg).flatMap(o -> o instanceof List ? ((List<?>) o).stream() : Stream.of(o))
				.filter(o -> o instanceof String).map(o -> (String) o).forEach(msg -> processMessage(sender, msg));
	}

	private static void processMessage(CommandSender sender, String msg) {
		String message = msg.replace("{sender}", sender.getName()).replace("\\n", "\n").replace("&", "§");

		MessageHandler handler = MessageHandlers.getHandler(message);
		if (handler != null) {
			handler.handle(sender, message);
		}
	}

	@FunctionalInterface
	interface MessageHandler {
		void handle(CommandSender sender, String message);
	}

	enum MessageHandlers implements MessageHandler {
		MENSAGEM("mensagem: ", (sender, message) -> sender.sendMessage(message.replace("mensagem: ", ""))),
		BROADCAST("broadcast: ", (sender, message) -> Bukkit.broadcastMessage(message.replace("broadcast: ", ""))),
		JSON("json: ", (sender, message) -> JsonMessage.send((Player) sender, message.replace("json: ", ""))),
		ACTION_BAR("actionbar: ",
				(sender, message) -> ActionbarAPI.sendActionBarMessage((Player) sender,
						message.replace("actionbar: ", ""))),
		TITLE("title: ", (sender, message) -> TitleAPI.sendTitle((Player) sender, 20, 30, 20,
				message.replace("title: ", ""), "")),
		TITLE_SUB("titlesub: ", (sender, message) -> {
			String[] parts = message.replace("titlesub: ", "").split(":");
			TitleAPI.sendTitle((Player) sender, 20, 30, 20, parts[0], parts.length > 1 ? parts[1] : "");
		}), TITLE_INSTA("titleinsta: ", (sender, message) -> {
			String[] parts = message.replace("titleinsta: ", "").split(":");
			String title = parts[0].replaceAll("&[a-zA-Z0-9]", "");
			String subtitle = parts.length > 1 ? parts[1].replaceAll("&[a-zA-Z0-9]", "") : "";
			int fadeIn = Integer.parseInt(parts[2]);
			int stay = Integer.parseInt(parts[3]);
			int fadeOut = Integer.parseInt(parts[4]);

			TitleAPI.sendTitle((Player) sender, fadeIn, stay, fadeOut, title, subtitle);
		});

		private final String prefix;
		private final BiConsumer<CommandSender, String> handler;

		MessageHandlers(String prefix, BiConsumer<CommandSender, String> handler) {
			this.prefix = prefix;
			this.handler = handler;
		}

		public static MessageHandler getHandler(String message) {
			for (MessageHandlers handler : values()) {
				if (message.startsWith(handler.prefix)) {
					return handler;
				}
			}
			return null;
		}

		@Override
		public void handle(CommandSender sender, String message) {
			handler.accept(sender, message.replace(prefix, ""));
		}
	}

}
