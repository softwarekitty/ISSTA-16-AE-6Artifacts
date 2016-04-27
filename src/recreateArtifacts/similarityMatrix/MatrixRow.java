package recreateArtifacts.similarityMatrix;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.io.IOUtil;

public class MatrixRow
{

    private int rowIndex;
    private double[] values;
    private int nCols;

    public MatrixRow(int rowIndex,double[] values, int nCols)
    {
        this.rowIndex = rowIndex;
        this.values = values;
        this.nCols = nCols;
    }

    public MatrixRow(String rowFileBase, int rowIndex, int nCols) throws IOException
    {
        this.nCols = nCols;
        this.rowIndex = rowIndex;
        this.values = computeValuesFromFileContents(rowFileBase);
    }

    public double[] getValues()
    {
        return values;
    }

    public void setValues(double[] vals)
    {
        this.values = vals;
    }

    private double[] computeValuesFromFileContents(String rowFileBase) throws IOException
    {
        double[] valuesFromFile = new double[nCols];
        for (int i = 0; i < nCols; i++)
        {
            valuesFromFile[i] = SimilarityMatrixBuilder.verifiedTimeoutFlag;
        }
        String rowFilePath = RowUtil.getRowFilePath(rowFileBase, nCols, rowIndex);        
        List<String> lines = IOUtil.readLines(rowFilePath);
        for(String line : lines){
            if (line.startsWith("belowMinimumList"))
            {
                setBelowMinValues(line,valuesFromFile);
            }
            else if (line.startsWith("similarityValues:"))
            {
                setSimilarityValues(line,valuesFromFile);
            }
            //do nothing for other rows - these cols can use the verifiedTimeoutFlag they already have
            //we will set these to belowMinimum if they exceed nErrors	
        }
        return valuesFromFile;
    }

    private void setSimilarityValues(String line, double[] vals)
    {
        String similarityValueList = removeBrackets(line);
        Pattern parens = Pattern.compile("(\\((.*?)\\))");
        Matcher m = parens.matcher(similarityValueList);
        while(m.find()){
            String pair = m.group(2);
            String[] splitPair = pair.split(":");
            int colIndex = Integer.parseInt(splitPair[0]);
            double colValue = Double.parseDouble(splitPair[1]);
            vals[colIndex] = colValue;
        }
    }

    private void setBelowMinValues(String line, double[] vals)
    {
        String[] indices = removeBrackets(line).split(",");

        //some of these are empty
        for (String indexString : indices)
        {
            if (indexString.length() > 0)
            {
                int colIndex = Integer.parseInt(indexString);
                vals[colIndex] = SimilarityMatrixBuilder.belowMinFlag;
            }
        }
    }

    private String removeBrackets(String line)
    {
        int startIndex = line.indexOf("[");
        int endIndex = line.indexOf("]");
        return line.substring(startIndex + 1, (endIndex-startIndex-1));
    }

    public void writeRowToFile(String rowFileBase, double minSimilarity)
    {
		DecimalFormat df5 = new DecimalFormat("0.0000");

        boolean[] notFirstFlags = new boolean[5];

        StringBuilder initializedList = new StringBuilder();
        StringBuilder incompleteList = new StringBuilder();
        StringBuilder cancelledList = new StringBuilder();
        StringBuilder belowMinimumList = new StringBuilder();
        StringBuilder similarityValues = new StringBuilder();

        initializedList.append("initializedList: [");
        incompleteList.append("incompleteList: [");
        cancelledList.append("cancelledList: [");
        belowMinimumList.append("belowMinimumList: [");
        similarityValues.append("similarityValues: [");


        for (int j = 0; j < values.length; j++)
        {
            double value_j = values[j];
            if (value_j == SimilarityMatrixBuilder.initializedFlag)
            {
                if (notFirstFlags[0])
                {
                    initializedList.append(",");
                }
                initializedList.append(j);
            }
            else if (value_j == SimilarityMatrixBuilder.incompleteFlag)
            {
                if (notFirstFlags[1])
                {
                    incompleteList.append(",");
                }
                incompleteList.append(j);
            }
            else if (value_j == SimilarityMatrixBuilder.cancelledFlag)
            {
                if (notFirstFlags[2])
                {
                    cancelledList.append(",");
                }
                cancelledList.append(j);
            }
            else if (value_j < minSimilarity)
            {
                if (notFirstFlags[3])
                {
                    belowMinimumList.append(",");
                }
                belowMinimumList.append(j);
            }
            else
            {
                if (notFirstFlags[4])
                {
                	similarityValues.append(",");
                }
                similarityValues.append("(");
                similarityValues.append(j);
                similarityValues.append(":");
                similarityValues.append(df5.format(value_j));
                similarityValues.append(")");
            }

            //set not first to true - this is for comma neatness
            if (!notFirstFlags[0])
            {
                for (int x = 0; x < notFirstFlags.length; x++)
                {
                    notFirstFlags[x] = true;
                }
            }
        }
        initializedList.append("]\n");
        incompleteList.append("]\n");
        cancelledList.append("]\n");
        belowMinimumList.append("]\n");
        similarityValues.append("]\n");

        StringBuilder rowFileContent = new StringBuilder();
        rowFileContent.append(initializedList.toString());
        rowFileContent.append(incompleteList.toString());
        rowFileContent.append(cancelledList.toString());
        rowFileContent.append(belowMinimumList.toString());
        rowFileContent.append(similarityValues.toString());

        String rowFilePath = RowUtil.getRowFilePath(rowFileBase, nCols, rowIndex);
        IOUtil.createAndWrite(new File(rowFilePath), rowFileContent.toString());
    }

}