package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class CSV {
	
	private FileWriter writer;
	
	public CSV(File file) throws IOException {
		writer = new FileWriter(file);
		writer.write("Commit;Change;ChangedEntity;LocationBefore;LocationAfter");
		writer.flush();
	}
	
	public void addLine(String commit, String change, String changedEntity, String locationBefore, String locationAfter) throws IOException { 
		writer.write("\n"+commit);
		writer.write(";"+change);
		writer.write(";"+changedEntity);
		writer.write(";"+locationBefore);
		writer.write(";"+locationAfter);
		writer.flush();
	}
	
}
