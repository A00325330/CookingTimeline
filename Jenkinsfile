pipeline {
    agent {
        docker {
            image 'maven:3.9.4-eclipse-temurin-17'
        }
    }

    environment {
        SONARQUBE_ENV = 'My SonarQube Server'
        SONAR_TOKEN = credentials('sonar-token') // Make sure this matches your Jenkins credentials ID
    }

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/A00325330/CookingTimeline.git', branch: 'develop'
            }
        }

        stage('Build') {
            steps {
                dir('Individual_proj') {
                    sh 'mvn clean install -DskipTests'
                }
            }
        }

        stage('Test') {
            steps {
                dir('Individual_proj') {
                    sh 'mvn test'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir('Individual_proj') {
                    sh '''
                        mvn clean verify sonar:sonar \
                        -Dsonar.projectKey=CookingTime \
                        -Dsonar.projectName="CookingTime" \
                        -Dsonar.host.url=http://host.docker.internal:9000 \
                        -Dsonar.token=${SONAR_TOKEN}
                    '''
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'export DOCKER_BUILDKIT=0 && docker build -t cooking-timeline:latest ./Individual_proj'
            }
        }
    }
}
