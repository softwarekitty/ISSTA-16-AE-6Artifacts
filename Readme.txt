Artifacts prepared for ISSTA-16-AE-6

A brief tutorial of how to generate the main results table in the paper (Table 4) that lists the frequency of observed features in the corpus. This code does not mine GitHub for Python regexes, rather, it performs a feature analysis on an existing database of regexes. 

DATA SOURCE
The database of scraped regexes used in the paper is provided and used in the analysis: input/merged_report.db

ENVIRONMENT SETUP
1. Use the source to create a Java Project in Eclipse (use Java 1.7)
2. Add the five included jar files to the build path (commons-lang3-3.3.2.jar, antlr-3.5.2-complete.jar, sqlite-jdbc-3.7.2.jar, commons-io-2.4.jar, jython-standalone-2.5.4-rc1.jar)
3. In the `src/c/C.java’ file, change the `artifactPath' file to the root of this cloned repo.
4. Check the output folder - the only content is a ‘dummyFile’.
5. Run the main() function in the src/create_table/Main.java file. (Some error noise from the PCRE parser is normal - ignore this unless the program stops prematurely. The word, “Done.” will print to the console when the process is complete. This should take no more than 5 minutes on a standard laptop.)
6. Check the output folder - a latex table should be present displaying the feature frequencies for the studied feature set, featureStats.tex.

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


