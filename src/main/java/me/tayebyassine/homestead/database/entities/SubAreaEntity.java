package me.tayebyassine.homestead.database.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "subareas")
public final class SubAreaEntity {

	@DatabaseField(id = true, columnName = "id")
	public long id;

	@DatabaseField(columnName = "regionId")
	public long regionId;

	@DatabaseField(columnName = "name")
	public String name;

	@DatabaseField(columnName = "worldId")
	public String worldId;

	@DatabaseField(columnName = "point1", dataType = DataType.LONG_STRING)
	public String point1;

	@DatabaseField(columnName = "point2", dataType = DataType.LONG_STRING)
	public String point2;

	@DatabaseField(columnName = "playerFlags")
	public long playerFlags;

	@DatabaseField(columnName = "rent", dataType = DataType.LONG_STRING)
	public String rent;

	@DatabaseField(columnName = "createdAt")
	public long createdAt;

	public SubAreaEntity() {
	}
}