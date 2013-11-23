package edu.isi.pfindr.learn.maxent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import cc.mallet.pipe.Pipe;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.Label;
import cc.mallet.types.LabelAlphabet;
import edu.isi.pfindr.learn.model.Model;
import edu.isi.pfindr.learn.model.ModelFactory;


/*
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * * MaxEntInputPipe.java 
 * 
 * InputPipe for MaxEntDistTrainer. All transformations on the input data are done here(all input is passed through the pipe), before training
 *
 * @author fuu (original)
 * @author sharma@isi.edu (modifications + additions)
 * 
 */

public class MaxEntInputPipe extends Pipe implements Serializable{

	private final double TRUE = 1.0;

	StandardAnalyzer analyzer = null;
	Model model =  ModelFactory.createModel();
	static final long serialVersionUID = 42L;
	
	public MaxEntInputPipe() {
		super(new Alphabet(), new LabelAlphabet());
	}

	/*
	 * Process the input before learning
	 */
	@Override
	public Instance pipe(Instance inst) {
		
		StringBuilder dataBuilder = new StringBuilder();
		dataBuilder.append(((String) inst.getData()).split("\\t")[0]); //No need to expand the user-entered string again, it is only done once
		dataBuilder.append("\t");
		dataBuilder.append(((String) inst.getData()).split("\\t")[1]); 

		//System.out.println("DataBuildr:" + dataBuilder.toString());
		Alphabet alphabet = getAlphabet();
		Label label = ((LabelAlphabet) getTargetAlphabet()).lookupLabel("0", true);
		if (inst.getTarget().toString().equals("1")) {
			label = ((LabelAlphabet) getTargetAlphabet()).lookupLabel("1", true);
		}

		//assign indices to each feature
		Map<String, Double> featList = extractFeatures(dataBuilder.toString());
		List<Integer> indiceList = new ArrayList<Integer>();
		List<Double> valueList = new ArrayList<Double>();
		int index = 0;
		for (Entry<String, Double> feature : featList.entrySet()) {
			// System.out.println(f);
			if (isTargetProcessing()) {
				//System.out.println("feature.getKey(): "+ feature.getKey() + "  feature.getValue()"+ feature.getValue());
				index = alphabet.lookupIndex(feature.getKey(), true); //create it, if it does not exist
				indiceList.add(index); //add the index
				valueList.add(feature.getValue()); //add the feature value
			} else {
				index = alphabet.lookupIndex(feature.getKey(), false); //do not create it, if it does not exist
				if (index != -1) { //if there is already an index for that feature
					indiceList.add(index); //add the index
					valueList.add(feature.getValue()); //add the corresponding feature value
				}
			}
		}
		int[] indice = new int[indiceList.size()];
		double[] values = new double[valueList.size()];
		for (int i = 0; i < indiceList.size(); i++) {
			indice[i] = indiceList.get(i);
			values[i] = valueList.get(i);
			//System.out.println("Indice:"+ indice[i] + "value:"+ values[i]);
		}

		// System.out.println(indiceList);
		FeatureVector fv = new FeatureVector(alphabet, indice, values);
		inst.setData(fv);
		inst.setTarget(label);

		return inst;
	}

	/*
	 * Construct a feature list for weighted Jaccard similarity. 
	 */
	private Map<String, Double> extractFeatures(String data) {

		//System.out.println("Extracting Features .."+ data);
		String[] v = data.split("\\t");
		Set<String> v0 = new HashSet<String>();
		v0.addAll(Arrays.asList(v[0].trim().split("\\s+")));
		
		Set<String> v1 = new HashSet<String>();
		v1.addAll(Arrays.asList(v[1].trim().split("\\s+"))); //already pre-processed expanded version from database
		//System.out.println("Size of user entered set after tokenization bi-gram: "+ v1.size());

		Set<String> v_i = new HashSet<String>(); //common tokens
		for (String i : v0) { //find common tokens
			if (v1.contains(i))
				v_i.add(i);
		}

		//System.out.println("After PreProcess"+ v0.toString() + "\t"+ v1.toString());
		Map<String, Double> featList = new HashMap<String, Double>();
		//If there is nothing in common among the two strings return empty feature List
		if (v_i.size() == 0)
			return featList;
		
		StringBuilder feature = new StringBuilder();
		//This was commented in the version received by me from Clark. 
		for (String i : v_i) { //present in both the strings
			featList.put(feature.append("True:").append(i).toString(), TRUE );
			feature.setLength(0);
			//featList.put("True:" + i, TRUE);
		}
		
		v0.removeAll(v_i);
		v1.removeAll(v_i);
		for (String i : v0) { //present in first string, not in the second
			featList.put(feature.append("False:").append(i).toString(), TRUE );
			feature.setLength(0);
			//featList.put("False:" + i, TRUE);
		}
		for (String i : v1) { //present in second string, not in the first
			featList.put(feature.append("False:").append(i).toString(), TRUE );
			feature.setLength(0);
			//featList.put("False:" + i, TRUE);
		}
		return featList;
	}
}
