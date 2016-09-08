
package ct414;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.security.SecureRandom;
import java.math.BigInteger;

public class token implements TokenInterface{

	// TO DO
	private String token;
	private Calendar timeout;
	private SecureRandom random = new SecureRandom();
	
	public token (){
		token = nextSessionId();
		timeout= timeout();
	}

	public token (String t, Calendar d){
		token = t;
		timeout= d;
	}
	
	
	public String getToken(){
		return token;
	}
	
	public Calendar getTimeout(){
		return timeout;
	}

	public String nextSessionId() {
		return new BigInteger(130, random).toString(32);
	 }

	  public Calendar timeout(){
		  Calendar date = new GregorianCalendar(); //date now
		  
			 int i = date.get(Calendar.MINUTE); //minutes now
			 i+=15; //15 minute timer
			 date.set(Calendar.MINUTE, i); //add timer
			 //System.out.println(date.getTime());
			 //boolean date1 = new Date().after(date); 
			 //is current time after end time *Should return false*
			
			 return date;
			 }
	  

}