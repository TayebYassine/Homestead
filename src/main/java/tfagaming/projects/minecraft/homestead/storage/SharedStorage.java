package tfagaming.projects.minecraft.homestead.storage;

import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SharedStorage {
	private final long regionId;
	private final int size;
	private final Map<Integer, ItemStack> items;
	private final Object lock;

	public SharedStorage(long regionId, int size) {
		this.regionId = regionId;
		this.size = size;
		this.items = new HashMap<>();
		this.lock = new Object();
	}

	public static SharedStorage deserialize(long regionId, String data) {
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
			DataInputStream dataInput = new DataInputStream(inputStream);
			int size = dataInput.readInt();
			int itemCount = dataInput.readInt();
			SharedStorage storage = new SharedStorage(regionId, size);
			for (int i = 0; i < itemCount; i++) {
				int slot = dataInput.readInt();
				byte[] itemBytes = new byte[dataInput.readInt()];
				dataInput.readFully(itemBytes);
				storage.items.put(slot, ItemStack.deserializeBytes(itemBytes));
			}
			dataInput.close();
			return storage;
		} catch (IOException e) {
			throw new RuntimeException("Failed to deserialize storage", e);
		}
	}

	public static SharedStorage createEmpty(long regionId) {
		return new SharedStorage(regionId, 54);
	}

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

	public long getRegionId() {
		return regionId;
	}

	public int getSize() {
		return size;
	}

	public ItemStack getItem(int slot) {
		synchronized (lock) {
			ItemStack item = items.get(slot);
			return item != null ? item.clone() : null;
		}
	}

	public Map<Integer, ItemStack> getAllItems() {
		synchronized (lock) {
			Map<Integer, ItemStack> copy = new HashMap<>();
			for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
				copy.put(entry.getKey(), entry.getValue().clone());
			}
			return copy;
		}
	}

	public void setItem(int slot, ItemStack item) {
		synchronized (lock) {
			if (item == null || item.getType().isAir()) {
				items.remove(slot);
			} else {
				items.put(slot, item.clone());
			}
		}
	}

	public ItemStack takeItem(int slot) {
		synchronized (lock) {
			ItemStack item = items.remove(slot);
			return item != null ? item.clone() : null;
		}
	}

	public void placeItem(int slot, ItemStack item) {
		synchronized (lock) {
			if (item == null || item.getType().isAir()) {
				items.remove(slot);
			} else {
				items.put(slot, item.clone());
			}
		}
	}

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