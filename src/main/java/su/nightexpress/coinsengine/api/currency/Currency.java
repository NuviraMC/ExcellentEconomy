package su.nightexpress.coinsengine.api.currency;

import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.nightcore.util.number.CompactNumber;

@Deprecated(forRemoval = true)
public interface Currency extends ExcellentCurrency {

    @Deprecated
    default double fine(double amount) {
        return this.floorIfNeeded(amount);
    }

    @Deprecated
    default double limit(double amount) {
        return this.limitIfNeeded(amount);
    }

    @Deprecated
    default double fineAndLimit(double amount) {
        return this.floorAndLimit(amount);
    }

    @NonNull
    @Deprecated
    default CompactNumber formatCompactValue(double balance) {
        return this.compacted(balance);
    }

    @Deprecated
    @NonNull
    default ItemStack getIcon() {
        return this.icon().getItemStack();
    }

    @Deprecated
    default void setIcon(@NonNull ItemStack icon) {
        this.setIcon(NightItem.fromItemStack(icon));
    }

    @Deprecated
    default boolean isVaultEconomy() {
        return this.isPrimary();
    }
}
