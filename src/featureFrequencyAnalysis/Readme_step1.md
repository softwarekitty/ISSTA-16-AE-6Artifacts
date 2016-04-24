######Follow the instructions in the Readme files for each step.


####Get the MWE Running First
Regexes are little programs, and running arbitrary programs on arbitrary inputs is a recipe for unpredictable behavior (i.e., it can hang).  The default setting is not to recreate the results of the paper, but to run the minimum working example.  Recreating the results of the paper *is possible* using these tools, but the behavioral analysis, in particular, may take some effort.


1. Place an input file in the `data/custom/step1_featureAnalysis' path.





4. Change the .
5. Run the main() function in the src/create_table/Main.java file. (Some error noise from the PCRE parser is normal - ignore this unless the program stops prematurely. The word, “Done.” will print to the console when the process is complete. This should take no more than 5 minutes on a standard laptop.)
6. Check the output folder - a latex table should be present displaying the feature frequencies for the studied feature set, featureStats.tex.






We provide the regexes that we mined as a database and a text file.  We also provide a small set of regex




A brief tutorial of how to generate the main results table in the paper (Table 4) that lists the frequency of observed features in the corpus. , rather, it performs a feature analysis on an existing database of regexes.

DATA SOURCE
The database of scraped regexes used in the paper is provided and used in the analysis: input/merged_report.db



USING YOUR OWN REGEX DATABASE
Note that the tool should work with any data set matching the schema. Here is information on how to recreate the database with your own corpus matching the schema:

The project builds the corpus using the following select statment:
"select pattern, uniqueSourceID from RegexCitationMerged where (flags=0 or flags like 'arg%' or flags=128 or flags='re.DEBUG') and pattern!='arg1';"

So a corpus can be build from any database conforming to the following schema:
Table1:(USED) uniqueSourceID: int, repoID: int, sourceJSON: text, fileHash: char(44), filePath: text, pattern: text, flags: int, regexFunction: int

uniqueSourceID is an int unique to a source project (USED)
repoID is the ID assigned by GitHub to a repo (not used)
sourceJSON contains data about the clone_url, etc (not used)
the fileHash is the SHA_224 hash of the original source file (not used)
the filePath is the path of the file relative to the repo root folder. (not used)
the pattern is the string used to define the regex (USED)
Flags 0,2,4,8,16,32,64 and 128 map to their internal Python values, as indicated in this source code. (USED: all utilizations ignored except those using flags: 0, like 'arg%', 128, 're.DEBUG')
regexFunction maps 0,1,2,3,4,5,6 to the 7 functions of Python's re module, as indicated in this source code. (not used)


Table2:(not used) nFiles: int, frequency: int

nFiles counts the number of files a project has (not used)
frequency indicates the number of times a project having nFiles was observed, (not used)
[note these special values:
nFiles=-1 (not used): number of files observed by sourcer (tested for Python content)
nFiles=-2 (not used): an error counter for refreshing a repository]

THIS REPO IS SIMPLIFIED
The version presented here is simplified, so that the source code is more clear - in the original and more complex version, the code automatically generated several minor tables, and gathered various statistics.
The source code for these functions is available on the original repository (https://github.com/softwarekitty/tour_de_source).
This version will accept a database as input and produce the main feature counting table as output, which should generalize to any source of valid patterns using a subset of PCRE features.
So flags and functions will also be ignored so that this version of the tool can generalize.

------
