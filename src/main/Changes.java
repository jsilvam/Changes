package main;

import java.io.File;
import java.util.List;
import java.util.Map;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import utils.*;

public class Changes {
	
	private String urlRepository;
	private String RefactoringsCSV;
	private CSV ResultCSV;
	
	public Changes(String urlRepository, String refactoringsCSV, CSV resultCSV){
		this.urlRepository=urlRepository;
		this.RefactoringsCSV=refactoringsCSV;
		this.ResultCSV=resultCSV;
	}
	
	public void getChanges(String commit) throws Exception {
		//initialize
		Refactorings refactorings = new Refactorings(this.RefactoringsCSV,commit); //class not finished.
		Analyser analyser=new Analyser(refactorings,commit);
		String parent=refactorings.getParent();
		GithubDownloader git = new GithubDownloader(this.urlRepository);
		
		//download and extract projects
		File targetFile=git.downloadCommit(commit);
		File targetFolder=ZipExtractor.extract(targetFile, new File(git.getLocation(),commit));
		File sourceFile=git.downloadCommit(parent);
		File sourceFolder=ZipExtractor.extract(sourceFile, new File(git.getLocation(),parent));
		
		//list Java files
		Map<String,File> sourceFiles=FileUtils.getClasses(sourceFolder);
		Map<String,File> targetFiles=FileUtils.getClasses(targetFolder);
		
		for(String clazz:sourceFiles.keySet()) {
			if(refactorings.getChangedClassSignatures().containsKey(clazz)) {
				String newSignature=refactorings.getChangedClassSignatures().get(clazz);
				File left=sourceFiles.get(clazz);
				File right=targetFiles.get(newSignature);
				analyser.analyse(this.listChangesOfClass(left,right),ResultCSV,clazz,newSignature);
				targetFiles.remove(newSignature);
			}else if (targetFiles.containsKey(clazz)){
				File left=sourceFiles.get(clazz);
				File right=targetFiles.get(clazz);
				analyser.analyse(this.listChangesOfClass(left,right),ResultCSV,clazz,clazz);
				targetFiles.remove(clazz);
			}else {
				System.out.println("File Removed: "+clazz);
				analyser.addRemovedClass(clazz,ResultCSV);
			}
		}
		
		for(String clazz:targetFiles.keySet()) {
			System.out.println("New class: "+clazz);
			analyser.addNewClass(clazz,ResultCSV);
		}
	}
	
	public List<SourceCodeChange> listChangesOfClass(File left, File right) {
		FileDistiller distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
		try {
		    distiller.extractClassifiedSourceCodeChanges(left, right);
		} catch(Exception e) {
		    System.err.println("Warning: error while change distilling. " + e.getMessage());
		}
		List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
		return changes;
	}
	
	private void deleteDirectory(File dir){
		File[] contents=dir.listFiles();
		for(File f: contents){
			if(f.isDirectory())
				deleteDirectory(f);
			else
				f.delete();
		}
		dir.delete();
	}

}
