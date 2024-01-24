# Domain mapping for the application

The parent domain for the application is [`point-of-sale.retail.cymbal.dev`](http://point-of-sale.retail.cymbal.dev)
hosted via [Google Domains](https://domains.google/). There are a few
sub-domains that are used to reach key deployments of the application:
- Latest release _(using MySQL DB)_: [point-of-sale.retail.cymbal.dev](http://point-of-sale.retail.cymbal.dev)
- Latest release _(using H2 Embedded DB)_: [im.point-of-sale.retail.cymbal.dev](http://im.point-of-sale.retail.cymbal.dev)
- Build from **main** branch  _(using MySQL DB)_: [staging.point-of-sale.retail.cymbal.dev](http://staging.point-of-sale.retail.cymbal.dev)
- Build from **main** branch  _(using H2 Embedded D)_: [im.staging.point-of-sale.retail.cymbal.dev](http://im.staging.point-of-sale.retail.cymbal.dev)

### To create a new mapping you have to: _(examples show how the above were created)_
1. Create a global static IP Address
    ```sh
    MAIN_DB_STATIC_IP_NAME=abm-pos-com-ip
    MAIN_INMEMORY_STATIC_IP_NAME=im-abm-pos-com-ip
    STAGING_DB_STATIC_IP_NAME=staging-abm-pos-com-ip
    STAGING_INMEMORY_STATIC_IP_NAME=im-staging-abm-pos-com-ip

    gcloud compute addresses create $MAIN_DB_STATIC_IP_NAME --global --project point-of-sale-ci
    gcloud compute addresses create $MAIN_INMEMORY_STATIC_IP_NAME --global --project point-of-sale-ci
    gcloud compute addresses create $STAGING_DB_STATIC_IP_NAME --global --project point-of-sale-ci
    gcloud compute addresses create $STAGING_INMEMORY_STATIC_IP_NAME --global --project point-of-sale-ci
    ```
2. Create a `ManagedCertificate` that uses the intended domain
    ```sh
    # apiVersion: networking.gke.io/v1
    # kind: ManagedCertificate
    # metadata:
    #   name: pos-certificate
    #   namespace: release-db
    # spec:
    #   domains:
    #     - point-of-sale.retail.cymbal.dev

    kubectl apply -f k8-manifests/util/certificates.yaml
    ```
3. Create an `Ingress` that refers to the: `Global Static IP`, `ManagedCertificate` and the `K8s Service`
    ```sh
    # apiVersion: networking.k8s.io/v1
    # kind: Ingress
    # metadata:
    #   name: pos-ingress
    #   namespace: release-db
    #   annotations:
    #     kubernetes.io/ingress.global-static-ip-name: abm-pos-com-ip
    #     networking.gke.io/managed-certificates: pos-certificate
    # spec:
    #   defaultBackend:
    #     service:
    #       name: api-server-svc
    #       port:
    #         number: 8080

    kubectl apply -f k8-manifests/util/ingresses.yaml
    ```
5. [Add a custom record](https://support.google.com/domains/answer/3290350?visit_id=637879921124266523-2802260515&rd=1) to the domain under Google Domains