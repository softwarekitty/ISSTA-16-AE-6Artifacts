package recreateArtifacts.similarityMatrix;

import java.io.File;
import java.util.List;

import main.io.IOUtil;
import recreateArtifacts.PathUtil;

public class Main_MatrixBuilder {

    static void Main(String[] args)
    {
        System.out.println("hello from MatrixBuilder");
        String allRowsBase = PathUtil.allRowsBase();
        String rexStringsBase = PathUtil.rexStringsBase();
        String filteredCorpusPath = PathUtil.pathToFilteredCorpus();
        File filteredCorpusFile = new File(filteredCorpusPath);

        if(!filteredCorpusFile.exists()){
        	throw new RuntimeException("missing required file: "+filteredCorpusPath);
        }
        double minSimilarity = 0.75;


        //determine nRows by inspecting the line numbers in filteredCorpus.txt
        int nRows = IOUtil.readLines(filteredCorpusPath).size();
        int nRowsBefore = nRowsExist(allRowsBase, nRows);
        
        //TODO - thought this was 400, was this part of that one experiment?
        int nRexStringsToUse = 300;

        // we have to do batches bc runaway regex matchings never release memory
        int batchSize = 256;
        if (nRowsBefore==nRows)
        {
        	System.out.println("all row files are present! Counting unverified rows");
            int unverifiedTimeoutRows = RowUtil.countUnverifiedRows(allRowsBase, nRows);
            if(unverifiedTimeoutRows > 0)
             {
                int nRunnawaysWithoutStress = 1024;
                System.out.println(unverifiedTimeoutRows+ " rows have unverified timeouts.  verifying a chunk of up to "+nRunnawaysWithoutStress+" cells with timeouts.");
                TimeoutVerifier.verifyRows(allRowsBase,nRows,minSimilarity, filteredCorpusPath,nRunnawaysWithoutStress, batchSize, rexStringsBase, nRexStringsToUse);
                System.out.println("chunk of timeout verification complete");
            }
            else
            {
            	System.out.println("all cells are valid - creating matrices and abc file");
            PostProcess.createMatricesAndABC(allRowsBase, nRows, minSimilarity, filteredCorpusPath);
            System.out.println("matrix and abc files are written - exiting");
            return;
            }
        }
        else
        {

        	System.out.println("batchSize: " + batchSize + " nRowsBefore: "+nRowsBefore+ " nRows: "+nRows);
            SimilarityMatrixBuilder.createBatchOfRows(batchSize,allRowsBase,filteredCorpusPath, rexStringsBase, minSimilarity, nRexStringsToUse);
            int nRowsAfter = nRowsExist(allRowsBase, nRows);
            System.out.println("nRowsAfter: "+nRowsAfter + " diff: "+(nRowsAfter-nRowsBefore)+" batchSize:"+batchSize );
            return;
        }
    }


    private static int nRowsExist(String allRowsBase, int nRows)
    {

        // create all the bucket directories if this is the first time here
        List<String> bucketList = RowUtil.getBucketList(nRows);
        for (String bucketName : bucketList)
        {
            String rowBucketDirectory = allRowsBase + bucketName;
            File rowBucketFile = new File(rowBucketDirectory);
            if(!rowBucketFile.exists()){
            	rowBucketFile.mkdirs();
            }
        }

        
        //count the times a file exists for a row in its bucket
        int numRowsExist = 0;
        for (int rowIndex = 0; rowIndex < nRows; rowIndex++)
        {

            String rowFilePath = RowUtil.getRowFilePath(allRowsBase,nRows,rowIndex);
            File rowFile = new File(rowFilePath);
            if (rowFile.exists())
            {
                numRowsExist++;
            }
        }
        return numRowsExist;
    }

}
