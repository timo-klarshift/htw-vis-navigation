package org.htw.vis.setup;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.htw.vis.config.EnvDbConfig;
import org.htw.vis.db.Database;

public abstract class SetupProcess {
	protected final Logger log = Logger.getLogger(this.getClass());
	private String name;	
	private float progress = 0;
	private String progressTitle;
	
	public SetupProcess(String name){
		this.name = name;		
		
		// do some setup
		Logger.getRootLogger().setLevel(Level.INFO);
		
		setProgress("Init", 0);
		init();
		setProgress("Init", 1);
	}
	
	public String toString(){
		return "Process :: " + name;
	}
	
	/**
	 * process
	 */
	public void process(){
		// process
		log.info("Processing ...");
		doProcess();
		log.info("Processing ... DONE.");
		
		// clean up
		shutdown();
	}
	
	protected abstract void doProcess();
	protected abstract void shutdown();
	protected abstract void init();
	
	public void setProgress(float progress){
		setProgress(progressTitle, progress);
	}
	
	public void setProgress(String title, float progress){
		this.progress = progress;
		this.progressTitle = title;
		
		int pValue = Math.round(100*progress);
		log.info("Progress (" + title + "): " + pValue + " %"); 
	}
	
	public float getProgress(){
		return progress;
	}
	

	
	protected void registerDatabases(){
		// register databases
		Database.register(Database.FEATURES, EnvDbConfig.get().featureConfig());
		Database.register(Database.LAYERS, EnvDbConfig.get().layerConfig());
	}
}
