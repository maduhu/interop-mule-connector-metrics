

# interop-mule-connector-metrics

Metrics Anypoint Connector

[Connector description including destination service or application with]

Mule supported versions
Examples:
Mule 3.4.x, 3.5.x
Mule 3.4.1

[Destination service or application name] supported versions
Example:
Oracle E-Business Suite 12.1 and above.

#Service or application supported modules
Example:
Oracle CRM
Oracle Financials
or 
Salesforce API v.24
Salesforce Metadata API

Contents:

- [Deployment](#deployment)
- [Configuration](#configuration)
- [API](#api)
- [Logging](#logging)
- [Tests](#tests)

## Deployment

Installation 

For beta connectors you can download the source code and build it with devkit to find it available on your local repository. Then you can add it to Studio

For released connectors you can download them from the update site in Anypoint Studio. 
Open Anypoint Studio, go to Help → Install New Software and select Anypoint Connectors Update Site where you’ll find all avaliable connectors.

Usage

For information about usage our documentation at http://github.com/mulesoft/metrics.

## Configuration

[pom.xml](./pom.xml) and [circle.yml](./circle.yml) can be found in the repo

## API

This repo does not have an API.

## Logging

Server path to logs is: <mule_home>/logs/*.log

Metrics details are logged based on configuration to console, csv and/or log files. Every app currently has metrics connector configured to record several metrics and then log these details, which include time taken for HTTP calls, average times and such similar statistics.

## Tests

Metrics tests are a bit different from other tests for interop projects as this is not a typical service or interop project. There are tests to test the functionality of each of the loggers such as csv or slf4j.

### Reporting Issues

We use GitHub:Issues for tracking issues with this connector. You can report new issues at this link http://github.com/mulesoft/metrics/issues.
