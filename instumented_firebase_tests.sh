#!/bin/bash

# Firebase service account decrypt
openssl aes-256-cbc -K $encrypted_6ac108e7f874_key -iv $encrypted_6ac108e7f874_iv -in service-account.json.enc -out /tmp/service-account.json -d

# Firebase setup
wget --quiet --output-document=/tmp/google-cloud-sdk.tar.gz https://dl.google.com/dl/cloudsdk/channels/rapid/google-cloud-sdk.tar.gz  
mkdir -p /opt  
tar zxf /tmp/google-cloud-sdk.tar.gz --directory /opt  
/opt/google-cloud-sdk/install.sh --quiet
source /opt/google-cloud-sdk/path.bash.inc

# Setup and configure the project
gcloud components update  
echo $CLOUD_PROJECT_ID  
gcloud config set project $CLOUD_PROJECT_ID

# Activate cloud credentials
gcloud auth activate-service-account --key-file /tmp/service-account.json

# List available options for logging purpose only (so that we can review available options)
gcloud firebase test android models list  
gcloud firebase test android versions list

gcloud firebase test android run --type instrumentation --app app/build/outputs/apk/debug/app-debug.apk --test app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk --device model=$DEVICE_MODEL,version=$DEVICE_VERSION,locale=en,orientation=portrait