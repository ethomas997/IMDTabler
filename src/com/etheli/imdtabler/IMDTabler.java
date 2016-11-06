//IMDTabler.java:  Utility for generating tables of frequency values for
//                 intermodulation distortion.
//
//  11/4/2016 -- [ET]
//

package com.etheli.imdtabler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

/**
 * Class IMDTabler is a utility for generating tables of frequency values
 * for intermodulation distortion.
 */
public class IMDTabler
{
  public static final String VERSION_STR = "1.1";
  public static final int MIN_DISP_FREQ = 5100;
  public static final int MAX_DISP_FREQ = 6099;
  public static final int MIN_FREQ_SEP = 35; //min freq separation for warning
  public static final String OUT_FILE_NAME = "IMDTableFile.html";
  public static final int RATING_MAX_VALUE = 100; //top value for ratings
  public static final int RATING_DIFF_LIMIT = 35; //anything higher is "good"
                        //minimum difference for colored cell background:
  public static final int NO_CLR_DIFF = RATING_DIFF_LIMIT;
  
  /**
   * Generates an IMDTable for the given set of frequencies.
   * @param inStr source string containing space-separated list of numbers.
   * @return A new IMDTable object.
   */
  public static String getIMDStringForFreqSet(String inStr)
  {
    return getIMDTableForFreqSet(stringToIntArr(inStr)).toString();
  }
  
  /**
   * Generates an IMDTable for the given set of frequencies.
   * @param inStrArr source string array containing numeric strings.
   * @return A new IMDTable object.
   */
  public static String getIMDStringForFreqSet(String [] inStrArr)
  {
    return getIMDTableForFreqSet(strArrToIntArr(inStrArr)).toString();
  }
  
  /**
   * Generates an IMDTable for the given set of frequencies.
   * @param selFreqSetArr array of frequencies selected for testing.
   * @return A new IMDTable object.
   */
  public static String getIMDStringForFreqSet(int [] selFreqSetArr)
  {
    return getIMDTableForFreqSet(selFreqSetArr).toString();
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
    final String outStr = getIMDStringForFreqSet(args);
//    System.out.println(imdTableObj);
    try
    {
      final BufferedWriter wtrObj =
                          new BufferedWriter(new FileWriter(OUT_FILE_NAME));
      wtrObj.write(outStr);
      wtrObj.close();
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }

    (new LaunchBrowser()).showApplicationURL(OUT_FILE_NAME);
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
     * Returns true if the cell value is within the display range.
     * @return true if the cell value is within the display range.
     */
    public boolean isCellValueInRange()
    {
      return (cellFreqValue >= MIN_DISP_FREQ &&
                                            cellFreqValue <= MAX_DISP_FREQ);
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
      return rowIdx != colIdx && isCellColored() && isCellValueInRange();
    }
    
    /**
     * Returns true if the cell should be included when checking difference
     * values for ratings.
     * @return true if the cell should be included when checking difference
     * values for ratings.
     */
    public boolean checkCellDiffFlag()
    {
      return rowIdx != colIdx && isCellValueInRange();
    }

    /**
     * Returns a string representation of the cell.
     * @return A string representation of the cell.
     */
    public String toString()
    {
      final String bgClrStr;
      if(rowIdx != colIdx)
      {
        if(isCellColored())
        {  //convert difference to color value (lower is more red)
          final int clrVal = 255 - (NO_CLR_DIFF - cellDiffValue) * 150 / NO_CLR_DIFF;
          final String str = (clrVal<=9?"0":"") + Integer.toString(clrVal,16);
          bgClrStr = "#FF" + str + str;
        }
        else
          bgClrStr = null;
        return isCellValueInRange() ?
                genHtmlValueCellStr(cellFreqValue,false,cellInfoStr,bgClrStr) :
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
    public int tableRatingValue;
    private int [] selFreqSetArr;
    private IMDTableRow [] imdTableRowArr;
    
    /**
     * Defines a table.
     * @param selFreqSetArr array of frequencies selected for testing.
     */
    public IMDTable(int [] selFreqSetArr)
    {
      this.selFreqSetArr = selFreqSetArr;
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
     * Returns a string representation of the table.
     * @return A string representation of the table.
     */
    public String toString()
    {
      final StringBuffer buff = new StringBuffer();
      final int minSepVal;
      if((minSepVal=getMinFreqSeparation(selFreqSetArr,false)) < MIN_FREQ_SEP)
      {
        buff.append("<b>Warning:  Minimum separation for entered " +
                         "frequencies is " + minSepVal + "MHz</b><br><br>");
      }
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
  }
}
