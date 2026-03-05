package su.nightexpress.coinsengine.currency.command;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import su.nightexpress.coinsengine.command.CommandArguments;
import su.nightexpress.coinsengine.command.currency.CurrencyCommand;
import su.nightexpress.coinsengine.config.Lang;
import su.nightexpress.coinsengine.config.Perms;
import su.nightexpress.coinsengine.currency.CurrencyManager;
import su.nightexpress.excellenteconomy.api.currency.operation.NotificationTarget;
import su.nightexpress.excellenteconomy.api.currency.operation.OperationContext;
import su.nightexpress.excellenteconomy.api.currency.operation.OperationResult;
import su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.ParsedArguments;

public class GiveAllCommand implements CurrencyCommand {

    private final CurrencyManager manager;
    
    public GiveAllCommand(@NonNull CurrencyManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean isFallback() {
        return false;
    }

    @Override
    public void build(@NonNull LiteralNodeBuilder builder, @NonNull ExcellentCurrency currency) {
        builder
            .permission(Perms.COMMAND_CURRENCY_GIVE_ALL)
            .description(Lang.COMMAND_CURRENCY_GIVE_ALL_DESC)
            .withArguments(CommandArguments.positiveAmount(currency))
            .withFlags(CommandArguments.FLAG_SILENT, CommandArguments.FLAG_NO_FEEDBACK);
    }

    @Override
    public boolean execute(@NonNull CommandContext context, @NonNull ParsedArguments arguments, @NonNull ExcellentCurrency currency) {
        double amount = arguments.getDouble(CommandArguments.AMOUNT);

        OperationContext operationContext = OperationContext.of(context.getSender())
            .silentFor(NotificationTarget.CONSOLE_LOGGER)
            .silentFor(NotificationTarget.USER, context.hasFlag(CommandArguments.FLAG_SILENT))
            .silentFor(NotificationTarget.EXECUTOR, context.hasFlag(CommandArguments.FLAG_NO_FEEDBACK));

        return this.manager.giveAll(operationContext, currency, amount) == OperationResult.SUCCESS;
    }
}
