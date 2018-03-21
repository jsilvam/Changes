package main;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.google.inject.Guice;
import com.google.inject.Injector;

import ch.uzh.ifi.seal.changedistiller.JavaChangeDistillerModule;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaSourceCodeChangeClassifier;
import ch.uzh.ifi.seal.changedistiller.distilling.Distiller;
import ch.uzh.ifi.seal.changedistiller.distilling.DistillerFactory;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.matching.measure.LevenshteinSimilarityCalculator;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.matching.measure.NGramsCalculator;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.matching.measure.StringSimilarityCalculator;
import utils.CallerAnaliser;

public class analyser {
	
	private Refactorings refactorings;
	private ModificationHistory modificationHistory;
	private List<SourceCodeChange> sourceCodeChanges;
	private	StringSimilarityCalculator strSimCalc;
	private double lTh;
	private JavaSourceCodeChangeClassifier classifier;
	
	
	public analyser(Refactorings refactorings, ModificationHistory modificationHistory) {
		this.refactorings = refactorings;
		this.modificationHistory = modificationHistory;
		this.sourceCodeChanges = new LinkedList<SourceCodeChange>();
		this.strSimCalc = new NGramsCalculator(2);
		this.classifier = new JavaSourceCodeChangeClassifier();
		this.lTh= 0.6;
	}
	
	
	public void analise() {
		CallerAnaliser ca = new CallerAnaliser();
		ca.extractShortNames(refactorings);
		ca.markCallers(modificationHistory);
		
		for(SourceCodeChange scc: modificationHistory.getChangeHistory().keySet()) {
			if(!modificationHistory.isChecked(scc)) {
				if(refactorings.isExtractedMethod(scc.getRootEntity().getUniqueName())) {
					analiseExtractedMethod(scc.getRootEntity());
				}else if (refactorings.isInlinedMethod(scc.getRootEntity().getUniqueName())) {
					analiseInlinedMethod(scc.getRootEntity());
				}else if(scc.getChangedEntity().getType().isMethod()){
					String method = scc.getChangedEntity().getUniqueName();
					if (refactorings.isMovedMethod(method))  
						analiseMovedMethod(scc, method);
					else if(scc.getChangeType()!=ChangeType.METHOD_RENAMING
							&& !refactorings.isExtractedMethod(scc.getChangedEntity().getUniqueName())
							&& !refactorings.isInlinedMethod(scc.getChangedEntity().getUniqueName()))
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
	
	//verify if the entities are equal or, if it is a leaf, if it is similar
	//Do I remove the leaf verification?
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
					if(isSameEntity(node,scc)
							//Verify only parent's type or full data?
							&& isSameEntityType(( (Node)node.getParent() ).getEntity(),scc.getParentEntity())) {	
						modificationHistory.setCheckedChange(scc);
						changes.remove(scc);
						node.enableMatched();
						break;
					}
				}
			}
		
			body = method.getBodyStructure().preorderEnumeration();
			
			while(body.hasMoreElements()) {
				Node node=body.nextElement();
				if(!node.isMatched()) {
					for(SourceCodeChange scc: changes) {
						if(!this.modificationHistory.isChecked(scc)
								&& isSameEntityType(node.getEntity(),scc.getChangedEntity())
								&& similarity(node,scc) >= this.lTh) {
							if(!isSameEntityType(( (Node)node.getParent() ).getEntity(),scc.getParentEntity())) {
								Node parent=(Node) node.getParent();
								SourceCodeChange scc1 = classifier.classify(  //new Move(ChangeType.STATEMENT_PARENT_CHANGE,
										new Move(method.getStructureEntityVersion(),
										scc.getChangedEntity(),
										node.getEntity(),
										scc.getParentEntity(),
										((Node)node.getParent()).getEntity()));
								if ((scc1 != null) && !sourceCodeChanges.contains(scc1)) {
									sourceCodeChanges.add(scc1);
									modificationHistory.setCheckedChange(scc);
									changes.remove(scc);
									node.enableMatched();
								}
							}
							if(!isSameEntity(node, scc)) {
								Node parent=(Node) node.getParent();
								SourceCodeChange scc1 = classifier.classify(
										new Update(method.getStructureEntityVersion(),
												node.getEntity(),
												scc.getChangedEntity(),
												parent.getEntity()));
								if ((scc1 != null) && !sourceCodeChanges.contains(scc1)) {
									sourceCodeChanges.add(scc1);
									modificationHistory.setCheckedChange(scc);
									changes.remove(scc);
									node.enableMatched();
								}
							}
							break;
						}
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
	
	//verify if the entities are equal or, if it is a leaf, if it is similar
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
					if(isSameEntity(node,scc)
							//Verify only parent's type or full data?
							&& isSameEntityType(( (Node)node.getParent() ).getEntity(),scc.getParentEntity())) {	
						modificationHistory.setCheckedChange(scc);
						changes.remove(scc);
						node.enableMatched();
						break;
					}
				}
			}
		
			body = method.getBodyStructure().preorderEnumeration();
			
			while(body.hasMoreElements()) {
				Node node=body.nextElement();
				if(!node.isMatched()) {
					for(SourceCodeChange scc: changes) {
						if(!this.modificationHistory.isChecked(scc)
								&& isSameEntityType(node.getEntity(),scc.getChangedEntity())
								&& similarity(node,scc) >= this.lTh) {
							if(!isSameEntityType(( (Node)node.getParent() ).getEntity(),scc.getParentEntity())) {
								Node parent=(Node) node.getParent();
								SourceCodeChange scc1 = classifier.classify(  //new Move(ChangeType.STATEMENT_PARENT_CHANGE,
										new Move(method.getStructureEntityVersion(),
										scc.getChangedEntity(),
										node.getEntity(),
										scc.getParentEntity(),
										((Node)node.getParent()).getEntity()));
								if ((scc1 != null) && !sourceCodeChanges.contains(scc1)) {
									sourceCodeChanges.add(scc1);
									modificationHistory.setCheckedChange(scc);
									changes.remove(scc);
									node.enableMatched();
								}
							}
							if(!isSameEntity(node, scc)) {
								Node parent=(Node) node.getParent();
								SourceCodeChange scc1 = classifier.classify(
										new Update(method.getStructureEntityVersion(),
												node.getEntity(),
												scc.getChangedEntity(),
												parent.getEntity()));
								if ((scc1 != null) && !sourceCodeChanges.contains(scc1)) {
									sourceCodeChanges.add(scc1);
									modificationHistory.setCheckedChange(scc);
									changes.remove(scc);
									node.enableMatched();
								}
							}
							break;
						}
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
		String newSignature = this.refactorings.getMovedMethodSignature(oldSignature);
		if(newSignature == null)
			return;
		
		SourceCodeChange newMethod = this.modificationHistory.getCreatedMethod(newSignature);
	    StructureEntityVersion rootEntity = newMethod.getStructureEntityVersion();
	    List<SourceCodeChange> changes = extractChanges(oldMethod.getDeclarationStructure(), newMethod.getDeclarationStructure(), rootEntity); 
	    //still have to filter the rename changes
	    this.sourceCodeChanges.addAll(changes);
	    changes = extractChanges(oldMethod.getBodyStructure(), newMethod.getBodyStructure(), rootEntity); 
	    this.sourceCodeChanges.addAll(changes);
	}
	
	private boolean isLeafUpdate(Node node, SourceCodeChange scc) {
		if(node.isLeaf() && isSameEntityType(node.getEntity(),scc.getChangedEntity())) {
			return similarity(node,scc) >= this.lTh;
		}
		return false;
	}
	
	private double similarity(Node node, SourceCodeChange scc) {
		SourceCodeEntity sce1= node.getEntity();
		SourceCodeEntity sce2= scc.getChangedEntity();
		return this.strSimCalc.calculateSimilarity(sce1.getUniqueName(), sce2.getUniqueName());
	}
	
	private boolean isSameEntityType(SourceCodeEntity entity1, SourceCodeEntity entity2) {
		EntityType et1 = entity1.getType();
		EntityType et2 = entity2.getType();
		return et1.equals(et2);
	}
	
	private boolean isSameEntity(Node node, SourceCodeChange scc) {
		SourceCodeEntity e1 = node.getEntity();
		SourceCodeEntity e2 = scc.getChangedEntity();
		return new EqualsBuilder().append(e1.getUniqueName(), e2.getUniqueName()).append(e1.getType(), e2.getType())
                .append(e1.getModifiers(), e2.getModifiers()).isEquals();
	}
	
	private List<SourceCodeChange> extractChanges(Node left, Node right, StructureEntityVersion rootEntity) {
		Injector injector = Guice.createInjector(new JavaChangeDistillerModule());
	    DistillerFactory df = injector.getInstance(DistillerFactory.class);
        Distiller distiller = df.create(rootEntity);
        distiller.extractClassifiedSourceCodeChanges(left, right);
        return distiller.getSourceCodeChanges();
    }
}
