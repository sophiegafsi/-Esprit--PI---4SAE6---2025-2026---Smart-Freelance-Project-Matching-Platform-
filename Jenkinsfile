pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'Java-17'
    }

    environment {
        IMAGE_NAME = "dalirom123/reservation-service"
        SONAR_TOKEN = credentials('sonar-token')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch: ${env.BRANCH_NAME} | Build: #${env.BUILD_NUMBER}"
            }
        }

        stage('Unit Tests') {
            steps {
                sh './mvnw clean verify -q'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    jacoco(
                        execPattern: 'target/*.exec',
                        classPattern: 'target/classes',
                        sourcePattern: 'src/main/java'
                    )
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        ./mvnw sonar:sonar \
                        -Dsonar.projectKey=reservation-service \
                        -Dsonar.login=$SONAR_TOKEN \
                        -q
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Package') {
            steps {
                sh './mvnw package -DskipTests -q'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Docker Build') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} -t ${IMAGE_NAME}:latest ."
                echo "✅ Image built successfully: ${IMAGE_NAME}:${BUILD_NUMBER}"
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_TOKEN'
                )]) {
                    sh '''
                        echo "$DOCKER_TOKEN" | docker login -u "$DOCKER_USER" --password-stdin
                        docker push $IMAGE_NAME:latest
                        docker push $IMAGE_NAME:$BUILD_NUMBER
                    '''
                }
            }
        }

        stage('Monitoring Config Check') {
            steps {
                sh '''
                    echo "Checking monitoring files..."
                    test -f monitoring/docker-compose-monitoring.yml
                    test -f monitoring/prometheus.yml
                    echo "✅ Monitoring files exist"
                '''
            }
        }

        stage('Monitoring Deploy') {
            steps {
                sh 'docker compose -f monitoring/docker-compose-monitoring.yml up -d'
                echo "✅ Monitoring stack (Prometheus/Grafana) started"
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline SUCCESS — Image pushed to Docker Hub: ${IMAGE_NAME}:latest"
        }
        failure {
            echo "❌ Pipeline FAILED — Check the logs above"
        }
    }
}
