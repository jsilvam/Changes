package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;


public class CSV {
	
	private FileWriter writer;
	
	public CSV(File file) throws IOException {
		writer = new FileWriter(file);
		writer.write("Commit;Change;"
				//+ "ChangedEntity;"
				+ "LocationBefore;LocationAfter");
		writer.flush();
	}
	
	public void addAll(List<SourceCodeChange> changes, String commit) throws IOException {
		for(SourceCodeChange scc: changes)
			addChange(scc, commit);
	}
	
	public void addChange(SourceCodeChange scc, String commit) throws IOException {
		if(scc!=null) {
			StructureEntityVersion rootEntity = scc.getRootEntity();
			String root = "";
			if(rootEntity != null)
				root= rootEntity.getUniqueName();
			
			if(scc.getChangedEntity().getType().isComment())
				addLine(
						commit,
						scc.getChangeType().toString(),
						"",
						root);
			else
				addLine(commit,
						scc.getChangeType().toString(),
						scc.getChangedEntity().getUniqueName(),
						root);
		}
	}
	
	public void addLine(String commit, String change, String changedEntity, String rootEntity) throws IOException { 
		writer.write("\n"+commit);
		writer.write(";"+change);
		writer.write(";\""+changedEntity+"\"");
		writer.write(";\""+rootEntity+"\"");
		writer.flush();
	}
	
}
