@echo off


set CLASSPATH=conf;lib/antlr.jar;lib/chardet.jar;lib/commons-codec-1.3.jar;lib/commons-logging-1.1.1.jar;lib/crawler-1.3.0-repack.jar;lib/htmlparser.jar;lib/jwf.jar;lib/log4j.jar;lib/backport-util-concurrent-3.1.jar;lib/cls.jar;lib/commons-httpclient-3.1.jar;lib/cpdetector_1.0.7.jar;lib/htmllexer.jar;lib/junit-4.1.jar;lib/klingerIncludes.jar;PupSniffer.jar;%CLASSPATH%

java  -Xms512m -Xmx1024m  com.googlecode.pupsniffer.PupSniffer config.txt
