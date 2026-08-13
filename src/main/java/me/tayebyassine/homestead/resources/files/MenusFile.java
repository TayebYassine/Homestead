package me.tayebyassine.homestead.resources.files;

import me.tayebyassine.homestead.resources.ResourceFile;

import java.io.File;
import java.io.FileNotFoundException;

public class MenusFile extends ResourceFile {

	public MenusFile(File file) throws FileNotFoundException {
		super(file);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String path) {
		return (T) getRaw(path);
	}
}