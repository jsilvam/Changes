package utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public abstract class FileUtils {
	
	public static List<String> listClasses(File srcFolder){
		if(!srcFolder.exists())
			return new ArrayList<String>();
		
		List<String> result= new ArrayList<String>();
		
		File[] files=srcFolder.listFiles();
		
		for(File file:files) {
			if(file.isDirectory())
				result.addAll(listClasses(file,file.getName()));
			else
				if(file.getName().toLowerCase().endsWith(".java") && 
						!file.getName().toLowerCase().equals("package-info.java"))
					result.add(file.getName().substring(0, file.getName().lastIndexOf(".")));
				
		}
		
		return result;
		
	}
	
	public static List<String> listClasses(File srcFolder,String name){
		List<String> result= new ArrayList<String>();
		
		File[] files=srcFolder.listFiles();
		
		
		for(File file:files) {
			if(file.isDirectory())
				result.addAll(listClasses(file,name+"."+file.getName()));
			else
				if(file.getName().toLowerCase().endsWith(".java") && 
						!file.getName().toLowerCase().equals("package-info.java"))
					result.add(name+"."+file.getName().substring(0, file.getName().lastIndexOf(".")));
				
		}
		
		return result;
		
	}

}
