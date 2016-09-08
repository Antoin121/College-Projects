package ct414;


	public class QuestionImpl implements Question {

		private int questionNum;
		private String questionDetail;
		private String[] answersOpt;
		
		public QuestionImpl(int qNum, String qDetail, String[] a){
			questionNum=qNum;
			questionDetail=qDetail;
			answersOpt=a;
		}
		
		// Return the question number
		public int getQuestionNumber() {
			return questionNum;
		}

		// Return the question text
		public String getQuestionDetail() {
			return questionDetail;
		}

		// Return the possible answers to select from
		public String[] getAnswerOptions() {
			return answersOpt;
		}

}
