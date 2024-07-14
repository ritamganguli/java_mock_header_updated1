#!/bin/bash

# Parameters
SCRIPT_PATH=$1
LISTEN_PORT=$2

# Start mitmproxy with the specified script and port
mitmproxy -s $SCRIPT_PATH --listen-port $LISTEN_PORT
