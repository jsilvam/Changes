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
				}else if(scc.getChangedEntity().getType().isMethod()){
					String method = scc.getChangedEntity().getUniqueName();
					if (refactorings.isMovedMethod(method))  
						analiseMovedAttribute(scc, method);
					else
						this.sourceCodeChanges.add(scc);
				}else if(scc.getChangedEntity().getType().isField()){
					String field = scc.getChangedEntity().getUniqueName();
					int index = field.indexOf(" : ");
					field = field.substring(0, index);
					if (refactorings.isMovedAttribute(field))  
						analiseMovedAttribute(scc, field);
					else
						this.sourceCodeChanges.add(scc);
				}else if(scc.getChangedEntity().getType().isClass()){
					String clazz = scc.getChangedEntity().getUniqueName();
					if(!refactorings.isRefactoredClass(clazz))
						this.sourceCodeChanges.add(scc);
				}else
					this.sourceCodeChanges.add(scc);
			}	
		}
	}
	
	//verify only if the entities if entities are equal, not similar.
	private void analiseExtractedMethod(StructureEntityVersion root) {
		String createdMethod = this.refactorings.getExtractedMethodSignature(root.getUniqueName());
		SourceCodeChange method=modificationHistory.getCreatedMethod(createdMethod);
		Enumeration<Node> body = method.getBodyStructure().preorderEnumeration();
		List<SourceCodeChange> changes=root.getSourceCodeChanges();
		
		try {
			while(body.hasMoreElements()) {
				Node node=body.nextElement();
				node.disableMatched();
				for(SourceCodeChange scc: changes) {
					if(node.getEntity().equals(scc.getChangedEntity())) {	
						modificationHistory.setCheckedChange(scc);
						changes.remove(scc);
						node.enableMatched();
					}
				}
			}
			modificationHistory.setCheckedChange(method);
		} catch (Exception e) {
			e.printStackTrace();
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
		String deletedMethod = this.refactorings.getInlinedMethodSignature(root.getUniqueName());
		SourceCodeChange method=modificationHistory.getDeletedMethod(deletedMethod);
		Enumeration<Node> body = method.getBodyStructure().preorderEnumeration();
		List<SourceCodeChange> changes=root.getSourceCodeChanges();
		

		try {
			while(body.hasMoreElements()) {
				Node node=body.nextElement();
				node.disableMatched();
				for(SourceCodeChange scc: changes) {
					if(node.getEntity().equals(scc.getChangedEntity())) {
						modificationHistory.setCheckedChange(scc);
						changes.remove(scc);
						node.enableMatched();		
				}
			}
		}
		modificationHistory.setCheckedChange(method);
		} catch (Exception e) {
			e.printStackTrace();
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
	
	private void analiseMovedAttribute(SourceCodeChange oldField, String oldSignature) {
		String newSignature = this.refactorings.getMovedAttributeSignature(oldSignature);
		if(newSignature == null)
			return;
		
		SourceCodeChange newField = this.modificationHistory.getCreatedField(newSignature);
		StructureEntityVersion rootEntity = newField.getStructureEntityVersion();
	    List<SourceCodeChange> changes = extractChanges(oldField.getDeclarationStructure(), newField.getDeclarationStructure(), rootEntity); 
	    this.sourceCodeChanges.addAll(changes);
	}
	
	private void analiseMovedMethod(SourceCodeChange oldMethod, String oldSignature) {
		String newSignature = this.refactorings.getMovedAttributeSignature(oldSignature);
		if(newSignature == null)
			return;
		
		SourceCodeChange newMethod = this.modificationHistory.getCreatedField(newSignature);
	    StructureEntityVersion rootEntity = newMethod.getStructureEntityVersion();
	    List<SourceCodeChange> changes = extractChanges(oldMethod.getDeclarationStructure(), newMethod.getDeclarationStructure(), rootEntity); 
	    this.sourceCodeChanges.addAll(changes);
	    changes = extractChanges(oldMethod.getBodyStructure(), newMethod.getBodyStructure(), rootEntity); 
	    this.sourceCodeChanges.addAll(changes);
	}
	
	
	private List<SourceCodeChange> extractChanges(Node left, Node right, StructureEntityVersion rootEntity) {
		Injector injector = Guice.createInjector(new JavaChangeDistillerModule());
	    DistillerFactory df = injector.getInstance(DistillerFactory.class);
        Distiller distiller = df.create(rootEntity);
        distiller.extractClassifiedSourceCodeChanges(left, right);
        return distiller.getSourceCodeChanges();
    }
}
