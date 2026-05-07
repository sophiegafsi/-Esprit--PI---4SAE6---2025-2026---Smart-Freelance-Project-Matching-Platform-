pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "sofie2002/gestion-planning:latest"
    }

    stages {

        stage('Clone') {
            steps {
                git branch: 'soufia-planning',
                url: 'https://github.com/sophiegafsi/-Esprit--PI---4SAE6---2025-2026---Smart-Freelance-Project-Matching-Platform-.git'
            }
        }

        stage('Clean & Compile') {
            steps {
                dir('gestion-planing') {
                    sh 'mvn clean compile'
                }
            }
        }

        stage('Unit Tests') {
            steps {
                dir('gestion-planing') {
                    sh 'mvn test'
                }
            }

            post {
                always {
                    junit 'gestion-planing/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                dir('gestion-planing') {
                    sh 'mvn package -DskipTests'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('gestion-planing') {
                    withSonarQubeEnv('SonarQube') {
                        sh 'mvn sonar:sonar -Dsonar.projectKey=gestion-planning'
                    }
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                dir('gestion-planing') {

                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )]) {

                        sh '''
                        docker build -t $DOCKER_IMAGE .

                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin

                        docker push $DOCKER_IMAGE
                        '''
                    }
                }
            }
        }

        stage('Deploy Container') {
            steps {
                sh '''
                docker rm -f gestion-planning-app || true

                docker run -d \
                --name gestion-planning-app \
                --network freelink-network \
                -p 8086:8086 \
                -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql-planning:3306/gestion_planning?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC \
                -e SPRING_DATASOURCE_USERNAME=root \
                -e SPRING_DATASOURCE_PASSWORD=root \
                -e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
                -e SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.MySQLDialect \
                -e EUREKA_CLIENT_ENABLED=false \
                sofie2002/gestion-planning:latest
                '''
            }
        }
    }
}
