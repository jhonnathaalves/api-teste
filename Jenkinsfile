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
               withCredentials([string(credentialsId: 'SEMGREP_TOKEN', variable: 'SEMGREP_TOKEN')]) {
               sh '''
               docker run --rm \
                   -e SEMGREP_TOKEN=$SEMGREP_TOKEN \
                   -v $(pwd):/src \
                   -v $HOME/.semgrep:/home/semgrep/.semgrep \
                   returntocorp/semgrep semgrep login

               docker run --rm \
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
                docker run --rm \
                    -v $(pwd):/repo \
                    zricethezav/gitleaks:latest detect \
                    --source=/repo --no-git \
                    --report-format=sarif \
                    --report-path=/repo/gitleaks.sarif
                '''
            }
        }

        stage('Convert SARIF to HTML') {
            steps {
                sh '''
                docker run --rm -v $(pwd):/workdir sarif-html:latest \
                    --input /workdir/semgrep.sarif \
                    --output /workdir/semgrep-report.html

                docker run --rm -v $(pwd):/workdir sarif-html:latest \
                    --input /workdir/gitleaks.sarif \
                    --output /workdir/gitleaks-report.html
                '''
            }
        }

        stage('SCA - Trivy (File System)') {
            steps {
                sh '''
                docker run --rm \
                    -v $(pwd):/app \
                    aquasec/trivy fs /app \
                    --format template \
                    --template "@contrib/html.tpl" \
                    -o trivy-report.html \
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

        stage('Quality Gate - SonarQube') {
            steps {
                waitForQualityGate abortPipeline: true
            }
        }
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
            archiveArtifacts artifacts: '*.html', allowEmptyArchive: true
        }

        failure {
            echo 'Pipeline failed. Check the security and quality reports.'
        }
    }
}
