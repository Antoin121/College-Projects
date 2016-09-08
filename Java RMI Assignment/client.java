package ct414;

import java.rmi.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.io.*;

public class client {

	public static void main(String args[]) throws Exception {

		ExamServer exam = (ExamServer) Naming.lookup("//localhost/ExamServer");

		int id = 0, opt = 0;
		TokenInterface token = new token("Failed Login", new GregorianCalendar());
		boolean valid = false;
		boolean finished = false;
		boolean validCourseCode=false;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {

			do {
				System.out.println("\n\n      Assesment Login    \n\n ");

				System.out.print("Enter Student ID: ");

				try {
					id = Integer.parseInt(br.readLine());
				} catch (NumberFormatException nfe) {
					System.err.println("Invalid Format!");
				}

				System.out.print("\nEnter Password: ");
				String password = br.readLine();
				try{
				System.out.println("\nAttempting Login...");
				token = exam.login(id, password);
				if (!(token.getToken().equals("Failed Login"))) {
					valid = true;
					System.out.println("\n You're Logged in!");
				}
				}catch(UnauthorizedAccess ua){
					System.err.println("\nError Login Failed please try again.");
				}


			} while (valid == false);

			do {
				boolean submitted = false;
				// BufferedReader brr = new BufferedReader(new
				// InputStreamReader(System.in));
				
				//Calendar d = new GregorianCalendar();
				//d.getTime();
	    //		if (d.getTime().before(token.getTimeout().getTime()) == true)
				int i = 1;
				List<String> summarys = new ArrayList<String>();
				try{
				summarys = exam.getAvailableSummary(token, id);
				}catch(UnauthorizedAccess ua){
					System.err.println("\nError you have timed out.");
				}
				catch(NoMatchingAssessment nma){
					System.err.println("\nError there is no matching assessments for you.");
				}
				
				if (summarys.size()>0  && new GregorianCalendar().getTime().before(token.getTimeout().getTime()) == true){
					System.out.println("\nAssesments avaiable to you\n");
				for (String s : summarys) {
					System.out.println(i + ") " + s + "\n\n");
					i++;
				}
				System.out
						.println("\nPlease enter the course code of the assmessment you would like do:");
				Assessment myAssessment = null;
				List<QuestionImpl> questions = null;
				String courseCode = br.readLine();
				
				try {
					myAssessment = exam.getAssessment(token, id, courseCode);
					if (myAssessment != null && new GregorianCalendar().getTime().before(token.getTimeout().getTime()) == true){
						validCourseCode = true;
						System.out.println("\nCourse Code: "
							+ myAssessment.getCourseCode() + "\n\nSummary:\n"
							+ myAssessment.getInformation()
							+ "\nClosing Date: "
							+ myAssessment.getClosingDate().getTime());

					questions = myAssessment.getQuestions();
					if(questions != null && new GregorianCalendar().getTime().before(token.getTimeout().getTime()) == true){
					System.out.println("\n\n        Questions\n\n");
					for (Question q : questions) {
						String[] answersOpt = q.getAnswerOptions();

						System.out.println(q.getQuestionDetail());
						System.out.println("\n\n       Answer Choices: \t1) "
								+ answersOpt[0] + "\t2)" + answersOpt[1]
								+ "\t3)" + answersOpt[2] + "\t4)"
								+ answersOpt[3]);

					}						
					}
					else if(questions == null){
						System.out.println("Error retrieving questions");
					}
					}					
					else if (myAssessment == null && new GregorianCalendar().getTime().before(token.getTimeout().getTime()) == true){
						System.err.println("Closing Date has passed for all your assessments.");
					}
				} catch (NoMatchingAssessment e) {
					System.err.println("Invalid Course Code");
				}
				 catch (UnauthorizedAccess e) {
						System.err.println("Error you timed out");
					}
				if (myAssessment != null && questions != null && new GregorianCalendar().getTime().before(token.getTimeout().getTime()) == true){
				do {
					int option = 0, answerSelected = 0;
					System.out
							.println("\nPlease select from the options 1,2 or 3");
					System.out.println("\n1) Answer Question\n2) Change Answer"
							+ "\n3) Submit Assessment\n");
					try {
						option = Integer.parseInt(br.readLine());
					} catch (NumberFormatException nfe) {
						System.err.println("Invalid Format!");
					}

					if (option == 1 && new GregorianCalendar().getTime().before(token.getTimeout().getTime()) == true) {

						int qNum = 1;
						for (Question q : questions) {

							String[] answersOpt = q.getAnswerOptions();

							System.out.println(q.getQuestionDetail());
							System.out
									.println("\n\n       Answer Chocies: \t1) "
											+ answersOpt[0] + "\t2)"
											+ answersOpt[1] + "\t3)"
											+ answersOpt[2] + "\t4)"
											+ answersOpt[3]);

							int preAns = myAssessment.getSelectedAnswer(q
									.getQuestionNumber());

							if (preAns != 0) {

								System.out
										.println("\nPrevious Selected Answer Number: "
												+ answersOpt[preAns - 1]);
							}

							System.out.println("Enter answer number: ");
							try {
								answerSelected = Integer
										.parseInt(br.readLine());
							} catch (NumberFormatException nfe) {
								System.err.println("Invalid Format!");
							}
							try{
								myAssessment.selectAnswer(qNum, answerSelected);
								}catch(InvalidQuestionNumber iqn){
									System.err.println("Invalid question number entered");	
								}catch(InvalidOptionNumber ion){
									System.err.println("Invalid answer number entered");
								}
							qNum++;
							
							if(new GregorianCalendar().getTime().after(token.getTimeout().getTime()) == true){
								break;
							}
						}
					}

					else if (option == 2 && new GregorianCalendar().getTime().before(token.getTimeout().getTime()) == true) {
						int qChange = 0;
						System.out
								.println("Enter the number of the question you would like change: ");
						try {
							qChange = Integer.parseInt(br.readLine());
						} catch (NumberFormatException nfe) {
							System.err.println("Invalid Format!");
						}
						try{
						Question q = myAssessment.getQuestion(qChange);
						System.out.println("Getting answers " + qChange);
						String[] answersOpt = q.getAnswerOptions();
						System.out.println(qChange);

						System.out.println(q.getQuestionDetail());
						System.out.println("\n\n       Answer Chocies: \t1) "
								+ answersOpt[0] + "\t2)" + answersOpt[1]
								+ "\t3)" + answersOpt[2] + "\t4)"
								+ answersOpt[3]);

						int previousAns = myAssessment.getSelectedAnswer(q
								.getQuestionNumber());

						if (previousAns != 0) {

							System.out
									.println("\nPrevious Selected Answer Number: "
											+ answersOpt[previousAns - 1]);
						}

						System.out.println("Enter new answer number: ");

						try {
							answerSelected = Integer.parseInt(br.readLine());
						} catch (NumberFormatException nfe) {
							System.err.println("Invalid Format!");
						}
						}catch(InvalidQuestionNumber iqn){
							System.err.println("Invalid question number entered");
							answerSelected =0;
						}
						try{
						myAssessment.selectAnswer(qChange, answerSelected);
						}catch(InvalidQuestionNumber iqn){
							//didn't print error as error is already printed from catch above	
						}catch(InvalidOptionNumber ion){
							System.err.println("Invalid answer number entered");
						}
					} else if (option == 3 && new GregorianCalendar().getTime().before(token.getTimeout().getTime()) == true) {
						System.out.println(" Submitting Assignment... ");
						try{
						exam.submitAssessment(token, id, myAssessment);
						System.out.println(" Submitted");
						submitted = true;
						}catch(UnauthorizedAccess ua){
							System.err.println("Error you timed out");
						}
						catch(NoMatchingAssessment nma){
							System.err.println("Error attempted submit null assessment");
						}
					}
					//Asking user to login again if timed out
					 if (new GregorianCalendar().getTime().after(token.getTimeout().getTime()) == true){
						System.out.println("\nYou have timed out please login again");
						do {
							System.out.println("\n\n      Assesment Login    \n\n ");

							System.out.print("Enter Student ID: ");

							try {
								id = Integer.parseInt(br.readLine());
							} catch (NumberFormatException nfe) {
								System.err.println("Invalid Format!");
							}

							System.out.print("\nEnter Password: ");
							String password = br.readLine();

							System.out.println("\nAttempting Login...");
							token = exam.login(id, password);

							if (token.getToken().equals("Failed Login")) {
								valid = false;
								System.out.println("\n Login failed please try again.");
							} else {
								valid = true;
								System.out.println("\n Your logged in!");
							}

						} while (valid == false);
					}

				} while (submitted == false);
				}
				

				if(validCourseCode==true){
				System.out.println("\nWould you like to log of? (Yes/No)");
				String logoff = br.readLine();

				if (logoff.equalsIgnoreCase("Yes")) {
					token = null;
					finished = true;
					System.out.println("\nYou're logged out!");
				}
				}
			}
				else if (summarys.size()== 0 && new GregorianCalendar().getTime().before(token.getTimeout().getTime()) == true){
					System.err.println("Closing Date has passed for all your assessments.");
					break;
				}
				//Asking user to login again if timed out
				if (new GregorianCalendar().getTime().after(token.getTimeout().getTime()) == true){
					System.out.println("\nYou have timed out please login again");
					do {
						System.out.println("\n\n      Assesment Login    \n\n ");

						System.out.print("Enter Student ID: ");

						try {
							id = Integer.parseInt(br.readLine());
						} catch (NumberFormatException nfe) {
							System.err.println("Invalid Format!");
						}

						System.out.print("\nEnter Password: ");
						String password = br.readLine();

						System.out.println("\nAttempting Login...");
						token = exam.login(id, password);

						if (token.getToken().equals("Failed Login")) {
							valid = false;
							System.out.println("\n Login failed please try again.");
						} else {
							valid = true;
							System.out.println("\n Your logged in!");
						}

					} while (valid == false);
				}
				
				validCourseCode=false;
			} while (finished == false);

		}

		catch (Exception e) {
		}
	}

}