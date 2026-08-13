package me.tayebyassine.homestead.resources.files;

import me.tayebyassine.homestead.resources.ResourceFile;

import java.io.File;
import java.io.FileNotFoundException;

public class LanguageFile extends ResourceFile {

	public LanguageFile(File file) throws FileNotFoundException {
		super(file);
	}

	public String getPrefix() {
		return getString("prefix");
	}

	@Override
	public String getString(String path) {
		return getString(path, "NULL @ " + path);
	}
}