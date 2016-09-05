call mkjars.bat
call wzzip ETs_IMDTools_dist.zip FreqSetGen.jar IMDTabler.jar .classpath .project *.bat FreqSetGen FreqSetGen_manifest.mf IMDTabler_manifest.mf readme.html license.txt
call wzzip -rP ETs_IMDTools_dist.zip src classes lib