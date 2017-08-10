package main;

import java.io.File;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;

public class Class {
	
	File left;
	File right;
		
	public Class(String left,String right) {
		this.left=new File(left);
		this.left=new File(left);
	}
	
	public File getLeft() {
		return left;
	}

	public void setLeft(File left) {
		this.left = left;
	}

	public File getRight() {
		return right;
	}

	public void setRight(File right) {
		this.right = right;
	}

	public List<SourceCodeChange> getChanges() {
		FileDistiller distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
		try {
		    distiller.extractClassifiedSourceCodeChanges(left, right);
		} catch(Exception e) {
		    System.err.println("Warning: error while change distilling. " + e.getMessage());
		}

		return distiller.getSourceCodeChanges();
	}
}