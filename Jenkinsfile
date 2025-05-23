pipeline {
    agent any

    tools {
        maven 'Maven 3.9'
        jdk 'JDK_17'
    }

    environment {
        SONARQUBE_ENV = credentials('sonarqube-token')
        DOCKER_IMAGE = "meuapp:${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Unit Tests') {
            steps {
                sh 'mvn clean verify'
            }
        }

        stage('SonarQube Scan') {
            steps {
                withSonarQubeEnv('sonarqube-server') {
                    sh '''
                    mvn sonar:sonar \
                        -Dsonar.projectKey=meu-app \
                        -Dsonar.host.url=$SONAR_HOST_URL \
                        -Dsonar.login=$SONAR_AUTH_TOKEN
                    '''
                }
            }
        }

        stage('SAST - Semgrep') {
            steps {
                sh '''
                docker run --rm \
                    -v $(pwd):/src \
                    returntocorp/semgrep semgrep \
                    --config=auto /src
                '''
            }
        }

        stage('Secrets Scan - Gitleaks') {
            steps {
                sh '''
                docker run --rm -v $(pwd):/repo zricethezav/gitleaks:latest detect \
                    --source=/repo --no-git \
                    --report-format=json \
                    --report-path=gitleaks-report.json
                '''
            }
        }

        stage('SCA - Trivy (File System)') {
            steps {
                sh '''
                docker run --rm -v $(pwd):/app aquasec/trivy fs /app \
                    --exit-code 0 --severity HIGH,CRITICAL
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                sh "docker build -t $DOCKER_IMAGE ."
            }
        }

        stage('Trivy Image Scan') {
            steps {
                sh '''
                docker run --rm \
                    -v /var/run/docker.sock:/var/run/docker.sock \
                    aquasec/trivy image $DOCKER_IMAGE
                '''
            }
        }
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        }
        failure {
            echo 'Pipeline failed. Check reports and fix issues.'
        }
    }
}
