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
package com.rapidminer.example.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.tools.Ontology;


/**
 *  <p>This example set uses a mapping of indices to access the examples provided by the 
 *  parent example set. In contrast to the mapped example set, where the sorting would 
 *  have been disturbed for performance reasons this class simply use the given mapping.
 *  A convenience constructor exist to create a view based on the sorting based on a
 *  specific attribute.</p>
 *  
 *  @author Ingo Mierswa
 */
public class SortedExampleSet extends AbstractExampleSet {

	private static final long serialVersionUID = 3937175786207007275L;

	public static final String[] SORTING_DIRECTIONS = {
		"increasing",
		"decreasing"
	};
	
	public static final int INCREASING = 0;
	public static final int DECREASING = 1;

	private static class SortingIndex implements Comparable<SortingIndex> {
		
		private Object key;
		private int index;
		
		public SortingIndex(Object key, int index) {
			this.key   = key;
			this.index = index;
		}

		@Override
		public int hashCode() {
			if (key instanceof Double) {
				return ((Double)key).hashCode();
			} else if (key instanceof String) {
				return ((String)key).hashCode();
			} else {
				return 42;
			}
		}
		
		@Override
		public boolean equals(Object other) {
			if (!(other instanceof SortingIndex))
				return false;
			SortingIndex o = (SortingIndex)other;
			if (key instanceof Double) {
				return ((Double)key).equals(o.key);
			} else if (key instanceof String) {
				return ((String)key).equals(o.key);
			}
			return true;
		}
		
		public int compareTo(SortingIndex o) {
			if (key instanceof Double) {
				return ((Double)key).compareTo((Double)o.key);
			} else if (key instanceof String) {
				return ((String)key).compareTo((String)o.key);
			}
			return 0;
		}
		
		public int getIndex() { return index; }
		
		@Override
		public String toString() { return key + " --> " + index; }
	}
	
	
	/** The parent example set. */
	private ExampleSet parent;
	
    /** The used mapping. */
    private int[] mapping;
    
    public SortedExampleSet(ExampleSet parent, Attribute sortingAttribute, int sortingDirection) {
    	this.parent = (ExampleSet)parent.clone();
		List<SortingIndex> sortingIndex = new ArrayList<SortingIndex>(parent.size());
		
		int counter = 0;
		Iterator<Example> i = parent.iterator();
		while (i.hasNext()) {
			Example example = i.next();
			if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(sortingAttribute.getValueType(), Ontology.DATE_TIME)) {
				sortingIndex.add(new SortingIndex(Double.valueOf(example.getDateValue(sortingAttribute).getTime()), counter));
			} else if (sortingAttribute.isNominal()) {
				sortingIndex.add(new SortingIndex(example.getNominalValue(sortingAttribute), counter));
			} else {
				sortingIndex.add(new SortingIndex(Double.valueOf(example.getNumericalValue(sortingAttribute)), counter));
			}
			counter++;
		}
		
		Collections.sort(sortingIndex);
		
		int[] mapping = new int[parent.size()];
		counter = 0;
		Iterator<SortingIndex> k = sortingIndex.iterator();
		while (k.hasNext()) {
			int index = k.next().getIndex();
			if (sortingDirection == INCREASING) {
				mapping[counter] = index;
			} else {
				mapping[parent.size() - 1 - counter] = index;
			}
			counter++;
		}
		
		this.mapping = mapping;
    }
    
    /** Constructs an example set based on the given sort mapping. */
    public SortedExampleSet(ExampleSet parent, int[] mapping) {
    	this.parent = (ExampleSet)parent.clone();
        this.mapping = mapping; 
    }

    /** Clone constructor. */
    public SortedExampleSet(SortedExampleSet exampleSet) {
    	this.parent = (ExampleSet)exampleSet.parent.clone();
        this.mapping = new int[exampleSet.mapping.length];
        System.arraycopy(exampleSet.mapping, 0, this.mapping, 0, exampleSet.mapping.length);
    }

    @Override
	public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        if (!(o instanceof SortedExampleSet))
            return false;
        
        SortedExampleSet other = (SortedExampleSet)o;    
        if (this.mapping.length != other.mapping.length)
            return false;
        for (int i = 0; i < this.mapping.length; i++) 
            if (this.mapping[i] != other.mapping[i])
                return false;
        return true;
    }

    @Override
	public int hashCode() {
        return super.hashCode() ^ Arrays.hashCode(this.mapping);
    }
    
    /** Returns a {@link SortedExampleReader}. */
    public Iterator<Example> iterator() {
        return new SortedExampleReader(this);
    }

    /** Returns the i-th example in the mapping. */
    public Example getExample(int index) {
        if ((index < 0) || (index >= this.mapping.length)) {
            throw new RuntimeException("Given index '" + index + "' does not fit the mapped ExampleSet!");
        } else {
            return this.parent.getExample(this.mapping[index]);
        }
    }

    /** Counts the number of examples. */
    public int size() {
        return mapping.length;
    }

	public Attributes getAttributes() {
		return this.parent.getAttributes();
	}

	public ExampleTable getExampleTable() {
		return this.parent.getExampleTable();
	}
}
