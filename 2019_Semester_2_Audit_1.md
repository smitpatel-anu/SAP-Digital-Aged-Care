# 2019 Semester 2 Audit 1

## SAP Digital Aged Care

## Team Members
* Smit Patel (u6839284@anu.edu.au)
* Hala Alsouly (u5995105@anu.edu.au)
* Yuzhao Li (u6724153@anu.edu.au)
* Ziang Xu (u6645802@anu.edu.au)
* Yikai Sun (u6444614@anu.edu.au)
* Jinpei Chen (u6743886@anu.edu.au)
* Ning Cai (u6456964@anu.edu.au)

## Challenge and Story
‘Active’ Telemonitoring Pilot: Enabling Aged Care providers to monitor and respond to the health, activity, and requests of senior clients living independently in their homes. Using artificial intelligence on data from sensors, devices, and other applications to predict risk, mobilise the care team, and give visibility to the family.
## Overview of our two apps
* Android Wear app 2.0: Wearables can help understand a person’s wellbeing, predict health risks and detect falls. We need to supercharge our capabilities on the wearable device to collect and process the right data, whilst proving a great user experience. Primary features of the Android Wear app:
    * Sending data without a phone (using 4G) i.e. standalone app
    * GPS location tracking
    * Parkinson tremor monitoring
     * Improvements to device battery life
     * Fall detection
* “Where’s that document?” chatbot mini-app: Most elderly people do not use email. They receive countless documents via mail and generally can’t remember the vast amount of information they need to get by independently. We need to build a PoC app that can take a photo of a document and then:  
     * Use off the shelf OCR to scan and analyse the text and determine key information Use a cloud storage to store documents and text records
     * Use SAP Conversational AI (Chatbot tool) for an elderly person to ask the app “what’s my insurance number?” for example

## Stakeholders
* Simon Grace - Team leader (leads the digital aged care project at SAP)
* Abhinav Singhal - Founder and Venture Lead (works with Simon on digital aged care)
* Leon Ren - Head of Design and Engineering (manages software development, administers GitHub and JIRA)
* Cathy McGurk - Executive Manager (manages ANU's engagement with SAP)
* Alan Bradbury - Strategic Initiatives (works with various internal projects at SAP)

## Statement of Work
* A draft of the Statement of Work document prepared by team members is available [here](https://docs.google.com/document/d/1EQXTvbllQohvQxRmt6kTLMxnqgFR5qrLN83I9oHH72s/edit?usp=sharing)
* The client will be preparing a Statement of Work independently with additional information as discussed in a previous [meeting](https://drive.google.com/open?id=1RhMwgnwSnsN3cFKq1MBUeDXbxyeNDqcR)

## Development plan, schedule
* 10-week timeframe, beginning week 3 of the semester
    * Weeks 3 and 4 (2 weeks) - IP agreement and NDA, Statement of Work, requirements/user-stories, architecture/design, SAP’s private GitHub repo and environment setup
    * Weeks 5 to 10 (6 weeks) - development phase, prototyping
    * Weeks 11 and 12 (2 weeks) - unit, integration, and manual testing
* 2-week sprints managed by the client (Simon Grace) and Yuzhao Li
* Weekly conference calls with Simon to report progress, review/revise requirements, and discuss issues
* Deliverables: Android Wear app 2.0, “Where’s that document?” chatbot app (proof of concept), detailed architecture and code documentation

## Deliverables
* Android Wear app 2.0
* "Where's my document?" chatbot app (proof of concept)
* Architecture and design documentation
* Documentation regarding research and findings during development
* Code documentation, including in-line comments where applicable
* Manual testing documentation

## Communication
* Using Slack workspace
* Weekly conference calls with Simon
* Emails with other SAP Digital Aged Care team members

## Resources
* Using a private GitHub repository for development. Managed by the client
* Physical devices for testing including Android Wear device(s) and smartphone(s)
* Trello board(s) for Agile development. Managed by the client and Yuzhao Li
* Google Drive for storing project documents
* Zoom for audio/video conferencing
* Cloud storage service (tentative - SAP’s internal service or AWS)

## Risks, Potential Costs
* Physical device (Android Wear devices, smartphones) procurement by SAP
* Cloud storage costs (document storage)
* Communication issues due to a global team (members in Ireland, India, Australia)
* Getting access to SAP GitHub repository
* Licensing and approvals for third-party libraries (for OCR, cloud storage SDKs, etc.)
* [Risk register](https://docs.google.com/spreadsheets/d/1bk8ooR6tqfAQXfUt8OAQKlaZAj1Er4ufeNuLKyR5vz0/edit?usp=sharing)
