package me.tayebyassine.homestead.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "region_rates")
public final class RegionRateEntity {

	@DatabaseField(id = true, columnName = "id")
	public long id;

	@DatabaseField(columnName = "regionId")
	public long regionId;

	@DatabaseField(columnName = "playerId")
	public String playerId;

	@DatabaseField(columnName = "rate")
	public int rate;

	@DatabaseField(columnName = "ratedAt")
	public long ratedAt;

	public RegionRateEntity() {
	}
}