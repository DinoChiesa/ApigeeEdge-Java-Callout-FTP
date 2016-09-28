# Java FTP custom policy

This directory contains the Java source code and pom.xml file required to
compile a simple custom policy for Apigee Edge. The policy performs FTP operations. 

It uses Apache Commons Net for the FTP and Apache Commons Codec for the
base64 decoding. These jars need to be present in the final jar in order
for this callout to work at runtime.

This code also uses Codehaus' Jackson library for Json parsing. This one
is included in the classpath of Apigee callouts by default, so you don't
need to include it in the uploaded package.

## License

This material is copyright 2015, 2016 Apigee Corporation. 
and is licensed under the Apache 2.0 license. See the [LICENSE](LICENSE) file. 


## Using the Policy

The policy is usable as-is, without building or compiling. All you need to do is include the appropriate Java JAR into your Apigee Edge proxy definition, and configure the policy with the appropriate settings. 

### Example Configuration

```xml
<JavaCallout name="Java-FtpCallout">
  <DisplayName>Java FTP</DisplayName>
  <Properties>
    <Property name='debug'>true</Property>
    <Property name='ftp-server'>{ftp-server}</Property>
    <Property name='ftp-port'>21</Property>
    <Property name='ftp-user'>{ftp-user}</Property>
    <Property name='ftp-password'>{ftp-password}</Property>
    <Property name='initial-directory'>{initial-directory}</Property>
    <Property name='remote-file-name'>{remote-file-name}</Property>
    <Property name='source-variable'>content-to-upload</Property>
    <Property name='want-base64-decode'>false</Property>
  </Properties>
  <ClassName>com.dinochiesa.edgecallouts.FtpPut</ClassName>
  <ResourceURL>java://edge-custom-policy-ftp.jar</ResourceURL>
</JavaCallout>
```

The above configuration tells the policy to find the server name in a context variable 'ftp-server'.
Similarly for the user name, password, initial directory on the server, and the remote file name.
The source-variable property specifies the context variable name that holds the data to drop into the file. If you do not specify a property with the name='source-variable', the policy will use the message content.

Finally, the policy can perform base64 decoding of the message content before uploading. If you want this, set the want-base64-decode to true. It defaults to false. 



## Building the JAR File

The build requires Apache maven.


1. ```mvn clean package```

2. The package step will copy the appropriate JAR file to the bundle/apiproxy/resources/java directory included in this repo. If you have a separate proxy, you will need to copy the JAR manually. 



## Bugs

There is no FTP GET.
