package su.nightexpress.coinsengine.user;

import org.jspecify.annotations.NonNull;
import su.nightexpress.coinsengine.CoinsEnginePlugin;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import su.nightexpress.coinsengine.currency.CurrencyRegistry;
import su.nightexpress.coinsengine.data.DataHandler;
import su.nightexpress.coinsengine.user.data.CurrencySettings;
import su.nightexpress.nightcore.user.AbstractUserManager;
import su.nightexpress.nightcore.user.data.DefaultUserDataAccessor;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager extends AbstractUserManager<CoinsEnginePlugin, CoinsUser> {

    private static final long STALE_SYNC_GUARD_MILLIS = 15_000L;

    private final CurrencyRegistry registry;
    private final Map<UUID, Long>  recentBalanceChanges;

    public UserManager(@NonNull CoinsEnginePlugin plugin, @NonNull CurrencyRegistry registry, @NonNull DataHandler dataHandler) {
        super(plugin, new DefaultUserDataAccessor<>(dataHandler, dataHandler));
        this.registry = registry;
        this.recentBalanceChanges = new ConcurrentHashMap<>();
    }

    @Override
    @NonNull
    protected CoinsUser create(@NonNull UUID uuid, @NonNull String name, @NonNull InetAddress address) {
        return this.create(uuid, name);
    }

    @NonNull
    public CoinsUser create(@NonNull UUID uuid, @NonNull String name) {
        UserBalance balance = new UserBalance();
        Map<String, CurrencySettings> settingsMap = new HashMap<>();
        long lastSeen = System.currentTimeMillis();
        boolean hiddenFromTops = false;

        this.registry.getCurrencies().forEach(currency -> balance.set(currency, currency.getStartValue()));

        return new CoinsUser(uuid, name, balance, settingsMap, lastSeen, hiddenFromTops);
    }

    @Override
    protected void handleJoin(@NonNull CoinsUser user) {
        // Refresh from storage on each join so temporary cache does not serve stale cross-server balances.
        this.plugin.runTaskAsync(() -> this.getDataAccessor().loadById(user.getId()).ifPresent(fetched -> this.synchronize(fetched, user)));
        user.setLastSeen(System.currentTimeMillis());
        user.markDirty();
    }

    @Override
    protected void handleQuit(@NonNull CoinsUser user) {
        user.setLastSeen(System.currentTimeMillis());
        this.markRecentChange(user);

        // Force-write before the framework's async quit update to shrink server-switch race windows.
        try {
            this.getDataAccessor().update(user);
        }
        catch (Exception exception) {
            this.plugin.error("Could not flush user data on quit for '" + user.getName() + "' (" + user.getId() + "):");
            exception.printStackTrace();
        }
    }

    @Override
    protected void synchronize(@NonNull CoinsUser fetched, @NonNull CoinsUser cached) {
        long lastChange = this.recentBalanceChanges.getOrDefault(cached.getId(), 0L);
        if (System.currentTimeMillis() - lastChange < STALE_SYNC_GUARD_MILLIS) return;

        for (ExcellentCurrency currency : this.registry.getCurrencies()) {
            if (!currency.isSynchronizable()) continue;

            double balance = fetched.getBalance(currency);
            cached.getBalance().set(currency, balance); // Bypass balance event call.
        }
    }

    public void persistSoon(@NonNull CoinsUser user) {
        this.markRecentChange(user);
        this.plugin.runTaskAsync(() -> this.getDataAccessor().update(user));
    }

    public void persistSoon(@NonNull Collection<CoinsUser> users) {
        if (users.isEmpty()) return;
        users.forEach(this::markRecentChange);
        this.plugin.runTaskAsync(() -> this.getDataAccessor().update(users));
    }

    public void markRecentChange(@NonNull CoinsUser user) {
        this.recentBalanceChanges.put(user.getId(), System.currentTimeMillis());
    }
}
