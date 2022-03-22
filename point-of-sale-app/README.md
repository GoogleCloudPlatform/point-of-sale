## Sample application running in Anthos on Bare Metal at the Edge

1. Use skaffold to deploy the application
```bash
skaffold run -p release
```

2. Get the external IP of the `api-server`
```sh
while [ -z "$API_SERVER_IP" ] || [ "$API_SERVER_IP" = "<pending>" ]; do
  echo "Fetching API_SERVER_IP....."
  sleep 3
  API_SERVER_IP=$(kubectl get service api-server-lb | awk '{print $4}' | tail -n 1)
done
echo "API_SERVER IP is $API_SERVER_IP"
```

3. Access the application using the IP printed above

<p align="center">
  <img src="../anthos-baremetal-edge-deployment/docs/images/pos-v1.png">
</p>
