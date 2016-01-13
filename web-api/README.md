## Using Sendgrid's Web API on HCP - example##

This example is a Maven application that you can simply build and then deploy on the HANA Cloud Platform. It allows you to compose emails via a web interface and send them via [Sendgrid](https://sendgrid.com/) using [Sendgrid's APIs](https://sendgrid.com/docs/API_Reference).

### Usage ###

To run, first of all you need to sign up with Sendgrid (if you haven't done so already). Once you have a Sendgrid account, you are going to need to generate an API Key from the Sendgrid dashboard. Copy API Key's value in the properties file found here: `/src/main/webapp/WEB-INF/lib/sendgrid.properties`. In the same properties file, please fill in the missing information (username, password, email accounts for incoming email notifications and for email events notifications) for the application to work correctly.

Also, to demonstrate features like templates and substitutions, please create a few email templates in your Sendgrid account. Please use also the following substitution variables where possible: *:first_name* and *:order_number*.

Next, on your HANA Cloud Platform account, you will need to create two destinations named SendgridAPI pointing to Sendgrid's APIs endpoint and SendgridAPI_v3 pointing to Sendgrid's v3 API endpoint. For convenience, import the destinations found in this folder: `/src/main/resources/`. 

Use Maven to build the project and then deploy it on HCP.

#### Use Case 1 - Sending emails ####
Once it is started, access the application's URL and you should be able to see a simple form. Enter the **From**, **To**, **Subject** and **Mail Text** fields, then press send. Your email should've been sent via Sengrid using their web API.

To test the templates API and substitutions, use the **Choose Template** dropdown. Once you select a template, its text and name will populate the appropriate label and the **Mail Text** field. If you've used the *:first_name* substitution variable, then in the **To** field please enter an address such as 

> `Somebody's Name <somebodysaddress@somedomain.sometld>`

In the received email the substitution variable should've been replaced with "*Somebody's Name*". 

Similarly, the *:order_number* variable should be replaced with a randomly generated order number.

#### Use Case 2 - Email events notifications ####
Go to your **Sendgrid dashboard->Settings->Mail Settings->Event Notifications**. Configure the HTTP Post URL to point to `https://<your_hcp_app_url>/mailevent`. From the events list, select "Dropped" and "Bounced". This means that we've configured our Sendgrid account to post a notification to the indicated URL (which is one of our app's servlets) whenever a sent email is dropped or bounced.

Now, return to the web ui and send an email to "fiagdiaufga97823ryei@ffkdsauioh78h.iysdf" (or to any random unexisting email address). The email will be obviously dropped, but you will receive an email at the address specified in the **sendgrid.properties** file, in the EMAIL_EVENT_TO field.

#### Use Case 3 - Inbound email parse ####
Through this use case we demonstrate the usage of Sendgrid APIs on incoming emails.
For that, you have to go to follow the [setup steps](https://sendgrid.com/docs/API_Reference/Webhooks/parse.html) described by Sendgrid, the URL webhook being `https://<your_hcp_app_url>/inbound`.

Once you are done, simply send (using any email client) an email to an address belonging to your domain. Since we parse the subject, try adding one of the following keywords in there: *fw:, ref:, support, error, question, order*. 

You will receive an email at the address speficied in the **sendgrid.properties** file, in the INBOUND_TO field. The email will contain information about the incoming email, and it will have had it categorized already based on its subject. This way, we've demonstrated how Sendgrid's inbound parse webhook can be used to route the emails appropriately (of course, more advanced routing can be put in place, but the present code just showcases the feature).
