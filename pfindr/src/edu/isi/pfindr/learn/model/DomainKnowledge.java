package edu.isi.pfindr.learn.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import edu.isi.pfindr.listeners.ServletContextInfo;

public class DomainKnowledge {
	
	public static Map<String, String> loadDomainKnowledge(Properties properties){
		//System.out.println("Loading Domain Knowledge  ..");

		BufferedReader br;
		String thisLine;
		Map<String, String> domainMap = new HashMap<String, String>();
		try {
			br = new BufferedReader(new FileReader(getDomainKnowledgePath(properties)));

			while ((thisLine = br.readLine()) != null) {
				thisLine = thisLine.trim();
				if (thisLine.equals(""))
					continue;
				String[] fields = thisLine.split("\\t");
				if (fields.length == 2){
					domainMap.put(fields[0].trim().toLowerCase(), fields[1].trim());
					//System.out.println("adding to dict: "+fields[0].trim().toLowerCase() );
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return domainMap;
	}
	
	private static String getDomainKnowledgePath(Properties properties){
		return ServletContextInfo.getContextPath() + ((String)properties.get("domain.knowledge.file.path")).trim();
	}
	
	public static String expandVariableWithDomainKnowledge(Map<String,String> domainMap, String variableData){
		StringBuilder domainKnowledgeExpansion = new StringBuilder();
		domainKnowledgeExpansion.append("");
		
		//System.out.println("Size of domainMap: "+ domainMap.size());
		
		String[] variableTokens = variableData.trim().split("\\s+");
		for(String token : variableTokens){
			//System.out.println("Token is: "+ token);
			if(domainMap.containsKey(token.trim().toLowerCase())){
				domainKnowledgeExpansion.append(" ").append(domainMap.get(token.trim().toLowerCase()));
				//System.out.println("Found it in the map:"+ domainKnowledgeExpansion.toString());
			}
		}
		if(domainMap.containsKey(variableData.toLowerCase().trim()))
			domainKnowledgeExpansion.append(" ").append(domainMap.get(variableData.toLowerCase().trim()));
		
		//System.out.println("Inside expandVariableWithDomainKnowledge returning"+ variableData + " " + 
			//	domainKnowledgeExpansion.toString().trim());
		return variableData + " " + domainKnowledgeExpansion.toString().trim();
	}
	
}
