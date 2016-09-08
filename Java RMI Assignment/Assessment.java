package ct414;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.io.Serializable;

public interface Assessment extends Serializable {

	// Return information about the assessment	
	public String getInformation();
	
	//Return course code for assessment
	public String getCourseCode();

	// Return the final date / time for submission of completed assessment
	public Calendar getClosingDate();

	// Return a list of all questions and answer options
	public List<QuestionImpl> getQuestions();

	// Return one question only with answer options
	public QuestionImpl getQuestion(int questionNumber) throws
		InvalidQuestionNumber;

	// Answer a particular question
	public void selectAnswer(int questionNumber, int optionNumber) throws
		InvalidQuestionNumber, InvalidOptionNumber;

	// Return selected answer or zero if none selected yet
	public int getSelectedAnswer(int questionNumber);

	// Return studentid associated with this assessment object
	// This will be preset on the server before object is downloaded
	public int getAssociatedID();

	public void setAssociatedID(int id);
}