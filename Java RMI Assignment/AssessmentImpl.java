package ct414;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AssessmentImpl implements Assessment {

	String Information;
	Calendar closingDate;
	List<QuestionImpl> questions;
	String courseCode;
	QuestionImpl q;
	int qNumber;
	int[] answers = new int[5];
	int studentid;
	
    // Constructor is required
    public AssessmentImpl(String information, Calendar closingDate, List<QuestionImpl> questions, String courseCode) {
        this.Information = information;
        this.closingDate = closingDate;
        this.questions = questions;
        this.courseCode = courseCode;
        answers[0]=0;
        answers[1]=0;
        answers[2]=0;
        answers[3]=0;
        answers[4]=0;
        
    }

	// Return information about the assessment	
	public String getInformation(){
		return Information;
	}
	
	//Course code for assessment
	public String getCourseCode(){
		return courseCode;
	}

	// Return the final date / time for submission of completed assessment
	public Calendar getClosingDate(){
		return closingDate;
	}

	// Return a list of all questions and answer options
	public List<QuestionImpl> getQuestions(){
		//q.getAnswerOptions();
		return questions;
		
	}

	// Return one question only with answer options
	public QuestionImpl getQuestion(int questionNumber) throws InvalidQuestionNumber{		
		if(questionNumber<=5 && questionNumber>0){
			QuestionImpl q = questions.get(questionNumber-1);
		return q;
		}
		else{
			throw new InvalidQuestionNumber();
		}
	}

	// Answer a particular question
	public void selectAnswer(int questionNumber, int optionNumber) throws
		InvalidQuestionNumber, InvalidOptionNumber{	
		
//		List<QuestionImpl> qtemp = getQuestions();
//		qtemp.get(questionNumber-1);
		if(questionNumber<=5 && questionNumber>0 && optionNumber>0 && optionNumber<=4){
		answers[questionNumber-1] = optionNumber;
		}
		else if(questionNumber>5 || questionNumber<=0){
			throw new InvalidQuestionNumber();
		}
		else if(optionNumber<=0 || optionNumber>4){
			throw new InvalidOptionNumber();
		}
	}

	// Return selected answer or zero if none selected yet
	public int getSelectedAnswer(int questionNumber){
		return answers[questionNumber-1];
	}
	
	// Return studentid associated with this assessment object
	// This will be preset on the server before object is downloaded
	public int getAssociatedID(){
		return studentid;
	}
	
	public void setAssociatedID(int id){
		studentid = id;
	}


}