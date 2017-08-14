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
	private Map<String, String> movedAtributes;
	private Map<String, String> movedMethods;
	
	private List<String> addedClasses;
	private List<String> addedMethods;
	private List<String> removedMethods;
	
	private List<String> changedMethods; //very low probability of use
	
	
	private String parent;
	
	public Refactorings(String csvPath,String commit) throws FileNotFoundException {
		init(csvPath, commit);
	}
	
	private void init(String csvPath,String commit) throws FileNotFoundException {
		
		this.changedClassSignatures = new HashMap<String,String>();
		this.movedMethods = new HashMap<String,String>();
		this.movedAtributes = new HashMap<String,String>();
		
		this.addedClasses = new ArrayList<String>();
		this.addedMethods = new ArrayList<String>();
		this.changedMethods = new ArrayList<String>();
		this.addedMethods = new ArrayList<String>();
		this.removedMethods = new ArrayList<String>();
		
		
		Scanner in = new Scanner(new FileReader(csvPath)).useDelimiter(";");
		
		boolean flag=false;  //responsible for stopping the loop when there is no more entries of the selected commit
		String key,value;
		in.nextLine();
		while(in.hasNext()) {
			in.next();
			if(in.next().equals(commit)) {
				flag=true;
				this.parent=in.next();
				in.next();
				switch(in.next()) {
					case "Extract Method":
						in.next();
						this.addedMethods.add(in.next());
						break;
					case "Inline Method":
						this.removedMethods.add(in.next());
						break;
					case "Rename Method"://There is cases where the method were also moved, analyse it later. 
						break;
					case "Move Method":
						break;
					case "Pull Up Method":
						break;
					case "Push Down Method":
						break;
					case "Move Attribute":
						break;
					case "Pull Up Attribute":
						break;
					case "Push Down Attribute":
						break;
					case "Rename Class":
						key=in.next();
						value=in.next();
						changedClassSignatures.put(key, value);
						break;
					case "Move Class":
						key=in.next();
						value=in.next();
						changedClassSignatures.put(key, value);
						break;
					case "Move And Rename Class":
						key=in.next();
						value=in.next();
						changedClassSignatures.put(key, value);
						break;
					case "Extract Superclass":
						break;
					case "Extract Interface":
						break;
					default:
					
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
