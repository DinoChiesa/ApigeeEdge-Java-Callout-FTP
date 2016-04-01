# Apigee Edge callout: FTP

This directory contains the Java source code for a simple Java callout that
works with Apigee Edge. It does one thing:   performs an FTP PUT. 

## Building

1. unpack (if you can read this, you've already done that).

2. Before building the first time, you must import the apigee jars into your local Maven cache.
   Do this with the [buildsetup.sh](buildsetup.sh) script:
   ```
   buildsetup.sh
   ```

3. ```mvn clean package```


Congratulations! You have built the Jar.


## Using this Custom Policy in a bundle

If you are using [the sample proxy](../bundle) included here, you don't need to do the first two steps; the sample already has the custom policy JAR present in the proxy. 


1. copy target/edge-custom-policy-ftp.jar to your apiproxy/resources/java directory
   also copy all the lib/*.jar files to the same directory.
   This pom file copies the appropriate jar to the resources/java directory for
   the sample proxy included here. 

2. Be sure to include a Java callout policy in your
   apiproxy/resources/policies directory. It should look like
   this:
    ```xml
    <JavaCallout name="Java-FtpPut" >
      <DisplayName>Java Ftp Put</DisplayName>
      <Properties>
        <Property name='debug'>true</Property>
        <Property name='ftp-server'>{settings_ftp_server}</Property>
        <Property name='ftp-port'>21</Property>
        <Property name='ftp-user'>{settings_ftp_user}</Property>
        <Property name='ftp-password'>{settings_ftp_password}</Property>
        <Property name='remote-file-name'>{request.queryparam.remote-file-name}</Property>
        <Property name='want-base64-decode'>true</Property>
        <Property name='initial-directory'>{request.queryparam.initial-directory}</Property>
        <Property name='want-base64-decode'>false</Property>
      </Properties>
      <ClassName>com.dinochiesa.edgecallouts.FtpPut</ClassName>
      <ResourceURL>java://edge-custom-policy-ftp.jar</ResourceURL>
    </JavaCallout>
   ```
   
3. Import and deploy the API proxy bundle into your Edge organization with a tool like [pushapi](https://github.com/carloseberhardt/apiploy) or [apigeetool](https://github.com/apigee/apigeetool-node).

4. invoke the API proxy.



## Dependencies

Jars available in Edge: 
* Apigee Edge expressions v1.0
* Apigee Edge message-flow v1.0
* Apache commons lang v2.6 - for ExceptionUtils
* Jackson v1.9.7 - for JSON serialization
* Apache commons net v3.3 - for Ftp classes

All these jars must be available on the classpath for the compile to
succeed. The first four are available within Apigee Edge, so there's no need to
include them when you use the callout jar in an Edge proxy. The last one needs to be included with your API Proxy.


## Notes:

To run tests, you must specify settings for the FTP server in the
[default-values.json](src/test/resources/default-values.json) file. 


