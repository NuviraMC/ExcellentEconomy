package su.nightexpress.coinsengine.currency.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.coinsengine.CoinsEnginePlugin;
import su.nightexpress.coinsengine.command.CommandArguments;
import su.nightexpress.coinsengine.config.Config;
import su.nightexpress.coinsengine.config.Lang;
import su.nightexpress.coinsengine.config.Perms;
import su.nightexpress.coinsengine.currency.CurrencyManager;
import su.nightexpress.coinsengine.user.CoinsUser;
import su.nightexpress.coinsengine.user.UserManager;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.command.NightCommand;
import su.nightexpress.nightcore.core.config.CoreLang;

public class WalletCommand {

    @NonNull
    public static NightCommand create(@NonNull CoinsEnginePlugin plugin, @NonNull CurrencyManager manager, @NonNull UserManager userManager) {
        return NightCommand.literal(plugin, Config.WALLET_ALIASES.get(), builder -> builder
            .description(Lang.COMMAND_WALLET_DESC)
            .permission(Perms.COMMAND_WALLET)
            .withArguments(Arguments.playerName(CommandArguments.PLAYER).permission(Perms.COMMAND_WALLET_OTHERS).optional())
            .executes((context, arguments) -> {
                CommandSender sender = context.getSender();

                if (!arguments.contains(CommandArguments.PLAYER)) {
                    if (!context.isPlayer()) {
                        context.errorBadPlayer();
                        return false;
                    }

                    Player player = context.getPlayerOrThrow();
                    return manager.showWallet(player);
                }

                String name = arguments.getString(CommandArguments.PLAYER);

                userManager.loadByNameAsync(name).thenAccept(opt -> {
                    CoinsUser user = opt.orElse(null);
                    if (user == null) {
                        CoreLang.ERROR_INVALID_PLAYER.withPrefix(plugin).send(sender);
                        return;
                    }

                    manager.showWallet(sender, user);
                });

                return true;
            })
        );
    }
}
