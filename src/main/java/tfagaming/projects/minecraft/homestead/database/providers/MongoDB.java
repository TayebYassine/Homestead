package tfagaming.projects.minecraft.homestead.database.providers;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.World;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.models.*;
import tfagaming.projects.minecraft.homestead.models.serialize.SeBlock;
import tfagaming.projects.minecraft.homestead.models.serialize.SeLocation;
import tfagaming.projects.minecraft.homestead.models.serialize.SeRent;

import java.util.*;

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

	private MongoCollection<Document> regionMembers() {
		return mongoDatabase.getCollection(COLLECTION_PREFIX + "region_members");
	}

	private MongoCollection<Document> regionChunks() {
		return mongoDatabase.getCollection(COLLECTION_PREFIX + "region_chunks");
	}

	private MongoCollection<Document> regionLogs() {
		return mongoDatabase.getCollection(COLLECTION_PREFIX + "region_logs");
	}

	private MongoCollection<Document> regionRates() {
		return mongoDatabase.getCollection(COLLECTION_PREFIX + "region_rates");
	}

	private MongoCollection<Document> regionInvites() {
		return mongoDatabase.getCollection(COLLECTION_PREFIX + "region_invites");
	}

	private MongoCollection<Document> regionBannedPlayers() {
		return mongoDatabase.getCollection(COLLECTION_PREFIX + "region_banned_players");
	}

	private MongoCollection<Document> subareas() {
		return mongoDatabase.getCollection(COLLECTION_PREFIX + "subareas");
	}

	private MongoCollection<Document> levels() {
		return mongoDatabase.getCollection(COLLECTION_PREFIX + "levels");
	}

	private MongoCollection<Document> wars() {
		return mongoDatabase.getCollection(COLLECTION_PREFIX + "wars");
	}

	private MongoCollection<Document> warRegions() {
		return mongoDatabase.getCollection(COLLECTION_PREFIX + "war_regions");
	}


	@Override
	public void prepareTables() throws Exception {
		if (legacyCollectionsExist()) {
			migrateFromLegacy();
		}

	}

	private boolean legacyCollectionsExist() {
		for (String name : mongoDatabase.listCollectionNames()) {
			if (name.equals(COLLECTION_PREFIX + "regions")) {

				Document doc = regions().find().first();
				if (doc != null && doc.containsKey("chunks") && doc.get("chunks") instanceof String) {
					return true;
				}
			}
		}
		return false;
	}

	private void migrateFromLegacy() {
		Map<String, Long> regionIdMap = new HashMap<>();
		Map<String, Long> subAreaIdMap = new HashMap<>();

		List<Region> newRegions = new ArrayList<>();
		List<RegionMember> newMembers = new ArrayList<>();
		List<RegionChunk> newChunks = new ArrayList<>();
		List<RegionLog> newLogs = new ArrayList<>();
		List<RegionRate> newRates = new ArrayList<>();
		List<RegionInvite> newInvites = new ArrayList<>();
		List<RegionBannedPlayer> newBanned = new ArrayList<>();
		List<SubArea> newSubAreas = new ArrayList<>();
		List<Level> newLevels = new ArrayList<>();
		List<War> newWars = new ArrayList<>();
		Map<Long, List<Long>> warRegionMap = new LinkedHashMap<>();


		for (Document doc : regions().find()) {
			String oldId = doc.getString("id");
			try {
				long newId = Homestead.SNOWFLAKE.nextId();
				regionIdMap.put(oldId, newId);

				UUID ownerId = UUID.fromString(doc.getString("ownerId"));
				long createdAt = doc.getLong("createdAt");
				Region region = new Region(newId, doc.getString("name"), ownerId, createdAt);

				region.setDisplayName(doc.getString("displayName"));
				region.setDescription(doc.getString("description"));
				region.setPlayerFlags(doc.getLong("playerFlags"));
				region.setWorldFlags(doc.getLong("worldFlags"));
				region.setBank(doc.getDouble("bank"));
				region.setMapColor(doc.getInteger("mapColor"));
				region.setUpkeepAt(doc.getLong("upkeepAt"));
				region.setTaxes(doc.getDouble("taxesAmount"));
				region.setWeather(doc.getInteger("weather"));
				region.setTime(doc.getInteger("time"));
				region.setMapIcon(str(doc, "icon"));

				String locStr = str(doc, "location");
				if (LegacyParsers.isNotBlank(locStr)) region.setLocation(SeLocation.deserialize(locStr));

				String wsStr = str(doc, "welcomeSign");
				if (LegacyParsers.isNotBlank(wsStr)) region.setWelcomeSign(SeLocation.deserialize(wsStr));

				String rentStr = str(doc, "rent");
				if (LegacyParsers.isNotBlank(rentStr)) region.setRent(SeRent.deserialize(rentStr));

				newRegions.add(region);

				String chunksRaw = doc.getString("chunks");
				if (chunksRaw != null && !chunksRaw.isEmpty()) {
					for (String part : chunksRaw.split("\u00A7")) {
						if (part == null || part.isBlank()) continue;
						RegionChunk c = LegacyParsers.parseLegacyChunk(newId, part);
						if (c != null) newChunks.add(c);
					}
				}

				String membersRaw = doc.getString("members");
				if (membersRaw != null && !membersRaw.isEmpty()) {
					for (String part : membersRaw.split("\u00A7")) {
						if (part == null || part.isBlank()) continue;
						RegionMember m = LegacyParsers.parseLegacyMember(newId, RegionMember.LinkageType.REGION, part);
						if (m != null) newMembers.add(m);
					}
				}

				String ratesRaw = doc.getString("rates");
				if (ratesRaw != null && !ratesRaw.isEmpty()) {
					for (String part : ratesRaw.split("\u00A7")) {
						if (part == null || part.isBlank()) continue;
						RegionRate r = LegacyParsers.parseLegacyRate(newId, part);
						if (r != null) newRates.add(r);
					}
				}

				String invitedRaw = doc.getString("invitedPlayers");
				if (invitedRaw != null && !invitedRaw.isEmpty()) {
					for (String part : invitedRaw.split("\u00A7")) {
						if (part == null || part.isBlank()) continue;
						try {
							UUID pid = UUID.fromString(part.trim());
							newInvites.add(new RegionInvite(Homestead.SNOWFLAKE.nextId(), newId, pid, createdAt));
						} catch (IllegalArgumentException ignored) {
						}
					}
				}

				String bannedRaw = doc.getString("bannedPlayers");
				if (bannedRaw != null && !bannedRaw.isEmpty()) {
					for (String part : bannedRaw.split("\u00A7")) {
						if (part == null || part.isBlank()) continue;
						RegionBannedPlayer b = LegacyParsers.parseLegacyBannedPlayer(newId, part);
						if (b != null) newBanned.add(b);
					}
				}

				String logsRaw = doc.getString("logs");
				if (logsRaw != null && !logsRaw.isEmpty()) {
					for (String part : logsRaw.split("\u00B5")) {
						if (part == null || part.isBlank()) continue;
						RegionLog l = LegacyParsers.parseLegacyLog(newId, part);
						if (l != null) newLogs.add(l);
					}
				}
			} catch (Exception ignored) {
			}
		}


		for (Document doc : subareas().find()) {
			String oldId = doc.getString("id");
			String oldRegionId = doc.getString("regionId");
			try {
				Long newRegionId = regionIdMap.get(oldRegionId);
				if (newRegionId == null) continue;

				long newSubAreaId = Homestead.SNOWFLAKE.nextId();
				subAreaIdMap.put(oldId, newSubAreaId);

				UUID worldId = LegacyParsers.resolveWorldUUID(doc.getString("worldName"));
				if (worldId == null) continue;

				var point1 = LegacyParsers.parseLegacyBlock(worldId, doc.getString("point1"));
				var point2 = LegacyParsers.parseLegacyBlock(worldId, doc.getString("point2"));
				if (point1 == null || point2 == null) continue;

				String rentStr = str(doc, "rent");
				SeRent rent = LegacyParsers.isNotBlank(rentStr) ? SeRent.deserialize(rentStr) : null;

				SubArea subArea = new SubArea(
						newSubAreaId,
						newRegionId,
						doc.getString("name"),
						worldId,
						point1,
						point2,
						doc.getLong("flags"),
						rent,
						doc.getLong("createdAt"));
				newSubAreas.add(subArea);

				String membersRaw = doc.getString("members");
				if (membersRaw != null && !membersRaw.isEmpty()) {
					for (String part : membersRaw.split("\u00A7")) {
						if (part == null || part.isBlank()) continue;
						RegionMember m = LegacyParsers.parseLegacyMember(newSubAreaId, RegionMember.LinkageType.SUBAREA, part);
						if (m != null) newMembers.add(m);
					}
				}
			} catch (Exception ignored) {
			}
		}


		for (Document doc : levels().find()) {
			String oldRegionId = doc.getString("regionId");
			try {
				Long newRegionId = regionIdMap.get(oldRegionId);
				if (newRegionId == null) continue;
				newLevels.add(new Level(
						Homestead.SNOWFLAKE.nextId(),
						newRegionId,
						doc.getInteger("level"),
						doc.getLong("experience"),
						doc.getLong("totalExperience"),
						doc.getLong("createdAt")));
			} catch (Exception ignored) {
			}
		}


		for (Document doc : wars().find()) {
			String oldWarId = doc.getString("id");
			try {
				long newWarId = Homestead.SNOWFLAKE.nextId();
				List<Long> mappedRegionIds = new ArrayList<>();
				String regionsRaw = doc.getString("regions");
				if (regionsRaw != null && !regionsRaw.isEmpty()) {
					for (String raw : regionsRaw.split("\u00A7")) {
						if (raw == null || raw.isBlank()) continue;
						Long mapped = regionIdMap.get(raw.trim());
						if (mapped != null) mappedRegionIds.add(mapped);
					}
				}
				War war = new War(
						newWarId,
						doc.getString("name"),
						doc.getString("displayName"),
						doc.getString("description"),
						mappedRegionIds,
						doc.getDouble("prize"),
						doc.getLong("startedAt"));
				newWars.add(war);
				warRegionMap.put(newWarId, mappedRegionIds);
			} catch (Exception ignored) {
			}
		}


		regions().drop();
		subareas().drop();
		levels().drop();
		wars().drop();


		batchInsertRegions(newRegions);
		batchInsertRegionMembers(newMembers);
		batchInsertRegionChunks(newChunks);
		batchInsertRegionLogs(newLogs);
		batchInsertRegionRates(newRates);
		batchInsertRegionInvites(newInvites);
		batchInsertRegionBannedPlayers(newBanned);
		batchInsertSubAreas(newSubAreas);
		batchInsertLevels(newLevels);
		batchInsertWars(newWars);
		batchInsertWarRegions(warRegionMap);
	}


	private void batchInsertRegions(List<Region> rows) {
		if (rows.isEmpty()) return;
		for (Region r : rows) {
			Document doc = new Document("id", r.getUniqueId())
					.append("name", r.getName())
					.append("displayName", r.getDisplayName())
					.append("description", r.getDescription())
					.append("ownerId", r.getOwnerId().toString())
					.append("location", r.getLocation() != null ? r.getLocation().serialize() : null)
					.append("playerFlags", r.getPlayerFlags())
					.append("worldFlags", r.getWorldFlags())
					.append("taxes", r.getTaxes())
					.append("bank", r.getBank())
					.append("mapColor", r.getMapColor())
					.append("mapIcon", r.getMapIcon())
					.append("rent", r.getRent() != null ? r.getRent().serialize() : null)
					.append("weather", r.getWeather())
					.append("time", r.getTime())
					.append("welcomeSign", r.getWelcomeSign() != null ? r.getWelcomeSign().serialize() : null)
					.append("upkeepAt", r.getUpkeepAt())
					.append("createdAt", r.getCreatedAt());
			regions().replaceOne(Filters.eq("id", r.getUniqueId()), doc, UPSERT);
		}
	}

	private void batchInsertRegionMembers(List<RegionMember> rows) {
		if (rows.isEmpty()) return;
		for (RegionMember m : rows) {
			Document doc = new Document("id", m.getUniqueId())
					.append("playerId", m.getPlayerId().toString())
					.append("linkageType", m.getLinkageType().getValue())
					.append("regionId", m.getRegionId())
					.append("subAreaId", m.getSubAreaId())
					.append("playerFlags", m.getPlayerFlags())
					.append("controlFlags", m.getControlFlags())
					.append("joinedAt", m.getJoinedAt())
					.append("taxesAt", m.getTaxesAt());
			regionMembers().replaceOne(Filters.eq("id", m.getUniqueId()), doc, UPSERT);
		}
	}

	private void batchInsertRegionChunks(List<RegionChunk> rows) {
		if (rows.isEmpty()) return;
		for (RegionChunk c : rows) {
			Document doc = new Document("id", c.getUniqueId())
					.append("regionId", c.getRegionId())
					.append("worldId", c.getWorldId().toString())
					.append("x", c.getX())
					.append("z", c.getZ())
					.append("claimedAt", c.getClaimedAt())
					.append("forceLoaded", c.isForceLoaded());
			regionChunks().replaceOne(Filters.eq("id", c.getUniqueId()), doc, UPSERT);
		}
	}

	private void batchInsertRegionLogs(List<RegionLog> rows) {
		if (rows.isEmpty()) return;
		for (RegionLog l : rows) {
			Document doc = new Document("id", l.getUniqueId())
					.append("regionId", l.getRegionId())
					.append("author", l.getAuthor())
					.append("message", l.getMessage())
					.append("sentAt", l.getSentAt())
					.append("read", l.isRead());
			regionLogs().replaceOne(Filters.eq("id", l.getUniqueId()), doc, UPSERT);
		}
	}

	private void batchInsertRegionRates(List<RegionRate> rows) {
		if (rows.isEmpty()) return;
		for (RegionRate r : rows) {
			Document doc = new Document("id", r.getUniqueId())
					.append("regionId", r.getRegionId())
					.append("playerId", r.getPlayerId().toString())
					.append("rate", r.getRate())
					.append("ratedAt", r.getRatedAt());
			regionRates().replaceOne(Filters.eq("id", r.getUniqueId()), doc, UPSERT);
		}
	}

	private void batchInsertRegionInvites(List<RegionInvite> rows) {
		if (rows.isEmpty()) return;
		for (RegionInvite i : rows) {
			Document doc = new Document("id", i.getUniqueId())
					.append("regionId", i.getRegionId())
					.append("playerId", i.getPlayerId().toString())
					.append("invitedAt", i.getInvitedAt());
			regionInvites().replaceOne(Filters.eq("id", i.getUniqueId()), doc, UPSERT);
		}
	}

	private void batchInsertRegionBannedPlayers(List<RegionBannedPlayer> rows) {
		if (rows.isEmpty()) return;
		for (RegionBannedPlayer b : rows) {
			Document doc = new Document("id", b.getUniqueId())
					.append("regionId", b.getRegionId())
					.append("playerId", b.getPlayerId().toString())
					.append("reason", b.getReason())
					.append("bannedAt", b.getBannedAt());
			regionBannedPlayers().replaceOne(Filters.eq("id", b.getUniqueId()), doc, UPSERT);
		}
	}

	private void batchInsertSubAreas(List<SubArea> rows) {
		if (rows.isEmpty()) return;
		for (SubArea s : rows) {
			Document doc = new Document("id", s.getUniqueId())
					.append("regionId", s.getRegionId())
					.append("name", s.getName())
					.append("worldId", s.getWorldId().toString())
					.append("point1", s.getPoint1().serialize())
					.append("point2", s.getPoint2().serialize())
					.append("playerFlags", s.getPlayerFlags())
					.append("rent", s.getRent() != null ? s.getRent().serialize() : null)
					.append("createdAt", s.getCreatedAt());
			subareas().replaceOne(Filters.eq("id", s.getUniqueId()), doc, UPSERT);
		}
	}

	private void batchInsertLevels(List<Level> rows) {
		if (rows.isEmpty()) return;
		for (Level l : rows) {
			Document doc = new Document("id", l.getUniqueId())
					.append("regionId", l.getRegionId())
					.append("level", l.getLevel())
					.append("experience", l.getExperience())
					.append("totalExperience", l.getTotalExperience())
					.append("createdAt", l.getCreatedAt());
			levels().replaceOne(Filters.eq("id", l.getUniqueId()), doc, UPSERT);
		}
	}

	private void batchInsertWars(List<War> rows) {
		if (rows.isEmpty()) return;
		for (War w : rows) {
			Document doc = new Document("id", w.getUniqueId())
					.append("name", w.getName())
					.append("displayName", w.getDisplayName())
					.append("description", w.getDescription())
					.append("prize", w.getPrize())
					.append("startedAt", w.getStartedAt());
			wars().replaceOne(Filters.eq("id", w.getUniqueId()), doc, UPSERT);
		}
	}

	private void batchInsertWarRegions(Map<Long, List<Long>> warRegionMap) {
		if (warRegionMap.isEmpty()) return;
		for (var entry : warRegionMap.entrySet()) {
			for (long regionId : entry.getValue()) {
				Document doc = new Document("warId", entry.getKey())
						.append("regionId", regionId);
				warRegions().replaceOne(
						Filters.and(Filters.eq("warId", entry.getKey()), Filters.eq("regionId", regionId)),
						doc, UPSERT);
			}
		}
	}


	@Override
	public List<Region> importRegions() throws Exception {
		List<Region> list = new ArrayList<>();
		for (Document doc : regions().find()) {
			long id = doc.getLong("id");
			UUID ownerId = UUID.fromString(doc.getString("ownerId"));
			long created = doc.getLong("createdAt");

			Region region = new Region(id, doc.getString("name"), ownerId, created);
			region.setDisplayName(doc.getString("displayName"));
			region.setDescription(doc.getString("description"));
			region.setPlayerFlags(doc.getLong("playerFlags"));
			region.setWorldFlags(doc.getLong("worldFlags"));
			region.setTaxes(doc.getDouble("taxes"));
			region.setBank(doc.getDouble("bank"));
			region.setMapColor(doc.getInteger("mapColor"));
			region.setMapIcon(str(doc, "mapIcon"));
			region.setWeather(doc.getInteger("weather"));
			region.setTime(doc.getInteger("time"));
			region.setUpkeepAt(doc.getLong("upkeepAt"));

			String locStr = str(doc, "location");
			if (locStr != null) region.setLocation(SeLocation.deserialize(locStr));
			String wsStr = str(doc, "welcomeSign");
			if (wsStr != null) region.setWelcomeSign(SeLocation.deserialize(wsStr));
			String rentStr = str(doc, "rent");
			if (rentStr != null) region.setRent(SeRent.deserialize(rentStr));

			list.add(region);
		}
		return list;
	}

	@Override
	public List<RegionMember> importRegionMembers() throws Exception {
		List<RegionMember> list = new ArrayList<>();
		for (Document doc : regionMembers().find()) {
			UUID playerId = UUID.fromString(doc.getString("playerId"));
			int typeVal = doc.getInteger("linkageType");
			long regionId = doc.getLong("regionId");
			long subAreaId = doc.getLong("subAreaId");

			RegionMember.LinkageType type =
					typeVal == RegionMember.LinkageType.REGION.getValue()
							? RegionMember.LinkageType.REGION
							: RegionMember.LinkageType.SUBAREA;
			long linkageId = type == RegionMember.LinkageType.REGION ? regionId : subAreaId;

			RegionMember member = new RegionMember(playerId, type, linkageId);
			member.setPlayerFlags(doc.getLong("playerFlags"));
			member.setControlFlags(doc.getLong("controlFlags"));
			member.setJoinedAt(doc.getLong("joinedAt"));
			member.setTaxesAt(doc.getLong("taxesAt"));
			list.add(member);
		}
		return list;
	}

	@Override
	public List<RegionChunk> importRegionChunks() throws Exception {
		List<RegionChunk> list = new ArrayList<>();
		for (Document doc : regionChunks().find()) {
			list.add(new RegionChunk(
					doc.getLong("id"),
					doc.getLong("regionId"),
					UUID.fromString(doc.getString("worldId")),
					doc.getInteger("x"),
					doc.getInteger("z"),
					doc.getLong("claimedAt"),
					Boolean.TRUE.equals(doc.getBoolean("forceLoaded"))));
		}
		return list;
	}

	@Override
	public List<RegionLog> importRegionLogs() throws Exception {
		List<RegionLog> list = new ArrayList<>();
		for (Document doc : regionLogs().find()) {
			list.add(new RegionLog(
					doc.getLong("id"),
					doc.getLong("regionId"),
					doc.getString("author"),
					doc.getString("message"),
					doc.getLong("sentAt"),
					Boolean.TRUE.equals(doc.getBoolean("read"))));
		}
		return list;
	}

	@Override
	public List<RegionRate> importRegionRates() throws Exception {
		List<RegionRate> list = new ArrayList<>();
		for (Document doc : regionRates().find()) {
			list.add(new RegionRate(
					doc.getLong("id"),
					doc.getLong("regionId"),
					UUID.fromString(doc.getString("playerId")),
					doc.getInteger("rate"),
					doc.getLong("ratedAt")));
		}
		return list;
	}

	@Override
	public List<RegionInvite> importRegionInvites() throws Exception {
		List<RegionInvite> list = new ArrayList<>();
		for (Document doc : regionInvites().find()) {
			list.add(new RegionInvite(
					doc.getLong("id"),
					doc.getLong("regionId"),
					UUID.fromString(doc.getString("playerId")),
					doc.getLong("invitedAt")));
		}
		return list;
	}

	@Override
	public List<RegionBannedPlayer> importRegionBannedPlayers() throws Exception {
		List<RegionBannedPlayer> list = new ArrayList<>();
		for (Document doc : regionBannedPlayers().find()) {
			list.add(new RegionBannedPlayer(
					doc.getLong("id"),
					doc.getLong("regionId"),
					UUID.fromString(doc.getString("playerId")),
					doc.getString("reason"),
					doc.getLong("bannedAt")));
		}
		return list;
	}

	@Override
	public List<SubArea> importSubAreas() throws Exception {
		List<SubArea> list = new ArrayList<>();
		for (Document doc : subareas().find()) {
			UUID worldId = UUID.fromString(doc.getString("worldId"));
			SeBlock p1 = SeBlock.deserialize(doc.getString("point1"));
			SeBlock p2 = SeBlock.deserialize(doc.getString("point2"));
			if (p1 == null || p2 == null) continue;
			String rentStr = str(doc, "rent");
			SeRent rent = rentStr != null ? SeRent.deserialize(rentStr) : null;
			list.add(new SubArea(
					doc.getLong("id"),
					doc.getLong("regionId"),
					doc.getString("name"),
					worldId, p1, p2,
					doc.getLong("playerFlags"),
					rent,
					doc.getLong("createdAt")));
		}
		return list;
	}

	@Override
	public List<Level> importLevels() throws Exception {
		List<Level> list = new ArrayList<>();
		for (Document doc : levels().find()) {
			list.add(new Level(
					doc.getLong("id"),
					doc.getLong("regionId"),
					doc.getInteger("level"),
					doc.getLong("experience"),
					doc.getLong("totalExperience"),
					doc.getLong("createdAt")));
		}
		return list;
	}

	@Override
	public List<War> importWars() throws Exception {
		Map<Long, List<Long>> warRegions = new HashMap<>();
		for (Document doc : warRegions().find()) {
			warRegions.computeIfAbsent(doc.getLong("warId"), k -> new ArrayList<>()).add(doc.getLong("regionId"));
		}

		List<War> list = new ArrayList<>();
		for (Document doc : wars().find()) {
			long warId = doc.getLong("id");
			list.add(new War(
					warId,
					doc.getString("name"),
					doc.getString("displayName"),
					doc.getString("description"),
					warRegions.getOrDefault(warId, new ArrayList<>()),
					doc.getDouble("prize"),
					doc.getLong("startedAt")));
		}
		return list;
	}


	@Override
	public void exportRegions(List<Region> regions) throws Exception {
		Set<Long> dbIds = new HashSet<>();
		for (Document doc : regions().find().projection(new Document("id", 1))) {
			dbIds.add(doc.getLong("id"));
		}

		Set<Long> cacheIds = new HashSet<>();

		for (Region region : regions) {
			long id = region.getUniqueId();
			cacheIds.add(id);

			Document doc = new Document("id", id)
					.append("name", region.getName())
					.append("displayName", region.getDisplayName())
					.append("description", region.getDescription())
					.append("ownerId", region.getOwnerId().toString())
					.append("location", region.getLocation() != null ? region.getLocation().serialize() : null)
					.append("playerFlags", region.getPlayerFlags())
					.append("worldFlags", region.getWorldFlags())
					.append("taxes", region.getTaxes())
					.append("bank", region.getBank())
					.append("mapColor", region.getMapColor())
					.append("mapIcon", region.getMapIcon())
					.append("rent", region.getRent() != null ? region.getRent().serialize() : null)
					.append("weather", region.getWeather())
					.append("time", region.getTime())
					.append("welcomeSign", region.getWelcomeSign() != null ? region.getWelcomeSign().serialize() : null)
					.append("upkeepAt", region.getUpkeepAt())
					.append("createdAt", region.getCreatedAt());

			regions().replaceOne(Filters.eq("id", id), doc, UPSERT);
		}

		dbIds.removeAll(cacheIds);
		for (long deletedId : dbIds) {
			regions().deleteOne(Filters.eq("id", deletedId));
		}
	}

	@Override
	public void exportRegionMembers(List<RegionMember> members) throws Exception {
		Set<Long> dbIds = new HashSet<>();
		for (Document doc : regionMembers().find().projection(new Document("id", 1))) {
			dbIds.add(doc.getLong("id"));
		}

		Set<Long> cacheIds = new HashSet<>();
		for (RegionMember m : members) {
			long id = m.getUniqueId();
			cacheIds.add(id);

			Document doc = new Document("id", id)
					.append("playerId", m.getPlayerId().toString())
					.append("linkageType", m.getLinkageType().getValue())
					.append("regionId", m.getRegionId())
					.append("subAreaId", m.getSubAreaId())
					.append("playerFlags", m.getPlayerFlags())
					.append("controlFlags", m.getControlFlags())
					.append("joinedAt", m.getJoinedAt())
					.append("taxesAt", m.getTaxesAt());

			regionMembers().replaceOne(Filters.eq("id", id), doc, UPSERT);
		}

		dbIds.removeAll(cacheIds);
		for (long deletedId : dbIds) {
			regionMembers().deleteOne(Filters.eq("id", deletedId));
		}
	}

	@Override
	public void exportRegionChunks(List<RegionChunk> chunks) throws Exception {
		Set<Long> dbIds = new HashSet<>();
		for (Document doc : regionChunks().find().projection(new Document("id", 1))) {
			dbIds.add(doc.getLong("id"));
		}

		Set<Long> cacheIds = new HashSet<>();
		for (RegionChunk c : chunks) {
			long id = c.getUniqueId();
			cacheIds.add(id);

			Document doc = new Document("id", id)
					.append("regionId", c.getRegionId())
					.append("worldId", c.getWorldId().toString())
					.append("x", c.getX())
					.append("z", c.getZ())
					.append("claimedAt", c.getClaimedAt())
					.append("forceLoaded", c.isForceLoaded());

			regionChunks().replaceOne(Filters.eq("id", id), doc, UPSERT);
		}

		dbIds.removeAll(cacheIds);
		for (long deletedId : dbIds) {
			regionChunks().deleteOne(Filters.eq("id", deletedId));
		}
	}

	@Override
	public void exportRegionLogs(List<RegionLog> logs) throws Exception {
		Set<Long> dbIds = new HashSet<>();
		for (Document doc : regionLogs().find().projection(new Document("id", 1))) {
			dbIds.add(doc.getLong("id"));
		}

		Set<Long> cacheIds = new HashSet<>();
		for (RegionLog l : logs) {
			long id = l.getUniqueId();
			cacheIds.add(id);

			Document doc = new Document("id", id)
					.append("regionId", l.getRegionId())
					.append("author", l.getAuthor())
					.append("message", l.getMessage())
					.append("sentAt", l.getSentAt())
					.append("read", l.isRead());

			regionLogs().replaceOne(Filters.eq("id", id), doc, UPSERT);
		}

		dbIds.removeAll(cacheIds);
		for (long deletedId : dbIds) {
			regionLogs().deleteOne(Filters.eq("id", deletedId));
		}
	}

	@Override
	public void exportRegionRates(List<RegionRate> rates) throws Exception {
		Set<Long> dbIds = new HashSet<>();
		for (Document doc : regionRates().find().projection(new Document("id", 1))) {
			dbIds.add(doc.getLong("id"));
		}

		Set<Long> cacheIds = new HashSet<>();
		for (RegionRate r : rates) {
			long id = r.getUniqueId();
			cacheIds.add(id);

			Document doc = new Document("id", id)
					.append("regionId", r.getRegionId())
					.append("playerId", r.getPlayerId().toString())
					.append("rate", r.getRate())
					.append("ratedAt", r.getRatedAt());

			regionRates().replaceOne(Filters.eq("id", id), doc, UPSERT);
		}

		dbIds.removeAll(cacheIds);
		for (long deletedId : dbIds) {
			regionRates().deleteOne(Filters.eq("id", deletedId));
		}
	}

	@Override
	public void exportRegionInvites(List<RegionInvite> invites) throws Exception {
		Set<Long> dbIds = new HashSet<>();
		for (Document doc : regionInvites().find().projection(new Document("id", 1))) {
			dbIds.add(doc.getLong("id"));
		}

		Set<Long> cacheIds = new HashSet<>();
		for (RegionInvite i : invites) {
			long id = i.getUniqueId();
			cacheIds.add(id);

			Document doc = new Document("id", id)
					.append("regionId", i.getRegionId())
					.append("playerId", i.getPlayerId().toString())
					.append("invitedAt", i.getInvitedAt());

			regionInvites().replaceOne(Filters.eq("id", id), doc, UPSERT);
		}

		dbIds.removeAll(cacheIds);
		for (long deletedId : dbIds) {
			regionInvites().deleteOne(Filters.eq("id", deletedId));
		}
	}

	@Override
	public void exportRegionBannedPlayers(List<RegionBannedPlayer> bannedPlayers) throws Exception {
		Set<Long> dbIds = new HashSet<>();
		for (Document doc : regionBannedPlayers().find().projection(new Document("id", 1))) {
			dbIds.add(doc.getLong("id"));
		}

		Set<Long> cacheIds = new HashSet<>();
		for (RegionBannedPlayer b : bannedPlayers) {
			long id = b.getUniqueId();
			cacheIds.add(id);

			Document doc = new Document("id", id)
					.append("regionId", b.getRegionId())
					.append("playerId", b.getPlayerId().toString())
					.append("reason", b.getReason())
					.append("bannedAt", b.getBannedAt());

			regionBannedPlayers().replaceOne(Filters.eq("id", id), doc, UPSERT);
		}

		dbIds.removeAll(cacheIds);
		for (long deletedId : dbIds) {
			regionBannedPlayers().deleteOne(Filters.eq("id", deletedId));
		}
	}

	@Override
	public void exportSubAreas(List<SubArea> subAreas) throws Exception {
		Set<Long> dbIds = new HashSet<>();
		for (Document doc : subareas().find().projection(new Document("id", 1))) {
			dbIds.add(doc.getLong("id"));
		}

		Set<Long> cacheIds = new HashSet<>();
		for (SubArea s : subAreas) {
			long id = s.getUniqueId();
			cacheIds.add(id);

			Document doc = new Document("id", id)
					.append("regionId", s.getRegionId())
					.append("name", s.getName())
					.append("worldId", s.getWorldId().toString())
					.append("point1", s.getPoint1().serialize())
					.append("point2", s.getPoint2().serialize())
					.append("playerFlags", s.getPlayerFlags())
					.append("rent", s.getRent() != null ? s.getRent().serialize() : null)
					.append("createdAt", s.getCreatedAt());

			subareas().replaceOne(Filters.eq("id", id), doc, UPSERT);
		}

		dbIds.removeAll(cacheIds);
		for (long deletedId : dbIds) {
			subareas().deleteOne(Filters.eq("id", deletedId));
		}
	}

	@Override
	public void exportLevels(List<Level> levels) throws Exception {
		Set<Long> dbIds = new HashSet<>();
		for (Document doc : levels().find().projection(new Document("id", 1))) {
			dbIds.add(doc.getLong("id"));
		}

		Set<Long> cacheIds = new HashSet<>();
		for (Level l : levels) {
			long id = l.getUniqueId();
			cacheIds.add(id);

			Document doc = new Document("id", id)
					.append("regionId", l.getRegionId())
					.append("level", l.getLevel())
					.append("experience", l.getExperience())
					.append("totalExperience", l.getTotalExperience())
					.append("createdAt", l.getCreatedAt());

			levels().replaceOne(Filters.eq("id", id), doc, UPSERT);
		}

		dbIds.removeAll(cacheIds);
		for (long deletedId : dbIds) {
			levels().deleteOne(Filters.eq("id", deletedId));
		}
	}

	@Override
	public void exportWars(List<War> wars) throws Exception {
		Set<Long> dbWarIds = new HashSet<>();
		for (Document doc : wars().find().projection(new Document("id", 1))) {
			dbWarIds.add(doc.getLong("id"));
		}

		Set<Long> cacheIds = new HashSet<>();
		for (War w : wars) {
			long warId = w.getUniqueId();
			cacheIds.add(warId);

			Document doc = new Document("id", warId)
					.append("name", w.getName())
					.append("displayName", w.getDisplayName())
					.append("description", w.getDescription())
					.append("prize", w.getPrize())
					.append("startedAt", w.getStartedAt());

			wars().replaceOne(Filters.eq("id", warId), doc, UPSERT);


			warRegions().deleteMany(Filters.eq("warId", warId));
			for (long regionId : w.getRegionIds()) {
				Document junc = new Document("warId", warId).append("regionId", regionId);
				warRegions().insertOne(junc);
			}
		}

		dbWarIds.removeAll(cacheIds);
		for (long staleId : dbWarIds) {
			wars().deleteOne(Filters.eq("id", staleId));
			warRegions().deleteMany(Filters.eq("warId", staleId));
		}
	}


	@Override
	public long getLatency() {
		List<String> collections = List.of(
				COLLECTION_PREFIX + "regions",
				COLLECTION_PREFIX + "region_members",
				COLLECTION_PREFIX + "region_chunks",
				COLLECTION_PREFIX + "region_logs",
				COLLECTION_PREFIX + "region_rates",
				COLLECTION_PREFIX + "region_invites",
				COLLECTION_PREFIX + "region_banned_players",
				COLLECTION_PREFIX + "subareas",
				COLLECTION_PREFIX + "levels",
				COLLECTION_PREFIX + "wars",
				COLLECTION_PREFIX + "war_regions"
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