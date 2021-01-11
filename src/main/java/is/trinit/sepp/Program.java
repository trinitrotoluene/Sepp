package is.trinit.sepp;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import is.trinit.sepp.commands.CommandRegistry;
import is.trinit.sepp.commands.MessageCommandSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class Program {
    private static final Logger s_logger = LoggerFactory.getLogger(Program.class.getName());
    private static final CommandDispatcher<MessageCommandSource> s_commandDispatcher = new CommandDispatcher<>();

    public static void main(String[] args) {
        ConfigurationLoader.loadConfiguration().ifPresentOrElse(Program::runBot, () -> s_logger.error("Failed to load bot configuration. Have you included config.properties?"));
    }

    private static void runBot(Properties config) {
        var token = config.getProperty("token");
        var botBuilder = DiscordClientBuilder.create(token);
        var bot = botBuilder.build();
        var client = bot.login().block();
        if (client == null) {
            s_logger.error("Client failed to log in.");
            return;
        }

        onLogin(client);
        client.onDisconnect().block();
    }

    private static void onLogin(GatewayDiscordClient gatewayClient) {
        s_logger.info("Login successful, hooking events");

        CommandRegistry.register(s_commandDispatcher);

        final var prefix = "!";
        gatewayClient.on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
                .filter(m -> m.getContent().startsWith(prefix))
                .subscribe(m -> {
                    var command = m.getContent().substring(prefix.length());
                    var source = new MessageCommandSource(command, m);
                    var parseResult = s_commandDispatcher.parse(command, source);
                    try {
                        s_commandDispatcher.execute(parseResult);
                    } catch (CommandSyntaxException e) {
                        s_logger.warn("An exception was thrown while executing a command.", e);
                    }
                });

        s_logger.info("Hooks registered");
    }
}
