package main;

import java.io.File;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import utils.*;

public class Changes {
	
	private String urlRepository;
	
	public Changes(String urlRepository){
		this.urlRepository=urlRepository;
	}
	
	public void check(String commit,String parent) throws Exception {
		Refactorings refactorings=new Refactorings("","");
		GithubDownloader git=new GithubDownloader(urlRepository);
		List<SourceCodeChange> changes;
		
		File targetFile=git.downloadCommit(commit);
		File targetFolder=ZipExtractor.extract(targetFile, new File(git.getLocation(),commit));
		
		
		File sourceFile=git.downloadCommit(parent);
		File sourceFolder=ZipExtractor.extract(sourceFile, new File(git.getLocation(),parent));
		
		List<String> sourceFiles=FileUtils.listClass(sourceFolder);
		List<String> targetFiles=FileUtils.listClass(targetFolder);
		
		for(String file:sourceFiles) {
			if(refactorings.getChangedClasses().containsKey(file)) {
				FileDistiller distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
				try {
				    //distiller.extractClassifiedSourceCodeChanges(file, refactorings.getChangedClasses().get(file));
				} catch(Exception e) {
				    System.err.println("Warning: error while change distilling. " + e.getMessage());
				}

				changes = distiller.getSourceCodeChanges();
			}
		}
		
		
		deleteDirectory(git.getLocation());
		
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
