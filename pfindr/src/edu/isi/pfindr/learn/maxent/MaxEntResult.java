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
 * * MaxEntResult.java 
 * 
 * Representation of the classification result. (Not currently being used )
 * 
 * @author sharma@isi.edu 
 * 
 */
public class MaxEntResult {

	String source;
	String firstScore; 
	String secondScore; 
	String combinedScore;
	
	public MaxEntResult(String source, String firstScore, String secondScore){
		this.source = source;
		this.firstScore = firstScore;
		this.secondScore = secondScore;
	}
	
	public MaxEntResult(String source, String firstScore, String secondScore, String combinedScore){
		this.source = source;
		this.firstScore = firstScore;
		this.secondScore = secondScore;
		this.combinedScore = combinedScore;
	}
	
	public String getSource() {
		return source;
	}
	public String getFirstScore() {
		return firstScore;
	}
	public String getSecondScore() {
		return secondScore;
	}
	public String getCombinedScore() {
		return combinedScore;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public void setFirstScore(String firstScore) {
		this.firstScore = firstScore;
	}
	public void setSecondScore(String secondScore) {
		this.secondScore = secondScore;
	}
	public void setCombinedScore(String combinedScore) {
		this.combinedScore = combinedScore;
	}
}
