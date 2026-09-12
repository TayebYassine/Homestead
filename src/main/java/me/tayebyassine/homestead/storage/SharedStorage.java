package me.tayebyassine.homestead.storage;

import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Thread-safe, in-memory representation of a region's shared storage.
 *
 * <p>Items are stored by slot index and serialized to a Base64-encoded
 * string for persistence. All public methods are synchronized on an
 * internal lock to allow safe concurrent access.
 * </p>
 */
public final class SharedStorage {

    private final long regionId;
    private final int size;
    private final Map<Integer, ItemStack> items;
    private final Object lock;

    /**
     * Create a new storage with the given capacity.
     *
     * @param regionId the unique region ID that owns this storage
     * @param size     the inventory size (must be a valid Bukkit size)
     */
    public SharedStorage(long regionId, int size) {
        this.regionId = regionId;
        this.size = size;
        this.items = new HashMap<>();
        this.lock = new Object();
    }

    /**
     * Deserialise a storage from its Base64-encoded representation.
     *
     * @param regionId the unique region ID
     * @param data     the Base64-encoded storage data
     * @return the deserialised storage, or an empty storage if the data is corrupt
     */
    public static SharedStorage deserialize(long regionId, String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            DataInputStream dataInput = new DataInputStream(inputStream);
            int size = dataInput.readInt();
            int itemCount = dataInput.readInt();
            if (size <= 0 || size > 1080 || itemCount < 0 || itemCount > 1080) {
                throw new IllegalArgumentException("Invalid storage size or item count");
            }
            SharedStorage storage = new SharedStorage(regionId, size);
            for (int i = 0; i < itemCount; i++) {
                int slot = dataInput.readInt();
                int itemBytesLen = dataInput.readInt();
                if (itemBytesLen <= 0 || itemBytesLen > 1000000) continue;
                byte[] itemBytes = new byte[itemBytesLen];
                dataInput.readFully(itemBytes);
                try {
                    ItemStack item = ItemStack.deserializeBytes(itemBytes);
                    if (item != null && !item.getType().isAir()) {
                        storage.items.put(slot, item);
                    }
                } catch (Exception ignored) {
                }
            }
            dataInput.close();
            return storage;
        } catch (IOException | IllegalArgumentException e) {
            return createEmpty(regionId);
        }
    }

    /**
     * Create an empty storage with the default size of 54 slots.
     *
     * @param regionId the unique region ID
     * @return a new empty storage
     */
    public static SharedStorage createEmpty(long regionId) {
        return new SharedStorage(regionId, 54);
    }

    /**
     * Serialize the storage to a Base64-encoded string for persistence.
     *
     * @return the encoded storage data
     */
    public String serialize() {
        synchronized (lock) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                DataOutputStream dataOutput = new DataOutputStream(outputStream);
                dataOutput.writeInt(size);
                dataOutput.writeInt(items.size());
                for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                    dataOutput.writeInt(entry.getKey());
                    byte[] itemBytes = entry.getValue().serializeAsBytes();
                    dataOutput.writeInt(itemBytes.length);
                    dataOutput.write(itemBytes);
                }
                dataOutput.close();
                return Base64.getEncoder().encodeToString(outputStream.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize storage", e);
            }
        }
    }

    /**
     * Get the unique region ID that owns this storage.
     *
     * @return the region ID
     */
    public long getRegionId() {
        return regionId;
    }

    /**
     * Get the inventory size of this storage.
     *
     * @return the number of slots
     */
    public int getSize() {
        return size;
    }

    /**
     * Get a clone of the item in the given slot.
     *
     * @param slot the slot index
     * @return a clone of the item, or {@code null} if the slot is empty
     */
    public ItemStack getItem(int slot) {
        synchronized (lock) {
            ItemStack item = items.get(slot);
            return item != null ? item.clone() : null;
        }
    }

    /**
     * Get a snapshot of all items in this storage.
     *
     * @return a map of slot index to cloned item
     */
    public Map<Integer, ItemStack> getAllItems() {
        synchronized (lock) {
            Map<Integer, ItemStack> copy = new HashMap<>();
            for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                copy.put(entry.getKey(), entry.getValue().clone());
            }
            return copy;
        }
    }

    /**
     * Set the item in the given slot, replacing any existing item. Air or
     * {@code null} items are treated as removals.
     *
     * @param slot the slot index
     * @param item the item to place, or {@code null} to clear
     */
    public void setItem(int slot, ItemStack item) {
        synchronized (lock) {
            if (item == null || item.getType().isAir()) {
                items.remove(slot);
            } else {
                items.put(slot, item.clone());
            }
        }
    }

    /**
     * Take (remove and return) the item in the given slot.
     *
     * @param slot the slot index
     * @return a clone of the removed item, or {@code null} if the slot was empty
     */
    public ItemStack takeItem(int slot) {
        synchronized (lock) {
            ItemStack item = items.remove(slot);
            return item != null ? item.clone() : null;
        }
    }

    /**
     * Place an item into the given slot, removing it if air or
     * {@code null}.
     *
     * @param slot the slot index
     * @param item the item to place, or {@code null} to clear
     */
    public void placeItem(int slot, ItemStack item) {
        synchronized (lock) {
            if (item == null || item.getType().isAir()) {
                items.remove(slot);
            } else {
                items.put(slot, item.clone());
            }
        }
    }

    /**
     * Atomically swap the item in the given slot with a new item.
     *
     * @param slot    the slot index
     * @param newItem the item to place, or {@code null} to clear
     * @return a clone of the previous item, or {@code null} if the slot was empty
     */
    public ItemStack swapItem(int slot, ItemStack newItem) {
        synchronized (lock) {
            ItemStack old = items.get(slot);
            if (newItem == null || newItem.getType().isAir()) {
                items.remove(slot);
            } else {
                items.put(slot, newItem.clone());
            }
            return old != null ? old.clone() : null;
        }
    }
}
