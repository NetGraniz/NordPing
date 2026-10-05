package com.nordfjell.nordping;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Plugin(
        id = "nordping",
        name = "NordPing",
        version = "1.0.1",
        description = "Custom server-list version label for Nord Fjell",
        authors = {"Nord Fjell"}
)
public final class NordPingPlugin {
    private static final String DEFAULT_VERSION_NAME = "Minecraft 26.2+";

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;
    private volatile String versionName = DEFAULT_VERSION_NAME;

    @Inject
    public NordPingPlugin(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        loadSettings();
        registerCommand();
        logger.info("NordPing enabled with version label: {}", versionName);
    }

    @Subscribe(priority = Short.MIN_VALUE)
    public void onProxyPing(ProxyPingEvent event) {
        ServerPing current = event.getPing();
        ServerPing.Version currentVersion = current.getVersion();
        ServerPing.Version customVersion = new ServerPing.Version(
                currentVersion.getProtocol(),
                versionName
        );
        event.setPing(current.asBuilder().version(customVersion).build());
    }

    private void registerCommand() {
        CommandMeta meta = proxy.getCommandManager().metaBuilder("nordping")
                .plugin(this)
                .build();
        proxy.getCommandManager().register(meta, new ReloadCommand());
    }

    private void loadSettings() {
        try {
            Files.createDirectories(dataDirectory);
            Path config = dataDirectory.resolve("config.properties");
            Properties properties = new Properties();
            if (Files.exists(config)) {
                try (InputStream input = Files.newInputStream(config)) {
                    properties.load(input);
                }
            }

            String configuredName = properties.getProperty("version-name", DEFAULT_VERSION_NAME).trim();
            versionName = configuredName.isEmpty() ? DEFAULT_VERSION_NAME : configuredName;

            if (!Files.exists(config) || !properties.containsKey("version-name")) {
                properties.setProperty("version-name", versionName);
                try (OutputStream output = Files.newOutputStream(config)) {
                    properties.store(output, "Text shown as the server version in the multiplayer server list.");
                }
            }
        } catch (IOException | RuntimeException exception) {
            versionName = DEFAULT_VERSION_NAME;
            logger.error("Could not load NordPing configuration; using {}", DEFAULT_VERSION_NAME, exception);
        }
    }

    private final class ReloadCommand implements SimpleCommand {
        @Override
        public boolean hasPermission(Invocation invocation) {
            return invocation.source().hasPermission("nordping.admin");
        }

        @Override
        public void execute(Invocation invocation) {
            loadSettings();
            invocation.source().sendRichMessage("<green>NordPing reloaded:</green> <white>" + versionName + "</white>");
        }
    }
}
