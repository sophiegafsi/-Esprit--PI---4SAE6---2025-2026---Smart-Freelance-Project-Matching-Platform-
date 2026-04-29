pipeline {
    agent any

    environment {
        SONAR_TOKEN = credentials('sonar-token')
        SONAR_HOST_URL = 'http://sonarqube:9000'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'chmod +x mvnw || true'
                sh './mvnw clean verify'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                sh '''
                ./mvnw sonar:sonar \
                -Dsonar.host.url=$SONAR_HOST_URL \
                -Dsonar.login=$SONAR_TOKEN \
                -Dsonar.projectKey=projet-service
                '''
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t projet-service:latest .'
            }
        }
    }
}
