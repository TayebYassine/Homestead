package tfagaming.projects.minecraft.homestead.resources.files;

import tfagaming.projects.minecraft.homestead.resources.ResourceFile;

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