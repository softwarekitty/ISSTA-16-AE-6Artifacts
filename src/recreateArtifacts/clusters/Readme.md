#Generate Clusters From the Similarity Graph

######**Input**: *similarityGraph.abc*

######**Output**: *clusters.tsv* and *patternClusterDump.tsv*

##Depends on mcl

This program requires that the mcl executable can be executed from Java.

On a mac, you can use:

```
>brew install homebrew/science/mcl
```

Or you can install from the [mcl website](http://micans.org/)

To verify that you have mcl installed in ```/usr/local/bin``` as required, run:

```
>which mcl
/urs/local/bin/mcl
```

##Program summary
The corpus is loaded (which takes a moment), and then mcl is run on the graph.  The output of mcl is written by mcl into the ```output``` folder, and then read by this program, which creates clusters in memory of the loaded RegexProjectSet objects.  These are used to create a dump that provides info for each cluster, like the patterns and total number of projects touched by a cluster.
