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
package com.rapidminer.operator.preprocessing.join;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.OperatorVersion;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetPrecondition;
import com.rapidminer.operator.ports.metadata.ExampleSetUnionRule;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.container.Pair;


/**
 * <p>
 * Build the join of two example sets.
 * </p>
 * <p>
 * Please note that this check for duplicate attributes will only be applied 
 * for regular attributes. Special attributes of the second input example set
 * which do not exist in the first example set will simply be added. If they 
 * already exist they are simply skipped.
 * </p>
 * 
 * @author Ingo Mierswa
 */
public abstract class AbstractExampleSetJoin extends Operator {

	public static final OperatorVersion VERSION_SWAPPED_INPUT_PORTS = new OperatorVersion(5,1,8);
	
	protected static final String LEFT_EXAMPLE_SET_INPUT = "left";
	protected static final String RIGHT_EXAMPLE_SET_INPUT = "right";
	
	private InputPort leftInput = getInputPorts().createPort(LEFT_EXAMPLE_SET_INPUT);
	private InputPort rightInput = getInputPorts().createPort(RIGHT_EXAMPLE_SET_INPUT);
	private OutputPort joinOutput = getOutputPorts().createPort("join");
	
	/** The parameter name for &quot;Indicates if double attributes should be removed or renamed&quot; */
	public static final String PARAMETER_REMOVE_DOUBLE_ATTRIBUTES = "remove_double_attributes";
	
    /** Helper class to find the correct data for all union attributes. */
    protected static class AttributeSource {
    	
        protected static final int FIRST_SOURCE = 1;

        protected static final int SECOND_SOURCE = 2;

        protected int source;

        protected Attribute attribute;

        public AttributeSource(int source, Attribute attribute) {
            this.source = source;
            this.attribute = attribute;
        }
        
        protected int getSource() {
            return source;
        }

        protected Attribute getAttribute() {
            return attribute;
        }
    }

    public AbstractExampleSetJoin(OperatorDescription description) {
        super(description);
        leftInput.addPrecondition(new ExampleSetPrecondition(leftInput));
    	rightInput.addPrecondition(new ExampleSetPrecondition(rightInput));

    	getTransformer().addRule(new ExampleSetUnionRule(rightInput, leftInput, joinOutput, "_from_ES2") {
        	 @Override
        	 protected String getPrefix() {
        		 return getParameterAsBoolean(PARAMETER_REMOVE_DOUBLE_ATTRIBUTES) ? null : "_from_ES2";
        	 }
        });
    }
    
	public InputPort getLeftInput() {
		return leftInput;
	}

	public InputPort getRightInput() {
		return rightInput;
	}
	
	public OutputPort getJoinOutput() {
		return joinOutput;
	}


    protected abstract MemoryExampleTable joinData(ExampleSet es1, ExampleSet es2, List<AttributeSource> originalAttributeSources, List<Attribute> unionAttributeList) throws OperatorException;

    protected abstract boolean isIdNeeded();
    
    @Override
	public void doWork() throws OperatorException {

    	ExampleSet es1;
    	ExampleSet es2;
    	if (getCompatibilityLevel().isAtMost(VERSION_SWAPPED_INPUT_PORTS)) {
    		/* please note the order of calls: As a result from the transformation from process tree to process flow
    		 * this error was introduced. We introduced an incompatibly version change to overcome this.
    		 */
    		es2 = leftInput.getData(ExampleSet.class);
            es1 = rightInput.getData(ExampleSet.class);
    	} else {
    		/* This is the correct order used by all operators that using a more current version than VERSION_SWAPPED_INPUT_PORTS */
    		es1 = leftInput.getData(ExampleSet.class);
            es2 = rightInput.getData(ExampleSet.class);
    	}
        
        if (this.isIdNeeded()) {
            Attribute id1 = es1.getAttributes().getId();
            Attribute id2 = es2.getAttributes().getId();

            // sanity checks
            if ((id1 == null) || (id2 == null)) {
                throw new UserError(this, 129);
            }
            if (!Ontology.ATTRIBUTE_VALUE_TYPE.isA(id1.getValueType(), id2.getValueType()) 
        	    && !Ontology.ATTRIBUTE_VALUE_TYPE.isA(id2.getValueType(), id1.getValueType()) ){
//            if (id1.getValueType() != id2.getValueType()) {
                throw new UserError(this, 120, new Object[] {
                        id2.getName(), Ontology.VALUE_TYPE_NAMES[id2.getValueType()], Ontology.VALUE_TYPE_NAMES[id1.getValueType()]
                });
            }
        }
        
        Set<Pair<Integer,Attribute>> excludedAttributes = getExcludedAtttributes(es1, es2);
        
        // regular attributes
        List<AttributeSource> originalAttributeSources = new LinkedList<AttributeSource>();
        List<Attribute> unionAttributeList = new LinkedList<Attribute>();
        for (Attribute attribute : es1.getAttributes()) {
        	if (!excludedAttributes.contains(new Pair<Integer,Attribute>(AttributeSource.FIRST_SOURCE, attribute))) {
	            originalAttributeSources.add(new AttributeSource(AttributeSource.FIRST_SOURCE, attribute));
	            unionAttributeList.add((Attribute) attribute.clone());
        	}
        }
        
        for (Attribute attribute : es2.getAttributes()) {
        	if (!excludedAttributes.contains(new Pair<Integer,Attribute>(AttributeSource.SECOND_SOURCE, attribute))) {
	            Attribute cloneAttribute = (Attribute) attribute.clone();
	            if (containsAttribute(unionAttributeList, attribute)) { // in list...
	                if (!getParameterAsBoolean(PARAMETER_REMOVE_DOUBLE_ATTRIBUTES)) { // ... but should not be removed --> rename
	                    originalAttributeSources.add(new AttributeSource(AttributeSource.SECOND_SOURCE, attribute));
	                    cloneAttribute.setName(cloneAttribute.getName() + "_from_ES2");
	                    if (containsAttribute(unionAttributeList, cloneAttribute)) {
	                        cloneAttribute.setName(cloneAttribute.getName() + "_from_ES2");
	                    }
	                    unionAttributeList.add(cloneAttribute);
	                } // else do nothing, i.e. remove
	            } else { // not in list --> add
	                originalAttributeSources.add(new AttributeSource(AttributeSource.SECOND_SOURCE, attribute));
	                unionAttributeList.add(cloneAttribute);
	            }
        	}
        }

        // special attributes
        Map<Attribute, String> unionSpecialAttributes = new HashMap<Attribute, String>();
        Set<String> usedSpecialAttributes = new HashSet<String>();
        
        // first example set's special attributes
        Iterator<AttributeRole> s = es1.getAttributes().specialAttributes();
        while (s.hasNext()) {
            AttributeRole role = s.next();
            Attribute specialAttribute = role.getAttribute();
            Attribute specialAttributeClone = (Attribute) specialAttribute.clone();
            originalAttributeSources.add(new AttributeSource(AttributeSource.FIRST_SOURCE, specialAttribute));
            unionAttributeList.add(specialAttributeClone);
            unionSpecialAttributes.put(specialAttributeClone, role.getSpecialName());
            usedSpecialAttributes.add(role.getSpecialName());
        }
        // second example set's special attributes
        s = es2.getAttributes().specialAttributes();
        while (s.hasNext()) {
            AttributeRole role = s.next();
            String specialName = role.getSpecialName();
            Attribute specialAttribute = role.getAttribute();
            if (!usedSpecialAttributes.contains(specialName)) { // not there
                originalAttributeSources.add(new AttributeSource(AttributeSource.SECOND_SOURCE, specialAttribute));
                Attribute specialAttributeClone = (Attribute) specialAttribute.clone();
                unionAttributeList.add(specialAttributeClone);
                unionSpecialAttributes.put(specialAttributeClone, specialName);
                usedSpecialAttributes.add(specialName);
            } else {
                logWarning("Special attribute '" + specialName + "' already exist, skipping!");
            }
        }

        // join data
        MemoryExampleTable unionTable = joinData(es1, es2, originalAttributeSources, unionAttributeList);

        // create new example set
        joinOutput.deliver(unionTable.createExampleSet(unionSpecialAttributes));
    }

    /**
     * Returns a set of original attributes which will not be copied to the output example set.
     * The default implementation returns an empty set. 
     */
    protected Set<Pair<Integer,Attribute>> getExcludedAtttributes(ExampleSet es1, ExampleSet es2) throws OperatorException {
    	return new HashSet<Pair<Integer,Attribute>>();
    }

	/**
     * Returns true if the list already contains an attribute with the given name. The method contains from List cannot be used since the equals method of Attribute also checks for the same table
     * index which is not applicable here.
     */
    public boolean containsAttribute(List<Attribute> attributeList, Attribute attribute) {
        Iterator<Attribute> i = attributeList.iterator();
        while (i.hasNext()) {
            if (i.next().getName().equals(attribute.getName()))
                return true;
        }
        return false;
    }
    
    @Override
    public OperatorVersion[] getIncompatibleVersionChanges() {
    	return new OperatorVersion[] {VERSION_SWAPPED_INPUT_PORTS};
    }

    @Override
	public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeBoolean(PARAMETER_REMOVE_DOUBLE_ATTRIBUTES, "Indicates if double attributes should be removed or renamed", true));
        return types;
    }
}
