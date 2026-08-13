package me.tayebyassine.homestead.api;

import me.tayebyassine.homestead.Homestead;

public class API implements HomesteadAPI {
	public String getVersion() {
		return Homestead.getVersion();
	}

	public Homestead getInstance() {
		return Homestead.getInstance();
	}
}
