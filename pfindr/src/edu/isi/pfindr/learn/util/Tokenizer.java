package edu.isi.pfindr.learn.util;

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
 * * Tokenizer.java 
 * 
 * Creates tokens from strings that are read from a file and/or a reader
 * 
 * @param  FileName whose contents is used to create tokens
 * @param  Reader with content that is used to create tokens
 * 
 * Overrides the functionality from Lucene.
 * 
 * @author sharma@isi.edu 
 * 
 */

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

public class Tokenizer extends StandardAnalyzer {

	public Tokenizer(Version matchVersion) {
		super(matchVersion);
	}

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		BufferedReader bufferedReader = new BufferedReader(reader);
		StringBuilder stringBuilder = new StringBuilder();
		String thisLine;

		try {
			while ((thisLine = bufferedReader.readLine()) != null) {
				stringBuilder.append(thisLine);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		thisLine = stringBuilder.toString().toLowerCase();
		//System.out.println("line after function return "+ newLine.toString());
		reader = new StringReader(thisLine.toString().replaceAll("\\s+", " "));
		return super.tokenStream(fieldName, reader);
	}

}

