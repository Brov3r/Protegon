package com.brov3r.protegon;

import com.avrix.events.EventManager;
import com.avrix.plugin.Metadata;
import com.avrix.plugin.Plugin;
import com.avrix.utils.YamlFile;
import com.brov3r.protegon.handlers.OnPlayerBanHandler;
import com.brov3r.protegon.handlers.OnPlayerKickHandler;
import com.brov3r.protegon.modules.ChatModule;

/**
 * Main entry point of the example plugin
 */
public class Main extends Plugin {
    private static Main instance;

    /**
     * Constructs a new {@link Plugin} with the specified metadata.
     * Metadata is transferred when the plugin is loaded into the game context.
     *
     * @param metadata The {@link Metadata} associated with this plugin.
     */
    public Main(Metadata metadata) {
        super(metadata);
    }

    /**
     * Called when the plugin is initialized.
     * <p>
     * Implementing classes should override this method to provide the initialization logic.
     */
    @Override
    public void onInitialize() {
        instance = this;
        loadDefaultConfig();

        EventManager.addListener(new OnPlayerBanHandler());
        EventManager.addListener(new OnPlayerKickHandler());

        ModuleManager.loadModules();

        ChatModule.initFilters();
    }

    /**
     * Getting a plugin instance
     *
     * @return plugin instance
     */
    public static Main getInstance() {
        return instance;
    }

    /**
     * Getting an instance of the standard plugin config
     *
     * @return config instance
     */
    public static YamlFile getConfig() {
        return instance.getDefaultConfig();
    }
}