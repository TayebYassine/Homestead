package me.tayebyassine.homestead.database.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "region_logs")
public final class RegionLogEntity {

	@DatabaseField(id = true, columnName = "id")
	public long id;

	@DatabaseField(columnName = "regionId")
	public long regionId;

	@DatabaseField(columnName = "author")
	public String author;

	@DatabaseField(columnName = "message", dataType = DataType.LONG_STRING)
	public String message;

	@DatabaseField(columnName = "sentAt")
	public long sentAt;

	@DatabaseField(columnName = "read")
	public boolean read;

	public RegionLogEntity() {
	}
}