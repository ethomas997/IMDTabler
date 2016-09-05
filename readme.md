ET's FPV IMD Tools
------------------

IMD (Intermodulation Distortion) is when two transmitters combine to
produce interference at a third frequency.  For instance, if using the
frequencies 5760, 5800 and 5840, any two of these will work well
together, but when all three frequencies are in use IMD interferece is
generated at 5760 and 5840 Mhz.  If the 5840 frequency is changed to
5880 then IMD interference is not seen at any of the three frequencies
in use.  IMD is calculated with this formula:  F3 = (F1\^2) - F2
 
 See [here for an rcgroups discussion thread on
IMD](http://www.rcgroups.com/forums/showthread.php?t=2447038).  (Also
[here](http://forum.multigp.com/forum/event-race-management/186-frequency-profiles-what-they-actually-mean)
and
[here](http://www.nwfpv.com/the-lounge/189-5-8ghz-fpv-distortion-imd-intermodulation-distortion.html).) 
There's a [video](http://www.youtube.com/watch?v=axWXu8ih8Oc) that
demonstrates the effect.  Folks on the thread have generated
spreadsheets to tabulate IMD values.  This looked like a good place for
some software, so I've created the tools below:
 
 [**IMDTabler**](http://etserv.etheli.com/IMDTabler/run)
 
 The [IMDTabler tool](http://etserv.etheli.com/IMDTabler/run) generates
a table of IMD values for a given set of frequencies.  Table cells are
colored red to indicate where IMD frequencies would cause interference
with the given frequencies, and hovering the mouse cursor over a cell
will show more information.  Each generated table is given an IMD
rating, with a score of 100 corresponding to minimal IMD interference.
 
 Clicking on one of the links at the top of the [IMDTabler
page](http://etserv.etheli.com/IMDTabler/run) will generate a table with
the given frequencies.  To generate tables for the examples described
above:  [5760 5800
5840](http://etserv.etheli.com/IMDTabler/run?freq_values=5760+5800+5840) 
  [5760 5800
5880](http://etserv.etheli.com/IMDTabler/run?freq_values=5760+5800+5880)
 
 [**FreqSetGen**](http://etserv.etheli.com/FreqSetGen/run)
 
 The [FreqSetGen tool](http://etserv.etheli.com/FreqSetGen/run) attempts
to generate an optimal set of frequencies given a larger set of possible
frequencies.  The tool has the following entry fields:
 
 *Number of frequencies:*  The number of frequencies in each generated
set.
 
 *Possible frequencies:*  A list of all the possible frequencies to
choose from.  Note that the larger this list is, the more time the
generator processor will need to complete.
 
 *Mandatory frequencies:*  An optional list of frequencies that mush
appear in each generated set.
 
 *Minimum separation:*  The minimum separation (in MHz) between values
in the generated frequency sets.  Separation less than 37 MHz is likely
to result in more interference between adjacent frequencies.
 
 *Maximum run time:*  The maximum time allowed (in seconds) for a run of
the generator process.  The largest value allowed is 600 (10 minutes).
 
 
So far the best 6-frequency set I've found is this one:

ETBest6:  5645 5685 5760 5805 5905 5945    IMD rating: 87   
[view](http://etserv.etheli.com/IMDTabler/run?freq_values=+5645+5685+5760+5805+5905+5945)
 
Removing 5805 yields a very good 5-frequency set:

ET6minus1:  5645 5685 5760 5905 5945    IMD rating: 100    
[view](http://etserv.etheli.com/IMDTabler/run?freq_values=+5645+5685+5760+5905+5945)
 
**[Distribution](http://www.etheli.com/IMD/dist)**
 
 The FreqSetGen tool can be run as a local application for faster
processing and unlimited maximum-run-time.  ([Java](http://www.java.com)
version 7 or later is required.)  The latest version of ET's IMD Tools
may be downloaded from [here](http://www.etheli.com/IMD/dist). 
FreqSetGen application usage:

    FreqSetGen numFreq "possibleFreqs" "mandatoryFreqs" [minFreqSep] [maxRunSecs]

Example:

    FreqSetGen 4 "5740 5760 5780 5800 5820 5840 5860 5880" "5740" 37 300

**Source Code**
 
 The Java source is available in the distribution.  The web content is
via Java
[servlets](https://www.ntu.edu.sg/home/ehchua/programming/java/JavaServlets.html)
running on a [Tomcat](http://tomcat.apache.org) server.  If reusing the
code, please provide attribution:  "ET's IMD Tools -
http://www.etheli.com"
 
 **Feedback:**  If you have comments, questions or suggestions, [do let
me know](http://www.etheli.com/contact), or post a comment to the
[thread on
rcgroups](http://www.rcgroups.com/forums/showthread.php?t=2447038).

* * * * *

Click [here to contact me](http://www.etheli.com/contact/index.html)

[etheli.com home page](http://www.etheli.com)