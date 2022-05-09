# UI

- Anthos BareMetal Edge Workload UI
### Local development of the UI

***The following steps assume that you are running them form the root of this repository***

1. First deploy the app to your kubernetes cluster
```sh
# deploy the apps to the kubernetes cluster with skaffold; 

# to deploy the released manifests
skaffold dev -p release 

# to deploy the local source based manifests
skaffold dev -p dev --default-repo us-docker.pkg.dev/<GOOGLE_CLOUD_PROJECT>/<IMAGE_REPO>
```

2. Get the IP address of the `api-server=lb` service
```sh
while [ -z "$API_SERVER_IP" ] || [ "$API_SERVER_IP" = "<pending>" ]; do
  echo "Fetching API_SERVER_IP....."
  sleep 3
  API_SERVER_IP=$(kubectl get service api-server-lb | awk '{print $4}' | tail -n 1)
done
echo "API_SERVER IP is $API_SERVER_IP"
```

3. Create a local env file with this IP
```sh
sed "s/IP_ADDRESS/$API_SERVER_IP/g" ui/.env.development.sample > ui/.env.development.local
```

4. Start the UI project for development
```sh
# must install modules during the first run
npm install --prefix ui
npm run serve --prefix ui
```

5. Now you can access the applciation at `localhost:8080` and observe the UI changes live
