/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2012 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.operator.preprocessing.transformation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.RandomAccess;

import com.rapidminer.tools.container.Tupel;
import com.rapidminer.tools.math.container.BoundedPriorityQueue;
import com.rapidminer.tools.math.container.GeometricDataCollection;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 * This class is an implementation of the GeometricDataCollection interface, which
 * searches all datapoints linearly for the next k neighbours. Hence O(n) computations
 * are required for this operation.
 * 
 * @author Sebastian Land
 * 
 * @param <T> This is the type of value with is stored with the points and retrieved on nearest
 * neighbour search
 */
public class LinearListFixed<T extends Serializable> implements GeometricDataCollection<T>, RandomAccess {

	private static final long serialVersionUID = -746048910140779285L;

	DistanceMeasure distance;
	ArrayList<double[]> samples = new ArrayList<double[]>();
	ArrayList<T> storedValues = new ArrayList<T>();
	double[][] latestValues ;
	
	
	public LinearListFixed(DistanceMeasure distance) {
		this.distance = distance;
	}
	
	public void add(double[] values, T storeValue) {
		this.samples.add(values);
		this.storedValues.add(storeValue);
	}

	public Collection<T> getNearestValues(int k, double[] values) {
		BoundedPriorityQueue<Tupel<Double, T>> queue = new BoundedPriorityQueue<Tupel<Double, T>>(k);
		int i = 0;
		for (double[] sample: this.samples) {
			queue.add(new Tupel<Double, T>(distance.calculateDistance(sample, values), storedValues.get(i)));
			i++;
		}
		
		Collection<T> result = new ArrayList<T>(k);
		for (Tupel<Double, T> tupel: queue) {
			result.add(tupel.getSecond());
		}
		return result;
	}

	public Collection<Tupel<Double, T>> getNearestValueDistances(int k, double[] values) {
		BoundedPriorityQueue<TupelVector<Double, T>> queue = new BoundedPriorityQueue<TupelVector<Double, T>>(k);
		latestValues = new double[k][];
		
		int i = 0;
		for (double[] sample: this.samples) {
			queue.add(new TupelVector<Double, T>(distance.calculateDistance(sample, values), storedValues.get(i),sample));
			i++;
		}
		
		Collection<Tupel<Double, T>> result = new ArrayList<Tupel<Double, T>>(k);
		i=0;
		for (TupelVector<Double, T> tupel: queue) {
			
			Tupel<Double, T> newTupel= new Tupel<Double, T> ((Double)tupel.getFirst(),(T)tupel.getSecond());
			latestValues[i]= tupel.getValues();
			
			result.add(newTupel);
			i++;
		} 
		return result;
	}

	public Collection<Tupel<Double, T>> getNearestValueDistances(double withinDistance, double[] values) {
		ArrayList<Tupel<Double, T>> queue = new ArrayList<Tupel<Double, T>>();
		int i = 0;
		for (double[] sample: this.samples) {
			double currentDistance = distance.calculateDistance(sample, values);
			if (currentDistance <= withinDistance)
				queue.add(new Tupel<Double, T>(currentDistance, storedValues.get(i)));
			i++;
		}
		return queue;
	}

	public Collection<Tupel<Double, T>> getNearestValueDistances(double withinDistance, int butAtLeastK, double[] values) {
		Collection<Tupel<Double, T>> result = getNearestValueDistances(withinDistance, values);
		if (result.size() < butAtLeastK)
			return getNearestValueDistances(butAtLeastK, values);
		return result;
	}

	public int size() {
		return samples.size();
	}

	public Iterator<T> iterator() {
		return storedValues.iterator();
	}

	public T get(int index) {
		return storedValues.get(index);
	}
	
	public double[][] getLatestValues()
	{
		return latestValues;
	}
	
}



class TupelVector <T1 extends Comparable<T1>, T2> extends Tupel
{

	private double values[];
	
	public TupelVector(T1 t1, T2 t2, double values[]) {
		super(t1, t2);
		this.values = values;
	}

	public double[] getValues() {
		return values;
	}

	public void setValues(double[] values) {
		this.values = values;
	}
	
}