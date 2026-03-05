package su.nightexpress.coinsengine.user;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import su.nightexpress.coinsengine.api.event.ChangeBalanceEvent;
import su.nightexpress.coinsengine.user.data.CurrencySettings;
import su.nightexpress.nightcore.user.UserTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class CoinsUser extends UserTemplate {

    private final UserBalance                   balance;
    private final Map<String, CurrencySettings> settingsMap;

    private long    lastSeen;
    private boolean hiddenFromTops;

    public CoinsUser(@NotNull UUID uuid,
                     @NotNull String name,
                     @NotNull UserBalance balance,
                     @NotNull Map<String, CurrencySettings> settingsMap,
                     long lastSeen,
                     boolean hiddenFromTops) {
        super(uuid, name);
        this.balance = balance;
        this.settingsMap = new HashMap<>(settingsMap);

        this.setLastSeen(lastSeen);
        this.setHiddenFromTops(hiddenFromTops);
    }

    @NotNull
    @Deprecated
    public Map<String, Double> getBalanceMap() {
        return this.balance.getBalanceMap();
    }

    @NotNull
    public UserBalance getBalance() {
        return this.balance;
    }

    /**
     * Edits user's balance of specific currency and fires the ChangeBalanceEvent event. If event was cancelled, the balance is set back to previous (old) value.
     *
     * @param currency Currency to edit balance of.
     * @param consumer balance function.
     */
    public void editBalance(@NotNull ExcellentCurrency currency, @NotNull Consumer<UserBalance> consumer) {
        double oldBalance = this.getBalance(currency);

        consumer.accept(this.balance);

        ChangeBalanceEvent event = new ChangeBalanceEvent(this, currency, oldBalance, this.getBalance(currency));
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            this.balance.set(currency, oldBalance);
        }
    }

    public void resetBalance(@NotNull Collection<ExcellentCurrency> currencies) {
        currencies.forEach(this::resetBalance);
    }

    public void resetBalance(@NotNull ExcellentCurrency currency) {
        this.editBalance(currency, balance -> balance.set(currency, currency.getStartValue()));
    }

    public boolean hasEnough(@NotNull ExcellentCurrency currency, double amount) {
        return this.balance.has(currency, amount);
    }

    public double getBalance(@NotNull ExcellentCurrency currency) {
        return this.balance.get(currency);
    }

    public void addBalance(@NotNull ExcellentCurrency currency, double amount) {
        this.editBalance(currency, balance -> balance.add(currency, amount));
    }

    public void removeBalance(@NotNull ExcellentCurrency currency, double amount) {
        this.editBalance(currency, lookup -> lookup.remove(currency, amount));
    }

    public void setBalance(@NotNull ExcellentCurrency currency, double amount) {
        this.editBalance(currency, lookup -> lookup.set(currency, amount));
    }

    @NotNull
    public Map<String, CurrencySettings> getSettingsMap() {
        return this.settingsMap;
    }

    @NotNull
    public CurrencySettings getSettings(@NotNull ExcellentCurrency currency) {
        return this.settingsMap.computeIfAbsent(currency.getId(), k -> CurrencySettings.create(currency));
    }

    public long getLastSeen() {
        return this.lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public boolean isHiddenFromTops() {
        return this.hiddenFromTops;
    }

    public void setHiddenFromTops(boolean hiddenFromTops) {
        this.hiddenFromTops = hiddenFromTops;
    }
}
