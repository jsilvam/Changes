package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import utils.*;

public class Changes {
	
	private String urlRepository;
	private String csvPath;
	
	public Changes(String urlRepository, String csvPath){
		this.urlRepository=urlRepository;
		this.csvPath=csvPath;
	}
	
	public List<SourceCodeChange> getChanges(String commit) throws Exception {
		//initialize
		Refactorings refactorings = new Refactorings(this.csvPath,commit); //class not finished.
		String parent=refactorings.getParent();
		GithubDownloader git = new GithubDownloader(this.urlRepository);
		List<SourceCodeChange> changes = new ArrayList<SourceCodeChange>();
		
		//download and extract projects
		File targetFile=git.downloadCommit(commit);
		File targetFolder=ZipExtractor.extract(targetFile, new File(git.getLocation(),commit));
		File sourceFile=git.downloadCommit(parent);
		File sourceFolder=ZipExtractor.extract(sourceFile, new File(git.getLocation(),parent));
		
		//list Java files
		List<String> sourceFiles=FileUtils.listClass(new File(sourceFolder,"src\\main\\java"));
		List<String> targetFiles=FileUtils.listClass(new File(targetFolder,"src\\main\\java"));
		
		for(String file:sourceFiles) {
			File left = new File(sourceFolder,"src\\main\\java"+file.replace(".", "\\"));
			if(refactorings.getChangedClassSignatures().containsKey(file)) {
				File right = new File(targetFolder,"src\\main\\java"+refactorings.getChangedClassSignatures().get(file).replace(".", "\\"));
				changes.addAll(listChangesOfClass(left, right));
			}else if(targetFiles.contains(file)){
				File right = new File(targetFolder,"src\\main\\java"+file.replace(".", "\\"));
				changes.addAll(listChangesOfClass(left, right));
			}else
				System.out.println("File Removed: "+file);
		}
		
		//Remove the files that were already analyzed, remaining only the new files
		targetFiles.removeAll(sourceFiles);
		
		for(String file: targetFiles)
			System.out.println("File Removed: "+file);
		
		System.out.println("\n\nChanges...\n");
		
		deleteDirectory(git.getLocation());
		
		return changes;
		
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
