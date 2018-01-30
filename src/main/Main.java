package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import utils.CSV;
import utils.FileUtils;

public class Main {
	
	private static void check(String repositoryUrl) throws IOException  {
		
		String dir;
		if(System.getProperty("os.name").contains("Linux"))
			dir  = "/home/jaziel/Dropbox/UFCG/Projeto/Dados/CSVs/"; //Linux
		else
			dir  = "C:\\Users\\Jaziel Moreira\\Dropbox\\UFCG\\Projeto\\Dados\\CSVs\\"; //Windows
		
		String aux=repositoryUrl.substring(repositoryUrl.lastIndexOf("/")+1);
		
		
		String refactoringsCSV=dir+"Refatoramentos/Part 1/"+aux+".csv";
		CSV result= new CSV(new File(dir+"Mudancas/"+aux+".csv"));
		Scanner in = new Scanner(new FileReader(dir+"Refatoramentos/Part 2/"+aux+".csv")).useDelimiter(";");
		PrintStream ps = new PrintStream(
			     new FileOutputStream(dir+"Mudancas/"+aux+" - log.txt", true));
		
		Changes changes=new Changes(repositoryUrl, refactoringsCSV, result);
		
		
		
		in.nextLine();
		while(in.hasNext()) {
			String commit=in.next();
			if(in.nextLine().equals(";0")) {
				try {
					changes.getChanges(commit);
				} catch (Exception e) {
					System.out.println("Get Changes: Error");
					ps.println("Commit error: "+commit);
					e.printStackTrace(ps);
					ps.flush();
				}
			}
		}
		in.close();
		ps.close();
	}

	
	public static void main(String[] args) throws IOException {
		
		check("https://github.com/Kailashrb/scribe-java");
		check("https://github.com/jopt-simple/jopt-simple");
		check("https://github.com/notnoop/java-apns");	
		check("https://github.com/vkostyukov/la4j");
		
		
		/*
		//for(String s:FileUtils.listClass(new File("/home/jaziel/git/Changes/src"))) {
			//System.out.println(s);
		//}
		
		
		File left = new File("Purity5.java");
		File right = new File("Purity6.java");

		FileDistiller distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
		try {
		    distiller.extractClassifiedSourceCodeChanges(left, right);
		} catch(Exception e) {
		    System.err.println("Warning: error while change distilling. " + e.getMessage());
		}

		List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
		if(changes != null) {
		    for(SourceCodeChange change : changes) {
		    	//System.out.println(change.getLabel()+"  |  "+change.getChangedEntity().getUniqueName());
		    	
		    	System.out.println("\nbegin");
		    	System.out.println("change.getLabel():  "+change.getLabel());
		        System.out.println("change.toString():  "+change.toString());
		        
		        System.out.println("\nChanged entity");
		        System.out.println("change.getChangedEntity().getLabel():  "+change.getChangedEntity().getLabel());
		        System.out.println("change.getChangedEntity().getUniqueName():  "+change.getChangedEntity().getUniqueName());
		        System.out.println("change.getChangedEntity().getModifiers():  "+change.getChangedEntity().getModifiers());
		        System.out.println("change.getChangedEntity().getType().name():  "+change.getChangedEntity().getType().name());
		        System.out.println("change.getChangedEntity().getSourceRange().toString():  "+change.getChangedEntity().getSourceRange().toString()+"\n");
		        System.out.println("AssociatedEntities");
		        for(SourceCodeEntity sc:change.getChangedEntity().getAssociatedEntities()) {
		        	System.out.println(sc.getLabel());
		        }
		        
		        System.out.println("\nChange type");
		        System.out.println("change.getChangeType().name():  "+change.getChangeType().name());
		        System.out.println("change.getChangeType().toString():  "+change.getChangeType().toString());
		        System.out.println("change.getChangeType().getSignificance().name():  "+change.getChangeType().getSignificance().name());
		        System.out.println("change.getChangeType().getSignificance().toString():  "+change.getChangeType().getSignificance().toString());
		                
		    }
		}*/

	}

}
