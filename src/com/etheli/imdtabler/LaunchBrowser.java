//LaunchBrowser.java:  Contains methods for launching browser windows.
//
//  6/26/2013 -- [ET]
//

package com.etheli.imdtabler;

import java.applet.Applet;
import java.applet.AppletContext;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;

/**
 * Class LaunchBrowser contains methods for launching browser windows.
 * Application and applet environments are supported.  In an application
 * environment, the operating system may be auto-determined and several
 * different versions of the launch command may be attempted, including
 * a user-specified launch command.  The following default launch commands
 * are used: <pre>
 *   Non-Windows:     netscape URL
 *   Windows95/98:    start "URL"
 *   Windows2000/XP:  cmd /C "start URL" (for non-file URLs)
 *   Windows2000/XP:  cmd /C "URL" (for file URLs)
 *   Macintosh:       /usr/bin/open URL
 * </pre>The order in which the launch commands are attempted is varied such
 * that the one for the operating system in use is attempt first (after any
 * 'setLaunchCommand()' entered).  When using "start" or "cmd /C" under
 * Windows operating systems, special characters ("&|()^") are "escaped"
 * by preceding them with the carrot ("^") character.
 */
public class LaunchBrowser
{
    /** Selection value to indicate non-Windows operating system. */
  public static final int NON_WINDOWS = 0;
    /** Selection value to indicate Windows 98 or earlier OS. */
  public static final int WINDOWS_98 = 1;
    /** Selection value to indicate Windows XP/2000 operating system. */
  public static final int WINDOWS_XP = 2;
    /** Selection value to indicate Macintosh operating system. */
  public static final int MACINTOSH = 3;
              //array of strings corresponding to OS selection values:
  protected static final String [] osSelStringsArr =
           { "Non-Windows", "Windows95/98", "Windows2000/XP", "Macintosh" };
  protected final AppletContext appletContextObj;
  protected int osSelectVal = NON_WINDOWS;
  protected static String osNamePropString = null;
  protected String errorMessage = null;
  protected static String NETSCPSP_STR = "netscape ";
  protected static String UBOPENSP_STR = "/usr/bin/open ";
  protected static String STARTSP_STR = "start ";
  protected static String STARTQT_STR = STARTSP_STR + "\"";
  protected static String CMDCQT_STR = "cmd /C \"";
  protected static String EMPTY_STR = "";
  protected static String QUOTE_STR = "\"";
  protected static String NEED_ESC_CHARS = "&|()^";
         //setup arrays of prefix/suffix strings for operating systems
         // (version for non-file URLs):
  protected final String [] httpOsPrefixArr = new String []
                      { NETSCPSP_STR, STARTQT_STR, (CMDCQT_STR+STARTSP_STR),
                                                             UBOPENSP_STR };
  protected final String [] httpOsSuffixArr = new String []
                             { EMPTY_STR, QUOTE_STR, QUOTE_STR, EMPTY_STR };
         //setup arrays of prefix/suffix strings for operating systems
         // (version for file URLs):
  protected final String [] fileOsPrefixArr = new String []
                    { NETSCPSP_STR, STARTQT_STR, CMDCQT_STR, UBOPENSP_STR };
  protected final String [] fileOsSuffixArr = new String []
                             { EMPTY_STR, QUOTE_STR, QUOTE_STR, EMPTY_STR };
         //arrays to hold prefix/suffix strings in the current order:
         // (version for non-file URLs):
  protected String [] httpCmdPrefixArr = null;
  protected String [] httpCmdSuffixArr = null;
         //arrays to hold prefix/suffix strings in the current order:
         // (version for file URLs):
  protected String [] fileCmdPrefixArr = null;
  protected String [] fileCmdSuffixArr = null;
         //1st index for cmdPre/SuffixArr; 0 if 'setLaunchCommand()' used:
  protected int firstCmdArrIdx = 1;
         //last command string used by 'showApplicationURL()':
  protected String lastShowURLCmdStr = null;
         //flag set false to disable functionality:
  protected boolean browserLaunchEnabledFlag = true;


  /**
   * Creates a launch-browser object for an application environment.
   * The operating system type is automatically determined.
   */
  public LaunchBrowser()
  {
    this(determineOSType());
  }

  /**
   * Creates a launch-browser object for an application environment.
   * The operating system type is automatically determined.
   * @param cmdPrefixStr the preferred string to put before the URL in
   * the launch command, or null for none.
   */
  public LaunchBrowser(String cmdPrefixStr)
  {
    this(determineOSType());
                        //setup preferred launch command string:
    setLaunchCommand(cmdPrefixStr);
  }

  /**
   * Creates a launch-browser object for an application environment.
   * The operating system type is automatically determined.
   * @param cmdPrefixStr the preferred string to put before the URL in
   * the launch command, or null for none.
   * @param cmdSuffixStr the preferred string to put after the URL in
   * the launch command, or null for none.
   */
  public LaunchBrowser(String cmdPrefixStr, String cmdSuffixStr)
  {
    this(determineOSType());
                        //setup preferred launch command strings:
    setLaunchCommand(cmdPrefixStr,cmdSuffixStr);
  }

  /**
   * Creates a launch-browser object for an application environment.
   * @param selVal one of the operating system select values (NON_WINDOWS,
   * WINDOWS_98, WINDOWS_XP or MACINTOSH).
   */
  public LaunchBrowser(int selVal)
  {
    appletContextObj = null;           //clear applet context handle
    if(!setOSType(selVal))             //setup OS prefix/suffix strings
      setOSType(NON_WINDOWS);          //if error then use default
  }

  /**
   * Creates a launch-browser object for an application environment.
   * @param selVal one of the operating system select values (NON_WINDOWS,
   * WINDOWS_98, WINDOWS_XP or MACINTOSH).
   * @param cmdPrefixStr the preferred string to put before the URL in
   * the launch command, or null for none.
   */
  public LaunchBrowser(int selVal, String cmdPrefixStr)
  {
    this(selVal);
                        //setup preferred launch command string:
    setLaunchCommand(cmdPrefixStr);
  }

  /**
   * Creates a launch-browser object for an application environment.
   * @param selVal one of the operating system select values (NON_WINDOWS,
   * WINDOWS_98, WINDOWS_XP or MACINTOSH).
   * @param cmdPrefixStr the preferred string to put before the URL in
   * the launch command, or null for none.
   * @param cmdSuffixStr the preferred string to put after the URL in
   * the launch command, or null for none.
   */
  public LaunchBrowser(int selVal, String cmdPrefixStr, String cmdSuffixStr)
  {
    this(selVal);
                        //setup preferred launch command strings:
    setLaunchCommand(cmdPrefixStr,cmdSuffixStr);
  }

  /**
   * Creates a launch-browser object for an applet environment.
   * @param appletContextObj the 'AppletContext' object to be used.
   */
  public LaunchBrowser(AppletContext appletContextObj)
  {
    this.appletContextObj = appletContextObj;
  }

  /**
   * Creates a launch-browser object for an applet environment.
   * @param appletObj the 'Applet' object containing the 'AppletContext'
   * object to be used.
   */
  public LaunchBrowser(Applet appletObj)
  {
    this.appletContextObj = (appletObj != null) ?
                                        appletObj.getAppletContext() : null;
  }

  /**
   * Selects the operating system for this launch-browser object.  Only
   * used in application environment.
   * @param selVal one of the operating system select values (NON_WINDOWS,
   * WINDOWS_98, WINDOWS_XP or MACINTOSH).
   * @return true if successful, false if invalid selection value.
   */
  public boolean setOSType(int selVal)
  {
    final int [] idxValArr = new int[httpOsPrefixArr.length];
    switch(selVal)
    {    //setup order of OS select indices:
      case NON_WINDOWS:
        idxValArr[0] = NON_WINDOWS;
        idxValArr[1] = WINDOWS_98;
        idxValArr[2] = WINDOWS_XP;
        idxValArr[3] = MACINTOSH;
        break;

      case WINDOWS_98:
        idxValArr[0] = WINDOWS_98;
        idxValArr[1] = WINDOWS_XP;
        idxValArr[2] = NON_WINDOWS;
        idxValArr[3] = MACINTOSH;
        break;

      case WINDOWS_XP:
        idxValArr[0] = WINDOWS_XP;
        idxValArr[1] = WINDOWS_98;
        idxValArr[2] = NON_WINDOWS;
        idxValArr[3] = MACINTOSH;
        break;

      case MACINTOSH:
        idxValArr[0] = MACINTOSH;
        idxValArr[1] = NON_WINDOWS;
        idxValArr[2] = WINDOWS_98;
        idxValArr[3] = WINDOWS_XP;
        break;

      default:
        return false;
    }
    osSelectVal = selVal;         //save OS select value
         //if not yet created then create prefix/suffix arrays (allocate
         // one extra element for possible 'setLaunchCommand()' item):
    if(httpCmdPrefixArr == null)       //non-file URL version
    {
      httpCmdPrefixArr = new String[httpOsPrefixArr.length+1];
                        //put in default for 'setLaunchCommand()' item:
      httpCmdPrefixArr[0] = httpOsPrefixArr[idxValArr[0]];
    }
    if(httpCmdSuffixArr == null)       //non-file URL version
    {
      httpCmdSuffixArr = new String[httpOsPrefixArr.length+1];
                        //put in default for 'setLaunchCommand()' item:
      httpCmdSuffixArr[0] = httpOsSuffixArr[idxValArr[0]];
    }
    for(int i=0; i<httpOsPrefixArr.length; ++i)
    {    //setup each prefix/suffix string
      httpCmdPrefixArr[i+1] = httpOsPrefixArr[idxValArr[i]];
      httpCmdSuffixArr[i+1] = httpOsSuffixArr[idxValArr[i]];
    }
         //if not yet created then create prefix/suffix arrays (allocate
         // one extra element for possible 'setLaunchCommand()' item):
    if(fileCmdPrefixArr == null)       //file URL version
    {
      fileCmdPrefixArr = new String[fileOsPrefixArr.length+1];
                        //put in default for 'setLaunchCommand()' item:
      fileCmdPrefixArr[0] = fileOsPrefixArr[idxValArr[0]];
    }
    if(fileCmdSuffixArr == null)       //file URL version
    {
      fileCmdSuffixArr = new String[fileOsPrefixArr.length+1];
                        //put in default for 'setLaunchCommand()' item:
      fileCmdSuffixArr[0] = fileOsSuffixArr[idxValArr[0]];
    }
    for(int i=0; i<fileOsPrefixArr.length; ++i)
    {    //setup each prefix/suffix string
      fileCmdPrefixArr[i+1] = fileOsPrefixArr[idxValArr[i]];
      fileCmdSuffixArr[i+1] = fileOsSuffixArr[idxValArr[i]];
    }
    return true;
  }

  /**
   * Sets up a preferred command used to launch the browser.  Only
   * used in application environment.  A command suffix is provided
   * to allow characters (such as a quote) to be placed at the end
   * of the launch command.  If both parameters are null then the
   * default launch commands will be used.
   * @param cmdPrefixStr the string put before the URL in the launch
   * command, or null for none.
   * @param cmdSuffixStr the string put after the URL in the launch
   * command, or null for none.
   * @param addSpaceFlag if true and the command prefix string ends with
   * an alphanumeric or underscore character then a space is appended
   * to the prefix.
   * @param httpCmdFlag true to enter the given commands for use with
   * non-file URLs.
   * @param fileCmdFlag true to enter the given commands for use with
   * local-file URLs.
   */
  public void setLaunchCommand(String cmdPrefixStr, String cmdSuffixStr,
             boolean addSpaceFlag, boolean httpCmdFlag, boolean fileCmdFlag)
  {
    if(cmdPrefixStr == null && cmdSuffixStr == null)
    {    //null handles given; setup for default launch commands
      firstCmdArrIdx = 1;         //set first-index value to one
      return;                     //exit method
    }
    if(cmdPrefixStr == null)      //if null string then
      cmdPrefixStr = "";          //change to empty string
    if(cmdSuffixStr == null)      //if null string then
      cmdSuffixStr = "";          //change to empty string
    if(httpCmdPrefixArr == null || httpCmdSuffixArr == null)
    {    //arrays not yet created
      if(!setOSType(NON_WINDOWS))      //create them now
        return;         //if error then abort
    }
    if(addSpaceFlag)
    {    //flag set; add space to prefix if it ends with an alphanumeric
      final int len;
      if((len=cmdPrefixStr.length()) > 0 &&
              Character.isUnicodeIdentifierPart(cmdPrefixStr.charAt(len-1)))
      {  //prefix ends with alphanumeric or underscore
        cmdPrefixStr += " ";      //append space to prefix
      }
    }
    if(httpCmdFlag)
    {    //flag set; save strings for non-file URLs
      httpCmdPrefixArr[0] = cmdPrefixStr;   //set prefix str (non-file URLs)
      httpCmdSuffixArr[0] = cmdSuffixStr;   //set suffix str (non-file URLs)
    }
    if(fileCmdFlag)
    {    //flag set; save strings for file URLs
      fileCmdPrefixArr[0] = cmdPrefixStr;   //set prefix string (file URLs)
      fileCmdSuffixArr[0] = cmdSuffixStr;   //set suffix string (file URLs)
    }
    firstCmdArrIdx = 0;           //set first-index value to zero
  }

  /**
   * Sets up a preferred command used to launch the browser.  Only
   * used in application environment.  A command suffix is provided
   * to allow characters (such as a quote) to be placed at the end
   * of the launch command.  If both parameters are null then the
   * default launch commands will be used.
   * @param cmdPrefixStr the string put before the URL in the launch
   * command, or null for none.
   * @param cmdSuffixStr the string put after the URL in the launch
   * command, or null for none.
   * @param addSpaceFlag if true and the command prefix string ends with
   * an alphanumeric or underscore character then a space is appended
   * to the prefix.
   */
  public void setLaunchCommand(String cmdPrefixStr, String cmdSuffixStr,
                                                       boolean addSpaceFlag)
  {
    setLaunchCommand(cmdPrefixStr,cmdSuffixStr,addSpaceFlag,true,true);
  }

  /**
   * Sets up a preferred command used to launch the browser.  Only
   * used in application environment.  A command suffix is provided
   * to allow characters (such as a quote) to be placed at the end
   * of the launch command.  If both parameters are null then the
   * default launch commands will be used.
   * @param cmdPrefixStr the string put before the URL in the launch
   * command, or null for none.
   * @param cmdSuffixStr the string put after the URL in the launch
   * command, or null for none.
   * @param httpCmdFlag true to enter the given commands for use with
   * non-file URLs.
   * @param fileCmdFlag true to enter the given commands for use with
   * local-file URLs.
   */
  public void setLaunchCommand(String cmdPrefixStr, String cmdSuffixStr,
                                   boolean httpCmdFlag, boolean fileCmdFlag)
  {
    setLaunchCommand(cmdPrefixStr,cmdSuffixStr,true,httpCmdFlag,httpCmdFlag);
  }

  /**
   * Sets up a preferred command used to launch the browser.  Only
   * used in application environment.  A command suffix is provided
   * to allow characters (such as a quote) to be placed at the end
   * of the launch command.  If both parameters are null then the
   * default launch commands will be used.  If the command prefix
   * string ends with an alphanumeric or underscore character then
   * a space is appended to the prefix.
   * @param cmdPrefixStr the string put before the URL in the launch
   * command, or null for none.
   * @param cmdSuffixStr the string put after the URL in the launch
   * command, or null for none.
   */
  public void setLaunchCommand(String cmdPrefixStr, String cmdSuffixStr)
  {
    setLaunchCommand(cmdPrefixStr,cmdSuffixStr,true,true,true);
  }

  /**
   * Sets up a preferred command used to launch the browser.  Only
   * used in application environment.
   * @param cmdPrefixStr the string put before the URL in the launch
   * command, or null to have the default launch commands be used.
   * @param addSpaceFlag if true and the command prefix string ends with
   * an alphanumeric or underscore character then a space is appended
   * to the prefix.
   */
  public void setLaunchCommand(String cmdPrefixStr, boolean addSpaceFlag)
  {
    setLaunchCommand(cmdPrefixStr,null,addSpaceFlag,true,true);
  }

  /**
   * Sets up a preferred command used to launch the browser.  Only
   * used in application environment.  If the command prefix
   * string ends with an alphanumeric or underscore character then
   * a space is appended to the prefix.
   * @param cmdPrefixStr the string put before the URL in the launch
   * command, or null to have the default launch commands be used.
   */
  public void setLaunchCommand(String cmdPrefixStr)
  {
    setLaunchCommand(cmdPrefixStr,null,true,true,true);
  }

  /**
   * Displays the given URL address string in a new browser window.  Only
   * used in application environment.
   * @param urlStr a string containing the URL.
   * @return true if successful; false if error (in which case an error
   * message may be fetched via the 'getErrorMessage()' method).
   */
  public boolean showApplicationURL(String urlStr)
  {
    if(!browserLaunchEnabledFlag)      //if disabled then
      return true;                     //abort method
         //determine if file or non-file URL given and
         // setup to use associated command arrays:
    final String [] cmdPrefixArr,cmdSuffixArr;
    boolean fileUrlFlag = false;
    if(urlStr.length() > 6 &&
                           urlStr.substring(0,6).equalsIgnoreCase("file:/"))
    {    //URL starts with "file:/"; treat as local file
      cmdPrefixArr = fileCmdPrefixArr;
      cmdSuffixArr = fileCmdSuffixArr;
      fileUrlFlag = true;         //indicate starts with "file:/"
    }
    else if(!isURLAddress(urlStr))
    {    //not in URL-address form; treat as local file
      cmdPrefixArr = fileCmdPrefixArr;
      cmdSuffixArr = fileCmdSuffixArr;
    }
    else
    {    //non-file URL
      cmdPrefixArr = httpCmdPrefixArr;
      cmdSuffixArr = httpCmdSuffixArr;
    }
    if(cmdPrefixArr != null && cmdSuffixArr != null)
    {    //arrays of prefix/suffix strings are setup
      final boolean winOSFlag =        //set flag true if Windows OS
                   (osSelectVal == WINDOWS_XP || osSelectVal == WINDOWS_98);
      String prefixStr, cmdStr, escUrlStr = null, errStr = null;
      for(int i=firstCmdArrIdx; i<httpOsPrefixArr.length; ++i)
      {  //for each set of prefix/suffix strings in arrays
        prefixStr = cmdPrefixArr[i];        //get prefix string
              //build command to show URL in browser:
        if(winOSFlag && i > 0 && prefixStr != null &&
                                        (prefixStr.startsWith(CMDCQT_STR) ||
                                         prefixStr.startsWith(STARTQT_STR)))
        {     //Windows OS and default launch command in use
          if(escUrlStr == null)
          {  //"escaped" version of URL string not yet setup
            if(fileUrlFlag)
            {  //URL starts with "file:/"; remove it
              escUrlStr = urlStr.substring(6);
                        //if "file:/" followed by "//" then remove:
              if(escUrlStr.startsWith("//"))
                escUrlStr = escUrlStr.substring(2);
            }
            else   //URL does not start with "file:/"; fix any special chars
              escUrlStr = escSpecialWinChars(urlStr);
          }
                   //build cmd; if leading "file:/" then remove it:
          cmdStr = prefixStr + escUrlStr + cmdSuffixArr[i];
        }
        else  //not Windows OS or not default launch command in use
          cmdStr = prefixStr + urlStr + cmdSuffixArr[i];   //build cmd

        lastShowURLCmdStr = cmdStr;         //save command string
//        System.out.println("DEBUG cmdStr:  " + cmdStr);

        try
        {          //have OS execute the command:
          Runtime.getRuntime().exec(cmdStr);
          return true;       //return with success flag
        }
        catch(Exception ex)
        {        //error occurred; set error message (if first one)
          if(errStr == null)
            errStr = "Runtime.exec('" + cmdStr + "') exception:  " + ex;
        }
      }
      setErrorMessage(errStr);    //set (first) error message
    }
    else      //arrays of prefix/suffix strings not setup; set error message
      setErrorMessage("Invalid application configuration");
    return false;
  }

  /**
   * Returns the "preferred" command that would be used to launch a
   * browser for viewing a non-local-file URL in an application
   * environment.  Note that the 'showURL()' method may also revert
   * to using a default launch command if the preferred command fails.
   * @param urlStr the URL string to use in the command.
   * @return The launch-browser command.
   */
  public String getPrefApplicationLaunchCmd(String urlStr)
  {
    return httpCmdPrefixArr[firstCmdArrIdx] + urlStr +
                                           httpCmdSuffixArr[firstCmdArrIdx];
  }

  /**
   * Returns the "preferred" command that would be used to launch a
   * browser for viewing a local file in an application environment.
   * Note that the 'showURL()' method may also revert to using a default
   * launch command if the preferred command fails.
   * @param urlStr the URL string to use in the command.
   * @return The launch-browser command.
   */
  public String getPrefApplicationLaunchFileCmd(String urlStr)
  {
    return fileCmdPrefixArr[firstCmdArrIdx] + urlStr +
                                           fileCmdSuffixArr[firstCmdArrIdx];
  }

  /**
   * Returns the last command string used by the 'showURL()' (application
   * mode) or 'showApplicationURL()' method.
   * @return The last command string used by the 'showURL()' (application
   * mode) or 'showApplicationURL()' method, or null if none available.
   */
  public String getLastShowURLCmdStr()
  {
    return lastShowURLCmdStr;
  }

  /**
   * Displays the given URL address string in a separate browser window.
   * @param urlStr a string containing the URL.
   * @param titleStr a string used to select a target browser window.
   * @return true if successful; false if error (in which case an error
   * message may be fetched via the 'getErrorMessage()' method).
   */
  public boolean showAppletURL(String urlStr, String titleStr)
  {
    if(!browserLaunchEnabledFlag)      //if disabled then
      return true;                     //abort method
    if(appletContextObj != null)
    {         //applet context is OK
      final URL urlObj;
      try
      {            //attempt to create URL object out of address string:
        urlObj = new URL(urlStr);
        final int len;
        if(titleStr != null && (len=titleStr.length()) > 0)
        {     //valid title string was given
                   //replace any non-alphanumeric chars in the title
                   // string to keep Internet Explorer (v5) happy:
          final StringBuffer buff = new StringBuffer(titleStr);
          boolean changedFlag = false;
          for(int p=0; p<len; ++p)
          {   //for each character in string
            if(!Character.isLetterOrDigit(buff.charAt(p)))
            {      //character is not letter or digit
              buff.setCharAt(p,'_');        //replace with underscore
              changedFlag = true;           //indicate change made
            }
          }
          if(changedFlag)                   //if data was changed then
            titleStr = buff.toString();     //change buffer to string
        }
        else       //no valid title string was given
          titleStr = "_blank";    //put in default title string
//        if(showDebugFlag)
//        {     //if show-debug enabled then show command
//          System.out.println("Executing:  appletContextObj.showDocument(" +
//                                            urlObj + ", " + titleStr + ")");
//        }
                   //send to document to a browser window:
        appletContextObj.showDocument(urlObj,titleStr);
        return true;         //return with success flag
      }
      catch(MalformedURLException ex)
      {            //URL format error; show error message
        setErrorMessage("Unable to interpret URL:  " + urlStr);
      }
      catch(Exception ex)
      {            //some other error; show error message
        setErrorMessage("Unable to display URL:  " + urlStr);
      }
    }
    else      //no applet context
      setErrorMessage("Invalid applet context (null)");
    return false;
  }

  /**
   * Displays the given URL address string in a browser window.
   * @param urlStr a string containing the URL.
   * @return true if successful; false if error (in which case an error
   * message may be fetched via the 'getErrorMessage()' method).
   */
  public boolean showAppletURL(String urlStr)
  {
    return showAppletURL(urlStr,null);
  }

  /**
   * Displays the given URL address string in a browser window.  This
   * method calls either the 'showAppletURL()' or 'showApplicationURL()'
   * method, depending on how this object was constructed.
   * @param urlStr a string containing the URL.
   * @param titleStr a string used to select a target browser window.
   * @return true if successful; false if error (in which case an error
   * message may be fetched via the 'getErrorMessage()' method).
   */
  public boolean showURL(String urlStr, String titleStr)
  {
    return (appletContextObj != null) ? showAppletURL(urlStr,titleStr) :
                                                 showApplicationURL(urlStr);
  }

  /**
   * Displays the given URL address string in a browser window.  This
   * method calls either the 'showAppletURL()' or 'showApplicationURL()'
   * method, depending on how this object was constructed.
   * @param urlStr a string containing the URL.
   * @return true if successful; false if error (in which case an error
   * message may be fetched via the 'getErrorMessage()' method).
   */
  public boolean showURL(String urlStr)
  {
    clearErrorMessage();          //clear any previous error message
    return (appletContextObj != null) ? showAppletURL(urlStr,null) :
                                                 showApplicationURL(urlStr);
  }

//  /**
//   * Displays the given URL address string in a browser window.  If the
//   * URL target is not immediately available then a separate thread is
//   * launched that waits for the URL target to become available, up to
//   * the specified 'timeOutMs' time, and then launches the browser window.
//   * If the URL target never becomes available then the 'callBackObj'
//   * method is invoked.  This method calls either the 'showAppletURL()'
//   * or 'showApplicationURL()' method, depending on how this object was
//   * constructed.
//   * @param urlStr a string containing the URL.
//   * @param titleStr a string used to select a target browser window.
//   * @param timeOutMs maximum time to wait (in ms), or 0 for indefinite.
//   * @param checkIntvlMs delay time between checks (in ms).
//   * @param callBackObj call-back object to be invoked if the URL target
//   * does not become available before the timeout, or null for none.
//   * @return true if successful; false if error (in which case an error
//   * message may be fetched via the 'getErrorMessage()' method).
//   */
//  public boolean showURL(final String urlStr, final String titleStr,
//                                final int timeOutMs, final int checkIntvlMs,
//                                          final CallBackNoParam callBackObj)
//  {
//    final URL urlObj;
//    try
//    {              //convert URL string to object:
//      urlObj = new URL(urlStr);
//    }
//    catch(MalformedURLException ex)
//    {  //bad URL; set error message and abort method
//      setErrorMessage("Malformed URL:  " + urlStr);
//      return false;
//    }
//    if(checkResponseCodeOK(urlObj))         //if URL immediately available
//      return showURL(urlStr,titleStr);      // then do launch now
//         //create and start thread to check/wait and then do launch:
//    (new Thread("launchBrowserShowUrl")
//        {
//          public void run()
//          {                  //wait for URL to be available:
//            if(waitForURLAvailable(urlObj,timeOutMs,checkIntvlMs))
//              showURL(urlStr,titleStr);     //if URL available then do launch
//            else if(callBackObj != null)    //invoke call-back if given
//              callBackObj.callBackMethod();
//          }
//        }).start();
//    return true;
//  }
//
//  /**
//   * Displays the given URL address string in a browser window.  If the
//   * URL target is not immediately available then a separate thread is
//   * launched that waits for the URL target to become available, up to
//   * the specified 'timeOutMs' time, and then launches the browser window.
//   * If the URL target never becomes available then the 'callBackObj'
//   * method is invoked.  This method calls either the 'showAppletURL()'
//   * or 'showApplicationURL()' method, depending on how this object was
//   * constructed.
//   * @param urlStr a string containing the URL.
//   * @param timeOutMs maximum time to wait (in ms), or 0 for indefinite.
//   * @param checkIntvlMs delay time between checks (in ms).
//   * @param callBackObj call-back object to be invoked if the URL target
//   * does not become available before the timeout, or null for none.
//   * @return true if successful; false if error (in which case an error
//   * message may be fetched via the 'getErrorMessage()' method).
//   */
//  public boolean showURL(final String urlStr, final int timeOutMs,
//                  final int checkIntvlMs, final CallBackNoParam callBackObj)
//  {
//    return showURL(urlStr,null,timeOutMs,checkIntvlMs,callBackObj);
//  }
//
//  /**
//   * Displays the given URL address string in a browser window.  If the
//   * URL target is not immediately available then a separate thread is
//   * launched that waits for the URL target to become available, up to
//   * the specified 'timeOutMs' time, and then launches the browser window.
//   * A default check-interval delay of of 5000ms is used.
//   * If the URL target never becomes available then the 'callBackObj'
//   * method is invoked.  This method calls either the 'showAppletURL()'
//   * or 'showApplicationURL()' method, depending on how this object was
//   * constructed.
//   * @param urlStr a string containing the URL.
//   * @param timeOutMs maximum time to wait (in ms), or 0 for indefinite.
//   * @param callBackObj call-back object to be invoked if the URL target
//   * does not become available before the timeout, or null for none.
//   * @return true if successful; false if error (in which case an error
//   * message may be fetched via the 'getErrorMessage()' method).
//   */
//  public boolean showURL(final String urlStr, final int timeOutMs,
//                                          final CallBackNoParam callBackObj)
//  {
//    return showURL(urlStr,null,timeOutMs,5000,callBackObj);
//  }

  /**
   * Enables or disables the launching of browser windows.  The default
   * state is 'enabled'.
   * @param flgVal true to enable, false to disable.
   */
  public void setEnabled(boolean flgVal)
  {
    browserLaunchEnabledFlag = flgVal;
  }

  /**
   * Returns an indicator of whether or not the launching of browser windows
   * is enabled.
   * @return true if enabled, false if disabled.
   */
  public boolean isEnabled()
  {
    return browserLaunchEnabledFlag;
  }

  /**
   * Enters error message.
   * @param str error message string.
   */
  protected void setErrorMessage(String str)
  {
    if(errorMessage == null)      //if no previous error then
      errorMessage = str;         //set error message
  }

  /**
   * Returns true if an error was detected.  The error message may be
   * fetched via the 'getErrorMessage()' method.
   * @return true if an error was detected.
   */
  public boolean getErrorFlag()
  {
    return (errorMessage != null);
  }

  /**
   * Returns message string for last error (or 'No error' if none).
   * @return message string for last error (or 'No error' if none).
   */
  public String getErrorMessage()
  {
    return (errorMessage != null) ? errorMessage : "No error";
  }

  /** Clears the error message string. */
  public void clearErrorMessage()
  {
    errorMessage = null;
  }

  /**
   * Returns the operating system select value for this launch browser
   * object (NON_WINDOWS, WINDOWS_98, WINDOWS_XP or MACINTOSH).
   * @return the operating system select value for this launch browser
   * object (NON_WINDOWS, WINDOWS_98, WINDOWS_XP or MACINTOSH).
   */
  public int getOSSelectVal()
  {
    return osSelectVal;
  }

  /**
   * Returns a description string for the operating system select value
   * for this launch browser object ("Non-Windows", "Windows95/98",
   * "Windows2000/XP" or "Macintosh").
   * @return a description string for the operating system select value
   * for this launch browser object ("Non-Windows", "Windows95/98",
   * "Windows2000/XP" or "Macintosh").
   */
  public String getOSSelectString()
  {
    return (osSelectVal >= 0 && osSelectVal < osSelStringsArr.length) ?
                                   osSelStringsArr[osSelectVal] : "Unknown";
  }

  /**
   * Returns the Operating System name found in the "os.name" system
   * property.
   * @return The name of the current operating system, or an empty
   * string if the name could not be fetched.
   */
  public String getOsNamePropString()
  {
    if(osNamePropString == null)
      determineOSType();
    return osNamePropString;
  }

  /**
   * Determines the type of operating system in use.
   * @return one of the operating system select values (NON_WINDOWS,
   * WINDOWS_98, WINDOWS_XP or MACINTOSH).
   */
  public static int determineOSType()
  {
    osNamePropString = "";        //initialize value
    try
    {
      osNamePropString = System.getProperty("os.name","");
      final String osNameLwrCase;
      if((osNameLwrCase=osNamePropString.toLowerCase()).indexOf(
                                                            "windows") >= 0)
      {  //windows OS; determine which type and return value
        return (osNamePropString.indexOf("windows 98") >= 0) ? WINDOWS_98 :
                                                                 WINDOWS_XP;
      }
      if(osNameLwrCase.startsWith("mac"))        //support for Macintosh
        return MACINTOSH;
    }
    catch(Exception ex) {}
    return NON_WINDOWS;
  }

  /**
   * Returns true if the contents of the given string qualify as a valid
   * URL address; otherwise returns false.  If the string begins with 1
   * to 10 letters (or digits) followed by "://" then it is considered
   * a valid URL address.
   * @param str the string to check.
   * @return true if the contents of the given string qualify as a valid
   * URL address; otherwise false.
   */
  public static boolean isURLAddress(String str)
  {
    if(str == null || str.length() <= 0)
      return false;          //if no string data then return false
    int p;         //find position of "://" in string:
    if((p=str.indexOf("://",1)) <= 0 || p > 10)
      return false;          //if not found then return false
    while(--p >= 0)          //scan backward through leading characters
    {    //for each character before colon
      if(!Character.isLetterOrDigit(str.charAt(p)))
        return false;        //if not a letter or digit then return false
    }
    return true;        //return true for valid URL address
  }

  /**
   * Scans the given URL string for special characters ("&|()^") and
   * "escapes" them be preceding each one with the carrot ("^") character.
   * These special characters need to be "escaped" when a parameter is
   * sent to a Windows command interpreter.
   * @param urlStr string to scan.
   * @return The given string with special characters "escaped", or the
   * original given string if no special characters were found.
   */
  public static String escSpecialWinChars(String urlStr)
  {
    final int urlStrLen = urlStr.length();
    StringBuffer buff = null;
    char ch;
    for(int p=0; p<urlStrLen; ++p)
    {    //for each character in URL string
      if(NEED_ESC_CHARS.indexOf(ch=urlStr.charAt(p)) >= 0)
      {       //character is one that needs to be "escaped"
        if(buff == null)     //if buffer not created then create and fill
          buff = new StringBuffer(urlStr.substring(0,p));
        buff.append('^');         //insert in "escape" character
        buff.append(ch);          //put in special character
      }
      else if(buff != null)       //character does not need to be "escaped"
        buff.append(ch);          //if buffer created then append char
    }
         //if chars "escaped" then return new URL string:
    return (buff != null) ? buff.toString() : urlStr;
  }
  
  /**
   * Returns the status code from an HTTP response message.
   * @param urlConnObj URL-connection object to use.
   * @return An HTTP Status-Code, or -1 if none available.
   */
  public static int getResponseCode(URLConnection urlConnObj)
  {
    try
    {
      if(urlConnObj instanceof HttpURLConnection)
        return ((HttpURLConnection)urlConnObj).getResponseCode();
              //not HTTP-connection object; try opening stream to target:
      final InputStream stmObj = urlConnObj.getInputStream();
      stmObj.close();
      return HttpURLConnection.HTTP_OK;     //open worked; return 200
    }
    catch(Exception ex)
    {  //some kind of exception error; return -1 below
    }
    return -1;
  }
  
  /**
   * Returns the status code from an HTTP response message.
   * @param urlObj URL object to use.
   * @return An HTTP Status-Code, or -1 if none available.
   */
  public static int getResponseCode(URL urlObj)
  {
    try
    {
      return getResponseCode(urlObj.openConnection());
    }
    catch(Exception ex)
    {  //some kind of exception error; return -1 below
    }
    return -1;
  }

  /**
   * Determines if the given response code corresponds to the HTTP_OK code.
   * @param codeVal response code to check.
   * @return true if the given response code corresponds to the HTTP_OK code.
   */
  public static boolean checkResponseCodeOK(int codeVal)
  {
    return (codeVal == HttpURLConnection.HTTP_OK);
  }

  /**
   * Determines if the response code fetched for the given URLConnection
   * corresponds to the HTTP_OK code.
   * @param urlConnObj URL-connection object to use.
   * @return true if the response code corresponds to the HTTP_OK code.
   */
  public static boolean checkResponseCodeOK(URLConnection urlConnObj)
  {
    return (getResponseCode(urlConnObj) == HttpURLConnection.HTTP_OK);
  }

  /**
   * Determines if the response code fetched for the given URL
   * corresponds to the HTTP_OK code.
   * @param urlObj URL object to use.
   * @return true if the response code corresponds to the HTTP_OK code.
   */
  public static boolean checkResponseCodeOK(URL urlObj)
  {
    return (getResponseCode(urlObj) == HttpURLConnection.HTTP_OK);
  }
  
  /**
   * Opens a connection to the given URL.  Returns null if an I/O exception
   * occurs.
   * @param urlObj URL object for the given URL.
   * @return A new URLConnection object, or null if an exception occurred.
   */
  public static URLConnection openConnection(URL urlObj)
  {
    try
    {
      return urlObj.openConnection();
    }
    catch(Exception ex)
    {
      return null;
    }
  }
  
  /**
   * Waits for the specified URL to become available.
   * @param urlObj URL object for URL.
   * @param timeOutMs maximum time to wait (in ms), or 0 for indefinite.
   * @param checkIntvlMs delay time between checks (in ms).
   * @return true if the URL is available; false if not.
   */
  public static boolean waitForURLAvailable(URL urlObj,
                                            int timeOutMs, int checkIntvlMs)
  {
    URLConnection urlConnObj;
    if((urlConnObj=openConnection(urlObj)) != null)
    {  //URL-connection is setup OK
      if(checkResponseCodeOK(urlConnObj))        //if URL available then
        return true;                             //return flag
    }
    final long startTimeMs = System.currentTimeMillis();
    while(true)
    {  //loop while waiting for URL available
      try
      {            //delay between checks:
        Thread.sleep(checkIntvlMs);
      }
      catch(InterruptedException ex)
      {            //if thread interrupted then exit method
        break;
      }
      if((urlConnObj=openConnection(urlObj)) != null)
      {  //URL-connection is setup OK
        if(checkResponseCodeOK(urlConnObj))      //if URL available then
          return true;                           //return flag
      }
      if(timeOutMs > 0 &&
                       System.currentTimeMillis() - startTimeMs > timeOutMs)
      {  //timeout enabled and enough time has passed
        break;     //exit method
      }
    }
    return false;
  }


//  /** Test program. */
//  public static void main(String [] args)
//  {
//    final LaunchBrowser launchObj = new LaunchBrowser();
//    System.out.println(launchObj.getOSSelectString() +
//                                         "-type operating system detected");
//    if(!launchObj.showURL("http://www.isti.com/testNotFound",60000,
//        new CallBackNoParam()
//            {
//              public void callBackMethod()
//              {
//                System.err.println("URL target not available");
//              }
//            }))
//    {
//      System.err.println("Error showing URL:  " +
//                                               launchObj.getErrorMessage());
//    }
//  }

//  /** Test program. */
//  public static void main(String [] args)
//  {
////    final LaunchBrowser launchObj = new LaunchBrowser("\"C:\\Program Files" +
////       "\\Netscape\\Communicator\\Program\\netscape.exe\" -browser \"","\"");
//    final LaunchBrowser launchObj = new LaunchBrowser();
//    System.out.println(launchObj.getOSSelectString() +
//                                         "-type operating system detected");
//    if(!launchObj.showURL("http://www.isti.com"))
//    {
//      System.err.println("Error showing URL:  " +
//                                               launchObj.getErrorMessage());
//    }
//  }
}
