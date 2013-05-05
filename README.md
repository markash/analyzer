Yellowfire Analyzer
===================

Background
----------
The Oracle Weblogic administrative console has an awesome interface for viewing the statistics of a single queue or topic but not one for view all the queues or topics statistics. I have written similar front-end tools in Java, JSF, JMX and wanted to try out the recently released Playframework and Scala to determine if the environment made a developer more productive than the code-build-deploy-test cycle (without a bought tool like JRebel) that is prevalent in the JEE space. 


Technology
----------
Scala - Nuff said, I wanted to program this project using a functional language although my style is still heavily influenced by Java (go figure, over 13 years of programming like that tends to soften the brain ;-S)

Playframework - Although I have used the Lift framework in the past I wanted to give Playframework a test drive.

Jolokia - A lightweight JMX bridge that allows Analyzer to communicate with the Weblogic JMX server so that the JMS statistics can be extracted


Upcoming
--------
1. Importing WLDF JMS logging into the Analyzer so that the production and consumption of messages can be tracked

2. Filtering the queues page so that only queues of interest by the user is displayed and updated.

3. Peristing the statistics in a database so that historical statistics can be viewed and charted 