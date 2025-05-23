pipeline {
    agent any

    tools {
        maven 'MAVEN_3.9'
        jdk 'JAVA_17'
    }

    environment {
        DOCKER_IMAGE = "api-devops:${BUILD_NUMBER}"
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
                        -Dsonar.projectKey=api-devops
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
                    --config=auto /src \
                    --json > semgrep-report.json
                '''
            }
        }

        stage('Secrets Scan - Gitleaks') {
            steps {
                sh '''
                docker run --rm \
                    -v $(pwd):/repo zricethezav/gitleaks:latest detect \
                    --source=/repo --no-git \
                    --report-format=json \
                    --report-path=gitleaks-report.json
                '''
            }
        }

        stage('SCA - Trivy (File System)') {
            steps {
                sh '''
                docker run --rm \
                    -v $(pwd):/app \
                    aquasec/trivy fs /app \
                    --exit-code 0 \
                    --severity HIGH,CRITICAL \
                    --format json \
                    --output trivy-fs-report.json
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
                    -v $(pwd):/root/reports \
                    aquasec/trivy image $DOCKER_IMAGE \
                    --exit-code 0 \
                    --severity HIGH,CRITICAL \
                    --format json \
                    --output /root/reports/trivy-image-report.json
                '''
            }
        }
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'

            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true

            // 📝 Arquivos de relatório de segurança
            archiveArtifacts artifacts: '**/*-report.json', allowEmptyArchive: true
        }

        failure {
            echo '❌ Pipeline failed. Check reports and fix issues.'
        }
    }
}
