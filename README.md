# volume-prediction
An implementation of http://roseyu.com/Papers/kdd2016.pdf

# Data format

* Volumes of each day and all roads are stored in a single txt file, named data+".txt", e.g.`20161001.txt`
* A record include `RoadId`,`Time`,`SegmentId`,`Direction`,`Speed`,`Volume`
* Records are sorted by time from 00:05:00 to 24:00:00
