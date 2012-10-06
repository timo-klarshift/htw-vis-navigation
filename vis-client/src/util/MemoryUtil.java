package util;

public class MemoryUtil {
	final Runtime runtime = Runtime.getRuntime();
	
	long usedMemory = -1;		// currently used memory
	long freeMemory = -1;		// free memory
	long totalMemory = -1;		// currently assigned total memory	
	long maxMemory = -1;		// maximum jvm memory available
	
	double usedMemoryMb = -1;		// currently used memory
	double freeMemoryMb = -1;		// free memory
	double totalMemoryMb = -1;		// currently assigned total memory	
	double maxMemoryMb = -1;		// maximum jvm memory available
	
	
	public MemoryUtil(){
		refresh();	
		print();
	}
	
	public void gc(){
		System.out.println("Calling Garbage Collector ...");
		System.gc();
	}
	
	public void refresh(){
		int mb = 1024*1024*100;
		 
		totalMemory = runtime.totalMemory();
		freeMemory = runtime.freeMemory();
		usedMemory = totalMemory-freeMemory;
		maxMemory = runtime.maxMemory();
		
		totalMemoryMb = (100*totalMemory) / mb;
		freeMemoryMb = (100*freeMemory) / mb;
		usedMemoryMb = (100*usedMemory) / mb;
		maxMemoryMb = (100*maxMemory) / mb;		
	}
	
	public void print(){
		refresh();
		System.out.println("++ Memory Stats ++");		
		System.out.println("JVM Total: " + maxMemoryMb + "MB | " + usedMemoryMb + "MB of " + totalMemoryMb + "MB used (" + (Math.round(100.0*freeMemory/(double)totalMemory)) + "% / " + freeMemoryMb + "MB free) ");
	}
	 
    public static void main(String [] args) {
    	MemoryUtil u = new MemoryUtil();
    	
    	
    	char[] a = new char[400*1024*1024];
    	u.print();
    	try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	u.print();
    	a = null;
    	char[] b = new char[200*1024*1024];
    	u.print();
        
    }
}