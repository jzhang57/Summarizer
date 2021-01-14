package src;
class improved_summary{
	public static void main(String[] args){

		
	/* If you wish to single thread of multi thread, comment out the 
	   corresponding code! */
		 

    /* ========= Multi-thread Portion of Code, uses 8 threads. ============= */

        /* Determine the amount of work each thread should be doing. If there
        are, not divisible by 8, number of files, the threads will spread the  
        remainder to the beginning threads. */
        int numFiles = args.length;
        int numThreads = 8;
        int equalWork = numFiles/numThreads;
        int currentRemainder = numFiles % numThreads;
        int currentIndex = 0;
        String [] fileNames = args;
        

        SummaryTool s = new SummaryTool();
        s.startThreads(numThreads, numFiles, equalWork, currentRemainder, currentIndex, fileNames);
    		
    	

    /* ========= Single threaded portion of Code, uses 1 thread =========== */

//        int numFiles = args.length;
//        String [] fileNames = args;
//
//        SummaryTool s2 = new SummaryTool();
//        s2.startThreads2(numFiles, fileNames);
	}	
	
}
