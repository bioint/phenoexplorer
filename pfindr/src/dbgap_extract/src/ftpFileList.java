import java.util.Date;


public class ftpFileList {
	private String fileName;
	private Date lastedUpdateTime;
	private String localPath;
	private int nVNum;
	private int nPNum;
	
	public ftpFileList(String fileName, Date laostedUpdateTime, String localPath){
		this.fileName = fileName;
		this.lastedUpdateTime = laostedUpdateTime;
		this.localPath = localPath;
		this.nVNum = 0;
		this.nPNum = 0;
	}
	
	public void setLocalPath(String localPath){
		this.localPath = localPath;
	}
	
	public String getLocalPath(){
		return this.localPath;
	}
	
	public void setName(String fileName){
		this.fileName = fileName;
	}
	
	public String getName(){
		return this.fileName;
	}
	
	public void setLastedUpdateTime(Date lastedUpdateTime){
		this.lastedUpdateTime = lastedUpdateTime;
	}
	public Date getLastedUpdateTime(){
		return this.lastedUpdateTime;
	}
	
	public int getVNum(){
		return this.nVNum;
	}
	
	public int getPNum(){
		return this.nPNum;
	}
	
	public void setVNum(int nVNum){
		this.nVNum = nVNum;
	}
	
	public void setPNum(int nPNum){
		this.nPNum = nPNum;
	}
	
	public void setVersionNum(int nVNum, int nPNum){
		this.nVNum = nVNum;
		this.nPNum = nPNum;
	}
}
