package edu.isi.pfindr.learn.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


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
 * * SortMap.java 
 * 
 * Utility to sort the contents of a Map
 * 
 * @author sharma@isi.edu 
 * 
 */
public class SortMap{

	public static Map sortByComparator(Map unsortMap) {

		List list = new LinkedList(unsortMap.entrySet());
		//sort list based on comparator
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue())
						.compareTo(((Map.Entry) (o1)).getValue());
			}
		});

		//put sorted list into map again
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry)it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}	

	public static void main(String[] args) {

		System.out.println("Unsort Map......");
		Map<String,String> unsortMap = new HashMap<String,String>();
		unsortMap.put("1", "1");
		unsortMap.put("2", "A");
		unsortMap.put("3", "2");
		unsortMap.put("4", "B");
		unsortMap.put("5", "C");
		unsortMap.put("6", "c");
		unsortMap.put("7", "b");
		unsortMap.put("8", "a");

		Iterator iterator=unsortMap.entrySet().iterator();

		for (Map.Entry entry : unsortMap.entrySet()) {
			System.out.println("Key : " + entry.getKey() 
					+ " Value : " + entry.getValue());
		}

		System.out.println("Sorted Map......");
		Map<String,String> sortedMap =  sortByComparator(unsortMap);

		for (Map.Entry entry : sortedMap.entrySet()) {
			System.out.println("Key : " + entry.getKey() 
					+ " Value : " + entry.getValue());
		}
	}
}
