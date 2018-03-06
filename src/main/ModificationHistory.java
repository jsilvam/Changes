package main;

import java.util.HashMap;
import java.util.Map;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;

public class ModificationHistory {
	
	private Map<SourceCodeChange,Boolean> changeHistory;
	private Map<String,SourceCodeChange> createdMethods;
	private Map<String,SourceCodeChange> deletedMethods;
	private Map<String,SourceCodeChange> createdFields;
	private Map<String,SourceCodeChange> deletedFields;
	

	public ModificationHistory() {
		changeHistory = new HashMap<SourceCodeChange,Boolean>();
		createdMethods = new HashMap<String,SourceCodeChange>();
		deletedMethods = new HashMap<String,SourceCodeChange>();
		createdFields = new HashMap<String,SourceCodeChange>();
		deletedFields = new HashMap<String,SourceCodeChange>();
	}
	
	public void addChange(SourceCodeChange sc) {
		String field;
		switch(sc.getChangeType()) {
		case ADDITIONAL_FUNCTIONALITY:
			createdMethods.put(sc.getChangedEntity().getUniqueName(), sc);
			break;
		case REMOVED_FUNCTIONALITY:
			deletedMethods.put(sc.getChangedEntity().getUniqueName(), sc);
			break;
		case ADDITIONAL_OBJECT_STATE:
			field=sc.getChangedEntity().getUniqueName();
			field=field.substring(0, field.indexOf(" : "));
			System.out.println(field);
			createdFields.put(field, sc);
			break;
		case REMOVED_OBJECT_STATE:
			field=sc.getChangedEntity().getUniqueName();
			field=field.substring(0, field.indexOf(" : "));
			System.out.println(field);
			deletedFields.put(sc.getChangedEntity().getUniqueName(), sc);
			break;
		default:
			break;
		}
		this.changeHistory.put(sc,false);
	}
	
	public void setCheckedChange(SourceCodeChange sc) throws Exception{
		if(this.changeHistory.containsKey(sc))
			this.changeHistory.put(sc,true);
		else
			throw new Exception("Source Code Change not Found: "+sc+". Root Entity: "+sc.getRootEntity());
		
	}
	
	public void setUncheckedChange(SourceCodeChange sc) throws Exception {
		if(this.changeHistory.containsKey(sc))
			this.changeHistory.put(sc,false);
		else
			throw new Exception("Source Code Change not Found");
		
	}
	
	public boolean isChecked(SourceCodeChange sc) {
		return this.changeHistory.containsKey(sc) || this.changeHistory.get(sc);
	}
	
	public boolean containsChange(SourceCodeChange sc) {
		return this.changeHistory.containsKey(sc);
	}

	public Map<SourceCodeChange, Boolean> getChangeHistory() {
		return changeHistory;
	}

	public SourceCodeChange getCreatedMethod(String signature) {
		return createdMethods.get(signature);
	}

	public SourceCodeChange getDeletedMethod(String signature) {
		return deletedMethods.get(signature);
	}

	public SourceCodeChange getCreatedField(String signature) {
		return createdFields.get(signature);
	}

	public SourceCodeChange getDeletedField(String signature) {
		return deletedFields.get(signature);
	}
	
	
}
