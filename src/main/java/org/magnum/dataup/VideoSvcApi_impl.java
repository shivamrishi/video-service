/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.http.HttpStatus;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


import org.springframework.http.HttpHeaders;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.mime.TypedFile;

@RestController
public class VideoSvcApi_impl implements VideoSvcApi {
	
	private VideoCache c;
	private iDManager idm;
	private VideoFileManager vfm;

	public static final String VIDEO_SVC_PATH = "/video";
	
	

	@GetMapping("/playback")
	public ModelAndView index(/*Model model,*/ @RequestParam(name="id") int id) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("playback");
        modelAndView.addObject("ID", id);
        return modelAndView; // Return the name of the HTML template (without the file extension)
    }
	
	@GetMapping("/upload")
	public ModelAndView upload() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("upload");
        return modelAndView; // Return the name of the HTML template (without the file extension)
    }
	
	@GetMapping("/find")
	public ModelAndView find(@RequestParam(name="title", required= false) String title, @RequestParam(name="id", required=false) Integer id) {
        ModelAndView modelAndView = new ModelAndView();
        if(title==null && id==null)
        	modelAndView.setViewName("find");
        else if(id!=null) {
//            return new ModelAndView("redirect:/redirectedUrl", model);
        	modelAndView.setViewName("redirect:playback?id="+id);
        }
        return modelAndView; // Return the name of the HTML template (without the file extension)
    }
	
	
	@GetMapping("/")
    public ModelAndView getHtml() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        return modelAndView;
    }
	

	@GetMapping(VIDEO_SVC_PATH)
    public ModelAndView videoListing() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("listing");
       // ArrayList<Pair<Integer,String> >  model= new ArrayList<Pair<Integer,String> >();
        //for(int i = 0; i< c. )
        modelAndView.addObject("list", c.getAll());
        return modelAndView;
	}
	@Autowired
	public VideoSvcApi_impl(VideoCache c, iDManager idm, VideoFileManager vfm) {
		this.c = c;
		this.idm = idm;
		this.vfm = vfm;
	}
	
	@Override
	public String getVideoList_2(){
		// TODO Auto-generated method stub
		String p = "test";
		return p;
	}
	@Override
	public Collection<Video> getVideoList() {
		return c.getAll();
	}
	
	/*
	public ResponseEntity<Collection<Video>> getVideoList_3() {
		/*ArrayList<Video> v = new ArrayList<Video>();
		Video p = Video.create().withContentType("video/mpeg")
				.withDuration(123).withSubject("Mobile Cloud")
				.withTitle("Programming Cloud Services for ...").build();
		v.add(p);
		return ResponseEntity.ok().body(v);
		return ResponseEntity.ok().body(c.getAll());
		
	}*/
	
	@Override
	@PostMapping(VIDEO_SVC_PATH)
	public Video addVideo(@RequestBody Video v) {
		// TODO Auto-generated method stub
		System.out.println("in post req");
		long idt = idm.generate();
		v.setId(idt);
		v.setDataUrl("playback?id="+Long.toString(idt));
		c.addVideo(v);
		return v;
	}

	@Override
	public VideoStatus setVideoData(long id, TypedFile videoData) {
		// TODO Auto-generated method stub
		return null;
	}

	@PostMapping(VIDEO_DATA_PATH)
	public VideoStatus setVideoData_2(@PathVariable("id") long id, @RequestParam("data") MultipartFile videoData) {
		// TODO Auto-generated method stub
		System.out.println("in post data req");
		if(!idm.isGenerated((int) id))
            throw new ResponseStatusException(HttpStatus.SC_NOT_FOUND, "Id cannot be 0", null);
        
		InputStream inputStream = null;
		try {
			inputStream = videoData.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Video t = new Video();
			t.setId(id);
			vfm.saveVideoData(t, inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new VideoStatus(VideoState.READY);
		
		
	}
	
	
	@GetMapping(VIDEO_DATA_PATH)
	public ResponseEntity<StreamingResponseBody> getData_2(@PathVariable("id") long id) {
		System.out.println("in get data");
		Video t = new Video();
		t.setId(id);
		if(!vfm.hasVideoData(t)) {
			throw new ResponseStatusException(HttpStatus.SC_NOT_FOUND, "Data is not uploaded", null);
		}
		
		StreamingResponseBody resp = out->{
			vfm.copyVideoData(t, out);};
		System.out.println("initialized stream");
		HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", "video.mp4");
		//return ResponseEntity.ok().contentType(MediaType.MULTIPART_FORM_DATA).body(responseBody);
		//return new ResponseEntity<StreamingResponseBody>(resp, null, HttpStatus.SC_OK).header("Content-Type", "video/mp4");
		return ResponseEntity.ok().header("Content-Type", "video/mp4").headers(headers).body(resp);
			
				
	}
	
	@Override
	public
	Response getData(long id) {return null;}
	
}
