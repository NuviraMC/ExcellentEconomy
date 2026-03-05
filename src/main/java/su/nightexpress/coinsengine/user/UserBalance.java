package su.nightexpress.coinsengine.user;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;

import java.util.HashMap;
import java.util.Map;

public class UserBalance {

    private final Map<String, Double> balanceMap;

    public UserBalance() {
        this(new HashMap<>());
    }

    public UserBalance(@NotNull Map<String, Double> balanceMap) {
        this.balanceMap = balanceMap;
    }

    @NotNull
    public Map<String, Double> getBalanceMap() {
        return this.balanceMap;
    }

    public void clear() {
        this.balanceMap.clear();
    }

    public void clear(@NotNull ExcellentCurrency currency) {
        this.clear(currency.getId());
    }

    public void clear(@NotNull String currencyId) {
        this.balanceMap.remove(currencyId);
    }

    public boolean has(@NotNull ExcellentCurrency currency, double amount) {
        return this.get(currency) >= amount;
    }

    public double get(@NotNull ExcellentCurrency currency) {
        return this.get(currency.getId());
    }

    public double get(@NotNull String currencyId) {
        return Math.max(0, this.balanceMap.getOrDefault(currencyId, 0D));
    }

    public void add(@NotNull ExcellentCurrency currency, double amount) {
        this.add(currency.getId(), amount);
    }

    public void add(@NotNull String currencyId, double amount) {
        this.set(currencyId, this.get(currencyId) + Math.abs(amount));
    }

    public void remove(@NotNull ExcellentCurrency currency, double amount) {
        this.remove(currency.getId(), amount);
    }

    public void remove(@NotNull String currencyId, double amount) {
        this.set(currencyId, this.get(currencyId) - Math.abs(amount));
    }

    public void set(@NotNull ExcellentCurrency currency, double amount) {
        this.set(currency.getId(), currency.floorAndLimit(amount));
    }

    public void set(@NotNull String currencyId, double amount) {
        this.balanceMap.put(currencyId, Math.max(0, amount));
    }
}
