package main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.google.inject.Guice;
import com.google.inject.Injector;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.JavaChangeDistillerModule;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaSourceCodeChangeClassifier;
import ch.uzh.ifi.seal.changedistiller.distilling.Distiller;
import ch.uzh.ifi.seal.changedistiller.distilling.DistillerFactory;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.distilling.SourceCodeChangeClassifier;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.structuredifferencing.StructureNode;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.TreeDifferencer;
import ch.uzh.ifi.seal.changedistiller.ast.ASTHelper;
import ch.uzh.ifi.seal.changedistiller.ast.ASTHelperFactory;
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
			     new FileOutputStream(dir+"Mudancas/Logs/"+aux+" - log.txt", true));
		
		Changes changes=new Changes(repositoryUrl, refactoringsCSV, result);
		
		
		
		in.nextLine();
		while(in.hasNext()) {
			String commit=in.next();
			if(in.nextLine().equals(";0")) {
				try {
					changes.extractChanges(commit);
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
//		
//		check("https://github.com/square/okhttp");
//		check("https://github.com/square/retrofit");
//		check("https://github.com/Kailashrb/scribe-java");
//		check("https://github.com/jopt-simple/jopt-simple");
//		check("https://github.com/notnoop/java-apns");	
		check("https://github.com/vkostyukov/la4j");
//		
		
		
		//for(String s:FileUtils.listClass(new File("/home/jaziel/git/Changes/src"))) {
			//System.out.println(s);
		//}
		
		
//		File left = new File("Purity5.java");
//		File right = new File("Purity6.java");
//		Set<SourceCodeChange> history=new HashSet<SourceCodeChange>();
//
//		FileDistiller distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
//		try {
//		    distiller.extractClassifiedSourceCodeChanges(left, right);
//		} catch(Exception e) {
//		    System.err.println("Warning: error while change distilling. " + e.getMessage());
//		}
//		
//		ModificationHistory mh=new ModificationHistory();
//		List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
//		if(changes != null) {
//		    for(SourceCodeChange change : changes) {
//		    	//System.out.println(change.getLabel()+"  |  "+change.getChangedEntity().getUniqueName());
//		    	
//		    	System.out.println("\nbegin");
//		    	System.out.println("change.getLabel():  "+change.getLabel());
//		    	System.out.println("change.getChangeType():  "+change.getChangeType());
//		        System.out.println("change.toString():  "+change.toString());
////		        mh.addChange(change);
////		        System.out.println();
////		        
////		        if(!history.contains(change)) {
////		        	System.out.println("change.getParentEntity(): "+change.getParentEntity());
////		        	System.out.println("change.getRootEntity(): "+change.getRootEntity());
////		        	history.addAll(change.getRootEntity().getSourceCodeChanges());
////		        	
////		        }
////		        
////		        System.out.println("\nDeclaration:");
////		        if(change.getDeclarationStructure()!=null) {
////		        	Enumeration e=change.getDeclarationStructure().preorderEnumeration();
////		        	while(e.hasMoreElements())
////		        		System.out.println(e.nextElement());
////		        }else {
////		        	System.out.println("Declaration: null");
////		        }
////		        
////		        System.out.println("\nBody:");
////		        if(change.getBodyStructure()!=null) {
////		        	Enumeration e=change.getBodyStructure().preorderEnumeration();
////		        	while(e.hasMoreElements()) {
////		        		Node n=(Node)e.nextElement();
////		        		System.out.println(n.getLabel()+ "  "+ n.getValue());
////		        	}
////		        }else {
////		        	System.out.println("body: null");
////		        }
//		        
////		        
////		        
//		        System.out.println("change.getParentEntity().toString():  "+change.getParentEntity().toString());
//		        
//		        System.out.println("\nChanged entity");
//		        System.out.println("change.getChangedEntity().getLabel():  "+change.getChangedEntity().getLabel());
//		        System.out.println("change.getChangedEntity().getUniqueName():  "+change.getChangedEntity().getUniqueName());
//		        System.out.println("change.getChangedEntity().getModifiers():  "+change.getChangedEntity().getModifiers());
//		        System.out.println("change.getChangedEntity().getType().name():  "+change.getChangedEntity().getType().name());
//		        System.out.println("change.getChangedEntity().getSourceRange().toString():  "+change.getChangedEntity().getSourceRange().toString()+"\n");
//		        System.out.println("AssociatedEntities");
//		        for(SourceCodeEntity sc:change.getChangedEntity().getAssociatedEntities()) {
//		        	System.out.println(sc.getLabel());
//		        }
//		        
//		        System.out.println("\nChange type");
//		        System.out.println("change.getChangeType().name():  "+change.getChangeType().name());
//		        System.out.println("change.getChangeType().toString():  "+change.getChangeType().toString());
//		        System.out.println("change.getChangeType().getSignificance().name():  "+change.getChangeType().getSignificance().name());
//		        System.out.println("change.getChangeType().getSignificance().toString():  "+change.getChangeType().getSignificance().toString());
////		        
////		        
////		        Enumeration<Node> body = change.getBodyStructure().preorderEnumeration();
////				while(body.hasMoreElements()) {
////					Node n=body.nextElement();
////					System.out.println(n.toString()+"  "+n.isMatched());
////				}
//		        
//		        
//		    }
//		    SourceCodeChange scc1 = changes.get(0);
//		    SourceCodeChange scc2 = changes.get(1);
//		    
//		    
//		    
//		    Injector injector = Guice.createInjector(new JavaChangeDistillerModule());
//		    DistillerFactory df = injector.getInstance(DistillerFactory.class); 
//		    
//		    //fLeftASTHelper.createStructureEntityVersion(scc1.getBodyStructure());
//		    
//		    
//		    Distiller distille = df.create(scc1.getEntityVersion());
//		    distille.extractClassifiedSourceCodeChanges(scc1.getDeclarationStructure(), scc2.getDeclarationStructure());
//		    
//		    
//		    
//		    System.out.println("Teste");
//		    changes=scc1.getEntityVersion().getSourceCodeChanges();
//		    if(changes != null) {
//			    for(SourceCodeChange change : changes) {
//			    	//System.out.println(change.getLabel()+"  |  "+change.getChangedEntity().getUniqueName());
//			    	
//			    	System.out.println("\nbegin");
//			    	System.out.println("change.getLabel():  "+change.getLabel());
//			        System.out.println("change.toString():  "+change.toString());
//			    }
//		    }
//		}

	}

}
