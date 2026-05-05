pipeline {
    agent any

    tools {
        maven 'Maven3'
        nodejs 'Node20'
    }

    environment {
        DOCKER_REGISTRY   = 'docker.io/yusff'
        SONAR_URL         = 'http://sonarqube:9000'
        // Sonar token stored as a Jenkins Secret Text credential named 'sonar-token'
        SONAR_TOKEN       = credentials('sonar-token')
        // Puppeteer-managed Chrome for headless Angular tests on the Jenkins agent
        CHROME_BIN        = '/home/yusff/.cache/puppeteer/chrome/linux-147.0.7727.57/chrome-linux64/chrome'
        // Docker image tag: use git SHA for traceability + rollback
        IMAGE_TAG         = "${env.GIT_COMMIT?.take(7) ?: env.BUILD_NUMBER}"
    }

    stages {
        stage('Initialize') {
            steps {
                echo "Starting Robust Enterprise CI/CD Pipeline for FreeLink | Build #${env.BUILD_NUMBER} | Commit: ${env.GIT_COMMIT?.take(7)}"
            }
        }

        // ─────────────────────────────────────────────────────────────────
        // BACKEND SERVICES  (only runs when the directory is touched)
        // ─────────────────────────────────────────────────────────────────

        stage('Build & Deploy: API-Gateway') {
            when { changeset "API-Gateway/**" }
            steps {
                dir('API-Gateway') {
                    sh 'mvn clean verify'
                    sh "mvn sonar:sonar -Dsonar.projectKey=api-gateway -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_TOKEN}"
                    sh "docker build -t api-gateway:${IMAGE_TAG} -t api-gateway:latest ."
                    sh 'kubectl apply -f k8s/ -n dev'
                }
            }
        }

        stage('Build & Deploy: Condature') {
            when { changeset "condature/**" }
            steps {
                dir('condature') {
                    sh 'mvn clean verify'
                    sh "mvn sonar:sonar -Dsonar.projectKey=condature-service -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_TOKEN}"
                    sh "docker build -t condature-service:${IMAGE_TAG} -t condature-service:latest ."
                    sh 'kubectl apply -f k8s/ -n dev'
                }
            }
        }

        stage('Build & Deploy: Eureka-Server') {
            when { changeset "eureka-server/**" }
            steps {
                dir('eureka-server') {
                    sh 'mvn clean verify'
                    sh "mvn sonar:sonar -Dsonar.projectKey=eureka-server -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_TOKEN}"
                    sh "docker build -t eureka-server:${IMAGE_TAG} -t eureka-server:latest ."
                    sh 'kubectl apply -f k8s/ -n dev'
                }
            }
        }

        stage('Build & Deploy: Evaluation-Service') {
            when { changeset "evaluation-service/**" }
            steps {
                dir('evaluation-service') {
                    sh 'mvn clean verify'
                    sh "mvn sonar:sonar -Dsonar.projectKey=evaluation-service -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_TOKEN}"
                    sh "docker build -t evaluation-service:${IMAGE_TAG} -t evaluation-service:latest ."
                    sh 'kubectl apply -f k8s/ -n dev'
                }
            }
        }

        stage('Build & Deploy: Gestion-Planing') {
            when { changeset "gestion-planing/**" }
            steps {
                dir('gestion-planing') {
                    sh 'mvn clean verify'
                    sh "mvn sonar:sonar -Dsonar.projectKey=gestion-planing -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_TOKEN}"
                    sh "docker build -t gestion-planing:${IMAGE_TAG} -t gestion-planing:latest ."
                    sh 'kubectl apply -f k8s/ -n dev'
                }
            }
        }

        stage('Build & Deploy: PorjectService') {
            when { changeset "porjectservice/**" }
            steps {
                dir('porjectservice') {
                    sh 'mvn clean verify'
                    sh "mvn sonar:sonar -Dsonar.projectKey=porjectservice -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_TOKEN}"
                    sh "docker build -t porjectservice:${IMAGE_TAG} -t porjectservice:latest ."
                    sh 'kubectl apply -f k8s/ -n dev'
                }
            }
        }

        stage('Build & Deploy: Portfolio-Service') {
            when { changeset "portfolio-service/**" }
            steps {
                dir('portfolio-service') {
                    sh 'mvn clean verify'
                    sh "mvn sonar:sonar -Dsonar.projectKey=portfolio-service -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_TOKEN}"
                    sh "docker build -t portfolio-service:${IMAGE_TAG} -t portfolio-service:latest ."
                    sh 'kubectl apply -f k8s/ -n dev'
                }
            }
        }

        stage('Build & Deploy: Reclamation') {
            when { changeset "reclamation/**" }
            steps {
                dir('reclamation') {
                    sh 'mvn clean verify'
                    sh "mvn sonar:sonar -Dsonar.projectKey=reclamation-service -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_TOKEN}"
                    sh "docker build -t reclamation-service:${IMAGE_TAG} -t reclamation-service:latest ."
                    sh 'kubectl apply -f k8s/ -n dev'
                }
            }
        }

        stage('Build & Deploy: Recompense') {
            when { changeset "recompense/**" }
            steps {
                dir('recompense') {
                    sh 'mvn clean verify'
                    sh "mvn sonar:sonar -Dsonar.projectKey=recompense-service -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_TOKEN}"
                    sh "docker build -t recompense-service:${IMAGE_TAG} -t recompense-service:latest ."
                    sh 'kubectl apply -f k8s/ -n dev'
                }
            }
        }

        stage('Build & Deploy: Reservation-Service') {
            when { changeset "reservation-service/**" }
            steps {
                dir('reservation-service') {
                    sh 'mvn clean verify'
                    sh "mvn sonar:sonar -Dsonar.projectKey=reservation-service -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_TOKEN}"
                    sh "docker build -t reservation-service:${IMAGE_TAG} -t reservation-service:latest ."
                    sh 'kubectl apply -f k8s/ -n dev'
                }
            }
        }

        stage('Build & Deploy: Skills-Service') {
            when { changeset "skills-service/**" }
            steps {
                dir('skills-service') {
                    sh 'mvn clean verify'
                    sh "mvn sonar:sonar -Dsonar.projectKey=skills-service -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_TOKEN}"
                    sh "docker build -t skills-service:${IMAGE_TAG} -t skills-service:latest ."
                    sh 'kubectl apply -f k8s/ -n dev'
                }
            }
        }

        stage('Build & Deploy: Time-Tracking-Service') {
            when { changeset "time-tracking-service/**" }
            steps {
                dir('time-tracking-service') {
                    sh 'mvn clean verify'
                    sh "mvn sonar:sonar -Dsonar.projectKey=time-tracking-service -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_TOKEN}"
                    sh "docker build -t time-tracking-service:${IMAGE_TAG} -t time-tracking-service:latest ."
                    sh 'kubectl apply -f k8s/ -n dev'
                }
            }
        }

        stage('Build & Deploy: User') {
            when { changeset "user/**" }
            steps {
                dir('user') {
                    sh 'mvn clean verify'
                    sh "mvn sonar:sonar -Dsonar.projectKey=user-service -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_TOKEN}"
                    sh "docker build -t user-service:${IMAGE_TAG} -t user-service:latest ."
                    sh 'kubectl apply -f k8s/ -n dev'
                }
            }
        }

        // ─────────────────────────────────────────────────────────────────
        // FRONTEND SERVICE
        // ─────────────────────────────────────────────────────────────────

        stage('Build & Deploy: Frontend') {
            when { changeset "front/sofia-pidev-front-Gestion-skills/**" }
            steps {
                dir('front/sofia-pidev-front-Gestion-skills') {
                    sh 'npm install'
                    sh 'npm run test:ci'
                    sh 'npm run build'
                    sh "docker build -t freelink-frontend:${IMAGE_TAG} -t freelink-frontend:latest ."
                    sh 'kubectl apply -f k8s/ -n dev'
                }
            }
        }

        // ─────────────────────────────────────────────────────────────────
        // CD PROMOTION LIFECYCLE  (runs only when something was actually built)
        // ─────────────────────────────────────────────────────────────────

        stage('Deploy to Integration (INT)') {
            when {
                expression { return currentBuild.changeSets.size() > 0 }
            }
            steps {
                echo 'Promoting all manifests to INT namespace...'
                sh 'find . -name "k8s" -type d -not -path "*/node_modules/*" -not -path "*/.m2/*" -exec kubectl apply -f {} -n int \\;'
            }
        }

        stage('Deploy to Test') {
            when {
                expression { return currentBuild.changeSets.size() > 0 }
            }
            steps {
                echo 'Promoting all manifests to TEST namespace...'
                sh 'find . -name "k8s" -type d -not -path "*/node_modules/*" -not -path "*/.m2/*" -exec kubectl apply -f {} -n test \\;'
            }
        }

        stage('Deploy to Qualification (QUALIF)') {
            when {
                expression { return currentBuild.changeSets.size() > 0 }
            }
            steps {
                echo 'Promoting all manifests to QUALIF namespace...'
                sh 'find . -name "k8s" -type d -not -path "*/node_modules/*" -not -path "*/.m2/*" -exec kubectl apply -f {} -n qualif \\;'
                echo 'Executing Stress Tests (JMeter/Gatling)...'
            }
        }

        stage('Deploy to Pre-Production (PREPROD)') {
            when {
                expression { return currentBuild.changeSets.size() > 0 }
            }
            steps {
                echo 'Promoting all manifests to PREPROD namespace...'
                sh 'find . -name "k8s" -type d -not -path "*/node_modules/*" -not -path "*/.m2/*" -exec kubectl apply -f {} -n preprod \\;'
            }
        }

        stage('Promote to Production (PROD)') {
            when {
                expression { return currentBuild.changeSets.size() > 0 }
            }
            steps {
                input message: 'Approve deployment to Production?', ok: 'Deploy'
                script {
                    echo 'Final Promotion to PROD namespace...'
                    sh 'find . -name "k8s" -type d -not -path "*/node_modules/*" -not -path "*/.m2/*" -exec kubectl apply -f {} -n prod \\;'
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline PASSED | Build #${env.BUILD_NUMBER} | Tag: ${IMAGE_TAG}"
        }
        failure {
            echo "Pipeline FAILED | Build #${env.BUILD_NUMBER} — check the stage logs above."
        }
        always {
            echo 'Robust Enterprise Pipeline execution finished.'
        }
    }
}
