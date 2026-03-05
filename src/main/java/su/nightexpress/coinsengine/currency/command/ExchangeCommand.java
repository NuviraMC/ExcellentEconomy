package su.nightexpress.coinsengine.currency.command;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import su.nightexpress.coinsengine.command.CommandArguments;
import su.nightexpress.coinsengine.command.currency.CurrencyCommand;
import su.nightexpress.coinsengine.config.Lang;
import su.nightexpress.coinsengine.config.Perms;
import su.nightexpress.coinsengine.currency.CurrencyManager;
import su.nightexpress.coinsengine.currency.CurrencyRegistry;
import su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.ParsedArguments;

public class ExchangeCommand implements CurrencyCommand {

    private final CurrencyRegistry registry;
    private final CurrencyManager manager;
    
    public ExchangeCommand(@NonNull CurrencyRegistry registry, @NonNull CurrencyManager manager) {
        this.registry = registry;
        this.manager = manager;
    }

    @Override
    public boolean isFallback() {
        return false;
    }

    @Override
    public void build(@NonNull LiteralNodeBuilder builder, @NonNull ExcellentCurrency currency) {
        builder
            .playerOnly()
            .permission(Perms.COMMAND_CURRENCY_EXCHANGE)
            .description(Lang.COMMAND_CURRENCY_EXCHANGE_DESC)
            .withArguments(
                CommandArguments.currency(this.registry, currency::canExchangeTo),
                CommandArguments.positiveAmount(currency)
            );
    }

    @Override
    public boolean execute(@NonNull CommandContext context, @NonNull ParsedArguments arguments, @NonNull ExcellentCurrency currency) {
        Player player = context.getPlayerOrThrow();
        ExcellentCurrency targetCurrency = arguments.get(CommandArguments.CURRENCY, ExcellentCurrency.class);
        double amount = arguments.getDouble(CommandArguments.AMOUNT);

        return this.manager.exchange(player, currency, targetCurrency, amount);
    }
}
