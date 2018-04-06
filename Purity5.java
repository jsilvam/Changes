package main;



import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import saferefactor.core.SafeRefactorImp;
import saferefactor.core.Parameters;
import saferefactor.core.Report;
import saferefactor.core.SafeRefactor;
import saferefactor.core.SafeRefactorException;
import saferefactor.core.util.Project;

public class Purity{
	
	private String urlRepository;
	
	public Purity(String urlRepository){
		this.urlRepository=urlRepository;
	}
	
	public void setUrlRepository(String urlRepository){
		this.urlRepository=urlRepository;
	}
	
	public String getUrlRepository(){
		return this.urlRepository;
	}
	

	public int check(String commit, String parent) throws Exception{
		
		GithubDownloader git=new GithubDownloader(urlRepository);
		ZipExtractor ze=new ZipExtractor();
		
		File targetFile=git.downloadCommit(commit);
		File targetFolder=ze.extract(targetFile, new File(git.getLocation(),commit));
		
		
		File sourceFile=git.downloadCommit(parent);
		File sourceFolder=ze.extract(sourceFile, new File(git.getLocation(),parent));
		
		
		
		//File targetFolder=new File("C:\\tmp\\Projeto\\Downloads\\jasmine-maven-plugin\\88e4387b52361442b7a70c84cd2e3625258d4ae2");
		//File sourceFolder=new File("C:\\tmp\\Projeto\\Downloads\\jasmine-maven-plugin\\36217276b60cb69c9acf3a1f21b7017e390210b5");
		
		Project source = new Project();
		source.setProjectFolder(sourceFolder);
		File bin=new File(sourceFolder, "bin");
		if(!bin.exists())
			bin.mkdir();
		source.setBuildFolder(bin);
		source.setSrcFolder(new File(sourceFolder, "src\\main\\java"));
		System.out.println("Downloading dependencies to: "+sourceFolder);
		Process p=Runtime.getRuntime().exec("cmd /C mvn -f "+sourceFolder.getAbsolutePath()+" dependency:copy-dependencies");
		BufferedReader reader =
				new BufferedReader(new InputStreamReader(p.getInputStream()));
		while ((reader.readLine()) != null) {}
		p.waitFor();
		File libFolder = new File(sourceFolder, "target\\dependency");
		if (libFolder.exists())
			source.setLibFolder(libFolder);
		System.out.println("Source project: "+source.getLibFolder());
		
		
		Project target = new Project();
		target.setProjectFolder(targetFolder);
		bin=new File(targetFolder, "bin");
		if(!bin.exists())
			bin.mkdir();
		target.setBuildFolder(bin);
		target.setSrcFolder(new File(targetFolder, "src\\main\\java"));
		System.out.println("Downloading dependencies to: "+targetFolder);
		p=Runtime.getRuntime().exec("cmd /C mvn -f "+targetFolder.getAbsolutePath()+" dependency:copy-dependencies");
		reader =
				new BufferedReader(new InputStreamReader(p.getInputStream()));
		while ((reader.readLine()) != null) {}
		p.waitFor();
		File targetLibFolder = new File(targetFolder, "target\\dependency");
		if (targetLibFolder.exists())
			target.setLibFolder(targetLibFolder);
		System.out.println("Target project: "+target.getLibFolder());

		
		//SafeRefactor sr = new SafeRefactorImp(source, target);
		
		
		Parameters parameters = new Parameters();
		parameters.setTimeLimit(120);
		//parameters.setCheckCoverage(true);
		parameters.setCompileProjects(true);
		SafeRefactor sr = new SafeRefactorImp(source, target,parameters);
		
		try {
			sr.checkTransformation();
		} catch (SafeRefactorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			deleteDirectory(git.getLocation());
			return -1;
		}
		
		Report result=sr.getReport();
		
		deleteDirectory(git.getLocation());
		if(result.isRefactoring()) 
			return 1;
		else
			return 0;
	}

	private void deleteDirectory(File dir){
		int aux;
		File[] contents=dir.listFiles();
		for(int f = 0; f<contents.length; f++){
			if(f.isDirectory())
				deleteDirectory(f);
			else
				f.delete();
		}
		dir.delete();
	}

	private class Teste{
		String m;
		
		private int getM() {
			return m;
		}
		
		private void setM(String m1, int g, String gh) {
			m=m1;
		}
		
	}
	
}
