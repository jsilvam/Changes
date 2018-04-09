package analyser;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.google.inject.Guice;
import com.google.inject.Injector;

import analyser.callerAnalyser.CallerAnalyser;
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
import ch.uzh.ifi.seal.changedistiller.treedifferencing.matching.measure.NGramsCalculator;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.matching.measure.StringSimilarityCalculator;
import main.ModificationHistory;
import main.Refactorings;

public class Analyser {
	
	private Refactorings refactorings;
	private ModificationHistory modificationHistory;
	private List<SourceCodeChange> verifiedSourceCodeChanges;
	private	StringAnalyser strAnalyser;
	private double lTh;
	private JavaSourceCodeChangeClassifier classifier;
	private CallerAnalyser callerAnalyser;
	
	public Analyser(Refactorings refactorings, ModificationHistory modificationHistory) {
		this.refactorings = refactorings;
		this.modificationHistory = modificationHistory;
		this.verifiedSourceCodeChanges = new LinkedList<SourceCodeChange>();
		this.strAnalyser = new StringAnalyser();
		this.classifier = new JavaSourceCodeChangeClassifier();
		this.callerAnalyser = new CallerAnalyser();
		this.lTh= 0.6;
	}
	
	public List<SourceCodeChange> getVerifiedSourceCodeChanges(){
		return this.verifiedSourceCodeChanges;
	}
	
	public void analyse() {
		callerAnalyser.extractShortNames(refactorings);
//		callerAnalyser.markCallers(modificationHistory); 
		
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
						addChange(scc);
				}else if(scc.getChangedEntity().getType().isField()){
					String field = scc.getChangedEntity().getUniqueName();
					int index = field.indexOf(" : ");
					field = field.substring(0, index);
					if (refactorings.isMovedAttribute(field))  
						analiseMovedAttribute(scc, field);
					else
						addChange(scc);
				}else if(scc.getChangedEntity().getType().isClass()){
					String clazz = scc.getChangedEntity().getUniqueName();
					if(!refactorings.isRefactoredClass(clazz))
						addChange(scc);
				}else if(!callerAnalyser.isCaller(scc))
					addChange(scc);
			}	
		}
		
		for(SourceCodeChange scc: modificationHistory.getCreatedClasses().values()) {
			String clazz = scc.getChangedEntity().getUniqueName();
			if(!refactorings.isRefactoredClass(clazz))
				addChange(scc);
		}
		
		for(SourceCodeChange scc: modificationHistory.getDeletedClasses().values()) {
			String clazz = scc.getChangedEntity().getUniqueName();
			if(!refactorings.isRefactoredClass(clazz))
				addChange(scc);
		}
	}
	
	//verify if the entities are equal or, if it is a leaf, if it is similar
	//Do I remove the leaf verification?
	private void analiseExtractedMethod(StructureEntityVersion root) {
		String createdMethod = this.refactorings.getExtractedMethodSignature(root.getUniqueName());
		SourceCodeChange method=modificationHistory.getCreatedMethod(createdMethod);
		if(method==null) {//new method not found, new class.
			method = modificationHistory.getDisposableCreatedMethod(createdMethod);
			if(method==null) //new method not found, signature incompability
				return;
		}
		Enumeration<Node> body = method.getBodyStructure().preorderEnumeration();
		List<SourceCodeChange> changes=root.getSourceCodeChanges();
		List<MatchedPair> matches= new LinkedList<MatchedPair>();
		
		
		try {
			body.nextElement();
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
						matches.add(new MatchedPair(node,scc));
						break;
					}
				}
			}
		
			body = method.getBodyStructure().preorderEnumeration();
			body.nextElement();
			while(body.hasMoreElements()) {
				Node node=body.nextElement();
				if(!node.isMatched()) {
					for(SourceCodeChange scc: changes) {
						if(!this.modificationHistory.isChecked(scc)
								&& similarity(node,scc) >= this.lTh) {
							matches.add(new MatchedPair(node,scc));
							modificationHistory.setCheckedChange(scc);
							changes.remove(scc);
							node.enableMatched();
							if(!isSameEntity(node, scc)) {
								Node parent=(Node) node.getParent();
								SourceCodeChange scc1 = classifier.classify(
										new Update(method.getStructureEntityVersion(),
												node.getEntity(),
												scc.getChangedEntity(),
												parent.getEntity()));
								if ((scc1 != null 
										&& !verifiedSourceCodeChanges.contains(scc1)
										&& !this.callerAnalyser.isCaller(scc1))) {
									addChange(scc1);
									
								}
							}
							
							break;
						}
					}
				}
			}
			
			ParentAnalyser pa = new ParentAnalyser(matches,'l');
			
			for(MatchedPair match: matches) {
				if(pa.isParentChange(match)) {
					SourceCodeChange scc1 = classifier.classify(  //new Move(ChangeType.STATEMENT_PARENT_CHANGE,
							new Move(method.getStructureEntityVersion(),
									match.getSourceCodeChangeEntity(),
									match.getNodeEntity(),
									match.getSourceCodeChangeParent(),
									match.getNodeParent()));
					if ((scc1 != null) && !verifiedSourceCodeChanges.contains(scc1)) {
						addChange(scc1);
					}
				}
			
			}
			
			body = method.getBodyStructure().preorderEnumeration();
			body.nextElement();
			while(body.hasMoreElements()) {
				Node node=body.nextElement();
				if(!node.isMatched()) {
					Node parent=(Node) node.getParent();
					SourceCodeChange scc = classifier.classify(
							new Insert(method.getStructureEntityVersion(),node.getEntity(),parent.getEntity()));
					if ((scc != null) && !verifiedSourceCodeChanges.contains(scc)) {
						addChange(scc);
					}	
				}
			}
			
			
			for(SourceCodeChange scc: changes) {
				if (!verifiedSourceCodeChanges.contains(scc)
						&& !this.callerAnalyser.isCaller(scc)
						&& !this.callerAnalyser.isCaller(scc, method)) {
					addChange(scc);
					modificationHistory.setCheckedChange(scc);
				}else
					modificationHistory.setCheckedChange(scc);
			}
			modificationHistory.setCheckedChange(method);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
			
	}
	
	//verify if the entities are equal or, if it is a leaf, if it is similar
	private void analiseInlinedMethod(StructureEntityVersion root) {
		String deletedMethod = this.refactorings.getInlinedMethodSignature(root.getUniqueName());
		SourceCodeChange method=modificationHistory.getDeletedMethod(deletedMethod);
		if(method==null) {//new method not found, new class.
			method = modificationHistory.getDisposableDeletedMethod(deletedMethod);
			if(method==null) //new method not found, signature incompability
				return;
		}
		Enumeration<Node> body = method.getBodyStructure().preorderEnumeration();
		List<SourceCodeChange> changes=root.getSourceCodeChanges();
		List<MatchedPair> matches = new LinkedList<MatchedPair>();
		

		try {
			body.nextElement();
			while(body.hasMoreElements()) {
				Node node=body.nextElement();
				node.disableMatched();
				for(SourceCodeChange scc: changes) {
					if(isSameEntity(node,scc)
							//Verify only parent's type or full data?
							&& isSameEntityType(( (Node)node.getParent() ).getEntity(),scc.getParentEntity())) {	
						matches.add(new MatchedPair(node,scc));
						modificationHistory.setCheckedChange(scc);
						changes.remove(scc);
						node.enableMatched();
						break;
					}
				}
			}
		
			body = method.getBodyStructure().preorderEnumeration();
			body.nextElement();
			while(body.hasMoreElements()) {
				Node node=body.nextElement();
				if(!node.isMatched()) {
					for(SourceCodeChange scc: changes) {
						if(!this.modificationHistory.isChecked(scc)
								&& similarity(node,scc) >= this.lTh) {
							matches.add(new MatchedPair(node,scc));
							modificationHistory.setCheckedChange(scc);
							changes.remove(scc);
							node.enableMatched();
							if(!isSameEntity(node, scc)) {
								Node parent=(Node) node.getParent();
								SourceCodeChange scc1 = classifier.classify(
										new Update(method.getStructureEntityVersion(),
												node.getEntity(),
												scc.getChangedEntity(),
												parent.getEntity()));
								if ((scc1 != null) 
										&& !verifiedSourceCodeChanges.contains(scc1)
										&& !this.callerAnalyser.isCaller(scc1)) {
									addChange(scc1);
									
								}
							}
							break;
						}
					}
					
				}
			}
			
			
			
			body = method.getBodyStructure().preorderEnumeration();
			body.nextElement();
			while(body.hasMoreElements()) {
				Node node=body.nextElement();
				if(!node.isMatched()) {
					Node parent=(Node) node.getParent();
					SourceCodeChange scc = classifier.classify(
							new Delete(method.getStructureEntityVersion(),node.getEntity(),parent.getEntity()));
					if (!verifiedSourceCodeChanges.contains(scc)) {
						addChange(scc);
					}	
				}
			}
			
			for(SourceCodeChange scc: changes) {
				if ((scc != null) 
						&& !verifiedSourceCodeChanges.contains(scc)
						&& !this.callerAnalyser.isCaller(scc, method)) {
					addChange(scc);
					modificationHistory.setCheckedChange(scc);
				}else
					modificationHistory.setCheckedChange(scc);
			}	
			
			modificationHistory.setCheckedChange(method);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	private void analiseMovedAttribute(SourceCodeChange oldField, String oldSignature) {
		String newSignature = this.refactorings.getMovedAttributeSignature(oldSignature);
		if(newSignature == null)
			return;
		
		SourceCodeChange newField = this.modificationHistory.getCreatedField(newSignature);
		StructureEntityVersion rootEntity = newField.getStructureEntityVersion();
	    List<SourceCodeChange> changes = extractChanges(oldField.getDeclarationStructure(), newField.getDeclarationStructure(), rootEntity); 
	    this.verifiedSourceCodeChanges.addAll(changes);
	    try {
	    	modificationHistory.setCheckedChange(oldField);
			modificationHistory.setCheckedChange(newField);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void analiseMovedMethod(SourceCodeChange oldMethod, String oldSignature) {
		String newSignature = this.refactorings.getMovedMethodSignature(oldSignature);
		if(newSignature == null)
			return;
		
		//Analyse signature incompability
		SourceCodeChange newMethod = this.modificationHistory.getCreatedMethod(newSignature);
		if(newMethod == null)
			return;
		
	    StructureEntityVersion rootEntity = newMethod.getStructureEntityVersion();
	    List<SourceCodeChange> changes = extractChanges(oldMethod.getDeclarationStructure(), newMethod.getDeclarationStructure(), rootEntity);
	    for(SourceCodeChange scc: changes) {
	    	if(scc.getChangeType()!=ChangeType.METHOD_RENAMING)
	    		this.verifiedSourceCodeChanges.add(scc);
	    }
	    changes = extractChanges(oldMethod.getBodyStructure(), newMethod.getBodyStructure(), rootEntity);
	    for(SourceCodeChange scc: changes) {
	    	if(!callerAnalyser.isCaller(scc))
	    		this.verifiedSourceCodeChanges.add(scc);
	    }
	    try {
	    	modificationHistory.setCheckedChange(oldMethod);
			modificationHistory.setCheckedChange(newMethod);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		if(!isSameEntityType(sce1,sce2))
			return 0;
		else
			return this.strAnalyser.similarity(sce1.getUniqueName(), sce2.getUniqueName());
		
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
	
	private void addChange(SourceCodeChange scc) {
		if(scc!=null){
			this.verifiedSourceCodeChanges.add(scc);
		}
	}
}
