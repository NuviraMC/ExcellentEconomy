package su.nightexpress.excellenteconomy.user;

import org.jspecify.annotations.NonNull;

import su.nightexpress.excellenteconomy.EconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import su.nightexpress.excellenteconomy.currency.CurrencyRegistry;
import su.nightexpress.excellenteconomy.data.DataHandler;
import su.nightexpress.excellenteconomy.user.data.CurrencySettings;
import su.nightexpress.nightcore.user.AbstractUserManager;
import su.nightexpress.nightcore.user.data.DefaultUserDataAccessor;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UserManager extends AbstractUserManager<EconomyPlugin, CoinsUser> {

    private final CurrencyRegistry registry;

    public UserManager(@NonNull EconomyPlugin plugin, @NonNull CurrencyRegistry registry,
                       @NonNull DataHandler dataHandler) {
        super(plugin, new DefaultUserDataAccessor<>(dataHandler, dataHandler));
        this.registry = registry;
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
        user.setLastSeen(System.currentTimeMillis());
    }

    @Override
    protected void handleQuit(@NonNull CoinsUser user) {
        user.setLastSeen(System.currentTimeMillis());
        this.getDataAccessor().save(user);
    }

    @Override
    protected void synchronize(@NonNull CoinsUser fetched, @NonNull CoinsUser cached) {
        for (ExcellentCurrency currency : this.registry.getCurrencies()) {
            if (!currency.isSynchronizable()) continue;

            double fetchedBalance = fetched.getBalance(currency);
            double cachedBalance = cached.getBalance(currency);

            if (fetchedBalance != cachedBalance) {
                cached.getBalance().set(currency, fetchedBalance);
            }
        }
    }

    @NonNull
    public Optional<CoinsUser> getOrFetch(@NonNull UUID uuid) {
        CoinsUser cached = this.getCachedUser(uuid);
        if (cached != null) return Optional.of(cached);

        return this.getDataAccessor().load(uuid);
    }

    @NonNull
    public Optional<CoinsUser> getOrFetch(@NonNull String name) {
        CoinsUser cached = this.getOnlineUsers().stream()
            .filter(u -> u.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
        if (cached != null) return Optional.of(cached);

        return this.getDataAccessor().load(name);
    }
}
