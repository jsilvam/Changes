package main;

import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;

public class UpdateCandidate {
	
	StructureEntityVersion rootEntity;
	SourceCodeEntity oldEntity;
	SourceCodeEntity newEntity;
	SourceCodeEntity parentEntity;
	
	public UpdateCandidate(
			StructureEntityVersion rootEntity,
			SourceCodeEntity oldEntity,
			SourceCodeEntity newEntity,
			SourceCodeEntity parentEntity) {
		this.rootEntity = rootEntity;
		this.oldEntity = oldEntity;
		this.newEntity = newEntity;
		this.parentEntity = parentEntity;
	}
	
	public StructureEntityVersion getRootEntity() {
		return rootEntity;
	}
	public void setRootEntity(StructureEntityVersion rootEntity) {
		this.rootEntity = rootEntity;
	}
	public SourceCodeEntity getOldEntity() {
		return oldEntity;
	}
	public void setOldEntity(SourceCodeEntity oldEntity) {
		this.oldEntity = oldEntity;
	}
	public SourceCodeEntity getNewEntity() {
		return newEntity;
	}
	public void setNewEntity(SourceCodeEntity newEntity) {
		this.newEntity = newEntity;
	}
	public SourceCodeEntity getParentEntity() {
		return parentEntity;
	}
	public void setParentEntity(SourceCodeEntity parentEntity) {
		this.parentEntity = parentEntity;
	}
	
	

}
