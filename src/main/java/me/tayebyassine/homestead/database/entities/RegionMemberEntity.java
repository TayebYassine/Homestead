package me.tayebyassine.homestead.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "region_members")
public final class RegionMemberEntity {

	@DatabaseField(id = true, columnName = "id")
	public long id;

	@DatabaseField(columnName = "playerId")
	public String playerId;

	@DatabaseField(columnName = "linkageType")
	public int linkageType;

	@DatabaseField(columnName = "regionId")
	public long regionId;

	@DatabaseField(columnName = "subAreaId")
	public long subAreaId;

	@DatabaseField(columnName = "playerFlags")
	public long playerFlags;

	@DatabaseField(columnName = "controlFlags")
	public long controlFlags;

	@DatabaseField(columnName = "joinedAt")
	public long joinedAt;

	@DatabaseField(columnName = "taxesAt")
	public long taxesAt;

	public RegionMemberEntity() {
	}
}