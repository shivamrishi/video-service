package org.magnum.dataup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class iDManager{
	
	@Component
	private static class idGenerator{
		int s=1;
		
		int newID() {
			return s++;
		}	
	}
	
	idGenerator idGen;
	
	@Autowired
	public iDManager(idGenerator p) {
		idGen = p;
	}
	
	public boolean isGenerated(int id) {
		if(id<idGen.s && id>=1) {
			return true;
		}else return false;
			
	}
	
	
	public int generate() {
		return idGen.newID();
	}
	
	
}