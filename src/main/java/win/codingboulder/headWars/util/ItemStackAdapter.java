package win.codingboulder.headWars.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ItemStackAdapter extends TypeAdapter<ItemStack> {

    @Override
    public void write(@NotNull JsonWriter jsonWriter, ItemStack itemStack) throws IOException {

        String encodedItem = Util.encodeItemStack(itemStack);
        jsonWriter.value(encodedItem);

    }

    @Override
    public ItemStack read(@NotNull JsonReader jsonReader) throws IOException {

        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }
        String base64 = jsonReader.nextString();
        return Util.decodeItemStack(base64);

    }

}
