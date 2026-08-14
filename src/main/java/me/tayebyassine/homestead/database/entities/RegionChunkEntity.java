package me.tayebyassine.homestead.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "region_chunks")
public final class RegionChunkEntity {

	@DatabaseField(id = true, columnName = "id")
	public long id;

	@DatabaseField(columnName = "regionId")
	public long regionId;

	@DatabaseField(columnName = "worldId")
	public String worldId;

	@DatabaseField(columnName = "x")
	public int x;

	@DatabaseField(columnName = "z")
	public int z;

	@DatabaseField(columnName = "claimedAt")
	public long claimedAt;

	@DatabaseField(columnName = "forceLoaded")
	public boolean forceLoaded;

	public RegionChunkEntity() {
	}
}