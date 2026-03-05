package su.nightexpress.coinsengine.migration.command;

import org.jspecify.annotations.NonNull;
import su.nightexpress.excellenteconomy.api.currency.ExcellentCurrency;
import su.nightexpress.coinsengine.command.CommandArguments;
import su.nightexpress.coinsengine.config.Lang;
import su.nightexpress.coinsengine.config.Perms;
import su.nightexpress.coinsengine.currency.CurrencyRegistry;
import su.nightexpress.coinsengine.migration.MigrationManager;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.Commands;
import su.nightexpress.nightcore.commands.builder.LiteralNodeBuilder;

public class MigrationCommand {

    @NonNull
    public static LiteralNodeBuilder create(@NonNull CurrencyRegistry registry, @NonNull MigrationManager manager) {
        return Commands.literal("migrate")
            .permission(Perms.COMMAND_MIGRATE)
            .description(Lang.COMMAND_MIGRATE_DESC)
            .withArguments(
                Arguments.string(CommandArguments.NAME).localized(Lang.COMMAND_ARGUMENT_NAME_PLUGIN).suggestions((reader, context) -> manager.getMigratorNames()),
                CommandArguments.currency(registry)
            )
            .executes((context, arguments) -> {
                String name = arguments.getString(CommandArguments.NAME);
                ExcellentCurrency currency = arguments.get(CommandArguments.CURRENCY, ExcellentCurrency.class);

                return manager.startMigration(context.getSender(), name, currency);
            });
    }
}
