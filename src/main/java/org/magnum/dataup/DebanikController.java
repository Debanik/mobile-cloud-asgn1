package org.magnum.dataup;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class DebanikController {

	private static final AtomicLong currentId = new AtomicLong(0L);

	private Map<Long, Video> videos = new HashMap<>();

	@ResponseBody
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public Collection<Video> getVideoList() {
		return videos.values();
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.GET)
	public void getData(@PathVariable("id") long id, HttpServletResponse response)
			throws Exception {
		VideoFileManager videoData = VideoFileManager.get();

		try {
			videoData.copyVideoData(videos.get(id), response.getOutputStream());
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@ResponseBody
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public Video addVideoMetadata(@RequestBody Video v, HttpServletRequest request) {
		v.setId(currentId.incrementAndGet());
		v.setDataUrl(getUrlBaseForLocalServer(request) + "/" + VideoSvcApi.VIDEO_SVC_PATH + v.getId() + "/data");
		videos.put(v.getId(), v);
		return v;
	}

	@ResponseBody
	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.POST)
	public VideoStatus addVideoData(@PathVariable("id") long id,
									@RequestParam MultipartFile data,
									HttpServletResponse response) throws IOException {
		VideoFileManager videoData = VideoFileManager.get();
		try {
			videoData.saveVideoData(videos.get(id), data.getInputStream());
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
		return new VideoStatus(VideoState.READY);
	}

	private String getUrlBaseForLocalServer(HttpServletRequest request) {
		String baseURL = "http://" + request.getServerName()
				+ ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "");
		return baseURL;
	}

}