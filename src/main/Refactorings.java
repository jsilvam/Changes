package main;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Refactorings {
	
	private Map<String, String> changedClassSignatures;
	private Map<String, String> movedAttributes;
	private Map<String, String> movedMethods;
	private Map<String, String> extractedMethods;
	private Map<String, String> inlinedMethods;
	
	private List<String> addedClasses;
	
	private List<String> changedMethods; //very low probability of use
	
	
	private String parent;
	
	public Refactorings(String csvPath,String commit) throws FileNotFoundException {
		init(csvPath, commit);
	}
	
	private void init(String csvPath,String commit) throws FileNotFoundException {
		
		this.changedClassSignatures = new HashMap<String,String>();
		this.movedMethods = new HashMap<String,String>();
		this.movedAttributes = new HashMap<String,String>();
		this.extractedMethods = new HashMap<String,String>();
		this.inlinedMethods = new HashMap<String,String>();
		
		this.addedClasses = new ArrayList<String>();
		this.changedMethods = new ArrayList<String>();
		
		
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
						key=in.next();
						value=in.next();
						this.extractedMethods.put(key, value);
						break;
					case "Inline Method":
						value=in.next();
						key=in.next();
						this.inlinedMethods.put(key, value);
						break;
					case "Rename Method"://when the method e also moved, it is saved as moved method. When is not, does nothing.
						key=in.next();
						value=in.next();
						String before=key.substring(0, key.lastIndexOf("."));
						String after=value.substring(0, value.indexOf("."));
						if(!before.equals(after))
							this.movedMethods.put(key, value);
						break;
					case "Pull Up Method":
					case "Push Down Method":
					case "Move Method":
						key=in.next();
						value=in.next();
						this.movedMethods.put(key, value);
						break;
					case "Pull Up Attribute":
					case "Push Down Attribute":
					case "Move Attribute":
						key=in.next();
						value=in.next();
						this.movedAttributes.put(key, value);
						break;
					case "Rename Class":
					case "Move Class":
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
	
	public boolean isMovedMethod(String signature) {
		return (this.movedMethods.containsKey(signature) || this.movedMethods.containsValue(signature));
	}
	
	public boolean isMovedAttribute(String signature) {
		return (this.movedAttributes.containsKey(signature) || this.movedAttributes.containsValue(signature));
	}
	
	//needs better name
	public boolean isExtractedMethod(String signature) {
		return (this.extractedMethods.containsKey(signature) || this.extractedMethods.containsValue(signature));
	}
	
	//needs better name
	public boolean isInlinedMethod(String signature) {
		return (this.inlinedMethods.containsKey(signature) || this.inlinedMethods.containsValue(signature));
	}

	public Map<String, String> getChangedClassSignatures() {
		return changedClassSignatures;
	}

	public Map<String, String> getMovedAttributes() {
		return movedAttributes;
	}

	public Map<String, String> getMovedMethods() {
		return movedMethods;
	}

	public List<String> getAddedClasses() {
		return addedClasses;
	}

	public Map<String, String> getExtractedMethods() {
		return extractedMethods;
	}

	public Map<String, String> getInlinedMethods() {
		return inlinedMethods;
	}

	public List<String> getChangedMethods() {
		return changedMethods;
	}

	public String getParent() {
		return parent;
	}
	
}
