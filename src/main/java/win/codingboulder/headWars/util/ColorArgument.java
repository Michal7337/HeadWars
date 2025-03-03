package win.codingboulder.headWars.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage")
public class ColorArgument implements CustomArgumentType<DyeColor, String> {

    public @NotNull DyeColor parse(@NotNull StringReader reader) {
        return DyeColor.valueOf(reader.readUnquotedString());
    }

    public <S> @NotNull DyeColor parse(@NotNull StringReader reader, @NotNull S source) throws CommandSyntaxException {
        return CustomArgumentType.super.parse(reader, source);
    }

    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        Arrays.stream(DyeColor.values())
            .filter(entry -> entry.toString().toLowerCase().startsWith(builder.getRemainingLowerCase()))
            .forEach(dyeColor -> builder.suggest(dyeColor.toString().toUpperCase()));
        //for (DyeColor color : DyeColor.values()) builder.suggest(color.toString());
        return builder.buildFuture();
    }
}
