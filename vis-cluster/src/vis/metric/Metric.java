package vis.metric;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


public class Metric {

	
	public static double distance(byte[] a, byte[] b){
		double distance = 0;
		int l=a.length;
		
		int g1, g2;
		for(int i=0; i<l; i++){
			g1 = a[i];
			g2 = b[i];
			//System.out.println(g1 + "-" + g2);
			distance += Math.abs(g1-g2);
		}					
		return distance;
	}
	
	public static double cosim(String s1, String s2){
		return cosim(getFrequency(s1), getFrequency(s2));
	}
	
	public static double cosim(Map<String, Double> v1, Map<String, Double> v2) {
        Set<String> both = new HashSet<>(v1.keySet());
        both.retainAll(v2.keySet());
        double sclar = 0, norm1 = 0, norm2 = 0;
        for (String k : both) sclar += v1.get(k) * v2.get(k);
        for (String k : v1.keySet()) norm1 += v1.get(k) * v1.get(k);
        for (String k : v2.keySet()) norm2 += v2.get(k) * v2.get(k);
        return sclar / Math.sqrt(norm1 * norm2);
}
	
	public static HashMap<String,Double> getFrequency(String s){
		HashMap<String,Double> map = new HashMap<String,Double>();
		String b = s.replace(",", "");
		StringTokenizer st = new StringTokenizer(b, " ");
		while(st.hasMoreTokens()){
			String t = st.nextToken();
			Double d = map.get(t);
			if(d != null){
				map.put(t, d+1);
			}else{
				map.put(t, 1d);
			}
		}
		return map;
		
	}
	
	public static void main(String[] args){
		System.out.println(cosim("i boy", "i"));
	}
	

}
