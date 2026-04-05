package su.nightexpress.coinsengine.currency.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.coinsengine.currency.CurrencyManager;
import su.nightexpress.coinsengine.data.DataHandler;
import su.nightexpress.coinsengine.user.UserManager;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;

import java.nio.file.Path;

public class EconomyCurrency extends NormalCurrency {

    public EconomyCurrency(@NotNull Path path,
                           @NotNull String id,
                           @NotNull ExcellentEconomyAPI api,
                           @NotNull CurrencyManager currencyManager,
                           @NotNull DataHandler dataHandler,
                           @NotNull UserManager userManager) {
        super(path, id);
    }

    @Override
    public boolean isPrimary() {
        return false;
    }
}
