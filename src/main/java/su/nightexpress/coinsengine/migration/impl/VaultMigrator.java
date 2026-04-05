package su.nightexpress.coinsengine.migration.impl;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.coinsengine.CoinsEnginePlugin;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import su.nightexpress.coinsengine.migration.Migrator;

import java.util.Collections;
import java.util.Map;

public class VaultMigrator extends Migrator {

    public VaultMigrator(@NotNull CoinsEnginePlugin plugin) {
        super(plugin, "Vault");
    }

    @Override
    public boolean canMigrate(@NotNull ExcellentCurrency currency) {
        return !currency.isPrimary();
    }

    @Override
    @NotNull
    public Map<OfflinePlayer, Double> getBalances(@NotNull ExcellentCurrency currency) {
        return Collections.emptyMap();
    }
}
