pipeline {
    agent any 
    stages {
        stage('PULL') {
            steps {
                git branch: 'master', url: 'https://github.com/kartik-bhapkar/jenkins.git'
            }
        }
        stage ('PLAN') {
            steps {
                sh ''' cd eks-cluster
                    terraform init
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