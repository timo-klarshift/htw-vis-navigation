package org.htw.vis.clustering.kmeans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.htw.vis.clustering.feature.ByteFeatureFactory;
import org.htw.vis.clustering.feature.Feature;
import org.htw.vis.clustering.feature.FeatureFactory;
import org.htw.vis.clustering.feature.SemanticFeatureFactory;

/**
 * kmeans++
 * 
 * @author timo
 * 
 */
public class KMeans {
	private static final double MIN_CHANGERATE = 0.005;
	private int clusterCount;
	private final ArrayList<KClusterCenter> clusters = new ArrayList<KClusterCenter>();
	private final ArrayList<KData> dataSet = new ArrayList<KData>();
	private FeatureFactory factory;
	private boolean clustering = false;

	/**
	 * create kmeans
	 * @param clusterCount
	 * @param factory
	 */
	public KMeans(int clusterCount, FeatureFactory factory) {
		this.clusterCount = clusterCount;
		this.factory = factory;

	}

	public int getDataCount() {
		return dataSet.size();
	}

	public void addData(KData data) {
		assert data != null;
		assert data.getFeature() != null;
		dataSet.add(data);
	}
	
	private void initClusters(){
		Random random = new Random();
		Set<Integer> used = new HashSet<Integer>();
		clusterCount = Math.min(clusterCount, dataSet.size());
		while(clusters.size() < clusterCount){		
			int rid = random.nextInt(dataSet.size());
			if(used.contains(rid) == false){
				Feature rnd = dataSet.get(rid)
						.getFeature();
				Feature feature = factory.clone(rnd);
				KClusterCenter center = new KClusterCenter(feature);
				clusters.add(center);
				used.add(rid);
			}
		}
	}
	
	
	
	private void initClustersPlusPlus(){
		Random random = new Random();
		
		// get first random cluster center
		int rid = random.nextInt(dataSet.size());
		Feature selected = dataSet.get(rid).getFeature();
		Feature centerFeature = factory.clone(selected);
		KClusterCenter firstCenter = new KClusterCenter(centerFeature);
		clusters.add(firstCenter);
		
		
		Set<Integer> used = new HashSet<Integer>();
		used.add(rid);
		
		
		while(clusters.size() < clusterCount){
			double maxD = 0;
			int bestData = -1;
			
			
			
			for(int d=0; d<dataSet.size(); d++){
				if(used.contains(d) == false){
					double dist = 0;
					for(int c=0; c<clusters.size(); c++){
						dist += getDistance(clusters.get(c), dataSet.get(d));
					}
					
					if(dist > maxD){
						maxD = dist;
						bestData = d;
					}
					
					
				}
			}
			
			System.out.println("SELECT " + bestData + " as next cluster center // " + clusters.size() + " // " + maxD);
			
			used.add(bestData);
			Feature feature = factory.clone(dataSet.get(bestData).getFeature());
			KClusterCenter nextCenter = new KClusterCenter(feature);
			clusters.add(nextCenter);			
		}
	}

	public void cluster() {
		clustering = true;

		initClusters();
		//initClustersPlusPlus();

		int iteration = 0;
		int maxIterations = 100;
		int changes = 0;
		
		// reset data
		for(KData d : dataSet){
			if(d != null)
				d.reset();
		}

		while (clustering) {

			changes = 0;

			// loop data
			for (int d = 0; d < dataSet.size(); d++) {
				KData data = dataSet.get(d);
				if (data == null) {
					System.out.println("EROR " + d);
				} else {

					int cBest = getClusterId(data);

					// get prev assigned center
					int prevCluster = data.getClusterId();
					if (prevCluster != cBest) {
						if (prevCluster != -1)
							clusters.get(prevCluster).remove(d);

						// assign best center
						KClusterCenter bestCluster = clusters.get(cBest);
						bestCluster.add(d, data);

						dataSet.get(d).setClusterId(cBest);

						changes++;
					}
				}

			}

			// adjust centers
			for (int c = 0; c < clusterCount; c++) {
				KClusterCenter center = clusters.get(c);
				if (center.getDataCount() > 0) {
					List<Feature> featuresInCluster = new LinkedList<Feature>();
					for (KData da : clusters.get(c).getData()) {
						featuresInCluster.add(da.getFeature());
					}
					Feature meanFeature = factory.mean(featuresInCluster);
					clusters.get(c).setFeature(meanFeature);
				}
			}

			printStats();

			double changeRate = (double) changes / dataSet.size();
			System.out.println("Iteration " + iteration + " / changed = "
					+ changes + " / dC = " + changeRate + " / dataSize "
					+ dataSet.size());

			// iteration

			if (++iteration >= maxIterations || changeRate < MIN_CHANGERATE) {
				clustering = false;
			}
		}

		System.out.println("DONE.");
	}

	private void printStats() {
		for (int c = 0; c < clusterCount; c++) {
			System.out.println("Cluster " + c + " / "
					+ clusters.get(c).getDataCount() + " / ");
			/*
			 * for(KData d : clusters.get(c).getData()){ System.out.println("\t"
			 * + d.getFeature()); }
			 */
		}
	}

	private double getDistance(KClusterCenter center, KData data) {		
		Feature a = center.getFeature();
		Feature b = data.getFeature();
		return a.getDistance(b);
	}

	public static void main(String[] args) {
		FeatureFactory factory = new SemanticFeatureFactory(32);
		KMeans means = new KMeans(32, factory);

		// add data
		for (int d = 0; d < 1 * 100 * 100; d++) {
			Feature f = factory.generate();
			/*
			 * f.set(0, (byte)(Math.random() > 0.5 ? 0 : 1)); f.set(1,
			 * (byte)(Math.random() > 0.1 ? 0 : 1)); f.set(2,
			 * (byte)(Math.random() > 0.25 ? 0 : 1));
			 */
			f.randomInit();
			means.addData(new KData(f));
		}

		System.out.println("INI");

		// cluster
		means.cluster();			
	}

	public KClusterCenter getCluster(int c) {
		return clusters.get(c);
	}

	public int getClusterId(KData kData) {
		// loop centers
		int cBest = -1;
		double minD = Double.MAX_VALUE;
		for (int c = 0; c < clusters.size(); c++) {
			double dist = getDistance(clusters.get(c), kData);
			if (dist < minD) {
				minD = dist;
				cBest = c;
			}
		}
		return cBest;
	}

	public int getClusterCount() {
		return clusterCount;
	}

}
