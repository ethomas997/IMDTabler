//FreqSetGen.java:  Utility for generating sets of frequency values
//                  ranked via intermodulation distortion.
//
//  11/4/2016 -- [ET]
//

package com.etheli.imdtabler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.TreeSet;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * Class FreqSetGen is a utility for generating sets of frequency values
 * ranked via intermodulation distortion.
 */
public class FreqSetGen
{
  public static final String VERSION_STR = "1.21";
  public static final String PROG_SHORT_TITLESTR = "ET's FreqSetGen";
  public static final String PROG_LONG_TITLESTR = "Frequency Set Generator";
  public static final String SITE_HOME_URLSTR = "http://etserv.etheli.com";
  public static final String TABLER_VIEW_URLSTR = SITE_HOME_URLSTR +
                                                           "/IMDTabler/run";
  public static final String FREQSETGEN_RET_URLSTR = SITE_HOME_URLSTR +
                                                          "/FreqSetGen/run";
  public static final String OUT_SUBDIR_STR = "fsgresults";
  public static final String ERROR_OUTFILE_STR = "FreqSetGenErrors.txt";
  public static final String CHECK_INSTANCES_STR = "checkInstances";
  public static final String DONE_TASK_CMDSTR = "FreqSetGenDoneTask.bat";
  public static final String FILE_SEPARATOR_STR = File.separator;
  public static final char FILE_SEPARATOR_CHAR = File.separatorChar;
  public static final boolean DONE_TASK_FLAG = false;
  public static final int MAX_INSTANCE_COUNT = 3;
  public static final int NUM_RESULTS_SAVED = 25;
  public static final int DEF_MIN_FREQ_SEP = 37; //default minimum freq separation
  public static final int DEF_MAX_RUN_TIMESEC = 120;  //default max run time
  public static final int PAGE_AUTOREFRESH_SECS = 5;
  public static final boolean DEBUG_OUT_FLAG = false;
  public static final boolean CONSOLE_OUT_FLAG = false;
  public static final String TEMP_FILE_EXTSTR = ".tmp";
  public final String outputFileNameStr;
  public final boolean launchFileInBrowserFlag;
  public int numberFreqInSet = 0;
  public int minFreqSeparationValue = 0;
  private int [] possibleFreqSetArr = null;
  private int [] mandatoryFreqSetArr = null;
  private String remoteAddressString = null;
  private String outputHeaderWRefreshString = PROG_SHORT_TITLESTR;
  private String outputHeaderWoRefreshString = PROG_SHORT_TITLESTR;
  private String outputFooterString = "";
  private int possFreqSetSize = 0;
  private int possTestSetSize = 0;
  private int mandFreqSetSize = 0;
  private long possFreqSetMaskMaximum = 0;
  private long possFreqSetMaskValue = 0;
  private long genStartTimeMs = 0;
  private long genStopTimeMs = 0;
  private int progressPercentDone = 0;
  private int estTimeRemainingSecs = 0;
  private static boolean isOSDeterminedFlag = false;
  private static boolean isOSWindowsFlag = false;
  private static boolean normalProgramExitFlag = false;
  private boolean programTerminateFlag = false;
  private final DateFormat lastUpdatedDateFormatter =
                              createDateFormatObj("yyyy-MM-dd h:mm:ss a z");
  private final TreeSet<FreqSetResult> sortedFreqSetResultList =
                                               new TreeSet<FreqSetResult>();
  private static final String zerosString =
         "0000000000000000000000000000000000000000000000000000000000000000";
  private static final String spacesString =
         "                                                                ";


  /**
   * Creates a generator object.
   * @param outputFileNameStr file to receive output, or null for none.
   * @param launchFileInBrowserFlag true to launch output file in local
   * browser.
   */
  public FreqSetGen(String outputFileNameStr, boolean launchFileInBrowserFlag)
  {
    this.outputFileNameStr = outputFileNameStr;
    this.launchFileInBrowserFlag = launchFileInBrowserFlag;
  }

  /**
   * Runs the generator process.
   * @param numberFreqInSet number of frequencies in generated sets.
   * @param possibleFreqSetArr list of possible frequency values for
   * generated sets.
   * @param mandatoryFreqSetArr optional list of frequency values that
   * must be included in all generated sets.
   * @param minFreqSeparationValue minimum allowed separation between
   * frequency values in generated sets.
   * @param maxRunTimeSecs maximum time allow for program run.
   * @param optionStr option string ("checkInstances" to check and
   * restrict number of simultaneous instances), or null for none.
   * @param remoteAddrStr IP address for request, or null for none.
   * @throws RuntimeException if an error occurs.
   */
  public void runGenProcess(int numberFreqInSet, int [] possibleFreqSetArr,
                     int [] mandatoryFreqSetArr, int minFreqSeparationValue,
                 int maxRunTimeSecs, String optionStr, String remoteAddrStr)
                  throws RuntimeException
  {
    genStartTimeMs = System.currentTimeMillis();
    
         //setup footer early in case of early exit due to error:
    outputFooterString = ((remoteAddrStr != null) ?
                               ("\n<br>Request IP: " + remoteAddrStr) : "") +
                        "\n<br><br>" + "<a href=\"" + FREQSETGEN_RET_URLSTR +
          "\" target=\"_blank\">Return to Frequency Set Generator page</a>" + 
                                         "\n<br><br>" + PROG_SHORT_TITLESTR +
                                 " Version " + VERSION_STR + "<br><hr><br>" +
              "<a href=\"http://www.etheli.com/freq/FPV_5.8GHz_Freqs.jpg\"" +
      "target=\"_blank\">5.8GHz FPV \"Visual\" Frequency Chart</a><br><br>" +
              "Click <a href=\"http://www.etheli.com/contact/index.html\">" +
                                           "here to contact me</a><br><br>" +
       "<a href=\"http://www.etheli.com\">Back to etheli.com home page</a>" +
                                                        "\n</body>\n</html>";

    this.numberFreqInSet = numberFreqInSet;
    this.minFreqSeparationValue = minFreqSeparationValue;
    remoteAddressString = remoteAddrStr;
    possibleFreqSetArr = sortArrayAndRemoveDups(possibleFreqSetArr);
    this.mandatoryFreqSetArr = mandatoryFreqSetArr =
                                sortArrayAndRemoveDups(mandatoryFreqSetArr);
    mandFreqSetSize = (mandatoryFreqSetArr != null) ?
                                             mandatoryFreqSetArr.length : 0;
    if(numberFreqInSet < 2)
    {
      throw new RuntimeException(
                         "Requested number of frequencies value too small");
    }
    if(mandFreqSetSize > 0)
    {
      if(mandFreqSetSize >= numberFreqInSet)
      {
        throw new RuntimeException("Too many values in mandatory-frequency" +
                             " set; must be less than requested number of " +
                                            "frequencies in generated sets");
      }
      if(mandatoryFreqSetArr[0] < IMDTabler.MIN_DISP_FREQ)
      {
        throw new RuntimeException("Mandatory-frequency value too low (" +
                                               mandatoryFreqSetArr[0] + ")");
      }
      if(mandatoryFreqSetArr[mandFreqSetSize-1] > IMDTabler.MAX_DISP_FREQ)
      {
        throw new RuntimeException("Mandatory-frequency value too high (" +
                              mandatoryFreqSetArr[mandFreqSetSize-1] + ")");
      }
      if(!isFreqSeparationValid(mandatoryFreqSetArr))
        throw new RuntimeException("Mandatory-frequency spacing too tight");
              //remove any mandatory freqs found in possible freqs set:
      possibleFreqSetArr = removeDupsBtwArrays(
                                    possibleFreqSetArr,mandatoryFreqSetArr);
    }
    this.possibleFreqSetArr = possibleFreqSetArr;
    possFreqSetSize = possibleFreqSetArr.length;
    
    if(possFreqSetSize > 62)
      throw new RuntimeException("Too many possible frequencies (max=62)");
    if(possFreqSetSize < 1)
      throw new RuntimeException("Too few possible frequencies");
    if(possibleFreqSetArr[0] < IMDTabler.MIN_DISP_FREQ)
    {
      throw new RuntimeException("Possible-frequency value too low (" +
                                               possibleFreqSetArr[0] + ")");
    }
    if(possibleFreqSetArr[possFreqSetSize-1] > IMDTabler.MAX_DISP_FREQ)
    {
      throw new RuntimeException("Possible-frequency value too high (" +
                                possibleFreqSetArr[possFreqSetSize-1] + ")");
    }
    if(numberFreqInSet >= mandFreqSetSize + possFreqSetSize)
    {
      throw new RuntimeException("Requested number of frequencies too " +
                                        "large; must be smaller than the " +
                                    "number of available frequency values");
    }

         //number of values from 'possible' set for each test iteration:
    possTestSetSize = numberFreqInSet - mandFreqSetSize;
    if(possTestSetSize < 1 || possTestSetSize > 62)
      throw new RuntimeException("possTestSetSize value out of range");

    possFreqSetMaskMaximum = (1L << possFreqSetSize) - 1;
    possFreqSetMaskValue = 0;

    if(DEBUG_OUT_FLAG)
    {
      System.out.println("DEBUG:  numberFreqInSet=" + numberFreqInSet +
            ", possibleFreqSetArr:  " + intArrToString(possibleFreqSetArr));
      System.out.println("DEBUG:  possFreqSetSize=" + possFreqSetSize +
                                    ", possTestSetSize=" + possTestSetSize +
                         ", possibleNumberFreqs=" + possFreqSetMaskMaximum + 
                      ((mandFreqSetSize > 0) ? (", mandatoryFreqSetArr:  " +
                                intArrToString(mandatoryFreqSetArr)) : ""));
    }
    
         //if "checkInstances" option argument then check number running:
    if(CHECK_INSTANCES_STR.equals(optionStr) &&
                                getProgInstanceCount() > MAX_INSTANCE_COUNT)
    {  //too many other instances of program running
      throw new RuntimeException("Too many generator processes are " +
                               "currently running; please try again later");
    }
    
    final String hdr1Str = "<!DOCTYPE html>\n<html><head>\n" +
       "<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>";
    final String hdr2Str = "\n<title>Results - " + PROG_SHORT_TITLESTR +
                            "</title></head>\n" + "<body>\n<h3>Results - " +
              PROG_SHORT_TITLESTR + " - " + PROG_LONG_TITLESTR + "</h3>\n" +
                "Number of frequencies: " + numberFreqInSet +
       "\n<br>Possible frequencies: " + intArrToString(possibleFreqSetArr) +
                 ((mandFreqSetSize > 0) ? ("<br>\nMandatory frequencies: " +
                                intArrToString(mandatoryFreqSetArr)) : "") +
                     "<br>\nMinimum separation: " + minFreqSeparationValue +
                  "<br>\nMaximum run time: " + maxRunTimeSecs + " seconds" +
                "<br>\nNumber of possible frequencies: " + possFreqSetSize +
                   " (2^" + possFreqSetSize + "=" + possFreqSetMaskMaximum + 
                                                                      ")\n";
    outputHeaderWRefreshString = hdr1Str +
                                 "<meta http-equiv=\"refresh\" content=\"" +
                                   PAGE_AUTOREFRESH_SECS + "\" >" + hdr2Str;
    outputHeaderWoRefreshString = hdr1Str + hdr2Str;

    updateOutputFile(null,false);
    
         //setup response to Ctrl-C or 'kill' signal:
    Runtime.getRuntime().addShutdownHook(new Thread("ShutdownHook")
        {
          public void run()
          {
            programTerminateFlag = true;    //indicate terminated
            if(!normalProgramExitFlag)
            {  //exit is via Ctrl-C or 'kill'; write final update to out file
              updateOutputFile(("Generator terminated; stopping processing ("+
                                          progressPercentDone +"%)"), true);
            }
          }
        });
    
         //if flag then launch output file in local browser:
    if(launchFileInBrowserFlag)
      (new LaunchBrowser()).showApplicationURL(outputFileNameStr);

    int [] nextSubsetArr;
    IMDTabler.IMDTable imdTableObj;
    int minListRating = 0;
    int timeChkCnt = 0, timeChkCntThresh = 0;
    long itemCount = 0, invalidCount = 0, prevChkItemCount = 0,
         prevChkInvalidCount = 0;
    long nextCheckTimeMs = genStartTimeMs + 1000;
    String dispStr, errorMsgStr = null;
    FreqSetResult freqSetResultObj;
    while((nextSubsetArr=getNextPossFreqSubset()) != null)
    {
      if(isFreqSeparationValid(nextSubsetArr))
      {
        imdTableObj = IMDTabler.getIMDTableForFreqSet(nextSubsetArr);
        ++itemCount;
        if(imdTableObj.tableRatingValue >= minListRating ||
                         sortedFreqSetResultList.size() < NUM_RESULTS_SAVED)
        {  //table rating is good enough to (possibly) be added
           // or list not yet at capacity
          freqSetResultObj = new FreqSetResult(imdTableObj,itemCount,
                                                       possFreqSetMaskValue);
          sortedFreqSetResultList.add(freqSetResultObj);     //add to results
          if(sortedFreqSetResultList.size() > NUM_RESULTS_SAVED)  //limit # of results
          {
            sortedFreqSetResultList.pollLast();       //remove last item
            if(minListRating >= IMDTabler.RATING_MAX_VALUE)
            {
              errorMsgStr = "IMD rating of all saved frequency sets is " +
                                  IMDTabler.RATING_MAX_VALUE + "; stopping";
              break;
            }
          }
          minListRating = sortedFreqSetResultList.last().tableRatingValue;
          if(DEBUG_OUT_FLAG)
          {
            dispStr = freqSetResultObj.toString() + " *" +
                                                  ((progressPercentDone>0) ?
                                    (" " + progressPercentDone + "%") : "");
          }
        }
        else
        {
          if(DEBUG_OUT_FLAG)
          {
            dispStr = FreqSetResult.toString(imdTableObj,itemCount) +
                                                  ((progressPercentDone>0) ?
                                    (" " + progressPercentDone + "%") : "");
          }
        }
        if(DEBUG_OUT_FLAG)
        {
          if(imdTableObj.tableRatingValue >= 50)
          {
            System.out.println(possFreqSetMaskValueBinStr() + " " + dispStr);
          }
        }
      }
      else
      {
        ++invalidCount;
//        System.out.println(possFreqSetMaskValueBinStr() + "  " +
//                            intArrToString(nextSubsetArr) + "  [rejected]");
      }

      if(++timeChkCnt > timeChkCntThresh)
      {  //enough iterations have occurred; check time elapsed
        if(programTerminateFlag)  //if program terminating then
          return;                 //exit method (and program)
        timeChkCnt = 0;
        final long curTimeMs = System.currentTimeMillis();
        if(curTimeMs >= nextCheckTimeMs)
        {  //one second has elapsed
          nextCheckTimeMs = curTimeMs + 1000;
              //setup check threshold to have about 10 checks per second:
          timeChkCntThresh = (int)((itemCount - prevChkItemCount +
                                    invalidCount - prevChkInvalidCount)/10);
          if(timeChkCntThresh > 99)    //don't allow too large
            timeChkCntThresh = 99;     //to keep checks timely
          prevChkItemCount = itemCount;
          prevChkInvalidCount = invalidCount;
          final double complRatio = (double)possFreqSetMaskValue /
                                             (double)possFreqSetMaskMaximum;
          progressPercentDone = (int)(complRatio * 100.0 + 0.5);
          final long msElapsed = curTimeMs - genStartTimeMs;
          final int secsElapsed = (int)((msElapsed) / 1000);
          estTimeRemainingSecs =
                           (complRatio > 0.005 && complRatio < 1.0) ? (int)(
                 ((double)msElapsed/complRatio-msElapsed) / 1000 + 0.5) : 0;
          if(secsElapsed > maxRunTimeSecs)
          {  //maximum allowed run time reached
            errorMsgStr = "Maximum run time reached; stopping processing ("+
                                                  progressPercentDone +"%)";
            break;
          }
          if(DEBUG_OUT_FLAG)
          {
            System.out.println("DEBUG:  itemCount=" + itemCount +
                                          ", invalidCount=" + invalidCount +
                                  ", timeChkCntThresh=" + timeChkCntThresh);
          }
          if(CONSOLE_OUT_FLAG)
          {
            System.out.println(intToPadStr(secsElapsed,5) + " second" +
                        ((secsElapsed!=1) ? ("s") : " ") +  ", progress: " +
                               progressPercentDone + "%, time remaining: " +
                                                 ((estTimeRemainingSecs>0) ?
                        (estTimeRemainingSecs + " seconds") : "(unknown)"));
            System.out.println("------------------------------------------");
            for(FreqSetResult fsrObj : sortedFreqSetResultList)
              System.out.println(fsrObj);
          }
          updateOutputFile(null,false);
        }
      }
    }
    genStopTimeMs = System.currentTimeMillis();
    if(CONSOLE_OUT_FLAG)
    {
      if(errorMsgStr != null)
        System.out.println(errorMsgStr);
      System.out.println("------------------------------------------");
      for(FreqSetResult fsrObj : sortedFreqSetResultList)
        System.out.println(fsrObj);
    }
    updateOutputFile(errorMsgStr,true);
  }
  
  //Generates next subset from 'possibleFreqSetArr[]', combined with
  // the 'mandatoryFreqSetArr[]' values (if any).
  private final int [] getNextPossFreqSubset()
  {
    int cnt;
    long msk;
    while(true)
    {
      if(++possFreqSetMaskValue > possFreqSetMaskMaximum)
        return null;
         //count # of 1's in mask:
      cnt = (int)(possFreqSetMaskValue & 1);
      msk = possFreqSetMaskValue;
      for(int i=1; i<possFreqSetSize; ++i)
        if((int)((msk>>=1) & 1) != 0)  ++cnt;
         //if desired count then exit loop:
      if(cnt == possTestSetSize)
        break;
    }
    if(mandFreqSetSize <= 0)
    {  //no mandatory frequencies; return possible freqs for test
      final int [] retArr = new int[possTestSetSize];
      cnt = 0;
      msk = possFreqSetMaskValue;
      for(int i=0; i<possFreqSetSize; ++i)
      {  //for each '1' in mask, use freq at that position
        if((int)(msk & 1) != 0)
        {
          retArr[cnt] = possibleFreqSetArr[i];
          if(++cnt >= possTestSetSize) //if reached number of possible freqs
            break;                     // for test then exit loop
        }
        msk >>= 1;
      }
      return retArr;
    }
         //combine mandatory and possible freqs, in sort order:
    final int [] retArr = new int[mandFreqSetSize+possTestSetSize];
    cnt = 0;
    msk = possFreqSetMaskValue;
    int possVal, retIdx = 0, mandIdx = 0;
    for(int i=0; i<possFreqSetSize; ++i)
    {  //for each '1' in mask, use freq at that position
      if((int)(msk & 1) != 0)
      {
        possVal = possibleFreqSetArr[i];
        while(mandIdx < mandFreqSetSize &&
                                     mandatoryFreqSetArr[mandIdx] < possVal)
        {  //copy any mandatory freqs less than current into return array
          retArr[retIdx++] = mandatoryFreqSetArr[mandIdx++];
        }
        retArr[retIdx++] = possVal;    //copy over current possible freq
        if(++cnt >= possTestSetSize)   //if reached number of possible freqs
          break;                       // for test then exit loop
      }
      msk >>= 1;
    }
    while(mandIdx < mandFreqSetSize)
    {  //copy any remaining mandatory freqs into return array
      retArr[retIdx++] = mandatoryFreqSetArr[mandIdx++];
    }
    return retArr;
  }
  
  /**
   * Updates the output file (if one was specified).
   * @param errorMsgStr error message to be displayed, or null for none.
   * @param doneFlag true if program complete; false if not.
   */
  private final synchronized void updateOutputFile(String errorMsgStr,
                                                           boolean doneFlag)
  {
    if(outputFileNameStr == null)
      return;
    final StringBuffer buff = new StringBuffer();

    buff.append(doneFlag ? outputHeaderWoRefreshString :
                                                outputHeaderWRefreshString);
    buff.append("<br>");
    if(errorMsgStr != null)
      buff.append("<br>" + errorMsgStr + "<br>\n");
    else if(!doneFlag)
    {
      buff.append("<br>Generation in progress (" + getElaspsedRunTimeStr() +
               ") " + progressPercentDone + "% complete, time remaining: " +
                                                 ((estTimeRemainingSecs>0) ?
             (estTimeRemainingSecs + " seconds") : "(unknown)") + "<br>\n");
    }
    else
    {
      buff.append("<br>Completed; process run time: " +
                                        getElaspsedRunTimeStr() + "<br>\n");
    }

    for(FreqSetResult fsrObj : sortedFreqSetResultList)
    {
      buff.append("<br>\n &nbsp; " + fsrObj.getTableSelFreqSetDispStr() +
                    " &nbsp;&nbsp; IMD rating: " + fsrObj.tableRatingValue +
                 " &nbsp;&nbsp; <a href=\"" + fsrObj.getTablerViewUrlStr() +
                                           "\" target=\"_blank\">view</a>");
    }

    buff.append("\n<br><br>Last updated: " +
                               lastUpdatedDateFormatter.format(new Date()));
    buff.append("\n<br>Process started: " +
                 lastUpdatedDateFormatter.format(new Date(genStartTimeMs)));
    buff.append(outputFooterString);

    commitOutputFile(buff.toString(),doneFlag);

//    if(doneFlag && DONE_TASK_FLAG && !launchFileInBrowserFlag &&
//                                    (new File(DONE_TASK_CMDSTR)).exists() &&
//                                     (new File(outputFileNameStr)).exists())
//    {  //"done" task enabled & not using local browser (running via servlet)
//       // and task-command and results-output files found OK
//              //execute done-task command with output pathname and "subject"
//              // text ("FreqSetGen reqIP outDir/outFile") as arguments:
//      execCmdNoResp(DONE_TASK_CMDSTR + " \"" + outputFileNameStr +
//                       "\" \"FreqSetGen " + ((remoteAddressString != null &&
//                                  remoteAddressString.trim().length() > 0) ?
//                                         (remoteAddressString + ' ') : "") +
//                      getViewDirFromOutDirFileStr(outputFileNameStr) + '/' +
//                    getViewFileFromOutDirFileStr(outputFileNameStr) + '\"');
//    }
  }

  /**
   * Commits the given data to the output file.
   * @param outDataStr output data string.
   * @param doneFlag true if program complete; false if not.
   * @return true if successful; false if an I/O error occurred.
   */
  private final boolean commitOutputFile(String outDataStr, boolean doneFlag)
  {
    int cnt = doneFlag ? 20 : 3;
    final File tempFileObj = new File(outputFileNameStr+TEMP_FILE_EXTSTR);
    final File delFileObj = new File(outputFileNameStr+".del");
    final File outFileObj = new File(outputFileNameStr);
    do
    {    //write data to file; retry if error
      try
      {       //first, write to temp file
        final BufferedWriter wtrObj = new BufferedWriter(
                                               new FileWriter(tempFileObj));
        wtrObj.write(outDataStr);
        wtrObj.close();
              //remove old copy and move temp file to output file:
        outFileObj.renameTo(delFileObj);
        final boolean retFlag = tempFileObj.renameTo(outFileObj);
        delFileObj.delete();
        if(retFlag)
          return true;
      }
      catch(IOException ex)
      {
      }
      try { Thread.sleep(100); }
      catch(InterruptedException ex) {}
    }
    while(--cnt > 0);
    return false;
  }

  /**
   * Returns a binary-string version of 'possFreqSetMaskValue' padded with
   * leading zeros.
   * @return A binary-string version of 'possFreqSetMaskValue' padded with
   * leading zeros
   */
  private final String possFreqSetMaskValueBinStr()
  {
    String binStr = Long.toBinaryString(possFreqSetMaskValue);
    final int diff;
    if((diff=possFreqSetSize-binStr.length()) <= 0)
      return binStr;
    return zerosString.substring(0,diff) + binStr;
  }

  /**
   * Returns a description of the elapsed time for the program run.
   * @return A description of the elapsed time for the program run.
   */
  private final String getElaspsedRunTimeStr()
  {
    final long timeMs = (genStopTimeMs > 0) ?
                                            genStopTimeMs - genStartTimeMs :
                                System.currentTimeMillis() - genStartTimeMs;
    if(timeMs > 5000)
      return (timeMs/1000) + " seconds";
    else
      return timeMs/1000 + " ms";
  }
  
  /**
   * Checks if the minimum-separation for the given set of frequency
   * values is large enough for it to be a valid set.
   * @param intArr set of frequency values.
   * @return true if the set of frequency values is valid.
   */
  public final boolean isFreqSeparationValid(int [] intArr)
  {
    for(int i=1; i<intArr.length; ++i)
    {
      if(intArr[i]-intArr[i-1] < minFreqSeparationValue)
        return false;
    }
    return true;
  }

  /**
   * Program entry point (if application).
   * @param args array of command-line arguments.
   */
  public static void main(String [] args)
  {
    final FreqSetGen freqSetGenObj;
    int numberFreqInSet, baseIdx = 0;
    String outFileArgStr = null;
    boolean launchFlag = true;
    try
    {
      if(args.length < 2)
      {
        System.err.println("Not enough parameters");
        System.err.println("Usage:  FreqSetGen [outFile] numFreq " +
                  "possibleFreqs mandatoryFreqs [minFreqSep] [maxRunSecs]");
        return;
      }
      String outFileStr = null;
      try
      {  //attempt to parse first parameter as numeric
        numberFreqInSet = Integer.parseInt(args[0]);
      }
      catch(NumberFormatException ex)
      {  //first parameter not numeric; take as output file name
        outFileStr = outFileArgStr = args[0];
        numberFreqInSet = Integer.parseInt(args[1]);
        ++baseIdx;      //shift indices +1 for rest of parameters
      }
         //if no 'outFile' argument given then generate name
         // and set flag to launch file in local browser:
      if(launchFlag = (outFileStr == null))
      {
        outFileStr = generateOutDirFileNameStr();
        makeDirsForPathname(outFileStr);
      }
      freqSetGenObj = new FreqSetGen(outFileStr,launchFlag);
    }
    catch(Throwable ex)
    {
      if(CONSOLE_OUT_FLAG || launchFlag)
        ex.printStackTrace();
      writeExcToErrorFile(ex,outFileArgStr);
      return;
    }
    try
    {
      freqSetGenObj.runGenProcess(numberFreqInSet,
                                  IMDTabler.stringToIntArr(args[baseIdx+1]),
                                                  ((args.length>baseIdx+2) ?
                          IMDTabler.stringToIntArr(args[baseIdx+2]) : null),
                                                  ((args.length>baseIdx+3) ?
                      Integer.parseInt(args[baseIdx+3]) : DEF_MIN_FREQ_SEP),
                                                  ((args.length>baseIdx+4) ?
                   Integer.parseInt(args[baseIdx+4]) : DEF_MAX_RUN_TIMESEC),
                         ((args.length>baseIdx+5) ? args[baseIdx+5] : null),
                        ((args.length>baseIdx+6) ? args[baseIdx+6] : null));
      normalProgramExitFlag = true;    //indicate not Ctrl-C or 'kill' exit
    }
    catch(NumberFormatException ex)
    {  //error converting string to number
      normalProgramExitFlag = true;    //indicate not Ctrl-C or 'kill' exit
      final String errStr = "Error parsing numeric input:  " + ex.getMessage();
      if(CONSOLE_OUT_FLAG || launchFlag)
        System.out.println(errStr);
      try
      {
        freqSetGenObj.updateOutputFile(errStr,true);  //send to output file
      }
      catch(Throwable ex2)
      {
        if(CONSOLE_OUT_FLAG)
          ex2.printStackTrace();
        writeExcToErrorFile(ex2,outFileArgStr);
      }
    }
    catch(RuntimeException ex)
    {  //runGenProcess error
      normalProgramExitFlag = true;    //indicate not Ctrl-C or 'kill' exit
      final String errStr = "Error:  " + ex.getMessage();
      if(CONSOLE_OUT_FLAG || launchFlag)
        System.out.println(errStr);
      freqSetGenObj.updateOutputFile(errStr,true);    //send to output file
    }
    catch(Throwable ex)
    {  //some kind of exception error
      normalProgramExitFlag = true;    //indicate not Ctrl-C or 'kill' exit
      final String errStr = "Error:  " + ex.getMessage() + "; stopping";
      if(CONSOLE_OUT_FLAG)
      {
        System.out.println(errStr);
        ex.printStackTrace();
      }
      writeExcToErrorFile(ex,outFileArgStr);
      freqSetGenObj.updateOutputFile(errStr,true);    //send to output file
    }
  }

  /**
   * Appends an error message to the error file for the given exception.
   * @param ex exception object.
   * @param outFileArgStr output filename given on command line, or null
   * if none.
   */
  private static void writeExcToErrorFile(Throwable ex, String outFileArgStr)
  {
    try
    {
      final DateFormat dateFormatObj =
                              createDateFormatObj("yyyy-MM-dd h:mm:ss a z");
      String outFStr = ERROR_OUTFILE_STR;
      final int p;
      if(outFileArgStr != null &&
                              (p=outFileArgStr.indexOf(OUT_SUBDIR_STR)) > 0)
      {  //path to output files located OK; use top of path for error file
        outFStr = outFileArgStr.substring(0,p) + OUT_SUBDIR_STR +
                                               FILE_SEPARATOR_STR + outFStr;
      }
      final PrintWriter wtrObj = new PrintWriter(
                                              new FileWriter(outFStr,true));
      wtrObj.println();
      wtrObj.println(dateFormatObj.format(new Date()) + ":");
      ex.printStackTrace(wtrObj);
      wtrObj.println();
      wtrObj.println("---------------------------------------------------");
      wtrObj.close();
    }
    catch(Throwable ex2)
    {
      System.err.println("Exception writing to error file:  " + ex2);
      System.err.println("Original error:  " + ex);
      ex.printStackTrace();
    }
  }

  /**
   * Generates an output directory and file name based on the current
   * date/time.
   * @return A new output pathname.
   */
  public static String generateOutDirFileNameStr()
  {
    final Date curTimeDateObj = new Date();
    final DateFormat dirNameFormatter = createDateFormatObj("yyyyDDD");
    final DateFormat fileNameFormatter =
                                   createDateFormatObj("yyyyMMddHHmmssSSS");
    return generateOutDirFileNameStr(
                              ("d"+dirNameFormatter.format(curTimeDateObj)),
                            ("r"+fileNameFormatter.format(curTimeDateObj)));
  }

  /**
   * Generates an output pathname based on the given directory and file
   * name strings.
   * @param dirStr directory name.
   * @param fileStr file name.
   * @return A new output pathname.
   */
  public static String generateOutDirFileNameStr(String dirStr, String fileStr)
  {
    return OUT_SUBDIR_STR + FILE_SEPARATOR_CHAR + dirStr +
                                    FILE_SEPARATOR_CHAR + fileStr + ".html";
  }

  /**
   * Extracts the "viewFile" part of the given output pathname.
   * @param outDirFileStr output pathname.
   * @return The "viewFile" part of the given output pathname.
   */
  public static String getViewFileFromOutDirFileStr(String outDirFileStr)
  {
    int p,q;
    if((p=outDirFileStr.lastIndexOf(FILE_SEPARATOR_CHAR)) > 0)
    {  //found last '/' separator; remove trailing ".html" (if any)
      if((q=outDirFileStr.lastIndexOf('.')) <= p)
        q = outDirFileStr.length();
      return outDirFileStr.substring(p+1,q);
    }
    return "";
  }

  /**
   * Extracts the "viewDir" part of the given output pathname.
   * @param outDirFileStr output pathname.
   * @return The "viewDir" part of the given output pathname.
   */
  public static String getViewDirFromOutDirFileStr(String outDirFileStr)
  {
    int p,q;
    if((q=outDirFileStr.lastIndexOf(FILE_SEPARATOR_CHAR)) > 0)
    {  //found last '/' separator; find next-to-last separator
      if((p=outDirFileStr.lastIndexOf(FILE_SEPARATOR_CHAR,q-1)) < 0)
        p = 0;
      return outDirFileStr.substring(p+1,q);
    }
    return "";
  }

  /**
   * Creates any parent directories needed for the given pathname.
   * @param pathStr pathname for an output file.
   */
  public static void makeDirsForPathname(String pathStr)
  {
    (new File(pathStr)).getParentFile().mkdirs();
  }

  /**
   * Returns a string representation of the given integer array.
   * @param intArr given integer array
   * @return A string representation of the given integer array.
   */
  public static String intArrToString(int [] intArr)
  {
    final StringBuffer buff = new StringBuffer();
    if(intArr.length > 0)
    {
      int i = 0;
      while(true)
      {
        buff.append(Integer.toString(intArr[i]));
        if(++i >= intArr.length)
          break;
        buff.append(" ");
      }
    }
    return buff.toString();
  }

  /**
   * Sorts items and removes duplicate values from the given array.
   * @param intArr array of integer values.
   * @return A new array with values in sorted order and duplicate values
   * removed.
   */
  public static int [] sortArrayAndRemoveDups(int [] intArr)
  {
    final int intArrLen = (intArr != null) ? intArr.length : 0;
    if(intArrLen > 0)
    {
      final int [] sortedArr = Arrays.copyOf(intArr,intArrLen);
      Arrays.sort(sortedArr);
      return removeDupsFromSortedArray(sortedArr);
    }
    return intArr;
  }

  /**
   * Removes duplicate values from the given array.
   * @param intArr array of integer values in sorted order.
   * @return A new array with duplicate values removed, or the given
   * array if no duplicates were found.
   */
  public static int [] removeDupsFromSortedArray(int [] intArr)
  {
    if(intArr == null || intArr.length < 2)
      return intArr;
    final int [] newIntArr = new int[intArr.length];
    int val, i = 1, nIdx = 1;
    val = newIntArr[0] = intArr[0];
    do
    {         //copy over if value not same as previous
      if(val != intArr[i])
        val = newIntArr[nIdx++] = intArr[i];
    }
    while(++i < intArr.length);
    if(i == nIdx)       //if none skpped then
      return intArr;    //return original array
    return Arrays.copyOf(newIntArr,nIdx);   //return smaller array
  }

  /**
   * Checks if the given array contains the given value.
   * @param checkArr array to check.
   * @param chkVal value to check.
   * @return true if the given array contains the given value; false if not.
   */
  public static boolean isValueInArray(int [] checkArr, int chkVal)
  {
    for(int i=0; i<checkArr.length; ++i)
    {
      if(checkArr[i] == chkVal)
        return true;
    }
    return false;
  }

  /**
   * Removes duplicate values between arrays.  Any values in the data
   * array that are also in the check array are removed from the data
   * array.
   * @param dataArr data array.
   * @param checkArr check array.
   * @return A new array with duplicate values removed, or the given
   * data array if no duplicates were found.
   */
  public static int [] removeDupsBtwArrays(int [] dataArr, int [] checkArr)
  {
    final int [] retArr = new int[dataArr.length];
    int val, i = 0, retArrIdx = 0;
    while(i < dataArr.length)
    {  //for each value in data array
      val = dataArr[i++];
      if(!isValueInArray(checkArr,val))     //if not in check array then
        retArr[retArrIdx++] = val;          //keep value in data array
    }
         //if no change return given data array; if changes
         // then return new, properly-sized array:
    return (i == retArrIdx) ? dataArr : Arrays.copyOf(retArr,retArrIdx);
  }

  /**
   * Generates a numeric string for the given data value, padded with
   * leading spaces to fit the given field length.
   * @param intVal data value.
   * @param fieldLen field length.
   * @return Padded numeric string.
   */
  public static String intToPadStr(int intVal, int fieldLen)
  {
    final String str = Integer.toString(intVal);
    int numSp;
    return (((numSp=fieldLen-str.length()) > 0) ?
                                spacesString.substring(0,numSp) : "") + str;
  }

  /**
   * Generates a numeric string for the given data value, padded with
   * leading spaces to fit the given field length.
   * @param longVal data value.
   * @param fieldLen field length.
   * @return Padded numeric string.
   */
  public static String longToPadStr(long longVal, int fieldLen)
  {
    final String str = Long.toString(longVal);
    int numSp;
    return (((numSp=fieldLen-str.length()) > 0) ?
                                spacesString.substring(0,numSp) : "") + str;
  }

  /**
   * Creates a 'DateFormat' object and applies the given pattern string to
   * it.  The recommended method of using "DateFormat.getDateTimeInstance()"
   * is followed.  See the Java help reference for 'SimpleDateFormat' for
   * details on the characters used in the pattern string.
   * @param patternStr the pattern string to apply.
   * @return A new 'SimpleDateFormat' object with the given pattern
   * string applied to it.
   */
  public static DateFormat createDateFormatObj(String patternStr)
  {
              //create instance (the "right" way):
    DateFormat formatObj = DateFormat.getDateTimeInstance(
                                     DateFormat.DEFAULT,DateFormat.DEFAULT);
              //attempt to set pattern; if fails create instance directly:
    if(!setDateFormatPattern(formatObj,patternStr))
      formatObj = new SimpleDateFormat(patternStr);
    return formatObj;        //return new instance
  }

  /**
   * Sets the pattern string for the given 'DateFormat' object.  If the
   * given 'DateFormat' object is not of type 'SimpleDateFormat' then
   * it is left unchanged.  See the Java help reference for
   * 'SimpleDateFormat' for details on the characters used in
   * the pattern string.
   * @param formatObj the 'DateFormat' to be changed.
   * @param patternStr the pattern string to apply.
   * @return true if the pattern was applied successfully; false if an
   * error occurred.
   */
  public static boolean setDateFormatPattern(DateFormat formatObj,
                                                          String patternStr)
  {
    try       //trap any exceptions that might occur
    {              //if object is 'SimpleDateFormat' type and pattern
                   // string is not null then apply it:
      if(formatObj instanceof SimpleDateFormat && patternStr != null)
        ((SimpleDateFormat)formatObj).applyPattern(patternStr);
      return true;
    }
    catch(Exception ex) {}        //ignore any exceptions
    return false;
  }

  /**
   * Returns a count of the number of instances of this program
   * currently running.
   * @return A count of the number of instances of this program
   * currently running.
   */
  public static int getProgInstanceCount()
  {
    try
    {
      String respStr;
        //get list of running processes:
      if(isWindowsOS())
      {
  //      respStr = execCmdGetResp("wmic process list");
        respStr = execCmdGetResp("tasklist /V");
      }
      else
        respStr = execCmdGetResp("ps -Af");
      
      if(respStr != null)
      {
        int p = 0, count = 0;
        while((p=respStr.indexOf(FreqSetGen.class.getName(),p+1)) > 0)
          ++count;
        return count;
      }

    }
    catch(Exception ex)
    {
    }
    return 0;
  }

  /**
   * Executes the given command and returns the response.
   * @param cmdStr system command to be executed.
   * @return A string containing the generated response, or null
   * if an error occurred.
   */
  public static String execCmdGetResp(String cmdStr)
  {
    try
    {
      final Process procObj = Runtime.getRuntime().exec(cmdStr);
      final InputStream inStm = procObj.getInputStream();
      @SuppressWarnings("resource")
      final Scanner scannerObj = new Scanner(inStm).useDelimiter("\\A");
      return scannerObj.hasNext() ? scannerObj.next() : "";
    }
    catch(IOException e)
    {
      return null;
    }
  }

  /**
   * Executes the given command and returns the response.
   * @param cmdStr system command to be executed.
   * @return A string containing the generated response, or an
   * error message if an error occurred.
   */
  public static String execCmdGetRespMsg(String cmdStr)
  {
    try
    {
      final Process procObj = Runtime.getRuntime().exec(cmdStr);
      final InputStream inStm = procObj.getInputStream();
      @SuppressWarnings("resource")
      final Scanner scannerObj = new Scanner(inStm).useDelimiter("\\A");
      return scannerObj.hasNext() ? scannerObj.next() : "";
    }
    catch(IOException e)
    {
      return e.toString();
    }
  }

  /**
   * Executes the given command and returns the response.
   * @param cmdStrArr system command and arguments to be executed.
   * @return A string containing the generated response, or an
   * error message if an error occurred.
   */
  public static String execCmdGetRespMsg(String [] cmdStrArr)
  {
    try
    {
      final Process procObj = Runtime.getRuntime().exec(cmdStrArr);
      final InputStream inStm = procObj.getInputStream();
      @SuppressWarnings("resource")
      final Scanner scannerObj = new Scanner(inStm).useDelimiter("\\A");
      return scannerObj.hasNext() ? scannerObj.next() : "";
    }
    catch(IOException e)
    {
      return e.toString();
    }
  }

  /**
   * Executes the given command and ignores any response.
   * @param cmdStr system command to be executed.
   * @return true if successful; false if an error occurred.
   */
  public static boolean execCmdNoResp(String cmdStr)
  {
    try
    {
      Runtime.getRuntime().exec(cmdStr);
      return true;
    }
    catch(Exception e)
    {
      return false;
    }
  }

  /**
   * Executes the given command and ignores any response.
   * @param cmdStrArr system command and arguments to be executed.
   * @return true if successful; false if an error occurred.
   */
  public static boolean execCmdNoResp(String [] cmdStrArr)
  {
    try
    {
      Runtime.getRuntime().exec(cmdStrArr);
      return true;
    }
    catch(Exception e)
    {
      return false;
    }
  }
  

  /**
   * Determines if the operating system is Windows.
   * @return true if the operating system is Windows.
   */
  public static boolean isWindowsOS()
  {
    if (!isOSDeterminedFlag)
    {
      isOSDeterminedFlag = true;
      try
      {
        isOSWindowsFlag = (System.getProperty("os.name","").toLowerCase().indexOf(
                                                                 "windows") >= 0);
      }
      catch(Exception ex) {}
    }
    return isOSWindowsFlag;
  }


  /**
   * Class FreqSetResult holds data for a frequency-set-result item.
   */
  public static class FreqSetResult implements Comparable<FreqSetResult>
  {
    public final int tableRatingValue;
    public final long itemCountVal;
    public final long freqSetMaskValue;
    private final IMDTabler.IMDTable imdTableObj;

    /**
     * Creates a holder for a frequency-set-result item.
     * @param imdTableObj table for result.
     * @param itemCountVal count value for item.
     * @param freqSetMaskValue mask value for item.
     */
    public FreqSetResult(IMDTabler.IMDTable imdTableObj, long itemCountVal,
                                                      long freqSetMaskValue)
    {
      this.imdTableObj = imdTableObj;
      this.itemCountVal = itemCountVal;
      this.freqSetMaskValue = freqSetMaskValue;
      tableRatingValue = imdTableObj.tableRatingValue;
    }

    /**
     * Compares this object to the given object, using the table-rating
     * value.
     * @return A negative integer, zero, or a positive integer as this
     * object is less than, equal to, or greater than the specified object. 
     */
    public int compareTo(FreqSetResult fsrObj)
    {
      final int val;
      if((val=fsrObj.tableRatingValue-tableRatingValue) != 0)
        return val;
      return (int)itemCountVal - (int)fsrObj.itemCountVal;
    }

    /**
     * Determines if the given object is equal to this one.
     * @return true if the given object is equal to this one.
     */
    public boolean equals(Object obj)
    {
      return (obj instanceof FreqSetResult) &&
                                         compareTo((FreqSetResult)obj) == 0;
    }
    
    /**
     * Returns a display string containing the 'selFreqSetArr' values
     * from the table for this result.
     * @return A display string containing the 'selFreqSetArr' values
     * from the table for this result.
     */
    public String getTableSelFreqSetDispStr()
    {
      return imdTableObj.getSelFreqSetDispStr();
    }
    
    /**
     * Returns a URL string for viewing the 'selFreqSetArr' values on
     * the IMDTabler page.
     * @return A URL string for viewing the 'selFreqSetArr' values on
     * the IMDTabler page.
     */
    public String getTablerViewUrlStr()
    {
      return TABLER_VIEW_URLSTR + "?freq_values=" +
                        imdTableObj.getSelFreqSetDispStr().replace(' ','+');
    }

    /**
     * Returns a string representation of this object.
     * @return A string representation of this object.
     */
    public String toString()
    {
      return intToPadStr(imdTableObj.tableRatingValue,5) + " " +
                                        longToPadStr(itemCountVal,8) + "  " +
                                          imdTableObj.getSelFreqSetDispStr();
    }

    /**
     * Returns a string representation using the given parameters.
     * @param imdTableObj table for result.
     * @param itemCountVal count value for item.
     * @return A string representation.
     */
    public static String toString(IMDTabler.IMDTable imdTableObj, long itemCountVal)
    {
      return intToPadStr(imdTableObj.tableRatingValue,5) + " " +
                                        longToPadStr(itemCountVal,8) + "  " +
                                          imdTableObj.getSelFreqSetDispStr();
    }
  }
}
