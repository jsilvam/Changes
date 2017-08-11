package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Refactorings {
	
	private Map<String, String> changedClasses;
	
	public Refactorings(String csvPath,String commit) throws FileNotFoundException {
		
		Scanner in = new Scanner(new FileReader(csvPath)).useDelimiter(";");
		
		in.nextLine();
		while(in.hasNext()) {
			in.next();
			if(in.next().equals(commit)) {
				in.next();
				in.next();
				in.next();
				String refactoring=in.next();
				if(refactoring.equals("Rename Class")) {
					String key=in.next();
					in.next();
					String value=in.next();
					changedClasses.put(key, value);
				}else if(refactoring.equals("Move Class")) {
					in.next();
					String key=in.next();
					in.next();
					String value=in.next();
					changedClasses.put(key, value);
				}
			}
			in.nextLine();
		}
		in.close();
	}

	public Map<String, String> getChangedClasses() {
		return changedClasses;
	}
	
	
}
