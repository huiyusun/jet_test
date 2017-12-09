\rm responseTriples
java -cp .:$JET_HOME/jet-all.jar AceJet.ApfToTriples docs/ docList sgm sgm.apf x | sort | uniq > responseTriples
