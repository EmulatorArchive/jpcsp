#!/bin/sh\n
java -Xmx1024m -XX:MaxPermSize=128m -XX:ReservedCodeCacheSize=64m -Djava.library.path=lib/amd-64 -jar bin/jpcsp.jar