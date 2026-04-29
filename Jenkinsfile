pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk   'Java-17'
    }

    environment {
        IMAGE_NAME    = "reservation-service"
        SONAR_TOKEN   = credentials('sonar-token')
    }

    stages {

        // ── Stage 1: Checkout ──────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch: ${env.BRANCH_NAME} | Build: #${env.BUILD_NUMBER}"
            }
        }

        // ── Stage 2: Unit Tests + Jacoco coverage ──────────────────────
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
                        sourcePattern: 'src/main/java',
                        minimumInstructionCoverage: '70'
                    )
                }
            }
        }

        // ── Stage 3: SonarQube Analysis ───────────────────────────
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh """
                        ./mvnw sonar:sonar \
                          -Dsonar.projectKey=${IMAGE_NAME} \
                          -Dsonar.login=${SONAR_TOKEN} \
                          -q
                    """
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

        // ── Stage 4: Package ───────────────────────────────────────────
        stage('Package') {
            steps {
                sh './mvnw package -DskipTests -q'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        // ── Stage 5: Docker Build ──────────────────────────────────────
        stage('Docker Build') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${BUILD_NUMBER} -t ${IMAGE_NAME}:latest ."
                echo "✅ Image built successfully: ${IMAGE_NAME}:${BUILD_NUMBER}"
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline SUCCESS — Image ready at ${IMAGE_NAME}:latest"
        }
        failure {
            echo "❌ Pipeline FAILED — Check the logs above"
        }
    }
}
