#!/bin/bash

# Parameters
SCRIPT_PATH=$1
LISTEN_PORT=$2

# Start mitmproxy with the specified script and port
/usr/local/bin/mitmdump -s $SCRIPT_PATH --mode regular@$LISTEN_PORT
