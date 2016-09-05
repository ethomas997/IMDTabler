//FreqSetGenServlet.java:  Servlet interface for IMDTabler.
//
//   9/4/2016 -- [ET]
//

package com.etheli.imdtabler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
/**
 * Class FreqSetGenServlet is a servlet interface for the FreqSetGen utility.
 */
public class FreqSetGenServlet extends HttpServlet
{
  public static final int MAXVAL_RUN_TIMESECS = 600;
  public static final String SERVLET_NAME_STR = "FreqSetGen";
  public static final String CATALINA_PROP_STR = "catalina.base";
  public static final String WEBAPPS_DIR_STR = "webapps";
  public static final String WEBINF_DIR_STR = "WEB-INF";
  public static final String ROOT_DIR_STR = "ROOT";
  public static final String numberFreqInSetTag = "numberFreqInSet";
  public static final String possibleFreqSetTag = "possibleFreqSet";
  public static final String mandatoryFreqSetTag = "mandatoryFreqSet";
  public static final String minFreqSeparationTag = "minFreqSeparation";
  public static final String maxRunTimeSecsTag = "maxRunTimeSecs";
  public static final String viewResultFileTag = "viewResultFile";
  public static final String viewResultDirTag = "viewResultDir";
  private static final long serialVersionUID = 5471499176035995524L;


  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
                                        throws IOException, ServletException
  {
    // Set the response message's MIME type
    response.setContentType("text/html;charset=UTF-8");
    // Allocate a output writer to write the response message into the network socket
    PrintWriter out = response.getWriter();

    // Write the response message, in an HTML page
    try
    {
         //handle any "viewResultFile" queries:
      String viewResultFileStr = request.getParameter(viewResultFileTag);
      viewResultFileStr = (viewResultFileStr != null) ?
                                              viewResultFileStr.trim() : "";
      String viewResultDirStr = request.getParameter(viewResultDirTag);
      viewResultDirStr = (viewResultDirStr != null) ?
                                               viewResultDirStr.trim() : "";
      if(viewResultFileStr.length() > 0)
      {
        try
        {
//          out.println("<br>viewResultFileStr='" + viewResultFileStr +
//                          "', viewResultDirStr='" + viewResultDirStr + "'");
              //convert given parameters to results-file pathname:
          final char SEPCH = File.separatorChar;
          String tomcatBaseDir = System.getProperty(CATALINA_PROP_STR);
          if(tomcatBaseDir == null || tomcatBaseDir.trim().length() <= 0)
            tomcatBaseDir = "C:" + SEPCH + "JavaApps" + SEPCH + "Tomcat";
          final String outputPathameStr = tomcatBaseDir + SEPCH +
                            WEBAPPS_DIR_STR + SEPCH + ROOT_DIR_STR + SEPCH +
                                       FreqSetGen.generateOutDirFileNameStr(
                                        viewResultDirStr,viewResultFileStr);
              //read results file (with retries) and output data:
          out.print(readFileToBuffWRetry(outputPathameStr,1048577,
                                                                "</html>"));
        }
        catch(Throwable ex)
        {
          ex.printStackTrace(out);
        }
        return;
      }

      out.println("<!DOCTYPE html>");
      out.println("<html><head>");

      String numberFreqInSetStr = request.getParameter(numberFreqInSetTag);
      numberFreqInSetStr = (numberFreqInSetStr != null) ?
                                          numberFreqInSetStr.trim() : "";
      String possibleFreqSetStr = request.getParameter(possibleFreqSetTag);
      possibleFreqSetStr = (possibleFreqSetStr != null) ?
                                          possibleFreqSetStr.trim() : "";
      String mandatoryFreqSetStr = request.getParameter(mandatoryFreqSetTag);
      mandatoryFreqSetStr = (mandatoryFreqSetStr != null) ?
                                         mandatoryFreqSetStr.trim() : "";
      String minFreqSeparationStr = request.getParameter(minFreqSeparationTag);
      minFreqSeparationStr = (minFreqSeparationStr != null) ?
                                        minFreqSeparationStr.trim() : "";
      String maxRunTimeSecsStr = request.getParameter(maxRunTimeSecsTag);
      maxRunTimeSecsStr = (maxRunTimeSecsStr != null) ?
                                           maxRunTimeSecsStr.trim() : "";

      String outputPathameStr = "";
      int numberFreqInSet = 0;
      int [] possibleFreqSetArr = null;
      int [] mandatoryFreqSetArr = null;
      int minFreqSeparation = 0;
      int maxRunTimeSecs = 0;

      String outErrorMessageStr = null;
      if(numberFreqInSetStr.length() > 0 || possibleFreqSetStr.length() > 0)
      {  //at least one of the first two input fields contains data
        try
        {
          if(numberFreqInSetStr.length() > 0 && possibleFreqSetStr.length() > 0)
          {  //both of the first two input fields contain data
            final char SEPCH = File.separatorChar;
            String tomcatBaseDir = System.getProperty(CATALINA_PROP_STR);
            if(tomcatBaseDir == null || tomcatBaseDir.trim().length() <= 0)
              tomcatBaseDir = "C:" + SEPCH + "JavaApps" + SEPCH + "Tomcat";
            final String outDirFileNameStr =
                                     FreqSetGen.generateOutDirFileNameStr();
            outputPathameStr = tomcatBaseDir + SEPCH + WEBAPPS_DIR_STR +
                           SEPCH + ROOT_DIR_STR + SEPCH + outDirFileNameStr;

            numberFreqInSet = Integer.parseInt(numberFreqInSetStr);
            possibleFreqSetArr =
                               IMDTabler.stringToIntArr(possibleFreqSetStr);
            mandatoryFreqSetArr =
                              IMDTabler.stringToIntArr(mandatoryFreqSetStr);
            minFreqSeparation = Integer.parseInt(minFreqSeparationStr);
            maxRunTimeSecs = Integer.parseInt(maxRunTimeSecsStr);
            if(maxRunTimeSecs > 0 && maxRunTimeSecs <= MAXVAL_RUN_TIMESECS)
            {
              FreqSetGen.makeDirsForPathname(outputPathameStr);
                   //path to class files in servlet context:
              final String classesDirStr = tomcatBaseDir + SEPCH +
                                WEBAPPS_DIR_STR + SEPCH + SERVLET_NAME_STR +
                                 SEPCH + WEBINF_DIR_STR + SEPCH + "classes";
                   //build command string to launch FreqSetGen process
                   // (set class name as window title so it will show when
                   //  "tasklist /V" command used to check # of instances):
              final String cmdStr = "cmd.exe /C start \"" +
                          FreqSetGen.class.getName() + "\" /MIN java -cp " +
                  classesDirStr + ' ' + FreqSetGen.class.getName() + " \"" +
                        outputPathameStr + "\" " + numberFreqInSet + " \"" +
                   FreqSetGen.intArrToString(possibleFreqSetArr) + "\" \"" +
                    FreqSetGen.intArrToString(mandatoryFreqSetArr) + "\" " +
                                  minFreqSeparation + ' ' + maxRunTimeSecs +
                                      ' ' + FreqSetGen.CHECK_INSTANCES_STR +
                                              ' ' + request.getRemoteAddr();
                   //launch FreqSetGen process:
              FreqSetGen.execCmdNoResp(cmdStr);
                   //wait for results file to be available:
              final File outFileObj = new File(outputPathameStr);
              int cnt = 0;
              do
              {
                if(++cnt > 200)
                  break;
                try { Thread.sleep(25); }
                catch(InterruptedException ex) {}
              }
              while(!outFileObj.exists());
              
                   //build URL for results page:
              String urlStr = request.getRequestURL().toString();
              int p;

//                   //build redirect URL to output file:
//              if((p=urlStr.indexOf(SERVLET_NAME_STR)) < 0)
//                p = urlStr.length();
//              urlStr = urlStr.substring(0,p) +
//                                        outDirFileNameStr.replace('\\','/');

                   //build redirect URL with view-file and view-dir values:
              if((p=urlStr.indexOf('?')) < 0)
                p = urlStr.length();
              urlStr = urlStr.substring(0,p) + '?' + viewResultFileTag + '=' +
                  FreqSetGen.getViewFileFromOutDirFileStr(outDirFileNameStr) +
                                                '&' + viewResultDirTag + '=' +
                    FreqSetGen.getViewDirFromOutDirFileStr(outDirFileNameStr);
              urlStr = urlStr.replace(' ','+');  //make sure no spaces

                   //redirect to results page:
              out.println("<meta http-equiv=\"REFRESH\" content=\"0;url=" +
                                                          urlStr + "\" />");
            }
            else
            {
              outErrorMessageStr = "'Maximum run time' must be from 1 to " +
                                                        MAXVAL_RUN_TIMESECS;
            }
          }
          else
          {
            if(numberFreqInSetStr.length() <= 0)
              outErrorMessageStr = "'Number of frequencies' value cannot be blank";
            else if(possibleFreqSetStr.length() <= 0)
              outErrorMessageStr = "'Possible frequencies' set cannot be empty";
          }
        }
        catch(NumberFormatException ex)
        {
          outErrorMessageStr = "Error parsing entered values:  " +
                                                 ex.getMessage() + "<br />";
        }
        catch(Exception ex)
        {
          outErrorMessageStr = "Error processing data:  " + ex + "<br />";
        }
      }
      
      out.println("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
      out.println("<title>ET's FreqSetGen</title></head>");
      out.println("<body>");
      out.println("<h3>ET's FreqSetGen - Frequency Set Generator</h3>");

      out.println("Links:&nbsp; <a href=\"http://www.etheli.com/IMD\" target=\"_blank\">IMDTools</a>, " +
          "<a href=\"http://etserv.etheli.com/IMDTabler/run\" target=\"_blank\">IMDTabler</a><br />");
      out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;");
      out.println("&nbsp;&nbsp;&nbsp; 1&nbsp;&nbsp; &nbsp; &nbsp; 2&nbsp; &nbsp;");
      out.println("&nbsp;&nbsp; 3&nbsp; &nbsp; &nbsp;&nbsp; 4&nbsp;&nbsp;&nbsp;");
      out.println("&nbsp;&nbsp; 5&nbsp; &nbsp; &nbsp;&nbsp; 6&nbsp; &nbsp; &nbsp;&nbsp;");
      out.println("7&nbsp; &nbsp; &nbsp;&nbsp; 8<br />");
      out.println("Band F: 5740 5760 5780 5800 5820 5840 5860 5880  (IRC NexWave / Fatshark)<br />");
      out.println("Band E: 5705 5685 5665 5645 5885 5905 5925 5945  (Boscam E / DJI)<br />");
      out.println("Band B: 5733 5752 5771 5790 5809 5828 5847 5866  (Boscam B)<br />");
      out.println("Band A: 5865 5845 5825 5805 5785 5765 5745 5725  (Boscam A / TBS / RC305)<br />");
      out.println("Band R:  5658 5695 5732 5769 5806 5843 5880 5917  (Raceband)<br />");
      out.println("<br />");

      // Return the existing session if there is one. Otherwise, create a new session
//         HttpSession session = request.getSession();

      // Display session information
//         out.println("sessions.id" + " " + session.getId() + "<br />");
//         out.println("sessions.created" + " ");
//         out.println(new Date(session.getCreationTime()) + "<br />");
//         out.println("sessions.lastaccessed" + " ");
//         out.println(new Date(session.getLastAccessedTime()) + "<br /><br />");

      // Set an attribute (name-value pair) if present in the request
//         String attName = request.getParameter("attribute_name");
//         if (attName != null) attName = attName.trim();
//         String attValue = request.getParameter("attribute_value");
//         if (attValue != null) attValue = attValue.trim();
//         if (attName != null && !attName.equals("")
//               && attValue != null && !attValue.equals("") )
//         {
//            // synchronized session object to prevent concurrent update
//            synchronized(session)
//            {
//               session.setAttribute(attName, attValue);
//            }
//         }

      // Display the attributes (name-value pairs) stored in this session
//         out.println("sessions.data" + "<br>");
//         Enumeration names = session.getAttributeNames();
//         while (names.hasMoreElements())
//         {
//             String name = (String) names.nextElement();
//             String value = session.getAttribute(name).toString();
//             out.println(HtmlFilter.filter(name) + " = "
//                   + HtmlFilter.filter(value) + "<br>");
//         }
//         out.println("<br />");


      // Display a form to prompt user to create session attribute
      out.println("<form method='get'>");

      out.println("Number of frequencies: ");
      out.println("<input type='text' name='" + numberFreqInSetTag +
                         "' size='3' " + "value='" + numberFreqInSetStr +
                                  "' onfocus='this.select()' autofocus" +
                " title='Number of frequencies in each generated set'>");
      out.println("<br>Possible frequencies: ");
      out.println("<input type='text' name='" + possibleFreqSetTag +
                        "' size='80' " + "value='" + possibleFreqSetStr +
                                            "' onfocus='this.select()'" +
                " title='List of possible frequencies to choose from'>");
      out.println("<br>Mandatory frequencies: ");
      out.println("<input type='text' name='" + mandatoryFreqSetTag +
                       "' size='40' " + "value='" + mandatoryFreqSetStr +
                                            "' onfocus='this.select()'" +
               " title='Optional list of frequencies that must appear " +
                                              "in all generated sets'>");
      out.println("<br>Minimum separation: ");
      out.println("<input type='text' name='" + minFreqSeparationTag +
                                              "' size='3' " + "value='" +
              ((minFreqSeparationStr.length()>0) ? minFreqSeparationStr :
                         Integer.toString(FreqSetGen.DEF_MIN_FREQ_SEP)) +
                                            "' onfocus='this.select()'" +
                          " title='Minimum allowed separation between " +
                                      "frequencies in generated sets'>");
      out.println("<br>Maximum run time: ");
      out.print("<input type='text' name='" + maxRunTimeSecsTag +
                                              "' size='3' " + "value='" +
                    ((maxRunTimeSecsStr.length()>0) ? maxRunTimeSecsStr :
                      Integer.toString(FreqSetGen.DEF_MAX_RUN_TIMESEC)) +
                                            "' onfocus='this.select()'" +
                                 " title='Maximum time allowed (up to " +
                                    MAXVAL_RUN_TIMESECS + " seconds)'>");
      out.println(" seconds<br>");

      out.println("<input type='submit' value='Run'>");
      out.println("</form>");


      if(numberFreqInSetStr.length() > 0 || possibleFreqSetStr.length() > 0)
      {
        if(outErrorMessageStr != null)
          out.println("<br>" + outErrorMessageStr + "<br>");
        else
        {
          out.println("<br>Launching process...<br>");
//          out.println("<br>");
//          out.println("outFileNameStr = '" + outFileNameStr + "'<br>");
//          out.println("numberFreqInSet = " + numberFreqInSet + "<br>");
//          out.println("possibleFreqSet:  " +
//                  FreqSetGen.intArrToString(possibleFreqSetArr) + "<br>");
//          out.println("mandatoryFreqSet:  " +
//                 FreqSetGen.intArrToString(mandatoryFreqSetArr) + "<br>");
//          out.println("minFreqSeparation = " + minFreqSeparation + "<br>");
//          out.println("maxRunTimeSecs = " + maxRunTimeSecs + "<br>");
//            out.println("<br>" + System.getProperties().toString().replace('{','\n').replace('}','\n').replace(", ","<br>"));
//          out.println("<br>");
        }
      }


//         out.print("<a href='");
//         // Encode URL by including the session ID (URL-rewriting)
//         out.print(response.encodeURL(request.getRequestURI() + "?attribute_name=foo&attribute_value=bar"));
//         out.println("'>Encode URL with session ID (URL re-writing)</a>");
//         out.println("</body></html>");
      
      out.println("<br />" + FreqSetGen.PROG_SHORT_TITLESTR +
                           " Version " + FreqSetGen.VERSION_STR + "<br />");
      out.println("<hr /><br />");
      out.println("<a href=\"http://www.etheli.com/freq/FPV_5.8GHz_Freqs.jpg\"");
      out.println("target=\"_blank\">5.8GHz FPV \"Visual\" Frequency Chart</a><br /><br />");
      out.println("Click <a href=\"http://www.etheli.com/contact/index.html\">here to contact me</a><br /><br />");
      out.println("<a href=\"http://www.etheli.com/IMD\">IMD Tools page</a><br /><br />");
      out.println("<a href=\"http://www.etheli.com\">etheli.com home page</a>");
      out.println("</body>");
      out.println("</html>");
       
    }
    finally
    {
       out.close();  // Always close the output writer
    }
  }

  // Do the same thing for GET and POST requests
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException
  {
     doGet(request, response);
  }

  /**
   * Reads data from the given file to the given buffer, with retry after
   * error or no data found.
   * @param fNameStr name of file.
   * @param maxFileSize maximum file size allowed, in bytes.
   * @param checkStr string that received data must contain to be
   * considered a successful read.
   * @return data read from file, or an error message.
   */
  private static String readFileToBuffWRetry(String fNameStr,
                                          long maxFileSize, String checkStr)
  {
    int cnt = 0;
    final StringBuffer buff = new StringBuffer();
    do
    {
      if((new File(fNameStr)).length() < maxFileSize &&
                       readFileToBuffer(fNameStr,buff) && buff.length() > 0)
      {  //file not too large, read OK, and data found
        final String retStr = buff.toString();
        if(checkStr == null || checkStr.length() <= 0 ||
                                              retStr.indexOf(checkStr) >= 0)
        {
          return retStr;
        }
      }
      try { Thread.sleep(100); }
      catch(InterruptedException ex) {}
      buff.delete(0,buff.length());
    }
    while(++cnt < 30);
    return "Error:  Unable to read results data";
  }

  /**
   * Reads data from the given file to the given buffer.
   * @param fNameStr name of file.
   * @param buff buffer to fill.
   * @return true if successful; false if error.
   */
  private static boolean readFileToBuffer(String fNameStr, StringBuffer buff)
  {
    BufferedReader rdrObj = null;
    try
    {
      rdrObj = new BufferedReader(new FileReader(fNameStr));
      String str;
      while((str=rdrObj.readLine()) != null)
        buff.append(str + '\n');
      rdrObj.close();
      return true;
    }
    catch(Exception ex)
    {
      if(rdrObj != null)
      {
        try
        {
          rdrObj.close();
        }
        catch(Exception ex2)
        {
        }
      }
      return false;
    }
  }
}
