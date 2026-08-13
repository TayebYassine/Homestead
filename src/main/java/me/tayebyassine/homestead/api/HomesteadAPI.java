package me.tayebyassine.homestead.api;

import me.tayebyassine.homestead.Homestead;

public interface HomesteadAPI {
	String getVersion();

	Homestead getInstance();
}