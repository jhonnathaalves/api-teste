pipeline {
    agent any

    tools {
        maven 'MAVEN_3.9'
        jdk 'JAVA_17'
    }

    environment {
        DOCKER_IMAGE = "api-devops:${BUILD_NUMBER}"
    }

    options {
        skipDefaultCheckout()
    }

    stages {
        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }

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
                withCredentials([string(credentialsId: 'SEMGREP_TOKEN', variable: 'SEMGREP_TOKEN')]) {
                    sh '''
                    export SEMGREP_APP_TOKEN=$SEMGREP_APP_TOKEN
                    semgrep \
                        --config=p/owasp-top-ten \
                        --sarif --output=semgrep.sarif \
                        .
                    '''
                }
            }
        }

        stage('Secrets Scan - Gitleaks') {
            steps {
                sh '''
                echo '{"version":"2.1.0","runs":[]}' > gitleaks.sarif
                gitleaks detect \
                    --source=. --no-git \
                    --report-format=sarif \
                    --report-path=gitleaks.sarif || true
                '''
            }
        }

        stage('SCA - Trivy (File System)') {
            steps {
                sh '''
                trivy fs . \
                    --format sarif \
                    --output trivy-report-fs.sarif \
                    --exit-code 0 \
                    --severity HIGH,CRITICAL
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
                trivy image "$DOCKER_IMAGE" \
                    --timeout 5m \
                    --format sarif \
                    --output trivy-report-image.sarif \
                    --exit-code 0 \
                    --severity HIGH,CRITICAL
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
