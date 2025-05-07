package win.codingboulder.headWars.util;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import win.codingboulder.headWars.maps.HeadWarsMap;
import win.codingboulder.headWars.maps.HeadWarsMapManager;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class HeadWarsMapArgument implements CustomArgumentType.Converted<HeadWarsMap, String> {

    private static final DynamicCommandExceptionType ERROR_NON_EXISTENT_MAP = new DynamicCommandExceptionType(map -> MessageComponentSerializer.message().serialize(Component.text("Map " + map + " doesn't exist or isn't loaded properly!", NamedTextColor.RED)));

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {

        HeadWarsMapManager.loadedMaps().forEach((map, obj) -> builder.suggest(map));
        return builder.buildFuture();

    }

    @Override
    public @NotNull HeadWarsMap convert(@NotNull String nativeType) throws CommandSyntaxException {

        HeadWarsMap map = HeadWarsMapManager.loadedMaps().get(nativeType);
        if (map == null) throw ERROR_NON_EXISTENT_MAP.create(nativeType);
        return map;

    }

}
