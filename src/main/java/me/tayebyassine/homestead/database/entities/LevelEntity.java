package me.tayebyassine.homestead.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "levels")
public final class LevelEntity {

	@DatabaseField(id = true, columnName = "id")
	public long id;

	@DatabaseField(columnName = "regionId")
	public long regionId;

	@DatabaseField(columnName = "level")
	public int level;

	@DatabaseField(columnName = "experience")
	public long experience;

	@DatabaseField(columnName = "totalExperience")
	public long totalExperience;

	@DatabaseField(columnName = "createdAt")
	public long createdAt;

	public LevelEntity() {
	}
}