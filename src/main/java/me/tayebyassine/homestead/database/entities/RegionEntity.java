package me.tayebyassine.homestead.database.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "regions")
public final class RegionEntity {

	@DatabaseField(id = true, columnName = "id")
	public long id;

	@DatabaseField(columnName = "name")
	public String name;

	@DatabaseField(columnName = "displayName")
	public String displayName;

	@DatabaseField(columnName = "description", dataType = DataType.LONG_STRING)
	public String description;

	@DatabaseField(columnName = "ownerId")
	public String ownerId;

	@DatabaseField(columnName = "location", dataType = DataType.LONG_STRING)
	public String location;

	@DatabaseField(columnName = "playerFlags")
	public long playerFlags;

	@DatabaseField(columnName = "worldFlags")
	public long worldFlags;

	@DatabaseField(columnName = "taxes")
	public double taxes;

	@DatabaseField(columnName = "bank")
	public double bank;

	@DatabaseField(columnName = "mapColor")
	public int mapColor;

	@DatabaseField(columnName = "mapIcon")
	public String mapIcon;

	@DatabaseField(columnName = "rent", dataType = DataType.LONG_STRING)
	public String rent;

	@DatabaseField(columnName = "weather")
	public int weather;

	@DatabaseField(columnName = "time")
	public int time;

	@DatabaseField(columnName = "welcomeSign", dataType = DataType.LONG_STRING)
	public String welcomeSign;

	@DatabaseField(columnName = "upkeepAt")
	public long upkeepAt;

	@DatabaseField(columnName = "createdAt")
	public long createdAt;

	public RegionEntity() {
	}
}
