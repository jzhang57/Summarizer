package src;
class Sentence{
	int paragraphNumber;
	int sentenceIndex;
	int stringLength; 
	double score;
	int numWords;
	String value;

	Sentence(int number, String value, int stringLength, int paragraphNumber){
		sentenceIndex = number;
		this.value = new String(value);
		this.stringLength = stringLength;
		numWords = value.split("\\s+").length;
		score = 0.0;
		this.paragraphNumber=paragraphNumber;
	}
}