package win.codingboulder.headWars;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.game.GeneratorType;
import win.codingboulder.headWars.game.HeadWarsGame;
import win.codingboulder.headWars.game.HeadWarsGameManager;
import win.codingboulder.headWars.game.shop.ItemShop;
import win.codingboulder.headWars.game.shop.ShopConfigGUI;
import win.codingboulder.headWars.game.shop.ShopGui;
import win.codingboulder.headWars.maps.HeadWarsMap;
import win.codingboulder.headWars.maps.HeadWarsMapManager;
import win.codingboulder.headWars.maps.HeadWarsTeam;
import win.codingboulder.headWars.util.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static io.papermc.paper.command.brigadier.Commands.literal;
import static io.papermc.paper.command.brigadier.Commands.argument;

@SuppressWarnings("UnstableApiUsage")
public class HeadWarsCommand {

    public static void registerCommand(@NotNull LifecycleEventManager<@NotNull Plugin> lifecycleEventManager) {

        lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> event.registrar().register(

            literal("headwars")

                .then(literal("map")

                    .then(literal("create")
                        .then(argument("id", StringArgumentType.word())
                            .then(argument("name", StringArgumentType.string())
                                .then(argument("world", StringArgumentType.word())
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

                    .then(literal("edit")
                        .then(argument("map", new HeadWarsMapArgument())

                            .then(literal("name")
                                .then(argument("name", StringArgumentType.string())
                                    .executes(context -> {

                                        context.getArgument("map", HeadWarsMap.class).editAndSave(map -> map.setName(context.getArgument("name", String.class)));

                                        return 1;

                                    }))
                            )

                            .then(literal("world")
                                .then(argument("world", StringArgumentType.string())
                                    .executes(context -> {

                                        context.getArgument("map", HeadWarsMap.class).editAndSave(map -> map.setWorld(context.getArgument("world", String.class)));

                                        return 1;

                                    }))
                            )

                            .then(literal("players-per-team")
                                .then(argument("players-per-team", IntegerArgumentType.integer(1))
                                    .executes(context -> {

                                        context.getArgument("map", HeadWarsMap.class).editAndSave(map -> map.setPlayersPerTeam(context.getArgument("players-per-team", Integer.class)));

                                        return 1;

                                    }))
                            )

                            .then(literal("lobby-spawn")
                                .then(argument("position", ArgumentTypes.finePosition(true))
                                    .executes(context -> {

                                        SimpleFinePos pos = SimpleFinePos.fromCmdArgument("position", context);
                                        context.getArgument("map", HeadWarsMap.class).editAndSave(map -> map.lobbySpawn(pos));

                                        return 1;

                                    })))

                            .then(literal("add-team")
                                .then(argument("color", new ColorArgument())
                                    .executes(context -> {

                                        context.getArgument("map", HeadWarsMap.class).editAndSave(map -> map.createTeam(context.getArgument("color", DyeColor.class)));

                                        return 1;

                                    }))
                            )

                            .then(literal("edit-team")
                                .then(argument("team", new ColorArgument()).suggests(HeadWarsCommand::headWarsTeamSuggestion)

                                    .then(literal("color")
                                        .then(argument("color", new ColorArgument())
                                            .executes(context -> {

                                                HeadWarsMap map = context.getArgument("map", HeadWarsMap.class);
                                                HeadWarsTeam team = getTeam(context, map);
                                                DyeColor color = context.getArgument("color", DyeColor.class);

                                                map.teams().remove(team.getTeamColor());
                                                team.setTeamColor(color);
                                                map.teams().put(color, team);

                                                map.updateMapFile();

                                                return 1;

                                            }))
                                    )

                                    .then(literal("spawn-position")
                                        .then(argument("position", ArgumentTypes.finePosition())
                                            .executes(context -> {

                                                HeadWarsMap map = context.getArgument("map", HeadWarsMap.class);
                                                HeadWarsTeam team = getTeam(context, map);
                                                team.setSpawnPosition(SimpleFinePos.fromCmdArgument("position", context));
                                                map.updateMapFile();

                                                return 1;

                                            }))

                                    )

                                    .then(literal("base-area")
                                        .then(argument("corner1", ArgumentTypes.blockPosition())
                                            .then(argument("corner2", ArgumentTypes.blockPosition())
                                                .executes(context -> {

                                                    HeadWarsMap map = context.getArgument("map", HeadWarsMap.class);
                                                    HeadWarsTeam team = getTeam(context, map);
                                                    team.setBasePosition(
                                                        SimpleBlockPos.fromCmdArgument("corner1", context),
                                                        SimpleBlockPos.fromCmdArgument("corner2", context)
                                                    );
                                                    map.updateMapFile();

                                                    return 1;

                                                })))
                                    )

                                    .then(literal("base-perimeter")
                                        .then(argument("corner1", ArgumentTypes.blockPosition())
                                            .then(argument("corner2", ArgumentTypes.blockPosition())
                                                .executes(context -> {

                                                    HeadWarsMap map = context.getArgument("map", HeadWarsMap.class);
                                                    HeadWarsTeam team = getTeam(context, map);
                                                    team.setBasePerimeter(
                                                        SimpleBlockPos.fromCmdArgument("corner1", context),
                                                        SimpleBlockPos.fromCmdArgument("corner2", context)
                                                    );
                                                    map.updateMapFile();

                                                    return 1;

                                                })))
                                    )

                                    .then(literal("add-head")
                                        .then(argument("position", ArgumentTypes.blockPosition())
                                            .executes(context -> {

                                                HeadWarsMap map = context.getArgument("map", HeadWarsMap.class);
                                                HeadWarsTeam team = getTeam(context, map);
                                                team.heads().add(SimpleBlockPos.fromCmdArgument("position", context));
                                                map.updateMapFile();

                                                return 1;

                                            }))
                                    )

                                    .then(literal("remove-head")
                                        .then(argument("position", ArgumentTypes.blockPosition())
                                            .executes(context -> {

                                                HeadWarsMap map = context.getArgument("map", HeadWarsMap.class);
                                                HeadWarsTeam team = getTeam(context, map);
                                                team.heads().remove(SimpleBlockPos.fromCmdArgument("position", context));
                                                map.updateMapFile();

                                                return 1;

                                            }))
                                    )

                                )
                            )

                            .then(literal("add-click-generator")
                                .then(argument("button-position", ArgumentTypes.blockPosition())
                                    .then(argument("spawn-position", ArgumentTypes.finePosition(true))
                                        .executes(context -> {

                                            HeadWarsMap map = context.getArgument("map", HeadWarsMap.class);
                                            map.clickGenerators().add(Pair.of(
                                                SimpleBlockPos.fromCmdArgument("button-position", context),
                                                SimpleFinePos.fromCmdArgument("spawn-position", context)
                                            ));
                                            map.updateMapFile();

                                            return 1;

                                        })))
                            )

                            .then(literal("remove-click-generator")
                                .then(argument("button-position", ArgumentTypes.blockPosition())
                                    .executes(context -> {

                                        HeadWarsMap map = context.getArgument("map", HeadWarsMap.class);
                                        SimpleBlockPos blockPos = SimpleBlockPos.fromCmdArgument("button-position", context);

                                        map.clickGenerators().stream()
                                            .filter(entry -> entry.left().equals(blockPos))
                                            .findFirst().ifPresent(entry -> map.clickGenerators().remove(entry));

                                        map.updateMapFile();

                                        return 1;

                                    }))
                            )

                            .then(literal("add-shop")
                                .then(argument("entity", ArgumentTypes.entity())
                                    .then(argument("shop", StringArgumentType.word())
                                        .executes(context -> {

                                            Entity entity = context.getArgument("entity", EntitySelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                            context.getArgument("map", HeadWarsMap.class).editAndSave(map ->
                                                map.itemShops().put(entity.getUniqueId(), context.getArgument("shop", String.class))
                                            );

                                            return 1;

                                        })))
                            )

                            .then(literal("remove-shop")
                                .then(argument("entity", ArgumentTypes.entity())
                                    .executes(context -> {

                                        Entity entity = context.getArgument("entity", EntitySelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                        context.getArgument("map", HeadWarsMap.class).editAndSave(map -> map.itemShops().remove(entity.getUniqueId()));

                                        return 1;

                                    }))
                            )

                            .then(literal("add-emerald-generator")
                                .then(argument("position", ArgumentTypes.blockPosition())
                                    .executes(context -> {

                                        SimpleBlockPos blockPos = SimpleBlockPos.fromCmdArgument("position", context);
                                        context.getArgument("map", HeadWarsMap.class).editAndSave(map -> map.emeraldGenerators().add(blockPos));

                                        return 1;

                                    }))
                            )

                            .then(literal("remove-emerald-generator")
                                .then(argument("position", ArgumentTypes.blockPosition())
                                    .executes(context -> {

                                        SimpleBlockPos blockPos = SimpleBlockPos.fromCmdArgument("position", context);
                                        context.getArgument("map", HeadWarsMap.class).editAndSave(map -> map.emeraldGenerators().remove(blockPos));

                                        return 1;

                                    }))
                            )

                            .then(literal("add-protected-area")
                                .then(argument("pos1", ArgumentTypes.blockPosition())
                                    .then(argument("pos2", ArgumentTypes.blockPosition())
                                        .executes(context -> {

                                            SimpleBlockPos pos1 = SimpleBlockPos.fromCmdArgument("pos1", context);
                                            SimpleBlockPos pos2 = SimpleBlockPos.fromCmdArgument("pos2", context);
                                            context.getArgument("map", HeadWarsMap.class).editAndSave(map -> map.protectedAreas().add(Pair.of(pos1, pos2)));

                                            return 1;

                                        })))
                            )

                            .then(literal("add-protected-block")
                                .then(argument("position", ArgumentTypes.blockPosition())
                                    .executes(context -> {

                                        SimpleBlockPos blockPos = SimpleBlockPos.fromCmdArgument("position", context);
                                        context.getArgument("map", HeadWarsMap.class).editAndSave(map -> map.protectedBlocks().add(blockPos));

                                        return 1;

                                    }))
                            )

                            .then(literal("add-no-place-area")
                                .then(argument("pos1", ArgumentTypes.blockPosition())
                                    .then(argument("pos2", ArgumentTypes.blockPosition())
                                        .executes(context -> {

                                            SimpleBlockPos pos1 = SimpleBlockPos.fromCmdArgument("pos1", context);
                                            SimpleBlockPos pos2 = SimpleBlockPos.fromCmdArgument("pos2", context);
                                            context.getArgument("map", HeadWarsMap.class).editAndSave(map -> map.noPlaceAreas().add(Pair.of(pos1, pos2)));

                                            return 1;

                                        })))
                            )

                        )
                    )

                    .then(literal("delete")

                    )

                    .then(literal("reload-maps")
                        .executes(context -> {

                            HeadWars.getInstance().getServer().getScheduler().runTaskAsynchronously(HeadWars.getInstance(), task -> {
                                HeadWarsMapManager.loadAllMaps();
                                context.getSource().getSender().sendRichMessage("<green>Reloaded all maps!");
                            });

                            return 1;

                        })
                    )

                    .then(literal("teleport-to-world")
                        .then(argument("world", StringArgumentType.word())
                            .executes(context -> {

                                World world = HeadWars.getInstance().getServer().createWorld(WorldCreator.name(context.getArgument("world", String.class)));
                                Player player = (Player) context.getSource().getSender();
                                player.teleport(new Location(world, 0, 100, 0));

                                return 1;

                            }))
                    )

                )

                .then(literal("game")

                    .then(literal("start")
                        .then(argument("map", new HeadWarsMapArgument())
                            .executes(context -> {

                                HeadWarsGameManager.startGame(context.getArgument("map", HeadWarsMap.class));

                                return 1;

                            })
                        )
                    )

                    .then(literal("join")
                        .then(argument("game", StringArgumentType.word())
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

                    .then(literal("stop")
                        .then(argument("game", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                HeadWarsGameManager.activeGameNames.keySet().forEach(builder::suggest);
                                return builder.buildFuture();
                            })
                            .executes(context -> {

                                HeadWarsGame game = HeadWarsGameManager.activeGameNames.get(context.getArgument("game", String.class));
                                if (game == null) {
                                    context.getSource().getSender().sendRichMessage("<red>That game doesn't exist!");
                                    return 1;
                                }

                                game.handleGameStop();
                                context.getSource().getSender().sendRichMessage("<green>The game has been stopped");

                                return 1;

                            }))
                    )

                    .then(literal("admin")
                        .then(argument("game", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                HeadWarsGameManager.activeGameNames.keySet().forEach(builder::suggest);
                                return builder.buildFuture();
                            })

                            .then(literal("set-game-time")
                                .then(argument("time", IntegerArgumentType.integer(0))
                                    .executes(context -> {

                                        HeadWarsGame game = HeadWarsGameManager.activeGameNames.get(context.getArgument("game", String.class));
                                        if (game == null) {
                                            context.getSource().getSender().sendRichMessage("<red>That game doesn't exist!");
                                            return 1;
                                        }

                                        game.gameTimer = context.getArgument("time", Integer.class);

                                        return 1;

                                    })))))

                )

                .then(literal("config")

                    .then(literal("itemshops")

                        .then(literal("create")
                            .then(argument("id", StringArgumentType.word())
                                .then(argument("title", StringArgumentType.string())
                                    .then(argument("rows", IntegerArgumentType.integer(1, 6))
                                        .executes(context -> {

                                            ItemShop itemShop = ItemShop.createShop(
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

                        .then(literal("edit")
                            .then(argument("id", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    ItemShop.registeredItemShops.keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {

                                    ItemShop itemShop = ItemShop.registeredItemShops.get(context.getArgument("id", String.class));
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

                        .then(literal("open")
                            .then(argument("id", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    ItemShop.registeredItemShops.keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {

                                    if (!(context.getSource().getSender() instanceof Player player)) return 1;
                                    ItemShop itemShop = ItemShop.registeredItemShops.get(context.getArgument("id", String.class));
                                    if (itemShop == null) return 1;
                                    itemShop.openShop(player);

                                    return 1;
                                }))
                        )

                        .then(literal("edit-item")
                            .requires(css -> css.getSender() instanceof Player)

                            .then(literal("action")
                                .then(literal("buy")
                                    .executes(context -> {

                                        Player player = (Player) context.getSource().getSender();
                                        ItemStack item = player.getInventory().getItemInMainHand();
                                        ShopGui.setItemAction(item, "buy");

                                        return 1;
                                    })
                                )
                                .then(literal("open-menu")
                                    .then(argument("menu", StringArgumentType.word())
                                        .executes(context -> {

                                            Player player = (Player) context.getSource().getSender();
                                            ItemStack item = player.getInventory().getItemInMainHand();
                                            ShopGui.setItemAction(item, "open-menu");

                                            return 1;
                                        }))
                                )

                            )

                            .then(literal("price")
                                .then(literal("add")
                                    .then(literal("offhand")
                                        .executes(context -> {

                                            Player player = (Player) context.getSource().getSender();
                                            ItemStack item = player.getInventory().getItemInMainHand();
                                            ItemStack offhand = player.getInventory().getItemInOffHand();

                                            HashMap<ItemStack, Integer> price = ShopGui.getItemPrice(item);
                                            price.put(offhand, offhand.getAmount());
                                            ShopGui.setItemPrice(item, price);

                                            return 1;
                                        }))
                                    .then(argument("item", ArgumentTypes.itemStack())
                                        .then(argument("amount", IntegerArgumentType.integer(0))
                                            .executes(context -> {

                                                Player player = (Player) context.getSource().getSender();
                                                ItemStack item = player.getInventory().getItemInMainHand();

                                                HashMap<ItemStack, Integer> price = ShopGui.getItemPrice(item);
                                                price.put(
                                                    context.getArgument("item", ItemStack.class),
                                                    IntegerArgumentType.getInteger(context, "amount")
                                                );
                                                ShopGui.setItemPrice(item, price);

                                                return 1;
                                            })))
                                )

                            )

                            .then(literal("bought-item")
                                .then(literal("offhand")
                                    .executes(context -> {

                                        Player player = (Player) context.getSource().getSender();
                                        ItemStack item = player.getInventory().getItemInMainHand();
                                        ItemStack offhand = player.getInventory().getItemInOffHand();

                                        ShopGui.setItemBought(item, offhand);

                                        return 1;
                                    }))
                                .then(argument("item", ArgumentTypes.itemStack())
                                    .executes(context -> {

                                        Player player = (Player) context.getSource().getSender();
                                        ItemStack item = player.getInventory().getItemInMainHand();

                                        ShopGui.setItemBought(item, context.getArgument("item", ItemStack.class));

                                        return 1;
                                    }))
                            )

                            .then(literal("item-id")
                                .then(argument("id", StringArgumentType.word())
                                    .executes(context -> {

                                        Player player = (Player) context.getSource().getSender();
                                        ItemStack item = player.getInventory().getItemInMainHand();

                                        ShopGui.setItemId(item, StringArgumentType.getString(context, "id"));

                                        return 1;
                                    }))
                            )

                            .then(literal("opened-menu")
                                .then(argument("menu", StringArgumentType.word())
                                    .executes(context -> {

                                        Player player = (Player) context.getSource().getSender();
                                        ItemStack item = player.getInventory().getItemInMainHand();

                                        ShopGui.setItemMenu(item, StringArgumentType.getString(context, "menu"));

                                        return 1;
                                    }))
                            )

                            .then(literal("add-price-lore")
                                .executes(context -> {

                                    Player player = (Player) context.getSource().getSender();
                                    ItemStack item = player.getInventory().getItemInMainHand();

                                    HashMap<ItemStack, Integer> price = ShopGui.getItemPrice(item);

                                    ArrayList<Component> lore = new ArrayList<>();
                                    price.forEach((priceItem, priceAmount) -> lore.add(Util.getItemCostComponent(priceItem, priceAmount)));
                                    item.setData(DataComponentTypes.LORE, ItemLore.lore(lore));

                                    return 1;
                                }))

                        )

                    )

                    .then(literal("generators")

                        .then(literal("create")

                            .then(argument("id", StringArgumentType.word())
                                .then(argument("name", StringArgumentType.string())
                                    .then(argument("carpet-material", ArgumentTypes.blockState())
                                        .then(argument("upgradeable-by-players", BoolArgumentType.bool())
                                            .then(argument("item-limit", IntegerArgumentType.integer(1))
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

                        .then(literal("edit")

                            .then(argument("generator", StringArgumentType.word())
                                .suggests((context, builder) -> {
                                    GeneratorType.registeredTypes.keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })

                                .then(literal("set-tier")
                                    .then(argument("tier", IntegerArgumentType.integer(0))
                                        .then(argument("speed", IntegerArgumentType.integer(1))
                                            .then(argument("material", ArgumentTypes.blockState())
                                                .then(argument("upgrade-message", StringArgumentType.string())
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

                                                    .then(argument("upgrade-cost", ArgumentTypes.itemStack())
                                                        .then(argument("upgrade-cost-amount", IntegerArgumentType.integer(1))
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

                                .then(literal("set-generated-resource")

                                    .then(literal("hand")
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

                                    .then(argument("item", ArgumentTypes.itemStack())
                                        .then(argument("count", IntegerArgumentType.integer(1))
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

                        .then(literal("reload")
                            .executes(context -> {

                                GeneratorType.reloadGeneratorTypes();
                                context.getSource().getSender().sendRichMessage("<green>Reloaded generator types!");
                                return 1;

                            }))

                        .then(literal("set-item-gen")
                            .requires(commandSourceStack -> commandSourceStack.getSender() instanceof Player)
                            .then(argument("generator-id", StringArgumentType.word())
                                .executes(context -> {

                                    Player player = (Player) context.getSource().getSender();

                                    player.getInventory().getItemInMainHand().editPersistentDataContainer(pcd ->
                                        pcd.set(new NamespacedKey("headwars", "generator_id"), PersistentDataType.STRING, StringArgumentType.getString(context, "generator-id")));

                                    return 1;

                                })))

                    )

                    .then(literal("reload-config")
                        .executes(context -> {

                            HeadWars.getInstance().loadConfig();

                            return 1;

                        }))

                )

                .build()
        ));

    }

    public static @NotNull HeadWarsTeam getTeam(@NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        HeadWarsMap map = context.getArgument("map", HeadWarsMap.class);
        DyeColor teamColor = context.getArgument("team", DyeColor.class);
        HeadWarsTeam team = map.getTeam(teamColor);
        if (team == null) throw new DynamicCommandExceptionType(teamObj -> MessageComponentSerializer.message().serialize(
            Component.text("Team " + teamColor + " doesn't exist!", NamedTextColor.RED)
        )).create(teamColor);
        return team;

    }

    public static @NotNull HeadWarsTeam getTeam(@NotNull CommandContext<CommandSourceStack> context, @NotNull HeadWarsMap map) throws CommandSyntaxException {

        DyeColor teamColor = context.getArgument("team", DyeColor.class);
        HeadWarsTeam team = map.getTeam(teamColor);
        if (team == null) throw new DynamicCommandExceptionType(teamObj -> MessageComponentSerializer.message().serialize(
            Component.text("Team " + teamColor + " doesn't exist!", NamedTextColor.RED)
        )).create(teamColor);
        return team;

    }
    public static CompletableFuture<Suggestions> headWarsTeamSuggestion(@NotNull CommandContext<CommandSourceStack> context, @NotNull SuggestionsBuilder builder) {

        HeadWarsMap map = context.getArgument("map", HeadWarsMap.class);
        map.teams().forEach(((dyeColor, headWarsTeam) -> builder.suggest(dyeColor.toString())));
        return builder.buildFuture();

    }

}
