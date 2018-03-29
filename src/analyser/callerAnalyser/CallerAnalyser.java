package analyser.callerAnalyser;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import analyser.StringAnalyser;
import analyser.callerAnalyser.CallerPattern.CallerType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import main.ModificationHistory;
import main.Refactorings;

public class CallerAnalyser {
	
	private LinkedList<CallerPattern> callerPatterns;
	
	
	
	public CallerAnalyser() {
		callerPatterns = new LinkedList<CallerPattern>();
	}
	
	public void extractShortNames(Refactorings refactorings) {
		Collection<String> signatures = refactorings.getInlinedMethods().values();
		for(String signature: signatures)
			callerPatterns.add(new CallerPattern(signature));
		
		signatures = refactorings.getMovedMethods().values();
		for(String signature: signatures)
			callerPatterns.add(new CallerPattern(signature));
		
		signatures = refactorings.getRenamedMethods().values();
		for(String signature: signatures)
			callerPatterns.add(new CallerPattern(signature));
		
		signatures = refactorings.getMovedAttributes().values();
		for(String signature: signatures)
			callerPatterns.add(new CallerPattern(signature));
		
		
		Set<String> signatures2 = refactorings.getMovedMethods().keySet();
		for(String signature: signatures2)
			callerPatterns.add(new CallerPattern(signature));
		
		signatures2 = refactorings.getRenamedMethods().keySet();
		for(String signature: signatures2)
			callerPatterns.add(new CallerPattern(signature));
		
		signatures2 = refactorings.getMovedAttributes().keySet();
		for(String signature: signatures2)
			callerPatterns.add(new CallerPattern(signature));
	}
	
	public void markCallers(ModificationHistory mh) {
		try {
			for(SourceCodeChange scc: mh.getChangeHistory().keySet()) { 
				if(isCaller(scc)) {
					mh.setCheckedChange(scc);
					break;
				}	
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isCaller(SourceCodeChange scc) {
		for(CallerPattern cp: callerPatterns) 
			if(fitPattern(scc,cp)) {
				return true;
			}
		return false;
	}
	
	public boolean isCaller(SourceCodeChange caller, SourceCodeChange callee) {
		SourceCodeEntity calleeEntity = callee.getChangedEntity();
		CallerPattern cp = new CallerPattern(calleeEntity.getUniqueName());
		return fitPattern(caller,cp);
	}
	
	public boolean fitPattern(SourceCodeChange caller, CallerPattern callee) {
		if(!caller.getChangedEntity().getType().isStatement())
			return false;
			
		String statment = caller.getChangedEntity().getUniqueName();
		if(callee.getType() == CallerType.Field 
				&& statment.contains(callee.getShortName()))
			return true;
		
		String[] split = statment.replaceAll("\\s","").split(callee.getShortName());
		for(int i = 1; i< split.length; i++) {
			if(split[i].startsWith("(")
					&& countParemetersFromInvocation(split[i])==callee.getnParameters())
				return true;
		}
		return false;
	}
	
	public int countParemetersFromInvocation(String str) {
		int result = 0;
		char[] sequence = str.replaceAll("\\s","").toCharArray();
		
		int count = 0;
		int begin=0;
		int end=0;
		for(int i=0; i<sequence.length; i++) {
			if(sequence[i]=='(') {
				if(count==0)
					begin=i;
				count++;
			}else if(sequence[i]==')') {
				count--;
				if(count==0) {
					end=i;
					break;
				}
			}else if(sequence[i]==',' && count==1)
				result++;
		}
		if(end-begin > 1)
			result++;
		return result;
	}

}
