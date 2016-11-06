call mkjars.bat
call 7z a ETs_IMDTools_dist.zip FreqSetGen.jar IMDTabler.jar .classpath .project *.bat FreqSetGen FreqSetGen_manifest.mf IMDTabler_manifest.mf readme.html license.txt
call 7z a -r ETs_IMDTools_dist.zip src classes lib