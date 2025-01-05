package win.codingboulder.headWars;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.maps.HeadWarsMap;
import win.codingboulder.headWars.maps.HeadWarsMapManager;
import win.codingboulder.headWars.maps.HeadWarsTeam;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class HeadWarsCommand {

    public static void registerCommand(@NotNull LifecycleEventManager<@NotNull Plugin> lifecycleEventManager) {

        lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> event.registrar().register(

                Commands.literal("headwars")

                        .then(Commands.literal("map")

                                .then(Commands.literal("create")
                                        .then(Commands.argument("id", StringArgumentType.word())
                                                .then(Commands.argument("name", StringArgumentType.string())
                                                        .then(Commands.argument("world", StringArgumentType.word())
                                                                .executes(context -> {

                                                                    try {

                                                                        HeadWarsMapManager.createMap(
                                                                                context.getArgument("id", String.class),
                                                                                context.getArgument("name", String.class),
                                                                                context.getArgument("world", String.class)
                                                                        );

                                                                    } catch (IOException e) {

                                                                        context.getSource().getSender().sendRichMessage("<red>An error occurred while creating the map!");

                                                                        throw new RuntimeException(e);

                                                                    }

                                                                    return 1;

                                                                }))))
                                )

                                .then(Commands.literal("edit")
                                        .then(Commands.argument("id", StringArgumentType.word()).suggests(HeadWarsCommand::headWarsMapSuggestion)

                                                .then(Commands.literal("name")
                                                        .then(Commands.argument("newName", StringArgumentType.string())
                                                                .executes(context -> {

                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                    if (map == null) {
                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                        return 1;
                                                                    }

                                                                    map.setName(context.getArgument("newName", String.class));

                                                                    map.updateMapFile();

                                                                    return 1;

                                                                })))

                                                .then(Commands.literal("world")
                                                        .then(Commands.argument("newWorld", StringArgumentType.word())
                                                                .executes(context -> {

                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                    if (map == null) {
                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                        return 1;
                                                                    }

                                                                    map.setWorld(context.getArgument("newWorld", String.class));

                                                                    map.updateMapFile();

                                                                    return 1;

                                                                })))

                                                .then(Commands.literal("playersPerTeam")
                                                        .then(Commands.argument("newPlayersPerTeam", IntegerArgumentType.integer(1))
                                                                .executes(context -> {

                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                    if (map == null) {
                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                        return 1;
                                                                    }

                                                                    map.setPlayersPerTeam(context.getArgument("newPlayersPerTeam", Integer.class));

                                                                    map.updateMapFile();

                                                                    return 1;

                                                                    })))

                                                .then(Commands.literal("addTeam")
                                                        .then(Commands.argument("teamColor", ArgumentTypes.namedColor())
                                                                .executes(context -> {

                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                    if (map == null) {
                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                        return 1;
                                                                    }

                                                                    map.createTeam(context.getArgument("teamColor", NamedTextColor.class));

                                                                    map.updateMapFile();

                                                                    return 1;

                                                                    })))

                                                .then(Commands.literal("editTeam")
                                                        .then(Commands.argument("teamToEdit", ArgumentTypes.namedColor())

                                                                .then(Commands.literal("spawnPosition")
                                                                        .then(Commands.argument("newSpawn", ArgumentTypes.finePosition(true))
                                                                                .executes(context -> {

                                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                                    if (map == null) {
                                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                                        return 1;
                                                                                    }

                                                                                    HeadWarsTeam team = map.getTeam(context.getArgument("teamToEdit", NamedTextColor.class));
                                                                                    if (team == null) {
                                                                                        context.getSource().getSender().sendRichMessage("<red>This team doesn't exist!");
                                                                                        return 1;
                                                                                    }

                                                                                    FinePositionResolver resolver = context.getArgument("newSpawn", FinePositionResolver.class);
                                                                                    team.setSpawnPosition(resolver.resolve(context.getSource()));

                                                                                    map.updateMapFile();

                                                                                    return 1;

                                                                                })))

                                                                .then(Commands.literal("setBaseArea")
                                                                        .then(Commands.argument("baseCorner1", ArgumentTypes.blockPosition())
                                                                                .then(Commands.argument("baseCorner2", ArgumentTypes.blockPosition())
                                                                                        .executes(context -> {

                                                                                            HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                                            if (map == null) {
                                                                                                context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                                                return 1;
                                                                                            }

                                                                                            HeadWarsTeam team = map.getTeam(context.getArgument("teamToEdit", NamedTextColor.class));
                                                                                            if (team == null) {
                                                                                                context.getSource().getSender().sendRichMessage("<red>This team doesn't exist!");
                                                                                                return 1;
                                                                                            }

                                                                                            BlockPositionResolver positionResolver1 = context.getArgument("baseCorner1", BlockPositionResolver.class);
                                                                                            BlockPositionResolver positionResolver2 = context.getArgument("baseCorner2", BlockPositionResolver.class);

                                                                                            team.setBasePosition(positionResolver1.resolve(context.getSource()), positionResolver2.resolve(context.getSource()));

                                                                                            map.updateMapFile();

                                                                                            return 1;

                                                                                        }))))

                                                                .then(Commands.literal("setBasePerimeter")
                                                                        .then(Commands.argument("basePerimeterCorner1", ArgumentTypes.blockPosition())
                                                                                .then(Commands.argument("basePerimeterCorner2", ArgumentTypes.blockPosition())
                                                                                        .executes(context -> {

                                                                                            HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                                            if (map == null) {
                                                                                                context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                                                return 1;
                                                                                            }

                                                                                            HeadWarsTeam team = map.getTeam(context.getArgument("teamToEdit", NamedTextColor.class));
                                                                                            if (team == null) {
                                                                                                context.getSource().getSender().sendRichMessage("<red>This team doesn't exist!");
                                                                                                return 1;
                                                                                            }

                                                                                            BlockPositionResolver positionResolver1 = context.getArgument("basePerimeterCorner1", BlockPositionResolver.class);
                                                                                            BlockPositionResolver positionResolver2 = context.getArgument("basePerimeterCorner2", BlockPositionResolver.class);

                                                                                            team.setBasePerimeter(positionResolver1.resolve(context.getSource()), positionResolver2.resolve(context.getSource()));

                                                                                            map.updateMapFile();

                                                                                            return 1;

                                                                                        }))))

                                                                .then(Commands.literal("addHead")
                                                                        .then(Commands.argument("headToAdd", ArgumentTypes.blockPosition())
                                                                                .executes(context -> {

                                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                                    if (map == null) {
                                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                                        return 1;
                                                                                    }

                                                                                    HeadWarsTeam team = map.getTeam(context.getArgument("teamToEdit", NamedTextColor.class));
                                                                                    if (team == null) {
                                                                                        context.getSource().getSender().sendRichMessage("<red>This team doesn't exist!");
                                                                                        return 1;
                                                                                    }

                                                                                    BlockPositionResolver positionResolver = context.getArgument("headToAdd", BlockPositionResolver.class);
                                                                                    team.heads().add(positionResolver.resolve(context.getSource()));

                                                                                    map.updateMapFile();

                                                                                    return 1;

                                                                                })))

                                                                .then(Commands.literal("removeHead")
                                                                        .then(Commands.argument("headToRemove", ArgumentTypes.blockPosition())
                                                                                .executes(context -> {

                                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                                    if (map == null) {
                                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                                        return 1;
                                                                                    }

                                                                                    HeadWarsTeam team = map.getTeam(context.getArgument("teamToEdit", NamedTextColor.class));
                                                                                    if (team == null) {
                                                                                        context.getSource().getSender().sendRichMessage("<red>This team doesn't exist!");
                                                                                        return 1;
                                                                                    }

                                                                                    BlockPositionResolver positionResolver = context.getArgument("headToAdd", BlockPositionResolver.class);
                                                                                    team.heads().remove(positionResolver.resolve(context.getSource()));

                                                                                    map.updateMapFile();

                                                                                    return 1;

                                                                                }))))

                                                )

                                                .then(Commands.literal("addClickGenerator")
                                                        .then(Commands.argument("buttonPosition", ArgumentTypes.blockPosition())
                                                                .then(Commands.argument("itemSpawnPosition", ArgumentTypes.finePosition(true))
                                                                        .executes(context -> {

                                                                            HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                            if (map == null) {
                                                                                context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                                return 1;
                                                                            }

                                                                            BlockPositionResolver positionResolver1 = context.getArgument("buttonPosition", BlockPositionResolver.class);
                                                                            FinePositionResolver positionResolver2 = context.getArgument("itemSpawnPosition", FinePositionResolver.class);

                                                                            map.clickGenerators().put(positionResolver1.resolve(context.getSource()), positionResolver2.resolve(context.getSource()));

                                                                            map.updateMapFile();

                                                                            return 1;

                                                                        }))))

                                                .then(Commands.literal("removeClickGenerator")
                                                        .then(Commands.argument("generatorButtonPosition", ArgumentTypes.blockPosition())
                                                                .executes(context -> {

                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                    if (map == null) {
                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                        return 1;
                                                                    }

                                                                    BlockPositionResolver positionResolver1 = context.getArgument("generatorButtonPosition", BlockPositionResolver.class);
                                                                    map.clickGenerators().remove(positionResolver1.resolve(context.getSource()));

                                                                    map.updateMapFile();

                                                                    return 1;

                                                                })))

                                                .then(Commands.literal("addItemShop")
                                                        .then(Commands.argument("itemShopEntity", ArgumentTypes.entity())
                                                                .executes(context -> {

                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                    if (map == null) {
                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                        return 1;
                                                                    }

                                                                    EntitySelectorArgumentResolver resolver = context.getArgument("itemShopEntity", EntitySelectorArgumentResolver.class);

                                                                    UUID shopUUID = resolver.resolve(context.getSource()).getFirst().getUniqueId();
                                                                    map.itemShops().add(shopUUID);

                                                                    map.updateMapFile();

                                                                    return 1;

                                                                })))

                                                .then(Commands.literal("addUpgradeShop")
                                                        .then(Commands.argument("upgradeShopEntity", ArgumentTypes.entity())
                                                                .executes(context -> {

                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                    if (map == null) {
                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                        return 1;
                                                                    }

                                                                    EntitySelectorArgumentResolver resolver = context.getArgument("upgradeShopEntity", EntitySelectorArgumentResolver.class);

                                                                    UUID shopUUID = resolver.resolve(context.getSource()).getFirst().getUniqueId();
                                                                    map.upgradeShops().add(shopUUID);

                                                                    map.updateMapFile();

                                                                    return 1;

                                                                })))

                                                .then(Commands.literal("removeItemShop")
                                                        .then(Commands.argument("itemShopEntityToRemove", ArgumentTypes.entity())
                                                                .executes(context -> {

                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                    if (map == null) {
                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                        return 1;
                                                                    }

                                                                    EntitySelectorArgumentResolver resolver = context.getArgument("itemShopEntityToRemove", EntitySelectorArgumentResolver.class);

                                                                    UUID shopUUID = resolver.resolve(context.getSource()).getFirst().getUniqueId();
                                                                    map.itemShops().remove(shopUUID);

                                                                    map.updateMapFile();

                                                                    return 1;

                                                                })))

                                                .then(Commands.literal("removeUpgradeShop")
                                                        .then(Commands.argument("upgradeShopEntityToRemove", ArgumentTypes.entity())
                                                                .executes(context -> {

                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                    if (map == null) {
                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                        return 1;
                                                                    }

                                                                    EntitySelectorArgumentResolver resolver = context.getArgument("upgradeShopEntityToRemove", EntitySelectorArgumentResolver.class);

                                                                    UUID shopUUID = resolver.resolve(context.getSource()).getFirst().getUniqueId();
                                                                    map.upgradeShops().remove(shopUUID);

                                                                    map.updateMapFile();

                                                                    return 1;

                                                                })))

                                                .then(Commands.literal("addEmeraldGenerator")
                                                        .then(Commands.argument("emeraldGeneratorPosition", ArgumentTypes.blockPosition())
                                                                .executes(context -> {

                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                    if (map == null) {
                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                        return 1;
                                                                    }

                                                                    BlockPositionResolver positionResolver1 = context.getArgument("emeraldGeneratorPosition", BlockPositionResolver.class);
                                                                    map.emeraldGenerators().add(positionResolver1.resolve(context.getSource()));

                                                                    map.updateMapFile();

                                                                    return 1;

                                                                })))

                                                .then(Commands.literal("removeEmeraldGenerator")
                                                        .then(Commands.argument("emeraldGeneratorToRemovePosition", ArgumentTypes.blockPosition())
                                                                .executes(context -> {

                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                    if (map == null) {
                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                        return 1;
                                                                    }

                                                                    BlockPositionResolver positionResolver1 = context.getArgument("emeraldGeneratorToRemovePosition", BlockPositionResolver.class);
                                                                    map.emeraldGenerators().remove(positionResolver1.resolve(context.getSource()));

                                                                    map.updateMapFile();

                                                                    return 1;

                                                                })))

                                                .then(Commands.literal("setLobbySpawn")
                                                        .then(Commands.argument("lobbySpawnPosition", ArgumentTypes.finePosition())
                                                                .executes(context -> {

                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                    if (map == null) {
                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                        return 1;
                                                                    }

                                                                    FinePositionResolver resolver = context.getArgument("lobbySpawnPosition", FinePositionResolver.class);

                                                                    return 1;

                                                                }))))

                                )

                                .then(Commands.literal("delete")

                                )

                                .then(Commands.literal("teleport-to-world")
                                        .then(Commands.argument("teleportMapWorld", StringArgumentType.word())
                                                .executes(context -> {

                                                    World world = HeadWars.getInstance().getServer().createWorld(WorldCreator.name(context.getArgument("teleportMapWorld", String.class)));
                                                    Player player = (Player) context.getSource().getSender();
                                                    player.teleport(new Location(world, 0, 100, 0));

                                                    return 1;

                                                }))

                                )

                        )

                        .then(Commands.literal("game")

                                .then(Commands.literal("start")
                                        .then(Commands.argument("map", StringArgumentType.word()).suggests(HeadWarsCommand::headWarsMapSuggestion)
                                                .executes(context -> {



                                                    return 1;

                                                }))))


                        .build()
        ));

    }

    public static CompletableFuture<Suggestions> headWarsMapSuggestion(@NotNull CommandContext<CommandSourceStack> context, @NotNull SuggestionsBuilder builder) {

        HeadWarsMapManager.loadedMaps().keySet().forEach(map -> builder.suggest(map, new LiteralMessage("map ID")));
        return builder.buildFuture();

    }

}
