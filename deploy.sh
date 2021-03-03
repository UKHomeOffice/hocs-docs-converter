#!/bin/bash

export KUBE_NAMESPACE=${ENVIRONMENT}
export KUBE_SERVER=${KUBE_SERVER}

if [[ -z ${VERSION} ]] ; then
    export VERSION=${IMAGE_VERSION}
fi

if [[ ${KUBE_NAMESPACE} == *prod ]]
then
    export MIN_REPLICAS="2"
    export MAX_REPLICAS="10"
else
    export MIN_REPLICAS="1"
    export MAX_REPLICAS="10"
fi

if [[ ${KUBE_NAMESPACE} == "cs-prod" ]] ; then
    echo "deploy ${VERSION} to prod namespace, using HOCS_DOCS_CONVERTER_CS_PROD drone secret"
    export KUBE_TOKEN=${HOCS_DOCS_CONVERTER_CS_PROD}
elif [[ ${KUBE_NAMESPACE} == "wcs-prod" ]] ; then
    echo "deploy ${VERSION} to prod namespace, using HOCS_DOCS_CONVERTER_WCS_PROD drone secret"
    export KUBE_TOKEN=${HOCS_DOCS_CONVERTER_WCS_PROD}
elif [[ ${KUBE_NAMESPACE} == "cs-qa" ]] ; then
    echo "deploying ${VERSION} to test namespace, using HOCS_DOCS_CONVERTER_CS_QA drone secret"
    export KUBE_TOKEN=${HOCS_DOCS_CONVERTER_CS_QA}
elif [[ ${KUBE_NAMESPACE} == "wcs-qa" ]] ; then
    echo "deploying ${VERSION} to test namespace, using HOCS_DOCS_CONVERTER_WCS_QA drone secret"
    export KUBE_TOKEN=${HOCS_DOCS_CONVERTER_WCS_QA}
elif [[ ${KUBE_NAMESPACE} == "cs-demo" ]] ; then
    echo "deploy ${VERSION} to demo namespace, using HOCS_DOCS_CONVERTER_CS_DEMO drone secret"
    export KUBE_TOKEN=${HOCS_DOCS_CONVERTER_CS_DEMO}
elif [[ ${KUBE_NAMESPACE} == "wcs-demo" ]] ; then
    echo "deploy ${VERSION} to demo namespace, using HOCS_DOCS_CONVERTER_WCS_DEMO drone secret"
    export KUBE_TOKEN=${HOCS_DOCS_CONVERTER_WCS_DEMO}
elif [[ ${KUBE_NAMESPACE} == "cs-dev" ]] ; then
    echo "deploy ${VERSION} to dev namespace, using HOCS_DOCS_CONVERTER_CS_DEV drone secret"
    export KUBE_TOKEN=${HOCS_DOCS_CONVERTER_CS_DEV}
elif [[ ${KUBE_NAMESPACE} == "wcs-dev" ]] ; then
    echo "deploy ${VERSION} to dev namespace, using HOCS_DOCS_CONVERTER_WCS_DEV drone secret"
    export KUBE_TOKEN=${HOCS_DOCS_CONVERTER_WCS_DEV}
elif [[ ${KUBE_NAMESPACE} == "hocs-qax" ]] ; then
    echo "deploy ${VERSION} to qax namespace, using HOCS_DOCS_CONVERTER_QAX drone secret"
    export KUBE_TOKEN=${HOCS_DOCS_CONVERTER_QAX}
else
    echo "Unable to find environment: ${ENVIRONMENT}"
fi

if [[ -z ${KUBE_TOKEN} ]] ; then
    echo "Failed to find a value for KUBE_TOKEN - exiting"
    exit -1
fi

cd kd

kd --insecure-skip-tls-verify \
   --timeout 10m \
    -f deployment.yaml \
    -f service.yaml \
    -f autoscale.yaml
