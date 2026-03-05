package su.nightexpress.coinsengine.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import su.nightexpress.coinsengine.command.CommandManager;
import su.nightexpress.coinsengine.currency.CurrencyManager;
import su.nightexpress.coinsengine.currency.CurrencyRegistry;
import su.nightexpress.coinsengine.user.CoinsUser;
import su.nightexpress.coinsengine.user.UserManager;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import su.nightexpress.excellenteconomy.api.currency.operation.OperationContext;
import su.nightexpress.nightcore.util.ServerUtils;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Deprecated(forRemoval = true)
public class CoinsEngineAPI {

    public static void load(@NotNull ExcellentEconomyAPI plugin) {
        //CoinsEngineAPI.api = plugin;
    }

    public static void clear() {
        //api = null;
    }

    public static boolean isLoaded() {
        return true;
    }

    /*@NotNull
    public static CoinsEnginePlugin plugin() {
        if (plugin == null) throw new IllegalStateException("API is not yet initialized!");

        return plugin;
    }*/

    @NonNull
    private static ExcellentEconomyAPI api() {
        return ServerUtils.serviceProvider(ExcellentEconomyAPI.class).orElseThrow();
    }

    @NotNull
    public static UserManager getUserManager() {
        return api().userManager();
    }

    @NotNull
    public static CurrencyManager getCurrencyManager() {
        return api().currencyManager();
    }

    @NotNull
    public static CurrencyRegistry getCurrencyRegistry() {
        return api().currencyRegistry();
    }

    @NotNull
    public static CommandManager getCommands() {
        return api().commandManager();
    }

    @NotNull
    public static Collection<ExcellentCurrency> getCurrencies() { // keep it Collection for the API compatibility
        return getCurrencyRegistry().getCurrencies();
    }

    @Nullable
    public static ExcellentCurrency getCurrency(@NotNull String id) {
        return getCurrencyRegistry().getById(id);
    }

    public static boolean hasCurrency(@NotNull String id) {
        return getCurrencyRegistry().isRegistered(id);
    }

    public static void regsiterCurrency(@NotNull ExcellentCurrency currency) {
        getCurrencyManager().registerCurrency(currency);
    }

    /*public static void regsiterCurrencyWithCommands(@NotNull Currency currency) {
        regsiterCurrency(currency);
        getCommandManager().getCurrencyCommands().loadCommands(currency);
    }

    public static void unregsiterCurrency(@NotNull Currency currency) {
        getCommandManager().getCurrencyCommands().unregisterCommands(currency);
        getCurrencyManager().unregisterCurrency(currency);
    }*/



    public static double getBalance(@NotNull UUID playerId, @NotNull String currencyName) {
        ExcellentCurrency currency = getCurrency(currencyName);

        return currency == null ? 0D : getBalance(playerId, currency);
    }

    public static double getBalance(@NotNull UUID playerId, @NotNull ExcellentCurrency currency) {
        CoinsUser user = getUserData(playerId);

        return user == null ? 0D : user.getBalance(currency);
    }

    public static double getBalance(@NotNull Player player, @NotNull ExcellentCurrency currency) {
        return getUserData(player).getBalance(currency);
    }



    public static boolean addBalance(@NotNull UUID playerId, @NotNull String currencyName, double amount) {
        api().depositAsync(playerId, currencyName, amount);
        return true;
    }

    public static boolean addBalance(@NotNull UUID playerId, @NotNull ExcellentCurrency currency, double amount) {
        api().depositAsync(playerId, currency, amount);
        return true;
    }

    public static boolean addBalance(@NotNull UUID playerId, @NotNull ExcellentCurrency currency, double amount, @NotNull OperationContext context) {
        api().depositAsync(playerId, currency, amount, context);
        return true;
    }

    public static void addBalance(@NotNull Player player, @NotNull ExcellentCurrency currency, double amount) {
        api().deposit(player, currency, amount);
    }

    public static boolean addBalance(@NotNull Player player, @NotNull ExcellentCurrency currency, double amount, @NotNull OperationContext context) {
        return api().deposit(player, currency, amount, context);
    }


    public static boolean removeBalance(@NotNull UUID playerId, @NotNull String currencyName, double amount) {
        api().withdrawAsync(playerId, currencyName, amount);
        return true;
    }

    public static boolean removeBalance(@NotNull UUID playerId, @NotNull ExcellentCurrency currency, double amount) {
        api().withdrawAsync(playerId, currency, amount);
        return true;
    }

    public static boolean removeBalance(@NotNull UUID playerId, @NotNull ExcellentCurrency currency, double amount, @NotNull OperationContext context) {
        api().withdrawAsync(playerId, currency, amount);
        return true;
    }

    public static void removeBalance(@NotNull Player player, @NotNull ExcellentCurrency currency, double amount) {
        api().withdraw(player, currency, amount);
    }

    public static boolean removeBalance(@NotNull Player player, @NotNull ExcellentCurrency currency, double amount, @NotNull OperationContext context) {
        return api().withdraw(player, currency, amount, context);
    }


    public static boolean setBalance(@NotNull UUID playerId, @NotNull String currencyName, double amount) {
        api().setBalanceAsync(playerId, currencyName, amount);
        return true;
    }

    public static boolean setBalance(@NotNull UUID playerId, @NotNull ExcellentCurrency currency, double amount) {
        api().setBalanceAsync(playerId, currency, amount);
        return true;
    }

    public static boolean setBalance(@NotNull UUID playerId, @NotNull ExcellentCurrency currency, double amount, @NotNull OperationContext context) {
        api().setBalanceAsync(playerId, currency, amount, context);
        return true;
    }

    public static void setBalance(@NotNull Player player, @NotNull ExcellentCurrency currency, double amount) {
        api().setBalance(player, currency, amount);
    }

    public static boolean setBalance(@NotNull Player player, @NotNull ExcellentCurrency currency, double amount, @NotNull OperationContext context) {
        return api().setBalance(player, currency, amount, context);
    }


    @NotNull
    public static CoinsUser getUserData(@NotNull Player player) {
        return getUserManager().getOrFetch(player);
    }

    @Nullable
    public static CoinsUser getUserData(@NotNull String name) {
        return getUserManager().getOrFetch(name).orElse(null);
    }

    @Nullable
    public static CoinsUser getUserData(@NotNull UUID uuid) {
        return getUserManager().getOrFetch(uuid).orElse(null);
    }

    @NotNull
    public static CompletableFuture<Optional<CoinsUser>> getUserDataAsync(@NotNull String name) {
        return getUserManager().loadByNameAsync(name);
    }

    @NotNull
    public static CompletableFuture<Optional<CoinsUser>> getUserDataAsync(@NotNull UUID uuid) {
        return getUserManager().loadByIdAsync(uuid);
    }
}
