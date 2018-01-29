package main;

import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import utils.Refactorings;

public class Analyser {
	
	private Refactorings refactorings;

	public Analyser(Refactorings refactorings) {
		this.refactorings = refactorings;
	}
	
	public void addNewClass(String clazz) {
		
	}
	
	public void addRemovedClass(String clazz) {
		
	}
	
	public void analyse(List<SourceCodeChange> changes) {
		for(SourceCodeChange change:changes) {
			switch(change.getLabel()) {
				case "ADDITIONAL_CLASS":
					break;
				case "":
					break;
				case "ADDITIONAL_FUNCTIONALITY":
					break;
				default:
			}
		}
	}
	
	
	
	

}
