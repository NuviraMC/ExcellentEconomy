package su.nightexpress.excellenteconomy.currency;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import su.nightexpress.excellenteconomy.EconomyFiles;
import su.nightexpress.excellenteconomy.EconomyPlaceholders;
import su.nightexpress.excellenteconomy.EconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import su.nightexpress.excellenteconomy.api.currency.operation.NotificationTarget;
import su.nightexpress.excellenteconomy.api.currency.operation.OperationContext;
import su.nightexpress.excellenteconomy.api.currency.operation.OperationExecutor;
import su.nightexpress.excellenteconomy.api.currency.operation.OperationResult;
import su.nightexpress.excellenteconomy.command.CommandManager;
import su.nightexpress.excellenteconomy.command.currency.CommandDefinition;
import su.nightexpress.excellenteconomy.config.Config;
import su.nightexpress.excellenteconomy.config.Lang;
import su.nightexpress.excellenteconomy.currency.command.*;
import su.nightexpress.excellenteconomy.currency.impl.AbstractCurrency;
import su.nightexpress.excellenteconomy.currency.impl.NormalCurrency;
import su.nightexpress.excellenteconomy.currency.placeholder.PlayerBalancePlaceholders;
import su.nightexpress.excellenteconomy.data.DataColumns;
import su.nightexpress.excellenteconomy.data.DataHandler;
import su.nightexpress.excellenteconomy.user.CoinsUser;
import su.nightexpress.excellenteconomy.user.UserManager;
import su.nightexpress.excellenteconomy.user.data.CurrencySettings;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.FileUtil;
import su.nightexpress.nightcore.util.Strings;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CurrencyManager extends AbstractManager<EconomyPlugin> {

    private final CurrencyRegistry registry;
    private final CommandManager   commandManager;
    private final DataHandler      dataHandler;
    private final UserManager      userManager;

    private boolean        operationsAllowed;
    private CurrencyLogger logger;

    public CurrencyManager(@NonNull EconomyPlugin plugin,
                           @NonNull CurrencyRegistry registry,
                           @NonNull CommandManager commandManager,
                           @NonNull DataHandler dataHandler,
                           @NonNull UserManager userManager) {
        super(plugin);
        this.registry = registry;
        this.commandManager = commandManager;
        this.dataHandler = dataHandler;
        this.userManager = userManager;
        this.allowOperations();
    }

    @Override
    protected void onLoad() {
        this.createDefaults();
        this.migrateSettings();
        this.loadPluginCommands();
        this.loadCurrencyCommands();
        this.plugin.addGlobalPlaceholders(new PlayerBalancePlaceholders(this.registry, this));

        FileUtil.findYamlFiles(this.getDirectory()).forEach(this::loadCurrency);

        try {
            this.loadLogger();
        }
        catch (IOException | IllegalArgumentException exception) {
            this.plugin.error("Could not create operations logger: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    @Override
    protected void onShutdown() {
        this.registry.getCurrencies().forEach(this::unregisterCurrency);

        if (this.logger != null) this.logger.shutdown();
        this.disableOperations();
    }

    @NonNull
    public String getDirectory() {
        return this.plugin.getDataFolder() + EconomyFiles.DIR_CURRENCIES;
    }

    public void allowOperations() {
        this.operationsAllowed = true;
        this.dataHandler.setSynchronizationActive(true);
    }

    public void disableOperations() {
        this.operationsAllowed = false;
        this.dataHandler.setSynchronizationActive(false);
    }

    public boolean canPerformOperations() {
        return this.operationsAllowed;
    }

    private boolean assertOperationsEnabled(@NonNull OperationContext context) {
        if (!this.canPerformOperations()) {
            context.getBukkitSender().ifPresent(sender -> Lang.CURRENCY_OPERATION_DISABLED.message().send(sender));
            return false;
        }
        return true;
    }

    private void migrateSettings() {
        FileUtil.findYamlFiles(this.getDirectory()).forEach(path -> {
            String fileName = path.getFileName().toString();
            if (!fileName.endsWith(FileConfig.EXTENSION)) return;

            FileConfig config = FileConfig.load(path);
            if (!config.contains("Economy")) return;

            config.remove("Economy");
            config.saveChanges();
        });
    }

    private void loadPluginCommands() {
        this.commandManager.addPluginCommand(ResetAllCommand.create(this.registry, this));
        this.commandManager.addPluginCommand(CreateCommand.create(this));

        if (Config.isWalletEnabled()) {
            this.commandManager.addStandaloneCommand(WalletCommand.create(this.plugin, this, this.userManager));
        }
    }

    private void loadCurrencyCommands() {
        this.commandManager.addCurrencyCommand("balance",
            () -> new BalanceCommand(this, this.userManager),
            CommandDefinition.allEnabled("balance", "balance", "bal")
        );

        this.commandManager.addCurrencyCommand("exchange",
            () -> new ExchangeCommand(this.registry, this),
            CommandDefinition.childOnly("exchange", "ecoexchange"),
            ExcellentCurrency::isExchangeAllowed
        );

        this.commandManager.addCurrencyCommand("giveall",
            () -> new GiveAllCommand(this),
            CommandDefinition.childOnly("giveall", "ecogiveall")
        );

        this.commandManager.addCurrencyCommand("give",
            () -> new GiveCommand(this, this.userManager),
            CommandDefinition.childOnly("give", "ecogive")
        );

        this.commandManager.addCurrencyCommand("payments",
            () -> new PaymentsCommand(this, this.userManager),
            CommandDefinition.allEnabled("payments", "paytoggle", "payments"),
            ExcellentCurrency::isTransferAllowed
        );

        this.commandManager.addCurrencyCommand("remove",
            () -> new RemoveCommand(this, this.userManager),
            CommandDefinition.allEnabled("take", "ecotake")
        );

        this.commandManager.addCurrencyCommand("reset",
            () -> new ResetCommand(this, this.userManager),
            CommandDefinition.childOnly("reset", "ecoreset")
        );

        this.commandManager.addCurrencyCommand("send",
            () -> new PayCommand(this, this.userManager),
            CommandDefinition.allEnabled("pay", "pay"),
            ExcellentCurrency::isTransferAllowed
        );

        this.commandManager.addCurrencyCommand("set",
            () -> new SetCommand(this, this.userManager),
            CommandDefinition.allEnabled("set", "ecoset")
        );
    }

    private void loadCurrency(@NonNull Path path) throws IllegalStateException {
        String fileName = path.getFileName().toString();
        if (!fileName.endsWith(FileConfig.EXTENSION)) return;

        String name = fileName.substring(0, fileName.length() - FileConfig.EXTENSION.length());
        String id = Strings.varStyle(name).orElseThrow(() -> new IllegalStateException("Malformed file name '" +
            fileName + "'"));

        AbstractCurrency currency = CurrencyFactory.createNormal(path, id);

        // Useless until we remake the plugin reload system.
        if (currency.isPrimary() && this.registry.hasPrimary()) {
            this.plugin.warn("Could not load primary currency '" + currency.getId() +
                "' as there is already one present. Reboot the server if you want to change your primary currency.");
            return;
        }

        currency.load();

        this.registerCurrency(currency);
    }

    private void createDefaults() {
        File dir = new File(this.getDirectory());
        if (dir.exists()) return;

        this.createCurrency("coins", currency -> {
            currency.setSymbol("⛂");
            currency.setIcon(NightItem.fromType(Material.SUNFLOWER));
            currency.setDecimal(false);
        });

        this.createCurrency("money", currency -> {
            currency.setSymbol("$");
            currency.setFormat(EconomyPlaceholders.FORMATTED_BALANCE + currency.getSymbol());
            currency.setIcon(NightItem.fromType(Material.EMERALD));
        });
    }

    public void createCurrency(@NonNull String name, @NonNull Consumer<NormalCurrency> consumer) {
        Path path = Paths.get(this.getDirectory() + name + FileConfig.EXTENSION);
        NormalCurrency currency = (NormalCurrency) CurrencyFactory.createNormal(path, name);
        consumer.accept(currency);
        currency.save();
    }

    private void loadLogger() throws IOException, IllegalArgumentException {
        this.logger = new CurrencyLogger(this.plugin);
    }

    public void registerCurrency(@NonNull AbstractCurrency currency) {
        this.registry.register(currency);
        this.dataHandler.createCurrencyColumns(currency);
        currency.onRegister();
        this.plugin.info("Loaded currency: '" + currency.getId() + "'.");
    }

    private void unregisterCurrency(@NonNull AbstractCurrency currency) {
        currency.onUnregister();
        this.registry.unregister(currency);
    }

    @NonNull
    public OperationResult give(@NonNull OperationContext context, @NonNull CoinsUser user,
                                @NonNull ExcellentCurrency currency, double amount) {
        if (!this.assertOperationsEnabled(context)) return OperationResult.FAILURE;
        return OperationExecutor.give(context, user, currency, amount);
    }

    @NonNull
    public OperationResult remove(@NonNull OperationContext context, @NonNull CoinsUser user,
                                  @NonNull ExcellentCurrency currency, double amount) {
        if (!this.assertOperationsEnabled(context)) return OperationResult.FAILURE;
        return OperationExecutor.remove(context, user, currency, amount);
    }

    @NonNull
    public OperationResult set(@NonNull OperationContext context, @NonNull CoinsUser user,
                               @NonNull ExcellentCurrency currency, double amount) {
        if (!this.assertOperationsEnabled(context)) return OperationResult.FAILURE;
        return OperationExecutor.set(context, user, currency, amount);
    }

    public void transfer(@NonNull OperationContext context, @NonNull CoinsUser from, @NonNull CoinsUser to,
                         @NonNull ExcellentCurrency currency, double amount) {
        if (!this.assertOperationsEnabled(context)) return;
        OperationExecutor.transfer(context, from, to, currency, amount);
    }

    @NonNull
    public OperationResult exchange(@NonNull Player player, @NonNull CoinsUser user,
                                    @NonNull ExcellentCurrency from, @NonNull ExcellentCurrency to, double amount) {
        if (!this.assertOperationsEnabled(OperationContext.player(player))) return OperationResult.FAILURE;
        return OperationExecutor.exchange(OperationContext.player(player), user, from, to, amount);
    }

    public void resetAll(@NonNull CommandSender sender, @NonNull ExcellentCurrency currency) {
        this.plugin.runTaskAsync(() -> {
            this.disableOperations();
            Lang.CURRENCY_RESET_ALL_STARTED.message().sendWith(sender,
                builder -> builder.with(currency.placeholders()));
            this.dataHandler.resetBalances(currency);
            this.userManager.getOnlineUsers().forEach(user -> user.setBalance(currency, currency.getStartValue()));
            Lang.CURRENCY_RESET_ALL_DONE.message().sendWith(sender, builder -> builder.with(currency.placeholders()));
            this.allowOperations();
        });
    }

    public boolean isEnoughBalance(@NonNull CoinsUser user, @NonNull ExcellentCurrency currency, double required) {
        double balance = user.getBalance(currency);
        return balance >= required;
    }

    @NonNull
    public Set<CoinsUser> findRichestUsers(@NonNull ExcellentCurrency currency, int amount) {
        return this.userManager.getOnlineUsers().stream()
            .sorted(Comparator.comparingDouble((CoinsUser u) -> u.getBalance(currency)).reversed())
            .limit(amount)
            .collect(Collectors.toSet());
    }

    @NonNull
    public Collection<ExcellentCurrency> getCurrencies() {
        return this.registry.getCurrencies();
    }

    @Nullable
    public ExcellentCurrency getCurrency(@NonNull String id) {
        return this.registry.getCurrency(id);
    }

    @NonNull
    public PlaceholderContext balancePlaceholders(@NonNull CoinsUser user, @NonNull ExcellentCurrency currency) {
        double balance = user.getBalance(currency);
        return PlaceholderContext.create()
            .with(CommonPlaceholders.GENERIC_AMOUNT, () -> currency.formatBalance(balance))
            .with(DataColumns.COLUMN_BALANCE + "_" + currency.getId(), () -> String.valueOf(balance));
    }
}
