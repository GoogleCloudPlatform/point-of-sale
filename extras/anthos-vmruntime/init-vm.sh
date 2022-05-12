#!/bin/bash
# Copyright 2022 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# install dependencies
sudo apt update -y
sudo apt install -y default-jre default-jdk

# clone the Point of Sale repository
git clone https://github.com/GoogleCloudPlatform/point-of-sale

# build the Point of Sale application
cd point-of-sale || exit
./mvnw clean install
cd ..

# create the folders to host the runnable artifacts
sudo mkdir -p /pos/jars
sudo mkdir -p /pos/scripts

# copy the built artifacts to the /pos directory
sudo cp point-of-sale/src/api-server/target/api-server-0.1.0-SNAPSHOT.jar /pos/jars/api-server.jar
sudo cp point-of-sale/src/inventory/target/inventory-0.1.0-SNAPSHOT.jar /pos/jars/inventory.jar
sudo cp point-of-sale/src/payments/target/payments-0.1.0-SNAPSHOT.jar /pos/jars/payments.jar

# delete the repository and maven jars that were downloaded
# we do this to minimize the size of the VM disk
rm -rf point-of-sale
rm -rf ~/.m2/repository/*

# create the scripts that will be run by the systemd services
cat <<EOF | sudo tee -a /pos/scripts/api-server.sh -
#!/bin/sh
java -jar /pos/jars/api-server.jar --server.port=$API_SERVER_PORT
EOF

cat <<EOF | sudo tee -a /pos/scripts/inventory.sh -
#!/bin/sh
java -jar /pos/jars/inventory.jar --server.port=$INVENTORY_PORT
EOF

cat <<EOF | sudo tee -a /pos/scripts/payments.sh -
#!/bin/sh
java -jar /pos/jars/payments.jar --server.port=$PAYMENTS_PORT
EOF

# make the above scripts executable
sudo chmod +x /pos/scripts/api-server.sh
sudo chmod +x /pos/scripts/inventory.sh
sudo chmod +x /pos/scripts/payments.sh

# create the systemd service definitions
cat <<EOF | sudo tee -a /etc/systemd/system/pos.service -
[Unit]
Description=Point of Sale Application

[Service]
# The dummy root program to spawn the 3 services of the Point of Sale Application
# After api-server.service, inventory.service and payments.service are spawned
# this service will exit
Type=oneshot
ExecStart=/bin/true
RemainAfterExit=yes

[Install]
WantedBy=multi-user.target
EOF

cat <<EOF | sudo tee -a /etc/systemd/system/pos_apiserver.service -
[Unit]
Description=API Server of the Point of Sale Application
PartOf=pos.service
After=pos.service
After=pos_inventory.service
After=pos_payments.service

[Service]
WorkingDirectory=/pos

Environment=API_SERVER_PORT=8081
Environment=INVENTORY_EP=http://localhost:8082
Environment=PAYMENTS_EP=http://localhost:8083
ExecStart=/pos/scripts/api-server.sh
TimeoutStopSec=10
Restart=on-failure
RestartSec=5

[Install]
WantedBy=pos.service
EOF

cat <<EOF | sudo tee -a /etc/systemd/system/pos_inventory.service -
[Unit]
Description=Inventory service of the Point of Sale Application
PartOf=pos.service
After=pos.service

[Service]
WorkingDirectory=/pos

Environment=INVENTORY_PORT=8082
Environment=MYSQL_HOST=mysql-db
Environment=SPRING_PROFILES_ACTIVE=database
Environment=ACTIVE_ITEM_TYPE=burgers
Environment=ITEMS="items:\n  - name: "BigBurger"\n    type: "burgers"\n    price: 5.50\n    imageUrl: "usr/lib/images/bigburger.png"\n    quantity: 200\n    labels: [ "retail", "restaurant", "food" ]\n  - name: "DoubleBurger"\n    type: "burgers"\n    price: 7.20\n    imageUrl: "usr/lib/images/burgers.png"\n    quantity: 200\n    labels: [ "retail", "restaurant", "food" ]\n  - name: "Shirt"\n    type: "textile"\n    price: 15.50\n    imageUrl: "usr/lib/images/shirt.png"\n    quantity: 50\n    labels: [ "retail", "textile", "clothing" ]\n  - name: "Short"\n    type: "textile"\n    price: 17.20\n    imageUrl: "usr/lib/images/short.png"\n    quantity: 20\n    labels: [ "retail", "textile", "clothing" ]"

ExecStart=/pos/scripts/inventory.sh
TimeoutStopSec=10
Restart=on-failure
RestartSec=5

[Install]
WantedBy=pos.service
EOF

cat <<EOF | sudo tee -a /etc/systemd/system/pos_payments.service -
[Unit]
Description=Payments service of the Point of Sale Application
PartOf=pos.service
After=pos.service

[Service]
WorkingDirectory=/pos

Environment=PAYMENTS_PORT=8083
Environment=MYSQL_HOST=mysql-db
Environment=SPRING_PROFILES_ACTIVE=database
ExecStart=/pos/scripts/payments.sh
TimeoutStopSec=10
Restart=on-failure
RestartSec=5

[Install]
WantedBy=pos.service
EOF

# reload the systemd service and ensure the new services are picked
sudo systemctl daemon-reload
sudo systemctl enable pos pos_inventory pos_payments pos_apiserver
sudo systemctl start pos