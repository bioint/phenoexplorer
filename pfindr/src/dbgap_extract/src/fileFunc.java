import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class fileFunc {
	//private String filePathName;
	//private String fileName;
	private String fileContent;
	/*
	public fileFunc(String filePathName, String fileName) {
		// TODO Auto-generated constructor stub
		this.fileName = fileName;
		this.filePathName = filePathName;
	}
	
	public void setFilePathName(String filePathName){
		this.filePathName = filePathName;
	}
	
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
	
	public void setVarDesc(List<String> lVarDesc){
		int nLen = lVarDesc.size();
		for(int nIndex = 0;  nIndex < nLen; nIndex ++){
			String varDesc = lVarDesc.get(nIndex);
			this.lVarDesc.add(varDesc);
		}
	}
	
	public void initVarDesc(){
		this.lVarDesc.clear();
	}
	*/
	public void setFileContent(String fileContent){
		this.fileContent = fileContent;
	}
	
	public void writeFile(String filePathName, String fileName){
		File file = new File(filePathName + '/' + fileName);
		File dir = new File(filePathName);
		if(!dir.exists()){
			//if no such file, create one
			dir.mkdir();
		}
		
		if(!fileContent.equals("")){			
			//file exits and non empty
			try {
				FileWriter fw = new FileWriter(file);
				fw.write(fileContent);
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public List<List<String>> readVarDescFile(String filePathName, String fileName){
		List<List<String>> llRst = new ArrayList<List<String>>();
		
		File file = new File(filePathName + "/" + fileName);
		if(file.exists()){
			try {
				FileInputStream fileIS = new FileInputStream(file);
				InputStreamReader isReader = new InputStreamReader(fileIS, "UTF-8");
				BufferedReader bReader = new BufferedReader(isReader);
				String content = bReader.readLine();
				while(content != null){
					List<String> l1Var = new ArrayList<String>();
					String[] descExpansion = content.split("\t");
					l1Var.add(descExpansion[0]);
					if(descExpansion.length > 1){
						l1Var.add(descExpansion[1]);
					}
					llRst.add(l1Var);
					content = bReader.readLine();
				}
				
				fileIS.close();
				isReader.close();
				bReader.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				System.err.println(e.getMessage());
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				System.err.println(e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println(e.getMessage());
				e.printStackTrace();
			}			
		}
		return llRst;
	}
}
