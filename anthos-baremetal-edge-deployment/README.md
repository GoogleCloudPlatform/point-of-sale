# Overview

<TODO>

### Pre-requisites

- Setup python stuff
   - python --version
   - pip install --upgrade pip # upgrade pip just-in-case
   - pip install ansible
   - pip install dnspython
   - pip install requests
   - pip install google-auth

## Quick starter

### Setup Google Cloud Environment
#### 1. Make a copy of this repository into any `git` based version control system you use _(e.g. Github, Gitlab, Bitbucket etc.)_

> **Note:** If you want to continue with Github, see [forking a repository](https://docs.github.com/en/get-started/quickstart/fork-a-repo#forking-a-repository) for creating
> your own copy of this repository in Github.

#### 2. Setup environment variables _(example values are set for some variables; you can change them if you want to name them something else)_
```sh
export PROJECT_ID="<YOUR_GCP_PROJECT_ID>"
export REGION="us-central1"
export ZONE="us-central1-a"
# path to which the Google Service Account key file will be downloaded to
export LOCAL_GSA_FILE="$(pwd)/remote-gsa-key.json"
# port on the GCE instance we will use to setup the nginx proxy to allow traffic into the AnthosBareMetal cluster
export PROXY_PORT="8082"
# should be a multiple of 3 since N/3 clusters are created with each having 3 nodes
export MACHINE_COUNT="3"
# fork of this repository: https://github.com/GoogleCloudPlatform/anthos-edge-usecases
export ROOT_REPO_URL="<LINK_TO_YOUR_FORK_OF_THIS_REPO>"
# this is the username used to authenticate to your fork of this repository
export SCM_TOKEN_USER="<YOUR_GIT_VERSION_CONTROL_USERNAME>"
# this is the access token that will be used to authenticate against your fork of this repository
export SCM_TOKEN_TOKEN="<ACCESS_TOKEN_FOR_YOUR_GIT_REPO>"
```

> **Note:** If you are trying this out with Github as your git version control and have forked this repository into your Github account then:
> - Used the link to your forked Github repository for `ROOT_REPO_URL` _(e.g. https://github.com/$GITHUB_USERNAME/anthos-edge-usecases)_
> - Use your Github username for `SCM_TOKEN_USER`
> - Use [this link](https://docs.github.com/en/github/authenticating-to-github/keeping-your-account-and-data-secure/creating-a-personal-access-token) to create a personal access token and use that for `SCM_TOKEN_TOKEN`

#### 3. Choose and configure the Google Cloud Project, Region and Zone you would like to use
```sh
gcloud config set project "${PROJECT_ID}"
gcloud services enable compute.googleapis.com

gcloud config set compute/region "${REGION}"
gcloud config set compute/zone "${ZONE}"
```

#### 4. Setup up GCP Service Account used by the GCE instances
```sh
# when asked "Create a new key for GSA? [y/N]" type "y" and press
./scripts/create-primary-gsa.sh
```

#### 5. Configure SSH keys and create the GCE instances where Anthos BareMetal will be installed
```sh
# just press the return key when asked for a passphrase for the SSH key (i.e. empty string)
./scripts/cloud/easy-install.sh
```

#### 6. Test SSH connectivity to the GCE instances
```sh
# If the checks fail the first time with errors like "sh: connect to host cnuc-1 port 22: Connection refused"
# then wait a few seconds and retry
for i in `seq $MACHINE_COUNT`; do
  HOSTNAME="cnuc-$i"
  ssh abm-admin@${HOSTNAME} 'ping -c 3 google.com'
done
```

#### 7. Generate Ansible inventory file from template and verify setup
```sh
envsubst < templates/inventory-cloud-example.yaml > inventory/gcp.yaml
./scripts/health-check.sh
./scripts/verify-pre-installation.sh
```

#### 8. Run the Ansible playbook for installing Anthos Bare Metal on the GCE instances
```sh
# this will configure the GCE instances with all the necessary tools, install
# Anthos BareMetal, install Anthos Config Management and configure it to sync
# with the configs at $ROOT_REPO_URL/anthos-baremetal-edge-deployment/acm-config-sink
ansible-playbook -i inventory cloud-full-install.yml
```

#### 9. Login to the Kubernetes cluster running on Anthos Bare Metal in GCE
```sh
# Copy the utility scripts into the admin node of the cluster
scp -i ~/.ssh/cnucs-cloud scripts/cloud/cnuc-k8s-login-setup.sh abm-admin@cnuc-1:
# SSH into the admin node of the cluster
ssh -i ~/.ssh/cnucs-cloud abm-admin@cnuc-1
# execute the script and copy token that is printed out
./cnuc-k8s-login-setup.sh
```

Once you have run the above steps, copy the `token` that is printed out and login
to the kubernetes cluster from the `Kubernetes clusters` page in the Google Cloud
console.

Verify that the cluster has `synced` with the [configurations from this repository](/acm-config-sink)
using **Anthos Config Management**

#### 10. Setup the `nginx` configuration to route traffic to the `API Server Loadbalancer` service
```sh
# get the IP address of the LoadBalancer type kubernetes service
ABM_INTERNAL_IP=$(kubectl get services api-server-lb -n pos | awk '{print $4}' | tail -n 1)
# update the template configuration file with the fetched IP address
sudo sh -c "sed 's/<K8_LB_IP>/${ABM_INTERNAL_IP}/g' /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf"
# restart nginx to ensure the new configuration is picked up
sudo systemctl restart nginx
# check and verify the status of the nginx server to be "active (running)"
sudo systemctl status nginx
# exit out of the admin instance
exit
```

#### 11. Get the external IP address of the admin GCE instance and access the UI of the **Point of Sales** application
```sh
EXTERNAL_IP=$(gcloud compute instances list --project ${PROJECT_ID} --filter="name:cnuc-1" | awk '{print $5}' | tail -n 1)
echo "Point the browser to: ${EXTERNAL_IP}:${PROXY_PORT}"
```
