pipeline{
    agent any

   parameters {
        choice(
            name: 'ACTION',
            choices: ['apply', 'destroy'],
            description: 'Terraform Action'
        )
    }

    stages{
        stage("PULL"){
            steps{
                git branch: 'master', url: 'https://github.com/kartik-bhapkar/jenkins.git'
            }
        }

        stage("TERRAFORM-INIT"){
            steps{
                 sh '''cd eks-cluster
                    terraform init'''
            }
        }

        stage('TERRAFORM-PLAN') {
            steps {
                script {
                    if (params.ACTION == "apply") {
                        sh '''cd eks-cluster
                            terraform plan'''
                    } else {
                        sh '''cd eks-cluster
                            terraform plan -destroy'''
                    }
                }
            }
        }
        
        stage("APPROVE"){
            steps{
                timeout(30) {
                        input 'Do you want to ${params.ACTION} infrastructure?'
                    }
                
            }
        }

        stage("EXECUTE"){
            steps{
                script {
                    if (params.ACTION == "apply") {
                        sh '''cd eks-cluster 
                            terraform apply -auto-approve'''
                    }else{
                        sh ''' cd eks-cluster terraform destroy -auto-approve'''
                    }
                }
            }
        }
    }
}
