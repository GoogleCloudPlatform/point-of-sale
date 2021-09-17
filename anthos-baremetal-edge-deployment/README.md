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
1. Make a copy of this repository into any `git` based version control system you use _(e.g. Github, Gitlab, Bitbucket etc.)_

> **Note:** If you want to continue with Github, see [forking a repository](https://docs.github.com/en/get-started/quickstart/fork-a-repo#forking-a-repository) for creating
> your own copy of this repository in Github.

2. Setup environment variables _(example values are set for some variables; you can change them if you want to name them something else)_
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
> - Used the link to your forked Github repository for `ROOT_REPO_URL`
> - Use your Github username for `SCM_TOKEN_USER`
> - Use [this link](https://docs.github.com/en/github/authenticating-to-github/keeping-your-account-and-data-secure/creating-a-personal-access-token) to create a personal access token and use that for `SCM_TOKEN_TOKEN`

3. Choose and configure the Google Cloud Project, Region and Zone you would like to use
```sh
gcloud config set project "${PROJECT_ID}"
gcloud services enable compute.googleapis.com

gcloud config set compute/region "${REGION}"
gcloud config set compute/zone "${ZONE}"
```

4. Setup up GCP Service Account used by the GCE instances
```sh
# when asked "Create a new key for GSA? [y/N]" type "y" and press
./scripts/create-primary-gsa.sh
```

5. Configure SSH keys and create the GCE instances where Anthos BareMetal will be installed
```sh
# just press the return key when asked for a passphrase for the SSH key (i.e. empty string)
./scripts/cloud/easy-install.sh
```

6. Test SSH connectivity to the GCE instances
```sh
# If the checks fail the first time with errors like "sh: connect to host cnuc-1 port 22: Connection refused"
# then wait a few seconds and retry
for i in `seq $MACHINE_COUNT`; do
  HOSTNAME="cnuc-$i"
  ssh abm-admin@${HOSTNAME} 'ping -c 3 google.com'
done

# -----------------------------------------------------
#                   Expected Output
# -----------------------------------------------------
PING google.com (74.125.124.113) 56(84) bytes of data.
64 bytes from jp-in-f113.1e100.net (74.125.124.113): icmp_seq=1 ttl=115 time=1.10 ms
64 bytes from jp-in-f113.1e100.net (74.125.124.113): icmp_seq=2 ttl=115 time=1.10 ms
64 bytes from jp-in-f113.1e100.net (74.125.124.113): icmp_seq=3 ttl=115 time=0.886 ms

--- google.com ping statistics ---
3 packets transmitted, 3 received, 0% packet loss, time 2003ms
rtt min/avg/max/mdev = 0.886/1.028/1.102/0.100 ms
PING google.com (108.177.112.139) 56(84) bytes of data.
...
...
...
```

7. Generate Ansible inventory file from template and verify setup
```sh
envsubst < templates/inventory-cloud-example.yaml > inventory/gcp.yaml
./scripts/health-check.sh
./scripts/verify-pre-installation.sh

# -----------------------------------------------------
#                   Expected Output
# -----------------------------------------------------
cnuc-1 | SUCCESS => {"ansible_facts": {"discovered_interpreter_python": "/usr/bin/python3"},"changed": false,"ping": "pong"}
cnuc-2 | SUCCESS => {"ansible_facts": {"discovered_interpreter_python": "/usr/bin/python3"},"changed": false,"ping": "pong"}
cnuc-3 | SUCCESS => {"ansible_facts": {"discovered_interpreter_python": "/usr/bin/python3"},"changed": false,"ping": "pong"}


SUCCESS!!

Proceed!!
```

8. Run the Ansible playbook for installing Anthos Bare Metal on the GCE instances
```sh
# this will configure the GCE instances with all the necessary tools, install Anthos BareMetal, install Anthos
# Config Management and configure it to sync with the configs at $ROOT_REPO_URL/anthos-baremetal-edge-deployment/acm-config-sink
ansible-playbook -i inventory cloud-full-install.yml
```

9. Login to the Kubernetes cluster running on Anthos Bare Metal in GCE
```sh
# Copy the utility scripts into the admin node of the cluster
scp -i ~/.ssh/cnucs-cloud scripts/cloud/cnuc-k8s-login-setup.sh abm-admin@cnuc-1:

# SSH into the admin node of the cluster
ssh -i ~/.ssh/cnucs-cloud abm-admin@cnuc-1

# execute the script and copy token that is printed out
./cnuc-k8s-login-setup.sh

# -----------------------------------------------------
#                   Expected Output
# -----------------------------------------------------
...
...
💡 Retreiving Kubernetes Service Account Token

🚀 ------------------------------TOKEN-------------------------------- 🚀
eyJhbGciOiJSUzI1NiIsImtpZCI6Imk2X3duZ3BzckQyWmszb09sZHFMN0FoWU9mV1kzOWNGZzMyb0x2WlMyalkifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJkZWZhdWx0Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZWNyZXQubmFtZSI6ImVkZ2Etc2EtdG9rZW4tc2R4MmQiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC5uYW1lIjoiZWRnYS1zYSIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjQwYWQxNDk2LWM2MzEtNDhiNi05YmUxLWY5YzgwODJjYzgzOSIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpkZWZhdWx0OmVkZ2Etc2EifQ.IXqXwX5pg9RIyNHJZTM6cBKTEWOMfQ4IQQa398f0qwuYlSe12CA1l6P8TInf0S1aood7NJWxxe-5ojRvcG8pdOuINq2yHyQ5hM7K7R4h2qRwUznRwuzOp_eXC0z0Yg7VVXCkaqnUR1_NzK7qSu4LJcuLzkCYkFdSnvKIQABHSvfvZMrJP4CwanGlof6s-fbu8IUy1_bTgCminylNKuFcfsx1trq0GqpjVwH_WgrhgwJgh9UCH-BvM1h_H1uz4P-hDEKURb5O6IxulTXWH6dxYxg66xMgyLOd9FJyhZgjbf-a-3cbDci5YABEzioJlHVnV8GOX_q-MnIagA9-t1KpHA
🚀 ------------------------------------------------------------------- 🚀
```

Once you have run the above steps, copy the `Token` that is printed out and login
to the kubernetes cluster from the [`Kubernetes clusters`](https://pantheon.corp.google.com/kubernetes/list/overview) page in the Google Cloud
console.

![gitlab token](docs/images/login-k8s.png)
![gitlab token](docs/images/login-k8s-token.png)

Verify that the cluster has `synced` with the [configurations from this repository](/acm-config-sink)
using **Anthos Config Management**

![gitlab token](docs/images/acm-sync.png)

10. Setup the `nginx` configuration to route traffic to the `API Server Loadbalancer` service
```sh
# get the IP address of the LoadBalancer type kubernetes service
ABM_INTERNAL_IP=$(kubectl get services api-server-lb -n pos | awk '{print $4}' | tail -n 1)

# update the template configuration file with the fetched IP address
sudo sh -c "sed 's/<K8_LB_IP>/${ABM_INTERNAL_IP}/g' /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf"

# restart nginx to ensure the new configuration is picked up
sudo systemctl restart nginx

# check and verify the status of the nginx server to be "active (running)"
sudo systemctl status nginx

# -----------------------------------------------------
#                   Expected Output
# -----------------------------------------------------
● nginx.service - A high performance web server and a reverse proxy server
     Loaded: loaded (/lib/systemd/system/nginx.service; enabled; vendor preset: enabled)
     Active: active (running) since Fri 2021-09-17 02:41:01 UTC; 2s ago
       Docs: man:nginx(8)
    Process: 92571 ExecStartPre=/usr/sbin/nginx -t -q -g daemon on; master_process on; (code=exited, status=0/SUCCESS)
    Process: 92572 ExecStart=/usr/sbin/nginx -g daemon on; master_process on; (code=exited, status=0/SUCCESS)
   Main PID: 92573 (nginx)
      Tasks: 17 (limit: 72331)
     Memory: 13.2M
     CGroup: /system.slice/nginx.service
             ├─92573 nginx: master process /usr/sbin/nginx -g daemon on; master_process on;
             ├─92574 nginx: worker process
             ├─92575 nginx: worker process
             ├─92576 nginx: worker process
             ├─92577 nginx: worker process
             ├─92578 nginx: worker process
             ├─92579 nginx: worker process
             ├─92580 nginx: worker process
             ├─92581 nginx: worker process

# exit out of the admin instance
exit
```

11. Get the external IP address of the admin GCE instance and access the UI of the **Point of Sales** application
```sh
EXTERNAL_IP=$(gcloud compute instances list --project ${PROJECT_ID} --filter="name:cnuc-1" | awk '{print $5}' | tail -n 1)
echo "Point the browser to: ${EXTERNAL_IP}:${PROXY_PORT}"

# -----------------------------------------------------
#                   Expected Output
# -----------------------------------------------------
Point the browser to: 34.134.194.84:8082
```
