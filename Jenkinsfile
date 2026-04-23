pipeline {
  agent any

  options {
    timestamps()
  }

  parameters {
    booleanParam(name: 'RUN_SONAR', defaultValue: false, description: 'Run SonarQube analysis.')
    string(name: 'SONAR_HOST_URL', defaultValue: 'http://localhost:9000', description: 'SonarQube server URL.')
  }

  stages {
    stage('Test and Package') {
      steps {
        script {
          if (isUnix()) {
            sh 'chmod +x mvnw && ./mvnw -B clean verify'
          } else {
            bat 'mvnw.cmd -B clean verify'
          }
        }
      }
    }

    stage('SonarQube') {
      when {
        expression { return params.RUN_SONAR }
      }
      steps {
        withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
          script {
            if (isUnix()) {
              sh "chmod +x mvnw && ./mvnw -B sonar:sonar -Dsonar.projectKey=pidev-skills-service -Dsonar.host.url=${params.SONAR_HOST_URL} -Dsonar.token=\$SONAR_TOKEN"
            } else {
              bat "mvnw.cmd -B sonar:sonar -Dsonar.projectKey=pidev-skills-service -Dsonar.host.url=${params.SONAR_HOST_URL} -Dsonar.token=%SONAR_TOKEN%"
            }
          }
        }
      }
    }
  }

  post {
    always {
      junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
      archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
    }
  }
}