package edu.isi.pfindr.learn.maxent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
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
 * * MaxEntResultUtil.java 
 * 
 * Util function for writing MaxEnt classification result. (Not currently being used )
 * 
 * @author sharma@isi.edu 
 * 
 */
public class MaxEntResultUtil {

	public static void writeResultToFile(String filePath, String fileName, List<MaxEntResult> MaxEntResultList){
		BufferedWriter outputWriter = null;
		try{
			outputWriter = new BufferedWriter(new FileWriter(filePath+fileName));
			StringBuilder outBuilder;
			for(MaxEntResult testResult: MaxEntResultList){
				outBuilder = new StringBuilder();
				//write output str1, str2, modelscore, tfidf, combinedscore
				outBuilder.append(testResult.getSource().split("\\t")[0]).append("\t")
						.append(testResult.getSource().split("\\t")[1]).append("\t")
					.append(testResult.getFirstScore()).append("\t")
					.append(testResult.getSecondScore()).append("\t")
					.append(testResult.getCombinedScore())
					.append("\n");
				outputWriter.append(outBuilder.toString());
				outBuilder.setLength(0);
			}
			outputWriter.flush();
		}catch (IOException io) {
			io.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(outputWriter != null)
					outputWriter.close();
			} catch (IOException e) {
				System.out.println("Problem occured while closing output stream  "+ outputWriter);
				e.printStackTrace();
			}
		}
	}
}
