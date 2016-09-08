package ct414;
import java.rmi.RemoteException;
import java.util.Date;


public class Student{

	// TO DO
	private int student_ID;
	private String password;
	private String[] courseCodes;
	private token token;
	
	public Student (int id, String p, String[] codes) {
		student_ID = id;
		password = p;
		courseCodes = codes;
	}
	
	public Student() {
		// TODO Auto-generated constructor stub
	}

	public int getID(){
		return student_ID;
	}
	
	public String getPassword(){
		return password;
	}
	
	public void setToken(token t){
		token =t;
	}
	
	public String getToken(){
		return token.getToken();
	}
	
	public Date getTimeout(){
		return token.getTimeout().getTime();
	}
	
	public void setCourseCodes(String[] codes){
		courseCodes = codes;
	}
	
	public String[] getCourseCodes(){
		return courseCodes;
	}

}