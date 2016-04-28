#Similarity Matrix

######Inputs: contents of ```rexStrings``` folder, and *filteredCorpus.tsv*

######Output: *similarityGraph.abc*

##In Progress

This program is working, but may not be fully debugged.  The original version is in C#, and this version is a port to Java.

####Why this program is complex
When doing 300 regex matching operations for a 2600 x 2600 matrix, that is over 2 billion operations, any of which can hang.  This complexity is mitigated by timing out regexes that hang, and doing the analysis in small batches.  After all the regexes are analysed, the cancelled regexes are validated (that they really do hang) in a second pass.

##How to run this program
Run the main file: *Main_MatrixBuilder*

You should see output like: completed i: 506/2871 nMatchStrings:300

Some exceptions may be thrown (later versions will be more clean), but rows of the matrix are being written to the output.  After 256 rows have been written, the program will stop by itself.  This is to free memory absorbed by various hanging regexes.  Run the main file again and you will notice that the index `i` picks up where it left off, as the program is aware of row files in the output folder.  Repeat this process until the program says it is done.

Running this program will take a lot of patience - some rows seem to be completed instantly, and others take the full minute allowed, before some hanging regex times out.

When all rows are complete, another pass will validate the cells of the matrix that may have been interrupted, and then the program produces the *similarityGraph.abc* file.





