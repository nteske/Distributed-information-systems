# Kubernetes

## Introducing Kubernetes API objects

- Node: A node represents a server, virtual or physical, in the cluster.
- Pod: A pod represents the smallest possible deployable component in Kubernetes, consisting of one or more co-located containers.
Typically, a pod consists of one container, but there are use cases for extending the functionality of the main container by running the second container in a pod.
- Deployment: Deployment is used to deploy and upgrade pods.
The deployment objects hand over the responsibility of creating and monitoring the pods to a ReplicaSet.
When creating a deployment for the first time, the work performed by the deployment object is no much more than creating the ReplicaSet object.
When performing a rolling upgrade of deployment, the role of the deployment object is more involved.
- ReplicaSet: A ReplicaSet is used to ensure that a specified number of pods are running at all times.
If a pod is deleted, it will be replaced with a new pod by the ReplicaSet.
- Service: A service is a stable network endpoint that you can use to connect to one or multiple pods.
A service is assigned an IP address and a DNS name in the internal network of the Kubernetes cluster.
The IP address of the service will stay the same for the lifetime of the service.
Requests that are sent to a service will be forwarded to one of the available pods using round-robin-based load balancing.
By default, a service is only exposed inside the cluster using a cluster IP address.
It is also possible to expose a service outside the cluster, either on a dedicated port on each node in the cluster or – even better – through an external load balancer that is aware of Kubernetes, that is, it can automatically provision a public IP address and/or DNS name for the service.
Cloud providers that offer Kubernetes as a service, in general, support this type of load balancer.
- Ingress: Ingress can manage external access to services in a Kubernetes cluster, typically using HTTP.
For example, it can route traffic to the underlying services based on URL paths or HTTP headers such as the hostname.
Instead of exposing a number of services externally, either using node ports or through load balancers, it is, in general, more convenient to set up an Ingress in front of the services.
To handle the actual communication defined by the Ingress objects, an Ingress controller must be running in the cluster.
We will see an example of an Ingress controller as we proceed.
- Namespace: A namespace is used to group and, on some levels, isolate resources in a Kubernetes cluster.
The names of resources must be unique in their namespaces, but not between namespaces.
ConfigMap: ConfigMap is used to store configuration that's used by containers.
ConfigMaps can be mapped into a running container as environment variables or files.
- Secret: This is used to store sensitive data used by containers, such as credentials.
Secrets can be made available to containers in the same way as ConfigMaps.
Anyone with full access to the API server can access the values of created secrets, so they are not as safe as the name might imply.
- DaemonSet: This ensures that one pod is running on each node in a set of nodes in the cluster.
In Chapter 19 , Centralized Logging with the EFK Stack, we will see an example of a log collector, Fluentd, that will run on each worker node.

## Running commands for deploying and testing
`minikube start`\
`bash ./kubernetes/scripts/deploy-dev-env.bash`

## Cleaning up
`kubectl delete namespace dis`\
`docker-compose down`
