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
	
	private Map<String, String> changedClassSignatures;
	private List<String> addedClasses;
	private List<String> changedMethods;
	private List<String> addedMethods;
	private List<String> removedMethods;
	private List<String> movedAtributes;
	
	private String parent;
	
	public Refactorings(String csvPath,String commit) throws FileNotFoundException {
		init(csvPath, commit);
	}
	
	private void init(String csvPath,String commit) throws FileNotFoundException {
		
		Scanner in = new Scanner(new FileReader(csvPath)).useDelimiter(";");
		
		boolean flag=false;  //responsible for stopping the loop when there is no more entries of the selected commit
		in.nextLine();
		while(in.hasNext()) {
			in.next();
			if(in.next().equals(commit)) {
				flag=true;
				this.parent=in.next();
				in.next();
				in.next();
				String refactoring=in.next();
				if(refactoring.equals("Rename Class")) {
					String key=in.next();
					in.next();
					String value=in.next();
					changedClassSignatures.put(key, value);
				}else if(refactoring.equals("Move Class")) {
					in.next();
					String key=in.next();
					in.next();
					String value=in.next();
					changedClassSignatures.put(key, value);
				}else if(refactoring.equals("Move And Rename Class")) {
					in.next();
					String key=in.next();
					in.next();
					String value=in.next();
					changedClassSignatures.put(key, value);
				}
			}else 
				if(flag)break;
			
			in.nextLine();
		}
		in.close();
	}
	
	public String getParent() {
		return this.parent;
	}

	public Map<String, String> getChangedClassSignatures() {
		return this.changedClassSignatures;
	}
	
	
}
