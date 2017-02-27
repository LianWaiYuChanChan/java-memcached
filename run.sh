#!/bin/bash
gradle jar
java -classpath build/libs/java-memcached.jar com.flash.memcached.netty.NettyServer 8000
