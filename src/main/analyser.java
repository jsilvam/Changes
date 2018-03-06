package main;

import java.util.Enumeration;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;

public class analyser {
	
	private Refactorings refactorings;
	private ModificationHistory modificationHistory;
	
	public analyser(Refactorings refactorings, ModificationHistory modificationHistory) {
		this.refactorings = refactorings;
		this.modificationHistory = modificationHistory;
	}
	
	
	public void analise() {
		for(SourceCodeChange sc: modificationHistory.getChangeHistory().keySet()) {
			if(!modificationHistory.isChecked(sc)) {
				if(refactorings.isExtractedMethod(sc.getRootEntity().getUniqueName())) {
					analiseExtractedMethod(sc.getRootEntity());
				}
			}	
		}
	}
	
	private void analiseExtractedMethod(StructureEntityVersion root) {
		List<SourceCodeChange> changes=root.getSourceCodeChanges();
		SourceCodeChange method=modificationHistory.getCreatedMethod(root.getUniqueName());
		Enumeration<Node> body = method.getBodyStructure().preorderEnumeration();
		
		while(body.hasMoreElements()) {
			Node node=body.nextElement();
			node.disableMatched();
			for(SourceCodeChange sc: changes) {
				if(node.getEntity().equals(sc.getChangedEntity())) {
					try {
						modificationHistory.setCheckedChange(sc);
						modificationHistory.setCheckedChange(method);
						changes.remove(sc);
						node.enableMatched();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		//verificar o classificador do change distiller para classificar.
		
		
	}

}
