package su.nightexpress.excellenteconomy.currency.impl;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellenteconomy.api.ExcellentEconomyAPI;
import su.nightexpress.excellenteconomy.currency.CurrencyManager;
import su.nightexpress.excellenteconomy.data.DataHandler;
import su.nightexpress.excellenteconomy.user.UserManager;

import java.nio.file.Path;

public class EconomyCurrency extends AbstractCurrency {

    private final ExcellentEconomyAPI api;

    public EconomyCurrency(@NonNull Path path,
                           @NonNull String id,
                           @NonNull ExcellentEconomyAPI api,
                           @NonNull CurrencyManager currencyManager,
                           @NonNull DataHandler dataHandler,
                           @NonNull UserManager userManager) {
        super(path, id);
        this.api = api;
    }

    @Override
    public void onRegister() {
    }

    @Override
    public void onUnregister() {
    }

    @Override
    public boolean isPrimary() {
        return true;
    }
}
