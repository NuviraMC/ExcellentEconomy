package su.nightexpress.coinsengine.tops;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.coinsengine.EconomyFiles;
import su.nightexpress.coinsengine.CoinsEnginePlugin;
import su.nightexpress.coinsengine.EconomyPlaceholders;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import su.nightexpress.coinsengine.command.currency.CommandDefinition;
import su.nightexpress.coinsengine.command.CommandManager;
import su.nightexpress.coinsengine.config.Config;
import su.nightexpress.coinsengine.config.Lang;
import su.nightexpress.coinsengine.config.Perms;
import su.nightexpress.coinsengine.currency.CurrencyRegistry;
import su.nightexpress.coinsengine.tops.listener.TopListener;
import su.nightexpress.coinsengine.tops.placeholder.ServerBalancePlaceholders;
import su.nightexpress.coinsengine.tops.placeholder.TopBalancePlaceholders;
import su.nightexpress.coinsengine.user.CoinsUser;
import su.nightexpress.coinsengine.tops.command.TopCommand;
import su.nightexpress.coinsengine.tops.menu.TopMenu;
import su.nightexpress.coinsengine.user.UserManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.LowerCase;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TopManager extends AbstractManager<CoinsEnginePlugin> {

    private final CurrencyRegistry currencyRegistry;
    private final CommandManager   commandManager;
    private final UserManager      userManager;

    private final Map<String, Map<String, TopEntry>> topEntries;

    private TopMenu topMenu;

    public TopManager(@NonNull CoinsEnginePlugin plugin,
                      @NonNull CurrencyRegistry currencyRegistry,
                      @NonNull CommandManager commandManager,
                      @NonNull UserManager userManager) {
        super(plugin);
        this.currencyRegistry = currencyRegistry;
        this.commandManager = commandManager;
        this.userManager = userManager;
        this.topEntries = new ConcurrentHashMap<>();
    }

    @Override
    protected void onLoad() {
        this.plugin.injectLang(TopLang.class);

        if (Config.TOPS_USE_GUI.get()) {
            this.topMenu = new TopMenu(this.plugin, this);
            this.topMenu.load(this.plugin, FileConfig.load(this.plugin.getDataFolder().toPath().resolve(EconomyFiles.DIR_MENU).resolve(TopFiles.FILE_LEADERBOARD)));
        }

        this.loadCommands();
        this.plugin.addGlobalPlaceholders(new ServerBalancePlaceholders(this));
        this.plugin.addGlobalPlaceholders(new TopBalancePlaceholders(this.currencyRegistry, this));

        this.addListener(new TopListener(this.plugin, this));

        this.addAsyncTask(this::updateBalances, Config.TOPS_UPDATE_INTERVAL.get());
    }

    @Override
    protected void onShutdown() {
        this.topEntries.clear();
    }

    private void loadCommands() {
        this.commandManager.addCurrencyCommand("top",
            () -> new TopCommand(this),
            CommandDefinition.allEnabled(TopDefaults.COMMAND_TOP,  "balancetop", "baltop"),
            ExcellentCurrency::isLeaderboardEnabled
        );
    }

    public void updateBalances() {
        this.topEntries.clear();

        Set<CoinsUser> users = this.userManager.getAll();

        users.removeIf(user -> {
            user.player().ifPresent(this::hideOrShowInTops);
            return user.isHiddenFromTops();
        });

        this.currencyRegistry.getCurrencies().forEach(currency -> {
            AtomicInteger counter = new AtomicInteger(0);
            Map<String, TopEntry> entries = new LinkedHashMap<>();

            users.stream().sorted(Comparator.comparingDouble((CoinsUser user) -> user.getBalance(currency)).reversed()).forEach(user -> {
                entries.put(LowerCase.INTERNAL.apply(user.getName()), new TopEntry(counter.incrementAndGet(), user.getName(), user.getId(), user.getBalance(currency)));
            });

            this.topEntries.put(currency.getId(), entries);
        });
    }

    public void handleJoin(@NonNull PlayerJoinEvent event) {
        this.hideOrShowInTops(event.getPlayer());
    }

    public void handleQuit(@NonNull PlayerQuitEvent event) {
        this.hideOrShowInTops(event.getPlayer());
    }

    public void hideOrShowInTops(@NonNull Player player) {
        boolean state = player.hasPermission(Perms.HIDE_FROM_TOPS);
        this.hideOrShowInTops(player, state);
    }

    public void hideOrShowInTops(@NonNull Player player, boolean state) {
        this.hideOrShowInTops(this.userManager.getOrFetch(player), state);
    }

    public void hideOrShowInTops(@NonNull CoinsUser user, boolean state) {
        if (user.isHiddenFromTops() != state) {
            user.setHiddenFromTops(state);
            user.markDirty();
        }
    }

    public boolean showLeaderboard(@NonNull CommandSender sender, @NonNull ExcellentCurrency currency, int page) {
        if (sender instanceof Player player && this.topMenu != null) {
            this.topMenu.show(this.plugin, player, currency);
            return true;
        }

        int perPage = Config.TOPS_ENTRIES_PER_PAGE.get();

        List<TopEntry> full = this.getTopEntries(currency);

        List<List<TopEntry>> split = Lists.split(full, perPage);
        int pages = split.size();
        int index = Math.max(0, Math.min(pages, page) - 1);
        int realPage = index + 1;

        List<TopEntry> entries = pages > 0 ? split.get(index) : new ArrayList<>();

        boolean hasNextPage = realPage < pages;
        boolean hasPrevPage = index > 0;

        currency.sendPrefixed(Lang.TOP_LIST, sender, builder -> builder
            .with(EconomyPlaceholders.GENERIC_NEXT_PAGE, () -> (hasNextPage ? Lang.TOP_LIST_NEXT_PAGE_ACTIVE : Lang.TOP_LIST_NEXT_PAGE_INACTIVE).text()
                .replace(CommonPlaceholders.GENERIC_VALUE, String.valueOf(realPage + 1))
            )
            .with(EconomyPlaceholders.GENERIC_PREVIOUS_PAGE, () -> (hasPrevPage ? Lang.TOP_LIST_PREVIOUS_PAGE_ACTIVE : Lang.TOP_LIST_PREVIOUS_PAGE_INACTIVE).text()
                .replace(CommonPlaceholders.GENERIC_VALUE, String.valueOf(realPage - 1))
            )
            .with(currency.placeholders())
            .with(EconomyPlaceholders.GENERIC_CURRENT, () -> String.valueOf(realPage))
            .with(EconomyPlaceholders.GENERIC_MAX, () -> String.valueOf(pages))
            .with(EconomyPlaceholders.GENERIC_ENTRY, () -> entries.stream().map(entry -> PlaceholderContext.builder()
                    .with(EconomyPlaceholders.GENERIC_POS, () -> NumberUtil.format(entry.getPosition()))
                    .with(EconomyPlaceholders.GENERIC_BALANCE, () -> currency.format(entry.getBalance()))
                    .with(CommonPlaceholders.PLAYER_NAME, entry::getName)
                    .build()
                    .apply(Lang.TOP_ENTRY.text())
                ).collect(Collectors.joining("\n"))
            )
        );

        return true;
    }

    @NonNull
    public Map<String, Map<String, TopEntry>> getTopEntriesMap() {
        return this.topEntries;
    }

    @NonNull
    public List<TopEntry> getTopEntries(@NonNull ExcellentCurrency currency) {
        return new ArrayList<>(this.topEntries.getOrDefault(currency.getId(), Collections.emptyMap()).values());
    }

    @Nullable
    public TopEntry getTopEntry(@NonNull ExcellentCurrency currency, @NonNull String name) {
        return this.topEntries.getOrDefault(currency.getId(), Collections.emptyMap()).get(LowerCase.INTERNAL.apply(name));
    }

    public double getTotalBalance(@NonNull ExcellentCurrency currency) {
        return this.getTopEntries(currency).stream().mapToDouble(TopEntry::getBalance).sum();
    }
}
