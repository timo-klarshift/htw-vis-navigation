package org.htw.vis.api

import org.htw.vis.layer.INode

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper


/**
 * 
 * @author timo
 *
 */
class Api {
	String apiBase = "http://141.45.146.52:9900/vis-web"
	String sessionId = null

	/**
	 * 
	 */
	public Api(String apiBase = null){
		if(apiBase){
			this.apiBase = apiBase;
		}
	}


	/**
	 * search for images
	 * 
	 * @param query
	 * @param minSimilarity
	 * @return
	 */
	public List<ApiImage> search(query, int max = 100, float minSimilarity = 0){
		String words = "";

		if(query instanceof String){
			words = query
		}

		if(query instanceof INode){
			words = getDetails(((INode)query).getFotoliaId())?.w
		}

		def results = getRequest("/image/search", [q: words, max: max, s: minSimilarity])?.data?.collect{
			return new ApiImage(it.id, it.p, it.s, it.x, it.y)
		}

		return results?.toList()
	}

	/**
	 * search for images
	 *
	 * @param query
	 * @param minSimilarity
	 * @return
	 */
	public List<ApiImage> list(int max = 200){

		def results = getRequest("/image/list", [max: max])?.data?.collect{
			new ApiImage(it.id, it.p, 0, it.x, it.y)
		}

		return results?.toList()
	}

	public void startSession(int width, int height){
		getRequest("/session/start", [width: width, height: height])
	}
	
	public void stopSession(){
		getRequest("/session/stop")
	}

	/**
	 * search for images
	 *
	 * @param query
	 * @param minSimilarity
	 * @return
	 */
	public List<ApiImage> shift(int x, int y){
		def response = getRequest("/image/shift", [x: x, y: y])
		println response

		def images = response?.data?.collect{
			new ApiImage(it.id, it.p, 0, it.x, it.y)
		}

		return images?.toList()
	}

	/**
	 * get image details
	 * @param id
	 * @return
	 */
	public Object getDetails(Integer id){
		return getRequest("/api/detail/$id").data
	}

	/**
	 * perform a request
	 * @param path
	 * @param params
	 * @return
	 */
	public Object getRequest(path, Map params = [:]){
		String uri = "$apiBase$path?" + params?.collect{"${it.key}=${URLEncoder.encode(it.value.toString(), 'utf-8')}"}.join("&")
		System.out.println("Request to: " + uri);
		try{
			URL url = new URL(uri)
			HttpURLConnection con = url.openConnection()

			// set cookie
			if(sessionId){
				con.setRequestProperty("Cookie", "JSESSIONID=$sessionId")
			}

			def response = new JsonSlurper().parseText(con.getInputStream().getText())
			def data = response.data

			println "Received from " + response.sessionId + " // context = " + response.context
			sessionId = response.sessionId

			return response
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * post data
	 * @param path
	 * @return
	 */
	public Object postRequest(path, Map params = [:]){
		JsonBuilder json = new JsonBuilder();
		json(params)
		String jsonData = json.toString()

		// get uri
		String uri = "$apiBase$path?"
		System.out.println("Request to: " + uri + " // DATA = $jsonData");

		try{
			URL url = new URL(uri)
			HttpURLConnection con = url.openConnection()
			con.setDoInput(true)
			con.setDoOutput(true)
			con.setRequestMethod("POST")
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")

			// set cookie
			if(sessionId){
				con.setRequestProperty("Cookie", "JSESSIONID=$sessionId")
			}

			// write data
			con.getOutputStream() << "json=$jsonData"

			def response = new JsonSlurper().parseText(con.getInputStream().getText())
			def data = response.data

			return response
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
