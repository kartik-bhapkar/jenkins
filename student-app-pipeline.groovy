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
                    kubectl apply -f . '''
            }
        }

        stage('BACKEND-BUILD') {
            steps {
                sh '''cd backend
                    mvn clean package -DskipTests'''
            }
        }

        stage('BACKEND-TEST') {
             steps {
                withSonarQubeEnv(installationName: 'sonarqube',credentialsId: 'sonar-cred') { 
                     sh '''cd backend
                       mvn sonar:sonar \
                         -Dsonar.projectKey=jenkins'''
                }
             }
         }

        stage('FRONTEND') {
            steps {
                sh '''cd frontend/yaml
                    kubectl apply -f .'''
            }
        }

        stage('FRONTEND-BUILD') {
            steps {
                sh '''cd frontend
                       npm install
                       npm run build'''
            }
        }
        
        stage('FRONTEND-TEST') {
            steps {
                script {
                    def scannerHome = tool 'sonarscanner'

                    withSonarQubeEnv(installationName: 'sonarscanner', credentialsId: 'sscanner-cred') {
                        sh """
                            cd frontend
                            ${scannerHome}/bin/sonar-scanner 
                                -Dsonar.projectKey=student-app-frontend \
                                -Dsonar.sources=. \
                        """
                    }
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
