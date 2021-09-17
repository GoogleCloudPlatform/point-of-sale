#!/bin/bash

# Cloud specific
ansible-playbook -i inventory/ cloud-full-install.yml --tags network-vxlan,update-dependencies
