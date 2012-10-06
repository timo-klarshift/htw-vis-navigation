package org.htw.vis.client.main

import org.htw.vis.api.Api

class ApiTest {

	static main(args) {
		def api = new Api("http://localhost:8080/vis-web") // leave blank for remote usage
		//def api = new Api() // leave blank for remote usage
		/*api.search("apple", 3000).each {
			println it
			//println "\t " + api.getDetails(it.fotoliaId)
		}*/
		
		api.list(10).each{
			println it
		}
		
		api.list(10).each{
			println it
		}
	}
}