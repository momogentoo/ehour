Authentication with Active Directory
===========================
Introduction
----------------
This fork provides limited support to authentication with Active Directory ("AD"). 

When configured properly, user authentication request is sent to Active Directory 
by Spring Security Framework's Authentication Manager.

Configuration is simple. This document will be a bit verbose to explain how it works.

Please read through this document before testing this feature.

Configuration
----------------
- Adjust Order of Authentication Providers

Current context-security.xml in eHour-wicketweb/src/main/resources/ as below:
```
	<sec:authentication-manager alias="authenticationManager">
		<sec:authentication-provider
			user-service-ref="authService">
			<sec:password-encoder ref="passwordEncoder">
				<sec:salt-source user-property="getSalt" />
			</sec:password-encoder>
		</sec:authentication-provider>
		<sec:authentication-provider ref="adAuthenticationProvider">
		</sec:authentication-provider>
	</sec:authentication-manager>
```	
Which means, username / password combination is verified against built-in User table first. 

To enable AD authentication, please switch order to make adAuthenticationProvider come first
```
	<sec:authentication-manager alias="authenticationManager">
		<sec:authentication-provider ref="adAuthenticationProvider">
		</sec:authentication-provider>
		<sec:authentication-provider
			user-service-ref="authService">
			<sec:password-encoder ref="passwordEncoder">
				<sec:salt-source user-property="getSalt" />
			</sec:password-encoder>
		</sec:authentication-provider>
	</sec:authentication-manager>
```	

- Set Active Directory Domain Controller

Set domain name and ldap connection string in context-security.xml

To use LDAP at port 389, provide connection string like ldap://your.domain.com

To use LDAPS at port 636 with SSL enabled, provide connection string like ldaps://your.domain.com:636

Make sure AD is working properly, especially for LDAPS which requires proper installation of certificate.

```
<bean id="adAuthenticationProvider"
		  class="org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider">
		<constructor-arg value="your.domain.com" />
		<constructor-arg value="ldaps://your.domain.com:636/" />
		<property name="userDetailsContextMapper" ref="ehourUserDetailsContextMapper"/>
		<property name="useAuthenticationRequestCredentials" value="true"/>
	</bean>
```

- Restart application and try to login with credentials on AD

How does it work
----------------
When user entered username and password on Login page, combination is authenticated 
by Spring Security Framework's Authentication Manager according to the configuration of 
"authentication-providers". 

If authenticated by AD successfully, eHour will retrieve information from both AD and builtin User table and combine them together 
to construct a User object for the web session.
If authenticated by AD unsuccessfully, eHour will authenticate by internal builtin User table.

Then, let's explain what information of a User object contains:
- From original eHour release

  Unique User Id, Last Name, First Name, User Name (Login Name), E-Mail, Department, User Roles
  
- Exclusive from this fork

  Country Code (2-digit like US, CA), Flag to send weekly email reminder, Last eHour Login Time, Last Password Change Time

If available, the following information from AD will override the values from built-in table.
- Country Code from LDAP attribute 'c'
- E-Mail from LDAP attribute 'mail'
- Last Password Change Time from LDAP attribute 'pwdlastset' (Note: assume this value is stored as a large integer that represents 
the number of 100 nanosecond intervals since January 1, 1601 (UTC) (https://msdn.microsoft.com/en-us/library/windows/desktop/ms679430%28v=vs.85%29.aspx?f=255&MSPPError=-2147217396))

So that, it means - the user must be created in built-in table first as information like unique User Id, Department and User Roles 
are NOT retrieved from AD/LDAP but from internal database User table. Password of a new user SHOULD be left unset.

**If a user exists on AD but not created inside eHour system (builtin User table), login will fail.**

**After initial login by builtin Admin account, create a user that exists on AD and set it as Administrator of eHour.**

**Automatically adding new user that is authenticated by AD into eHour is PLANNED.**





