package su.nightexpress.excellenteconomy.currency;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellenteconomy.currency.impl.AbstractCurrency;
import su.nightexpress.excellenteconomy.currency.impl.NormalCurrency;

import java.nio.file.Path;

public class CurrencyFactory {

    private CurrencyFactory() {
    }

    @NonNull
    public static AbstractCurrency createNormal(@NonNull Path path, @NonNull String id) {
        return new NormalCurrency(path, id);
    }
}
