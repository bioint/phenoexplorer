package edu.isi.pfindr.learn.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.io.FileUtils;

import cc.mallet.types.Instance;

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
 * Copyright 2012 University of Southern California
 *  
 * Utility file for the various operations to be performed on/with the "pairs" file
 *	
 * @author sharma@isi.edu
 *
 */

public class PairsFileIO {

	//Reads an input file, shuffles it, and writes it out
	@SuppressWarnings("unchecked")
	public static void shuffler(String pairsFilename) throws IOException{
		List<String> fileWithPairs = FileUtils.readLines(new File (pairsFilename));
		Collections.shuffle(fileWithPairs);
		String outputFilename = pairsFilename.split("\\.")[0] + "_s.txt"; //output filename derived from the pairs filename
		FileUtils.writeLines(new File (outputFilename), fileWithPairs );
	}

	/* Read the pairs file (where each line is <string1, string2, weight>) and constructs a Map of 
	 * distinct <elements, index>. This fixed index will be used to access a location in the 
	 * n x n Matrix (for weight or depth)
	 */
	public static LinkedMap readDistinctElementsIntoMap(String pairsFilename){
		File pairsFile =  new File (pairsFilename);
		LinkedMap phenotypeIndexMap = new LinkedMap();
		try{
			List<String> fileWithPairs = FileUtils.readLines(pairsFile); //Read one at a time to consume less memory
			int index = 0;
			for (String s : fileWithPairs) {
				//distinctElementsSet.add(s.split("\t")[0]);
				//distinctElementsSet.add(s.split("\t")[1]);
				if(!phenotypeIndexMap.containsKey(s.split("\t")[0])){
					phenotypeIndexMap.put(s.split("\t")[0], index );
					index++;
				}
			}
			for (String s : fileWithPairs) {
				if(!phenotypeIndexMap.containsKey(s.split("\t")[1])){
					phenotypeIndexMap.put(s.split("\t")[1], index );
					index++;
				}
			}
			System.out.println("Index "+ index);
		}catch (IOException e){
			System.out.println("Error while reading/writing file with pairs"+ e.getMessage());
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}
		return phenotypeIndexMap;
	}

	/* Read the pairs file (where each line is <string1, string2, weight>) and constructs a 
	 * list of distinct elements
	 */
	public static List<Object> readDistinctElementsIntoList(String pairsFilename){
		File pairsFile =  new File (pairsFilename);
		//Set<String> distinctElementsSet = new LinkedHashSet<String>();
		List<Object> distinctElementList = new ArrayList<Object>();
		try{
			List<String> fileWithPairs = FileUtils.readLines(pairsFile); //Read one at a time to consume less memory
			for (String s : fileWithPairs) {
				//distinctElementsSet.add(s.split("\t")[0]);
				//distinctElementsSet.add(s.split("\t")[1]);
				if(!distinctElementList.contains(s.split("\t")[0]))
					distinctElementList.add(s.split("\t")[0]);
			}
			for (String s : fileWithPairs) {
				if(!distinctElementList.contains(s.split("\t")[1]))
					distinctElementList.add(s.split("\t")[1]);
			}
		}catch (IOException e){
			System.out.println("Error while reading/writing file with pairs"+ e.getMessage());
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}
		return distinctElementList;
		//return new ArrayList<Object>(distinctElementsSet);
	}

	//Read two different input files and construct pairs from them, when no information of class is given
	public void generatePairsFromTwoDifferentFilesWithClass(String inputFilePath1,
			String inputFilePath2, String outputFilePath) {

		List<String> phenotypeList1 = new ArrayList<String>();
		List<String> phenotypeList2 = new ArrayList<String>();
		try{
			phenotypeList1 = FileUtils.readLines(new File (inputFilePath1));
			phenotypeList2 = FileUtils.readLines(new File (inputFilePath2));
		}catch (IOException ioe){
			ioe.printStackTrace();
		}
		String[] phenotype1, phenotype2;
		StringBuffer outputBuffer = new StringBuffer();

		//List<String> resultList = new ArrayList<String>();
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(outputFilePath));
			int count =0;

			for (int i = 0; i < phenotypeList1.size(); i++) {
				phenotype1 = phenotypeList1.get(i).split("\t");
				for (int j = 0; j < phenotypeList2.size(); j++) {
					count++;
					phenotype2 = phenotypeList2.get(j).split("\t");
					System.out.println("i "+ i + "j "+ j + "  " + phenotype1[0] + " " + phenotype2[0]);
					if (phenotype1[1].equals(phenotype2[1])) { //if the classes are the same
						//if (phenotype1[1].equals(phenotype2[0])) { //if the classes are the same

						//resultList.add(String.format("%s\t%s\t%d", phenotype1[3], phenotype2[3], 1));
						//resultList.add(String.format("%s\t%s\t%d", phenotype1[0], phenotype2[1], 1));
						outputBuffer.append(String.format("%s\t%s\t%d", phenotype1[0], phenotype2[0], 1)).append("\n");
						//bw.write(String.format("%s\t%s\t%d", phenotype1[0], phenotype2[0], 1) + "\n");
					} else {
						//resultList.add(String.format("%s\t%s\t%d", phenotype1[3], phenotype2[3], 0));
						//resultList.add(String.format("%s\t%s\t%d", phenotype1[0], phenotype2[1], 0));
						//bw.write(String.format("%s\t%s\t%d", phenotype1[0], phenotype2[0], 0) + "\n");
						outputBuffer.append(String.format("%s\t%s\t%d", phenotype1[0], phenotype2[0], 0)).append("\n");
					}
					bw.append(outputBuffer.toString());
					outputBuffer.setLength(0);
				}
			}
			bw.flush();
			System.out.println("The count is: "+ count);
		}catch (IOException io) {
			try {
				if(bw != null)
					bw.close();
				io.printStackTrace();
			} catch (IOException e) {
				System.out.println("Problem occured while closing output stream  "+ bw);
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(bw != null)
					bw.close();
			} catch (IOException e) {
				System.out.println("Problem occured while closing output stream  "+ bw);
				e.printStackTrace();
			}
		}
	}
	
	public static void generatePairsFromStringAndFileContentWithNoClass(String userInput, String inputFilePath, String outputFilePath) {
		
		List<String> phenotypeList = new ArrayList<String>();
		
		try{
			phenotypeList = FileUtils.readLines(new File (inputFilePath));
		}catch (IOException ioe){
			ioe.printStackTrace();
		}
		String[] phenotype2;
		StringBuffer outputBuffer = new StringBuffer();

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(outputFilePath));
				for (int j = 0; j < phenotypeList.size(); j++) {
					phenotype2 = phenotypeList.get(j).split("\t");
					
					outputBuffer.append(String.format("%s\t%s\t%d\n", userInput, phenotype2[3], 0));
					bw.append(outputBuffer.toString());
					outputBuffer.setLength(0);
				}
		}catch (IOException io) {
			try {
				if(bw != null){
					bw.close();
					bw = null;
				}
				io.printStackTrace();
			} catch (IOException e) {
				System.out.println("Problem occured while closing output stream  "+ bw);
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(bw != null){
					bw.close();
					bw = null;
				}
			} catch (IOException e) {
				System.out.println("Problem occured while closing output stream  "+ bw);
				e.printStackTrace();
			}
		}
	}

	public static void generatePairsFromTwoDifferentFilesWithNoClass(String inputFilePath1, String inputFilePath2, String outputFilePath) {

		List<String> phenotypeList1 = new ArrayList<String>();
		List<String> phenotypeList2 = new ArrayList<String>();
		try{
			
			//Reader paramReader = new InputStreamReader(getClass().getResourceAsStream("/com/test/services/LoadRunner/FireCollection/fire.txt"));  
	        //System.out.println(PairsFileIO.class.getClassLoader().getResource(inputFilePath1).getPath());
	        //System.out.println(PairsFileIO.class.getClassLoader().getResource(inputFilePath2).getPath());
	        
			//phenotypeList1 = FileUtils.readLines(new File (PairsFileIO.class.getClassLoader().getResource(inputFilePath1).getPath()));
			//phenotypeList2 = FileUtils.readLines(new File (PairsFileIO.class.getClassLoader().getResource(inputFilePath2).getPath()));
		
			phenotypeList1 = FileUtils.readLines(new File (inputFilePath1));
			phenotypeList2 = FileUtils.readLines(new File (inputFilePath2));

		}catch (IOException ioe){
			ioe.printStackTrace();
		}
		String[] phenotype1, phenotype2;
		StringBuffer outputBuffer = new StringBuffer();

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(outputFilePath));

			for (int i = 0; i < phenotypeList1.size(); i++) {
				phenotype1 = phenotypeList1.get(i).split("\t");
				for (int j = 0; j < phenotypeList2.size(); j++) {
					phenotype2 = phenotypeList2.get(j).split("\t");
					
					//System.out.println("The value is:"+  phenotype1[3] + " : " + phenotype2[3]);
					
					outputBuffer.append(String.format("%s\t%s\t%d\n", phenotype1[3], phenotype2[3], 0));
					bw.append(outputBuffer.toString());
					outputBuffer.setLength(0);
				}
			}
		}catch (IOException io) {
			try {
				if(bw != null){
					bw.close();
					bw = null;
				}
				io.printStackTrace();
			} catch (IOException e) {
				System.out.println("Problem occured while closing output stream  "+ bw);
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(bw != null){
					bw.close();
					bw = null;
				}
			} catch (IOException e) {
				System.out.println("Problem occured while closing output stream  "+ bw);
				e.printStackTrace();
			}
		}
	}

	public static void generatePairsFromFileWithNoClassSpecified(String inputFilePath, String pairsFileName) {

		//Buffered output file
		BufferedWriter output = null;
		try {
			//Read input file into a list
			List<String> inputList = FileUtils.readLines(new File (inputFilePath));
			int fileSize = inputList.size();
			System.out.println(fileSize);
			output = new BufferedWriter(new FileWriter(pairsFileName));

			for (int i = 0; i < fileSize; i++) {
				for (int j = i + 1; j < fileSize;  j++) {
					output.write(inputList.get(i).split("\t")[2] + "\t" + inputList.get(j).split("\t")[2] + "\t" + 0  + 
							"\n");
				}
			}
		} catch (IOException io) {
			try {
				if(output != null)
					output.close();
				io.printStackTrace();
			} catch (IOException e) {
				System.out.println("Problem occured while closing input stream while reading file "+ output);
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String> generatePairsFromFileWithClass(String inputFilePath) {
		List<String[]> rawData = new ArrayList<String[]>();
		List<String> resu = new ArrayList<String>();
		BufferedReader br;
		String thisLine;
		String[] a, b;
		try {
			br = new BufferedReader(new FileReader(inputFilePath));
			while ((thisLine = br.readLine()) != null) {
				thisLine = thisLine.trim();
				if (thisLine.equals(""))
					continue;
				a = thisLine.split("\\t");
				rawData.add(new String[]{a[1], a[3]}); //1=class, 3=phenotype string
				//rawData.add(new String[]{a[1], a[0]}); //1=class, 0=phenotype string
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (int i = 0; i < rawData.size(); i++) {
			a = rawData.get(i);
			for (int j = i + 1; j < rawData.size(); j++) {
				b = rawData.get(j);
				if(a[0].equals("null") || b[0].equals("null")){
					resu.add(String.format("%s\t%s\t%d", a[1], b[1], 0));
				}else{
					if (a[0].equals(b[0])) {
						resu.add(String.format("%s\t%s\t%d", a[1], b[1], 1));
					} else {
						resu.add(String.format("%s\t%s\t%d", a[1], b[1], 0));
					}
				}
			}
		}
		return resu;
	}

	public static List<Instance> loadInputFile(String inputFilePath){//, boolean isPositive) {
		System.out.print("loading " + inputFilePath + " ...");
		List<Instance> instList = new ArrayList<Instance>();
		int i = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputFilePath));
			String thisLine;
			while ((thisLine = br.readLine()) != null) {
				thisLine = thisLine.trim();
				if (thisLine.equals(""))
					continue;

				//if (isPositive && thisLine.split("\\t")[2].equals("0"))
					//continue;
				
				/*instList.add(new Instance(thisLine.split("\\t")[0] + "\t" + thisLine.split("\\t")[1], 
						thisLine.split("\\t")[2], i, thisLine));*/
				instList.add(new Instance(thisLine.split("\\t")[1] + "\t" + thisLine.split("\\t")[3], 	//data/target/name/source
						thisLine.split("\\t")[4], i, thisLine.split("\\t")[0] + "\t" + thisLine.split("\\t")[2] + "\t" + thisLine.split("\\t")[4]));
				i++;
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("complete.");
		return instList;
	}
	
	/*
	 * Reads distinct elements from pairs file into a list, and adds the (Gold) class from 
	 * the original test file
	 * 
	 */
	public void readDistinctElementsFromPairsAddClass(String pairsFilepath){
		//readDistinctElementsIntoList
		List<Object> distinctElements = readDistinctElementsIntoList(pairsFilepath);
		System.out.println("Size of distinctElements"+ distinctElements.size());
		for(int i=0; i<distinctElements.size(); i++){
			System.out.println("distinctElements "+ i + " " + distinctElements.get(i));
		}

		//get class for those distinct elements from original cohort file
		String originalFile = "data/cohort1/bio_nlp/cohort1_s.txt";
		BufferedReader br = null;
		String thisLine;
		String[] lineArray;
		LinkedMap originalMap = new LinkedMap();
		BufferedWriter distinctPriorityPairsWriter = null;

		try {
			br = new BufferedReader(new FileReader(originalFile));
			while ((thisLine = br.readLine()) != null) {
				thisLine = thisLine.trim();
				if (thisLine.equals(""))
					continue;

				lineArray = thisLine.split("\t");
				originalMap.put(lineArray[3], lineArray[1]);
			}

			//write distinct elements with class to an output file
			StringBuffer outfileBuffer = new StringBuffer();
			for(int i = 0; i < distinctElements.size(); i++)
				outfileBuffer.append(distinctElements.get(i)).append("\t")
				.append(originalMap.get(distinctElements.get(i)) + "\n");

			distinctPriorityPairsWriter = new BufferedWriter(
					new FileWriter( pairsFilepath.split("\\.")[0] + "_distinct_with_class.txt"));

			distinctPriorityPairsWriter.append(outfileBuffer.toString());
			outfileBuffer.setLength(0);
			distinctPriorityPairsWriter.flush();

		} catch (IOException io) {
			try {
				if(br != null)
					br.close();
				io.printStackTrace();
			} catch (IOException e) {
				System.out.println("Problem occured while closing output stream "+ br);
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public  LinkedMap readOriginalFileWithGoldClass(String originalTestFile){
		//String originalTestFile = "data/cohort1/bio_nlp/cohort1_s_test.txt";
		//Read the test file
		String thisLine;
		String[] lineArray;
		BufferedReader br = null;
		LinkedMap originalTestClassMap = new LinkedMap();
		try {
			br = new BufferedReader(new FileReader(originalTestFile));
			while ((thisLine = br.readLine()) != null) {
				thisLine = thisLine.trim();
				if (thisLine.equals(""))
					continue;

				lineArray = thisLine.split("\t");
				originalTestClassMap.put(lineArray[3], lineArray[1]); //phenotype, class
				//System.out.println("Adding "+ lineArray[1] + " : " + lineArray[3]);
			}
		} catch (IOException io) {
			try {
				if(br != null)
					br.close();
				io.printStackTrace();
			} catch (IOException e) {
				System.out.println("Problem occured while closing output stream while writing file "+ br);
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return originalTestClassMap;
	}

	public void filterPairsThatExist(String inputFilePath1, String inputFilePath2){ 
		//eg. testdata(the data to check), traindata(original data)

		//Read the files
		List<String> phenotypeList1 = new ArrayList<String>();	
		List<String> phenotypeList2 = new ArrayList<String>(); //sure pairs
		LinkedMap surePairsAdjacencyMap = new LinkedMap();
		try{
			phenotypeList1 = FileUtils.readLines(new File (inputFilePath1));
			phenotypeList2 = FileUtils.readLines(new File (inputFilePath2));

			String[] lineArray;
			List<String> resultList = new ArrayList<String>();
			List<String> surePairsMapValue =  null;

			System.out.println(phenotypeList2.size());
			//construct a map of phenotype and its neighbors for sure-pairs
			for (int i = 0; i < phenotypeList2.size(); i++) {
				lineArray = phenotypeList2.get(i).split("\t");
				surePairsMapValue = new ArrayList<String>();

				//if the first value is existing in the map, get the second value
				if(surePairsAdjacencyMap.containsKey(lineArray[0])){
					surePairsMapValue = (List<String>)surePairsAdjacencyMap.get(lineArray[0]);
				}
				//System.out.println("SurePairsMapValueSize " + surePairsMapValue.size());
				//if the value does not already contain the second, add the string and add it back to the map
				if(!surePairsMapValue.contains(lineArray[1]))
					surePairsMapValue.add(lineArray[1]);
				surePairsAdjacencyMap.put(lineArray[0], surePairsMapValue);

				//In the same manner, update the adjacency of the second string
				surePairsMapValue = new ArrayList<String>();
				if(surePairsAdjacencyMap.containsKey(lineArray[1])){
					surePairsMapValue = (List<String>)surePairsAdjacencyMap.get(lineArray[1]);
				}
				if(!surePairsMapValue.contains(lineArray[0]))
					surePairsMapValue.add(lineArray[0]);
				surePairsAdjacencyMap.put(lineArray[1], surePairsMapValue);
			}

			List valueList = null;
			for (int i=0; i<surePairsAdjacencyMap.size(); i++) {
				System.out.println("Key : " + surePairsAdjacencyMap.get(i)
						+ " Value : " + ((List)surePairsAdjacencyMap.get(surePairsAdjacencyMap.get(i))).size());
				/*valueList = (List)surePairsAdjacencyMap.get(surePairsAdjacencyMap.get(i));
	        	for(int j =0; j<valueList.size(); j++)
	        		System.out.println("Value :" + valueList.get(j) );
	        	//break;*/
			}

			//Now parse the new pairs file, and check if the pairs already exists in the sure pairs map
			boolean existsSurePairs = false;
			System.out.println(phenotypeList1.size());
			surePairsMapValue =  new ArrayList<String>();
			for (int j = 0; j < phenotypeList1.size(); j++) {

				lineArray = phenotypeList1.get(j).split("\t");
				if(surePairsAdjacencyMap.containsKey(lineArray[0])){	
					surePairsMapValue = (List) surePairsAdjacencyMap.get(lineArray[0]);
					if(surePairsMapValue.contains(lineArray[1])){
						existsSurePairs = true;
					}
				}else if(surePairsAdjacencyMap.containsKey(lineArray[1])){	
					surePairsMapValue = (List) surePairsAdjacencyMap.get(lineArray[1]);
					if(surePairsMapValue.contains(lineArray[0])){
						existsSurePairs = true;
					}
				}

				if(!existsSurePairs) //if it does not exist in surepairs, then write to output file
					resultList.add(String.format("%s\t%s\t%s", lineArray[0], lineArray[1], lineArray[2]));
				existsSurePairs = false;
			}
			String resultFilePath =  inputFilePath1.split("\\.")[0] + "_filtered.txt";
			FileUtils.writeLines(new File (resultFilePath), resultList);
		}catch (IOException ioe){
			ioe.printStackTrace();
		}
	}

	public void extractOriginalMappingFromClassificationFile(String inputFilePath1, String outputFileName){

		String thisLine;
		String[] lineArray;
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			//read the phenotype of the smaller file into an arraylist
			br = new BufferedReader(new FileReader(inputFilePath1));
			bw = new BufferedWriter(new FileWriter(outputFileName));

			while ((thisLine = br.readLine()) != null) {
				thisLine = thisLine.trim();
				if (thisLine.equals(""))
					continue;

				lineArray = thisLine.split("\t");
				bw.write(lineArray[0]+"\t"+lineArray[1]+"\t"+lineArray[2]+"\n");
			}
		} catch (IOException io) {
			io.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try{
				if(bw != null)
					bw.close();
				if(br != null)
					br.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	private void getGoldClassForPairs(String inputFilePath ){

		LinkedMap originalTestPairsClassMap = new LinkedMap();
		String originalTestPairsFile = "data/cohort1/bio_nlp/cohort1_s_test_pairs.txt";
		BufferedReader br = null;
		String[] lineArray;
		String thisLine;
		try {
			br = new BufferedReader(new FileReader(originalTestPairsFile));
			while ((thisLine = br.readLine()) != null) {
				thisLine = thisLine.trim();
				if (thisLine.equals(""))
					continue;

				lineArray = thisLine.split("\t");
				originalTestPairsClassMap.put(lineArray[0]+"\t"+lineArray[1], lineArray[2] );
			}
		} catch (IOException io) {
			try {
				if(br != null)
					br.close();
				io.printStackTrace();
			} catch (IOException e) {
				System.out.println("Problem occured while closing output stream "+ br);
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<String> phenotypeList = null;
		StringBuffer resultFileBuffer = new StringBuffer();

		//Read the pairs file, and write the pairs with class from actual test file
		try{
			BufferedWriter resultPairsWriter = new BufferedWriter(
					new FileWriter(inputFilePath.split("\\.")[0] + "_with_gold_class.txt"));
			phenotypeList = FileUtils.readLines(new File (inputFilePath));

			for (int i = 0; i < phenotypeList.size(); i++) {

				lineArray = phenotypeList.get(i).split("\t");
				resultFileBuffer.append(lineArray[0]).append("\t").append(lineArray[1]).append("\t")
				.append(originalTestPairsClassMap.get(lineArray[0]+"\t"+lineArray[1])).append("\n");
			}

			resultPairsWriter.append(resultFileBuffer.toString());
			resultFileBuffer.setLength(0);
			resultPairsWriter.flush();

		}catch (IOException e){
			System.out.println("Error while reading/writing file with pairs"+ e.getMessage());
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws Exception {
		//PairsFileIO pi = new PairsFileIO();
	}
}

