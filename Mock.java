Hi @Parmar, Chirag,

Recently, we have started working on the FFAPI migration from CADP pipeline to EDP, as the CADP instance is going to be retired by the end of this year.

While gathering the required information, we have noticed a few of your updates on the Confluence page and in the commits within the CADP deployment repositories.

References:
	1.	FFAPI CADP Pipelines Deployment: Confluence Link￼
	2.	CADP Deployment Script Repository: Code Repo Link￼
	3.	FFAPI Repository: GitHub Link￼
	4.	FFAPI-CADP-CONFIG Repository: GitHub Link￼

Questions:
	1.	Based on this page￼, it seems we need to supply the Nexus URL for the .zip file to the CADP pipeline to deploy it. How is the .zip file being pushed to Nexus — is it a manual upload? Because I don’t see any previous EDP CI job in release.td.com￼ or any CI.yml file in the project root folder.
	2.	What’s the role of the ffapi-cadp-config repository here? After checking that repo, I see it contains most of the property files for different environments, but I’m not sure how it comes into play during deployment.

During my POC on migration, I found one sample .NET repo that I’m using as a base to understand the .NET + CI.yml setup: dotnet-sample-app￼.