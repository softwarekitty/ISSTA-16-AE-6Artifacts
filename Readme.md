##Artifacts prepared for ISSTA-16-AE-6

The tools in this repo can be used to perform:

- Regex Feature Frequency Analysis (step 1)
- Regex Behavioral Clustering and Categorization (steps 2-5)

as published in the ISSTA-16 paper, [**Exploring Regular Expression Usage and Context in Python**](https://github.com/softwarekitty/ISSTA-16-AE-6Artifacts/pdf/ISSTA16_paper_108.pdf).

######This code does not mine GitHub for Python regexes.

-----

####Set up Eclipse, jars and home_path


1. Create a Java Project in Eclipse (use Java 1.7) using this repo as the project directory.
2. Add the four jar files in the `lib` directory to the build path (commons-lang3-3.3.2.jar, antlr-3.5.2-complete.jar, commons-io-2.4.jar, jython-standalone-2.5.4-rc1.jar).
3. In 'ISSTA_16_AE_6_config.json', change the value for the **home_path** key to specify the location of this repo.


####Input Format
A tab-separated-values (tsv) file with Python patterns and a list of project IDs, like:

```
"ab*c"  1,2
"(?:\\d+)\.(\\d+)"   2,3,5
u'[^a-zA-Z0-9_]' 1,5
'^[-\\w]+$' 2
'^\\s*\\n'  1,3,4
```

At this time, all patterns must be followed by a tab and at least one project.

No extra whitespace in input files, please.

The following inputs files are available in the `inputs` folder:

- 'minimalWorkingExample.txt' (two regexes)
- 'eightClusters.txt' (100 regexes)
- 'fullCorpus.txt' (13,597 regexes)

-----

##Getting started

Instructions for each step are in Readme files under the `src` directory.

We suggest you try using 'minimalWorkingExample.txt' before trying larger inputs.

####Workflow Directory Structure

```
_workflow
       |_step1_featureAnalysis
       |_step2_generateTestStrings
       |_step3_generateSimilarityMatrix
       |_step4_generateClusters
       |_step5_categorizeClusters
       |_step5_output
```
1. Place your input file into the `step1_featureAnalysis` folder.
2. Within 'ISSTA_16_AE_6_config.json', adjust the **input_filename** field to reflect your input.
3. Perform step 1, following the instructions found in 'Readme_step1.md'.
4. The output from running step 1 will be saved in `step2_generateTestStrings`.
5. Now perform step 2, and so on.

The output from one step is saved as the input for the next step (or later steps).
Steps may be repeated to tune or troubleshoot.  Intermediate data may be inspected.
A variety of configuration details are tunable using fields in 'ISSTA_16_AE_6_config.json'.
For the three provided inputs, you can check your results or skip a step using content from the `completed_workflows` folder.

######Step 2 requires virtualbox. (brew install Caskroom/cask/virtualbox)
######Step 3 may hang and must be run incrementally for large inputs.
######Step 4 requires mcl. (brew install homebrew/science/mcl)
######Step 5 is interactive - make your own categories using 'ISSTA_16_AE_6_config.json'

_____


##F.A.Q.
####why Python?
It was not an arbitrary choice, but it was not the only option, either.  JavaScript would have been a reasonable alternative using our rationalle.  Consider first that regular expression languages have different feature sets, and doing this analysis takes some time.  In order to maximize the impact of the research, we wanted a language that *includes* common features (features shared by other languages) and *excludes* rare features (features not shared by many other languaes).  Python fits this description, as can be seen by looking at [a comparison of language feature sets](https://github.com/softwarekitty/ISSTA-16-AE-6Artifacts/pdf/languageTables.pdf) from [my thesis](https://github.com/softwarekitty/ISSTA-16-AE-6Artifacts/pdf/thesis.pdf).

####where is the mining code?
It can be found in the [tour_de_source](https://github.com/softwarekitty/tour_de_source) repo, but it is not groomed for public consumption, and is probably not an optimal mining solution.

####why not use formal tools for behavioral analysis?
Because the tools we found cannot handle regexes using certain common features, like '$'.

####how can I submit an error report, bug report or pull requrest?
Please open an issue if you find any problems or want to be a contributor.


