package main;

import java.io.File;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import utils.FileUtils;

public class Main {

	public static void main(String[] args) {
		
		
		
		//for(String s:FileUtils.listClass(new File("/home/jaziel/git/Changes/src"))) {
			//System.out.println(s);
		//}
		
		
		File left = new File("Purity3.java");
		File right = new File("Purity4.java");

		FileDistiller distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
		try {
		    distiller.extractClassifiedSourceCodeChanges(left, right);
		} catch(Exception e) {
		    System.err.println("Warning: error while change distilling. " + e.getMessage());
		}

		List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
		if(changes != null) {
		    for(SourceCodeChange change : changes) {
		    	System.out.println(change.getLabel()+"  |  "+change.getChangedEntity().getUniqueName());
		    	
		    	
		    	/*System.out.println(change.getLabel());
		        System.out.println(change.toString());
		        
		        System.out.println("\nChanged entity");
		        System.out.println(change.getChangedEntity().getLabel());
		        System.out.println(change.getChangedEntity().getUniqueName());
		        System.out.println(change.getChangedEntity().getType().name());
		        System.out.println(change.getChangedEntity().getSourceRange().toString()+"\n");
		        for(SourceCodeEntity sc:change.getChangedEntity().getAssociatedEntities()) {
		        	System.out.println(sc.getLabel());
		        }
		        
		        System.out.println("\nChange type");
		        System.out.println(change.getChangeType().name());
		        System.out.println(change.getChangeType().toString());
		        System.out.println(change.getChangeType().getSignificance().name());
		        System.out.println(change.getChangeType().getSignificance().toString());*/
		        
		    }
		}

	}

}
