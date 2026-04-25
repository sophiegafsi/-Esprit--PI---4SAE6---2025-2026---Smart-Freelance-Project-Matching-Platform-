pipeline {
    agent any
    
    tools {
        nodejs 'NodeJS-18'  // Configurer dans Jenkins si nécessaire
    }
    
    environment {
        // Registre Docker (à adapter)
        DOCKER_REGISTRY = 'localhost:5000'  // ou votre registry
        SONAR_HOST_URL = 'http://localhost:9000'
        
        // Tokens (à configurer dans Jenkins Credentials)
        SONAR_TOKEN_EVALUATION = credentials('sonar-token-evaluation')
        SONAR_TOKEN_RECOMPENSE = credentials('sonar-token-recompense')
        SONAR_TOKEN_FRONTEND = credentials('sonar-token-frontend')
    }
    
    stages {
        stage('Parallel CI/CD') {
            parallel {
                stage('Microservice: evaluation-service') {
                    steps {
                        script {
                            dir('evaluation-service') {
                                sh '''
                                    # Installation dépendances
                                    npm install
                                    
                                    # Tests unitaires
                                    npm test -- --coverage
                                '''
                            }
                        }
                    }
                    post {
                        success {
                            script {
                                dir('evaluation-service') {
                                    // SonarQube analyse
                                    withSonarQubeEnv('SonarQube') {
                                        sh """
                                            sonar-scanner \
                                              -Dsonar.projectKey=evaluation-service \
                                              -Dsonar.sources=. \
                                              -Dsonar.host.url=${SONAR_HOST_URL} \
                                              -Dsonar.login=${SONAR_TOKEN_EVALUATION} \
                                              -Dsonar.javascript.lcov.reportPaths=coverage/lcov.info \
                                              -Dsonar.coverage.exclusions=**/node_modules/**,**/tests/**
                                        """
                                    }
                                    
                                    // Build Docker image
                                    sh """
                                        docker build -t ${DOCKER_REGISTRY}/evaluation-service:latest .
                                        docker push ${DOCKER_REGISTRY}/evaluation-service:latest
                                    """
                                }
                            }
                        }
                    }
                }
                
                stage('Microservice: recompense-service') {
                    steps {
                        script {
                            dir('recompense/recompense') {
                                sh '''
                                    npm install
                                    npm test -- --coverage
                                '''
                            }
                        }
                    }
                    post {
                        success {
                            script {
                                dir('recompense/recompense') {
                                    withSonarQubeEnv('SonarQube') {
                                        sh """
                                            sonar-scanner \
                                              -Dsonar.projectKey=recompense-service \
                                              -Dsonar.sources=. \
                                              -Dsonar.host.url=${SONAR_HOST_URL} \
                                              -Dsonar.login=${SONAR_TOKEN_RECOMPENSE} \
                                              -Dsonar.javascript.lcov.reportPaths=coverage/lcov.info
                                        """
                                    }
                                    
                                    sh """
                                        docker build -t ${DOCKER_REGISTRY}/recompense-service:latest .
                                        docker push ${DOCKER_REGISTRY}/recompense-service:latest
                                    """
                                }
                            }
                        }
                    }
                }
                
                stage('Frontend') {
                    steps {
                        script {
                            dir('frontend') {
                                sh '''
                                    npm install
                                    npm test -- --coverage
                                    npm run build
                                '''
                            }
                        }
                    }
                    post {
                        success {
                            script {
                                dir('frontend') {
                                    withSonarQubeEnv('SonarQube') {
                                        sh """
                                            sonar-scanner \
                                              -Dsonar.projectKey=frontend-app \
                                              -Dsonar.sources=. \
                                              -Dsonar.host.url=${SONAR_HOST_URL} \
                                              -Dsonar.login=${SONAR_TOKEN_FRONTEND} \
                                              -Dsonar.javascript.lcov.reportPaths=coverage/lcov.info
                                        """
                                    }
                                    
                                    sh """
                                        docker build -t ${DOCKER_REGISTRY}/frontend:latest .
                                        docker push ${DOCKER_REGISTRY}/frontend:latest
                                    """
                                }
                            }
                        }
                    }
                }
            }
        }
        
        stage('CD Global - Déploiement') {
            steps {
                script {
                    // Générer ou mettre à jour docker-compose global
                    writeFile file: 'docker-compose-prod.yml', text: """
version: '3.8'
services:
  evaluation:
    image: ${DOCKER_REGISTRY}/evaluation-service:latest
    ports:
      - "3001:3000"
    networks:
      - app-network
      
  recompense:
    image: ${DOCKER_REGISTRY}/recompense-service:latest
    ports:
      - "3002:3000"
    networks:
      - app-network
      
  frontend:
    image: ${DOCKER_REGISTRY}/frontend:latest
    ports:
      - "80:80"
    networks:
      - app-network
      - frontend-network
      
networks:
  app-network:
    driver: bridge
  frontend-network:
    driver: bridge
"""
                    
                    // Déploiement sur le serveur cible
                    sh '''
                        scp docker-compose-prod.yml user@your-server:~/app/
                        ssh user@your-server "cd ~/app && docker-compose -f docker-compose-prod.yml pull && docker-compose -f docker-compose-prod.yml up -d"
                    '''
                }
            }
        }
    }
    
    post {
        always {
            // Nettoyage
            cleanWs()
        }
        failure {
            // Notifications (Slack, Email, etc.)
            echo "Pipeline échoué ! Vérifiez les logs."
        }
        success {
            echo "✅ Tous les pipelines CI/CD sont réussis !"
        }
    }
}
