package su.nightexpress.excellenteconomy.migration.impl;

import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;

import su.nightexpress.excellenteconomy.EconomyPlugin;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import su.nightexpress.excellenteconomy.hook.HookPlugin;
import su.nightexpress.excellenteconomy.migration.Migrator;

import java.util.HashMap;
import java.util.Map;

public class VaultMigrator extends Migrator {

    public VaultMigrator(@NonNull EconomyPlugin plugin) {
        super(plugin, HookPlugin.PLAYER_POINTS);
    }

    @Override
    public boolean canMigrate(@NonNull ExcellentCurrency currency) {
        return !currency.isPrimary();
    }

    @Override
    @NonNull
    public Map<OfflinePlayer, Double> getBalances(@NonNull ExcellentCurrency currency) {
        return new HashMap<>();
    }
}
