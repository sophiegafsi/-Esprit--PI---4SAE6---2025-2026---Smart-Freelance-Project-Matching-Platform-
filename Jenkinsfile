pipeline {
    agent any

    tools {
        maven 'Maven3'
        nodejs 'Node20'
    }

    environment {
        DOCKER_REGISTRY = 'docker.io/yusff' // Example registry
        KUBECONFIG_CREDENTIAL_ID = 'kubeconfig-dev'
    }

    stages {
        stage('Initialize') {
            steps {
                echo 'Starting Multi-Service Pipeline for FreeLink...'
            }
        }

        // --- BACKEND SERVICES ---

        stage('Build & Deploy: API-Gateway') {
            when { changeset "API-Gateway/**" }
            steps {
                dir('API-Gateway') {
                    sh 'mvn clean verify'
                    sh 'docker build -t api-gateway:latest .'
                    sh 'kubectl apply -f k8s/ -n dev'
                    sh 'kubectl rollout restart deployment api-gateway -n dev'
                }
            }
        }

        stage('Build & Deploy: Condature') {
            when { changeset "condature/**" }
            steps {
                dir('condature') {
                    sh 'mvn clean verify'
                    sh 'docker build -t condature-service:latest .'
                    sh 'kubectl apply -f k8s/ -n dev'
                    sh 'kubectl rollout restart deployment condature-service -n dev'
                }
            }
        }

        stage('Build & Deploy: Eureka-Server') {
            when { changeset "eureka-server/**" }
            steps {
                dir('eureka-server') {
                    sh 'mvn clean verify'
                    sh 'docker build -t eureka-server:latest .'
                    sh 'kubectl apply -f k8s/ -n dev'
                    sh 'kubectl rollout restart deployment eureka-server -n dev'
                }
            }
        }

        stage('Build & Deploy: Evaluation-Service') {
            when { changeset "evaluation-service/**" }
            steps {
                dir('evaluation-service') {
                    sh 'mvn clean verify'
                    sh 'docker build -t evaluation-service:latest .'
                    sh 'kubectl apply -f k8s/ -n dev'
                    sh 'kubectl rollout restart deployment evaluation-service -n dev'
                }
            }
        }

        stage('Build & Deploy: Gestion-Planing') {
            when { changeset "gestion-planing/**" }
            steps {
                dir('gestion-planing') {
                    sh 'mvn clean verify'
                    sh 'docker build -t gestion-planing:latest .'
                    sh 'kubectl apply -f k8s/ -n dev'
                    sh 'kubectl rollout restart deployment gestion-planing -n dev'
                }
            }
        }

        stage('Build & Deploy: PorjectService') {
            when { changeset "porjectservice/**" }
            steps {
                dir('porjectservice') {
                    sh 'mvn clean verify'
                    sh 'docker build -t porjectservice:latest .'
                    sh 'kubectl apply -f k8s/ -n dev'
                    sh 'kubectl rollout restart deployment porjectservice -n dev'
                }
            }
        }

        stage('Build & Deploy: Portfolio-Service') {
            when { changeset "portfolio-service/**" }
            steps {
                dir('portfolio-service') {
                    sh 'mvn clean verify'
                    sh 'docker build -t portfolio-service:latest .'
                    sh 'kubectl apply -f k8s/ -n dev'
                    sh 'kubectl rollout restart deployment portfolio-service -n dev'
                }
            }
        }

        stage('Build & Deploy: Reclamation') {
            when { changeset "reclamation/**" }
            steps {
                dir('reclamation') {
                    sh 'mvn clean verify'
                    sh 'docker build -t reclamation-service:latest .'
                    sh 'kubectl apply -f k8s/ -n dev'
                    sh 'kubectl rollout restart deployment reclamation-service -n dev'
                }
            }
        }

        stage('Build & Deploy: Recompense') {
            when { changeset "recompense/**" }
            steps {
                dir('recompense') {
                    sh 'mvn clean verify'
                    sh 'docker build -t recompense-service:latest .'
                    sh 'kubectl apply -f k8s/ -n dev'
                    sh 'kubectl rollout restart deployment recompense-service -n dev'
                }
            }
        }

        stage('Build & Deploy: Reservation-Service') {
            when { changeset "reservation-service/**" }
            steps {
                dir('reservation-service') {
                    sh 'mvn clean verify'
                    sh 'docker build -t reservation-service:latest .'
                    sh 'kubectl apply -f k8s/ -n dev'
                    sh 'kubectl rollout restart deployment reservation-service -n dev'
                }
            }
        }

        stage('Build & Deploy: Skills-Service') {
            when { changeset "skills-service/**" }
            steps {
                dir('skills-service') {
                    sh 'mvn clean verify'
                    sh 'docker build -t skills-service:latest .'
                    sh 'kubectl apply -f k8s/ -n dev'
                    sh 'kubectl rollout restart deployment skills-service -n dev'
                }
            }
        }

        stage('Build & Deploy: Time-Tracking-Service') {
            when { changeset "time-tracking-service/**" }
            steps {
                dir('time-tracking-service') {
                    sh 'mvn clean verify'
                    sh 'docker build -t time-tracking-service:latest .'
                    sh 'kubectl apply -f k8s/ -n dev'
                    sh 'kubectl rollout restart deployment time-tracking-service -n dev'
                }
            }
        }

        stage('Build & Deploy: User') {
            when { changeset "user/**" }
            steps {
                dir('user') {
                    sh 'mvn clean verify'
                    sh 'docker build -t user-service:latest .'
                    sh 'kubectl apply -f k8s/ -n dev'
                    sh 'kubectl rollout restart deployment user-service -n dev'
                }
            }
        }

        // --- FRONTEND SERVICE ---

        stage('Build & Deploy: Frontend') {
            when { changeset "front/sofia-pidev-front-Gestion-skills/**" }
            steps {
                dir('front/sofia-pidev-front-Gestion-skills') {
                    sh 'npm install'
                    sh 'npm run test:ci'
                    sh 'npm run build'
                    sh 'docker build -t freelink-frontend:latest .'
                    sh 'kubectl apply -f k8s/ -n dev'
                    sh 'kubectl rollout restart deployment freelink-frontend -n dev'
                }
            }
        }

        // --- PRODUCTION PROMOTION ---

        stage('Promote to Production') {
            steps {
                input message: 'Promote the current dev build to production?', ok: 'Promote'
                script {
                    echo 'Simulating production deployment to "prod" namespace...'
                    echo 'Tagging images as :prod and updating prod deployments...'
                    // Mock commands
                    // sh 'kubectl apply -f k8s/prod/ -n prod'
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline execution finished.'
        }
        success {
            echo 'Build successful! All changed services deployed to dev.'
        }
        failure {
            echo 'Build failed. Please check the logs.'
        }
    }
}
