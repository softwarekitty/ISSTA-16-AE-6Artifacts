package recreateArtifacts.similarityMatrix;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class SimilarityMatrixBuilder
{
    public static double initializedFlag = 0.00000987654321;
    public static double incompleteFlag = 0.00000123456789;
    public static double cancelledFlag = 0.0000050101010101;
    public static double verifiedTimeoutFlag = 0.00000701702703;
    public static double belowMinFlag = 0.00000307207107;

    public static void createBatchOfRows(Integer batchSize, String allRowsBase, String filteredCorpusPath, String rexStringsBase, double minSimilarity, Integer nMatchStrings)
    {

        // create regexMap and keyList
        HashMap<Integer, Regex> regexMap = new HashMap<Integer, Regex>();

        Regex numberFinder = new Regex("(\\d+)\\t(.*)");
        using (StreamReader r = new StreamReader(filteredCorpusPath))
        {
            String line = null;
            while ((line=r.ReadLine()) != null)
            {
                Match lineMatch = numberFinder.Match(line);
                if (lineMatch.Success)
                {
                    Integer index = Integer.Parse(lineMatch.Groups[1].Value);
                    String pattern = lineMatch.Groups[2].Value;
                    regexMap.Add(index, new Regex(pattern));
                }
            }
        }
        List<Integer> keyList = new List<Integer>(regexMap.Keys);
        keyList.Sort();

        //create some seeds so that different runs on the same data are the same
        Random gen = new Random(Integer.MaxValue);
        Integer[] differentSeeds = Enumerable.Repeat(0, keyList.Count).Select(i => gen.Next(0, Integer.MaxValue)).toArray();

        //each of these will create 128 threads, so keep the paralelism down
        ParallelOptions options = new ParallelOptions();
        options.MaxDegreeOfParallelism = 4;

        //these do not have to be sequential or in order.
        Integer[] batchIndices = getBatchOfIndices(allRowsBase,keyList.Count, batchSize);
        Integer jitty = 0;
        Parallel.For(0, batchIndices.Length, options, i => evaluateOneRow(i, batchIndices,differentSeeds[i], rexStringsBase, regexMap, keyList, minSimilarity,allRowsBase, nMatchStrings));
    }


    static void evaluateOneRow(Integer batchArrayIndex, Integer[] batchIndices, Integer differentSeed, String rexStringsBase, HashMap<Integer, Regex> regexMap,List<Integer> keyList, double minSimilarity, String rowFileBase, Integer nMatchStrings)
    {
        Integer rowIndex = batchIndices[batchArrayIndex];
        Integer nKeys = keyList.size();
        double[] rowArray = new double[nKeys];
        for (Integer rowArrayIndex = 0; rowArrayIndex < nKeys; rowArrayIndex++)
        {
            rowArray[rowArrayIndex] = initializedFlag;
        }
        Random gen = new Random(differentSeed);

        HashSet<String> matchingStrings_outer = RowUtil.getRexGeneratedStrings(rowIndex,nKeys,rexStringsBase, nMatchStrings);
        
        // TODO - maybe check this, it's important logic about when to skip
        int maxErrors = (int)Math.ceil(((1 - minSimilarity) * nMatchStrings))+1;

        //in order to protect against exponential worst-case regexes we chunk the row,
        //and wait in powers of two for each chunk.  It's a mess but we cannot get inside of
        //this library code: 'regex_inner.Match(matchingString)' to do propper cancelation
        Integer[] nTimeouts = { 0 };
        Integer chunkSize = 128;
        Integer nChunks = nKeys / chunkSize;
        Integer remainder = nKeys % chunkSize;
        for (Integer chunkIndex = 0; chunkIndex < nChunks; chunkIndex++)
        {
            Integer chunkStart = chunkIndex * chunkSize;
            Integer chunkStop = chunkStart + chunkSize;
            processChunk(rowIndex,rowArray, chunkStart, chunkStop, matchingStrings_outer, regexMap, maxErrors, keyList, nTimeouts);
        }
        if (remainder > 0)
        {
            Integer chunkStart = nChunks * chunkSize;
            Integer chunkStop = chunkStart + remainder;
            processChunk(rowIndex,rowArray, chunkStart, chunkStop, matchingStrings_outer, regexMap, maxErrors, keyList,nTimeouts);
        }
        MatrixRow mr = new MatrixRow(rowIndex,rowArray, nKeys);
        mr.writeRowToFile(rowFileBase,minSimilarity);
        System.out.println("completed i: " +rowIndex + "/" + nKeys + " nTimeouts: " + nTimeouts[0] + " nMatchStrings:" + matchingStrings_outer.size());
    }

    static void processChunk(Integer rowIndex,double[] rowArray, Integer chunkStart, Integer chunkStop, HashSet<String> matchingStrings_outer, HashMap<Integer, Regex> regexMap,Integer maxErrors, List<Integer> keyList, Integer[] nTimeouts)
    {

        // rarely, a pathological regex can hang for a very long time.
        // but most of these finish within 120 microseconds (rate: 8300/sec)
        // usually should be able to do 128 in 16 milliseconds so start there
        Integer taskIndex = 0;
        Task[] tasks = new Task[chunkStop-chunkStart];
        CancellationTokenSource tokenSource = new CancellationTokenSource();
        for (Integer keyIndex = chunkStart; keyIndex < chunkStop; keyIndex++)
        {

            //get the regex mapped to this cell within the chunk
            Integer innerKey = keyList[keyIndex];
            Regex regex_inner = regexMap[innerKey];

            //wow, strange it looks like the keyIndex++
            //triggered at the end of the loop passes Integero the 
            //populateRowCell(...) code, so it gets the next value!
            Integer j_dont_increment_me = keyIndex;
            if (j_dont_increment_me >= rowArray.Length)
            {
                Console.WriteLine();
                throw new Exception("the j_dont_increment_me: " + j_dont_increment_me + " will be outside of the array length: " + rowArray.Length);
            }

            //create a task to populate that row cell
            //this task may hang and never die.
            tasks[taskIndex++] = Task.Factory.StartNew(() => populateRowCell(rowIndex, j_dont_increment_me, rowArray, matchingStrings_outer, regex_inner, maxErrors, tokenSource.Token));
        }
        bool chunkComplete = RowUtil.waitForTasks(tasks);
        if (!chunkComplete)
        {
            tokenSource.Cancel();
            for (Integer j = chunkStart; j < chunkStop; j++)
            {
                double currentRowValue = rowArray[j];
                if (currentRowValue == incompleteFlag || currentRowValue == initializedFlag || currentRowValue == cancelledFlag)
                {
                    nTimeouts[0]++;
                }
            }
        }
    }

    static void populateRowCell(Integer i, Integer j, double[] row, HashSet<String> matchingStrings_outer, Regex regex_inner, Integer maxErrors,CancellationToken ct)
    {
        if (i == j)
        {
            row[j] = 1.0;
            return;
        }
        double nMatchingStrings = matchingStrings_outer.size();
        Integer alsoMatchingCounter = 0;
        Integer errorCounter = 0;

        // for debugging - did we get this far?
        row[j] = incompleteFlag;

        for (String matchingString : matchingStrings_outer)
        {

            // free up resources ASAP if the task group has timed out
            if (ct.IsCancellationRequested)
            {
                row[j] = cancelledFlag;
                return;

                // after a certain number of errors, it is impossible to
                // be included anyway - give up early
                // this may drag down some corner cases when they are
                // averaged in the half-matrix, but will save a lot of time, 
                // since the vast majority of these comparisons have 0 similarity
                // anyway, maybe n^2 - n of them
            }else if(errorCounter > maxErrors){
                break;
            }


            Match attemptMatch = regex_inner.Match(matchingString);
            if (attemptMatch.Success)
            {
                alsoMatchingCounter++;
            }
            else
            {
                errorCounter++;
            }
        }
        double similarity = alsoMatchingCounter / nMatchingStrings;
        row[j] = similarity;
    }

    // helpers





    private static Integer[] getBatchOfIndices(String allRowsBase, Integer nKeys, Integer batchSize)
    {
        Integer nAdded = 0;
        List<Integer> indices = new LinkedList<Integer>();
        for (Integer rowIndex = 0; rowIndex < nKeys; rowIndex++)
        {
            String rowPath = RowUtil.getRowFilePath(allRowsBase, nKeys, rowIndex);
            File rowFile = new File(rowPath);
            if (!rowFile.exists())
            {
                indices.add(rowIndex);
                nAdded++;
            }
            if (nAdded >= batchSize)
            {
                return indices.toArray(new Integer[indices.size()]);
            }
        }

        return indices.toArray(new Integer[indices.size()]);
    }
}
