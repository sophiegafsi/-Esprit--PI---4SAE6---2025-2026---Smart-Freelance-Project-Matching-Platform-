pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk   'Java-17'
    }

    environment {
        IMAGE_NAME    = "reservation-service"
        NAMESPACE     = "reservation-ns"
        SONAR_TOKEN   = credentials('sonar-token')   // Jenkins secret
        DB_PASSWORD   = credentials('db-password')   // Jenkins secret
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

        // ── Stage 3: SonarQube Quality Gate ───────────────────────────
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
                echo "Image built: ${IMAGE_NAME}:${BUILD_NUMBER}"
            }
        }

        // ── Stage 6: Deploy to Kubernetes (kubeadm) ───────────────────
        stage('Deploy to Kubernetes') {
            steps {
                sh "kubectl apply -f k8s/ -n ${NAMESPACE}"
                sh """
                    kubectl set image deployment/reservation-app \
                        reservation-app=${IMAGE_NAME}:${BUILD_NUMBER} \
                        -n ${NAMESPACE}
                """
                sh "kubectl rollout status deployment/reservation-app -n ${NAMESPACE} --timeout=120s"
            }
        }

        // ── Stage 7: Health Check ──────────────────────────────────────
        stage('Health Check') {
            steps {
                script {
                    def nodeIp = sh(
                        script: "kubectl get nodes -o jsonpath='{.items[0].status.addresses[0].address}'",
                        returnStdout: true
                    ).trim()
                    sh "sleep 15 && curl -f http://${nodeIp}:30088/actuator/health"
                    echo "✅ Service is UP at http://${nodeIp}:30088"
                }
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline SUCCESS — Branch: ${env.BRANCH_NAME} | Build: #${BUILD_NUMBER}"
        }
        failure {
            echo "❌ Pipeline FAILED — Check logs above"
        }
        always {
            cleanWs()
        }
    }
}
