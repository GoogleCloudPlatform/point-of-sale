## Sample application running in Anthos on Bare Metal at the Edge

- Use skaffold to deploy the application
```bash
skaffold run
```

- Get the external IP of the `api-server`
```sh
while [ -z "$API_SERVER_IP" ] || [ "$API_SERVER_IP" = "<pending>" ]; do
  echo "Fetching API_SERVER_IP....."
  sleep 3
  API_SERVER_IP=$(kubectl get service api-server-lb | awk '{print $4}' | tail -n 1)
done
echo "API_SERVER IP is $API_SERVER_IP"
```

- Access the application using the IP printed above 
