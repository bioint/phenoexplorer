package edu.isi.pfindr.learn.util;

import java.io.File;

import org.apache.commons.io.FileUtils;

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
 * * FileUtil.java 
 * 
 * Utility function to delete file. (Usually temporary files need to be deleted for clean-up)
 * 
 * @author sharma@isi.edu 
 * 
 */
public class FileUtil {

	public static boolean deleteFile(String directoryName, String fileName){
		
		String fileToDelete = directoryName + fileName;
		//System.out.println("Inside Deleting File:"+ fileToDelete);
		File file = new File(fileToDelete);
		return	FileUtils.deleteQuietly(file);
	}
}
