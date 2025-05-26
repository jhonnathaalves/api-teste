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
                sh '''
                    pwd
                    mvn clean verify'''
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
               withCredentials([string(credentialsId: 'SEMGREP_TOKEN', variable: 'SEMGREP_TOKEN')]) {
               sh '''    
               docker run --rm \
                   -e SEMGREP_APP_TOKEN=$SEMGREP_TOKEN \
                   -v $(pwd):/src \
                   -v $HOME/.semgrep:/home/semgrep/.semgrep \
                   returntocorp/semgrep semgrep \
                   --config=p/owasp-top-ten /src \
                   --sarif --output=/src/semgrep.sarif
               '''
               }
            }
        }

        stage('Secrets Scan - Gitleaks') {
            steps {
                sh '''
                echo '{"version":"2.1.0","runs":[]}' > gitleaks.sarif
                docker run --rm \
                    -v $(pwd):/repo \
                    zricethezav/gitleaks:latest detect \
                    --source=/repo --no-git \
                    --report-format=sarif \
                    --report-path=/repo/gitleaks.sarif || true
                '''
            }
        }

        stage('SCA - Trivy (File System)') {
            steps {
                sh '''
                mkdir -p trivy-reports
                docker run --rm \
                    -v $(pwd):/app \
                    aquasec/trivy fs /app \
                    --format sarif \
                    -o "/app/trivy-report-fs.sarif"
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
                 mkdir -p trivy-reports                 
                 docker run --rm \
                    -v $(pwd):/app \
                    -v /var/run/docker.sock:/var/run/docker.sock \
                    -v "$HOME/.docker":/root/.docker \
                    -e DOCKER_HOST=unix:///var/run/docker.sock \
                    aquasec/trivy image \
                    --format sarif \
                    -o "/app/trivy-report-image.sarif" \
                    --exit-code 0 --severity HIGH,CRITICAL \
                    "$DOCKER_IMAGE"
                '''
            }
        }

        stage('Quality Gate - SonarQube') {
            steps {
                waitForQualityGate abortPipeline: true
            }
        }
        stage('Publish SARIF Findings') {
            steps {
              recordIssues(
                enabledForFailure: true,
                tools: [
                  sarif(id: 'semgrep-sarif', name: 'Semgrep Findings', pattern: 'semgrep.sarif'),
                  sarif(id: 'gitleaks-sarif', name: 'Gitleaks Findings', pattern: 'gitleaks.sarif'),
                  sarif(id: 'trivy-fs-sarif', name: 'Trivy fs Scan', pattern: 'trivy-report-fs.sarif'),
                  sarif(id: 'trivy-image-sarif', name: 'Trivy Image Scan', pattern: 'trivy-report-image.sarif')                  
                ]
              )
            }
        }
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
            archiveArtifacts artifacts: '*.sarif', allowEmptyArchive: true
            archiveArtifacts artifacts: '*.html', allowEmptyArchive: true
        }

        failure {
            echo 'Pipeline failed. Check the security and quality reports.'
        }
    }
}
