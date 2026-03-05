package su.nightexpress.coinsengine.currency;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;
import su.nightexpress.coinsengine.currency.impl.AbstractCurrency;
import su.nightexpress.coinsengine.currency.impl.EconomyCurrency;
import su.nightexpress.coinsengine.currency.impl.NormalCurrency;
import su.nightexpress.coinsengine.data.DataHandler;
import su.nightexpress.coinsengine.user.UserManager;

import java.nio.file.Path;

public class CurrencyFactory {

    private CurrencyFactory() {}

    @NonNull
    public static AbstractCurrency createEconomy(@NonNull Path path,
                                                 @NonNull String id,
                                                 @NonNull ExcellentEconomyAPI plugin,
                                                 @NonNull CurrencyManager currencyManager,
                                                 @NonNull DataHandler dataHandler,
                                                 @NonNull UserManager userManager) {
        return new EconomyCurrency(path, id, plugin, currencyManager, dataHandler, userManager);
    }

    @NonNull
    public static AbstractCurrency createNormal(@NonNull Path path, @NonNull String id) {
        return new NormalCurrency(path, id);
    }
}
