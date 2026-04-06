# CS 346 Group Project - Team 101-12

# HelloASL
The HelloASL app is a beginner-focused app that helps users recognize and practice common American Sign Language signs in real time using their camera. 

## Team Members
Name: Fangda Dai  Email: fdai@uwaterloo.ca

Name: Donghui Yu  Email: d9yu@uwaterloo.ca

Name: Tracy Hua   Email: t2hua@uwaterloo.ca 

Name: Mengdie Wu  Email: m283wu@uwaterloo.ca

## Acknowledgements

WLASL Dataset - https://dxli94.github.io/WLASL/
- Description: A large-scale video dataset for Word-Level American Sign Language (ASL) recognition.
- Contribution: Video samples from this dataset were used to create lesson materials and to support the ASL-to-English translation functionality in our application.

VideoMAE WLASL100 Model - https://huggingface.co/Shawon16/VideoMAE_Base_WLASL_100_200_epochs_p20_SR_8
- Description: An ASL video classification model based on the VideoMAE architecture, fine-tuned on the WLASL100 dataset for sign language recognition tasks.
- Contribution: Used to support the ASL recognition and translation functionality in our application.




# Project Information

This is our Wiki https://git.uwaterloo.ca/m283wu/cs-346-group-project/-/wikis/home.

Our https://git.uwaterloo.ca/m283wu/cs-346-group-project/-/wikis/Team-Contract includes details on how we will work together.

The https://git.uwaterloo.ca/m283wu/cs-346-group-project/-/wikis/Project-Proposal describes our project idea and anticipated features.

Our https://git.uwaterloo.ca/m283wu/cs-346-group-project/-/wikis/Meeting-Notes record the details of our meetings throughout the term.

The https://git.uwaterloo.ca/m283wu/cs-346-group-project/-/wikis/Team-Reflections reflect our team’s development process, challenges, decisions, and lessons learned throughout the project.

# User Guide

## Setup

### 1. Firebase Notification Setup

This app uses **Firebase Cloud Messaging (FCM)** to send push notifications on **Android**.

Note:
- Push notifications are supported on **Android only**.  
- Desktop builds do **not** receive notifications.

To-do:

1. Ask the project owner for the `google-services.json` file.

2. Place it here: `composeApp/google-services.json`

### 2. Connect to Supabase
To connect to the database, ask the project owner for `SUPABASE_URL` and `SUPABASE_ANON_KEY`, and add those to your local.properties file.

# Design Documents

This is our https://git.uwaterloo.ca/m283wu/cs-346-group-project/-/wikis/UML-ER-Diagram.

# Grading Instructions


