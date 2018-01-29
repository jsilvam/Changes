package main;

import java.io.IOException;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import utils.Refactorings;

import utils.CSV;

public class Analyser {
	
	private Refactorings refactorings;
	private String commit; 

	public Analyser(Refactorings refactorings, String commit) {
		this.refactorings = refactorings;
		this.commit=commit;
	}
	
	public void addNewClass(String clazz, CSV csv) throws IOException {
		csv.addLine(commit, "ADDITIONAL_CLASS", clazz, "", clazz);
	}
	
	public void addRemovedClass(String clazz, CSV csv) throws IOException {
		csv.addLine(commit, "ADDITIONAL_CLASS", clazz, clazz, "");
	}
	
	public void analyse(List<SourceCodeChange> changes, CSV csv, String classBefore, String classAfter) throws IOException {
		String aux;
		for(SourceCodeChange change:changes) {
			String changedEntity=change.getChangedEntity().getUniqueName();
			switch(change.getLabel()) {
				case "ADDITIONAL_CLASS":
					if(!refactorings.getAddedClasses().contains(changedEntity) &&
							!refactorings.getChangedClassSignatures().containsValue(changedEntity)) {
						csv.addLine(commit, change.getLabel(), changedEntity, "", classAfter);
					}
					break;
				case "ADDITIONAL_FUNCTIONALITY":
					if(!refactorings.getAddedMethods().contains(changedEntity) &&
							!refactorings.getMovedMethods().containsValue(changedEntity)){
						csv.addLine(commit, change.getLabel(), changedEntity, classBefore, classAfter);
					}
					break;
				case "ADDITIONAL_OBJECT_STATE":
					aux=changedEntity.substring(0, changedEntity.indexOf(" "));
					if(!refactorings.getMovedAtributes().containsValue(aux)){
						csv.addLine(commit, change.getLabel(), changedEntity, classBefore, classAfter);
					}
					break;
				case "REMOVED_CLASS":
					if(!refactorings.getChangedClassSignatures().containsKey(changedEntity)){
						csv.addLine(commit, change.getLabel(), changedEntity, classBefore, "");
					}
					break;
				case "REMOVED_FUNCTIONALITY":
					if(!refactorings.getRemovedMethods().contains(changedEntity) &&
							!refactorings.getMovedMethods().containsKey(changedEntity)){
						csv.addLine(commit, change.getLabel(), changedEntity, classBefore, classAfter);
					}
					break;
				case "REMOVED_OBJECT_STATE":
					aux=changedEntity.substring(0, changedEntity.indexOf(" "));
					if(!refactorings.getMovedAtributes().containsKey(aux)){
						csv.addLine(commit, change.getLabel(), changedEntity, classBefore, classAfter);
					}
					break;
					//refactorings does nothing
				case "CLASS_RENAMING":
					break;
				case "METHOD_RENAMING":
					break;
				//Other cases
				default:
					csv.addLine(commit, change.getLabel(), changedEntity, classBefore, classAfter);
			}
		}
	}
	
	
	
	

}
