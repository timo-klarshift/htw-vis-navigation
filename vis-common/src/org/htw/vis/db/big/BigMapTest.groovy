package org.htw.vis.db.big

import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.htw.vis.matching.ImageMatching
import org.htw.vis.matching.MatchParty;


class BigMapTest implements BigMapIteratorCallback<MatchParty>{
	@Override
	public void onKeyValue(Integer key, MatchParty value) {
		// TODO Auto-generated method stub
		println "$key = $value"
	}

	static main(args) {
		Logger.getRootLogger().setLevel(Level.INFO);
							
		def match = new ImageMatching()
		int max = 1000*10
		max.times{ int id ->
			def p = new MatchParty(id)
			
			(8).times{				
				int other = Math.random()*max-1;
				if(!id.equals(other))
					p.prefer(other, (float)Math.random())
			}
			
			match.addParty(p)	
						
		}
		
		
		
		match.match()
		
		match.getFreeParties().each{
			println it			
		}
		
		println "FREE " + match.getFreeParties().size()
		println "ITS " + match.getIterationCount()
		
		return
		
		
		
		
		
		
		
		
		def bigmap = new BigMap<Integer>("test", BigMap.INTEGER)
		
		//bigmap.clear()		
		
		// write 
		if(bigmap.size() == 0){
			(1*1000).times{
				bigmap.put(it, (int)(Math.random()*10000))							
			}	
		}
		
		// reading
		3.times{
			def start = new Date().time
			10.times{
				3000.times{
					println bigmap.get(it)
				}
			}	
			println "READ WITHOUT CACHE in ${new Date().time - start} ms"
		}			
		
		bigmap.putAll([1: 2, 4: 8]);
		
		
		assert bigmap.get(1) == 2
		assert bigmap.containsKey(4)
		assert bigmap.remove(1) == 2
		
		
		println "done"
		
		// count
		println bigmap.size();
		
		bigmap.shutdown();			
		
		def m = new BigMap<Double>("suckermap", BigMap.BLOB)
		1000.times { m.put(it, new Double(0)) }
		1000.times { println m.get(it) }
		1000.times { println m.get(it) }
				
		
		m.shutdown()
		
		
		
	}

	

}
