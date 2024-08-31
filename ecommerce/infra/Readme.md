run terraform apply
then run
kubectl patch deployment coredns \
    -n kube-system \
    --type json \
    -p='[{"op": "remove", "path": "/spec/template/metadata/annotations/eks.amazonaws.com~1compute-type"}]'


cd helm-release
export TF_VAR_application_vpc_id=${application_vpc_id} && \
            export TF_VAR_load_balancer_arn=${AWS_LB_ROLE_ARN} && \
            export TF_VAR_region=${region} && \
            export TF_VAR_cluster_id=${EKS_CLUSTER_ID} && \
            export TF_VAR_role_arn=${role_arn} && \
            export TF_VAR_cluster_name=${EKS_CLUSTER_NAME} && terraform apply

abis itu cd ke load balancer 
export TF_VAR_public_vpc_id=vpc-01eaa73077b68106c && export TF_VAR_cluster_name=eks-cluster && export TF_VAR_load_balancer_arn=arn:aws:iam::959896818063:role/aws-load-balancer-controller && terraform apply (contoh)