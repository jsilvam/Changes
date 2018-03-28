package analyser;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;

public class ParentAnalyser {
	
	SourceCodeEntity originalRoot;
	
	public ParentAnalyser(List<MatchedPair> matches, char c) {
		originalRoot = identifyParent(matches,c);
	}

	public boolean isParentChange(MatchedPair match){
		return !isSameEntityType(match.getNodeParent(), match.getSourceCodeChangeParent())
				&& !isSameEntityType(originalRoot, match.getSourceCodeChangeParent());
	}
	
	public boolean isParentChange(MatchedPair match, SourceCodeEntity originalRoot){
		return !isSameEntityType(match.getNodeParent(), match.getSourceCodeChangeParent())
				&& !isSameEntityType(originalRoot, match.getSourceCodeChangeParent());
	}
	
	public SourceCodeEntity identifyParent(List<MatchedPair> matches,char c) {
		if(matches.isEmpty())
			return null;
		
		SourceCodeChange scc = matches.get(0).getChange();
		Node root;
		if(c=='l' || c=='L')
			root = scc.getRootEntity().getBodyLeft();
		else if(c=='r' || c=='R')
			root = scc.getRootEntity().getBodyRigth();
		else 
			throw new IllegalArgumentException("Inform only 'r' or 'l'!");
		
		Node node = findNode(scc,root);
		int depth = depth(node,root);
		for(MatchedPair match: matches) {
			scc=match.getChange();
			Node n = findNode(scc,root);
			int depthAux = depth(n,root);
			if(depthAux < depth)
				node = n;
		}
		node = (Node) node.getParent();
		return node.getEntity();
	}
	
	private int depth(Node node, Node root) {
		Enumeration path= node.pathFromAncestorEnumeration(root);
		int cont = 0;
		while(path.hasMoreElements()) {
			path.nextElement();
			cont++;
		}
		node.getEntity();
		return cont;
	}
	
	private Node findNode(SourceCodeChange scc, Node root) {
		Enumeration body = root.breadthFirstEnumeration();
		while(body.hasMoreElements()) {
			Node element = (Node)body.nextElement();
			if(isSameEntity(element,scc))
				return element;
		}
		return null;
	}
	
	private boolean isSameEntity(Node node, SourceCodeChange scc) {
		SourceCodeEntity e1 = node.getEntity();
		SourceCodeEntity e2 = scc.getChangedEntity();
		return new EqualsBuilder().append(e1.getUniqueName(), e2.getUniqueName()).append(e1.getType(), e2.getType())
                .append(e1.getModifiers(), e2.getModifiers()).isEquals();
	}
	
	private boolean isSameEntityType(SourceCodeEntity entity1, SourceCodeEntity entity2) {
		EntityType et1 = entity1.getType();
		EntityType et2 = entity2.getType();
		return et1.equals(et2);
	}

	public SourceCodeEntity getParent() {
		return originalRoot;
	}

	public void setParent(SourceCodeEntity parent) {
		this.originalRoot = parent;
	}
}
