#!/bin/bash

deploy_to_bluemix() {
  export JAVA_HOME=~/java8
  export PATH=${JAVA_HOME}/bin:$PATH

  export CF_CONTEXT="mybluemix.net"
  
  if [ "${CF_TARGET_URL}" == "https://api.eu-gb.bluemix.net" ] ; then
    export CF_CONTEXT="eu-gb.${CF_CONTEXT}"""
  elif [ "${CF_TARGET_URL}" == "https://api.au-syd.bluemix.net" ] ; then
      export CF_CONTEXT="au-syd.${CF_CONTEXT}"
  fi

  mvn install -P bluemix \
      -Dcf.context=${CF_CONTEXT} \
      -Dcf.target=${CF_TARGET_URL} \
      -Dcf.org=${CF_ORG} \
      -Dcf.space=${CF_SPACE} \
      -Dapp.name=${CF_APP} \
      -Dcf.username=${USERNAME} \
      -Dcf.password=${PASSWORD} 
}
