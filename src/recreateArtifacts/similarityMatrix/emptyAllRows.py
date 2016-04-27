import sys, os

# dangerous!  erases lots of stuff!
# expects to be run in the directory containing AllRows
# only run if you really want to empty all the row directory contents

# cite: http://stackoverflow.com/questions/2212643/python-recursive-folder-read
allRowsAbsPath = os.path.join(sys.path[0],"allRows")
for root, subdirs, files in os.walk(allRowsAbsPath):
    print("starting walk")
    for subdir in subdirs:
        thisSubdirAbsPath = os.path.join(allRowsAbsPath,subdir)
        print("in subdir: "+str(thisSubdirAbsPath))
        for root, subdirs, files  in os.walk(thisSubdirAbsPath):
            for filename in files:

                # adding this little safety feature: file must start with 'row_'
                if(filename.startswith("row_")):
                    print("removing file: "+str(filename))
                    os.remove(os.path.join(thisSubdirAbsPath, filename))
