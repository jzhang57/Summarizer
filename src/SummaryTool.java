package src;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Collections;


public class SummaryTool{

    public class SummaryWorker implements Runnable {

        /* This determines what files each thread will be summarizing */
		private int startIndex;
		private int endIndex;
        	String [] allFileNames;
		
		public SummaryWorker (int start, int end, String[] fileNames) {
			startIndex = start;
			endIndex = end;
            		allFileNames = fileNames;
		}
		
		@Override
		public void run() {
			tool(startIndex, endIndex);
			
		}
		
		/* Each SummaryWorker does it's own portion of work 
        The start and end help determine what files they do */
		private void tool (int start, int end) {

	    	for (int x = start; x <= end; x++) {
                SummaryTool summary = new SummaryTool();
                
                summary.chooseFile("" + allFileNames[x]);
		        summary.extractSentences();
		        summary.groupSentencesIntoParagraphs();
		        summary.createMatrix();
		        summary.createDictionary();
		        summary.createSummary();
            }
			
		}
    }

	FileInputStream readFrom;
	FileOutputStream writeTo;
	ArrayList<Sentence> sentences, contentSummary;
	ArrayList<Paragraph> paragraphs;
	int numSentences, numParagraphs;
	double[][] matrix;
	LinkedHashMap<Sentence,Double> dictionary;

    

    /* Constructor which initializes all class variables */
    public SummaryTool(){
		sentences = new ArrayList<Sentence>();
		paragraphs = new ArrayList<Paragraph>();
		contentSummary = new ArrayList<Sentence>();
		dictionary = new LinkedHashMap<Sentence,Double>();
		numSentences = 0;
		numParagraphs = 0;
	}
    
    
    
    /* Starts the multi-threading while dividing up the work */
    public void startThreads(int numThreads, int numFiles, int equalWork, 
    		int currentRemainder, int currentIndex, String[] fileNames) {
    	
    	/* Assign the work of each thread */
        while (currentIndex < numFiles) {
    		if (currentRemainder != 0) {
    			Thread t = new Thread(new SummaryWorker (currentIndex, 
    					currentIndex + equalWork, fileNames));
    			t.start();

    			currentIndex = currentIndex + equalWork + 1; 
    			currentRemainder--;
    		} else {
    			Thread t = new Thread(new SummaryWorker (currentIndex, 
    					currentIndex + equalWork, fileNames));
    			
    			t.start();

    			currentIndex = currentIndex + equalWork;
    		}
    		
    	}
    }
    
    /* Starts the single-threading while dividing up the work */
    public void startThreads2(int numFiles, String[] fileNames) {
    	
    	Thread t = new Thread(new SummaryWorker(0, numFiles - 1, fileNames));
    	t.start();
    }
    
    
    


    /* Sets the readFrom and writeTo files. If a file name doesn't
    exist as a file, we simply print out "Wrong File Name" within
    the writeTo file. */
	public void chooseFile(String fileName){
		try {
	        readFrom = new FileInputStream("" + fileName);
	        writeTo = new FileOutputStream("" + fileName + "output.txt");
    	}catch(FileNotFoundException e){
    		System.out.println("Wrong File Name");
    	}catch(Exception e){
    		e.printStackTrace();
    	}
	}



	/* Breaks down the entire file into sentences. Appends the sentences
    onto a linkedlist called 'sentences' */
	public void extractSentences(){
		int nextChar, j = 0;
		int prevChar = -1;

        try{
            /* Keeps reading until the End of File */
	        while((nextChar = readFrom.read()) != -1) {
				j = 0;
	        	char[] temp = new char[100000];

                /* Determining if we're at the end of a sentence */
	        	while((char)nextChar != '.'){
	        		
	        		temp[j] = (char)nextChar;
	        		if((nextChar = readFrom.read()) == -1){
	        			break;
	        		}

                    /* Two new lines indicate a new paragraph indent */
	        		if((char)nextChar == '\n' && (char)prevChar == '\n'){
	        			numParagraphs++;
	        		}
	        		j++;
	        		prevChar = nextChar;
	        	}

	        	sentences.add(new Sentence(numSentences,(new String(temp)).trim(),(new String(temp)).trim().length(),numParagraphs));
	        	numSentences++;
	        	prevChar = nextChar;
	        }
	    }catch(Exception e){
	    	e.printStackTrace();
	    }

	}



    /* We now group all the sentences together based of the paragraph number 
    assigned to them */
	void groupSentencesIntoParagraphs(){
		int paraNum = 0;
		Paragraph paragraph = new Paragraph(0);

		for(int i = 0; i < numSentences; i++){
			if(sentences.get(i).paragraphNumber == paraNum){
				continue;

            /* Append all paragraphs to the list */
			}else{
				paragraphs.add(paragraph);
				paraNum++;
				paragraph = new Paragraph(paraNum);
				
			}

            /* This is where we append the sentences to their respective
            paragraphs depending on what paragraphIndex they belong to */
			paragraph.sentences.add(sentences.get(i));
		}

		paragraphs.add(paragraph);
	}



    /* Find the the number of common words between two sentences 
    and return it */
	double numCommon(Sentence str1, Sentence str2){
		double commonCount = 0;

        /* This a double for loop that'll compare the word's of each sentence. */
		for(String str1Word : str1.value.split("\\s+")){
			for(String str2Word : str2.value.split("\\s+")){
				if(str1Word.compareToIgnoreCase(str2Word) == 0){
					commonCount++;
				}
			}
		}

		return commonCount;
	}



    /* This creates a intersection matrix which will hold the intersection scores between
    all sentences. So matrix[0][1], will hold the intersection scores 
    between the first sentence and second sentence of the text */
	void createMatrix(){
		matrix = new double[numSentences][numSentences];
		for(int i = 0; i < numSentences; i++) {
			for(int j = 0; j < numSentences; j++){
                
                /* If enter the first conditional, that means the two sentences
                haven't been compared yet. There is no intersection score between the two. */
				if(i <= j){
					Sentence str1 = sentences.get(i);
					Sentence str2 = sentences.get(j);
                    double averageWordsPerSentence = (str1.numWords + str2.numWords)/2;

					matrix[i][j] = numCommon(str1,str2) / averageWordsPerSentence;
				}else{ /* Don't want to repeat any two sentences that have already been compared */
					matrix[i][j] = matrix[j][i];
				}
				
			}
		}
	}



    /* The dictionary calculates an individual score for each sentence and stores  it within the
    HashMap, by calculating all the intersections with the other sentences in the text. */
	void createDictionary(){
		for (int i = 0; i < numSentences; i++) {
			double score = 0;

			for (int j = 0; j < numSentences; j++) {
				score += matrix[i][j];
			}
			dictionary.put(sentences.get(i), score);
			((Sentence)sentences.get(i)).score = score;
		}
	}


    /* We split the text into paragraphs, and choose the best sentence from 
    each paragraph according to the dictionary greated */
	void createSummary(){

	      for(int j = 0;j <= numParagraphs; j++){
	      		int primary_set = paragraphs.get(j).sentences.size()/5; 

	      		//Sort based on score (importance)
	      		Collections.sort(paragraphs.get(j).sentences, new SentenceComparator());
		      	for(int i = 0; i <= primary_set; i++){
		      		contentSummary.add(paragraphs.get(j).sentences.get(i));
		      	}
	      }

	      /* This wil make the sentences of the Summary be in order, relative to
           to the order of the sentences from the ORIGINAL text */
	      Collections.sort(contentSummary, new SentenceComparatorForSummary());
		
	}



    
	
    /* Outputs the summary to the output.txt file of the corresponding file. */
	void printSummary () {
        String sum = "";
        
        /* This now puts the entire summary into sum */
		for(Sentence sentence : contentSummary){
            sum += sentence.value + '\n';
		}
		
		
		/* Attempts to write the output to the output file */
		try {
	        byte[] bytesArray = sum.getBytes();
	        writeTo.write(bytesArray);
	        writeTo.flush();
	        System.out.println("Summary Written Successfully");
	
	       
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			
			/* Attempts to close the file once the summary is all in the output file */
			try {
				writeTo.close();
			} catch (IOException e2) {
				System.out.println("Error in closing stream");
			}
		}
    }
	


}
