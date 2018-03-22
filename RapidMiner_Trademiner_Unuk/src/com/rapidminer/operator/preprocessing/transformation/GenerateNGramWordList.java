package com.rapidminer.operator.preprocessing.transformation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.OutputPort;
//import com.rapidminer.operator.text.WordList;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeString;

public class GenerateNGramWordList extends Operator {


	private static final String PARAMETER_TEXT_ATTRIBUTE = "text_attribute";
	//private static final String [] punctuation={",",".",";","!",":","?","@","&","#","(",")","-","$"};
	private InputPort exampleSetInput = getInputPorts().createPort("example set");
	private OutputPort wordlistOutput = getOutputPorts().createPort("wordlist");
		
	
	public static final String PARAMETER_FILE = "file";
	
	
	public GenerateNGramWordList(OperatorDescription description) {
		super(description);
	   // getTransformer().addPassThroughRule(exampleSetInput, exampleSetOutput);
	    
	
	}
    @SuppressWarnings("unused")
	private class Document
    {
    	List <NGram> ngrams = new ArrayList<NGram>();
    	int totalNumberOfTerms;
    	
    }
   
    @SuppressWarnings("rawtypes")
	private class NGram implements Comparable {
    	public Integer size;
    	public String ngram;
    	public int count;
    	public NGram()
    	{}
    	public NGram(NGram ngram)
    	{
    		this.size = ngram.size;
    		this.ngram = ngram.ngram;
    		
    	}
		@Override
		public int compareTo(Object o) {
			if (o ==null )
				return 0;
			if (! (o instanceof NGram))
				return 0;
			
			NGram otherNGram = (NGram)o;
			//-1 for desc
			return -1 * NGram.this.size.compareTo(otherNGram.size);
			
		}
		
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((ngram == null) ? 0 : ngram.hashCode());
			return result;
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NGram other = (NGram) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (ngram == null) {
				if (other.ngram != null)
					return false;
			} else if (!ngram.equals(other.ngram))
				return false;
			return true;
		}


		@Override
		public String toString()
		{
			return ngram;
			
		}


		private GenerateNGramWordList getOuterType() {
			return GenerateNGramWordList.this;
		}
		
    	
    }
	private List<NGram> loadDictionary() throws Exception
	{
		
		InputStreamReader reader=null;
		List<NGram> dictionaryNGramsOrderedBySize = new ArrayList<NGram>();
		try {
				reader = new InputStreamReader(new FileInputStream(getParameterAsFile(PARAMETER_FILE))); //, Encoding.getEncoding(this));
				BufferedReader br = new BufferedReader(reader);
				String line=null;
				
				 
				
				while ((line=br.readLine())!=null)
				{
					line = line.trim();
					if (line.length()==0)
						continue;
					
					NGram ngram = new NGram();
					ngram.size = line.length();
					ngram.ngram= line;
					dictionaryNGramsOrderedBySize.add(ngram);
					
				}
				
				
				Collections.sort(dictionaryNGramsOrderedBySize);
				
				
		}
		finally {
			
			if (reader!=null)
				reader.close();
			
		}
		return dictionaryNGramsOrderedBySize;
	}
	public void doWork() throws OperatorException {
	
	    ExampleSet input = exampleSetInput.getData(ExampleSet.class); 
	    List <NGram> wordList = new ArrayList<NGram>();
	    List <Document> corpora = new ArrayList <Document>();
	    
	    //TODO Remove this and use exampleset
	//    WordList wordListOutput = new WordList();
	    
	    
	    String attName = getParameterAsString(PARAMETER_TEXT_ATTRIBUTE);
		Attribute att = input.getAttributes().get(attName);
	    
		try {
			List<NGram> orderedNGramsBySize =loadDictionary();
			
			for(Example e : input)
			{
				if (orderedNGramsBySize.size()==0)
					break;
				corpora.add(processTokensInDocument(e,att,orderedNGramsBySize,wordList));
				System.out.print(".");
			}
			
		
		} catch (FileNotFoundException e) {
			throw new UserError(this, 301, getParameterAsFile(PARAMETER_FILE).getPath());
		} catch (IOException e) {
			throw new UserError(this, 302, getParameterAsFile(PARAMETER_FILE).getPath(), e.getMessage());
		
		} catch (Exception e) {
		throw new UserError(this, 303, "", e.getMessage());
		}
	
		//TODO EXTRACT EXAMPLESET FROM THE wordList, original exampleset, and documents
		//TODO CALCULATE TF-IDF (http://www.tfidf.com/)
		//wordlistOutput.deliver(wordListOutput);
		
	}
	protected Document  processTokensInDocument(Example example,Attribute att, List<NGram> dictionaryNGramsOrderedBySize, List <NGram> wordList) throws IOException
	{
	
		
		String textLower = example.getValueAsString(att).toLowerCase();
		textLower = cleanGarbage(textLower);
		Document doc= new Document();
		//TODO compute totalNumberOfTerms for doc
		
		for (int i=0;i<dictionaryNGramsOrderedBySize.size();++i) {
		
			NGram ngram= dictionaryNGramsOrderedBySize.get(i);
			String toSearch = ngram.ngram.toLowerCase();
			
			if (isAnAchronym(ngram.ngram))
				toSearch += " ";
			
			if (textLower.contains(toSearch))
			{
				String previous=textLower;
				textLower = textLower.replace(toSearch,"" );

				if (textLower.contains(toSearch))
					throw new RuntimeException("Text for "+ toSearch +" not replaced");

				
				
				//TODO CALCULATE THE CORRECT COUNT FOR THE DOCUMENT FOR TF
				int count = 1;
				
				//NGRAM
				NGram newDocumentNgram = new NGram(ngram);
				newDocumentNgram.count=count;
				doc.ngrams.add(newDocumentNgram); 

				//Compute the number of documents with term on it for IDF
				if (wordList.contains(ngram))
				{
					NGram newNgram =wordList.get(wordList.indexOf(ngram));
					newNgram.count +=1; 
				}
				else
				{
					NGram newNgram = new NGram(ngram);
					newNgram.count=1; 
					wordList.add(newNgram);
				}
				//End of compute for idf
			}
				
		
		}
		return doc;
		
		
		
	}
	/**
	 * TRATAR MUITOS ESPACOS, TABULADORES, QUEBRAS DE LINHA 
	 *TODO TRATAR ACENTUACAO
	 * @param textLower
	 * @return
	 */
	private String cleanGarbage(String textLower) {
		
		
		textLower = textLower.replace("\r\n"," ");
		textLower = textLower.replace("\n"," ");
		textLower = textLower.replace("\t"," ");
		textLower = textLower.replace("  "," ");
		return textLower;
	}

	private boolean isAnAchronym(String ngram)
	{
		String upperCase = ngram.toUpperCase();
		
		return upperCase.equals(ngram);
		
	}
	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeFile(PARAMETER_FILE, "Filename for the word dictionary (one word per line).", "fil", false));

		types.add(new ParameterTypeString(PARAMETER_TEXT_ATTRIBUTE, "The name of the text attribute.", false));
		
		
		return types;
	}
	
}
