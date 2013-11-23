package edu.isi.pfindr.learn.util;

import java.util.Comparator;


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
 * * SortList.java 
 * 
 * Utility to sort elements of a list on the length of the String
 * 
 * @author sharma@isi.edu 
 * 
 */
public class SortList implements Comparator<String>{

	//longest string first
	public int compare(String o1, String o2) {  
		if (o1.length() < o2.length()) {
			return 1;
		} else if (o1.length() > o2.length()) {
			return -1;
		} else { 
			return o1.compareTo(o2);
		}
	}
}
	
/*public class SortList{
	public static void main(String[] args) {
		System.out.println("Unsorted List......");
		List<String> unsorList = new ArrayList<String>();
		unsorList.add("body");
		unsorList.add("body mass index");
		unsorList.add("body mass");
		unsorList.add("mass index");
		unsorList.add("mass");
		unsorList.add("index");

		for(int i =0; i < unsorList.size(); i++)
			System.out.println("Value : " + unsorList.get(i));
		
		Collections.sort(unsorList, new SortListComparator() );
		
		System.out.println("Sorted List......");
		for(int i =0; i < unsorList.size(); i++)
			System.out.println("Value : " + unsorList.get(i));	
	}
}*/
