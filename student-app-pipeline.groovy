pipeline {
    agent any
    stages {
        stage('PULL') {
            steps {
                git branch: 'master', url: 'https://github.com/kartik-bhapkar/student-app-deployment-and-monitoring.git'
            }
        }
        stage('DATABASE') {
            steps {
                sh '''cd yaml
                    kubectl apply -f .'''
            }
        }

        stage('BACKEND') {
            steps {
                sh '''cd backend/yaml
                    kubectl apply -f svc.yml,hpa.yml,deploy.yml '''
            }
        }

        stage('FRONTEND') {
            steps {
                sh '''cd frontend/yaml
                    kubectl apply -f .'''
            }
        }
         stage('TEST') {
             steps {
                withSonarQubeEnv(installationName: 'sonarqube',credentialsId: 'sonar-cred') { 
                     sh '''cd backend
                       mvn sonar:sonar \
                         -Dsonar.projectKey=jenkins'''
                }
             }
         }
        stage ('Quality-Gate') {
            steps {
                timeout(10) {
                    waitForQualityGate abortPipeline: true, credentialsId: 'sonar-cred'
                }
        }
        }
        stage ('Delivery') {
            steps {
                sh 'aws s3 cp backend/target/student-registration-backend-0.0.1-SNAPSHOT.jar s3://kartik007-363/studentapp.jar'
            }
        }

        stage('DEPLOY') {
            steps {
                echo 'DEPLOY SUCCSESS'
            }
        }
    }
}