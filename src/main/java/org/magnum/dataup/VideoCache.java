package org.magnum.dataup;

import java.util.ArrayList;
import java.util.Collection;

import org.magnum.dataup.model.Video;
import org.springframework.stereotype.Component;

@Component
public class VideoCache{
	
	private ArrayList<Video> cache;
	
	public VideoCache() {
		cache = new ArrayList<Video>();
	}
	
	public Collection<Video> getAll() {
		return cache;
		
	}
	
	public boolean addVideo(Video v){
		return cache.add(v);
	}
	
	
}