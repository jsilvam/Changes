package utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public abstract class FileUtils {
	
	public static List<String> listClass(File srcFolder){
		List<String> result= new ArrayList<String>();
		
		File[] files=srcFolder.listFiles();
		
		for(File file:files) {
			if(file.isDirectory())
				result.addAll(listClass(file,file.getName()));
			else
				result.add(file.getName());
				
		}
		
		return result;
		
	}
	
	public static List<String> listClass(File srcFolder,String name){
		List<String> result= new ArrayList<String>();
		
		File[] files=srcFolder.listFiles();
		
		
		for(File file:files) {
			if(file.isDirectory())
				result.addAll(listClass(file,name+"."+file.getName()));
			else
				result.add(name+"."+file.getName());
				
		}
		
		return result;
		
	}

}
