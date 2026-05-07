pipeline {
  agent any

  options {
    timestamps()
  }

  parameters {
    booleanParam(name: 'RUN_SONAR', defaultValue: false, description: 'Run SonarQube analysis.')
    booleanParam(name: 'RUN_DOCKER_LINT', defaultValue: true, description: 'Run Hadolint on the Dockerfile.')
    booleanParam(name: 'RUN_TRIVY', defaultValue: true, description: 'Run Trivy filesystem scan.')
    string(name: 'SONAR_HOST_URL', defaultValue: 'http://localhost:9000', description: 'SonarQube server URL.')
    string(name: 'TRIVY_SEVERITY', defaultValue: 'HIGH,CRITICAL', description: 'Trivy severities to report.')
  }

  stages {
    stage('Test') {
      steps {
        script {
          if (isUnix()) {
            sh 'chmod +x mvnw && ./mvnw -B clean test'
          } else {
            bat 'mvnw.cmd -B clean test'
          }
        }
      }
    }

    stage('Package') {
      steps {
        script {
          if (isUnix()) {
            sh './mvnw -B package -DskipTests'
          } else {
            bat 'mvnw.cmd -B package -DskipTests'
          }
        }
      }
    }

    stage('Dockerfile Lint') {
      when {
        allOf {
          expression { return params.RUN_DOCKER_LINT }
          expression { return fileExists('Dockerfile') }
        }
      }
      steps {
        script {
          if (isUnix()) {
            sh '''
              mkdir -p reports
              set +e
              docker run --rm -v "$PWD:/workspace" hadolint/hadolint hadolint /workspace/Dockerfile --format tty > reports/hadolint-report.txt 2>&1
              echo $? > reports/hadolint.exitcode
              exit 0
            '''
          } else {
            bat '''
              if not exist reports mkdir reports
              docker run --rm -v "%cd%:/workspace" hadolint/hadolint hadolint /workspace/Dockerfile --format tty > reports\\hadolint-report.txt 2>&1
              echo %ERRORLEVEL%> reports\\hadolint.exitcode
              exit /b 0
            '''
          }
        }
      }
    }

    stage('Trivy Scan') {
      when {
        expression { return params.RUN_TRIVY }
      }
      steps {
        script {
          if (isUnix()) {
            sh """
              mkdir -p reports
              set +e
              docker run --rm -v "\$PWD:/workspace" aquasec/trivy:latest fs --scanners vuln,secret,misconfig --severity ${params.TRIVY_SEVERITY} --no-progress --format table -o /workspace/reports/trivy-report.txt /workspace
              echo \$? > reports/trivy.exitcode
              exit 0
            """
          } else {
            bat """
              if not exist reports mkdir reports
              docker run --rm -v "%cd%:/workspace" aquasec/trivy:latest fs --scanners vuln,secret,misconfig --severity ${params.TRIVY_SEVERITY} --no-progress --format table -o /workspace/reports/trivy-report.txt /workspace
              echo %ERRORLEVEL%> reports\\trivy.exitcode
              exit /b 0
            """
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
      archiveArtifacts artifacts: 'target/*-SNAPSHOT.jar', fingerprint: true, allowEmptyArchive: true
      archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true
      emailext(
        subject: "Build ${env.JOB_NAME} #${env.BUILD_NUMBER} - ${currentBuild.currentResult}",
        body: """Build result: ${currentBuild.currentResult}

Job: ${env.JOB_NAME}
Build: #${env.BUILD_NUMBER}
URL: ${env.BUILD_URL}
""",
        to: 'fares.belgacem@esprit.tn'
      )
    }
  }
}
