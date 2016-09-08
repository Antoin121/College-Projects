package ct414;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.io.Serializable;


public interface TokenInterface extends Serializable{


	
	public String getToken();
	
	public Calendar getTimeout();

	public String nextSessionId() ;

	  public Calendar timeout();
}
