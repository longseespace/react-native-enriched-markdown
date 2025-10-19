#!/bin/bash

# Script to copy MD4C source files for iOS build
# This ensures the MD4C submodule files are available for compilation

set -e

echo "Setting up MD4C for iOS build..."

# Create md4c directory in iOS if it doesn't exist
mkdir -p ios/md4c

# Copy MD4C source files
if [ -d "shared/MD4C/src" ]; then
    echo "Copying MD4C source files..."
    cp shared/MD4C/src/md4c.h ios/md4c/
    cp shared/MD4C/src/md4c.c ios/md4c/
    echo "MD4C setup complete!"
else
    echo "Error: MD4C submodule not found. Please run: git submodule update --init --recursive"
    exit 1
fi
