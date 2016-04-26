#Mining data sources

######Notice the file: projectInfo.txt

To aide the validation of the mining technique, various information about the 1645 projects containing regexes that provided all the data for analysis is extracted from the database in this package.

We provide the internal ID (useful for matching up patterns in other files to their projects), the clone url (useful for cloning the project for study), the last commit sha (necessary because reproducing the results would be sensitive to new commits), and the GitHub repoID, which may be used in their API for whatever purpose.

The code in this source folder was only written to extract that data once, but can be re-run if desired.