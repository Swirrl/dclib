dclib
=====

Data Conversion library for generating RDF from reasonably formatted CSV files. Used as part of the stack for data publishing utilities.

# Installation

- `mvn package` builds the command line jar in `target/dclib-<<VERSION>>-run.jar` and a library jar at `target/dclib-<<VERSION>>.jar`

# Deployment 

`mvn deploy` will deploy to `s3://swirrl-jars` using [AWS Maven](https://github.com/spring-projects/aws-maven)
