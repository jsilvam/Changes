package main;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import com.google.inject.Guice;
import com.google.inject.Injector;

import ch.uzh.ifi.seal.changedistiller.JavaChangeDistillerModule;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaSourceCodeChangeClassifier;
import ch.uzh.ifi.seal.changedistiller.distilling.Distiller;
import ch.uzh.ifi.seal.changedistiller.distilling.DistillerFactory;
import ch.uzh.ifi.seal.changedistiller.distilling.SourceCodeChangeClassifier;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;

public class analyser {
	
	private Refactorings refactorings;
	private ModificationHistory modificationHistory;
	private List<SourceCodeChange> sourceCodeChanges;
	private JavaSourceCodeChangeClassifier classifier;
	
	public analyser(Refactorings refactorings, ModificationHistory modificationHistory) {
		this.refactorings = refactorings;
		this.modificationHistory = modificationHistory;
		this.sourceCodeChanges = new LinkedList<SourceCodeChange>();
		this.classifier = new JavaSourceCodeChangeClassifier();
	}
	
	
	public void analise() {
		for(SourceCodeChange scc: modificationHistory.getChangeHistory().keySet()) {
			if(!modificationHistory.isChecked(scc)) {
				if(refactorings.isExtractedMethod(scc.getRootEntity().getUniqueName())) {
					analiseExtractedMethod(scc.getRootEntity());
				}else if (refactorings.isInlinedMethod(scc.getRootEntity().getUniqueName())) {
					analiseInlinedMethod(scc.getRootEntity());
				}else if(scc.getChangedEntity().getType().isField()){
					scc.getChangedEntity();
					if (refactorings.isMovedAttribute("")) {
					}
				}
				
			}	
		}
	}
	
	//verify only if the entities if entities are equal, not similar.
	private void analiseExtractedMethod(StructureEntityVersion root) {
		List<SourceCodeChange> changes=root.getSourceCodeChanges();
		SourceCodeChange method=modificationHistory.getCreatedMethod(root.getUniqueName());
		Enumeration<Node> body = method.getBodyStructure().preorderEnumeration();
		
		while(body.hasMoreElements()) {
			Node node=body.nextElement();
			node.disableMatched();
			for(SourceCodeChange scc: changes) {
				if(node.getEntity().equals(scc.getChangedEntity())) {
					try {
						modificationHistory.setCheckedChange(scc);
						modificationHistory.setCheckedChange(method);
						changes.remove(scc);
						node.enableMatched();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		body = method.getBodyStructure().preorderEnumeration();
		while(body.hasMoreElements()) {
			Node node=body.nextElement();
			if(!node.isMatched()) {
				Node parent=(Node) node.getParent();
				SourceCodeChange scc = classifier.classify(
						new Insert(method.getStructureEntityVersion(),node.getEntity(),parent.getEntity()));
				if ((scc != null) && !sourceCodeChanges.contains(scc)) {
					sourceCodeChanges.add(scc);
				}	
			}
		}
		
		for(SourceCodeChange scc: changes) {
			if (!sourceCodeChanges.contains(scc)) {
				sourceCodeChanges.add(scc);
			}
			
		}	
	}
	
	//verify only if the entities if entities are equal, not similar.
	private void analiseInlinedMethod(StructureEntityVersion root) {
		List<SourceCodeChange> changes=root.getSourceCodeChanges();
		SourceCodeChange method=modificationHistory.getDeletedMethod(root.getUniqueName());
		Enumeration<Node> body = method.getBodyStructure().preorderEnumeration();
		
		while(body.hasMoreElements()) {
			Node node=body.nextElement();
			node.disableMatched();
			for(SourceCodeChange scc: changes) {
				if(node.getEntity().equals(scc.getChangedEntity())) {
					try {
						modificationHistory.setCheckedChange(scc);
						modificationHistory.setCheckedChange(method);
						changes.remove(scc);
						node.enableMatched();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		body = method.getBodyStructure().preorderEnumeration();
		while(body.hasMoreElements()) {
			Node node=body.nextElement();
			if(!node.isMatched()) {
				Node parent=(Node) node.getParent();
				SourceCodeChange scc = classifier.classify(
						new Delete(method.getStructureEntityVersion(),node.getEntity(),parent.getEntity()));
				if (!sourceCodeChanges.contains(scc)) {
					sourceCodeChanges.add(scc);
				}	
			}
		}
		
		for(SourceCodeChange scc: changes) {
			if ((scc != null) && !sourceCodeChanges.contains(scc)) {
				sourceCodeChanges.add(scc);
			}
			
		}	
	}
	
	private void analiseMovedAttribute(SourceCodeChange root) {
		Injector injector = Guice.createInjector(new JavaChangeDistillerModule());
	    DistillerFactory df = injector.getInstance(DistillerFactory.class);
	    
	    Distiller distille = df.create(root.getStructureEntityVersion());
//	    distille.extractClassifiedSourceCodeChanges(scc1.getDeclarationStructure(), scc2.getDeclarationStructure());
	}
}
