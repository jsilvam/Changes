package main;

import java.io.File;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;

public class Main {

	public static void main(String[] args) {
		
		
		for(String s:FileUtils.listClass(new File("/home/jaziel/git/Changes/src"))) {
			System.out.println(s);
		}
		
		
		/*File left = new File("Purity3.java");
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
		        System.out.println(change.getLabel()+" | "+change.toString());
		    }
		}*/

	}

}
