pipeline {
    agent any 
    stages {
        stage('PULL') {
            steps {
                git branch: 'main', url: 'https://github.com/abhiharde07/eks-infra.git'
            }
        }
        stage ('PLAN') {
            steps {
                sh ''' terraform init
                    terraform plan '''
           }
        }
        stage ('APPROVAL') {
            steps {
                timeout(30) {
                            input 'Shall we proceed? ok: APPROVED'
                            }
            }
        }
        stage ('APPLY') {
            steps {
                sh 'terraform apply --auto-approve'
            }
        }
    }
}