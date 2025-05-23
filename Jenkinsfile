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

        stage('Convert SARIF to HTML') {
            steps {
                sh '''
                docker run --rm -v $(pwd):/workdir sarif-html:latest \
                    /workdir/semgrep.sarif \
                    /workdir/semgrep-report.html

                docker run --rm -v $(pwd):/workdir sarif-html:latest \
                    /workdir/gitleaks.sarif \
                    /workdir/gitleaks-report.html
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
                    -o /app/trivy-report-fs.html \
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
                 docker save -o api-devops.tar $DOCKER_IMAGE

                 docker run --rm \
                    -v $(pwd):/app \
                    aquasec/trivy image \
                    --input /app/api-devops.tar \
                    --format template \
                    --template "@contrib/html.tpl" \
                    -o /app/trivy-report-image.html \
                    --exit-code 0 --severity HIGH,CRITICAL \
					$DOCKER_IMAGE
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
