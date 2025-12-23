package tfagaming.projects.minecraft.homestead.api;

import tfagaming.projects.minecraft.homestead.Homestead;

public class API implements HomesteadAPI {
	public String getVersion() {
		return Homestead.getVersion();
	}
}
