package tfagaming.projects.minecraft.homestead.database.providers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import tfagaming.projects.minecraft.homestead.structure.Level;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.SubArea;
import tfagaming.projects.minecraft.homestead.structure.War;
import tfagaming.projects.minecraft.homestead.structure.serializable.*;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;

import java.util.*;
import java.util.stream.Collectors;

public final class MongoDB implements Provider {
	private static final ReplaceOptions UPSERT = new ReplaceOptions().upsert(true);
	private final MongoClient mongoClient;
	private final MongoDatabase mongoDatabase;
	private final String COLLECTION_PREFIX;

	public MongoDB(String uri, String database, String collectionPrefix) {
		COLLECTION_PREFIX = collectionPrefix.replaceAll("[^A-Za-z0-9_]", "");

		this.mongoClient = MongoClients.create(uri);
		this.mongoDatabase = mongoClient.getDatabase(database);
	}

	private static String str(Document doc, String key) {
		Object v = doc.get(key);
		return v != null ? v.toString() : null;
	}

	private static World resolveWorld(String value) {
		if (value == null || value.isBlank()) return null;
		try {
			return Bukkit.getWorld(UUID.fromString(value.trim()));
		} catch (IllegalArgumentException ignored) {
			return Bukkit.getWorld(value.trim());
		}
	}

	private MongoCollection<Document> regions() {
		return mongoDatabase.getCollection(COLLECTION_PREFIX + "regions");
	}

	private MongoCollection<Document> wars() {
		return mongoDatabase.getCollection(COLLECTION_PREFIX + "wars");
	}

	private MongoCollection<Document> subareas() {
		return mongoDatabase.getCollection(COLLECTION_PREFIX + "subareas");
	}

	private MongoCollection<Document> levels() {
		return mongoDatabase.getCollection(COLLECTION_PREFIX + "levels");
	}

	@Override
	public List<Region> importRegions() throws Exception {
		List<Region> regions = new ArrayList<>();

		for (Document doc : regions().find()) {
			UUID id = UUID.fromString(doc.getString("id"));
			String displayName = doc.getString("displayName");
			String name = doc.getString("name");
			String description = doc.getString("description");
			OfflinePlayer owner = tfagaming.projects.minecraft.homestead.Homestead.getInstance()
					.getOfflinePlayerSync(UUID.fromString(doc.getString("ownerId")));

			if (owner == null) continue;

			SerializableLocation location = SerializableLocation.fromString(str(doc, "location"));
			long createdAt = doc.getLong("createdAt");
			long playerFlags = doc.getLong("playerFlags");
			long worldFlags = doc.getLong("worldFlags");
			double bank = doc.getDouble("bank");
			int mapColor = doc.getInteger("mapColor");

			String chunksRaw = doc.getString("chunks");
			List<SerializableChunk> chunks = (chunksRaw != null && !chunksRaw.isEmpty())
					? Arrays.stream(chunksRaw.split("§")).map(SerializableChunk::fromString).collect(Collectors.toList())
					: new ArrayList<>();

			String membersRaw = doc.getString("members");
			List<SerializableMember> members = (membersRaw != null && !membersRaw.isEmpty())
					? Arrays.stream(membersRaw.split("§")).map(SerializableMember::fromString).collect(Collectors.toList())
					: new ArrayList<>();

			String ratesRaw = doc.getString("rates");
			List<SerializableRate> rates = (ratesRaw != null && !ratesRaw.isEmpty())
					? Arrays.stream(ratesRaw.split("§")).map(SerializableRate::fromString).collect(Collectors.toList())
					: new ArrayList<>();

			String invitedRaw = doc.getString("invitedPlayers");
			List<OfflinePlayer> invitedPlayers = (invitedRaw != null && !invitedRaw.isEmpty())
					? Arrays.stream(invitedRaw.split("§"))
					.map(u -> tfagaming.projects.minecraft.homestead.Homestead.getInstance().getOfflinePlayerSync(UUID.fromString(u)))
					.collect(Collectors.toList())
					: new ArrayList<>();

			String bannedRaw = doc.getString("bannedPlayers");
			List<SerializableBannedPlayer> bannedPlayers = (bannedRaw != null && !bannedRaw.isEmpty())
					? Arrays.stream(bannedRaw.split("§")).map(SerializableBannedPlayer::fromString).collect(Collectors.toList())
					: new ArrayList<>();

			String logsRaw = doc.getString("logs");
			List<SerializableLog> logs = (logsRaw != null && !logsRaw.isEmpty())
					? Arrays.stream(logsRaw.split("µ")).map(SerializableLog::fromString).collect(Collectors.toList())
					: new ArrayList<>();

			SerializableRent rent = SerializableRent.fromString(str(doc, "rent"));
			long upkeepAt = doc.getLong("upkeepAt");
			double taxesAmount = doc.getDouble("taxesAmount");
			int weather = doc.getInteger("weather");
			int time = doc.getInteger("time");

			String welcomeSignRaw = str(doc, "welcomeSign");
			SerializableLocation welcomeSign = welcomeSignRaw != null ? SerializableLocation.fromString(welcomeSignRaw) : null;
			String icon = str(doc, "icon");

			Region region = new Region(name, owner);

			region.setAutoUpdate(false);

			region.id = id;
			region.displayName = displayName;
			region.description = description;
			region.location = location;
			region.createdAt = createdAt;
			region.playerFlags = playerFlags;
			region.worldFlags = worldFlags;
			region.bank = bank;
			region.mapColor = mapColor;
			region.setChunks(chunks);
			region.setMembers(members);
			region.setRates(rates);
			region.setInvitedPlayers(ListUtils.removeNullElements(invitedPlayers));
			region.setBannedPlayers(bannedPlayers);
			region.setLogs(logs);
			region.rent = rent;
			region.upkeepAt = upkeepAt;
			region.taxesAmount = taxesAmount;
			region.weather = weather;
			region.time = time;
			region.welcomeSign = welcomeSign;
			region.icon = icon;

			region.setAutoUpdate(true);

			regions.add(region);
		}

		return regions;
	}

	@Override
	public List<War> importWars() throws Exception {
		List<War> wars = new ArrayList<>();

		for (Document doc : wars().find()) {
			UUID id = UUID.fromString(doc.getString("id"));
			String displayName = doc.getString("displayName");
			String name = doc.getString("name");
			String description = doc.getString("description");

			String regionsRaw = doc.getString("regions");
			List<UUID> regionIds = (regionsRaw != null && !regionsRaw.isEmpty())
					? Arrays.stream(regionsRaw.split("§")).map(UUID::fromString).collect(Collectors.toList())
					: new ArrayList<>();

			double prize = doc.getDouble("prize");
			long startedAt = doc.getLong("startedAt");

			War war = new War(name, regionIds);
			war.id = id;
			war.displayName = displayName;
			war.description = description;
			war.prize = prize;
			war.startedAt = startedAt;

			wars.add(war);
		}

		return wars;
	}

	@Override
	public List<SubArea> importSubAreas() throws Exception {
		List<SubArea> subAreas = new ArrayList<>();

		for (Document doc : subareas().find()) {
			UUID id = UUID.fromString(doc.getString("id"));
			UUID regionId = UUID.fromString(doc.getString("regionId"));
			String name = doc.getString("name");

			World world = resolveWorld(doc.getString("worldName"));
			if (world == null) continue;

			Block point1 = SubArea.parseBlockLocation(world, doc.getString("point1"));
			Block point2 = SubArea.parseBlockLocation(world, doc.getString("point2"));

			String membersRaw = doc.getString("members");
			List<SerializableMember> members = (membersRaw != null && !membersRaw.isEmpty())
					? Arrays.stream(membersRaw.split("§")).map(SerializableMember::fromString).collect(Collectors.toList())
					: new ArrayList<>();

			long flags = doc.getLong("flags");
			String rentRaw = str(doc, "rent");
			SerializableRent rent = rentRaw != null ? SerializableRent.fromString(rentRaw) : null;
			long createdAt = doc.getLong("createdAt");

			SubArea subArea = new SubArea(id, regionId, name, world.getUID(),
					point1, point2, members, flags, rent, createdAt);

			subAreas.add(subArea);
		}

		return subAreas;
	}

	@Override
	public List<Level> importLevels() throws Exception {
		List<Level> levels = new ArrayList<>();

		for (Document doc : levels().find()) {
			UUID id = UUID.fromString(doc.getString("id"));
			UUID regionId = UUID.fromString(doc.getString("regionId"));
			int level = doc.getInteger("level");
			long xp = doc.getLong("experience");
			long totalXp = doc.getLong("totalExperience");
			long createdAt = doc.getLong("createdAt");

			Level lvl = new Level(id, regionId, level, xp, totalXp, createdAt);
			levels.add(lvl);
		}

		return levels;
	}

	@Override
	public void exportRegions(List<Region> regions) throws Exception {
		Set<String> dbIds = new HashSet<>();
		for (Document doc : regions().find().projection(new Document("id", 1))) {
			dbIds.add(doc.getString("id"));
		}

		Set<String> cacheIds = new HashSet<>();

		for (Region region : regions) {
			String id = region.id.toString();
			cacheIds.add(id);

			String chunksStr = region.chunks.stream().map(SerializableChunk::toString).collect(Collectors.joining("§"));
			String membersStr = region.members.stream().map(SerializableMember::toString).collect(Collectors.joining("§"));
			String ratesStr = region.rates.stream().map(SerializableRate::toString).collect(Collectors.joining("§"));
			String invitedStr = region.getInvitedPlayers().stream().map(p -> p.getUniqueId().toString()).collect(Collectors.joining("§"));
			String bannedStr = region.bannedPlayers.stream().map(SerializableBannedPlayer::toString).collect(Collectors.joining("§"));
			String logsStr = region.logs.stream().map(SerializableLog::toString).collect(Collectors.joining("µ"));

			Document doc = new Document("id", id)
					.append("displayName", region.displayName)
					.append("name", region.name)
					.append("description", region.description)
					.append("ownerId", region.getOwner().getUniqueId().toString())
					.append("location", region.location != null ? region.location.toString() : null)
					.append("createdAt", region.createdAt)
					.append("playerFlags", region.playerFlags)
					.append("worldFlags", region.worldFlags)
					.append("bank", region.bank)
					.append("mapColor", region.mapColor)
					.append("chunks", chunksStr)
					.append("members", membersStr)
					.append("rates", ratesStr)
					.append("invitedPlayers", invitedStr)
					.append("bannedPlayers", bannedStr)
					.append("logs", logsStr)
					.append("rent", region.rent != null ? region.rent.toString() : null)
					.append("upkeepAt", region.upkeepAt)
					.append("taxesAmount", region.taxesAmount)
					.append("weather", region.weather)
					.append("time", region.time)
					.append("welcomeSign", region.welcomeSign != null ? region.welcomeSign.toString() : null)
					.append("icon", region.icon);

			regions().replaceOne(Filters.eq("id", id), doc, UPSERT);
		}

		dbIds.removeAll(cacheIds);
		for (String deletedId : dbIds) {
			regions().deleteOne(Filters.eq("id", deletedId));
		}
	}

	@Override
	public void exportWars(List<War> wars) throws Exception {
		Set<String> dbIds = new HashSet<>();
		for (Document doc : wars().find().projection(new Document("id", 1))) {
			dbIds.add(doc.getString("id"));
		}

		Set<String> cacheIds = new HashSet<>();

		for (War war : wars) {
			String id = war.id.toString();
			cacheIds.add(id);

			String regionsStr = war.regions.stream().map(UUID::toString).collect(Collectors.joining("§"));

			Document doc = new Document("id", id)
					.append("displayName", war.displayName)
					.append("name", war.name)
					.append("description", war.description)
					.append("regions", regionsStr)
					.append("prize", war.prize)
					.append("startedAt", war.startedAt);

			wars().replaceOne(Filters.eq("id", id), doc, UPSERT);
		}

		dbIds.removeAll(cacheIds);
		for (String deletedId : dbIds) {
			wars().deleteOne(Filters.eq("id", deletedId));
		}
	}

	@Override
	public void exportSubAreas(List<SubArea> subareas) throws Exception {
		Set<String> dbIds = new HashSet<>();
		for (Document doc : subareas().find().projection(new Document("id", 1))) {
			dbIds.add(doc.getString("id"));
		}

		Set<String> cacheIds = new HashSet<>();

		for (SubArea subArea : subareas) {
			String id = subArea.id.toString();
			cacheIds.add(id);

			String membersStr = subArea.members.stream().map(SerializableMember::toString).collect(Collectors.joining("§"));

			Document doc = new Document("id", id)
					.append("regionId", subArea.regionId.toString())
					.append("name", subArea.name)
					.append("worldName", subArea.worldId.toString())
					.append("point1", SubArea.toStringBlockLocation(subArea.getWorld(), subArea.point1))
					.append("point2", SubArea.toStringBlockLocation(subArea.getWorld(), subArea.point2))
					.append("members", membersStr)
					.append("flags", subArea.flags)
					.append("rent", subArea.rent != null ? subArea.rent.toString() : null)
					.append("createdAt", subArea.createdAt);

			subareas().replaceOne(Filters.eq("id", id), doc, UPSERT);
		}

		dbIds.removeAll(cacheIds);
		for (String deletedId : dbIds) {
			subareas().deleteOne(Filters.eq("id", deletedId));
		}
	}

	@Override
	public void exportLevels(List<Level> levels) throws Exception {
		Set<String> dbIds = new HashSet<>();
		for (Document doc : levels().find().projection(new Document("id", 1))) {
			dbIds.add(doc.getString("id"));
		}

		Set<String> cacheIds = new HashSet<>();

		for (Level lvl : levels) {
			String id = lvl.getUniqueId().toString();
			cacheIds.add(id);

			Document doc = new Document("id", id)
					.append("regionId", lvl.getRegionId().toString())
					.append("level", lvl.getLevel())
					.append("experience", lvl.getExperience())
					.append("totalExperience", lvl.getTotalExperience())
					.append("createdAt", lvl.getCreatedAt());

			levels().replaceOne(Filters.eq("id", id), doc, UPSERT);
		}

		dbIds.removeAll(cacheIds);
		for (String deletedId : dbIds) {
			levels().deleteOne(Filters.eq("id", deletedId));
		}
	}

	@Override
	public void prepareTables() throws Exception {
	}

	@Override
	public long getLatency() {
		List<String> collections = List.of(
				COLLECTION_PREFIX + "regions",
				COLLECTION_PREFIX + "wars",
				COLLECTION_PREFIX + "subareas",
				COLLECTION_PREFIX + "levels"
		);

		try {
			long startTime = System.currentTimeMillis();

			for (String collection : collections) {
				mongoDatabase.getCollection(collection).countDocuments();
			}

			long endTime = System.currentTimeMillis();

			return endTime - startTime;
		} catch (Exception ignored) {
			return -1L;
		}
	}

	@Override
	public void closeConnection() throws Exception {
		if (mongoClient != null) {
			mongoClient.close();
		}
	}
}