#Similarity Matrix

######Inputs: contents of ```rexStrings``` folder, and *filteredCorpus.tsv*

######Outputs: *similarityGraph.abc* (and row files)

####Hanging regexes
Most regexes behave well, and so to avoid creating too many threads, a set of inputs is evaluated in order.  Catastrophic backtracking and other isses can lead to a regex occasionally hanging on a particular input.  When this happens, the cell is marked as cancelled, and in another pass, all the cells that were cancelled have every input evaluated separately, aggregating the values that do match but may have been hidden by a hanging regex appearing sequentially before them.  The inputs that still hang are marked as not matching.


##How to run this program
Run the main file: *Main_MatrixBuilder*

You should see output like: 'completed i: 5/2871 nMatchStrings:400' as the program evaluates rows.

####Batch evaluation for stability

Evaluation of rows is done in batches, so that hanging regexes cannot absorb too much memory.  The BUILD_BATCH_SIZE constant in the main file dictates how many rows are evaluated per batch.

Rows of the matrix are being written to the output folder as the program runs.  After BUILD_BATCH_SIZE rows have been written, the program will stop by itself.  This is to free memory absorbed by various hanging regexes.  Run the main file again and you will notice that the index `i` picks up where it left off, as the program is aware of row files in the output folder.

When all rows are complete, another pass will validate the cells of the matrix that may have been interrupted, and then the program produces the *similarityGraph.abc* file.

Repeat this process until the program says it is done.

Running this program can take over half an hour on the given input.


##Differences from original version

The original version is in C#, and this version is a port to Java.  This version gets slightly different results in the *similarityGraph.abc* file, as the regex engine used to determine a match has some different rules.  The clusters formed from the resulting file, however, are mostly the same (some clusters are missing one pattern or so).

Being nearly identical, these clusters fall into the same categories as presented in the paper.  A diff of the *cluster.tsv* file (see image in this directory) shows that some of the larger clusters have changed their ordering, and a handful of small clusters are broken up differntly.

This version is remarkably faster, able to finish in about half an hour instead of a day or two.  For testing, a subset of the provided inputs can be created as a view of the original (see Test_MatrixBuilder).





