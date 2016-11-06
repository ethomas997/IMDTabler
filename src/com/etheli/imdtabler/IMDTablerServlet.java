//IMDTablerServlet.java:  Servlet interface for IMDTabler.
//
//  11/4/2016 -- [ET]
//

package com.etheli.imdtabler;

import java.io.PrintWriter;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
/**
 * Class IMDTablerServlet is a servlet interface for IMDTabler.
 */
public class IMDTablerServlet extends HttpServlet
{
  private static final long serialVersionUID = -5589513354107195913L;

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
         out.println("<!DOCTYPE html>");
         out.println("<html><head>");
         out.println("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>");
         out.println("<title>ET's IMDTabler</title></head>");
         out.println("<body>");
         out.println("<h3>ET's IMDTabler - Intermodulation Distortion Table Generator</h3>");

         out.println("Links:&nbsp; <a href=\"http://www.etheli.com/IMD\" target=\"_blank\">IMDTools</a>, " +
             "<a href=\"http://etserv.etheli.com/FreqSetGen/run\" target=\"_blank\">FreqSetGen</a>.&nbsp;");
         out.println("Sample frequency sets:<br>");
         out.println("<a href=\"run?freq_values=5685+5760+5800+5860+5905\">IMD 5</a>: 5685 5760 5800 5860 5905, <a href=\"run?freq_values=5645+5685+5760+5800+5860+5905\">IMD 6</a>: 5645 5685 5760 5800 5860 5905<br>");
         out.println("<a href=\"run?freq_values=5665+5752+5800+5866+5905\">ET5A</a>: 5665 5752 5800 5866 5905, <a href=\"run?freq_values=5665+5752+5800+5865+5905\">ET5B</a>: 5665 5752 5800 5865 5905<br>");
         out.println("<a href=\"run?freq_values=5665+5760+5800+5865+5905\">ET5C</a>: 5665 5760 5800 5865 5905, <a href=\"run?freq_values=5645+5685+5760+5805+5905+5945\">ETBest6</a>: 5645 5685 5760 5805 5905 5945<br>");
         out.println("<a href=\"run?freq_values=5645+5685+5760+5905+5945\">ET6minus1</a>: 5645 5685 5760 5905 5945, <a href=\"run?freq_values=5665+5725+5820+5860+5945\">ET 5</a>: 5665 5725 5820 5860 5945<br>");
         out.println("<a href=\"run?freq_values=5645+5740+5820+5860+5945\"></a><a href=\"run?freq_values=5658+5695+5732+5769+5806+5843+5880+5917\">raceband</a>: 5658 5695 5732 5769 5806 5843 5880 5917,");
         out.println("<a href=\"run?freq_values=5780+5860+5945\">Freq in YT video</a>: 5780 5860 5945<br>");
         out.println("<br>");
 
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

         String freqValuesStr = request.getParameter("freq_values");
         freqValuesStr = (freqValuesStr != null) ? freqValuesStr.trim() : "";

         // Display a form to prompt user to create session attribute
         out.println("<form method='get'>");
         out.println("Enter frequencies: ");
         out.println("<input type='text' name='freq_values' size='50' " +
                                                 "value='" + freqValuesStr +
                                   "' onfocus='this.select()' autofocus> ");
         out.println("<input type='submit' value='Run'>");
         out.println("</form>");
 
//         out.print("<a href='");
//         // Encode URL by including the session ID (URL-rewriting)
//         out.print(response.encodeURL(request.getRequestURI() + "?attribute_name=foo&attribute_value=bar"));
//         out.println("'>Encode URL with session ID (URL re-writing)</a>");
//         out.println("</body></html>");
         
         if(freqValuesStr.length() > 0)
         {
           try
           {
             final String imdStr = IMDTabler.getIMDStringForFreqSet(freqValuesStr);
             out.println("<br />(Hover the mouse cursor over table cells for information)<br /><br />");
             out.println(imdStr);
             out.println("<br>IMD rating:  A \"perfect\" table will be rated at " +
                  IMDTabler.RATING_MAX_VALUE + "; lower values are worse. " +
                  "For each (valid) cell in the table with a frequency-difference value less than " +
                  IMDTabler.RATING_DIFF_LIMIT + ", a value " + "of (" + IMDTabler.RATING_DIFF_LIMIT +
                  "-diff)^2 is added to a ratings total (the squaring is to make low-difference values " +
                  "detract more from the IMD rating). The ratings total is then divided by the number of " +
                  "selected frequencies and again by 5, and then subtracted from " +
                  IMDTabler.RATING_MAX_VALUE + " to get the IMD rating.<br>");
           }
           catch(NumberFormatException ex)
           {
             out.println("Error parsing entered values:  " +
                                                ex.getMessage() + "<br />");
             out.println("Entered values (" +
                                     freqValuesStr.split("[ ,\t]+").length +
                                         "):  " + freqValuesStr + "<br />");
           }
           catch(Exception ex)
           {
             out.println("Error processing data:  " + ex + "<br />");
           }
         }

         out.println("<br /><br />");
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

         out.println("<br />IMDTabler Version " + IMDTabler.VERSION_STR + "<br />");
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
}
