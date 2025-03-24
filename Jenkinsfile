pipeline {
    agent any

    environment {
        SONARQUBE_ENV = 'My SonarQube Server' // You’ll configure this in Jenkins later
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/A00325330/CookingTimeline.git', branch: 'develop'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${env.SONARQUBE_ENV}") {
                    sh 'mvn sonar:sonar'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t cooking-timeline:latest .'
            }
        }
    }
}
