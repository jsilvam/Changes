package analyser;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;

import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;

public class ParentAnalyser {

	
	
	public SourceCodeEntity parent(List<MatchedPair> matches) {
		List<MatchedPair> rootStatments = new LinkedList<MatchedPair>();
		for(MatchedPair match: matches) { 
			if(match.getNodeParent().getType().isMethod())
				rootStatments.add(match);
		}
		
		
		SourceCodeEntity rootSCE = rootStatments.get(0).getSourceCodeChangeParent();
		List<MatchedPair> divergentsStatments = new LinkedList<MatchedPair>();
		for(MatchedPair match: rootStatments) {
			if(!isSameEntity(rootSCE,match.getSourceCodeChangeParent())){
				divergentsStatments.add(match);
				match.getChange().getRootEntity();
			}
		}
		
		
		
			
		return null;
	}
	
	
	private boolean isParent(Node node, Node parent) {
		node.pathFromAncestorEnumeration(parent);
		
		return false;
	}
	
	private int distance(Node node, Node parent) {
		Enumeration path= node.pathFromAncestorEnumeration(parent);
		int cont = 0;
		while(path.hasMoreElements()) {
			path.nextElement();
			cont++;
		}
			
		return cont;
	}
	
	
	private boolean isSameEntity(SourceCodeEntity e1, SourceCodeEntity e2) {
		return new EqualsBuilder().append(e1.getUniqueName(), e2.getUniqueName()).append(e1.getType(), e2.getType())
                .append(e1.getModifiers(), e2.getModifiers()).isEquals();
	}
	
//	private void get() {
//		if(!isSameEntityType(( (Node)node.getParent() ).getEntity(),scc.getParentEntity())) {
//			Node parent=(Node) node.getParent();
//			SourceCodeChange scc1 = classifier.classify(  //new Move(ChangeType.STATEMENT_PARENT_CHANGE,
//					new Move(method.getStructureEntityVersion(),
//					scc.getChangedEntity(),
//					node.getEntity(),
//					scc.getParentEntity(),
//					((Node)node.getParent()).getEntity()));
//			if ((scc1 != null) && !verifiedSourceCodeChanges.contains(scc1)) {
//				addChange(scc1);
//				modificationHistory.setCheckedChange(scc);
//				changes.remove(scc);
//				node.enableMatched();
//			}
//		}
//	}
}
