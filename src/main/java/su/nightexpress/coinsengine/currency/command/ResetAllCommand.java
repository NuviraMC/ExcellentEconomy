package su.nightexpress.coinsengine.currency.command;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import su.nightexpress.coinsengine.command.CommandArguments;
import su.nightexpress.coinsengine.config.Lang;
import su.nightexpress.coinsengine.config.Perms;
import su.nightexpress.coinsengine.currency.CurrencyManager;
import su.nightexpress.coinsengine.currency.CurrencyRegistry;
import su.nightexpress.nightcore.commands.Commands;
import su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder;

public class ResetAllCommand {

    @NonNull
    public static LiteralNodeBuilder create(@NonNull CurrencyRegistry registry, @NonNull CurrencyManager manager) {
        return Commands.literal("resetall")
            .permission(Perms.COMMAND_RESET_ALL)
            .description(Lang.COMMAND_RESET_ALL_DESC)
            .withArguments(CommandArguments.currency(registry).optional())
            .executes((context, arguments) -> {
                if (arguments.contains(CommandArguments.CURRENCY)) {
                    ExcellentCurrency currency = arguments.get(CommandArguments.CURRENCY, ExcellentCurrency.class);
                    manager.resetBalances(context.getSender(), currency);
                }
                else {
                    manager.resetBalances(context.getSender());
                }
                return true;
            });
    }
}
