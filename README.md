# PLCnext Technology - Java RSC Demo - DEPRICATED

[![Feature Requests](https://img.shields.io/github/issues/PLCnext/JavaRSC_Example/feature-request.svg)](https://github.com/PLCnext/JavaRSC_Example/issues?q=is%3Aopen+is%3Aissue+label%3Afeature-request+sort%3Areactions-%2B1-desc)
[![Bugs](https://img.shields.io/github/issues/PLCnext/JavaRSC_Example/bug.svg)](https://github.com/PLCnext/JavaRSC_Example/issues?utf8=✓&q=is%3Aissue+is%3Aopen+label%3Abug)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Web](https://img.shields.io/badge/PLCnext-Website-blue.svg)](https://www.phoenixcontact.com/plcnext)
[![Community](https://img.shields.io/badge/PLCnext-Community-blue.svg)](https://www.plcnext-community.net)

> This project is depricated and will not be maintained anymore. Please use our gRPC services (https://www.plcnext.help/te/Service_Components/gRPC_Introduction.htm) to access PLCnext RSC services via Java. \
> There are a few Makers Blog posts with examples of how to use these gRPC services: \
> \
> •	[Command-line gRPC client](https://www.plcnext-community.net/makersblog/remote-grpc-using-grpcurl/) \
> •	[C# gRPC client](https://www.plcnext-community.net/makersblog/how-to-create-a-client-for-the-plcnext-control-grpc-server-in-c/) \
> •	[python gRPC client](https://www.plcnext-community.net/makersblog/grpc-python-read-and-write-process-data/) \
\
> It is possible to write a gRPC client in Java, and there are examples of this general concept on the gRPC website: https://grpc.io/docs/languages/java/basics/

## Introduction

This project is a GUI showcasing some of the functionalities exposed by PLCnext controllers, that can be used remotely. This is a simple eclipse project which can be interpreted by most Java IDEs. The only prerequisite is that you have a JDK java 10 or higher. Maybe the most useful feature of this app besides it´s demonstrational value is the ability to monitor and write to Global Data Space Variables on the PLC without using PLCnext Engineer. In case you want to read more about the functionality provided by this application, you can read the [community article](https://www.plcnext-community.net/en/hn-makers-blog/423-how-to-use-the-rsc-services-out-of-a-remote-java-application.html).

## Getting Started

To get this project running, clone this repository into a folder on your local machine and download the [PLCnext Java RSC API](https://www.phoenixcontact.com/qr/2404267/softw). The libraries in that package need to be placed into the libs folder like in the picture.

![libraries](libs/JavaRSC_Libraries.PNG)

Then Import the project into your Java IDE, build and run the application.
If you want to configure default credentials for the login screen, you can edit the config.txt file under src/main/resources.

If you need some more instructions on how to use this applications you might want to have a look at the community article linked above.

## Feedback

* Ask a question in our [Forum](https://www.plcnext-community.net/index.php?option=com_easydiscuss&view=categories&Itemid=221&lang=en).
* File a bug in [GitHub Issues](https://github.com/PLCnext/JavaRSC_Example/issues).

## License

Copyright (c) Phoenix Contact Gmbh & Co KG. All rights reserved.

Licensed under the [MIT](LICENSE) License.
