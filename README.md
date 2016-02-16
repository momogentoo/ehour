Introduction
===================

[eHour](https://github.com/te-con/ehour) is an open source webbased time tracking tool for companies and organizations who need accurate information on how much time is spend on projects.

eHour makes the amount of time your people spend on projects visible and available as simple and user friendly as possible.

This repository is a fork based on the [original author](https://github.com/te-con)'s eHour 1.4.3 RELEASE, with additional features, customization and bug fixes, mostly to meet requirements of internal users. Will keep this repository updated.

New Issues/Requests are always welcome.


License
-------------

Source codes are released under GPL v2.0 as per the license of eHour (https://ehour.nl/about/license.phtml).

Highlights of Changes
-------------
 - Support Active Directory Authentication
 - New Detailed Matrix Style Time Sheet Report
 
   See demo/timesheet_matrix.xlsx
 - Holiday / Special Working Days Calendar
 
   Support display of holidays and special working days in Calendar and Monthly Overview for multiple countries
   
   See demo/calendar_holidays.png
 - User Password Complexity Policy
   
   Password must contain at least 1 digit (0-9), 1 alphabet (a-z, A-Z) and 1 special character (~!@#$%^&+=). No spaces allowed. Min length is 8
 - Record and Display User Last Login Time and Last Password Change Time
   
   Two time-stamps are recorded and stored in database. Will display on User Management Page.
   
   See demo/user_profile.png

 - Allow to opt out email reminder for selected users
  
 Boss may need an account but won't enter hours. Weekly email reminder is unnecessary. Uncheck it in user profile editing

 - Several Other Changes   
   -- Modified parent pom.xml will make download of most of dependencies directly from official Maven repository. Only 1 charting module will still download from original author's repository.

   -- Fix an interal error on weekly timesheet panel, caused by saving timesheet entries in a different persistence context that used for querying. (org.hibernate.NonUniqueObjectException exception)



Authenticate with Active Directory
------------
See explanation in ACTIVE_DIRECTORY.md

Limitation
-------------
 - Update resource files for US/English only
 - Update database script for MySQL only
 - Testing. Every change is tested manually. Tried best but may skip updating test cases for limit of time.


Building from Source
------------
 - See BUILD.md
 

Prebuilt WAR Package
------------
Need a prebuilt WAR Package? Please create a request in Issues. Thank you.

   
***More Details to be entered***
