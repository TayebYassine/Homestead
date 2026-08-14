package me.tayebyassine.homestead.database.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "region_banned_players")
public final class RegionBanEntity {

	@DatabaseField(id = true, columnName = "id")
	public long id;

	@DatabaseField(columnName = "regionId")
	public long regionId;

	@DatabaseField(columnName = "playerId")
	public String playerId;

	@DatabaseField(columnName = "reason", dataType = DataType.LONG_STRING)
	public String reason;

	@DatabaseField(columnName = "bannedAt")
	public long bannedAt;

	public RegionBanEntity() {
	}
}