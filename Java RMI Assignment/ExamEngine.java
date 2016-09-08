package ct414;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExamEngine implements ExamServer {

	// private static List<Student> students = new ArrayList<Student>(10);
	// private static List<QuestionImpl> q = new ArrayList<QuestionImpl>(10);
	List<Student> students;
	List<Assessment> assessments;
	Map<Integer, Map<String, Assessment>> submitted;

	// Constructor is required
	public ExamEngine() throws FileNotFoundException {

		// Students initializer
		students = new ArrayList<Student>(3);

		String codesSean[] = { "CT414", "CT420" };
		Student s1 = new Student(123, "pass", codesSean);
		students.add(s1);

		String codesAntoin[] = { "CT424" };
		Student s2 = new Student(125, "pass", codesAntoin);
		students.add(s2);

		// Assessment initializer
		assessments = new ArrayList<Assessment>(3);
		TestReader reader = new TestReader();
		Assessment one = reader.getAssessment("CT414");
		Assessment two = reader.getAssessment("CT424");
		Assessment three = reader.getAssessment("CT420");
		assessments.add(one);
		assessments.add(two);
		assessments.add(three);

		submitted = new HashMap<Integer, Map<String, Assessment>>();
	}

	// Implement the methods defined in the ExamServer interface...
	// Return an access token that allows access to the server for some time
	// period
	public TokenInterface login(int studentid, String password)
			throws UnauthorizedAccess, RemoteException {
		token token = new token();
		TokenInterface t = token;
		boolean valid =false;
		
		for (Student s : students) {
			if (s.getID() == studentid && s.getPassword().equals(password)) {
				s.setToken(token);
				valid=true;
				return t;

			}
			
		}
		if(valid==false){
			throw new UnauthorizedAccess("Login failed");
		}
		// TBD: You need to implement this method!
		// For the moment method just returns an empty or null value to allow it
		// to compile
		TokenInterface failed = new token("Failed Login", new GregorianCalendar());
		return failed;
	}

	
	public List<String> getAvailableSummary(TokenInterface token, int studentid)
			throws UnauthorizedAccess, NoMatchingAssessment, RemoteException {

		Calendar d = new GregorianCalendar();
		List<String> summarys = new ArrayList<String>();
		//summarys=null;
		for (Student student : students) {
			if (student.getID() == studentid) {
				if (d.getTime().before(token.getTimeout().getTime()) == true && token.getToken().equals(student.getToken()) ){
				String[] codes = student.getCourseCodes();
				for (String code : codes) {
					for (Assessment a : assessments) {
						if (a.getCourseCode().equalsIgnoreCase(code)) {
							if (d.getTime().before(a.getClosingDate().getTime()) == true){
								String temp = a.getInformation();
								summarys.add(temp);
							}
						}
					}
				}
			}
		}
		}
		
		if(d.getTime().after(token.getTimeout().getTime()) == true){
			throw new UnauthorizedAccess("Timed out");
		}
		
		if(summarys.size()==0){
			throw new NoMatchingAssessment("There is no available assessments for this student");
		}
		
		
		return summarys;
	}

	// Return an Assessment object associated with a particular course code
    public Assessment getAssessment(TokenInterface token, int studentid, String courseCode) throws
                UnauthorizedAccess, NoMatchingAssessment, RemoteException {

    	Assessment test = null;
    	Calendar d = new GregorianCalendar();
    	
    			Map<String, Assessment> ccAssessmentMap = new HashMap<String, Assessment>();
    	
    			for (Student student : students){
    				if (student.getID() == studentid){
    					if (d.getTime().before(token.getTimeout().getTime()) == true && token.getToken().equals(student.getToken()) ){
    					if (submitted.get(studentid) != null){
    						ccAssessmentMap = submitted.get(studentid);
    						if(ccAssessmentMap.get(courseCode) != null && new GregorianCalendar().getTime().before(ccAssessmentMap.get(courseCode).getClosingDate().getTime()) == true){
    							test = ccAssessmentMap.get(courseCode);
    						}
    						else{
    							for (Assessment a : assessments){
    								if(a.getCourseCode().equalsIgnoreCase(courseCode)){
    									if (d.getTime().before(a.getClosingDate().getTime()) == true){
    										test =  a;
    										test.setAssociatedID(student.getID());
    									}
    								}
    							}
    						}
    					}
    					else {
    						for (Assessment a : assessments){
    							if(a.getCourseCode().equalsIgnoreCase(courseCode)){
    								if (d.getTime().before(a.getClosingDate().getTime()) == true){
    									test =  a;
    									test.setAssociatedID(student.getID());
    								}
    							}
    						}
    					}
    				}
    			}
    		}
    		if(d.getTime().after(token.getTimeout().getTime()) == true ){
    			throw new UnauthorizedAccess("Timed out");
    		}
    		
    		if(test==null){
    			throw new NoMatchingAssessment("Invalid course code entered");
    		}
    		
    	
    	return test;	
    }

    // Submit a completed assessment
    public void submitAssessment(TokenInterface token, int studentid, Assessment completed) throws 
                UnauthorizedAccess, NoMatchingAssessment, RemoteException {
    	
    	Calendar d = new GregorianCalendar();
    	if (d.getTime().before(token.getTimeout().getTime()) == true){
    		if (d.getTime().before(completed.getClosingDate().getTime()) == true){
    			for (Student student :students){
    				if (student.getID() == studentid){
    					if (token.getToken().equals(student.getToken()) ){
    					String courseCode = completed.getCourseCode();
    					Map<String, Assessment> ccAssessmentMap = new HashMap<String, Assessment>();
    					ccAssessmentMap.put(courseCode, completed);
    					submitted.put(studentid, ccAssessmentMap); 
    					}
    				}	
    			}
    		}
    	}
    	
    	if(d.getTime().after(token.getTimeout().getTime()) == true){
			throw new UnauthorizedAccess("Timed out");
		}
		
		if(completed==null){
			throw new NoMatchingAssessment("Error null assessment submitted ");
		}
    }

	public static void main(String[] args) throws FileNotFoundException {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {
			ExamServer engine = new ExamEngine();
			ExamServer stub = (ExamServer) UnicastRemoteObject.exportObject(
					engine, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("ExamServer", stub);
			System.out.println("ExamEngine bound");
		} catch (Exception e) {
			System.err.println("ExamEngine exception:");
			e.printStackTrace();
		}

		// TestReader reader = new TestReader();
		// Assessment test1 = reader.getAssessment("Assessment1.txt");
		// System.out.println(test1.getInformation());
	}

}
