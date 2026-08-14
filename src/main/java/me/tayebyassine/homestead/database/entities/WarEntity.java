package me.tayebyassine.homestead.database.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "wars")
public final class WarEntity {

	@DatabaseField(id = true, columnName = "id")
	public long id;

	@DatabaseField(columnName = "name")
	public String name;

	@DatabaseField(columnName = "displayName")
	public String displayName;

	@DatabaseField(columnName = "description", dataType = DataType.LONG_STRING)
	public String description;

	@DatabaseField(columnName = "prize")
	public double prize;

	@DatabaseField(columnName = "startedAt")
	public long startedAt;

	public WarEntity() {
	}
}