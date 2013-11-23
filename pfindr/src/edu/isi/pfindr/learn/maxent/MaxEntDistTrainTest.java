package edu.isi.pfindr.learn.maxent;


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
 * * MaxEntDistTrainTest.java 
 * 
 * Train and test MaxEnt model
 * 
 * @author sharma@isi.edu 
 * 
 */

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cc.mallet.classify.Classification;
import cc.mallet.classify.MaxEnt;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.pipe.Pipe;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Label;
import cc.mallet.types.LabelAlphabet;
import edu.isi.pfindr.learn.util.PairsFileIO;


public class MaxEntDistTrainTest {
	
	static private Logger logger = Logger.getLogger("AppLogging");
	
	/*Learn a model from the training instances, learn feature weights and then write to a binary object file */
	public void train(String directoryPath, String filename, String modelName) throws Exception{
		
		String trainingFilePath = directoryPath + filename;
		//Training phase
		Pipe pipe = new MaxEntInputPipe();
		pipe.setTargetProcessing(true);
		InstanceList trainingInstList = new InstanceList(pipe);
		List<Instance> instances = PairsFileIO.loadInputFile(trainingFilePath);
		trainingInstList.addThruPipe(instances.iterator());

		MaxEntTrainer meTrainer = new MaxEntTrainer();
		MaxEnt maxEnt = meTrainer.train(trainingInstList);
		//maxEnt.print();
		ObjectOutputStream oos = null;
		try{
			oos = new ObjectOutputStream(
					new FileOutputStream(directoryPath + modelName));
			oos.writeObject(maxEnt);
		}catch (IOException io){
			io.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(oos != null){
					oos.close();
					oos = null;
				}
			}catch(IOException ioe) {
				System.err.println("IO Exception when closing file : "+ ioe.getMessage());
				ioe.printStackTrace();
			}
		}
		System.out.println("Training Accuracy=" + maxEnt.getAccuracy(trainingInstList));
	}

	public void test(String directoryPath, String fileName, Object model) throws Exception{ //, String modelFileName) 
		String testFilePath = directoryPath + fileName;
		
		String resultFilePath =  ((String)directoryPath + 
				fileName).replaceAll(".txt", "_classification.txt");

		//Test phase
		Pipe pipe = ((MaxEnt)model).getInstancePipe();
		pipe.setTargetProcessing(false);
		
		InstanceList testInstList = new InstanceList(pipe);
		List<Instance> instances = PairsFileIO.loadInputFile(testFilePath);
		logger.info("Before adding instances through pipe, time(ms): "+ System.currentTimeMillis());
		for (Instance i : instances) {
			testInstList.addThruPipe(i);
		}
		logger.info("After adding instances through pipe time(ms):"+ System.currentTimeMillis());
		logger.info("Before classification");
		Label label = ((LabelAlphabet) pipe.getTargetAlphabet()).lookupLabel("1");

		BufferedWriter output = null;
		StringBuilder outputString = new StringBuilder();
		logger.info("#Test Accuracy=" + ((MaxEnt)model).getAccuracy(testInstList));
		try{
			output = new BufferedWriter(new FileWriter(resultFilePath));
			for (Instance inst : testInstList) {
				Classification clas = ((MaxEnt)model).classify(inst);
				
				outputString.append(inst.getSource()).append("\t"); // source = string1 \t string2 \t 0/1
				outputString.append(clas.getLabeling().getBestLabel()).append("\t")
					.append(String.format("%.4f", clas.getLabeling().value(label)));
				outputString.append("\n");

				output.append(outputString.toString());
				outputString.setLength(0);
				output.flush();
			}
		}catch (IOException ioe) {
			ioe.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
			try{
				if(output != null){
					output.close();
					output = null;
				}
			}catch(IOException ioe) {
				System.err.println("IO Exception when closing file : "+ ioe.getMessage());
				ioe.printStackTrace();
			}
		}
	}
	
	public List<MaxEntResult> test( List<Instance> instances, 
			 Object model) throws Exception{ 
		
		//Test phase
		Pipe pipe = ((MaxEnt)model).getInstancePipe();
		pipe.setTargetProcessing(false); 
		InstanceList testInstList = new InstanceList(pipe);
		
		logger.info("Before adding instances through pipe, time(ms): "+ System.currentTimeMillis());
		for (Instance i : instances) {
			testInstList.addThruPipe(i);
		}
		logger.info("After adding instances through pipe time(ms):"+ System.currentTimeMillis());
		logger.info("Before classification");
		Label label = ((LabelAlphabet) pipe.getTargetAlphabet()).lookupLabel("1");
		List<MaxEntResult> resultList = new ArrayList<MaxEntResult>();
		
		logger.info("#Test Accuracy=" + ((MaxEnt)model).getAccuracy(testInstList));
		try{
			for (Instance inst : testInstList) {
				Classification clas = ((MaxEnt)model).classify(inst);
				resultList.add(new MaxEntResult((String)inst.getSource(),clas.getLabeling().getBestLabel().toString(),
						String.format("%.4f", clas.getLabeling().value(label))));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return resultList;
	}
	
}

