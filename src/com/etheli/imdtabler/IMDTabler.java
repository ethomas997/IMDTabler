//IMDTabler.java:  Utility for generating tables of frequency values for
//                 intermodulation distortion.
//
//  3/19/2017 -- [ET]
//  1/19/2019 -- [ET]  Version 1.3:  Added command-line and output-to-console options.
//

package com.etheli.imdtabler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class IMDTabler is a utility for generating tables of frequency values
 * for intermodulation distortion.
 */
public class IMDTabler
{
  public static final String VERSION_STR = "1.3";
  public static final int MIN_DISP_FREQ = 5100;
  public static final int MAX_DISP_FREQ = 6099;
  public static final int MIN_FREQ_SEP = 35; //min freq separation for warning
  public static final String OUT_FILE_NAME = "IMDTableFile.html";
  public static final int RATING_MAX_VALUE = 100; //top value for ratings
  public static final int RATING_DIFF_LIMIT = 35; //anything higher is "good"
                        //minimum difference for colored cell background:
  public static final int NO_CLR_DIFF = RATING_DIFF_LIMIT;
  
  /**
   * Generates an IMDTable for the given set of frequencies and returns
   * an HTML-string representation of the table.
   * @param inStr source string containing space-separated list of numbers.
   * @return An HTML-string representation of a new IMDTable object.
   */
  public static String getIMDStringForFreqSet(String inStr)
  {
    return getIMDTableForFreqSet(stringToIntArr(inStr)).toString();
  }
  
  /**
   * Generates an IMDTable for the given set of frequencies and returns
   * an HTML-string representation of the table.
   * @param inStrArr source string array containing numeric strings.
   * @return An HTML-string representation of a new IMDTable object.
   */
  public static String getIMDStringForFreqSet(String [] inStrArr)
  {
    return getIMDTableForFreqSet(strArrToIntArr(inStrArr)).toString();
  }
  
  /**
   * Generates an IMDTable for the given set of frequencies and returns
   * an HTML-string representation of the table.
   * @param selFreqSetArr array of frequencies selected for testing.
   * @return An HTML-string representation of a new IMDTable object.
   */
  public static String getIMDStringForFreqSet(int [] selFreqSetArr)
  {
    return getIMDTableForFreqSet(selFreqSetArr).toString();
  }
  
  /**
   * Generates an IMDTable for the given set of frequencies.
   * @param inStr source string containing space-separated list of numbers.
   * @return A new IMDTable object.
   */
  public static IMDTable getIMDTableForFreqSet(String inStr)
  {
    return new IMDTable(stringToIntArr(inStr));
  }
  
  /**
   * Generates an IMDTable for the given set of frequencies.
   * @param selFreqSetArr array of frequencies selected for testing.
   * @return A new IMDTable object.
   */
  public static IMDTable getIMDTableForFreqSet(int [] selFreqSetArr)
  {
    return new IMDTable(selFreqSetArr);
  }

  /**
   * Program entry point (if application).
   * @param args array of command-line arguments.
   */
  public static void main(String [] args)
  {
    final SimpleArgsParser sArgsParser = new SimpleArgsParser(args);

    if (sArgsParser.isSwitch("v") || sArgsParser.isSwitch("ver") ||
        sArgsParser.isSwitch("version"))
    {
      System.out.println("IMDTabler Version " + VERSION_STR);
      System.exit(0);
      return;
    }

    if (sArgsParser.itemArgs.length <= 0 || sArgsParser.isSwitch("h") ||
        sArgsParser.isSwitch("help"))
    {
      System.out.println("IMDTabler Version " + VERSION_STR);
      System.out.println("Usage:  IMDTabler [-r] [-t] frequencies");
      System.out.println("  -r :  Output table rating to console");
      System.out.println("  -t :  Output table HTML to console");
      System.out.println("  -o :  Output table text to console");
      System.out.println("  -p :  Output table (with info lines) to console");
      System.exit(0);
      return;
    }

    final IMDTable imdTableObj;
    try
    {
      imdTableObj = getIMDTableForFreqSet(strArrToIntArr(sArgsParser.itemArgs));
    }
    catch(IllegalArgumentException ex)
    {
      System.err.println("Numeric-format error:  " + ex.getMessage());
      System.exit(1);
      return;
    }
    
    if (sArgsParser.isSwitch("r"))
    {
      System.out.println(imdTableObj.tableRatingValue);
    }
    else if (sArgsParser.isSwitch("t"))
    {
      System.out.println(imdTableObj.toString());
    }
    else if (sArgsParser.isSwitch("o") || sArgsParser.isSwitch("p"))
    {
      System.out.println("IMDTabler Version " + VERSION_STR);
      IMDTableCell [] tableCellArr;
      int freqVal;
      for(int i=0; i<imdTableObj.imdTableRowArr.length; ++i)
      {
        tableCellArr = imdTableObj.imdTableRowArr[i].tableCellArr;
        int j = 0;
        if (tableCellArr.length > 0)
        {
          while (true)
          {
            freqVal = tableCellArr[j].cellFreqValue;
            if (j != i && freqVal >= MIN_DISP_FREQ && freqVal <= MAX_DISP_FREQ)
              System.out.print(freqVal);
            else
              System.out.print("----");
            if (++j >= tableCellArr.length)
              break;
            System.out.print(' ');
          }
          System.out.println();
        }
      }
      System.out.println(imdTableObj.tableRatingValue);
      if (sArgsParser.isSwitch("p"))
      {
        for(int i=0; i<imdTableObj.imdTableRowArr.length; ++i)
        {
          tableCellArr = imdTableObj.imdTableRowArr[i].tableCellArr;
          for(int j=0; j<tableCellArr.length; ++j)
          {
            if(tableCellArr[j].showCellInfoFlag())
              System.out.println(tableCellArr[j].getCellInfoStr());
          }
        }
      }
    }
    else
    {
      try
      {
        final BufferedWriter wtrObj =
                            new BufferedWriter(new FileWriter(OUT_FILE_NAME));
        wtrObj.write(imdTableObj.toString());
        wtrObj.close();
      }
      catch(IOException ex)
      {
        ex.printStackTrace();
      }
      (new LaunchBrowser()).showApplicationURL(OUT_FILE_NAME);
    }
  }
  
  /**
   * Converts array of strings to array of integers.
   * @param strArr source array of strings.
   * @return new array of integer values.
   * @throws NumberFormatException if any conversion errors.
   */
  public static int [] strArrToIntArr(String [] strArr)
      throws NumberFormatException
  {
    final int strArrLen = (strArr != null) ? strArr.length : 0;
    final int [] intArr = new int [strArrLen];
    for(int i=0; i<strArrLen; ++i)
      intArr[i] = Integer.parseInt(strArr[i].trim());
    return intArr;
  }
  
  /**
   * Converts string to array of integers.
   * @param inStr source string containing space-separated
   * (or comma-separated) list of numbers.
   * @return new array of integer values.
   * @throws NumberFormatException if any conversion errors.
   */
  public static int [] stringToIntArr(String inStr)
      throws NumberFormatException
  {
    if(inStr != null && inStr.trim().length() > 0)
      return strArrToIntArr(inStr.split("[ ,\t]+"));
    return new int[0];
  }
  
  /**
   * Returns the minimum-separation value for the given list of frequency
   * values.
   * @param intArr list of frequency values.
   * @param isSortedFlag true if the list is in ascending sort order;
   * false if not.
   * @return The minimum-separation value, or MAX_DISP_FREQ*2 if none found.
   */
  public static int getMinFreqSeparation(int [] intArr, boolean isSortedFlag)
  {
    int minSepVal = MAX_DISP_FREQ * 2;
    if(!isSortedFlag)
    {
      intArr = Arrays.copyOf(intArr,intArr.length);
      Arrays.sort(intArr);
    }
    int val;
    for(int i=1; i<intArr.length; ++i)
    {
      if((val=intArr[i]-intArr[i-1]) < minSepVal)
        minSepVal = val;
    }
    return minSepVal;
  }


  /**
   * Class IMDTableCell manages a single cell in the table.
   */
  public static class IMDTableCell
  {
    public final int cellFreqValue;
    public final int rowIdx, colIdx;
    public int nearFreqIndex;
    public int nearFreqValue;
    public int cellDiffValue;
    public String cellInfoStr;
    private int [] selFreqSetArr;

    /**
     * Defines a single cell in the table.
     * @param selFreqSetArr array of frequencies selected for testing.
     * @param rowIdx row index for cell.
     * @param colIdx column index for cell.
     */
    public IMDTableCell(int [] selFreqSetArr, int rowIdx, int colIdx)
    {
      this.selFreqSetArr = selFreqSetArr;
      this.rowIdx = rowIdx;
      this.colIdx = colIdx;
      cellFreqValue = selFreqSetArr[rowIdx] * 2 - selFreqSetArr[colIdx];
      nearFreqIndex = findNearestFreqVal(cellFreqValue);
      nearFreqValue = selFreqSetArr[nearFreqIndex];
      cellDiffValue = Math.abs(cellFreqValue - nearFreqValue);
      cellInfoStr = cellFreqValue + " is " + cellDiffValue +
                                           "MHz away from " + nearFreqValue;
    }

    /**
     * Returns true if the cell should have a background color.
     * @return true if the cell should have a background color.
     */
    public boolean isCellColored()
    {
      return (cellDiffValue < NO_CLR_DIFF);
    }
    
    /**
     * Returns the background-color string for the table cell.
     * @return The background-color string for the table cell, or null
     * if none (white).
     */
    public String getCellBgColorStr()
    {
      if(isCellColored())
      {  //convert difference to color value (lower is more red)
        final int clrVal = 255 - (NO_CLR_DIFF - cellDiffValue) * 150 / NO_CLR_DIFF;
        final String str = (clrVal<=9?"0":"") + Integer.toString(clrVal,16);
        return "#FF" + str + str;
      }
      return null;
    }

    /**
     * Returns true if the cell value is within the display range.
     * @return true if the cell value is within the display range.
     */
    public boolean isCellValueInRange()
    {
      return (cellFreqValue >= MIN_DISP_FREQ &&
                                            cellFreqValue <= MAX_DISP_FREQ);
    }

    /**
     * Returns true if the cell value should be displayed in the table.
     * @return true if the cell value should be displayed.
     */
    public boolean isCellValueDisplayed()
    {
      return (rowIdx != colIdx && isCellValueInRange());
    }

    /**
     * Returns the informational string for the cell.
     * @return The informational string for the cell.
     */
    public String getCellInfoStr()
    {
      return cellInfoStr;
    }

    /**
     * Returns true if the informational string for this cell should be
     * displayed below the table.
     * @return true if the information string should be displayed.
     */
    public boolean showCellInfoFlag()
    {
      return (rowIdx != colIdx && isCellColored() && isCellValueInRange());
    }

    /**
     * Returns true if the cell should be included when checking difference
     * values for ratings.
     * @return true if the cell should be included when checking difference
     * values for ratings.
     */
    public boolean checkCellDiffFlag()
    {
      return (rowIdx != colIdx && isCellValueInRange());
    }

    /**
     * Returns a string representation of the cell.
     * @return A string representation of the cell.
     */
    public String toString()
    {
      if(rowIdx != colIdx)
      {
        return isCellValueInRange() ? genHtmlValueCellStr(
                      cellFreqValue,false,cellInfoStr,getCellBgColorStr()) :
                                         genHtmlBlankCellStr(cellFreqValue);
      }
      else
        return genHtmlBlankCellStr(0);
    }

    /**
     * Returns the HTML-cell string representation for the value.
     * @param cellVal integer value for cell.
     * @param hdrFlag true for bold/italic flags for header value.
     * @param titleStr value for title/tooltip string, or null for none.
     * @param bgClrStr value for cell background color, or null for none.
     * @return The HTML-cell string representation for the value.
     */
    public static String genHtmlValueCellStr(int cellVal, boolean hdrFlag,
                                           String titleStr, String bgClrStr)
    {
      return "<td align=\"center\"" +
                 (titleStr != null ? (" title=\"" + titleStr + "\"") : "") +
               (bgClrStr != null ? (" bgcolor=\"" + bgClrStr + "\"") : "") +
                                               ">" + (hdrFlag?"<b><i>":"") +
              Integer.toString(cellVal) + (hdrFlag?"</i></b>":"") + "</td>";
    }

    /**
     * Returns the HTML-cell string representation for the value.
     * @param cellVal integer value for cell.
     * @param hdrFlag true for bold/italic flags for header value.
     * @return The HTML-cell string representation for the value.
     */
    public static String genHtmlValueCellStr(int cellVal, boolean hdrFlag)
    {
      return genHtmlValueCellStr(cellVal,hdrFlag,null,null);
    }

    /**
     * Returns the HTML-cell string representation for the value.
     * @param cellVal integer value for cell.
     * @return The HTML-cell string representation for the value.
     */
    public static String genHtmlValueCellStr(int cellVal)
    {
      return genHtmlValueCellStr(cellVal,false);
    }

    /**
     * Returns the HTML-string representation for a blank cell.
     * @param cellVal integer value for cell.
     * @return The HTML-string representation for a blank cell.
     */
    public static String genHtmlBlankCellStr(int cellVal)
    {
      return "<td align=\"center\"" +
                      (cellVal != 0 ? (" title=\"" + cellVal + "\"") : "") +
                                                                   "></td>";
    }
    
    /**
     * Finds the frequency value nearest the given frequency.
     * @param freqVal frequency value to match.
     * @return The index for the nearest frequency.
     */
    public int findNearestFreqVal(int freqVal)
    {
      int foundIdx = 0;
      int d, diffVal = MAX_DISP_FREQ * 2;
      for(int i=0; i<selFreqSetArr.length; ++i)
      {
        if((d=Math.abs(freqVal-selFreqSetArr[i])) < diffVal)
        {
          diffVal = d;
          foundIdx = i;
        }
      }
      return foundIdx;
    }
  }


  /**
   * Class IMDTableRow manages a row in the table.
   */
  public static class IMDTableRow
  {
    public final int rowIdx;
    public final int rowRatingTotal;
    private int [] selFreqSetArr;
    private final IMDTableCell [] tableCellArr;
    
    /**
     * Defines a row in the table.
     * @param selFreqSetArr array of frequencies selected for testing.
     * @param rowIdx index value for row.
     */
    public IMDTableRow(int [] selFreqSetArr, int rowIdx)
    {
      this.selFreqSetArr = selFreqSetArr;
      this.rowIdx = rowIdx;
      tableCellArr = new IMDTableCell[selFreqSetArr.length];
      for(int i=0; i<tableCellArr.length; ++i)
        tableCellArr[i] = new IMDTableCell(selFreqSetArr, rowIdx, i);

         //calculate "rating total" for row (cells with low
         // frequency-difference values will increase total):
      int val, rrTotal = 0;
      IMDTableCell imdTableCellObj;
      for(int i=0; i<tableCellArr.length; ++i)
      {
        if((imdTableCellObj=tableCellArr[i]).checkCellDiffFlag() &&
                          imdTableCellObj.cellDiffValue < RATING_DIFF_LIMIT)
        {
          val = RATING_DIFF_LIMIT - imdTableCellObj.cellDiffValue;
          rrTotal += val * val;   //make low diff values hurt rating
        }
      }
      rowRatingTotal = rrTotal;
    }
    
    /**
     * Returns the information strings for the cells in the row.
     * @return The information strings for the cells in the row.
     */
    public String getCellInfoStrs()
    {
      final StringBuffer buff = new StringBuffer();
      for(int i=0; i<tableCellArr.length; ++i)
      {
        if(tableCellArr[i].showCellInfoFlag())
          buff.append(tableCellArr[i].getCellInfoStr() + "<br>\n");
      }
      return buff.toString();
    }
    
    /**
     * Returns the array of 'IMDTableCell' objects for this table row.
     * @return The array of 'IMDTableCell' objects for this table row.
     */
    public IMDTableCell [] getTableCellArray()
    {
      return tableCellArr;
    }

    /**
     * Returns a string representation of the row.
     * @return A string representation of the row.
     */
    public String toString()
    {
      final StringBuffer buff = new StringBuffer("    <tr>\n      " +
              IMDTableCell.genHtmlValueCellStr(selFreqSetArr[rowIdx],true) +
                                                                      "\n");
      for(int i=0; i<tableCellArr.length; ++i)
        buff.append("      " + tableCellArr[i] + "\n");
      buff.append("    </tr>\n");
      return buff.toString();
    }

    /**
     * Returns a header row.
     * @param intArr values for the header row.
     * @return A header row.
     */
    public static String intArrToHeaderStr(int [] intArr)
    {
      final StringBuffer buff = new StringBuffer("    <tr>\n      " +
                                IMDTableCell.genHtmlBlankCellStr(0) + "\n");
      for(int i=0; i<intArr.length; ++i)
      {
        buff.append("      " +
                   IMDTableCell.genHtmlValueCellStr(intArr[i],true) + "\n");
      }
      buff.append("    </tr>\n");
      return buff.toString();
    }
  }


  /**
   * Class IMDTable manages a table.
   */
  public static class IMDTable
  {
    public final int tableRatingValue;
    public final int minFreqSeparationValue;
    private final int [] selFreqSetArr;
    private final IMDTableRow [] imdTableRowArr;
    
    /**
     * Defines a table.
     * @param selFreqSetArr array of frequencies selected for testing.
     */
    public IMDTable(int [] selFreqSetArr)
    {
      this.selFreqSetArr = selFreqSetArr;
      minFreqSeparationValue = getMinFreqSeparation(selFreqSetArr,false);
      imdTableRowArr = new IMDTableRow[selFreqSetArr.length];
      int trTtotal = 0;
      IMDTableRow imdTableRowObj;
      for(int rowIdx=0; rowIdx<imdTableRowArr.length; ++rowIdx)
      {
        imdTableRowObj = imdTableRowArr[rowIdx] =
                                     new IMDTableRow(selFreqSetArr, rowIdx);
        trTtotal += imdTableRowObj.rowRatingTotal;    //sum ratings
      }
         //scale down total by freq count and a bit more
         // and subtract from max to get rating value:
      tableRatingValue = RATING_MAX_VALUE - trTtotal/5/selFreqSetArr.length;
    }

    /**
     * Returns a display string containing the 'selFreqSetArr' values.
     * @return A display string containing the 'selFreqSetArr' values.
     */
    public String getSelFreqSetDispStr()
    {
      final StringBuffer buff = new StringBuffer();
      if(selFreqSetArr.length > 0)
      {
        int i = 0;
        while(true)
        {
          buff.append(Integer.toString(selFreqSetArr[i]));
          if(++i >= selFreqSetArr.length)
            break;
          buff.append(' ');
        }
      }
      return buff.toString();
    }
    
    /**
     * Determines if the minimum separation for the frequencies selected for
     * testing is too small (less than MIN_FREQ_SEP).
     * @return true if the minimum separation for the frequencies selected
     * for testing is too small.
     */
    public boolean isMinFreqSeparationTooSmall()
    {
      return (minFreqSeparationValue < MIN_FREQ_SEP);
    }
    
    /**
     * Returns the warning message if the minimum separation for the
     * frequencies selected for testing is too small (less than
     * MIN_FREQ_SEP).
     * @return The warning message if the minimum separation for the
     * frequencies selected for testing is too small; else null.
     */
    public String getMinFreqSepMsgStr()
    {
      return isMinFreqSeparationTooSmall() ?
               ("Warning:  Minimum separation for entered frequencies is " +
                                     minFreqSeparationValue + "MHz") : null;
    }
    
    /**
     * Returns a string representation of the table.
     * @return A string representation of the table.
     */
    public String toString()
    {
      final StringBuffer buff = new StringBuffer();
      if(isMinFreqSeparationTooSmall())
        buff.append("<b>" + getMinFreqSepMsgStr() + "</b><br><br>");
      buff.append("<table border=\"1\" cellpadding=\"8\">\n  <tbody>\n" +
                              IMDTableRow.intArrToHeaderStr(selFreqSetArr));
      for(int i=0; i<imdTableRowArr.length; ++i)
        buff.append(imdTableRowArr[i]);
      buff.append("  </tbody>\n</table>\n<br>\n");
      buff.append("IMD rating (" + RATING_MAX_VALUE + "=best): " +
                                             tableRatingValue + "<br><br>");
      for(int i=0; i<imdTableRowArr.length; ++i)
        buff.append(imdTableRowArr[i].getCellInfoStrs());
      return buff.toString();
    }
    
    /**
     * Returns the array of frequencies selected for testing.
     * @return The array of frequencies selected for testing.
     */
    public int [] getSelFreqSetArr()
    {
      return selFreqSetArr;
    }

    /**
     * Returns the number of rows in the table.  This is also the number
     * of columns in the table (always same value).
     * @return The number of rows in the table.
     */
    public int getTableRowCount()
    {
      return imdTableRowArr.length;
    }
    
    /**
     * Returns the array of 'IMDTableRow' objects for the table.
     * @return The array of 'IMDTableRow' objects for the table.
     */
    public IMDTableRow [] getTableRowArray()
    {
      return imdTableRowArr;
    }
    
    /**
     * Returns a (one-dimensional) array containing all the 'IMDTableCell'
     * objects in the table.
     * @return A new array of 'IMDTableCell' objects.
     */
    public IMDTableCell [] getTableCellObjsArray()
    {
      final ArrayList<IMDTableCell> cellList = new ArrayList<IMDTableCell>();
      IMDTableCell [] cellArr;
      for(int row=0; row<imdTableRowArr.length; ++row)
      {  //for each row
        cellArr = imdTableRowArr[row].getTableCellArray();
        for(int col=0; col<cellArr.length; ++col)  //for each column
          cellList.add(cellArr[col]);
      }
      final IMDTableCell [] outArr = new IMDTableCell[cellList.size()];
      return cellList.toArray(outArr);
    }
    
    /**
     * Returns a (one-dimensional) array containing all the values
     * in the table as strings.
     * @return A new String array.
     */
    public String [] getTableCellStrsArray()
    {
      final ArrayList<String> strList = new ArrayList<String>();
      IMDTableCell [] cellArr;
      for(int row=0; row<imdTableRowArr.length; ++row)
      {  //for each row
        cellArr = imdTableRowArr[row].getTableCellArray();
        for(int col=0; col<cellArr.length; ++col)  //for each column
        {
          if(row != col && cellArr[col].isCellValueInRange())
            strList.add(Integer.toString(cellArr[col].cellFreqValue));
          else
            strList.add("");
        }
      }
      final String [] strArr = new String[strList.size()];
      return strList.toArray(strArr);
    }
  }
  
  
  /**
   * Class SimpleArgsParser breaks the given array of command-line arguments
   * into separate arrays of "switch" and "non-switch" arguments.
   */
  public static class SimpleArgsParser
  {
    public final ArrayList<String> switchArgsList = new ArrayList<String>();
    public final String [] itemArgs;
    
    /**
     * Processes the given array of command-line arguments into separate arrays
     * of "switch" and "non-switch" arguments.
     * @param inArgsArr array of command-line arguments.
     */
    public SimpleArgsParser(String [] inArgsArr)
    {
      ArrayList<String> itemArgsList = new ArrayList<String>();
      for (String argStr : inArgsArr)
      {
        if (argStr.startsWith("--"))
          switchArgsList.add(argStr.substring(2));  // remove leading switch chars
        else if (argStr.startsWith("-") || argStr.startsWith("/"))
          switchArgsList.add(argStr.substring(1));  // remove leading switch char
        else
          itemArgsList.add(argStr);
      }
      itemArgs = itemArgsList.toArray(new String[0]);
    }

    /**
     * Tests if the given switch argument was specified.
     * @param switchStr switch argument.
     * @return true if the given switch argument was specified.
     */
    public boolean isSwitch(String switchStr)
    {
      for (String argStr : switchArgsList)
      {
        if (switchStr.equalsIgnoreCase(argStr))
          return true;
      }
      return false;
    }
  }
}
