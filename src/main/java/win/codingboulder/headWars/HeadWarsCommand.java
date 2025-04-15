package win.codingboulder.headWars;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.BoolArgumentType;
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
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.game.GeneratorType;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.HeadWarsGameManager;
import win.codingboulder.headWars.game.shop.ItemShop;
import win.codingboulder.headWars.game.shop.ShopConfigGUI;
import win.codingboulder.headWars.game.shop.ShopManager;
import win.codingboulder.headWars.maps.HeadWarsMap;
import win.codingboulder.headWars.maps.HeadWarsMapManager;
import win.codingboulder.headWars.maps.HeadWarsTeam;
import win.codingboulder.headWars.util.ColorArgument;
import win.codingboulder.headWars.util.Pair;
import win.codingboulder.headWars.util.SimpleBlockPos;
import win.codingboulder.headWars.util.SimpleFinePos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
                                                        .then(Commands.argument("teamColor", new ColorArgument())
                                                                .executes(context -> {

                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                    if (map == null) {
                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                        return 1;
                                                                    }

                                                                    map.createTeam(context.getArgument("teamColor", DyeColor.class));

                                                                    map.updateMapFile();

                                                                    return 1;

                                                                    })))

                                                .then(Commands.literal("editTeam")
                                                        .then(Commands.argument("teamToEdit", new ColorArgument())

                                                                .then(Commands.literal("spawnPosition")
                                                                        .then(Commands.argument("newSpawn", ArgumentTypes.finePosition(true))
                                                                                .executes(context -> {

                                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                                    if (map == null) {
                                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                                        return 1;
                                                                                    }

                                                                                    HeadWarsTeam team = map.getTeam(context.getArgument("teamToEdit", DyeColor.class));
                                                                                    if (team == null) {
                                                                                        context.getSource().getSender().sendRichMessage("<red>This team doesn't exist!");
                                                                                        return 1;
                                                                                    }

                                                                                    FinePositionResolver resolver = context.getArgument("newSpawn", FinePositionResolver.class);
                                                                                    team.setSpawnPosition(SimpleFinePos.pos(resolver.resolve(context.getSource())));

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

                                                                                            HeadWarsTeam team = map.getTeam(context.getArgument("teamToEdit", DyeColor.class));
                                                                                            if (team == null) {
                                                                                                context.getSource().getSender().sendRichMessage("<red>This team doesn't exist!");
                                                                                                return 1;
                                                                                            }

                                                                                            BlockPositionResolver positionResolver1 = context.getArgument("baseCorner1", BlockPositionResolver.class);
                                                                                            BlockPositionResolver positionResolver2 = context.getArgument("baseCorner2", BlockPositionResolver.class);

                                                                                            team.setBasePosition(SimpleBlockPos.pos(positionResolver1.resolve(context.getSource())), SimpleBlockPos.pos(positionResolver2.resolve(context.getSource())));

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

                                                                                            HeadWarsTeam team = map.getTeam(context.getArgument("teamToEdit", DyeColor.class));
                                                                                            if (team == null) {
                                                                                                context.getSource().getSender().sendRichMessage("<red>This team doesn't exist!");
                                                                                                return 1;
                                                                                            }

                                                                                            BlockPositionResolver positionResolver1 = context.getArgument("basePerimeterCorner1", BlockPositionResolver.class);
                                                                                            BlockPositionResolver positionResolver2 = context.getArgument("basePerimeterCorner2", BlockPositionResolver.class);

                                                                                            team.setBasePerimeter(SimpleBlockPos.pos(positionResolver1.resolve(context.getSource())), SimpleBlockPos.pos(positionResolver2.resolve(context.getSource())));

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

                                                                                    HeadWarsTeam team = map.getTeam(context.getArgument("teamToEdit", DyeColor.class));
                                                                                    if (team == null) {
                                                                                        context.getSource().getSender().sendRichMessage("<red>This team doesn't exist!");
                                                                                        return 1;
                                                                                    }

                                                                                    BlockPositionResolver positionResolver = context.getArgument("headToAdd", BlockPositionResolver.class);
                                                                                    team.heads().add(SimpleBlockPos.pos(positionResolver.resolve(context.getSource())));

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

                                                                                    HeadWarsTeam team = map.getTeam(context.getArgument("teamToEdit", DyeColor.class));
                                                                                    if (team == null) {
                                                                                        context.getSource().getSender().sendRichMessage("<red>This team doesn't exist!");
                                                                                        return 1;
                                                                                    }

                                                                                    BlockPositionResolver positionResolver = context.getArgument("headToAdd", BlockPositionResolver.class);
                                                                                    team.heads().remove(SimpleBlockPos.pos(positionResolver.resolve(context.getSource())));

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

                                                                            map.clickGenerators().add(Pair.of(
                                                                                SimpleBlockPos.fromCmdArgument("buttonPosition", context),
                                                                                SimpleFinePos.fromCmdArgument("itemSpawnPosition", context))
                                                                            );

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

                                                                    SimpleBlockPos blockPos = SimpleBlockPos.fromCmdArgument("generatorButtonPosition", context);

                                                                    map.clickGenerators().stream()
                                                                        .filter(entry -> entry.left().equals(blockPos))
                                                                        .findFirst().ifPresent(entry -> map.clickGenerators().remove(entry));

                                                                    map.updateMapFile();

                                                                    return 1;

                                                                })))

                                                .then(Commands.literal("addItemShop")
                                                    .then(Commands.argument("itemShopEntity", ArgumentTypes.entity())
                                                        .then(Commands.argument("itemShopID", StringArgumentType.word())
                                                            .executes(context -> {

                                                                HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                if (map == null) {
                                                                    context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                    return 1;
                                                                    }

                                                                    EntitySelectorArgumentResolver resolver = context.getArgument("itemShopEntity", EntitySelectorArgumentResolver.class);
                                                                    UUID shopUUID = resolver.resolve(context.getSource()).getFirst().getUniqueId();
                                                                    String shopId = context.getArgument("itemShopID", String.class);
                                                                    map.itemShops().put(shopUUID, shopId);

                                                                    map.updateMapFile();

                                                                    return 1;

                                                            }))))

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

                                                .then(Commands.literal("addEmeraldGenerator")
                                                        .then(Commands.argument("emeraldGeneratorPosition", ArgumentTypes.blockPosition())
                                                                .executes(context -> {

                                                                    HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                                    if (map == null) {
                                                                        context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                        return 1;
                                                                    }

                                                                    BlockPositionResolver positionResolver1 = context.getArgument("emeraldGeneratorPosition", BlockPositionResolver.class);
                                                                    map.emeraldGenerators().add(SimpleBlockPos.pos(positionResolver1.resolve(context.getSource())));

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
                                                                    map.emeraldGenerators().remove(SimpleBlockPos.pos(positionResolver1.resolve(context.getSource())));

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
                                                                    map.lobbySpawn(SimpleFinePos.pos(resolver.resolve(context.getSource())));

                                                                    map.updateMapFile();

                                                                    return 1;

                                                                })))

                                            .then(Commands.literal("addProtectedArea")
                                                .then(Commands.argument("pos1", ArgumentTypes.blockPosition())
                                                    .then(Commands.argument("pos2", ArgumentTypes.blockPosition())
                                                        .executes(context -> {

                                                            HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                            if (map == null) {
                                                                context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                return 1;
                                                            }

                                                            SimpleBlockPos pos1 = SimpleBlockPos.fromCmdArgument("pos1", context);
                                                            SimpleBlockPos pos2 = SimpleBlockPos.fromCmdArgument("pos2", context);

                                                            map.protectedAreas().add(Pair.of(pos1, pos2));

                                                            map.updateMapFile();

                                                            return 1;

                                                        }))))

                                            .then(Commands.literal("addProtectedBlock")
                                                .then(Commands.argument("block", ArgumentTypes.blockPosition())
                                                    .executes(context -> {

                                                        HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                        if (map == null) {
                                                            context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                            return 1;
                                                        }

                                                        map.protectedBlocks().add(SimpleBlockPos.fromCmdArgument("block", context));
                                                        map.updateMapFile();

                                                        return 1;

                                                    })))

                                            .then(Commands.literal("addNoPlaceArea")
                                                .then(Commands.argument("pos1", ArgumentTypes.blockPosition())
                                                    .then(Commands.argument("pos2", ArgumentTypes.blockPosition())
                                                        .executes(context -> {

                                                            HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("id", String.class));
                                                            if (map == null) {
                                                                context.getSource().getSender().sendRichMessage("<red>This map doesn't exist or isn't loaded/detected!");
                                                                return 1;
                                                            }

                                                            if (map.noPlaceAreas() == null) map.setNoPlaceAreas(new ArrayList<>());
                                                            map.noPlaceAreas().add(Pair.of(
                                                                SimpleBlockPos.fromCmdArgument("pos1", context),
                                                                SimpleBlockPos.fromCmdArgument("pos2", context)
                                                            ));

                                                            map.updateMapFile();

                                                            return 1;

                                                        }))))

                                        )

                                )

                                .then(Commands.literal("delete")

                                )

                            .then(Commands.literal("reload-maps")
                                .executes(context -> {

                                    HeadWars.getInstance().getServer().getScheduler().runTaskAsynchronously(HeadWars.getInstance(), task -> {
                                        HeadWarsMapManager.loadAllMaps();
                                        context.getSource().getSender().sendRichMessage("<green>Reloaded all maps!");
                                    });

                                    return 1;

                                })
                            )

                            .then(Commands.literal("global-config")

                                .then(Commands.literal("itemshops")

                                    .then(Commands.literal("create")
                                        .then(Commands.argument("id", StringArgumentType.word())
                                            .then(Commands.argument("title", StringArgumentType.string())
                                                .then(Commands.argument("rows", IntegerArgumentType.integer(1, 6))
                                                    .executes(context -> {

                                                        ItemShop itemShop = ShopManager.createShop(
                                                            context.getArgument("id", String.class),
                                                            context.getArgument("title", String.class),
                                                            context.getArgument("rows", Integer.class)
                                                        );

                                                        context.getSource().getSender().sendRichMessage("<green>Created a new item shop!");
                                                        context.getSource().getSender().sendRichMessage("<green>Do /headwars map global-config itemshops edit <aqua>" + itemShop.id() + " <green>to edit the shop");

                                                        return 1;
                                                    })
                                                )
                                            )
                                        )
                                    )
                                    .then(Commands.literal("edit")
                                        .then(Commands.argument("id", StringArgumentType.word())
                                            .suggests((context, builder) -> {
                                                ShopManager.itemShops.keySet().forEach(builder::suggest);
                                                return builder.buildFuture();
                                            })
                                            .executes(context -> {

                                                ItemShop itemShop = ShopManager.itemShops.get(context.getArgument("id", String.class));
                                                if (itemShop == null) {
                                                    context.getSource().getSender().sendRichMessage("<red>That shop doesn't exist!");
                                                    return 1;
                                                }
                                                if (!(context.getSource().getSender() instanceof Player player)) {
                                                    context.getSource().getSender().sendRichMessage("<red>You must be a player to execute this!");
                                                    return 1;
                                                }
                                                //itemShop.openConfigGui(player);
                                                player.openInventory(new ShopConfigGUI(itemShop).getInventory());

                                                return 1;
                                            }))
                                    )
                                    .then(Commands.literal("open")
                                        .then(Commands.argument("id", StringArgumentType.word())
                                            .suggests((context, builder) -> {
                                                ShopManager.itemShops.keySet().forEach(builder::suggest);
                                                return builder.buildFuture();
                                            })
                                            .executes(context -> {

                                                if (!(context.getSource().getSender() instanceof Player player)) return 1;
                                                ItemShop itemShop = ShopManager.itemShops.get(context.getArgument("id", String.class));
                                                if (itemShop == null) return 1;
                                                itemShop.openShop(player);

                                                return 1;
                                            }))
                                    )

                                    .then(Commands.literal("edit-item")
                                        .then(Commands.literal("action")
                                            .then(Commands.literal("buy")
                                                .then(Commands.argument("price", ArgumentTypes.itemStack())
                                                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(context -> {

                                                            if (!(context.getSource().getSender() instanceof Player player)) {
                                                                context.getSource().getSender().sendRichMessage("<red>You must be a player to execute this!");
                                                                return 1;
                                                            }

                                                            ItemStack itemToEdit = player.getInventory().getItem(EquipmentSlot.HAND);
                                                            ItemStack price = context.getArgument("price", ItemStack.class);
                                                            price.setAmount(context.getArgument("amount", Integer.class));

                                                            ItemStack item = player.getInventory().getItem(EquipmentSlot.OFF_HAND);

                                                            itemToEdit.editPersistentDataContainer(pdc -> {
                                                                pdc.set(new NamespacedKey("headwars", "shopaction"), PersistentDataType.STRING, "buy");
                                                                pdc.set(new NamespacedKey("headwars", "shopprice"), PersistentDataType.BYTE_ARRAY, price.serializeAsBytes());
                                                                pdc.set(new NamespacedKey("headwars", "shopitem"), PersistentDataType.BYTE_ARRAY, item.serializeAsBytes());
                                                            });

                                                            return 1;
                                                        })
                                                        .then(Commands.argument("item-id", StringArgumentType.word())
                                                            .executes(context -> {

                                                                if (!(context.getSource().getSender() instanceof Player player)) {
                                                                    context.getSource().getSender().sendRichMessage("<red>You must be a player to execute this!");
                                                                    return 1;
                                                                }

                                                                ItemStack itemToEdit = player.getInventory().getItem(EquipmentSlot.HAND);
                                                                ItemStack price = context.getArgument("price", ItemStack.class);
                                                                price.setAmount(context.getArgument("amount", Integer.class));


                                                                itemToEdit.editPersistentDataContainer(pdc -> {
                                                                    pdc.set(new NamespacedKey("headwars", "shopaction"), PersistentDataType.STRING, "buy");
                                                                    pdc.set(new NamespacedKey("headwars", "shopprice"), PersistentDataType.BYTE_ARRAY, price.serializeAsBytes());
                                                                    pdc.set(new NamespacedKey("headwars", "itemid"), PersistentDataType.STRING, context.getArgument("item-id", String.class));
                                                                });

                                                                return 1;
                                                            })
                                                        )
                                                    )
                                                )
                                            )
                                            .then(Commands.literal("open-menu")
                                                .then(Commands.argument("menu", StringArgumentType.word())
                                                    .executes(context -> {

                                                        if (!(context.getSource().getSender() instanceof Player player)) {
                                                            context.getSource().getSender().sendRichMessage("<red>You must be a player to execute this!");
                                                            return 1;
                                                        }

                                                        ItemStack item = player.getInventory().getItem(EquipmentSlot.HAND);

                                                        item.editPersistentDataContainer(pdc -> {
                                                            pdc.set(new NamespacedKey("headwars", "shopaction"), PersistentDataType.STRING, "open-menu");
                                                            pdc.set(new NamespacedKey("headwars", "menu"), PersistentDataType.STRING, context.getArgument("menu", String.class));
                                                        });

                                                        return 1;
                                                    })
                                                )
                                            )
                                        )

                                        .then(Commands.literal("custom-id")
                                            .then(Commands.argument("id", StringArgumentType.word())
                                                .executes(context -> {

                                                    if (!(context.getSource().getSender() instanceof Player player)) return 1;

                                                    player.getInventory().getItemInMainHand().editPersistentDataContainer(pdc ->
                                                        pdc.set(new NamespacedKey("headwars", "itemid"), PersistentDataType.STRING, context.getArgument("id", String.class)));

                                                    player.sendRichMessage("<green>Custom item id set");

                                                    return 1;

                                                })
                                            )
                                        )

                                    )

                                )

                                .then(Commands.literal("generators")

                                    .then(Commands.literal("create")

                                        .then(Commands.argument("id", StringArgumentType.word())
                                            .then(Commands.argument("name", StringArgumentType.string())
                                                .then(Commands.argument("carpet-material", ArgumentTypes.blockState())
                                                    .then(Commands.argument("upgradeable-by-players", BoolArgumentType.bool())
                                                        .then(Commands.argument("item-limit", IntegerArgumentType.integer(1))
                                                            .executes(context -> {

                                                                GeneratorType generatorType = new GeneratorType(
                                                                    StringArgumentType.getString(context, "id"),
                                                                    StringArgumentType.getString(context, "name"),
                                                                    context.getArgument("carpet-material", BlockState.class).getType(),
                                                                    BoolArgumentType.getBool(context, "upgradeable-by-players"),
                                                                    IntegerArgumentType.getInteger(context, "item-limit"),
                                                                    new HashMap<>(), // tiers
                                                                    new byte[0] // resource
                                                                );

                                                                generatorType.saveGeneratorType();
                                                                GeneratorType.reloadGeneratorTypes();

                                                                context.getSource().getSender().sendRichMessage("<green>Created new generator type");

                                                                return 1;

                                                            }))))))
                                    )

                                    .then(Commands.literal("edit")

                                        .then(Commands.argument("generator", StringArgumentType.word())
                                            .suggests((context, builder) -> {
                                                GeneratorType.registeredTypes.keySet().forEach(builder::suggest);
                                                return builder.buildFuture();
                                            })

                                            .then(Commands.literal("set-tier")
                                                .then(Commands.argument("tier", IntegerArgumentType.integer(0))
                                                    .then(Commands.argument("speed", IntegerArgumentType.integer(1))
                                                        .then(Commands.argument("material", ArgumentTypes.blockState())
                                                            .then(Commands.argument("upgrade-message", StringArgumentType.string())
                                                                .executes(context -> {

                                                                    GeneratorType type = GeneratorType.registeredTypes.get(StringArgumentType.getString(context, "generator"));
                                                                    if (type == null) {
                                                                        context.getSource().getSender().sendRichMessage("<red>That generator type doesn't exist!");
                                                                        return 1;
                                                                    }

                                                                    byte[] upgradeCost;

                                                                    if (context.getSource().getSender() instanceof Player player) {
                                                                        if (player.getInventory().getItemInMainHand().isEmpty()) {
                                                                            player.sendRichMessage("<yellow>You aren't holding anything so the generator upgrade is free!");
                                                                            upgradeCost = new byte[0];
                                                                        } else {
                                                                            upgradeCost = player.getInventory().getItemInMainHand().serializeAsBytes();
                                                                            player.sendRichMessage("<yellow>Upgrade cost set to held item");
                                                                        }
                                                                    } else upgradeCost = new byte[0];

                                                                    int tierNum = IntegerArgumentType.getInteger(context, "tier");

                                                                    GeneratorType.GeneratorTier tier = new GeneratorType.GeneratorTier(
                                                                        tierNum,
                                                                        IntegerArgumentType.getInteger(context, "speed"),
                                                                        context.getArgument("material", BlockState.class).getType(),
                                                                        StringArgumentType.getString(context, "upgrade-message"),
                                                                        upgradeCost
                                                                    );

                                                                    type.tiers().put(tierNum, tier);
                                                                    type.saveGeneratorType();
                                                                    GeneratorType.reloadGeneratorTypes();

                                                                    context.getSource().getSender().sendRichMessage("<green>Added a new tier to the generator type");

                                                                    return 1;

                                                                })

                                                                .then(Commands.argument("upgrade-cost", ArgumentTypes.itemStack())
                                                                    .then(Commands.argument("upgrade-cost-amount", IntegerArgumentType.integer(1))
                                                                        .executes(context -> {

                                                                            GeneratorType type = GeneratorType.registeredTypes.get(StringArgumentType.getString(context, "generator"));
                                                                            if (type == null) {
                                                                                context.getSource().getSender().sendRichMessage("<red>That generator type doesn't exist!");
                                                                                return 1;
                                                                            }

                                                                            byte[] upgradeCost = context.getArgument("upgrade-cost", ItemStack.class)
                                                                                .asQuantity(IntegerArgumentType.getInteger(context, "upgrade-cost-amount"))
                                                                                .serializeAsBytes();

                                                                            int tierNum = IntegerArgumentType.getInteger(context, "tier");

                                                                            GeneratorType.GeneratorTier tier = new GeneratorType.GeneratorTier(
                                                                                tierNum,
                                                                                IntegerArgumentType.getInteger(context, "speed"),
                                                                                context.getArgument("material", BlockState.class).getType(),
                                                                                StringArgumentType.getString(context, "upgrade-message"),
                                                                                upgradeCost
                                                                            );

                                                                            type.tiers().put(tierNum, tier);
                                                                            type.saveGeneratorType();
                                                                            GeneratorType.reloadGeneratorTypes();

                                                                            context.getSource().getSender().sendRichMessage("<green>Added a new tier to the generator type");

                                                                            return 1;

                                                                        })))

                                                            ))))
                                            )

                                            .then(Commands.literal("set-generated-resource")

                                                .then(Commands.literal("hand")
                                                    .requires(commandSourceStack -> commandSourceStack.getSender() instanceof Player)
                                                    .executes(context -> {

                                                        GeneratorType type = GeneratorType.registeredTypes.get(StringArgumentType.getString(context, "generator"));
                                                        if (type == null) {
                                                            context.getSource().getSender().sendRichMessage("<red>That generator type doesn't exist!");
                                                            return 1;
                                                        }

                                                        Player player = (Player) context.getSource().getSender();
                                                        ItemStack item;
                                                        if (player.getInventory().getItemInMainHand().isEmpty()) {
                                                            player.sendRichMessage("<yellow>You aren't holding anything. The generator will not generate anything!");
                                                            item = ItemStack.empty();
                                                        } else item = player.getInventory().getItemInMainHand();

                                                        type.setResource(item);
                                                        type.saveGeneratorType();

                                                        player.sendRichMessage("<green>Set generated resource");

                                                        return 1;
                                                    }))

                                                .then(Commands.argument("item", ArgumentTypes.itemStack())
                                                    .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                                        .executes(context -> {

                                                            GeneratorType type = GeneratorType.registeredTypes.get(StringArgumentType.getString(context, "generator"));
                                                            if (type == null) {
                                                                context.getSource().getSender().sendRichMessage("<red>That generator type doesn't exist!");
                                                                return 1;
                                                            }

                                                            ItemStack itemStack = context.getArgument("item", ItemStack.class);
                                                            itemStack.setAmount(IntegerArgumentType.getInteger(context, "count"));

                                                            type.setResource(itemStack);
                                                            type.saveGeneratorType();

                                                            context.getSource().getSender().sendRichMessage("<green>Set generated resource");

                                                            return 1;

                                                        }))
                                                )

                                            )

                                        )
                                    )

                                    .then(Commands.literal("reload")
                                        .executes(context -> {

                                            GeneratorType.reloadGeneratorTypes();
                                            context.getSource().getSender().sendRichMessage("<green>Reloaded generator types!");
                                            return 1;

                                        }))

                                    .then(Commands.literal("set-item-gen")
                                        .requires(commandSourceStack -> commandSourceStack.getSender() instanceof Player)
                                        .then(Commands.argument("generator-id", StringArgumentType.word())
                                            .executes(context -> {

                                                Player player = (Player) context.getSource().getSender();

                                                player.getInventory().getItemInMainHand().editPersistentDataContainer(pcd ->
                                                    pcd.set(new NamespacedKey("headwars", "generator_id"), PersistentDataType.STRING, StringArgumentType.getString(context, "generator-id")));

                                                return 1;

                                            })))

                                )

                            )

                            .then(Commands.literal("teleport-to-world")
                                .then(Commands.argument("teleportMapWorld", StringArgumentType.word())
                                    .executes(context -> {

                                        World world = HeadWars.getInstance().getServer().createWorld(WorldCreator.name(context.getArgument("teleportMapWorld", String.class)));
                                        Player player = (Player) context.getSource().getSender();
                                        player.teleport(new Location(world, 0, 100, 0));

                                        return 1;

                                        }
                                    )
                                )
                            )

                        )

                        .then(Commands.literal("game")

                            .then(Commands.literal("start")
                                .then(Commands.argument("map", StringArgumentType.word()).suggests(HeadWarsCommand::headWarsMapSuggestion)
                                    .executes(context -> {

                                        HeadWarsMap map = HeadWarsMapManager.getMap(context.getArgument("map", String.class));
                                        if (map == null) {
                                            context.getSource().getSender().sendRichMessage("<red>That map doesn't exist or isn't loaded!");
                                            return 1;
                                        }

                                        HeadWarsGameManager.startGame(map);

                                        return 1;

                                    })
                                )
                            )
                            .then(Commands.literal("join")
                                .then(Commands.argument("game", StringArgumentType.word())
                                    .suggests((context, builder) -> {
                                        HeadWarsGameManager.activeGameNames.keySet().forEach(builder::suggest);
                                        return builder.buildFuture();
                                    })
                                    .executes(context -> {

                                        if (!(context.getSource().getSender() instanceof Player player)) {
                                            context.getSource().getSender().sendRichMessage("<red>You must be a player to execute this!");
                                            return 1;
                                        }

                                        HeadWarsGame game = HeadWarsGameManager.activeGameNames.get(context.getArgument("game", String.class));
                                        if (game == null) {
                                            player.sendRichMessage("<red>That game doesn't exist!");
                                            return 1;
                                        }

                                        if (game.isStarted()) {
                                            player.sendRichMessage("<red>That game has already started!");
                                            return 1;
                                        }

                                        player.sendRichMessage("<gray>Joining game...");
                                        game.addPlayer(player);
                                        player.sendRichMessage("<green>You have joined the game!");

                                        return 1;

                                    })
                                )
                            )

                        )



                        .build()
        ));

    }

    public static CompletableFuture<Suggestions> headWarsMapSuggestion(@NotNull CommandContext<CommandSourceStack> context, @NotNull SuggestionsBuilder builder) {

        HeadWarsMapManager.loadedMaps().keySet().forEach(map -> builder.suggest(map, new LiteralMessage("map ID")));
        return builder.buildFuture();

    }

}
