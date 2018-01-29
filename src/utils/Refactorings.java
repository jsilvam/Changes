package utils;

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
					case "Rename Method"://when the method e also moved, it is saved as moved method. When is not, does nothing.
						key=in.next();
						value=in.next();
						String before=key.substring(0, key.lastIndexOf("."));
						String after=value.substring(0, value.indexOf("."));
						if(!before.equals(after))
							this.movedMethods.put(key, value);
						break;
					case "Move Method":
						key=in.next();
						value=in.next();
						this.movedMethods.put(key, value);
						break;
					case "Pull Up Method":
						key=in.next();
						value=in.next();
						this.movedMethods.put(key, value);
						break;
					case "Push Down Method":
						key=in.next();
						value=in.next();
						this.movedMethods.put(key, value);
						break;
					case "Move Attribute":
						key=in.next();
						value=in.next();
						this.movedAtributes.put(key, value);
						break;
					case "Pull Up Attribute":
						key=in.next();
						value=in.next();
						this.movedAtributes.put(key, value);
						break;
					case "Push Down Attribute":
						key=in.next();
						value=in.next();
						this.movedAtributes.put(key, value);
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
						in.next();
						this.addedClasses.add(in.next());
						break;
					case "Extract Interface":
						in.next();
						this.addedClasses.add(in.next());
						break;
					default:
					
				}
				
			}else 
				if(flag)break;
			
			in.nextLine();
		}
		in.close();
	}

	public Map<String, String> getChangedClassSignatures() {
		return changedClassSignatures;
	}

	public Map<String, String> getMovedAtributes() {
		return movedAtributes;
	}

	public Map<String, String> getMovedMethods() {
		return movedMethods;
	}

	public List<String> getAddedClasses() {
		return addedClasses;
	}

	public List<String> getAddedMethods() {
		return addedMethods;
	}

	public List<String> getRemovedMethods() {
		return removedMethods;
	}

	public List<String> getChangedMethods() {
		return changedMethods;
	}

	public String getParent() {
		return parent;
	}
	
}
