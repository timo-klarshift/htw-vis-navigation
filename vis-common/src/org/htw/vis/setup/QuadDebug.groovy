package org.htw.vis.setup

import org.htw.vis.layer.NetworkNode
import org.htw.vis.layer.ZoomLayer

import groovy.xml.MarkupBuilder

class QuadDebug {
	public QuadDebug(List<Pairer.Quad> quads, ZoomLayer layer){
		File f = new File("test-${layer.getLOD()}.html")
		f.delete();
		def fs = new FileWriter(f)
		
		def mb = new MarkupBuilder(fs)
		mb.html{
			quads[0..99].each{ Pairer.Quad q ->
				div(style: 'margin: 20px;'){
					q.items.each{ i ->
						NetworkNode n = layer.getNodeById(i)
						img(style: 'float: left; width: 100px;', src: "${n.getImageSource()}")
					}
					div(style: 'clear: both;'){
						mb.yield(" ", false);
					}
				}
				
			}
			
			quads[quads.size()-1-100..quads.size()-1].each{ Pairer.Quad q ->
				div(style: 'margin: 20px;'){
					q.items.each{ i ->
						NetworkNode n = layer.getNodeById(i)
						img(style: 'float: left; width: 100px;', src: "${n.getImageSource()}")
					}
					div(style: 'clear: both;'){
						mb.yield(" ", false);
					}
				}
				
			}
		}
		
		fs.close()
	}
}
