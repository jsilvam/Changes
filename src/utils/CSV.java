package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;


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
		for(SourceCodeChange scc: changes) {
			if(scc.getChangedEntity().getType().isComment())
				addLine(commit,
						scc.getChangeType().toString(),
						null,
						scc.getRootEntity().getUniqueName());
			else
				addLine(commit,
						scc.getChangeType().toString(),
						scc.getChangedEntity().getUniqueName(),
						scc.getRootEntity().getUniqueName());
		}
	}
	
	
	public void addLine(String commit, String change, String changedEntity, String rootEntity) throws IOException { 
		writer.write("\n"+commit);
		writer.write(";"+change);
		writer.write(";\""+changedEntity+"\"");
		writer.write(";"+rootEntity);
		writer.flush();
	}
	
}
