package win.codingboulder.headWars.game.shop;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import win.codingboulder.headWars.HeadWars;

import java.io.*;
import java.util.ArrayList;

public class ItemShop implements Serializable {

    private String id;
    private String name;
    private int size;

    private ArrayList<ItemStack> items;

    public ItemShop(String id, String name, int size) {

        this.id = id;
        this.name = name;
        if (size < 9) size = 9;
        this.size = size;
        this.items = new ArrayList<>(size);

    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {

        ArrayList<byte[]> itemStacks = new ArrayList<>();
        for (ItemStack item : items) itemStacks.add(item.serializeAsBytes());

        out.writeObject(id);
        out.writeObject(name);
        out.write(size);
        out.writeObject(itemStacks);

    }

    @Serial
    private void readObject(@NotNull ObjectInputStream in) throws IOException, ClassNotFoundException {

        id = (String) in.readObject();
        name = (String) in.readObject();
        size = in.read();
        items = new ArrayList<>();

        @SuppressWarnings("unchecked") ArrayList<byte[]> itemStacks = (ArrayList<byte[]>) in.readObject();
        itemStacks.forEach(item -> items.add(ItemStack.deserializeBytes(item)));

    }

    public void storeToFile() {

        try { ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(HeadWars.getShopsFolder(), id + ".itemshop")));

            oos.writeObject(this);
            oos.flush();
            oos.close();

        } catch (IOException e) { throw new RuntimeException(e); }

    }

    public static @Nullable ItemShop readFromFile(String id) {

        try {ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(HeadWars.getShopsFolder(), id + ".itemshop")));

            ItemShop itemShop = (ItemShop) ois.readObject();
            ois.close();
            return itemShop;

        } catch (IOException | ClassNotFoundException e) {HeadWars.getInstance().getLogger().warning(e.getMessage()); return null;}

    }

    public String id() {
        return id;
    }

    public void id(String id) {
        this.id = id;
    }

    public String name() {
        return name;
    }

    public void name(String name) {
        this.name = name;
    }

    public int size() {
        return size;
    }

    public void size(int size) {
        this.size = size;
    }

    public ArrayList<ItemStack> items() {
        return items;
    }

}
