package main;

import java.util.HashMap;
import java.util.Map;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;

public class ModificationsHistory {
	
	Map<SourceCodeChange,Boolean> changesHistory;
	Map<String,SourceCodeChange> createdMethods;
	Map<String,SourceCodeChange> deletedMethods;
	Map<String,SourceCodeChange> createdFields;
	Map<String,SourceCodeChange> deletedFields;
	

	public ModificationsHistory() {
		changesHistory = new HashMap<SourceCodeChange,Boolean>();
		createdMethods = new HashMap<String,SourceCodeChange>();
		deletedMethods = new HashMap<String,SourceCodeChange>();
		createdFields = new HashMap<String,SourceCodeChange>();
		deletedFields = new HashMap<String,SourceCodeChange>();
	}
	
	public void addChange(SourceCodeChange sc) {
		this.addChange(sc,false);
	}
	
	public void addChange(SourceCodeChange sc, boolean checked) {
		this.changesHistory.put(sc,checked);
	}
	
	public boolean containsChange(SourceCodeChange sc) {
		return this.changesHistory.containsKey(sc);
	}
}
