package su.nightexpress.coinsengine;

import su.nightexpress.coinsengine.command.currency.CommandDefinition;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.ConfigType;

public class EconomyConfigTypes {

    public static final ConfigType<CommandDefinition> COMMAND_DEFINITION = ConfigType.of(CommandDefinition::read, FileConfig::set);
}
