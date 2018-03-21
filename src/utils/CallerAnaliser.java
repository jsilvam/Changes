package utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import main.ModificationHistory;
import main.Refactorings;

public class CallerAnaliser {
	
	private LinkedList<String> shortNames;
	
	
	
	public CallerAnaliser() {
		shortNames = new LinkedList<String>();
	}

	
	private String getShortName(String fullName) {
		int lastIndex = fullName.indexOf("(");		
		if(lastIndex<0)
			return fullName.substring(fullName.lastIndexOf('.')+1);
		else
			return fullName.substring(fullName.lastIndexOf('.')+1,lastIndex);
	}
	
	public void extractShortNames(Refactorings refactorings) {
		Collection<String> signatures = refactorings.getInlinedMethods().values();
		for(String signature: signatures)
			shortNames.add(getShortName(signature));
		
		signatures = refactorings.getMovedMethods().values();
		for(String signature: signatures)
			shortNames.add(getShortName(signature));
		
		signatures = refactorings.getMovedAttributes().values();
		for(String signature: signatures)
			shortNames.add(getShortName(signature));
		
		Set<String> signatures2 = refactorings.getMovedMethods().keySet();
		for(String signature: signatures2)
			shortNames.add(getShortName(signature));
		
		signatures2 = refactorings.getMovedAttributes().keySet();
		for(String signature: signatures2)
			shortNames.add(getShortName(signature));
	}
	
	public void markCallers(ModificationHistory mh) {
		try {
			for(SourceCodeChange scc: mh.getChangeHistory().keySet()) {
				SourceCodeEntity sce = scc.getChangedEntity();
				if(sce.getType().isStatement()) 
					for(String shortName: shortNames) 
						if(sce.getUniqueName().contains(shortName)) {
							mh.setCheckedChange(scc);
							break;
						}	
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
