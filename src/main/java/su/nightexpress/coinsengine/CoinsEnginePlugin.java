package su.nightexpress.coinsengine;

import org.bukkit.plugin.ServicePriority;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.coinsengine.command.CommandManager;
import su.nightexpress.coinsengine.config.Config;
import su.nightexpress.coinsengine.config.Lang;
import su.nightexpress.coinsengine.config.Perms;
import su.nightexpress.coinsengine.currency.CurrencyManager;
import su.nightexpress.coinsengine.currency.CurrencyRegistry;
import su.nightexpress.coinsengine.data.DataHandler;
import su.nightexpress.coinsengine.migration.MigrationManager;
import su.nightexpress.coinsengine.tops.TopManager;
import su.nightexpress.coinsengine.user.UserManager;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.config.PluginDetails;

public class CoinsEnginePlugin extends NightPlugin {

    private ExcellentEconomyProvider api;

    CurrencyRegistry currencyRegistry;
    CommandManager commandManager;

    DataHandler      dataHandler;
    UserManager      userManager;

    CurrencyManager  currencyManager;

    @Nullable TopManager       topManager;
    @Nullable MigrationManager migrationManager;

    @Override
    @NonNull
    protected PluginDetails getDefaultDetails() {
        return PluginDetails.create("Economy", new String[]{"coinsengine", "coe", "ce"})
            .setConfigClass(Config.class)
            .setPermissionsClass(Perms.class);
    }

    @Override
    protected void onStartup() {
        super.onStartup();

        this.currencyRegistry = new CurrencyRegistry();
    }

    @Override
    public void enable() {
        this.api = new ExcellentEconomyProvider(this);

        this.commandManager = new CommandManager(this, this.currencyRegistry);
        this.commandManager.setup();

        this.dataHandler = new DataHandler(this);
        this.dataHandler.setup();

        this.userManager = new UserManager(this, this.currencyRegistry, this.dataHandler);
        this.userManager.setup();

        this.currencyManager = new CurrencyManager(this, this.currencyRegistry, this.commandManager, this.dataHandler, this.userManager);
        this.currencyManager.setup();

        if (Config.isTopsEnabled()) {
            this.topManager = new TopManager(this, this.currencyRegistry, this.commandManager, this.userManager);
            this.topManager.setup();
        }

        if (Config.isMigrationEnabled()) {
            this.migrationManager = new MigrationManager(this, this.currencyRegistry, this.currencyManager, this.commandManager, this.userManager);
            this.migrationManager.setup();
        }

        // Register all commands (currency, plugin, standalones).
        this.commandManager.registerCommands();

        this.getServer().getServicesManager().register(ExcellentEconomyAPI.class, this.api, this, ServicePriority.Normal);
    }

    @Override
    protected boolean disableCommandManager() {
        return true;
    }

    @Override
    protected void addRegistries() {
        this.registerLang(Lang.class);
    }

    @Override
    public void disable() {
        if (this.commandManager != null) this.commandManager.shutdown();
        if (this.topManager != null) this.topManager.shutdown();
        if (this.migrationManager != null) this.migrationManager.shutdown();
        if (this.userManager != null) this.userManager.shutdown();
        if (this.dataHandler != null) this.dataHandler.shutdown();
        if (this.currencyManager != null) this.currencyManager.shutdown();
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();
        this.currencyRegistry.clear();

        this.getServer().getServicesManager().unregister(this.api);
        this.api = null;
    }

    @NonNull
    public ExcellentEconomyProvider getAPI() {
        return this.api;
    }
}
