## Using Sendgrid's Java Code Wrapper on HCP - example##

This example is a Maven application that you can simply build and then deploy on the HANA Cloud Platform. It allows you to compose emails via a web interface and send them via [Sendgrid](https://sendgrid.com/) using [Sendgrid's Java Library](https://github.com/sendgrid/sendgrid-java).

### Usage ###

To run, first of all you need to sign up with Sendgrid (if you haven't done so already). Once you have a Sendgrid account, you are going to need to generate an API Key from the Sendgrid dashboard. Copy API Key's value in the properties file found here: `/src/main/webapp/WEB-INF/lib/sendgrid.properties`.

Next, on your HANA Cloud Platform account, you will need to create a destination named SendgridAPI pointing to Sendgrid's APIs endpoint. For convenience, import the destination found here: `/src/main/resources/SendgridAPI_destination.properties`.

Use Maven to build the project and then deploy it on HCP.

Once it is started, access the application's URL and you should be able to see a simple form. Enter the **From**, **To**, **Subject** and **Mail Text** fields, then press send. Your email should've been sent via Sengrid using their java library.
