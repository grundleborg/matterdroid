MatterDroid
===========

**A native [Mattermost](http://mattermost.org) client for Android.**

This is an experimental *unofficial* fully native Android client for Mattermost written entirely in
Java and using all the latest and greatest Android libraries. At the moment, the feature set is 
quite limited, but it does the basic job of letting you see messages in channels and send them too.

Motivation
----------

This started out as a personal experiment to learn about the latest Android libraries that have come
onto the scene or evolved substantially since my last major Android project a couple of years ago.
It turned out to be rather fun, and I got a bit carried away adding features to it, so now it's up
on Github in case it's of interest to anyone else.

I'm still working on it (and tackling that huge list of limitations) as time permits, as I'm still
learning a lot from it, but I'm also very happy to receive pull requests if you want to chip in too.

Features
--------

A very basic feature set is provided at the moment.

* User/Pass based authentication.
* Changing team.
* Access public channels, private groups and direct messages.
* Basic Markdown message rendering.
* Posting text/Markdown messages.

Limitations
-----------

There's a lot this app doesn't do yet, including:

* Reliable handling of internet connectivity loss.
* Creating users/teams/channels/groups/etc.
* Signing in to multiple teams at once.
* Full Markdown support when rendering messages.
* Auto-linking @mentions and !channels etc.
* Marking messages as read, and indicating channels with new messages.
* Posting images/attachments/etc.
* Reply threading.
* Message editing and deltion.
* Push notifications.
* Authentication schemes other than simple user/pass.
* 2FA.
* Probably more things I've forgotten...

Installation
------------

### Demo Build

You can install the demo build of this App from the
[Google Play Store](https://play.google.com/store/apps/details?id=me.gberg.matterdroid). To take it
for a spin, connect to the server `https://pre-release.mattermost.com` (you'll need to sign up at
that link in a browser first if you don't already have an account).

### Build Your Own

To build this app from source follow the steps below:

1. Clone this repo.
2. Use Android Studio to build the project and install it to your device.
3. Have fun.

Contributing
------------

I'm happy to receive pull requests to fix bugs and add new features. If you want to make big changes
for a new feature, please discuss the planned implementation through a Github Issue first to avoid
the risk of separately developed large changes being accidentally incompatible.


