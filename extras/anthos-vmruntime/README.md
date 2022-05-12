# Run the PoS Application in a VM

You can run all the services of the application in a single VM as a system
service. Simply copy the [**init-vm.sh**](init-vm.sh) file into your VM and then
execute it from inside the VM.
```sh
sudo bash init-vm.sh
```

This sets up all the services of the application to run as a system service
inside your VM. Note that the `inventory service` and the `payments service`
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