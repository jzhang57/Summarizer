package src;
import java.util.Comparator;

class SentenceComparatorForSummary  implements Comparator<Sentence>{
	@Override
	public int compare(Sentence obj1, Sentence obj2) {
		
		if (obj1.sentenceIndex > obj2.sentenceIndex){
			return 1;
		} else if (obj1.sentenceIndex < obj2.sentenceIndex){
			return -1;
		} else {
			return 0;
		}
	}
}