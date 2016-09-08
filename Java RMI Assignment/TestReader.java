package ct414;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class TestReader {
	public TestReader() {
		
	}
		public Assessment getAssessment(String courseCode) throws FileNotFoundException{

		String filename = null;
		String cc1 = "CT414";
		String cc2 = "CT424";
		String cc3 = "CT420";
		
		if(courseCode.equalsIgnoreCase(cc1)){
			filename = "Assessment1.txt";
		}
		else if(courseCode.equalsIgnoreCase(cc2)){
			filename = "Assessment2.txt";
		}
		else if(courseCode.equalsIgnoreCase(cc3)){
			filename = "Assessment3.txt";
		}
		
		FileReader inputFile = new FileReader(filename);
		BufferedReader bufferedReader = new BufferedReader(inputFile);
		Scanner scanner = new Scanner(bufferedReader);

		String Summary = scanner.nextLine();
		String textClosingDate = scanner.nextLine();
		StringTokenizer st = new StringTokenizer(textClosingDate, "/");
		int day = Integer.parseInt(st.nextToken());
		int month = Integer.parseInt(st.nextToken())-1;
		int year = Integer.parseInt(st.nextToken());
		Calendar closingDate = new GregorianCalendar(year,month,day);

		List<QuestionImpl> qs = new ArrayList<QuestionImpl>(5);
		// loop
		for (int i = 1; i < 6; i++) {

			String Question = scanner.nextLine();
			String[] arr = new String[4];
			arr[0] = scanner.nextLine();
			arr[1] = scanner.nextLine();
			arr[2] = scanner.nextLine();
			arr[3] = scanner.nextLine();
			
			QuestionImpl q = new QuestionImpl(i, Question, arr); //q number, q text, choices
			qs.add(q);
		}
		
		AssessmentImpl a = new AssessmentImpl(Summary, closingDate, qs, courseCode); //Summary, Date, questions
		
		return a;

	}
}
