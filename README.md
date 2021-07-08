# Formulation of the problem

It is necessary to implement a web service that accepts a field of activity (for example, a cinema, cafe, bowling, etc.) and searches for the most popular company in each of the cities: Novosibirsk, Omsk, Tomsk, Kemerovo and Novokuznetsk. The result should be a JSON document with a list of found companies, sorted by their rating on Flamp. For each company, the following data must be present: name, address, Flamp rating.

# Implementation comments

Builds are done by running the "web-service" build configuration in the IntelliJ IDEA, or by running the "mvn exec: java" Maven command. Service requests are made after deployment on a local machine at localhost: 4567 / bestDepartments / QUERY, where QUERY is replaced with the name of the desired field of activity for which the client wants to search.
