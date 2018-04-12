package analyser.callerAnalyser;

import java.util.List;

public class CallerPattern {
	
	private String shortName;
	private int nParameters;
	private CallerType type;
	public List<Integer> test;
	
	
	public enum CallerType{
		Field,
		Method;
	}
	
	public CallerPattern(String shortName, int nParameters, CallerType type) {
		this.shortName = shortName;
		this.nParameters = nParameters;
		this.type = type;
	}
	
	public CallerPattern(String fullName) {
		setFromFullName(fullName);
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	public void setShortNameFromFullName(String fullName) {
		int lastIndex = fullName.indexOf("(");		
		if(lastIndex<0)
			this.shortName = fullName.substring(fullName.lastIndexOf('.')+1);
		else	
			this.shortName = fullName.substring(fullName.lastIndexOf('.')+1,lastIndex);
	}
	
	public int getnParameters() {
		return nParameters;
	}
	
	public void setnParameters(int nParameters) {
		this.nParameters = nParameters;
	}
	
	public void setnParametersFromFullName(String fullName) {
		int beginIndex = fullName.lastIndexOf("(");
		if(beginIndex<0) {
			this.nParameters = 0;
			return;
		}
		int endIndex = fullName.indexOf(")");
		String str = fullName.substring(beginIndex +1, endIndex);
		int count = str.split(",").length;
		this.nParameters = count;
	}
	
	public CallerType getType() {
		return type;
	}

	public void setType(CallerType type) {
		this.type = type;
	}

	public void setFromFullName(String fullName) {
		this.setShortNameFromFullName(fullName);
		int lastIndex = fullName.indexOf("(");		
		if(lastIndex<0) {
			this.type = CallerType.Field;
			this.nParameters = -1;
		}
		else {
			this.type = CallerType.Method;
			setnParametersFromFullName(fullName);
		}
	}
}
