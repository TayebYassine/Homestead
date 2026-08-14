package me.tayebyassine.homestead.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "region_invites")
public final class RegionInviteEntity {

	@DatabaseField(id = true, columnName = "id")
	public long id;

	@DatabaseField(columnName = "regionId")
	public long regionId;

	@DatabaseField(columnName = "playerId")
	public String playerId;

	@DatabaseField(columnName = "invitedAt")
	public long invitedAt;

	public RegionInviteEntity() {
	}
}