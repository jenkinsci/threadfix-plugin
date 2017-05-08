#! groovy

timestamps {
	node('osx || linux') {
		stage('Checkout') {
			checkout scm
		}

		stage('Build') {
			def mavenHome = tool name: 'Maven 3.5.0', type: 'maven'
			withEnv(["PATH+MAVEN=${mavenHome}/bin"]) {
				sh 'mvn -s settings.xml clean install'
			}
			archiveArtifacts 'target/threadfix.hpi'
		}
	}
}
