# Run the PoS Application in a VM

You can run all the services of the application in a single VM as a system
service. Simply copy the [**init-vm.sh**](init-vm.sh) file into your VM and then
execute it from inside the VM. This sets up all the services of the application
to run as a system service inside your VM.

```sh
sudo bash init-vm.sh
```

Note that the `inventory service` and the `payments service`
[expects the hostname `mysql-db` to resolve to an IP address](init-vm.sh#L115)
pointing to a MySQL service. This is because these _systemd service_ files were
written for a use-case where the PoS application VM is run inside an Anthos on
bare metal cluster using [Anthos VMRuntime](https://cloud.google.com/anthos/clusters/docs/bare-metal/latest/how-to/vm-workloads). In that use-case we have a MySQL container being made
available to the services via a `Kubernetes Service` named `mysql-db`. _See the
[full guide for that here](https://github.com/GoogleCloudPlatform/anthos-samples/tree/main/anthos-vmruntime)._

If you don't want the application connecting to an external MySQL instance, then
you can simply change the `SPRING_PROFILES_ACTIVE` environment variable in the
systemd service files to **inmemory**. This will spawn an embedded H2 database
per service that they will use.

```sh
# line 116 (pos_inventory.service) of the init-vm.sh file
Environment=SPRING_PROFILES_ACTIVE=inmemory

# line 141 (pos_payments.service) of the init-vm.sh file
Environment=SPRING_PROFILES_ACTIVE=inmemory
```

### Creating an image of this VM to be run in an Anthos cluster using Anthos VMRuntime

> Make sure you stop the VM first.
- Set your environment variables
    ```sh
    export PROJECT_ID=<YOUR_GCP_PROJECT>
    export ZONE=<YOUR_GCP_ZONE>
    export BUCKET_NAME=<STORAGE_BUCKET_NAME>
    ```

- Create a `Disk Image` of the VM
    ```sh
    gcloud compute images create pos-vm-image \
        --source-disk=pos-vm \
        --source-disk-zone=${ZONE} \
        --project=${PROJECT_ID}
    ```

- Create `Cloud Storage Bucket` to store the image
    ```sh
    gsutil mb -p ${PROJECT_ID} -c standard -b on gs://${BUCKET_NAME}
    ```

- Make the bucket **publicly readable**
    > NOTE: For standard use the bucket should be **private** and [credentials](https://cloud.google.com/anthos/clusters/docs/bare-metal/latest/vm-runtime/create-storage-credentials) used to import the image into VM Runtime.
    ```sh
    gsutil iam ch allUsers:objectViewer gs://${BUCKET_NAME}
    ```


- Export the Disk Image we create to the Cloud Storage Bucket
    ```sh
    gcloud compute images export \
        --destination-uri gs://${BUCKET_NAME}/pos-vm.qcow2 \
        --image pos-vm-image \
        --export-format qcow2 \
        --project ${PROJECT_ID}
    ```

- Download the Disk Image locally
    ```sh
    gcloud storage cp gs://${BUCKET_NAME}/pos-vm.qcow2 pos-vm.qcow2
    ```

- Enable Cloud Init using virt-sysprep
    ```sh
    virt-sysprep -a pos-vm.qcow2 \
        --uninstall google-compute-engine,google-compute-engine-oslogin,google-guest-agent,google-osconfig-agent \
        --delete '/etc/cloud/cloud.cfg.d/*.cfg' \
        --mkdir /etc/cloud/cloud.cfg.d \
        --write /etc/cloud/cloud.cfg.d/10_anthos.cfg:'datasource_list: [ NoCloud, ConfigDrive, None ]\n'
    ```
    > NOTE: On recent Ubuntu `virt-sysprep` is part of the `libguestfs-tools` package.

- Upload the Disk Image to Cloud Storage Bucket
    ```sh
    gcloud storage cp pos-vm.qcow2 gs://${BUCKET_NAME}/pos-vm.qcow2
    ```

Now you can use the URL `https://storage.googleapis.com/${BUCKET_NAME}/pos-vm.qcow2`
to refer to the VM image when creating your VM in an Anthos VMRuntime enabled
cluster.
